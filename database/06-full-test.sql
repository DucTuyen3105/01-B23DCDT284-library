-- Local full test dataset. User-requested credentials: admin / 1234, tuyen / 1234.
-- Only these two password hashes are deliberately reset on each application.
SET NAMES utf8mb4;
START TRANSACTION;

CREATE TEMPORARY TABLE fullTestMembers LIKE tblMember4;
INSERT INTO fullTestMembers (username,password,fullname,address,email) VALUES
 ('admin','pbkdf2-sha256$600000$iSvE8aKcvR5nhI6RSGvJCg==$0GvZnDn2UYj8TupO6ZwE9HZ57Jk1Ed9/jIuCPU4VgRc=','Nhân viên thư viện Admin','Hà Nội','admin@example.test'),
 ('tuyen','pbkdf2-sha256$600000$paGkz20skAWMzhJRtDa1DQ==$1r8R0wOEKFyLfVaSP1p2J+rJQaPPfCy+e1laiDza6FA=','Nguyễn Đức Tuyên','Hà Nội','tuyen@example.test'),
 ('test_expired','!DEMO_LOGIN_DISABLED!','Độc giả thẻ hết hạn','Hà Nội','expired@example.test'),
 ('test_inactive','!DEMO_LOGIN_DISABLED!','Độc giả ngừng hoạt động','Hà Nội','inactive@example.test'),
 ('test_no_card','!DEMO_LOGIN_DISABLED!','Độc giả chưa có thẻ','Hà Nội','no-card@example.test');
INSERT INTO tblMember4 (username,password,fullname,address,email)
 SELECT d.username,d.password,d.fullname,d.address,d.email FROM fullTestMembers d
 WHERE NOT EXISTS (SELECT 1 FROM tblMember4 m WHERE m.username=d.username);
UPDATE tblMember4 SET password='pbkdf2-sha256$600000$iSvE8aKcvR5nhI6RSGvJCg==$0GvZnDn2UYj8TupO6ZwE9HZ57Jk1Ed9/jIuCPU4VgRc=' WHERE username='admin';
UPDATE tblMember4 SET password='pbkdf2-sha256$600000$paGkz20skAWMzhJRtDa1DQ==$1r8R0wOEKFyLfVaSP1p2J+rJQaPPfCy+e1laiDza6FA=' WHERE username='tuyen';
DROP TEMPORARY TABLE fullTestMembers;

SET @adminId=(SELECT id FROM tblMember4 WHERE username='admin');
SET @tuyenId=(SELECT id FROM tblMember4 WHERE username='tuyen');
SET @expiredId=(SELECT id FROM tblMember4 WHERE username='test_expired');
SET @inactiveId=(SELECT id FROM tblMember4 WHERE username='test_inactive');
SET @noCardId=(SELECT id FROM tblMember4 WHERE username='test_no_card');
SET @demoReaderId=(SELECT id FROM tblMember4 WHERE username='reader_demo');
INSERT INTO tblEmployee4 (id,position) SELECT @adminId,'Nhân viên thư viện'
 WHERE NOT EXISTS (SELECT 1 FROM tblEmployee4 WHERE id=@adminId);
INSERT INTO tblLibraryStaff4 (id) SELECT @adminId
 WHERE NOT EXISTS (SELECT 1 FROM tblLibraryStaff4 WHERE id=@adminId);
INSERT INTO tblReader4 (id,registrationDate,status)
 SELECT m.id,'2026-01-01',CASE WHEN m.username='test_inactive' THEN 'INACTIVE' ELSE 'ACTIVE' END
 FROM tblMember4 m WHERE m.id IN (@tuyenId,@expiredId,@inactiveId,@noCardId)
 AND NOT EXISTS (SELECT 1 FROM tblReader4 r WHERE r.id=m.id);
UPDATE tblReader4 SET status='ACTIVE' WHERE id=@tuyenId;
INSERT INTO tblReaderCard4 (issueDate,expiryDate,status,readerId)
 SELECT DATE_SUB(CURRENT_DATE,INTERVAL 30 DAY),DATE_ADD(CURRENT_DATE,INTERVAL 1 YEAR),'ACTIVE',@tuyenId
 WHERE NOT EXISTS (SELECT 1 FROM tblReaderCard4 WHERE readerId=@tuyenId AND status='ACTIVE' AND issueDate<=CURRENT_DATE AND expiryDate>=CURRENT_DATE);
INSERT INTO tblReaderCard4 (issueDate,expiryDate,status,readerId)
 SELECT '2025-01-01','2026-01-01','EXPIRED',@expiredId
 WHERE NOT EXISTS (SELECT 1 FROM tblReaderCard4 WHERE readerId=@expiredId);
INSERT INTO tblReaderCard4 (issueDate,expiryDate,status,readerId)
 SELECT '2026-01-01','2027-12-31','ACTIVE',@inactiveId
 WHERE NOT EXISTS (SELECT 1 FROM tblReaderCard4 WHERE readerId=@inactiveId);

CREATE TEMPORARY TABLE fullTestDocuments LIKE tblDocument4;
INSERT INTO fullTestDocuments (name,category,author,publisher,publicationYear,description,quantity,status) VALUES
 ('Clean Code','Lập trình','Robert C. Martin','Prentice Hall',2008,'Dữ liệu kiểm thử: sách đang có bản cho mượn.',5,'AVAILABLE'),
 ('Design Patterns','Lập trình','Erich Gamma và cộng sự','Addison-Wesley',1994,'Dữ liệu kiểm thử: có một phiếu quá hạn.',3,'AVAILABLE'),
 ('Cấu trúc dữ liệu và giải thuật','Giáo trình','Nguyễn Văn An','NXB Đại học Quốc gia Hà Nội',2024,'Danh sách, cây, đồ thị và các thuật toán.',8,'AVAILABLE'),
 ('Mạng máy tính','Giáo trình','Trần Minh Anh','NXB Thông tin và Truyền thông',2024,'TCP/IP và các mô hình mạng.',6,'AVAILABLE'),
 ('Hệ điều hành','Giáo trình','Lê Hoàng Nam','NXB Giáo dục Việt Nam',2023,'Tiến trình, bộ nhớ và hệ thống tập tin.',4,'AVAILABLE'),
 ('Kiểm thử phần mềm','Giáo trình','Phạm Thu Hà','NXB Đại học Quốc gia Hà Nội',2025,'Kiểm thử đơn vị, tích hợp và hệ thống.',7,'AVAILABLE'),
 ('An toàn thông tin','Giáo trình','Nguyễn Hải Linh','NXB Thông tin và Truyền thông',2025,'Mật mã và bảo vệ hệ thống thông tin.',5,'AVAILABLE'),
 ('Trí tuệ nhân tạo','Giáo trình','Vũ Minh Đức','NXB Khoa học và Kỹ thuật',2024,'Tìm kiếm, suy luận và học máy.',4,'AVAILABLE'),
 ('Tài liệu đã hết số lượng','Sách','Nhóm tác giả LibMan','NXB Thử nghiệm',2026,'Ca kiểm thử tài liệu có quantity=0.',0,'UNAVAILABLE'),
 ('Tài liệu đang sửa chữa','Sách','Nhóm tác giả LibMan','NXB Thử nghiệm',2026,'Ca kiểm thử còn số lượng nhưng trạng thái không cho mượn.',2,'REPAIR');
INSERT INTO tblDocument4 (name,category,author,publisher,publicationYear,description,quantity,status)
 SELECT d.name,d.category,d.author,d.publisher,d.publicationYear,d.description,d.quantity,d.status FROM fullTestDocuments d
 WHERE NOT EXISTS (SELECT 1 FROM tblDocument4 e WHERE e.name=d.name AND e.author=d.author AND e.publisher=d.publisher);
DROP TEMPORARY TABLE fullTestDocuments;
SET @clean=(SELECT MIN(id) FROM tblDocument4 WHERE name='Clean Code' AND author='Robert C. Martin');
SET @patterns=(SELECT MIN(id) FROM tblDocument4 WHERE name='Design Patterns' AND author='Erich Gamma và cộng sự');
SET @algorithms=(SELECT MIN(id) FROM tblDocument4 WHERE name='Cấu trúc dữ liệu và giải thuật' AND author='Nguyễn Văn An');
SET @network=(SELECT MIN(id) FROM tblDocument4 WHERE name='Mạng máy tính' AND author='Trần Minh Anh');
SET @os=(SELECT MIN(id) FROM tblDocument4 WHERE name='Hệ điều hành' AND author='Lê Hoàng Nam');
SET @testing=(SELECT MIN(id) FROM tblDocument4 WHERE name='Kiểm thử phần mềm' AND author='Phạm Thu Hà');
SET @security=(SELECT MIN(id) FROM tblDocument4 WHERE name='An toàn thông tin' AND author='Nguyễn Hải Linh');
SET @ai=(SELECT MIN(id) FROM tblDocument4 WHERE name='Trí tuệ nhân tạo' AND author='Vũ Minh Đức');
SET @repair=(SELECT MIN(id) FROM tblDocument4 WHERE name='Tài liệu đang sửa chữa' AND author='Nhóm tác giả LibMan');

INSERT INTO tblSupplier4 (name,address,phoneNumber,email,description)
 SELECT 'Nhà cung cấp Giáo trình Hà Nội','Hà Nội','0240000001','supplier-hanoi@example.test','Nhà cung cấp mẫu phục vụ kiểm thử'
 WHERE NOT EXISTS (SELECT 1 FROM tblSupplier4 WHERE email='supplier-hanoi@example.test');
INSERT INTO tblSupplier4 (name,address,phoneNumber,email,description)
 SELECT 'Nhà cung cấp Sách Kỹ thuật','TP. Hồ Chí Minh','0280000002','supplier-tech@example.test','Nhà cung cấp mẫu phục vụ kiểm thử'
 WHERE NOT EXISTS (SELECT 1 FROM tblSupplier4 WHERE email='supplier-tech@example.test');
INSERT INTO tblSupplier4 (name,address,phoneNumber,email,description)
 SELECT 'Nhà cung cấp Khoa học','Đà Nẵng','0236000003','supplier-science@example.test','Nhà cung cấp mẫu phục vụ kiểm thử'
 WHERE NOT EXISTS (SELECT 1 FROM tblSupplier4 WHERE email='supplier-science@example.test');
SET @supplier1=(SELECT MIN(id) FROM tblSupplier4 WHERE email='supplier-hanoi@example.test');
SET @supplier2=(SELECT MIN(id) FROM tblSupplier4 WHERE email='supplier-tech@example.test');
SET @supplier3=(SELECT MIN(id) FROM tblSupplier4 WHERE email='supplier-science@example.test');
INSERT INTO tblImportedReceipt4 (importDate,supplierId,libraryStaffId)
 SELECT '2026-10-01',@supplier1,@adminId WHERE NOT EXISTS (SELECT 1 FROM tblImportedReceipt4 WHERE importDate='2026-10-01' AND supplierId=@supplier1 AND libraryStaffId=@adminId);
INSERT INTO tblImportedReceipt4 (importDate,supplierId,libraryStaffId)
 SELECT '2026-10-05',@supplier2,@adminId WHERE NOT EXISTS (SELECT 1 FROM tblImportedReceipt4 WHERE importDate='2026-10-05' AND supplierId=@supplier2 AND libraryStaffId=@adminId);
INSERT INTO tblImportedReceipt4 (importDate,supplierId,libraryStaffId)
 SELECT '2026-10-08',@supplier3,@adminId WHERE NOT EXISTS (SELECT 1 FROM tblImportedReceipt4 WHERE importDate='2026-10-08' AND supplierId=@supplier3 AND libraryStaffId=@adminId);
SET @import1=(SELECT MIN(id) FROM tblImportedReceipt4 WHERE importDate='2026-10-01' AND supplierId=@supplier1 AND libraryStaffId=@adminId);
SET @import2=(SELECT MIN(id) FROM tblImportedReceipt4 WHERE importDate='2026-10-05' AND supplierId=@supplier2 AND libraryStaffId=@adminId);
SET @import3=(SELECT MIN(id) FROM tblImportedReceipt4 WHERE importDate='2026-10-08' AND supplierId=@supplier3 AND libraryStaffId=@adminId);
INSERT INTO tblDetailImportedReceipt4 (quantity,unitPrice,importedReceiptId,documentId)
 SELECT d.qty,d.price,d.receipt,d.doc FROM (
 SELECT 5 AS qty,180000 AS price,@import1 AS receipt,@clean AS doc UNION ALL
 SELECT 3,220000,@import1,@patterns UNION ALL SELECT 8,95000,@import1,@algorithms UNION ALL
 SELECT 6,110000,@import2,@network UNION ALL SELECT 4,105000,@import2,@os UNION ALL SELECT 7,120000,@import2,@testing UNION ALL
 SELECT 5,130000,@import3,@security UNION ALL SELECT 4,125000,@import3,@ai UNION ALL SELECT 2,50000,@import3,@repair
 ) d WHERE NOT EXISTS (SELECT 1 FROM tblDetailImportedReceipt4 e WHERE e.importedReceiptId=d.receipt AND e.documentId=d.doc);

INSERT INTO tblLoanSlip4 (borrowingDate,dueDate,status,readerId,libraryStaffId)
 SELECT '2026-11-10','2026-11-24','BORROWING',@tuyenId,@adminId
 WHERE NOT EXISTS (SELECT 1 FROM tblLoanSlip4 WHERE borrowingDate='2026-11-10' AND dueDate='2026-11-24' AND readerId=@tuyenId AND libraryStaffId=@adminId);
INSERT INTO tblLoanSlip4 (borrowingDate,dueDate,status,readerId,libraryStaffId)
 SELECT '2026-10-20','2026-11-03','BORROWING',@tuyenId,@adminId
 WHERE NOT EXISTS (SELECT 1 FROM tblLoanSlip4 WHERE borrowingDate='2026-10-20' AND dueDate='2026-11-03' AND readerId=@tuyenId AND libraryStaffId=@adminId);
INSERT INTO tblLoanSlip4 (borrowingDate,dueDate,status,readerId,libraryStaffId)
 SELECT '2026-10-20','2026-11-03','RETURNED',@demoReaderId,@adminId
 WHERE NOT EXISTS (SELECT 1 FROM tblLoanSlip4 WHERE borrowingDate='2026-10-20' AND dueDate='2026-11-03' AND readerId=@demoReaderId AND libraryStaffId=@adminId);
SET @loan1=(SELECT MIN(id) FROM tblLoanSlip4 WHERE borrowingDate='2026-11-10' AND dueDate='2026-11-24' AND readerId=@tuyenId AND libraryStaffId=@adminId);
SET @loan2=(SELECT MIN(id) FROM tblLoanSlip4 WHERE borrowingDate='2026-10-20' AND dueDate='2026-11-03' AND readerId=@tuyenId AND libraryStaffId=@adminId);
SET @loan3=(SELECT MIN(id) FROM tblLoanSlip4 WHERE borrowingDate='2026-10-20' AND dueDate='2026-11-03' AND readerId=@demoReaderId AND libraryStaffId=@adminId);
-- Decrement each sample open loan's stock only when its detail is first created.
SET @newDetail=(SELECT COUNT(*)=0 FROM tblDetailLoanSlip4 WHERE loanSlipId=@loan1 AND documentId=@clean);
INSERT INTO tblDetailLoanSlip4 (loanSlipId,documentId) SELECT @loan1,@clean WHERE @newDetail=1;
UPDATE tblDocument4 SET quantity=quantity-1 WHERE id=@clean AND @newDetail=1;
SET @newDetail=(SELECT COUNT(*)=0 FROM tblDetailLoanSlip4 WHERE loanSlipId=@loan1 AND documentId=@algorithms);
INSERT INTO tblDetailLoanSlip4 (loanSlipId,documentId) SELECT @loan1,@algorithms WHERE @newDetail=1;
UPDATE tblDocument4 SET quantity=quantity-1 WHERE id=@algorithms AND @newDetail=1;
SET @newDetail=(SELECT COUNT(*)=0 FROM tblDetailLoanSlip4 WHERE loanSlipId=@loan2 AND documentId=@patterns);
INSERT INTO tblDetailLoanSlip4 (loanSlipId,documentId) SELECT @loan2,@patterns WHERE @newDetail=1;
UPDATE tblDocument4 SET quantity=quantity-1 WHERE id=@patterns AND @newDetail=1;
INSERT INTO tblDetailLoanSlip4 (loanSlipId,documentId,actualReturnDate,conditionOnReturn)
 SELECT @loan3,@os,'2026-10-25','Nguyên vẹn'
 WHERE NOT EXISTS (SELECT 1 FROM tblDetailLoanSlip4 WHERE loanSlipId=@loan3 AND documentId=@os);
COMMIT;
