-- Module 1 demo data. Safe to re-run: existing catalog records are preserved.
SET NAMES utf8mb4;
START TRANSACTION;

CREATE TEMPORARY TABLE libmanDemoDocuments LIKE tblDocument4;
INSERT INTO libmanDemoDocuments
 (name,category,author,publisher,publicationYear,description,quantity,status) VALUES
 ('Đắc Nhân Tâm','Kỹ năng sống','Dale Carnegie','NXB Tổng hợp TP.HCM',2021,'Tài liệu mẫu theo giao diện Hình 2.3 của báo cáo.',2,'AVAILABLE'),
 ('Quẳng gánh lo đi và vui sống','Kỹ năng sống','Dale Carnegie','NXB Tổng hợp TP.HCM',2022,'Tài liệu mẫu cùng tác giả để kiểm tra tìm kiếm.',4,'AVAILABLE'),
 ('Lập trình Java cơ bản','Giáo trình','Nguyễn Văn An','NXB Giáo dục Việt Nam',2024,'Kiến thức Java cơ bản, biến, vòng lặp và phương thức.',8,'AVAILABLE'),
 ('Lập trình Java nâng cao','Giáo trình','Trần Minh Anh','NXB Đại học Quốc gia Hà Nội',2025,'Lập trình hướng đối tượng, collection và xử lý ngoại lệ.',3,'AVAILABLE'),
 ('Java Web với JSP và Servlet','Giáo trình','Lê Hoàng Nam','NXB Thông tin và Truyền thông',2025,'Tài liệu mẫu phục vụ triển khai hệ thống thư viện.',0,'UNAVAILABLE'),
 ('Thiết kế cơ sở dữ liệu','Giáo trình','Phạm Thu Hà','NXB Đại học Quốc gia Hà Nội',2024,'Mô hình quan hệ, chuẩn hóa và truy vấn SQL.',6,'AVAILABLE'),
 ('Phân tích và thiết kế hệ thống thông tin','Giáo trình','Nguyễn Hải Linh','NXB Giáo dục Việt Nam',2023,'Use case, mô hình lớp, biểu đồ tuần tự và thiết kế giao diện.',5,'AVAILABLE'),
 ('Nhập môn quản lý dự án phần mềm','Sách','Vũ Minh Đức','NXB Thông tin và Truyền thông',2024,'Lập kế hoạch và theo dõi tiến độ dự án phần mềm.',2,'AVAILABLE'),
 ('Tạp chí Khoa học và Công nghệ','Tạp chí','Ban biên tập','NXB Khoa học và Kỹ thuật',2026,'Dữ liệu mẫu loại tài liệu tạp chí.',7,'AVAILABLE'),
 ('100% thực hành SQL_cơ bản!','Sách thực hành','Đỗ Thanh Bình','NXB Khoa học và Kỹ thuật',2025,'Dùng để kiểm tra tìm ký tự literal %, _ và !.',1,'AVAILABLE');

INSERT INTO tblDocument4
 (name,category,author,publisher,publicationYear,description,quantity,status)
 SELECT d.name,d.category,d.author,d.publisher,d.publicationYear,d.description,d.quantity,d.status
 FROM libmanDemoDocuments d
 WHERE NOT EXISTS (
   SELECT 1 FROM tblDocument4 existing
   WHERE existing.name=d.name AND existing.author=d.author AND existing.publisher=d.publisher
 );
DROP TEMPORARY TABLE libmanDemoDocuments;

-- Reader data only: no password/login flow is enabled by this seed.
INSERT INTO tblMember4 (username,password,fullname,address,phoneNumber,email)
 SELECT 'reader_demo','!DEMO_LOGIN_DISABLED!','Nguyễn Văn A','Hà Nội',NULL,'reader-demo@example.test'
 WHERE NOT EXISTS (SELECT 1 FROM tblMember4 WHERE username='reader_demo');
INSERT INTO tblReader4 (id,registrationDate,status)
 SELECT m.id,CURRENT_DATE,'ACTIVE' FROM tblMember4 m
 WHERE m.username='reader_demo'
 AND NOT EXISTS (SELECT 1 FROM tblReader4 r WHERE r.id=m.id);
INSERT INTO tblReaderCard4 (issueDate,expiryDate,status,readerId)
 SELECT CURRENT_DATE,DATE_ADD(CURRENT_DATE,INTERVAL 1 YEAR),'ACTIVE',r.id
 FROM tblReader4 r JOIN tblMember4 m ON m.id=r.id
 WHERE m.username='reader_demo'
 AND NOT EXISTS (SELECT 1 FROM tblReaderCard4 c WHERE c.readerId=r.id);

COMMIT;
