package security;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;
import model.*;
import org.apache.catalina.Context;
import org.apache.catalina.Session;
import org.apache.catalina.startup.Tomcat;
import org.h2.tools.RunScript;
import org.junit.*;
import static org.junit.Assert.*;

/** Real JSP login plus role filters; all accounts live in a temporary test database. */
public class LoginFlowTest {
    private static Tomcat server;
    private static Context context;
    private static Connection fixture;
    private static String root;
    private static final String[] KEYS={"DB_URL","DB_USER","DB_PASSWORD"};
    private static final String[] PREVIOUS=new String[3];
    private String cookie;

    @BeforeClass public static void start() throws Exception {
        for(int i=0;i<KEYS.length;i++)PREVIOUS[i]=System.getProperty(KEYS[i]);
        String url="jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL";
        System.setProperty("DB_URL",url);System.setProperty("DB_USER","sa");System.setProperty("DB_PASSWORD","");
        fixture=DriverManager.getConnection(url,"sa","");
        try(Reader schema=Files.newBufferedReader(Paths.get("database/01-schema.sql"),StandardCharsets.UTF_8)){RunScript.execute(fixture,schema);}
        addMember(1,"reader_demo","Nguyễn Văn A",PasswordHash4.hash("Reader@123"));
        addMember(2,"staff_demo","Trần Thị B",PasswordHash4.hash("Staff@123"));
        addMember(3,"manager_demo","Lê Văn C",PasswordHash4.hash("Manager@123"));
        addMember(4,"disabled","Disabled","!DEMO_LOGIN_DISABLED!");
        try(Statement s=fixture.createStatement()){
            s.executeUpdate("INSERT INTO tblReader4 VALUES (1,CURRENT_DATE,'ACTIVE'),(4,CURRENT_DATE,'ACTIVE')");
            s.executeUpdate("INSERT INTO tblEmployee4 VALUES (2,'Thủ thư'),(3,'Quản lý')");
            s.executeUpdate("INSERT INTO tblLibraryStaff4 VALUES (2)");s.executeUpdate("INSERT INTO tblManagementStaff4 VALUES (3)");
        }
        server=new Tomcat();server.setBaseDir(new File("target/login-flow-tomcat").getAbsolutePath());
        server.setPort(0);server.getConnector().setProperty("address","127.0.0.1");
        context=server.addWebapp("/libman",new File("src/main/webapp").getAbsolutePath());
        context.setParentClassLoader(LoginFlowTest.class.getClassLoader());server.start();
        root="http://127.0.0.1:"+server.getConnector().getLocalPort()+"/libman/";
    }
    private static void addMember(int id,String username,String name,String hash)throws Exception{
        try(PreparedStatement s=fixture.prepareStatement("INSERT INTO tblMember4 (id,username,password,fullname) VALUES (?,?,?,?)")){
            s.setInt(1,id);s.setString(2,username);s.setString(3,hash);s.setString(4,name);s.executeUpdate();}
    }
    private static final class Response { int status;String body,location; }
    private Response request(String path,String body)throws Exception{
        HttpURLConnection c=(HttpURLConnection)new URL(root+path).openConnection();
        c.setInstanceFollowRedirects(false);c.setConnectTimeout(5000);c.setReadTimeout(30000);
        if(cookie!=null)c.setRequestProperty("Cookie",cookie);
        try{
            if(body!=null){c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
                try(OutputStream out=c.getOutputStream()){out.write(body.getBytes(StandardCharsets.UTF_8));}}
            Response r=new Response();r.status=c.getResponseCode();r.location=c.getHeaderField("Location");
            String setCookie=c.getHeaderField("Set-Cookie");
            if(setCookie!=null){Matcher m=Pattern.compile("JSESSIONID=([^;]+)").matcher(setCookie);if(m.find())cookie="JSESSIONID="+m.group(1);}
            InputStream stream=r.status>=400?c.getErrorStream():c.getInputStream();
            if(stream==null)r.body="";else try(InputStream in=stream;ByteArrayOutputStream out=new ByteArrayOutputStream()){
                byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);r.body=new String(out.toByteArray(),StandardCharsets.UTF_8);}
            return r;
        }finally{c.disconnect();}
    }
    private static String token(String html){Matcher m=Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"").matcher(html);assertTrue(m.find());return m.group(1);}
    private Response login(String username,String password)throws Exception{
        String csrf=token(request("view/member/LoginView4.jsp",null).body);
        return request("view/member/LoginView4.jsp","csrf="+csrf+"&username="+URLEncoder.encode(username,"UTF-8")+"&password="+URLEncoder.encode(password,"UTF-8"));
    }

    @Test public void signsInEachRoleRotatesSessionAndBlocksOtherRoles()throws Exception{
        String[][] users={{"reader_demo","Reader@123","reader/ReaderHomeView4.jsp","Nguyễn Văn A"},
                {"staff_demo","Staff@123","libraryStaff/LibraryStaffHomeView4.jsp","Trần Thị B"},
                {"manager_demo","Manager@123","managementStaff/ManagementStaffHomeView4.jsp","Trang nhân viên quản lý"}};
        for(String[] user:users){
            cookie=null;Response form=request("view/member/LoginView4.jsp",null);String oldCookie=cookie;
            if("reader_demo".equals(user[0])){
                String css=new File("src/main/webapp/assets/member.css").toURI().toString();
                Files.write(Paths.get("target/login-preview.html"),form.body.replace("../../assets/member.css",css).getBytes(StandardCharsets.UTF_8));
            }
            Response logged=request("view/member/LoginView4.jsp","csrf="+token(form.body)+"&username="+user[0]+"&password="+URLEncoder.encode(user[1],"UTF-8"));
            assertEquals(302,logged.status);assertEquals("/libman/view/"+user[2],logged.location);assertNotEquals(oldCookie,cookie);
            Session session=context.getManager().findSession(cookie.substring("JSESSIONID=".length()));
            Member4 member=(Member4)session.getSession().getAttribute("currentMember");assertNull(member.getPassword());
            Response home=request("view/"+user[2],null);assertEquals(200,home.status);assertTrue(home.body.contains(user[3]));
            for(String[] other:users)if(other!=user)assertEquals(403,request("view/"+other[2],null).status);
        }
    }

    @Test public void rejectsWrongUnknownInactiveAndDisabledCredentials()throws Exception{
        assertEquals(200,login("reader_demo","wrong").status);
        assertTrue(request("view/member/LoginView4.jsp",null).body.contains("Tên đăng nhập:"));
        assertEquals(200,login("' OR 1=1 --","Reader@123").status);
        assertEquals(200,login("disabled","!DEMO_LOGIN_DISABLED!").status);
        try(Statement s=fixture.createStatement()){s.executeUpdate("UPDATE tblReader4 SET status='INACTIVE' WHERE id=1");}
        try{assertEquals(200,login("reader_demo","Reader@123").status);}
        finally{try(Statement s=fixture.createStatement()){s.executeUpdate("UPDATE tblReader4 SET status='ACTIVE' WHERE id=1");}}
        assertEquals(302,request("view/reader/DocumentSearchView4.jsp",null).status);
    }

    @Test public void protectsDirectUrlsAndRejectsForgedLoginForm()throws Exception{
        for(String path:new String[]{"reader/ReaderHomeView4.jsp","reader/DocumentSearchView4.jsp","reader/DocumentDetailView4.jsp?documentId=1",
                "libraryStaff/LibraryStaffHomeView4.jsp","libraryStaff/LoanSlipView4.jsp","libraryStaff/LoanSlipPrintView4.jsp?loanSlipId=1","managementStaff/ManagementStaffHomeView4.jsp"}){
            Response r=request("view/"+path,null);assertEquals(path,302,r.status);assertEquals("/libman/view/member/LoginView4.jsp",r.location);
        }
        request("view/member/LoginView4.jsp",null);
        assertEquals(400,request("view/member/LoginView4.jsp","csrf=wrong&username=staff_demo&password=Staff%40123").status);
        Response rootResponse=request("",null);assertEquals(302,rootResponse.status);
        assertEquals("/libman/view/member/LoginView4.jsp",rootResponse.location);
    }

    @Test public void logoutInvalidatesSessionAndExpiresOldAccess()throws Exception{
        assertEquals(302,login("staff_demo","Staff@123").status);
        String oldCookie=cookie;Response form=request("view/member/LogoutView4.jsp",null);assertEquals(200,form.status);
        assertEquals(403,request("view/member/LogoutView4.jsp","csrf=wrong").status);
        assertEquals(200,request("view/libraryStaff/LibraryStaffHomeView4.jsp",null).status);
        assertEquals(302,request("view/member/LogoutView4.jsp","csrf="+token(form.body)).status);
        cookie=oldCookie;assertEquals(302,request("view/libraryStaff/LoanSlipView4.jsp",null).status);
    }

    @Test public void reopeningLoginAllowsSwitchingAccountsAndClearsThePreviousRole()throws Exception{
        assertEquals(302,login("staff_demo","Staff@123").status);
        String staffCookie=cookie;
        Response rootResponse=request("",null);assertEquals(302,rootResponse.status);
        Response reopened=request("view/member/LoginView4.jsp",null);
        assertEquals(200,reopened.status);assertTrue(reopened.body.contains("id=\"username\""));
        Response switched=request("view/member/LoginView4.jsp","csrf="+token(reopened.body)+"&username=reader_demo&password=Reader%40123");
        assertEquals(302,switched.status);assertEquals("/libman/view/reader/ReaderHomeView4.jsp",switched.location);
        assertNotEquals(staffCookie,cookie);
        assertEquals(200,request("view/reader/ReaderHomeView4.jsp",null).status);
        assertEquals(403,request("view/libraryStaff/LoanSlipView4.jsp",null).status);
        Session readerSession=context.getManager().findSession(cookie.substring("JSESSIONID=".length()));
        assertNull(readerSession.getSession().getAttribute("currentStaff"));
        assertTrue(readerSession.getSession().getAttribute("currentReader") instanceof Reader4);
        cookie=staffCookie;
        assertEquals(302,request("view/libraryStaff/LoanSlipView4.jsp",null).status);
    }

    @Test public void hashesHaveIndependentSaltsAndRejectMalformedValues(){
        String first=PasswordHash4.hash("test password"),second=PasswordHash4.hash("test password");
        assertNotEquals(first,second);assertTrue(PasswordHash4.verify("test password",first));
        assertFalse(PasswordHash4.verify("wrong",first));assertFalse(PasswordHash4.verify("test password","test password"));
        assertFalse(PasswordHash4.verify("test password","pbkdf2-sha256$999999999$a$b"));
        assertFalse(PasswordHash4.verify("test password","pbkdf2-sha256$600000$broken$broken"));
    }

    @AfterClass public static void stop()throws Exception{
        try{if(server!=null){server.stop();server.destroy();}if(fixture!=null)fixture.close();}
        finally{for(int i=0;i<KEYS.length;i++){if(PREVIOUS[i]==null)System.clearProperty(KEYS[i]);else System.setProperty(KEYS[i],PREVIOUS[i]);}}
    }
}
