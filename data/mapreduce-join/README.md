# Join hai file bằng Hadoop MapReduce

Bài này nối dữ liệu khách hàng và đơn hàng theo cột `custId`, giống lệnh `INNER JOIN` trong SQL.

Dữ liệu đầu vào:

- `customers.csv`: `custId,name,city`
- `orders.csv`: `orderId,custId,product,amount`

Kết quả mỗi dòng có dạng `custId,name,city,orderId,product,amount`.

## Cách làm

MapReduce không có sẵn phép join nên nhóm dùng cách reduce-side join:

- Mỗi file có một Mapper riêng. Cả hai Mapper đều lấy `custId` làm key, nhờ vậy Hadoop sẽ gom các dòng cùng `custId` về một Reducer.
- Value được gắn thêm tiền tố `C|` (khách hàng) hoặc `O|` (đơn hàng) để Reducer biết dòng đó từ file nào.
- Reducer nhận một khách hàng và danh sách đơn hàng của người đó, rồi ghép lại thành từng dòng kết quả. Nếu thiếu một trong hai bên thì bỏ qua.
- Trong Driver, `MultipleInputs` dùng để gán Mapper tương ứng cho từng file.

Code nằm trong `src/com/hadoop/mapreduce/join/`:

| File | Việc làm |
|---|---|
| `CustomerMapper.java` | đọc customers.csv, xuất `(custId, "C\|name,city")` |
| `OrderMapper.java` | đọc orders.csv, xuất `(custId, "O\|orderId,product,amount")` |
| `JoinReducer.java` | ghép khách hàng với đơn hàng |
| `JoinDriver.java` | cấu hình và chạy job |

## Build và chạy

```bash
cd /data/mapreduce-join
mvn clean package

hdfs dfs -mkdir -p /join-demo/input
hdfs dfs -put -f customers.csv orders.csv /join-demo/input/

hadoop jar target/mapreduce-join-V1.jar com.hadoop.mapreduce.join.JoinDriver \
  /join-demo/input/customers.csv /join-demo/input/orders.csv /join-demo/output

hdfs dfs -cat /join-demo/output/part-r-00000
```

Nếu thư mục output đã có thì chương trình sẽ tự xóa trước khi chạy.

## Kết quả

Với 5 khách hàng và 6 đơn hàng, kết quả ra 5 dòng:

```
C001,Nguyen Van A,Ha Noi,O1002,Mouse,20
C001,Nguyen Van A,Ha Noi,O1001,Laptop,1200
C002,Tran Thi B,Ho Chi Minh,O1003,Keyboard,45
C004,Pham Thi D,Hai Phong,O1004,Monitor,300
C005,Hoang Van E,Can Tho,O1005,Headphone,80
```

Có 2 bản ghi không xuất hiện:

- `C003` (Le Van C) không có đơn hàng nào.
- Đơn `O1006` thuộc `C999`, mà không có khách hàng nào mang mã này.

## Có thể làm thêm

- Left join: sửa `JoinReducer`, khi khách hàng không có đơn thì vẫn in ra một dòng, để trống phần đơn hàng.
- Map-side join: nếu file khách hàng nhỏ, có thể nạp hết vào bộ nhớ trong `setup()` của Mapper rồi join luôn ở bước map. Cách này nhanh hơn vì không phải shuffle dữ liệu qua mạng.
