package com.hadoop.mapreduce.join;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Đọc file orders.csv: orderId,custId,product,amount
 * Xuất ra: key = custId, value = "O|orderId,product,amount"
 * Tiền tố "O" giúp Reducer biết dòng này là đơn hàng.
 */
public class OrderMapper extends Mapper<LongWritable, Text, Text, Text> {

	private static final Logger LOG = LoggerFactory.getLogger(OrderMapper.class);

	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		String line = value.toString().trim();
		if (line.isEmpty()) {
			return;
		}
		String[] fields = line.split(",", 3);
		if (fields.length < 3) {
			return;
		}
		String orderId = fields[0].trim();
		String custId = fields[1].trim();
		String rest = fields[2].trim();

		// Gửi cặp (key, value) cho Hadoop. Hadoop sẽ gom các cặp cùng key lại
		// rồi mới chuyển sang Reducer.
		Text outKey = new Text(custId);
		Text outValue = new Text("O|" + orderId + "," + rest);
		context.write(outKey, outValue);

		LOG.info("EMIT (OrderMapper) key={} value={}", outKey, outValue);
	}
}
