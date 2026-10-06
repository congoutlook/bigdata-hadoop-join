#!/usr/bin/env python3
"""
Reducer cho Hadoop Streaming.

Hadoop đưa vào stdin các dòng "key<TAB>value" đã sắp xếp theo key.
Khác với Java, các value cùng key không được gom sẵn, nên phải tự
kiểm tra khi nào key thay đổi thì xử lý nhóm vừa đọc xong.
"""
import sys


# In kết quả của một custId. Chỉ in khi có cả khách hàng và đơn hàng (inner join).
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
