# Reduce-side Join bằng Python (Hadoop Streaming)

Đây là **phương án thay thế** cho `mapreduce-join/` (Java) — cùng giải bài toán join `customers.csv` ⋈ `orders.csv` theo `custId`, nhưng viết Mapper/Reducer bằng **Python** thay vì Java, chạy qua cơ chế **Hadoop Streaming**.

## Hadoop Streaming là gì?

Hadoop Streaming (`hadoop-streaming-*.jar`) cho phép viết Mapper/Reducer bằng **bất kỳ ngôn ngữ nào đọc được stdin và ghi được stdout** (Python, bash, Node.js, ...), không cần biết Java. Cơ chế:

- Mapper: mỗi dòng input được đưa vào **stdin** của tiến trình Python; mọi dòng in ra **stdout** theo định dạng `key\tvalue` được Hadoop coi là 1 cặp (key, value) trung gian.
- Shuffle & Sort: Hadoop tự sort các dòng `key\tvalue` theo key, y hệt cơ chế Java API.
- Reducer: các dòng đã sort theo key được đưa vào **stdin** của tiến trình Python reducer — nhưng khác Java (`Iterable<value>` gom sẵn theo group), ở đây reducer nhận về **một luồng dòng phẳng**, phải tự phát hiện khi nào key đổi để biết một group đã "đóng" (giống kiểu code Word Count kinh điển bằng Streaming).

## Khác biệt so với bản Java (`MultipleInputs`)

Java API có `MultipleInputs.addInputPath(job, path, format, MapperClass)` để gắn **Mapper khác nhau cho từng file input**. Hadoop Streaming **không có tính năng tương đương** — chỉ có 1 mapper dùng chung cho mọi input. Do đó `mapper.py` phải **tự nhận biết** đang đọc file nào bằng biến môi trường Hadoop luôn set cho mỗi map task:

```python
input_file = os.environ.get("mapreduce_map_input_file", "")
is_customer_file = "customers" in input_file
```

## Cấu trúc

```
mapreduce-join-python/
  mapper.py     -- doc dong, tu phat hien file nguon, emit key\tvalue
  reducer.py    -- nhan dong da sort theo key, tu group thu cong, join
  customers.csv
  orders.csv
```

### mapper.py

```
customers.csv: custId,name,city              -> key=custId  value=C|name,city
orders.csv:    orderId,custId,product,amount  -> key=custId  value=O|orderId,product,amount
```

### reducer.py

Vì Streaming không gom sẵn theo group, reducer phải tự theo dõi khi nào `key` đổi so với dòng trước để "đóng" một group và xử lý (giống hệt logic `JoinReducer.java` nhưng viết thủ công vòng lặp group-by):

```python
if key != current_key:
    flush(current_key, customer_info, orders)   # xu ly group truoc do
    current_key = key
    customer_info = None
    orders = []
```

## Build & chạy trên Hadoop (trong container)

Không cần `mvn` — Python không cần biên dịch. Chỉ cần chmod thực thi và trỏ tới jar `hadoop-streaming`:

```bash
chmod +x mapper.py reducer.py

# Input đã có sẵn trên HDFS từ bản Java (/join-demo/input/), dùng lại luôn
hdfs dfs -mkdir -p /join-demo/input
hdfs dfs -put -f customers.csv orders.csv /join-demo/input/

hadoop jar $HADOOP_HOME/share/hadoop/tools/lib/hadoop-streaming-3.4.3.jar \
  -files mapper.py,reducer.py \
  -input /join-demo/input/customers.csv \
  -input /join-demo/input/orders.csv \
  -mapper mapper.py \
  -reducer reducer.py \
  -output /join-demo/output-python

hdfs dfs -cat /join-demo/output-python/part-00000
```

`-files` đóng gói 2 script Python cùng job (distributed cache), `-input` được khai 2 lần để đọc cả 2 file (thay cho `MultipleInputs` của Java).

## Kết quả demo (đã chạy thật, đối chiếu với bản Java)

```
C001,Nguyen Van A,Ha Noi,O1001,Laptop,1200
C001,Nguyen Van A,Ha Noi,O1002,Mouse,20
C002,Tran Thi B,Ho Chi Minh,O1003,Keyboard,45
C004,Pham Thi D,Hai Phong,O1004,Monitor,300
C005,Hoang Van E,Can Tho,O1005,Headphone,80
```

Job counters: `Map input records=11`, `Reduce input groups=6`, `Reduce output records=5` — **giống hệt bản Java**.

Đã `diff` trực tiếp với `part-r-00000` của bản Java (sau khi bỏ khoảng trắng cuối dòng) → khớp tuyệt đối, không sai khác 1 ký tự nào về dữ liệu.

## Lưu ý: dấu tab thừa cuối mỗi dòng output

Output thô có 1 ký tự **TAB thừa ở cuối mỗi dòng** (`part-00000` chứa `...,80\t` thay vì `...,80`). Nguyên nhân: `TextOutputFormat` mặc định của Hadoop luôn ghi `key<TAB>value`; vì `reducer.py` in ra 1 chuỗi CSV đã ghép sẵn (không có tab), Streaming coi **toàn bộ dòng là key, value rỗng** → khi ghi ra vẫn nối thêm `<TAB>` + chuỗi rỗng. Đây là artifact cosmetic thuần túy của cơ chế Streaming, **không ảnh hưởng dữ liệu**; có thể loại bỏ khi cần so sánh chính xác byte-by-byte bằng:

```bash
hdfs dfs -cat /join-demo/output-python/part-00000 | sed 's/[[:space:]]*$//'
```

## Ưu / nhược điểm so với bản Java

| Tiêu chí | Java (MapReduce API) | Python (Hadoop Streaming) |
|---|---|---|
| Tốc độ phát triển | Chậm hơn (biên dịch, kiểu tĩnh) | Nhanh hơn, không cần build |
| Hiệu năng runtime | Nhanh hơn (chạy trong JVM, không qua tiến trình con) | Chậm hơn (mỗi record phải serialize qua stdin/stdout giữa JVM và tiến trình Python) |
| Nhiều input khác Mapper | Có sẵn (`MultipleInputs`) | Phải tự phát hiện nguồn trong 1 mapper dùng chung |
| Group theo key ở Reducer | Tự động (`Iterable<value>`) | Phải tự viết vòng lặp phát hiện đổi key |
| Phù hợp | Ứng dụng production, cần hiệu năng | Prototype nhanh, script xử lý dữ liệu đơn giản, không rành Java |
