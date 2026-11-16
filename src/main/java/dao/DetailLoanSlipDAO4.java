package dao;

import model.*;
import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;

/** Implement JDBC using PreparedStatement and try-with-resources. */
public class DetailLoanSlipDAO4 extends DAO4 {
    public DetailLoanSlipDAO4() { }
    public DetailLoanSlipDAO4(Connection connection) { super(connection); }

    public List<DetailLoanSlip4> taoChiTietPhieuMuon(int loanSlipId, List<DetailLoanSlip4> details) throws SQLException {
        if (connection == null || connection.getAutoCommit()) throw new SQLException("A transaction connection is required");
        if (loanSlipId <= 0 || details == null || details.isEmpty()) throw new IllegalArgumentException("Empty loan slip");
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        for (DetailLoanSlip4 d : details) {
            if (d == null || d.getDocument() == null || d.getDocument().getId() <= 0
                    || !ids.add(d.getDocument().getId())) throw new IllegalArgumentException("Invalid or duplicate document");
        }
        try (java.sql.PreparedStatement s = connection.prepareStatement(
                "INSERT INTO tblDetailLoanSlip4 (loanSlipId,documentId) VALUES (?,?)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
            for (DetailLoanSlip4 d : details) {
                s.setInt(1, loanSlipId); s.setInt(2, d.getDocument().getId());
                s.executeUpdate();
                try (java.sql.ResultSet keys = s.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Missing generated detail id");
                    d.setId(keys.getInt(1));
                }
            }
        }
        return details;
    }

    public List<DetailLoanSlip4> taoChiTietPhieuMuon(Connection connection, int loanSlipId, List<DetailLoanSlip4> details) throws SQLException {
        return new DetailLoanSlipDAO4(connection).taoChiTietPhieuMuon(loanSlipId, details);
    }

}
