# Flyora Backend – Hướng dẫn chạy và kiểm thử API

## 1. Lưu ý về việc kết nối với Frontend

Frontend của hệ thống hiện đang được cấu hình để gọi API từ backend đã được **deploy trên Render**.

API production:

https://flyora-backend-v2.onrender.com

Do đó:

* Khi chạy source code backend **ở máy local**, sẽ **không ảnh hưởng đến frontend**
* Frontend vẫn tiếp tục gọi API từ **server Render**
* Backend chạy local chỉ dùng để **kiểm thử API**

Vì vậy giảng viên có thể chạy backend trên máy cá nhân mà **không làm thay đổi hệ thống frontend đang hoạt động**.

---

# 2. Hướng dẫn chạy Backend trên máy local

## Bước 1: Clone source code

```bash
git clone https://github.com/Huntrot/Flyora-Backend-v2.git
cd flyora-backend-v2
```

---

## Bước 2: Tạo file biến môi trường

Tạo file `.env` dựa trên file `.env.example`.

Ví dụ:

```env
DB_URL=jdbc:mysql://localhost:3306/flyora
DB_USERNAME=root
DB_PASSWORD=your_database_password

JWT_SECRET=your_jwt_secret_key

GHN_TOKEN=your_ghn_token
GHN_SHOP_ID=your_shop_id
GHN_DISTRICT_ID=your_district_id
GHN_WARD_CODE=your_ward_code

PAYOS_CLIENT_ID=your_payos_client_id
PAYOS_API_KEY=your_payos_api_key
PAYOS_CHECKSUM_KEY=your_payos_checksum_key

GEMINI_API_KEY=your_gemini_api_key

MAIL_PORT=587
MAIL_USERNAME=your_brevo_smtp_username
MAIL_PASSWORD=your_brevo_smtp_password
```

Sau đó cập nhật các giá trị phù hợp với môi trường chạy local.

---

## Bước 3: Chạy server backend

Sử dụng Maven:

```bash
mvn spring-boot:run
```

hoặc

```bash
./mvnw spring-boot:run
```

Sau khi chạy thành công, server backend sẽ hoạt động tại:

```
http://localhost:8080
```

---

# 3. Kiểm thử API bằng Swagger

Mở trình duyệt và truy cập:

```
http://localhost:8080/swagger-ui/index.html
```

Swagger cung cấp giao diện để:

* Xem danh sách tất cả API
* Gửi request trực tiếp
* Xem cấu trúc request và response

---

# 4. Kiểm thử API bằng Postman

Ngoài Swagger, có thể kiểm thử API bằng **Postman**.

Base URL:

```
http://localhost:8080/api
```

Ví dụ một số endpoint:

```
POST /orders
GET /products
GET /orders
```

Các bước thực hiện:

1. Mở Postman
2. Tạo request mới
3. Chọn phương thức (GET / POST / PUT / DELETE)
4. Nhập URL API
5. Gửi request và kiểm tra response

---

# 5. Thông tin triển khai hệ thống

Backend của hệ thống được triển khai trên **Render Cloud Platform**.

API production:

```
https://flyora-backend-v2.onrender.com
https://flyora-backend-v2.onrender.com/swagger-ui/index.html
```

Các biến môi trường của production được cấu hình trực tiếp trên **Render Dashboard**, không nằm trong source code.
