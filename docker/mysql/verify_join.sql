-- Đối chiếu kết quả MapReduce Join bằng SQL INNER JOIN.
-- Chạy: docker exec -i bigdata-mysql mysql -uadmin join_demo < mysql/verify_join.sql

-- 1. Kết quả join, cùng thứ tự cột với output MapReduce: custId,name,city,orderId,product,amount
SELECT c.custId, c.name, c.city, o.orderId, o.product, o.amount
FROM customers c
INNER JOIN orders o ON c.custId = o.custId
ORDER BY c.custId, o.orderId;

-- 2. Số dòng kết quả (MapReduce: Reduce output records=5)
SELECT COUNT(*) AS so_dong_join
FROM customers c
INNER JOIN orders o ON c.custId = o.custId;

-- 3. Các bản ghi bị loại theo ngữ nghĩa inner join
--    Khách không có đơn hàng (kỳ vọng: C003)
SELECT c.custId AS khach_khong_co_don
FROM customers c
LEFT JOIN orders o ON c.custId = o.custId
WHERE o.orderId IS NULL;

--    Đơn hàng không có khách khớp (kỳ vọng: O1006 / C999)
SELECT o.orderId AS don_khong_co_khach, o.custId
FROM orders o
LEFT JOIN customers c ON c.custId = o.custId
WHERE c.custId IS NULL;

-- 4. Kết quả dạng CSV một cột, để diff trực tiếp với part-r-* của MapReduce
SELECT CONCAT_WS(',', c.custId, c.name, c.city, o.orderId, o.product, o.amount) AS csv_line
FROM customers c
INNER JOIN orders o ON c.custId = o.custId
ORDER BY csv_line;
