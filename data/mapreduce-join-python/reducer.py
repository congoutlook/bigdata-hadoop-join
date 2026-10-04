#!/usr/bin/env python3
"""
Reducer cho Hadoop Streaming.

Khong nhu Java API (duoc goi 1 lan/group voi Iterable<value> co san),
Streaming chi dua vao stdin 1 luong dong "key\\tvalue" da duoc SORT theo key
(khong group san) -> reducer phai tu nhan biet khi nao key doi de "dong" 1
group va xu ly, giong cach lam kinh dien cua bai Word Count bang Streaming.

Voi moi group (custId): tach ra 1 customer record va N order record,
chi sinh output khi co ca 2 phia (inner join).
"""
import sys


def flush(cust_id, customer_info, orders):
    if customer_info is None or not orders:
        return
    for order in orders:
        print(f"{cust_id},{customer_info},{order}")


current_key = None
customer_info = None
orders = []

for raw_line in sys.stdin:
    line = raw_line.rstrip("\n")
    if not line:
        continue
    key, value = line.split("\t", 1)

    if key != current_key:
        flush(current_key, customer_info, orders)
        current_key = key
        customer_info = None
        orders = []

    if value.startswith("C|"):
        customer_info = value[2:]
    elif value.startswith("O|"):
        orders.append(value[2:])

flush(current_key, customer_info, orders)
