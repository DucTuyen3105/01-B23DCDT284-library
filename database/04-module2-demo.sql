SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO tblMember4 (username,password,fullname,address,phoneNumber,email)
 SELECT 'staff_demo','!DEMO_LOGIN_DISABLED!','Trần Thị B','Hà Nội',NULL,'staff-demo@example.test'
 WHERE NOT EXISTS (SELECT 1 FROM tblMember4 WHERE username='staff_demo');
INSERT INTO tblEmployee4 (id,position)
 SELECT m.id,'Nhân viên thư viện' FROM tblMember4 m WHERE m.username='staff_demo'
 AND NOT EXISTS (SELECT 1 FROM tblEmployee4 e WHERE e.id=m.id);
INSERT INTO tblLibraryStaff4 (id)
 SELECT e.id FROM tblEmployee4 e JOIN tblMember4 m ON m.id=e.id WHERE m.username='staff_demo'
 AND NOT EXISTS (SELECT 1 FROM tblLibraryStaff4 staff WHERE staff.id=e.id);
COMMIT;
