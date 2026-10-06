#!/usr/bin/env python3
"""
Mapper dùng chung cho cả customers.csv và orders.csv.

Hadoop Streaming không có MultipleInputs như Java, mapper dùng biến
môi trường mapreduce_map_input_file để biết đang đọc file nào.

customers.csv: custId,name,city; output ra: custId  C|name,city
orders.csv:    orderId,custId,product,amount; output ra: custId  O|orderId,product,amount
"""
import os
import sys

input_file = os.environ.get("mapreduce_map_input_file", "")
is_customer_file = "customers" in input_file

for raw_line in sys.stdin:
    line = raw_line.strip()
    if not line:
        continue

    if is_customer_file:
        parts = line.split(",", 1)
        if len(parts) != 2:
            continue
        cust_id, rest = parts
        print(f"{cust_id}\tC|{rest}")
    else:
        parts = line.split(",", 2)
        if len(parts) != 3:
            continue
        order_id, cust_id, rest = parts
        print(f"{cust_id}\tO|{order_id},{rest}")
