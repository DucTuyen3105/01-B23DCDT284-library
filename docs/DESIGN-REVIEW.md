# Rà soát giao diện hai module theo báo cáo

Nguồn đối chiếu: Hình 2.3 (giao diện module 1), Hình 2.5 (giao diện module 2), cùng Hình 2.4/2.6 và các scenario 3.0. Đã đọc mã JSP/CSS, render sáu trang bằng Chrome và đối chiếu với hai hình nguồn.

Theo yêu cầu tinh gọn tiếp theo, đã bỏ nhãn tên lớp ở cuối cả sáu màn hình và hai dòng hướng dẫn thường trực (chọn mã tài liệu, chỉ bật in sau khi lưu). Giữ các thành phần thao tác/dữ liệu theo thiết kế tĩnh; thông báo kết quả/lỗi chỉ xuất hiện khi có thao tác. Bỏ phần chiều cao và khoảng trống dành riêng cho các dòng đã xóa.

## Các sai lệch đã sửa

- Bỏ dropdown readerLookupMode tự thêm. Trang lập phiếu chỉ hiển thị nhãn tĩnh **Tra theo: Mã độc giả / Mã thẻ**, một ô Nhập hoặc quét mã và nút Xác định như Hình 2.5. Không thay bằng radio, checkbox hoặc bộ chọn khác.
- Bỏ CSS làm mờ nút bị vô hiệu; nút In phiếu mượn giữ chữ/viền đen và nền trắng như hình. Thuộc tính disabled vẫn giữ đúng yêu cầu chỉ cho in sau khi lưu thành công.
- Chỉnh chiều rộng nút In phiếu mượn theo tỷ lệ hình mẫu, rộng hơn nút Xác nhận.
- Cập nhật tài liệu hướng dẫn để không còn mô tả bộ chọn tự thêm.

## Kết quả đối chiếu thành phần

| Trang | Thành phần theo hình đã kiểm tra |
| --- | --- |
| ReaderHomeView4 | Tiêu đề, tên độc giả từ session, một nút Tìm kiếm tài liệu |
| DocumentSearchView4 | Tiêu đề, nhãn Tên tài liệu và ô nhập, Tìm kiếm/Về trang chủ, Kết quả tìm kiếm, bảng 6 cột Mã/Tên tài liệu/Thể loại/Tác giả/Nhà xuất bản/Số lượng, chọn mã để xem chi tiết |
| DocumentDetailView4 | Tiêu đề, đúng 6 trường Mã tài liệu/Tên tài liệu/Thể loại/Tác giả/Nhà xuất bản/Số lượng, một nút Quay lại |
| LibraryStaffHomeView4 | Tiêu đề, tên nhân viên từ session, một nút Cho mượn tài liệu |
| LoanSlipView4 | Nhãn tra mã tĩnh, một ô mã độc giả/thẻ và Xác định, thông tin độc giả/thẻ/hết hạn, một ô mã tài liệu và Tra cứu/Thêm tài liệu, thông tin tài liệu đang xem, bảng 5 cột, ngày mượn/hạn trả, Xác nhận/In phiếu mượn |
| LoanSlipPrintView4 | Tiêu đề, mã phiếu, độc giả/nhân viên, ngày mượn/hạn trả, bảng 2 cột Mã tài liệu/Tên tài liệu, một nút In |

Không thêm cột trạng thái, mô tả, năm xuất bản; không thêm nút xóa dòng, đăng xuất, bộ lọc hay menu vào sáu màn hình này. Đăng nhập/đăng xuất là phần được yêu cầu bổ sung riêng, không làm thay đổi thành phần hai module. Tên, mã, ngày và số dòng là dữ liệu thực từ DB nên khác ví dụ trong hình.

## Luồng và kiểm chứng

Module 1 vẫn giữ JSP tìm kiếm → getTaiLieuTheoTen → danh sách Document4; chọn mã → JSP chi tiết → getTaiLieuTheoID → Document4 → hiển thị.

Module 2 dùng một ô mã, gọi ReaderDAO4 để tra mã độc giả và mã thẻ rồi gọi ReaderCardDAO4 cho người xác định được. readerLookupMode chỉ là trạng thái nội bộ phía server. Mã trùng giữa hai người bị từ chối bằng dòng trạng thái sẵn có, không thêm điều khiển ngoài hình và không nhận mode từ client để tự chọn người. Phần tra tài liệu, đóng gói/lưu phiếu → lưu chi tiết → trừ tồn, rồi đọc lại phiếu để in giữ nguyên thứ tự đã triển khai.

Đã kiểm tra trực tiếp MySQL bằng admin/1234 và tuyen/1234, nhập mã độc giả 5 và mã thẻ 2 qua cùng ô, xem đủ sáu trang. Chỉ tạo bản nháp trong session để kiểm tra; không lưu thêm phiếu hay thay đổi tồn kho. Build verify đạt **19 kiểm tra**, gồm kiểm tra không có select/option, số nút/cột, mã thẻ hết hạn và mã trùng giữa hai độc giả.
