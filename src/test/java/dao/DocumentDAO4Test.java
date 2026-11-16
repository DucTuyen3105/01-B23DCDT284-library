package dao;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;
import model.Document4;
import org.h2.tools.RunScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Isolated JDBC tests; never writes to the development MySQL database. */
public class DocumentDAO4Test {
    private final String[] keys = {"DB_URL", "DB_USER", "DB_PASSWORD"};
    private final String[] previous = new String[3];
    private Connection fixture;
    private final DocumentDAO4 dao = new DocumentDAO4();

    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < keys.length; i++) previous[i] = System.getProperty(keys[i]);
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL";
        System.setProperty("DB_URL", url);
        System.setProperty("DB_USER", "sa");
        System.setProperty("DB_PASSWORD", "");
        fixture = DriverManager.getConnection(url, "sa", "");
        try (java.io.Reader schema = Files.newBufferedReader(
                Paths.get("database/01-schema.sql"), StandardCharsets.UTF_8)) {
            RunScript.execute(fixture, schema);
        }
        add(1, "Lập trình Java", 5, "AVAILABLE");
        add(2, "Java nâng cao", 0, "UNAVAILABLE");
        add(3, "Cơ sở dữ liệu", 3, "AVAILABLE");
        add(4, "100% thực hành_A!", 1, "AVAILABLE");
    }

    private void add(int id, String name, int quantity, String status) throws Exception {
        try (PreparedStatement s = fixture.prepareStatement(
                "INSERT INTO tblDocument4 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            s.setInt(1, id); s.setString(2, name); s.setString(3, "Giáo trình");
            s.setString(4, "Tác giả"); s.setString(5, "Nhà xuất bản"); s.setInt(6, 2024);
            s.setString(7, "Mô tả tiếng Việt"); s.setInt(8, quantity); s.setString(9, status);
            s.executeUpdate();
        }
    }

    @After
    public void tearDown() throws Exception {
        try { if (fixture != null) fixture.close(); }
        finally {
            for (int i = 0; i < keys.length; i++) {
                if (previous[i] == null) System.clearProperty(keys[i]);
                else System.setProperty(keys[i], previous[i]);
            }
        }
    }

    @Test
    public void findsSubstringIncludingUnavailableDocuments() throws Exception {
        List<Document4> results = dao.getTaiLieuTheoTen("  Java  ");
        assertEquals(2, results.size());
        assertEquals(2, results.get(0).getId());
        assertEquals(0, results.get(0).getQuantity());
        assertEquals(1, results.get(1).getId());
        assertEquals(1, dao.getTaiLieuTheoTen("Cơ sở").size());
    }

    @Test
    public void treatsWildcardsAndSqlAsLiteralInput() throws Exception {
        for (String keyword : new String[]{"%", "_", "!", "100% thực hành_A!"}) {
            List<Document4> results = dao.getTaiLieuTheoTen(keyword);
            assertEquals(1, results.size());
            assertEquals(4, results.get(0).getId());
        }
        assertTrue(dao.getTaiLieuTheoTen("' OR 1=1 --").isEmpty());
    }

    @Test
    public void returnsEmptyForBlankOrMissingSearch() throws Exception {
        assertTrue(dao.getTaiLieuTheoTen(null).isEmpty());
        assertTrue(dao.getTaiLieuTheoTen(" \t ").isEmpty());
        assertTrue(dao.getTaiLieuTheoTen("Không tồn tại").isEmpty());
    }

    @Test
    public void mapsAllDetailFields() throws Exception {
        Document4 d = dao.getTaiLieuTheoID(1);
        assertNotNull(d);
        assertEquals(1, d.getId()); assertEquals("Lập trình Java", d.getName());
        assertEquals("Giáo trình", d.getCategory()); assertEquals("Tác giả", d.getAuthor());
        assertEquals("Nhà xuất bản", d.getPublisher()); assertEquals(2024, d.getPublicationYear());
        assertEquals("Mô tả tiếng Việt", d.getDescription());
        assertEquals(5, d.getQuantity()); assertEquals("AVAILABLE", d.getStatus());
    }

    @Test
    public void returnsNullForInvalidOrMissingId() throws Exception {
        assertNull(dao.getTaiLieuTheoID(0));
        assertNull(dao.getTaiLieuTheoID(-1));
        assertNull(dao.getTaiLieuTheoID(99999));
    }
}
