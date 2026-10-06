package com.hadoop.mapreduce.join;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Chương trình chính: join customers.csv và orders.csv theo custId.
 *
 * Thêm tham số -D trước đường dẫn để đổi cấu hình, ví dụ chạy 4 Mapper và 2 Reducer:
 * -D mapreduce.input.fileinputformat.split.maxsize=64 
 * -D mapreduce.job.reduces=2
 */
public class JoinDriver extends Configured implements Tool {

	public static void main(String[] args) throws Exception {
		// ToolRunner đọc các tham số -D, sau đó mới gọi hàm run() bên dưới.
		System.exit(ToolRunner.run(new Configuration(), new JoinDriver(), args));
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length < 3) {
			System.err.println("Usage: JoinDriver [-D key=value ...] <customers_path> <orders_path> <output_path>");
			return 1;
		}

		String customersPath = args[0];
		String ordersPath = args[1];
		String outputPath = args[2];

		Configuration conf = getConf();
		Job job = Job.getInstance(conf, "Customer-Order Join");

		job.setJarByClass(JoinDriver.class);

		MultipleInputs.addInputPath(job, new Path(customersPath), TextInputFormat.class, CustomerMapper.class);
		MultipleInputs.addInputPath(job, new Path(ordersPath), TextInputFormat.class, OrderMapper.class);

		job.setReducerClass(JoinReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);

		FileSystem fs = FileSystem.get(conf);
		Path out = new Path(outputPath);
		if (fs.exists(out)) {
			fs.delete(out, true);
		}
		FileOutputFormat.setOutputPath(job, out);

		return job.waitForCompletion(true) ? 0 : 1;
	}
}
