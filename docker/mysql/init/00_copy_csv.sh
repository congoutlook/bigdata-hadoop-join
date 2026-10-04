#!/bin/bash
# CSV được mount read-only vào /csv-src. LOAD DATA INFILE chỉ đọc được file trong secure_file_priv
# (/var/lib/mysql-files), nên chép sang đó trước khi 01_schema_and_data.sql chạy.
cp /csv-src/customers.csv /csv-src/orders.csv /var/lib/mysql-files/
