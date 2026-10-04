#!/usr/bin/env python3
"""
Mapper dung chung cho ca customers.csv va orders.csv.
Hadoop Streaming khong ho tro MultipleInputs (moi input file -> mapper rieng)
nhu Java API, nen mapper tu nhan biet dang dang doc file nao qua bien moi
truong "mapreduce_map_input_file" ma Hadoop Streaming luon set cho moi task.

customers.csv: custId,name,city              -> key=custId  value=C|name,city
orders.csv:    orderId,custId,product,amount  -> key=custId  value=O|orderId,product,amount
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
