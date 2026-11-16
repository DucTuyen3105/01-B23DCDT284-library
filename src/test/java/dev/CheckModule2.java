package dev;

import dao.*;
import model.*;
import java.sql.*;
import java.time.LocalDate;

/** MySQL lending smoke test. Always rolls back the test slip and stock changes. */
public final class CheckModule2 {
    public static void main(String[] args) throws Exception {
        try (Connection c = DAO4.getConnection()) {
            c.setAutoCommit(false);
            try {
                int readerId = find(c, "SELECT id FROM tblMember4 WHERE username='reader_demo'");
                int staffId = find(c, "SELECT m.id FROM tblMember4 m JOIN tblLibraryStaff4 s ON s.id=m.id WHERE m.username='staff_demo'");
                int documentId = find(c, "SELECT id FROM tblDocument4 WHERE quantity>0 AND status='AVAILABLE' ORDER BY id");
                Reader4 reader = new ReaderDAO4(c).getReaderTheoID(readerId);
                ReaderCard4 card = new ReaderCardDAO4(c).getTheCuaReader(readerId);
                if (reader == null || !"ACTIVE".equals(reader.getStatus()) || card == null || !"ACTIVE".equals(card.getStatus())
                        || card.getIssueDate().isAfter(LocalDate.now()) || card.getExpiryDate().isBefore(LocalDate.now())) {
                    throw new IllegalStateException("A valid demo reader/card is required");
                }
                Document4 document = new DocumentDAO4().getTaiLieuTheoID(documentId);
                if (!new DocumentDAO4().kiemTraKhaDung(documentId)) throw new IllegalStateException("Unavailable demo document");
                LibraryStaff4 staff = new LibraryStaff4(); staff.setId(staffId);
                LoanSlip4 slip = new LoanSlip4(); slip.setReader(reader); slip.setLibraryStaff(staff);
                slip.setBorrowingDate(LocalDate.now()); slip.setDueDate(LocalDate.now().plusDays(14)); slip.setStatus("BORROWING");
                DetailLoanSlip4 detail = new DetailLoanSlip4(); detail.setDocument(document); slip.getDetails().add(detail);
                new LoanSlipDAO4(c).taoPhieuMuon(slip);
                new DetailLoanSlipDAO4(c).taoChiTietPhieuMuon(slip.getId(), slip.getDetails());
                new DocumentDAO4(c).capNhatSoLuong(slip.getDetails());
                LoanSlip4 loaded = new LoanSlipDAO4(c).getPhieuMuonTheoID(slip.getId());
                if (loaded == null || loaded.getDetails().size() != 1
                        || loaded.getDetails().get(0).getDocument().getQuantity() != document.getQuantity()-1) {
                    throw new IllegalStateException("Saved slip/detail/stock mismatch");
                }
                System.out.println("Module 2 MySQL: reader=" + reader.getFullName() + ", card=" + card.getId());
                System.out.println("Created header, detail, decremented stock and reloaded print data: OK");
            } finally {
                c.rollback();
                System.out.println("Rolled back smoke-test data; existing stock is preserved.");
            }
        } finally {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        }
    }
    private static int find(Connection c, String sql) throws SQLException {
        try (Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) {
            if (!r.next()) throw new SQLException("Required demo data is missing");
            return r.getInt(1);
        }
    }
}
