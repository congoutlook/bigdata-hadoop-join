package com.hadoop.mapreduce.join;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Join customers.csv và orders.csv theo custId (reduce-side join).
 *
 * Cách chạy:
 *   hadoop jar target/mapreduce-join-V1.jar com.hadoop.mapreduce.join.JoinDriver \
 *       <customers_path> <orders_path> <output_path>
 */
public class JoinDriver {

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.err.println("Usage: JoinDriver <customers_path> <orders_path> <output_path>");
			System.exit(1);
		}

		String customersPath = args[0];
		String ordersPath = args[1];
		String outputPath = args[2];

		Configuration conf = new Configuration();
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

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
