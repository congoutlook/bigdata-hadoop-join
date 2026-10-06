#!/bin/bash
# MySQL chỉ cho LOAD DATA đọc file trong /var/lib/mysql-files, nên chép 2 file CSV vào đó trước.
cp /csv-src/customers.csv /csv-src/orders.csv /var/lib/mysql-files/
