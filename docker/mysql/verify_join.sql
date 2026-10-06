-- Kiểm tra lại kết quả MapReduce bằng câu lệnh JOIN của SQL.

-- Kết quả join, các cột xếp giống output của MapReduce
SELECT c.custId, c.name, c.city, o.orderId, o.product, o.amount
FROM customers c
INNER JOIN orders o ON c.custId = o.custId
ORDER BY c.custId, o.orderId;
