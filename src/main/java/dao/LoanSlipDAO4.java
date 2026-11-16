package dao;

import model.*;
import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;

/** Implement JDBC using PreparedStatement and try-with-resources. */
public class LoanSlipDAO4 extends DAO4 {
    public LoanSlipDAO4() { }
    public LoanSlipDAO4(Connection connection) { super(connection); }

    public LoanSlip4 taoPhieuMuon(LoanSlip4 loanSlip) throws SQLException {
        if (connection == null || connection.getAutoCommit()) throw new SQLException("A transaction connection is required");
        if (loanSlip == null || loanSlip.getReader() == null || loanSlip.getLibraryStaff() == null
                || loanSlip.getReader().getId() <= 0 || loanSlip.getLibraryStaff().getId() <= 0
                || loanSlip.getBorrowingDate() == null || loanSlip.getDueDate() == null
                || loanSlip.getDueDate().isBefore(loanSlip.getBorrowingDate())
                || loanSlip.getDetails() == null || loanSlip.getDetails().isEmpty()
                || !"BORROWING".equals(loanSlip.getStatus())) throw new IllegalArgumentException("Invalid loan slip");
        try (java.sql.PreparedStatement s = connection.prepareStatement(
                "INSERT INTO tblLoanSlip4 (readerId,libraryStaffId,borrowingDate,dueDate,status) VALUES (?,?,?,?,?)",
                java.sql.Statement.RETURN_GENERATED_KEYS)) {
            s.setInt(1, loanSlip.getReader().getId()); s.setInt(2, loanSlip.getLibraryStaff().getId());
            s.setDate(3, java.sql.Date.valueOf(loanSlip.getBorrowingDate()));
            s.setDate(4, java.sql.Date.valueOf(loanSlip.getDueDate())); s.setString(5, loanSlip.getStatus());
            s.executeUpdate();
            try (java.sql.ResultSet keys = s.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Missing generated loan id");
                loanSlip.setId(keys.getInt(1));
            }
        }
        return loanSlip;
    }

    public LoanSlip4 taoPhieuMuon(Connection connection, LoanSlip4 loanSlip) throws SQLException {
        return new LoanSlipDAO4(connection).taoPhieuMuon(loanSlip);
    }

    public LoanSlip4 getPhieuMuonTheoID(int loanSlipId) throws SQLException {
        if (loanSlipId <= 0) return null;
        Connection opened = openConnection();
        String sql = "SELECT l.*,r.registrationDate,r.status AS readerStatus,"
                + "rm.fullname AS readerName,rm.username AS readerUsername,rm.address AS readerAddress,"
                + "rm.phoneNumber AS readerPhone,rm.email AS readerEmail,sm.fullname AS staffName,e.position "
                + "FROM tblLoanSlip4 l JOIN tblReader4 r ON r.id=l.readerId JOIN tblMember4 rm ON rm.id=r.id "
                + "JOIN tblLibraryStaff4 staff ON staff.id=l.libraryStaffId JOIN tblEmployee4 e ON e.id=staff.id "
                + "JOIN tblMember4 sm ON sm.id=staff.id WHERE l.id=?";
        try (java.sql.PreparedStatement s = opened.prepareStatement(sql)) {
            s.setInt(1, loanSlipId);
            LoanSlip4 slip;
            try (java.sql.ResultSet rs = s.executeQuery()) {
                if (!rs.next()) return null;
                slip = new LoanSlip4(); slip.setId(rs.getInt("id"));
                slip.setBorrowingDate(rs.getDate("borrowingDate").toLocalDate());
                slip.setDueDate(rs.getDate("dueDate").toLocalDate()); slip.setStatus(rs.getString("status"));
                Reader4 reader = new Reader4(); reader.setId(rs.getInt("readerId"));
                reader.setFullName(rs.getString("readerName")); reader.setUsername(rs.getString("readerUsername"));
                reader.setAddress(rs.getString("readerAddress")); reader.setPhoneNumber(rs.getString("readerPhone"));
                reader.setEmail(rs.getString("readerEmail")); reader.setStatus(rs.getString("readerStatus"));
                reader.setRegistrationDate(rs.getDate("registrationDate").toLocalDate()); slip.setReader(reader);
                LibraryStaff4 staff = new LibraryStaff4(); staff.setId(rs.getInt("libraryStaffId"));
                staff.setFullName(rs.getString("staffName")); staff.setPosition(rs.getString("position"));
                slip.setLibraryStaff(staff);
            }
            try (java.sql.PreparedStatement ds = opened.prepareStatement(
                    "SELECT d.*,x.id AS detailId,x.actualReturnDate,x.conditionOnReturn FROM tblDetailLoanSlip4 x "
                    + "JOIN tblDocument4 d ON d.id=x.documentId WHERE x.loanSlipId=? ORDER BY x.id")) {
                ds.setInt(1, loanSlipId);
                try (java.sql.ResultSet rs = ds.executeQuery()) {
                    while (rs.next()) {
                        Document4 document = new Document4(); document.setId(rs.getInt("id"));
                        document.setName(rs.getString("name")); document.setCategory(rs.getString("category"));
                        document.setAuthor(rs.getString("author")); document.setPublisher(rs.getString("publisher"));
                        document.setPublicationYear(rs.getInt("publicationYear")); document.setDescription(rs.getString("description"));
                        document.setQuantity(rs.getInt("quantity")); document.setStatus(rs.getString("status"));
                        DetailLoanSlip4 detail = new DetailLoanSlip4(); detail.setId(rs.getInt("detailId"));
                        detail.setDocument(document); detail.setConditionOnReturn(rs.getString("conditionOnReturn"));
                        java.sql.Date returned = rs.getDate("actualReturnDate");
                        if (returned != null) detail.setActualReturnDate(returned.toLocalDate());
                        slip.getDetails().add(detail);
                    }
                }
            }
            return slip;
        } finally { closeOwnedConnection(opened); }
    }

}
