package view;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Reader4;
import org.apache.catalina.Context;
import org.apache.catalina.Session;
import org.apache.catalina.startup.Tomcat;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/** Runs the actual JSP -> DAO -> model flow against an isolated JDBC fixture. */
public class Module1FlowTest {
    private static Tomcat server;
    private static Connection fixture;
    private static String root;
    private static String sessionId;
    private static final String[] KEYS = {"DB_URL", "DB_USER", "DB_PASSWORD"};
    private static final String[] PREVIOUS = new String[3];

    @BeforeClass
    public static void start() throws Exception {
        for (int i = 0; i < KEYS.length; i++) PREVIOUS[i] = System.getProperty(KEYS[i]);
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL";
        System.setProperty("DB_URL", url);
        System.setProperty("DB_USER", "sa");
        System.setProperty("DB_PASSWORD", "");
        fixture = DriverManager.getConnection(url, "sa", "");
        try (java.io.Reader schema = Files.newBufferedReader(Paths.get("database/01-schema.sql"), StandardCharsets.UTF_8)) {
            RunScript.execute(fixture, schema);
        }
        fixture.createStatement().executeUpdate("INSERT INTO tblDocument4 (id,name,category,author,publisher,quantity,status) "
                + "VALUES (1,'Đắc Nhân Tâm','Kỹ năng sống','Dale Carnegie','NXB Tổng hợp TP.HCM',2,'AVAILABLE')");
        server = new Tomcat();
        server.setBaseDir(new File("target/module1-flow-tomcat").getAbsolutePath());
        server.setPort(0);
        server.getConnector().setProperty("address", "127.0.0.1");
        Context context = server.addWebapp("/libman", new File("src/main/webapp").getAbsolutePath());
        context.setParentClassLoader(Module1FlowTest.class.getClassLoader());
        server.start();
        root = "http://127.0.0.1:" + server.getConnector().getLocalPort() + "/libman/view/reader/";
        // The login use case precedes module 1. Supply its session result only in this test.
        Reader4 currentReader = new Reader4();
        currentReader.setId(1); currentReader.setFullName("Nguyễn Văn A");
        Session session = context.getManager().createSession(null);
        session.getSession().setAttribute("currentReader", currentReader);
        sessionId = session.getId();
    }

    @AfterClass
    public static void stop() throws Exception {
        try {
            if (server != null) { server.stop(); server.destroy(); }
            if (fixture != null) fixture.close();
        } finally {
            for (int i = 0; i < KEYS.length; i++) {
                if (PREVIOUS[i] == null) System.clearProperty(KEYS[i]);
                else System.setProperty(KEYS[i], PREVIOUS[i]);
            }
        }
    }

    private String get(String path, int expectedStatus) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(root + path).openConnection();
        c.setConnectTimeout(5000); c.setReadTimeout(20000);
        c.setRequestProperty("Cookie", "JSESSIONID=" + sessionId);
        try {
            assertEquals(path, expectedStatus, c.getResponseCode());
            if (expectedStatus != 200) return "";
            try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096]; int read;
                while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
                return new String(out.toByteArray(), StandardCharsets.UTF_8);
            }
        } finally { c.disconnect(); }
    }

    @Test
    public void followsDesignedScreensAndReloadsDetailFromDatabase() throws Exception {
        String home = get("ReaderHomeView4.jsp", 200);
        assertTrue(home.contains("Nguyễn Văn A"));
        assertTrue(home.contains("id=\"subFindDocument\""));
        assertEquals(1, count(home, "<button\\b"));
        String initial = get("DocumentSearchView4.jsp", 200);
        assertFalse(initial.contains("Đắc Nhân Tâm"));
        String keyword = URLEncoder.encode("Đắc Nhân Tâm", "UTF-8");
        String search = get("DocumentSearchView4.jsp?keyword=" + keyword, 200);
        assertEquals(6, count(search, "<th\\b"));
        assertEquals(6, count(search, "<td\\b"));
        assertTrue(search.contains("Đắc Nhân Tâm"));
        Matcher link = Pattern.compile("class=\"subViewDocumentDetail\" href=\"([^\"]+)\"").matcher(search);
        assertTrue(link.find());
        String detailPath = link.group(1).replace("&amp;", "&");
        // A stale object carried from search would show 2 instead of the new quantity 1.
        fixture.createStatement().executeUpdate("UPDATE tblDocument4 SET quantity=1 WHERE id=1");
        String detail = get(detailPath, 200);
        assertEquals(6, count(detail, "<dt>"));
        assertTrue(detail.contains("id=\"outQuantity\">1</dd>"));
        assertTrue(detail.contains("NXB Tổng hợp TP.HCM"));
        assertFalse(detail.contains("Năm xuất bản"));
        assertFalse(detail.contains("Trạng thái"));
        Matcher back = Pattern.compile("href=\"([^\"]+)\" id=\"subBack\"").matcher(detail);
        assertTrue(back.find());
        assertTrue(get(back.group(1).replace("&amp;", "&"), 200).contains("Đắc Nhân Tâm"));
        assertTrue(search.contains("href=\"ReaderHomeView4.jsp\" id=\"subBack\""));
    }

    @Test
    public void handlesEmptyResultsInvalidIdsAndEscapesInput() throws Exception {
        String empty = get("DocumentSearchView4.jsp?keyword=Missing", 200);
        assertEquals(0, count(empty, "<td\\b"));
        String escaped = get("DocumentSearchView4.jsp?keyword="
                + URLEncoder.encode("\"><script>alert(1)</script>", "UTF-8"), 200);
        assertFalse(escaped.contains("<script>"));
        get("DocumentDetailView4.jsp", 400);
        get("DocumentDetailView4.jsp?documentId=abc", 400);
        get("DocumentDetailView4.jsp?documentId=0", 400);
        get("DocumentDetailView4.jsp?documentId=9999", 404);
    }

    private static int count(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        int count = 0; while (matcher.find()) count++;
        return count;
    }
}
