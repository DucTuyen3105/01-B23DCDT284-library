package dao;

import model.*;
import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;

/** Implement JDBC using PreparedStatement and try-with-resources. */
public class ReaderCardDAO4 extends DAO4 {
    public ReaderCardDAO4() { }
    public ReaderCardDAO4(Connection connection) { super(connection); }

    public ReaderCard4 getTheCuaReader(int readerId) throws SQLException {
        if (readerId <= 0) return null;
        String sql = "SELECT id,issueDate,expiryDate,status FROM tblReaderCard4 WHERE readerId=? "
                + "ORDER BY issueDate DESC,id DESC" + (connection == null ? "" : " FOR UPDATE");
        Connection opened = openConnection();
        try (java.sql.PreparedStatement s = opened.prepareStatement(sql)) {
            s.setInt(1, readerId);
            try (java.sql.ResultSet rs = s.executeQuery()) {
                ReaderCard4 latest = null, valid = null;
                java.time.LocalDate today = java.time.LocalDate.now();
                while (rs.next()) {
                    ReaderCard4 card = new ReaderCard4();
                    card.setId(rs.getInt("id")); card.setIssueDate(rs.getDate("issueDate").toLocalDate());
                    card.setExpiryDate(rs.getDate("expiryDate").toLocalDate()); card.setStatus(rs.getString("status"));
                    if (latest == null) latest = card;
                    if ("ACTIVE".equals(card.getStatus()) && !card.getIssueDate().isAfter(today)
                            && !card.getExpiryDate().isBefore(today)) {
                        if (valid != null) return null; // Ambiguous cards must not authorize a loan.
                        valid = card;
                    }
                }
                return valid == null ? latest : valid;
            }
        } finally { closeOwnedConnection(opened); }
    }

}
