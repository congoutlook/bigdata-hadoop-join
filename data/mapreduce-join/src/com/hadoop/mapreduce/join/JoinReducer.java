package com.hadoop.mapreduce.join;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Với mỗi custId, Reducer nhận tất cả value từ hai Mapper có cùng custId đó.
 * Reducer tách ra thông tin khách hàng và danh sách đơn hàng, rồi ghép lại.
 * Đây là inner join: chỉ in ra khi có cả khách hàng và ít nhất một đơn hàng.
 */
public class JoinReducer extends Reducer<Text, Text, Text, NullWritable> {

	private static final Logger LOG = LoggerFactory.getLogger(JoinReducer.class);

	@Override
	protected void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {

		String customerInfo = null;
		List<String> orders = new ArrayList<>();

		for (Text value : values) {
			String v = value.toString();
			if (v.startsWith("C|")) {
				customerInfo = v.substring(2);
			} else if (v.startsWith("O|")) {
				orders.add(v.substring(2));
			}
		}

		// Bỏ qua khách hàng không có đơn, và đơn hàng không tìm thấy khách hàng.
		if (customerInfo == null || orders.isEmpty()) {
			LOG.info("SKIP key={} (inner join: customer={}, orderCount={})", key, customerInfo != null, orders.size());
			return;
		}

		for (String order : orders) {
			String line = key.toString() + "," + customerInfo + "," + order;

			// Ghi một dòng kết quả ra file output (part-r-00000 trên HDFS).
			// Mỗi đơn hàng tạo ra một dòng.
			context.write(new Text(line), NullWritable.get());

			LOG.info("EMIT (JoinReducer) key={} line=\"{}\"", key, line);
		}
	}
}
