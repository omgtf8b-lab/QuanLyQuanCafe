# ☕ Đồ án: Website Quản Lý Quán Cafe (Lani Coffee)

Đây là kho lưu trữ mã nguồn (Source code) cho dự án Website Quản Lý Quán Cafe - Hệ thống đặt hàng trực tuyến và quản lý cửa hàng Lani Coffee.

## 👥 Danh sách thành viên (Nhóm 11)
1. **Nguyễn Đình Trung Hiếu**
2. **Lê Minh Hoài**
3. **Nông Văn Minh**
4. **Huỳnh Đặng Hải Anh**

## 🛠️ Công nghệ sử dụng
* **Backend:** Java Spring Boot
* **Frontend:** HTML, CSS, JavaScript, Thymeleaf
* **Database:** Microsoft SQL Server
* **Architecture:** MVC (Model-View-Controller)

## 🌟 Chức năng nổi bật
Hệ thống được chia làm hai phân hệ chính với các chức năng cốt lõi:

### 1. Phân hệ Khách hàng (Customer)
* Xem danh sách sản phẩm theo danh mục (Cà phê, Trà sữa, Bánh ngọt...).
* Xem chi tiết sản phẩm và các món Best Seller.
* Thêm, sửa, xóa sản phẩm trong Giỏ hàng (Cart) với logic tính toán tổng tiền trực tiếp.
* Áp dụng mã giảm giá (Coupon).
* Đặt hàng và xem lịch sử mua hàng.

### 2. Phân hệ Quản trị viên (Admin)
* Quản lý danh mục sản phẩm (Categories).
* Quản lý sản phẩm (Products): Thêm, sửa, xóa thông tin và tồn kho.
* Theo dõi và xử lý Đơn hàng (Orders).
* Quản lý kho Mã giảm giá (Coupons).

## 🚀 Hướng dẫn cài đặt & Chạy dự án
Để chạy dự án trên máy cá nhân, vui lòng thực hiện theo các bước sau:

1. **Clone repository này về máy:**
   ```bash
   git clone [https://github.com/LeMinhHoai1011/QuanLyQuanCafe.git](https://github.com/LeMinhHoai1011/QuanLyQuanCafe.git)
Thiết lập Database:

Mở SQL Server Management Studio (SSMS).

Khởi tạo database mới tên là lani_cafe.

Chạy file script lani_cafe_script.sql (đính kèm trong thư mục hoặc bài nộp) để tạo cấu trúc bảng và thêm dữ liệu mẫu (15 món đồ uống).

Cấu hình kết nối:

Mở file src/main/resources/application.properties.

Cập nhật username và password kết nối SQL Server của bạn.

Chạy ứng dụng:

Mở dự án bằng IntelliJ IDEA.

Chạy file LaniCoffeeApplication.java.

Truy cập giao diện khách hàng tại: http://localhost:8080/
