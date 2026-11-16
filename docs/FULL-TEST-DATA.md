# Bộ dữ liệu kiểm thử đầy đủ

Đã nạp trực tiếp vào MySQL đang chạy. URL đăng nhập: **http://localhost:8080/libman/**.

| Vai trò | Tài khoản | Mật khẩu | Họ tên |
| --- | --- | --- | --- |
| Nhân viên thư viện | admin | 1234 | Nhân viên thư viện Admin |
| Độc giả | tuyen | 1234 | Nguyễn Đức Tuyên |

Tên `admin` mang vai trò LibraryStaff theo yêu cầu, không phải ManagementStaff. Mật khẩu được lưu bằng PBKDF2 có salt riêng. Các tài khoản demo cũ vẫn được giữ nguyên.

## Dữ liệu hiện tại

| Bảng | Số bản ghi sau seed |
| --- | ---: |
| tblMember4 | 8 |
| tblEmployee4 | 3 |
| tblReader4 | 5 |
| tblLibraryStaff4 | 2 |
| tblManagementStaff4 | 1 |
| tblReaderCard4 | 4 |
| tblDocument4 | 23 |
| tblSupplier4 | 3 |
| tblLoanSlip4 | 3 |
| tblDetailLoanSlip4 | 4 |
| tblImportedReceipt4 | 3 |
| tblDetailImportedReceipt4 | 9 |

## Ca kiểm thử trên database đang chạy

Mã tự tăng có thể khác khi nạp vào database khác. Các mã dưới đây đã được kiểm tra trên database hiện tại.

| Ca kiểm thử | Dữ liệu để nhập |
| --- | --- |
| Độc giả hợp lệ | Mã độc giả **5**, mã thẻ **2** — tài khoản tuyen |
| Thẻ hết hạn | Mã độc giả **6**, mã thẻ **3** |
| Độc giả INACTIVE | Mã độc giả **7**, mã thẻ **4** |
| Độc giả chưa có thẻ | Mã độc giả **8** |
| Tài liệu theo hình thiết kế | Mã **4** — Đắc Nhân Tâm |
| Tài liệu có sẵn | Mã **19** — Clean Code, còn **4** sau phiếu mẫu |
| Tài liệu có sẵn | Mã **20** — Design Patterns, còn **2** sau phiếu mẫu |
| Hết số lượng | Mã **27** — Tài liệu đã hết số lượng |
| Có số lượng nhưng không cho mượn | Mã **28** — Tài liệu đang sửa chữa, quantity=2, status=REPAIR |

Đăng nhập admin để lập phiếu cho độc giả 5. Chọn nhiều tài liệu rồi xác nhận sẽ ghi dữ liệu thật và trừ số lượng. Đăng nhập tuyen để kiểm tra tên độc giả, tìm kiếm và xem chi tiết tài liệu.

Phiếu mẫu do admin lập, có thể xem bằng tài khoản admin:

- [PM002 đang mượn](http://localhost:8080/libman/view/libraryStaff/LoanSlipPrintView4.jsp?loanSlipId=2): tuyen mượn Clean Code và Cấu trúc dữ liệu và giải thuật; ngày mượn 10/11/2026, hạn trả 24/11/2026.
- [PM003 quá hạn](http://localhost:8080/libman/view/libraryStaff/LoanSlipPrintView4.jsp?loanSlipId=3): tuyen mượn Design Patterns; hạn trả 03/11/2026, chưa trả. Trạng thái lưu BORROWING, quá hạn được xác định bằng ngày.
- [PM004 đã trả](http://localhost:8080/libman/view/libraryStaff/LoanSlipPrintView4.jsp?loanSlipId=4): độc giả demo mượn Hệ điều hành, đã trả ngày 25/10/2026 với tình trạng Nguyên vẹn.

Ba nhà cung cấp có phiếu nhập và chi tiết đơn giá/số lượng. Đây là dữ liệu database để kiểm thử các quan hệ; giao diện quản lý, nhập/trả và thống kê chưa được triển khai ngoài hai module đã có.

## Chạy lại seed

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\seed-full-test.ps1
```

`06-full-test.sql` chỉ bổ sung bản ghi chưa có; chỉ lần đầu thêm chi tiết phiếu đang mượn mới trừ số lượng. Chạy lại không nhân đôi phiếu/chi tiết và không trừ tồn thêm. Không reset các giao dịch mượn người dùng đã tạo. Riêng mật khẩu admin/tuyen được đặt lại thành 1234 theo yêu cầu, và độc giả tuyen được đặt ACTIVE.

File được sinh từ `06-full-test.sql.template` bằng `dev.CreateFullTestSeed`, thay placeholder bằng hash có salt. Compose mới chạy file 06 sau các seed 01–05. Muốn sinh lại sau khi sửa template:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\mvnw.ps1 test-compile exec:java '-Dexec.mainClass=dev.CreateFullTestSeed'
```

Đã kiểm tra hai lần nạp cho cùng số bản ghi và tổng số lượng, xác thực hai tài khoản trên MySQL, bốn trường hợp độc giả/thẻ và quyền xem ba phiếu mẫu qua HTTP.
