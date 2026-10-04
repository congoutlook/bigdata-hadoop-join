-- Chạy tự động lần đầu container MySQL khởi tạo (thư mục dữ liệu còn trống).
-- Nạp đúng 2 file CSV mà MapReduce Join dùng làm input, để đối chiếu kết quả bằng SQL.

CREATE DATABASE IF NOT EXISTS join_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Tài khoản admin, mật khẩu rỗng (chỉ dùng cho môi trường thử nghiệm local)
CREATE USER IF NOT EXISTS 'admin'@'%' IDENTIFIED BY '';
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

USE join_demo;

-- customers.csv: custId,name,city
CREATE TABLE customers (
    custId VARCHAR(10)  NOT NULL PRIMARY KEY,
    name   VARCHAR(100) NOT NULL,
    city   VARCHAR(100) NOT NULL
);

-- orders.csv: orderId,custId,product,amount
-- Không đặt FOREIGN KEY: orders.csv cố ý có O1006 thuộc khách C999 không tồn tại
CREATE TABLE orders (
    orderId VARCHAR(10)  NOT NULL PRIMARY KEY,
    custId  VARCHAR(10)  NOT NULL,
    product VARCHAR(100) NOT NULL,
    amount  INT          NOT NULL,
    INDEX idx_orders_custId (custId)
);

-- 2 file CSV đã được 00_copy_csv.sh chép vào /var/lib/mysql-files (thư mục secure_file_priv mặc định của image)
LOAD DATA INFILE '/var/lib/mysql-files/customers.csv'
INTO TABLE customers
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(custId, name, city);

LOAD DATA INFILE '/var/lib/mysql-files/orders.csv'
INTO TABLE orders
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(orderId, custId, product, amount);
