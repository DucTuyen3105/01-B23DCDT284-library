package dao;

import java.sql.*;
import model.*;
import security.PasswordHash4;

/** Login uses the existing inheritance tables to determine the member's role. */
public class MemberDAO4 extends DAO4 {
    private static final String DUMMY_HASH = "pbkdf2-sha256$600000$AAAAAAAAAAAAAAAAAAAAAA==$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    /** Returns a member without its password hash; null means invalid/inactive. */
    public Member4 dangNhap(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty() || username.trim().length() > 50
                || password == null || password.isEmpty() || password.length() > 1024) return null;
        String sql = "SELECT m.*,r.id AS readerId,r.registrationDate,r.status AS readerStatus,"
                + "s.id AS staffId,g.id AS managerId,e.position FROM tblMember4 m "
                + "LEFT JOIN tblReader4 r ON r.id=m.id LEFT JOIN tblEmployee4 e ON e.id=m.id "
                + "LEFT JOIN tblLibraryStaff4 s ON s.id=e.id LEFT JOIN tblManagementStaff4 g ON g.id=e.id "
                + "WHERE m.username=?";
        try (Connection c = getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, username.trim());
            try (ResultSet rs = s.executeQuery()) {
                if (!rs.next()) { PasswordHash4.verify(password, DUMMY_HASH); return null; }
                if (!PasswordHash4.verify(password, rs.getString("password"))) return null;
                boolean reader = rs.getObject("readerId") != null;
                boolean staff = rs.getObject("staffId") != null;
                boolean manager = rs.getObject("managerId") != null;
                if ((reader ? 1 : 0) + (staff ? 1 : 0) + (manager ? 1 : 0) != 1) return null;
                Member4 member;
                if (reader) {
                    if (!"ACTIVE".equals(rs.getString("readerStatus"))) return null;
                    Reader4 r = new Reader4(); r.setStatus(rs.getString("readerStatus"));
                    r.setRegistrationDate(rs.getDate("registrationDate").toLocalDate()); member = r;
                } else if (staff) {
                    LibraryStaff4 employee = new LibraryStaff4(); employee.setPosition(rs.getString("position")); member = employee;
                } else {
                    ManagementStaff4 employee = new ManagementStaff4(); employee.setPosition(rs.getString("position")); member = employee;
                }
                member.setId(rs.getInt("id")); member.setUsername(rs.getString("username"));
                member.setFullName(rs.getString("fullname")); member.setAddress(rs.getString("address"));
                member.setPhoneNumber(rs.getString("phoneNumber")); member.setEmail(rs.getString("email"));
                return member;
            }
        }
    }
}
