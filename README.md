# Docker – môi trường Hadoop

Container Ubuntu 22.04 (không cài sẵn gói nào). Thư mục `data/` trên host được mount vào `/data` trong container, và chỉ dữ liệu trong `/data` được giữ lại khi container bị tạo lại.

## Chạy container

Chạy từ thư mục gốc của project:

```bash
cd docker
docker compose build        # dựng image
docker compose up -d        # tạo và chạy container ở chế độ nền
docker exec -it bigdata-container bash   # vào shell trong container
```

Dừng hoặc xoá container:

```bash
docker compose stop         # dừng, giữ nguyên container (các gói apt đã cài vẫn còn)
docker compose down         # xoá container (mất các gói apt, cần cài lại)
```

## Khởi động Hadoop (trong container)

Tham khảo tài liệu cài đặt và khởi động Hadoop.
