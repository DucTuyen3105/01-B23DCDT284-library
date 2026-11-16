package view;

import dao.*;
import model.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import org.apache.catalina.Context;
import org.apache.catalina.Session;
import org.apache.catalina.startup.Tomcat;
import org.h2.tools.RunScript;
import org.junit.*;
import static org.junit.Assert.*;

/** Exercises the actual module 2 JSP/DAO flow with independent database fixtures. */
public class Module2FlowTest {
    private static Tomcat server;
    private static Context context;
    private static Connection fixture;
    private static String root;
    private static final String[] KEYS = {"DB_URL", "DB_USER", "DB_PASSWORD"};
    private static final String[] PREVIOUS = new String[3];
    private String cookie, csrf;

    @BeforeClass public static void start() throws Exception {
        for (int i=0;i<KEYS.length;i++) PREVIOUS[i]=System.getProperty(KEYS[i]);
        String url="jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;LOCK_TIMEOUT=10000";
        System.setProperty("DB_URL",url); System.setProperty("DB_USER","sa"); System.setProperty("DB_PASSWORD","");
        fixture=DriverManager.getConnection(url,"sa","");
        try (Reader schema=Files.newBufferedReader(Paths.get("database/01-schema.sql"),StandardCharsets.UTF_8)) {
            RunScript.execute(fixture,schema);
        }
        sql("INSERT INTO tblMember4 (id,username,password,fullname) VALUES (1,'reader','DISABLED','Nguyễn Văn A'),"
                +"(10,'staff','DISABLED','Trần Thị B'),(11,'other','DISABLED','Nhân viên khác')");
        sql("INSERT INTO tblReader4 VALUES (1,CURRENT_DATE,'ACTIVE')");
        sql("INSERT INTO tblEmployee4 VALUES (10,'Thủ thư'),(11,'Thủ thư')");
        sql("INSERT INTO tblLibraryStaff4 VALUES (10),(11)");
        sql("INSERT INTO tblReaderCard4 VALUES (100,DATE '2025-01-01',DATE '2035-01-01','ACTIVE',1),"
                +"(101,DATE '2024-01-01',DATE '2025-01-01','EXPIRED',1)");
        sql("INSERT INTO tblDocument4 (id,name,category,author,publisher,quantity,status) VALUES "
                +"(1,'Đắc Nhân Tâm','Kỹ năng sống','Dale Carnegie','NXB Tổng hợp TP.HCM',2,'AVAILABLE'),"
                +"(2,'Java','Giáo trình','Tác giả','NXB',1,'AVAILABLE'),(3,'Hết sách','Sách','Tác giả','NXB',0,'UNAVAILABLE')");
        server=new Tomcat(); server.setBaseDir(new File("target/module2-flow-tomcat").getAbsolutePath());
        server.setPort(0); server.getConnector().setProperty("address","127.0.0.1");
        context=server.addWebapp("/libman",new File("src/main/webapp").getAbsolutePath());
        context.setParentClassLoader(Module2FlowTest.class.getClassLoader()); server.start();
        root="http://127.0.0.1:"+server.getConnector().getLocalPort()+"/libman/view/libraryStaff/";
    }

    @Before public void reset() throws Exception {
        sql("DELETE FROM tblDetailLoanSlip4"); sql("DELETE FROM tblLoanSlip4");
        sql("UPDATE tblDocument4 SET quantity=2,name='Đắc Nhân Tâm' WHERE id=1");
        sql("UPDATE tblDocument4 SET quantity=1 WHERE id=2");
        sql("UPDATE tblReader4 SET status='ACTIVE'");
        sql("UPDATE tblReaderCard4 SET status='ACTIVE',expiryDate=DATE '2035-01-01' WHERE id=100");
        cookie=staffSession(10);
        csrf=token(request("LoanSlipView4.jsp?new=1",null,200,cookie));
    }

    private static String staffSession(int id) {
        Session session=context.getManager().createSession(null);
        LibraryStaff4 staff=new LibraryStaff4(); staff.setId(id); staff.setFullName("Trần Thị B");
        session.getSession().setAttribute("currentStaff",staff);
        return "JSESSIONID="+session.getId();
    }
    private static void sql(String sql) throws Exception { try(Statement s=fixture.createStatement()){s.executeUpdate(sql);} }
    private static int scalar(String sql) throws Exception {
        try(Statement s=fixture.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getInt(1);}
    }
    private String post(String body) throws Exception { return request("LoanSlipView4.jsp","csrf="+csrf+"&"+body,200,cookie); }
    private void identifyAndAdd(int... ids) throws Exception {
        assertTrue(post("action=identify&readerCode=1").contains("hợp lệ"));
        for(int id:ids){post("action=lookup&documentCode="+id);post("action=add&documentCode="+id);}
    }
    private static String token(String html){Matcher m=Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"").matcher(html);assertTrue(m.find());return m.group(1);}
    private static int count(String text,String regex){Matcher m=Pattern.compile(regex).matcher(text);int n=0;while(m.find())n++;return n;}
    private String request(String path,String body,int expected,String session) throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(root+path).openConnection();
        c.setConnectTimeout(5000);c.setReadTimeout(20000);c.setInstanceFollowRedirects(false);
        if(session!=null)c.setRequestProperty("Cookie",session);
        try {
            if(body!=null){c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
                try(OutputStream out=c.getOutputStream()){out.write(body.getBytes(StandardCharsets.UTF_8));}}
            assertEquals(path,expected,c.getResponseCode());if(expected!=200)return "";
            try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
                byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return new String(out.toByteArray(),StandardCharsets.UTF_8);}
        } finally {c.disconnect();}
    }

    @Test public void savesOnceAndPrintsPersistedSlipWithDesignedColumns() throws Exception {
        String home=request("LibraryStaffHomeView4.jsp",null,200,cookie);
        assertTrue(home.contains("Trần Thị B"));assertEquals(1,count(home,"<button\\b"));
        preview("home",home);
        String initial=request("LoanSlipView4.jsp",null,200,cookie);
        assertEquals(5,count(initial,"<th\\b"));assertTrue(initial.contains("id=\"subPrint\" disabled"));
        assertTrue(initial.contains("<label for=\"inReaderCode\">Tra theo: Mã độc giả / Mã thẻ</label>"));
        assertEquals(0,count(initial,"<select\\b|<option\\b"));
        assertEquals(5,count(initial,"<button\\b"));
        identifyAndAdd(1,2);
        assertTrue(post("action=lookup&documentCode=1").contains("Đắc Nhân Tâm"));
        assertTrue(post("action=add&documentCode=1").contains("đã có trong danh sách"));
        preview("loan",request("LoanSlipView4.jsp",null,200,cookie));
        String saved=post("action=submit");assertTrue(saved.contains("Lưu phiếu mượn thành công"));
        assertFalse(saved.contains("id=\"subPrint\" disabled"));
        assertEquals(1,scalar("SELECT COUNT(*) FROM tblLoanSlip4"));assertEquals(2,scalar("SELECT COUNT(*) FROM tblDetailLoanSlip4"));
        assertEquals(1,scalar("SELECT quantity FROM tblDocument4 WHERE id=1"));assertEquals(0,scalar("SELECT quantity FROM tblDocument4 WHERE id=2"));
        request("LoanSlipView4.jsp","csrf="+csrf+"&action=submit",403,cookie);
        csrf=token(saved);post("action=submit");assertEquals(1,scalar("SELECT COUNT(*) FROM tblLoanSlip4"));
        int id=scalar("SELECT id FROM tblLoanSlip4");
        preview("print",request("LoanSlipPrintView4.jsp?loanSlipId="+id,null,200,cookie));
        sql("UPDATE tblDocument4 SET name='Tên đọc lại từ database' WHERE id=1");
        String print=request("LoanSlipPrintView4.jsp?loanSlipId="+id,null,200,cookie);
        assertEquals(2,count(print,"<th\\b"));assertTrue(print.contains("Tên đọc lại từ database"));
        assertTrue(print.contains("Nguyễn Văn A"));assertTrue(print.contains("Trần Thị B"));assertTrue(print.contains("window.print()"));
        request("LoanSlipPrintView4.jsp?loanSlipId="+id,null,302,null);
        request("LoanSlipPrintView4.jsp?loanSlipId="+id,null,403,staffSession(11));
        request("LoanSlipPrintView4.jsp?loanSlipId=999999",null,404,cookie);
        request("LoanSlipPrintView4.jsp?loanSlipId=abc",null,400,cookie);
    }

    @Test public void handlesReaderCardAndDocumentErrorsWithoutSaving() throws Exception {
        assertTrue(post("action=identify&readerCode=999").contains("Không tìm thấy độc giả"));
        assertTrue(post("action=identify&readerCode=abc").contains("số nguyên dương"));
        assertTrue(post("action=identify&readerCode=101").contains("không hợp lệ"));
        assertTrue(post("action=identify&readerCode=100").contains("hợp lệ"));
        assertTrue(post("action=lookup&documentCode=3").contains("không khả dụng"));
        assertTrue(post("action=add&documentCode=3").contains("không khả dụng"));
        assertTrue(post("action=lookup&documentCode=999").contains("Không tìm thấy tài liệu"));
        request("LoanSlipView4.jsp","csrf=wrong&action=submit",403,cookie);
        assertEquals(0,scalar("SELECT COUNT(*) FROM tblLoanSlip4"));
    }

    @Test public void rollsBackHeaderDetailsAndEarlierStockWhenLastDocumentIsUnavailable() throws Exception {
        identifyAndAdd(1,2);sql("UPDATE tblDocument4 SET quantity=0 WHERE id=2");
        String failed=post("action=submit");assertTrue(failed.contains("Không thể lưu"));
        assertTrue(failed.contains("id=\"subPrint\" disabled"));
        assertEquals(0,scalar("SELECT COUNT(*) FROM tblLoanSlip4"));assertEquals(0,scalar("SELECT COUNT(*) FROM tblDetailLoanSlip4"));
        assertEquals(2,scalar("SELECT quantity FROM tblDocument4 WHERE id=1"));
    }

    @Test public void rechecksCardAtConfirmation() throws Exception {
        identifyAndAdd(1);sql("UPDATE tblReaderCard4 SET status='EXPIRED' WHERE id=100");
        assertTrue(post("action=submit").contains("không còn hợp lệ"));
        assertEquals(0,scalar("SELECT COUNT(*) FROM tblLoanSlip4"));
        assertEquals(2,scalar("SELECT quantity FROM tblDocument4 WHERE id=1"));
    }

    @Test public void rejectsCodesSharedByDifferentReadersWithoutAddingASelector() throws Exception {
        sql("INSERT INTO tblMember4 (id,username,password,fullname) VALUES (20,'other_reader','DISABLED','Độc giả khác')");
        sql("INSERT INTO tblReader4 VALUES (20,CURRENT_DATE,'ACTIVE')");
        sql("INSERT INTO tblReaderCard4 VALUES (1,DATE '2025-01-01',DATE '2035-01-01','ACTIVE',20)");
        try {
            String ambiguous=post("action=identify&readerCode=1");
            assertTrue(ambiguous.contains("Mã trùng giữa độc giả và thẻ"));
            assertFalse(ambiguous.contains("<select"));
            // A forged old mode parameter cannot silently choose a different reader.
            assertTrue(post("action=identify&readerLookupMode=reader&readerCode=1").contains("Mã trùng giữa độc giả và thẻ"));
            assertTrue(post("action=submit").contains("Cần độc giả, thẻ hợp lệ"));
            assertEquals(0,scalar("SELECT COUNT(*) FROM tblLoanSlip4"));
            assertTrue(post("action=identify&readerCode=100").contains("Nguyễn Văn A"));
        } finally {
            sql("DELETE FROM tblReaderCard4 WHERE id=1");sql("DELETE FROM tblReader4 WHERE id=20");sql("DELETE FROM tblMember4 WHERE id=20");
        }
    }

    @Test public void concurrentTransactionsCannotLendTheLastCopyTwice() throws Exception {
        ExecutorService threads=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
        Callable<Boolean> borrow=()->{
            try(Connection c=DAO4.getConnection()){
                c.setAutoCommit(false);start.await();
                try {
                    LoanSlip4 loan=new LoanSlip4();Reader4 reader=new Reader4();reader.setId(1);
                    LibraryStaff4 staff=new LibraryStaff4();staff.setId(10);loan.setReader(reader);loan.setLibraryStaff(staff);
                    loan.setBorrowingDate(LocalDate.now());loan.setDueDate(LocalDate.now().plusDays(14));loan.setStatus("BORROWING");
                    Document4 document=new Document4();document.setId(2);DetailLoanSlip4 detail=new DetailLoanSlip4();detail.setDocument(document);loan.getDetails().add(detail);
                    new LoanSlipDAO4(c).taoPhieuMuon(loan);new DetailLoanSlipDAO4(c).taoChiTietPhieuMuon(loan.getId(),loan.getDetails());
                    new DocumentDAO4(c).capNhatSoLuong(loan.getDetails());c.commit();return true;
                }catch(SQLException e){c.rollback();if(!"45000".equals(e.getSQLState()))throw e;return false;}
            }
        };
        try {
            Future<Boolean> first=threads.submit(borrow),second=threads.submit(borrow);start.countDown();
            assertNotEquals(first.get(15,TimeUnit.SECONDS),second.get(15,TimeUnit.SECONDS));
            assertEquals(1,scalar("SELECT COUNT(*) FROM tblLoanSlip4"));assertEquals(1,scalar("SELECT COUNT(*) FROM tblDetailLoanSlip4"));
            assertEquals(0,scalar("SELECT quantity FROM tblDocument4 WHERE id=2"));
        }finally{threads.shutdownNow();}
    }

    @AfterClass public static void stop() throws Exception {
        try {if(server!=null){server.stop();server.destroy();}if(fixture!=null)fixture.close();}
        finally {for(int i=0;i<KEYS.length;i++){if(PREVIOUS[i]==null)System.clearProperty(KEYS[i]);else System.setProperty(KEYS[i],PREVIOUS[i]);}}
    }

    private static void preview(String page,String html) throws Exception {
        String css=new File("src/main/webapp/assets/library-module2.css").toURI().toString();
        Files.write(Paths.get("target/module2-preview-"+page+".html"),
                html.replace("../../assets/library-module2.css",css).getBytes(StandardCharsets.UTF_8));
    }
}
