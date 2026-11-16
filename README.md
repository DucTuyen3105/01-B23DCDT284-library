# LibMan

LibMan là ứng dụng web quản lý thư viện, hỗ trợ độc giả tìm kiếm thông tin tài liệu và nhân viên thư viện lập, lưu, in phiếu mượn.

Ứng dụng sử dụng Java 8, JSP/JSTL, JDBC MySQL và Maven để đóng gói WAR. Các chức năng đã triển khai gồm đăng nhập theo vai trò, băm mật khẩu PBKDF2, kiểm tra quyền truy cập, tìm kiếm tài liệu và cho mượn tài liệu. Dự án kèm dữ liệu mẫu, kiểm thử tự động và cấu hình hot reload/debug cho môi trường phát triển.

## 1. Mở dự án và kiểm tra build

Mở thư mục này trong IntelliJ IDEA, Eclipse, NetBeans hoặc VS Code có Java Extension Pack. Import `pom.xml` dưới dạng Maven project; chọn JDK 8 hoặc mới hơn. Không cần cài Maven toàn hệ thống: script tải Maven 3.9.11 từ Maven Central, xác minh SHA-512, lưu vào `.tools/`.

Chạy trong PowerShell tại thư mục dự án:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\mvnw.ps1 -B verify
```

Lần đầu cần Internet để tải Maven và dependency. `Bypass` chỉ áp dụng cho tiến trình này, không thay đổi chính sách PowerShell của máy. Nếu IDE yêu cầu Maven home, chọn `.tools/apache-maven-3.9.11`. File WAR nằm ở `target/libman.war`.

## 2. Chạy web để bắt đầu code giao diện

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\dev.ps1
```

Mở **http://localhost:8080/libman/**. Nhấn `Ctrl+C` để dừng. Đây là Tomcat nhúng dùng riêng cho phát triển, chỉ lắng nghe trên máy cục bộ; không cần cài Tomcat riêng và không cần database để xem các trang khung. Sửa JSP/CSS rồi tải lại trang; sửa Java thì script tự biên dịch và Tomcat nạp lại ứng dụng. Để debug bằng F5 và Hot Code Replace trong VS Code, xem [hướng dẫn phát triển](docs/DEVELOPMENT.md).

Nếu cổng 8080 đã bận:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\dev.ps1 -Port 8081
```

Khi chạy WAR bằng IDE hoặc Tomcat độc lập, chọn **Tomcat 9**, context path `/libman`. Tomcat 9 hỗ trợ `javax.servlet` 4.0 và JSP 2.3, chạy được trên Java 8: [tài liệu Apache](https://tomcat.apache.org/migration-9.html). Các dependency Tomcat nhúng/H2/JUnit có scope `test` nên không đi vào WAR.

## 3. Khởi tạo MySQL

Mở Docker Desktop và chờ engine sẵn sàng. Chạy:

```powershell
Copy-Item .env.example .env
docker compose up -d db
docker compose ps
docker compose logs db
```

Chờ container báo `healthy`. MySQL 8.4 được mở tại `localhost:3307`, database/user là `libman`, mật khẩu mặc định phát triển là `libman_dev`. Schema và 3 tài liệu mẫu được nạp tự động **chỉ khi volume còn mới**. Sửa SQL sau lần chạy đầu cần áp dụng thay đổi bằng migration hoặc công cụ SQL; `docker compose down` giữ nguyên dữ liệu.

Nếu đã có MySQL riêng, tạo database `libman` với charset `utf8mb4`, tạo user có quyền trên database đó, rồi lần lượt chạy `database/01-schema.sql` và `database/02-seed.sql`. Dùng DBeaver, MySQL Workbench hoặc CLI MySQL để chạy file SQL.

Ứng dụng đọc cấu hình theo thứ tự: JVM property `-DDB_*` → biến môi trường `DB_*` → `src/main/resources/db.properties`. File `.env` chỉ được Docker Compose tự đọc, Java không tự đọc file này. Để dùng thông tin khác mặc định, đặt biến trong cùng cửa sổ PowerShell trước khi chạy Maven:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3307/libman?connectionTimeZone=UTC&characterEncoding=UTF-8'
$env:DB_USER = 'libman'
$env:DB_PASSWORD = 'libman_dev'
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-db.ps1
```

Lệnh kiểm tra phải in đủ 12 bảng và số bản ghi. Khi đổi `MYSQL_*` trong `.env`, cập nhật `DB_*` tương ứng; thông tin MySQL trong volume đã khởi tạo không tự đổi theo `.env`. Thông tin mặc định chỉ dành cho máy phát triển. `database/05-login-demo.sql` tạo tài khoản đăng nhập mẫu bằng mật khẩu đã băm; database mới chạy tự động sau các seed còn lại.

## 4. Cấu trúc và nơi bắt đầu code

```text
src/main/java/model/       Các thực thể của hệ thống thư viện
src/main/java/dao/         DAO4 và 5 DAO của hai module
src/main/resources/        Cấu hình JDBC
src/main/webapp/view/
  member/                  Khung đăng nhập
  reader/                  ReaderHomeView4, DocumentSearchView4, DocumentDetailView4
  libraryStaff/            LibraryStaffHomeView4, LoanSlipView4, LoanSlipPrintView4
  managementStaff/         Khung trang quản lý
database/                  12 bảng và dữ liệu tài liệu mẫu
src/test/java/dev/         Runner Tomcat và kiểm tra MySQL
src/test/java/setup/       Kiểm tra schema bằng H2 chế độ MySQL
docs/DEVELOPMENT.md        Hướng dẫn hot reload và debug
```

Module tìm kiếm gọi trực tiếp từ JSP đến `DocumentDAO4.getTaiLieuTheoTen()` và `getTaiLieuTheoID()`. Kiểm tra tự động gồm DAO, schema và toàn bộ luồng HTTP/JSP với Tomcat cùng H2 tách biệt dữ liệu phát triển.

Để kiểm tra giao diện, khởi động MySQL và web rồi mở **http://localhost:8080/libman/view/reader/ReaderHomeView4.jsp**. Chọn “Tìm kiếm tài liệu”, nhập `Java`, nhấn “Tìm kiếm”, chọn mã tài liệu để xem chi tiết, rồi “Quay lại”.

Đăng nhập bằng `reader_demo` trước khi xem module 1. Login đặt `Reader4` vào session `currentReader`; trang chủ hiển thị tên từ đối tượng này. Chưa đăng nhập thì URL được chuyển về trang đăng nhập; tài khoản sai vai trò nhận HTTP 403.

## 5. Kiểm tra DAO module 1

Chạy kiểm tra tự động, không cần MySQL và không thay đổi dữ liệu phát triển:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\mvnw.ps1 -B test
```

Chạy kiểm tra chỉ đọc trên MySQL đã cấu hình (mặc định tìm `Java`, sau đó lấy chi tiết từng kết quả):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\mvnw.ps1 test-compile exec:java '-Dexec.mainClass=dev.CheckModule1'
```

Đổi từ khóa bằng tham số, ví dụ:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\mvnw.ps1 test-compile exec:java '-Dexec.mainClass=dev.CheckModule1' '-Dexec.args=Java'
```

`getTaiLieuTheoTen(keyword)` bỏ khoảng trắng đầu/cuối; đầu vào null/rỗng hoặc không có kết quả trả danh sách rỗng. Các ký tự `%`, `_`, `!` được tìm như ký tự thường; phân biệt hoa/thường và dấu phụ thuộc collation MySQL. Tài liệu hết số lượng vẫn xuất hiện trong kết quả. `getTaiLieuTheoID(id)` trả `null` nếu id không dương hoặc không tồn tại. Lỗi SQL được truyền lên caller dưới dạng `SQLException` để giao diện xử lý, không bị đổi thành kết quả rỗng.

## 6. Xem module 2

Chạy web rồi đăng nhập bằng nhân viên `staff_demo` – Trần Thị B:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\dev.ps1
```

Mở **http://localhost:8080/libman/** và đăng nhập. Chọn Cho mượn tài liệu → mã độc giả `1` → Xác định → mã tài liệu `4` (Đắc Nhân Tâm) → Tra cứu → Thêm tài liệu → Xác nhận → In phiếu mượn. Mã ở database khác có thể thay đổi. Xác nhận ghi phiếu thật và giảm tồn kho. Xem [hướng dẫn module 2](docs/MODULE2.md) để nạp seed và kiểm tra transaction.

Session nhân viên được tạo bởi đăng nhập; đã loại bỏ cơ chế tự gán nhân viên mẫu. Hạn trả mặc định sau 14 ngày, điều chỉnh bằng JVM property `loan.days`.

## 7. Tài khoản đăng nhập

URL: **http://localhost:8080/libman/view/member/LoginView4.jsp**

Tài khoản kiểm thử trong bộ dữ liệu mẫu: **nhân viên thư viện `admin` / `1234`**, **độc giả `tuyen` / `1234`**. Bộ dữ liệu đầy đủ có 23 tài liệu và đủ 12 bảng; xem [mã dữ liệu và các ca kiểm thử](docs/FULL-TEST-DATA.md). Độc giả tuyen có mã **5**, mã thẻ **2**.

| Vai trò | Tên đăng nhập | Mật khẩu mẫu | Trang sau đăng nhập |
| --- | --- | --- | --- |
| Nhân viên thư viện | admin | 1234 | Trang chủ nhân viên thư viện |
| Độc giả | tuyen | 1234 | Trang chủ độc giả |
| Độc giả | reader_demo | Reader@123 | Trang chủ độc giả |
| Nhân viên thư viện | staff_demo | Staff@123 | Trang chủ nhân viên thư viện |
| Nhân viên quản lý | manager_demo | Manager@123 | Trang nhân viên quản lý (chức năng thống kê chưa triển khai) |

Database hiện tại đã nạp seed. Với database có sẵn ở máy khác:

```powershell
docker compose cp ./database/05-login-demo.sql db:/tmp/libman-login-demo.sql
docker compose exec -T db sh -c 'MYSQL_PWD=$MYSQL_PASSWORD mysql --default-character-set=utf8mb4 -u$MYSQL_USER $MYSQL_DATABASE < /tmp/libman-login-demo.sql'
```

Seed chỉ kích hoạt mật khẩu mẫu đang mang giá trị `!DEMO_LOGIN_DISABLED!` hoặc tạo tài khoản mới; không đặt lại mật khẩu đã được thay đổi. Đăng xuất/đổi tài khoản tại **http://localhost:8080/libman/view/member/LogoutView4.jsp**, nhấn Đăng xuất. Xem [chi tiết đăng nhập](docs/LOGIN.md).

Phiên bản công cụ đã được ghim trong `pom.xml` để build có thể lặp lại. Maven 3.9 chạy được trên JDK 8 theo [tài liệu Maven](https://maven.apache.org/docs/3.9.0/release-notes.html); driver MySQL Connector/J 9.7 hỗ trợ MySQL 8.0 trở lên theo [release notes MySQL](https://dev.mysql.com/doc/relnotes/connector-j/en/news-9-7-0.html).
