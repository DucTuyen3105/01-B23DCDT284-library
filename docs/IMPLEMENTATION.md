# Đối chiếu pha thiết kế và hướng triển khai

## Quyết định cấu hình

- Nguồn: báo cáo `1B23DCDT284.docx`, mục 2.1–2.3 và Hình 2.1, 2.6, 2.12. Mô hình triển khai có client HTML → LibMan server → DB server. MySQL là lựa chọn cấu hình cho dự án; phần thiết kế không ghi rõ hãng database.
- Giữ các package `model`, `dao` và `view/{member,reader,libraryStaff,managementStaff}`. Giữ hậu tố `4` của tên lớp, bảng và JSP để dễ đối chiếu báo cáo.
- Mỗi lớp thực thể thành một bảng; các bảng con dùng `id` đồng thời là PK và FK đến lớp cha. Không bổ sung cột role ngoài thiết kế; vai trò có thể được xác định từ bảng con khi triển khai xác thực.
- `Member4.fullName` ánh xạ cột SQL `fullname`. DAO cần ánh xạ tường minh.
- `actualReturnDate` dùng Java `LocalDate` và SQL `DATE`, theo biểu đồ lớp; sửa lỗi `INT` trong mục 2.2.2.
- `description` dùng MySQL `TEXT` thay cho kiểu khái niệm `CLOB` trong báo cáo.
- Tiền dùng `BigDecimal`/`DECIMAL(18,2)` thay `double` trong hình để tránh sai số. `ImportedReceipt4.getTotalAmount()` tính từ details, không lưu `totalAmount` trong bảng (đúng mục 2.2.5).
- `Document4.quantity` được hiểu là số bản còn khả dụng; cần xác nhận quy ước này xuyên suốt nhập/mượn/trả khi code. Chưa có mô hình quản lý mã từng bản sao riêng trong báo cáo.
- Các trạng thái mẫu `AVAILABLE`/`UNAVAILABLE` cho tài liệu là quy ước phát triển ban đầu, chưa phải danh mục trạng thái được báo cáo quy định đầy đủ.
- `DAO4` mở connection cho từng thao tác và để caller đóng bằng try-with-resources. Không dùng connection static chia sẻ cho mọi request.
- Ba hàm ghi phiếu/chi tiết/trừ tồn kho nhận cùng một `Connection` để có thể chạy trong **một transaction**. Đây là bổ sung tham số kỹ thuật so với Hình 2.6; tên hàm nghiệp vụ được giữ nguyên.

## Module tìm tài liệu

1. Đã triển khai `DocumentDAO4.getTaiLieuTheoTen(String)` bằng PreparedStatement, `LIKE` escape bằng `!`, tìm literal `%`/`_`/`!`, sắp xếp theo tên và id. Null/blank trả danh sách rỗng; chưa phân trang.
2. Đã triển khai `getTaiLieuTheoID(int)` và ánh xạ đầy đủ 9 thuộc tính. Id không dương hoặc không tồn tại trả `null`; lỗi truy vấn ném `SQLException`.
3. Đã nối đủ ba JSP trực tiếp tới DAO theo scenario 3.0, không thêm tầng controller/service. Dữ liệu hiển thị dùng JSTL `c:out` và URL dùng `c:url`/`c:param`.

### Đối chiếu giao diện và lời gọi module 1

| Trang theo Hình 2.3 và 2.4 | Thành phần triển khai | Luồng theo Hình 2.8 |
| --- | --- | --- |
| ReaderHomeView4.jsp | Tiêu đề Trang chủ độc giả, tên độc giả từ currentReader, nút subFindDocument | Bước 1–3: mở DocumentSearchView4.jsp |
| DocumentSearchView4.jsp | inDocumentName, subSearch, subBack, outDocumentList với 6 cột Mã/Tên tài liệu/Thể loại/Tác giả/Nhà xuất bản/Số lượng, chọn mã subViewDocumentDetail | Bước 4–11: gọi getTaiLieuTheoTen(keyword), DAO đóng gói Document4, JSP hiển thị danh sách; bước 12–13: truyền selectedDocumentId sang documentId của JSP chi tiết |
| DocumentDetailView4.jsp | outDocumentCode, outName, outCategory, outAuthor, outPublisher, outQuantity, subBack | Bước 14–20: gọi lại getTaiLieuTheoID(documentId), DAO đóng gói Document4, JSP hiển thị 6 trường |

Giữ bố cục viền đen, nền trắng, nút chữ nhật theo Hình 2.3. Trang chi tiết không hiển thị thêm năm xuất bản, mô tả hay trạng thái dù model có các thuộc tính này. Quay lại giữ từ khóa tìm kiếm bằng tham số URL, không thêm thành phần giao diện.

Tiền điều kiện đăng nhập: use case đăng nhập bên ngoài module đặt `Reader4` vào session với tên `currentReader`. Module 1 sử dụng đối tượng này để hiển thị tên; không tự tạo tài khoản hay thêm luồng đăng nhập. Mã chi tiết thiếu/sai định dạng/không dương trả HTTP 400, không tồn tại trả 404, lỗi SQL trả 500 và ghi log server. Không thêm hộp thông báo hay trang lỗi riêng ngoài thiết kế; danh sách không có kết quả giữ bảng rỗng.

`Module1FlowTest` chạy JSP thật trong Tomcat bằng JDBC fixture riêng: kiểm tra session độc giả, đúng số cột/trường, điều hướng tìm kiếm/chi tiết/quay lại, nhập tiếng Việt, escape HTML, mã lỗi và truy vấn lại chi tiết sau khi dữ liệu thay đổi.

## Module cho mượn

Đã triển khai ReaderDAO4, ReaderCardDAO4, LoanSlipDAO4, DetailLoanSlipDAO4 và các phương thức module 2 của DocumentDAO4; nối ba JSP theo Hình 2.5, 2.6, 2.10. DAO4 có thuộc tính connection để dùng chung transaction; các chữ ký nghiệp vụ theo biểu đồ lớp được gọi trực tiếp từ JSP. Kiểm tra thẻ/tồn, rollback, chống submit trùng và quyền in đã có. Không thêm chức năng xóa dòng vì thiết kế không có nút này.

Xem [đối chiếu và hướng dẫn module 2](MODULE2.md). Đã triển khai use case đăng nhập riêng bằng MemberDAO4, LoginView4.jsp và AuthenticationFilter4. Session do đăng nhập tạo theo vai trò thực tế từ các bảng kế thừa; không còn tự gán nhân viên mẫu. Xem [đăng nhập](LOGIN.md).

Các chức năng quản lý tài liệu/độc giả/nhà cung cấp, trả tài liệu, nhập tài liệu và báo cáo thống kê chưa có DAO/giao diện chi tiết trong bộ khung này; các thực thể và bảng đã được chuẩn bị theo thiết kế toàn hệ thống.
