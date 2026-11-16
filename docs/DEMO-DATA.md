# Dữ liệu mẫu để xem module 1

`database/03-demo-data.sql` bổ sung 10 tài liệu, một độc giả `reader_demo` (Nguyễn Văn A) và một thẻ đang hoạt động. Script chỉ thêm bản ghi chưa có, không xóa hay ghi đè tài liệu đang tồn tại. Chạy tiếp `05-login-demo.sql` để kích hoạt tài khoản `reader_demo` / `Reader@123`; đăng nhập trước khi mở các liên kết tìm kiếm.

Mở trang tìm kiếm đang chạy:

- [Đắc Nhân Tâm](http://localhost:8080/libman/view/reader/DocumentSearchView4.jsp?keyword=%C4%90%E1%BA%AFc%20Nh%C3%A2n%20T%C3%A2m): tài liệu theo mẫu Hình 2.3, số lượng 2.
- [Java](http://localhost:8080/libman/view/reader/DocumentSearchView4.jsp?keyword=Java): nhiều kết quả, gồm tài liệu hết số lượng.
- Thử `cơ sở dữ liệu`, `Dale Carnegie` (tìm theo tên nên không khớp tác giả), `Tạp chí`, `%` hoặc `_`.

Chọn mã tài liệu để xem chi tiết. Tên độc giả trên trang chủ vẫn lấy từ session đăng nhập `currentReader`, không tự lấy bản ghi mẫu từ database.

Khi tạo database mới bằng Compose, script được chạy sau schema và seed cơ bản. Với database đã có, có thể chạy lại bằng PowerShell:

```powershell
docker compose cp ./database/03-demo-data.sql db:/tmp/libman-demo.sql
docker compose exec -T db sh -c 'MYSQL_PWD=$MYSQL_PASSWORD mysql --default-character-set=utf8mb4 -u$MYSQL_USER $MYSQL_DATABASE < /tmp/libman-demo.sql'
```
