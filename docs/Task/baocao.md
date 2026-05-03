📘 ĐỒ ÁN HỆ THỐNG ĐẶT LỊCH KHÁM BỆNH
2.3 USE CASE DIAGRAM
UC11 – Quản lý lịch làm việc

Actor: Bác sĩ

Chức năng:

Đăng nhập
Xem lịch làm việc
Thêm lịch làm việc
Cập nhật lịch làm việc
Xóa lịch làm việc

📌 Theo sơ đồ trang 24

UC12 – Khám bệnh

Actor: Bác sĩ

Chức năng:

Đăng nhập
Xem thông tin bệnh nhân
Nhập kết quả khám
Ghi chú bệnh án

📌 Theo sơ đồ trang 25

UC13 – Quản lý lịch hẹn bệnh nhân

Actor: Bác sĩ

Chức năng:

Đăng nhập
Xem lịch hẹn
Từ chối lịch hẹn

📌 Theo sơ đồ trang 25

UC14 – Quản lý hồ sơ bệnh án

Actor: Bác sĩ

Chức năng:

Xem hồ sơ bệnh án
Cập nhật bệnh án

📌 Trang 26

UC15 – Quản lý hệ thống

Actor: Admin

Chức năng:

Quản lý tài khoản
Quản lý bác sĩ
Quản lý nhân sự
Giám sát hệ thống
Thống kê & báo cáo

📌 Trang 26

2.4 USE CASE SPECIFICATION
UC01 – Đăng ký
Actor: Bệnh nhân
Tiền điều kiện: Chưa có tài khoản
Hậu điều kiện: Tạo tài khoản thành công
Main Flow
Truy cập trang đăng ký
Nhập thông tin (email, mật khẩu)
Xác nhận mật khẩu
Submit
Validate dữ liệu
Check email tồn tại
Tạo tài khoản
Lưu DB
Thông báo thành công
Alternate Flow
Email tồn tại / dữ liệu sai

📌 Trang 27

UC02 – Đăng nhập
Actor: Thành viên
Tiền điều kiện: Có tài khoản
Main Flow
Nhập email + password
Validate
Tạo session
Redirect trang chính
Alternate Flow
Sai thông tin

📌 Trang 28

UC03 – Quên mật khẩu
Flow
Nhập email
Gửi OTP/link
Nhập OTP
Nhập mật khẩu mới
Cập nhật DB

📌 Trang 29

UC04 – Đăng xuất
Flow
Click logout
Hủy session
Redirect login

📌 Trang 29

UC05 – Xem thông tin bác sĩ
Flow
Xem danh sách bác sĩ
Chọn bác sĩ
Hiển thị thông tin

📌 Trang 30

UC06 – Đặt lịch hẹn
Flow
Chọn bác sĩ
Hiển thị slot trống
Chọn ngày + giờ
Nhập triệu chứng
Xác nhận
Tạo lịch
Lưu DB
Gửi thông báo
Alternate
Slot đã bị đặt

📌 Trang 31

UC07 – Xem lịch đã đặt
Flow
Truy cập "Lịch của tôi"
Query DB
Hiển thị danh sách

📌 Trang 31

UC08 – Hủy lịch
Flow
Chọn lịch
Confirm
Update trạng thái
Thông báo
Alternate
Quá hạn

📌 Trang 32

UC09 – Nhận thông báo
Flow
Event xảy ra
Tạo nội dung
Gửi email/SMS
User nhận

📌 Trang 32

UC10 – Xem hồ sơ bệnh án
Flow
Check permission
Query DB
Hiển thị

📌 Trang 33

UC11 – Quản lý lịch làm việc
Flow
Truy cập
CRUD lịch
Validate
Lưu DB

📌 Trang 34

UC12 – Khám bệnh
Flow
Chọn lịch hẹn
Xem thông tin bệnh nhân
Nhập kết quả khám
Lưu hồ sơ

📌 Trang 35

UC13 – Quản lý lịch bệnh nhân
Flow
Xem danh sách
Update trạng thái
Lưu DB

📌 Trang 35

UC14 – Quản lý hồ sơ
Flow
Xem hồ sơ
Chỉnh sửa
Lưu

📌 Trang 36

UC15 – Quản lý hệ thống
Flow
Admin truy cập
Chọn module
CRUD dữ liệu
Validate
Lưu DB

📌 Trang 37

2.5 ACTIVITY DIAGRAM (TEXT VERSION)
UC01 – Đăng ký
Start
 → Nhập thông tin
 → Validate
   → [Sai] → Thông báo lỗi → Quay lại
   → [Đúng]
       → Check email
         → [Tồn tại] → Báo lỗi
         → [OK] → Tạo tài khoản
 → Thông báo thành công
End

📌 Trang 38

UC02 – Đăng nhập
Start
 → Nhập thông tin
 → Validate
   → [Sai] → Báo lỗi
   → [Đúng] → Tạo session
 → Redirect home
End

📌 Trang 39

UC06 – Đặt lịch
Start
 → Chọn bác sĩ
 → Chọn ngày giờ
 → Check slot
   → [Full] → Báo lỗi
   → [OK] → Tạo lịch
 → Lưu DB
 → Thông báo
End

📌 Trang 43

UC08 – Hủy lịch
Start
 → Chọn lịch
 → Confirm
   → [Không] → Cancel
   → [Có]
       → Check thời gian
         → [Không hợp lệ] → Báo lỗi
         → [OK] → Update trạng thái
 → Lưu DB
 → Thông báo
End

📌 Trang 44

UC09 – Notification Retry Logic
Event Trigger
 → Send notification
 → Success?
   → Yes → Done
   → No → Retry < 3 lần
       → Fail → Log error

📌 Trang 45