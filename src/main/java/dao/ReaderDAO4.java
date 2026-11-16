package dao;

import model.*;
import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;

/** Implement JDBC using PreparedStatement and try-with-resources. */
public class ReaderDAO4 extends DAO4 {
    public ReaderDAO4() { }
    public ReaderDAO4(Connection connection) { super(connection); }

    public Reader4 getReaderTheoID(int readerId) throws SQLException {
        return find("r.id", readerId, false);
    }

    public Reader4 getReaderTheoMaThe(int cardId) throws SQLException {
        return find("c.id", cardId, true);
    }

    private Reader4 find(String column, int id, boolean byCard) throws SQLException {
        if (id <= 0) return null;
        String sql = "SELECT m.id,m.username,m.fullname,m.address,m.phoneNumber,m.email,"
                + "r.registrationDate,r.status FROM tblMember4 m JOIN tblReader4 r ON r.id=m.id "
                + (byCard ? "JOIN tblReaderCard4 c ON c.readerId=r.id " : "")
                + "WHERE " + column + "=?" + (connection == null ? "" : " FOR UPDATE");
        Connection opened = openConnection();
        try (java.sql.PreparedStatement s = opened.prepareStatement(sql)) {
            s.setInt(1, id);
            try (java.sql.ResultSet rs = s.executeQuery()) {
                if (!rs.next()) return null;
                Reader4 reader = new Reader4();
                reader.setId(rs.getInt("id")); reader.setUsername(rs.getString("username"));
                reader.setFullName(rs.getString("fullname")); reader.setAddress(rs.getString("address"));
                reader.setPhoneNumber(rs.getString("phoneNumber")); reader.setEmail(rs.getString("email"));
                reader.setRegistrationDate(rs.getDate("registrationDate").toLocalDate());
                reader.setStatus(rs.getString("status"));
                return reader;
            }
        } finally { closeOwnedConnection(opened); }
    }
}
