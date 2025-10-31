# UTE SHOP — Web Final Project 
Đồ án được xây dựng với **Spring Boot + Thymeleaf + MySQL**, hỗ trợ **phân quyền đa vai trò**: *User, Shop, Shipper, Admin*.
---
## Sinh viên thực hiện

| Họ và tên         | MSSV      |
|------------------|------------|
| Thái Quang Huy   | 23110228   |
| Mai Hồng Tín     | 23110342   |
| Nguyễn Nhật Minh | 23110265   |
| Nguyễn Quốc Khánh| 23110239   |
---

## Mục lục
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Tính năng chính](#tính-năng-chính)
- [Cấu trúc dự án](#cấu-trúc-dự-án)
- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Hướng dẫn chạy dự án](#hướng-dẫn-chạy-dự-án)
- [Cấu hình môi trường](#cấu-hình-môi-trường)
- [Phân quyền & vai trò](#phân-quyền--vai-trò)
- [API nổi bật](#api-nổi-bật)
- [Quy ước đặt tên nhánh Git](#quy-ước-đặt-tên-nhánh-git)
---

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|-------------|------------|
| **Backend** | Spring Boot (Web, Security, Data JPA, Validation) |
| **Frontend** | Thymeleaf, Bootstrap 5 |
| **Database** | MySQL 8.x |
| **Build tool** | Maven |
| **Icons/UI** | Bootstrap Icons, Feather, Tiny Slider, Dropzone |

---

## Tính năng chính

### Người dùng (USER)
- Duyệt danh mục & chi tiết sản phẩm  
- **Tìm kiếm & gợi ý tìm kiếm (search suggest)**  
- Giỏ hàng, đặt hàng, theo dõi trạng thái đơn  
- **Wishlist, đánh giá và bình luận sản phẩm**  
- Quản lý hồ sơ cá nhân & địa chỉ giao hàng  

### Chủ cửa hàng (SELLER)
- CRUD sản phẩm, ảnh và mô tả  
- Quản lý đơn hàng của cửa hàng  

### Shipper
- Nhận & cập nhật trạng thái giao hàng  
- Ghi log giao hàng và tìm kiếm đơn  
- Giao diện riêng `/api/shipper/**` có bảo vệ phân quyền  

### Admin
- Quản lý tài khoản, cửa hàng, và dữ liệu hệ thống  

---

## Cấu trúc dự án

```bash
src/
 ├─ main/
 │   ├─ java/vn/host/
 │   │   ├─ controller/        # Controller lớp Web + API
 │   │   ├─ service/           # Xử lý nghiệp vụ
 │   │   ├─ repository/        # Tầng dữ liệu (JPA Repository)
 │   │   ├─ entity/            # Các entity chính: User, Product, Order,...
 │   │   ├─ dto/               # DTO truyền dữ liệu
 │   │   └─ util/              # Enum & Helper
 │   ├─ resources/
 │   │   ├─ templates/         # View Thymeleaf
 │   │   ├─ static/            # JS, CSS, hình ảnh
 │   │   └─ application.properties
 └─ test/
```
## Yêu cầu hệ thống
- JDK: 17 trở lên
- Maven: ≥ 3.8
- MySQL: 8.x
- IDE khuyến nghị: IntelliJ IDEA / VS Code / Eclipse
---
## Hướng dẫn chạy dự án
1. Clone source
```bash
git clone https://github.com/MHHuong/WEB-Final-Project-UTE-SHOP.git
cd WEB-Final-Project-UTE-SHOP
```
2. Tạo database MySQL
```bash
git clone https://github.com/MHHuong/WEB-Final-Project-UTE-SHOP.git
cd WEB-Final-Project-UTE-SHOP
``` 
4. Build & Run
```bash
mvn spring-boot:run
# hoặc
mvn clean package
java -jar target/*.jar
```
5. Truy cập trình duyệt
```bash
http://localhost:8082/UTE_SHOP/
``` 
---
## Cấu hình môi trường
- Có thể thay đổi server.servlet.context-path nếu muốn chạy ở root /
- Ảnh sản phẩm được lưu ở thư mục uploads/
- Đảm bảo quyền ghi cho thư mục nếu deploy trên server
---
## Phân quyền & vai trò
| Vai trò        | Quyền hạn chính      |
|------------------|------------|
| USER   | Xem sản phẩm, tìm kiếm sản phẩm, đặt hàng, thanh toán, đánh giá, wishlist   |
| SELLER | Quản lý sản phẩm và đơn hàng của shop   |
| SHIPPER | Cập nhật trạng thái đơn hàng, giao hàng   |
| ADMIN | Toàn quyền quản lý hệ thống   |
---
##  Một số API nổi bật
| Endpoint         | Tác dụng   |
|------------------|------------|
| `GET /api/shipper/orders`   | Danh sách đơn của shipper (phân trang, lọc)   |
| `PUT /api/shipper/orders/{id}/status`     | Cập nhật trạng thái giao hàng   |
|`POST /api/reviews`| Gửi đánh giá sau khi nhận hàng   |
| `GET /api/search/suggest?q=`| Gợi ý tìm kiếm sản phẩm   |
|`GET /api/products?page=1&size=10`|Phân trang sản phẩm phổ biến|
---
# Quy ước đặt tên nhánh Git

| Loại nhánh | Cấu trúc            | Ví dụ                  |
| ---------- | ------------------- | ---------------------- |
| Tính năng  | `feature/<tên>`     | `feature/add-product`  |
| Sửa lỗi    | `fix/<tên>`         | `fix/pagination-error` |
| Chính      | `main`, `develop`   | Dùng để merge & demo |

