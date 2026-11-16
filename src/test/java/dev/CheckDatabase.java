package dev;

import dao.DAO4;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/** Explicit MySQL check; never runs as part of unit tests. */
public final class CheckDatabase {
    public static void main(String[] args) throws Exception {
        try (Connection c = DAO4.getConnection(); Statement s = c.createStatement()) {
            System.out.println("Connected to " + c.getMetaData().getDatabaseProductName());
            String[] tables = {"tblMember4", "tblEmployee4", "tblReader4", "tblLibraryStaff4",
                "tblManagementStaff4", "tblReaderCard4", "tblDocument4", "tblSupplier4",
                "tblLoanSlip4", "tblDetailLoanSlip4", "tblImportedReceipt4", "tblDetailImportedReceipt4"};
            for (String table : tables) {
                try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    rs.next(); System.out.println(table + ": " + rs.getInt(1));
                }
            }
        } finally {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        }
    }
}
