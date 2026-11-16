# Đăng nhập LibMan

Mở `http://localhost:8080/libman/` hoặc `/libman/view/member/LoginView4.jsp`. Form gồm tên đăng nhập, mật khẩu và nút Đăng nhập, giữ kiểu đen trắng của dự án. Không thay đổi thành phần giao diện module 1/2.

Mở lại URL luôn hiện form, kể cả khi đang có phiên đăng nhập. Có thể nhập tài khoản khác ngay; chỉ khi thông tin mới được xác minh thành công thì session cũ và vai trò cũ mới bị xóa, rồi tạo phiên cho tài khoản mới.

## Luồng

1. LoginView4.jsp nhận POST, kiểm tra token CSRF, gọi `MemberDAO4.dangNhap(username,password)`.
2. DAO tìm username bằng PreparedStatement; xác minh password với hash PBKDF2-HMAC-SHA256, salt riêng 16 byte, 600000 vòng lặp, kết quả 32 byte. Giá trị lưu dạng `pbkdf2-sha256$iterations$salt$hash`, nằm trong VARCHAR(255) hiện có. Không hỗ trợ so sánh mật khẩu thuần.
3. Vai trò lấy từ tblReader4, tblLibraryStaff4 hoặc tblManagementStaff4, không nhận role từ client. Tài khoản không có vai trò, có nhiều vai trò mâu thuẫn, độc giả không ACTIVE, hoặc mật khẩu sai đều bị từ chối với thông báo chung.
4. Thành công: hủy session cũ để đổi session id và xóa bản nháp, tạo session mới; lưu currentMember (không có password hash) và currentReader/currentStaff/currentManagementStaff theo đúng kiểu đối tượng; chuyển về trang chủ tương ứng.
5. AuthenticationFilter4 bảo vệ toàn bộ ba thư mục vai trò. Chưa đăng nhập chuyển về LoginView4.jsp; đã đăng nhập nhưng sai vai trò trả 403. Trang đăng nhập và các trang được bảo vệ gửi Cache-Control: no-store. Session hết hạn sau 30 phút theo web.xml.
6. LogoutView4.jsp hiển thị nút Đăng xuất; POST kèm token hợp lệ mới hủy session. GET không tự đăng xuất. URL: `/libman/view/member/LogoutView4.jsp`.

Các tài khoản local: `reader_demo` / `Reader@123`, `staff_demo` / `Staff@123`, `manager_demo` / `Manager@123`. Seed 05 chỉ kích hoạt giá trị mật khẩu demo vô hiệu hoặc tạo tài khoản mới, giữ mật khẩu đã thay đổi. Seed không làm độc giả INACTIVE trở lại ACTIVE. Các chức năng đăng ký và đổi mật khẩu chưa nằm trong thay đổi này.

Seed 06 bổ sung tài khoản theo yêu cầu: **admin / 1234** (LibraryStaff) và **tuyen / 1234** (Reader), cùng bộ dữ liệu đủ 12 bảng. Hai mật khẩu này được đặt lại mỗi lần chạy seed 06; xem [dữ liệu kiểm thử đầy đủ](FULL-TEST-DATA.md).

`dev.CreateLoginSeed` sinh file SQL bằng hash ngẫu nhiên có salt; chỉ dùng cho dữ liệu mẫu local. `PasswordHash4.hash()` có thể dùng khi triển khai tạo/đổi mật khẩu sau này. Runner StartServer không còn hỗ trợ cơ chế tự gán session `demo.staff.username`.

`LoginFlowTest` kiểm tra HTTP/JSP thực với database tách biệt: ba vai trò, chuyển trang và đổi session id, chặn URL trực tiếp/sai vai trò, thông tin sai hoặc tài khoản không hoạt động, CSRF, đăng xuất và định dạng hash. Các kiểm tra module 1/2 tiếp tục dùng session tiền điều kiện trong fixture để kiểm tra nghiệp vụ riêng.
