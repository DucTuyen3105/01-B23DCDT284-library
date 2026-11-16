-- Local demo login accounts. Existing enabled passwords are not overwritten.
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO tblMember4 (username,password,fullname,email) SELECT 'reader_demo','pbkdf2-sha256$600000$/iZg73LXQNbszW2//P7beg==$Wotji6jLFEF/+aCVyUUXXnsR10/xD803Lq0YZbM3xNM=','Nguyễn Văn A','reader_demo@example.test' WHERE NOT EXISTS (SELECT 1 FROM tblMember4 WHERE username='reader_demo');
UPDATE tblMember4 SET password='pbkdf2-sha256$600000$/iZg73LXQNbszW2//P7beg==$Wotji6jLFEF/+aCVyUUXXnsR10/xD803Lq0YZbM3xNM=' WHERE username='reader_demo' AND password='!DEMO_LOGIN_DISABLED!';
INSERT INTO tblReader4 (id,registrationDate,status) SELECT m.id,CURRENT_DATE,'ACTIVE' FROM tblMember4 m WHERE m.username='reader_demo' AND NOT EXISTS (SELECT 1 FROM tblReader4 r WHERE r.id=m.id);
INSERT INTO tblMember4 (username,password,fullname,email) SELECT 'staff_demo','pbkdf2-sha256$600000$FnpUagnvLWPjBK6DkEXnhw==$fOhg8ThtHZm1uMcHz+3uV+j4EpdUb2QKWkz5ITKNVaE=','Trần Thị B','staff_demo@example.test' WHERE NOT EXISTS (SELECT 1 FROM tblMember4 WHERE username='staff_demo');
UPDATE tblMember4 SET password='pbkdf2-sha256$600000$FnpUagnvLWPjBK6DkEXnhw==$fOhg8ThtHZm1uMcHz+3uV+j4EpdUb2QKWkz5ITKNVaE=' WHERE username='staff_demo' AND password='!DEMO_LOGIN_DISABLED!';
INSERT INTO tblEmployee4 (id,position) SELECT m.id,'Nhân viên thư viện' FROM tblMember4 m WHERE m.username='staff_demo' AND NOT EXISTS (SELECT 1 FROM tblEmployee4 e WHERE e.id=m.id);
INSERT INTO tblLibraryStaff4 (id) SELECT e.id FROM tblEmployee4 e JOIN tblMember4 m ON m.id=e.id WHERE m.username='staff_demo' AND NOT EXISTS (SELECT 1 FROM tblLibraryStaff4 roleTable WHERE roleTable.id=e.id);
INSERT INTO tblMember4 (username,password,fullname,email) SELECT 'manager_demo','pbkdf2-sha256$600000$63gZjcN+3uzJ1Auqt1e92Q==$eJMoSa7B8hiz+3+t/xiMSz2fXGX6sDOtYV5/ElTkm9o=','Lê Văn C','manager_demo@example.test' WHERE NOT EXISTS (SELECT 1 FROM tblMember4 WHERE username='manager_demo');
UPDATE tblMember4 SET password='pbkdf2-sha256$600000$63gZjcN+3uzJ1Auqt1e92Q==$eJMoSa7B8hiz+3+t/xiMSz2fXGX6sDOtYV5/ElTkm9o=' WHERE username='manager_demo' AND password='!DEMO_LOGIN_DISABLED!';
INSERT INTO tblEmployee4 (id,position) SELECT m.id,'Nhân viên quản lý' FROM tblMember4 m WHERE m.username='manager_demo' AND NOT EXISTS (SELECT 1 FROM tblEmployee4 e WHERE e.id=m.id);
INSERT INTO tblManagementStaff4 (id) SELECT e.id FROM tblEmployee4 e JOIN tblMember4 m ON m.id=e.id WHERE m.username='manager_demo' AND NOT EXISTS (SELECT 1 FROM tblManagementStaff4 roleTable WHERE roleTable.id=e.id);
COMMIT;
