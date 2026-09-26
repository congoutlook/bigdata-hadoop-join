package com.hadoop.mapreduce.join;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Đọc file customers.csv: custId,name,city
 * Xuất ra: key = custId, value = "C|name,city"
 * Tiền tố "C" giúp Reducer biết dòng này là khách hàng.
 */
public class CustomerMapper extends Mapper<LongWritable, Text, Text, Text> {

	private static final Logger LOG = LoggerFactory.getLogger(CustomerMapper.class);

	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		String line = value.toString().trim();
		if (line.isEmpty()) {
			return;
		}
		String[] fields = line.split(",", 2);
		if (fields.length < 2) {
			return;
		}
		String custId = fields[0].trim();
		String rest = fields[1].trim();

		// Gửi cặp (key, value) cho Hadoop. Hadoop sẽ gom các cặp cùng key lại
		// rồi mới chuyển sang Reducer.
		Text outKey = new Text(custId);
		Text outValue = new Text("C|" + rest);
		context.write(outKey, outValue);

		LOG.info("EMIT (CustomerMapper) key={} value={}", outKey, outValue);
	}
}
