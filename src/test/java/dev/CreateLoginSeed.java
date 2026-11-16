package dev;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import security.PasswordHash4;

/** Creates only local demo-account SQL; production passwords must be provisioned separately. */
public final class CreateLoginSeed {
    public static void main(String[] args) throws Exception {
        String reader = PasswordHash4.hash("Reader@123");
        String staff = PasswordHash4.hash("Staff@123");
        String manager = PasswordHash4.hash("Manager@123");
        String sql = "-- Local demo login accounts. Existing enabled passwords are not overwritten.\n"
                + "SET NAMES utf8mb4;\nSTART TRANSACTION;\n"
                + member("reader_demo", "Nguyễn Văn A", reader)
                + "INSERT INTO tblReader4 (id,registrationDate,status) SELECT m.id,CURRENT_DATE,'ACTIVE' FROM tblMember4 m "
                + "WHERE m.username='reader_demo' AND NOT EXISTS (SELECT 1 FROM tblReader4 r WHERE r.id=m.id);\n"
                + member("staff_demo", "Trần Thị B", staff)
                + employee("staff_demo", "Nhân viên thư viện", "tblLibraryStaff4")
                + member("manager_demo", "Lê Văn C", manager)
                + employee("manager_demo", "Nhân viên quản lý", "tblManagementStaff4")
                + "COMMIT;\n";
        Files.write(Paths.get("database/05-login-demo.sql"), sql.getBytes(StandardCharsets.UTF_8));
        System.out.println("Created database/05-login-demo.sql with salted password hashes.");
    }
    private static String member(String username,String name,String hash) {
        return "INSERT INTO tblMember4 (username,password,fullname,email) SELECT '"+username+"','"+hash+"','"+name+"','"+username+"@example.test' "
                + "WHERE NOT EXISTS (SELECT 1 FROM tblMember4 WHERE username='"+username+"');\n"
                + "UPDATE tblMember4 SET password='"+hash+"' WHERE username='"+username+"' AND password='!DEMO_LOGIN_DISABLED!';\n";
    }
    private static String employee(String username,String position,String table) {
        return "INSERT INTO tblEmployee4 (id,position) SELECT m.id,'"+position+"' FROM tblMember4 m WHERE m.username='"+username+"' "
                + "AND NOT EXISTS (SELECT 1 FROM tblEmployee4 e WHERE e.id=m.id);\n"
                + "INSERT INTO "+table+" (id) SELECT e.id FROM tblEmployee4 e JOIN tblMember4 m ON m.id=e.id WHERE m.username='"+username+"' "
                + "AND NOT EXISTS (SELECT 1 FROM "+table+" roleTable WHERE roleTable.id=e.id);\n";
    }
}
