package setup;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.h2.tools.RunScript;
import org.junit.Test;
import static org.junit.Assert.*;

/** Portable setup checks; an actual MySQL smoke check is still required separately. */
public class SchemaTest {
    @Test
    public void schemaAndSeedRespectInheritanceAndStockConstraints() throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:setup;MODE=MySQL;DB_CLOSE_DELAY=-1")) {
            try (java.io.Reader schema = Files.newBufferedReader(Paths.get("database/01-schema.sql"), StandardCharsets.UTF_8);
                 java.io.Reader seed = Files.newBufferedReader(Paths.get("database/02-seed.sql"), StandardCharsets.UTF_8)) {
                RunScript.execute(c, schema);
                // SET NAMES is MySQL-specific; H2 reads the file itself as UTF-8.
                String seedSql = new String(Files.readAllBytes(Paths.get("database/02-seed.sql")), StandardCharsets.UTF_8)
                        .replace("SET NAMES utf8mb4;", "");
                RunScript.execute(c, new java.io.StringReader(seedSql));
            }
            try (ResultSet rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM tblDocument4")) {
                assertTrue(rs.next()); assertEquals(3, rs.getInt(1));
            }
            expectRejected(c, "INSERT INTO tblReader4 VALUES (999, CURRENT_DATE, 'ACTIVE')");
            expectRejected(c, "UPDATE tblDocument4 SET quantity=-1 WHERE id=1");
            c.createStatement().executeUpdate("INSERT INTO tblMember4 (id,username,password,fullname) VALUES (100,'test','HASH_PLACEHOLDER','Test Reader')");
            c.createStatement().executeUpdate("INSERT INTO tblReader4 VALUES (100,CURRENT_DATE,'ACTIVE')");
            try (ResultSet rs = c.createStatement().executeQuery("SELECT m.fullname FROM tblMember4 m JOIN tblReader4 r ON r.id=m.id WHERE r.id=100")) {
                assertTrue(rs.next()); assertEquals("Test Reader", rs.getString(1));
            }
        }
    }

    private void expectRejected(Connection c, String sql) throws Exception {
        try { c.createStatement().executeUpdate(sql); fail("Expected constraint rejection: " + sql); }
        catch (SQLException expected) { assertTrue(expected.getSQLState().startsWith("23")); }
    }
}
