# Module 2 Cho độc giả mượn tài liệu

Đã triển khai theo Hình 2.5, Hình 2.6 và Hình 2.10 cùng scenario 3.0 trong mục 2.3.2.2 của báo cáo. Giao diện gồm đúng ba JSP, giữ bố cục đen trắng và các thành phần trong thiết kế.

## Chạy và xem thử

MySQL cần có dữ liệu từ `03-demo-data.sql` và `04-module2-demo.sql`. Các script chỉ thêm bản ghi chưa tồn tại. Nếu database đã có:

```powershell
docker compose cp ./database/04-module2-demo.sql db:/tmp/libman-module2-demo.sql
docker compose exec -T db sh -c 'MYSQL_PWD=$MYSQL_PASSWORD mysql --default-character-set=utf8mb4 -u$MYSQL_USER $MYSQL_DATABASE < /tmp/libman-module2-demo.sql'
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-module2-demo.ps1
```

Mở **http://localhost:8080/libman/view/member/LoginView4.jsp**, đăng nhập `staff_demo` / `Staff@123`. Cần nạp `05-login-demo.sql` để kích hoạt mật khẩu mẫu. Login đặt `LibraryStaff4` vào session `currentStaff`; cơ chế tự gán session demo đã được loại bỏ. Script `start-module2-demo.ps1` được giữ để tương thích nhưng giờ chạy server thông thường trên cổng 8080 và yêu cầu đăng nhập.

Trong database phát triển đã seed hiện tại, độc giả Nguyễn Văn A có **mã độc giả 1**, **mã thẻ 1**; tài liệu Đắc Nhân Tâm có **mã 4**. Mã tự tăng có thể khác ở database khác. Chọn Cho mượn tài liệu → nhập mã độc giả và Xác định → nhập mã tài liệu và Tra cứu → Thêm tài liệu → lặp lại nếu cần → Xác nhận → In phiếu mượn → In. Xác nhận lưu dữ liệu thật và giảm số lượng khả dụng. Vào lại từ trang chủ nhân viên để bắt đầu phiếu mới.

## Đối chiếu giao diện và lời gọi

| Trang | Thành phần theo Hình 2.5 và 2.6 | Luồng |
| --- | --- | --- |
| LibraryStaffHomeView4.jsp | Tiêu đề, tên currentStaff, subLendDocument | Mở LoanSlipView4.jsp |
| LoanSlipView4.jsp | readerLookupMode, inReaderCode, Xác định, outReaderInformation; inDocumentCode, subSelectDocument, subAddDocument, outDocumentInformation; outDocumentList gồm 5 cột; ngày mượn, hạn trả; subSubmit, subPrint | ReaderDAO4.getReaderTheoID()/getReaderTheoMaThe() → ReaderCardDAO4.getTheCuaReader(); DocumentDAO4.getTaiLieuTheoID() → kiemTraKhaDung(); thêm DetailLoanSlip4 vào LoanSlip4 |
| LoanSlipView4.jsp khi Xác nhận | Các thành phần sẵn có, thông báo tại dòng trạng thái | Đóng gói LoanSlip4 → LoanSlipDAO4.taoPhieuMuon() → DetailLoanSlipDAO4.taoChiTietPhieuMuon() → DocumentDAO4.capNhatSoLuong() |
| LoanSlipPrintView4.jsp | outLoanSlipInformation: mã phiếu, độc giả, nhân viên, ngày mượn, hạn trả, bảng 2 cột; nút subPrint | Nhận loanSlipId → LoanSlipDAO4.getPhieuMuonTheoID() → hiển thị → window.print() |

Các phương thức ghi có đúng tên và danh sách tham số nghiệp vụ trong Hình 2.6; connection của DAO4 được truyền qua constructor để cùng transaction. Giữ overload có tham số Connection từ bộ khung để tương thích. Không thêm controller/service hoặc nút chức năng khác. Theo Hình 2.5, dòng “Tra theo: Mã độc giả / Mã thẻ” là nhãn tĩnh gắn với một ô nhập/quét mã; không có dropdown, radio hay bộ chọn. readerLookupMode được xác định phía server sau khi tra mã.

## Quy tắc xử lý

- Kiểm tra độc giả ACTIVE, thẻ ACTIVE trong thời hạn; quét thẻ phải khớp đúng thẻ hợp lệ. Nhiều thẻ cùng hợp lệ bị xem là dữ liệu không xác định, không cho mượn.
- Một ô nhận cả mã độc giả và mã thẻ: ReaderDAO4 tra hai loại mã, sau đó ReaderCardDAO4 lấy thẻ của độc giả xác định được. Nếu cùng một số trỏ đến hai độc giả khác nhau, không tự chọn một người; thông báo trên dòng trạng thái sẵn có. Hình không quy định cách phân biệt mã trùng và không có bộ chọn, nên cần nhập một mã xác định duy nhất độc giả. Với mã vừa là mã độc giả vừa là mã thẻ của cùng người, dùng nhánh mã độc giả.
- Mỗi tài liệu chỉ thêm một lần trong một phiếu theo mô hình chi tiết không có trường số lượng. Cần tra cứu trước khi thêm. Không mượn tài liệu hết số lượng hoặc không AVAILABLE.
- Ngày mượn lấy ngày xác nhận; hạn trả mặc định sau 14 ngày vì báo cáo chưa quy định thời hạn cụ thể. Có thể đặt JVM property `loan.days`; giao diện chỉ hiển thị hai ngày như thiết kế.
- JSP kiểm tra lại độc giả/thẻ trong transaction, khóa hàng khi kiểm tra; sau đó ghi phiếu, chi tiết, cập nhật số lượng theo thứ tự thiết kế. Các DAO ghi không tự commit/đóng connection được truyền vào. Nếu có lỗi, rollback toàn bộ.
- Cập nhật tồn bằng `quantity=quantity-1 WHERE quantity>0 AND status='AVAILABLE'`, xử lý tài liệu theo mã tăng dần; không cho hai giao dịch cùng mượn bản cuối cùng. quantity bằng 0 tự bị xem là không khả dụng kể cả cột status vẫn AVAILABLE.
- Bản nháp nằm trong session, không lấy reader/staff/danh sách chi tiết từ dữ liệu client khi xác nhận. CSRF token nằm trong trường ẩn; sau khi lưu đổi token và khóa phiếu để ngăn submit trùng. Thao tác tạo phiếu mới bắt đầu từ trang chủ.
- Chỉ bật In phiếu mượn sau commit. Trang in đọc lại phiếu từ database và kiểm tra nhân viên trong session khớp nhân viên lập phiếu. Không in dữ liệu bản nháp.
- Thông báo lỗi/thành công nằm trên trang lập phiếu theo scenario; không thêm trang giao diện mới.

## Kiểm tra

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\mvnw.ps1 -B verify
powershell -NoProfile -ExecutionPolicy Bypass -File .\mvnw.ps1 test-compile exec:java '-Dexec.mainClass=dev.CheckModule2'
```

`Module2FlowTest` chạy JSP thật với Tomcat và H2 tách biệt: xác định độc giả/thẻ, thêm nhiều tài liệu, chặn trùng, lưu một lần, đọc lại phiếu in, kiểm tra quyền, dữ liệu sai/hết hạn, rollback khi mất tồn và hai giao dịch tranh bản cuối. `CheckModule2` chạy chuỗi DAO trên MySQL trong transaction **luôn rollback**, giữ nguyên dữ liệu và số lượng hiện có; auto increment có thể bỏ qua mã sau kiểm tra rollback.
