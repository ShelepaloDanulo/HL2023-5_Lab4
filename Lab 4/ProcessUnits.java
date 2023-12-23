package hadoop;

import java.util.*;
import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class ProcessUnits {
   // Mapper class
   public static class E_EMapper extends MapReduceBase implements
   Mapper<LongWritable, /* Input key Type */
   Text,                /* Input value Type */
   Text,                /* Output key Type */
   FloatWritable>       /* Output value Type */ {

      // Map function
      public void map(LongWritable key, Text value, OutputCollector<Text, FloatWritable> output, Reporter reporter) throws IOException {
         String line = value.toString();
         StringTokenizer s = new StringTokenizer(line, "\t");
         String year = s.nextToken();

         float sum = 0;
         int count = 0;
         while (s.hasMoreTokens()) {
            sum += Float.parseFloat(s.nextToken());
            count++;
         }

         float avg = (count == 0) ? 0 : sum / count;
         output.collect(new Text(year), new FloatWritable(avg));
      }
   }

   // Reducer class
   public static class E_EReduce extends MapReduceBase implements
   Reducer<Text, FloatWritable, Text, FloatWritable> {

      // Reduce function
      public void reduce(Text key, Iterator<FloatWritable> values, OutputCollector<Text, FloatWritable> output, Reporter reporter) throws IOException {
         while (values.hasNext()) {
            output.collect(key, values.next());
         }
      }
   }

   // Main function
   public static void main(String args[]) throws Exception {
      JobConf conf = new JobConf(ProcessUnits.class);

      conf.setJobName("average_electricity_units");
      conf.setOutputKeyClass(Text.class);
      conf.setOutputValueClass(FloatWritable.class);
      conf.setMapperClass(E_EMapper.class);
      conf.setCombinerClass(E_EReduce.class);
      conf.setReducerClass(E_EReduce.class);
      conf.setInputFormat(TextInputFormat.class);
      conf.setOutputFormat(TextOutputFormat.class);

      FileInputFormat.setInputPaths(conf, new Path(args[0]));
      FileOutputFormat.setOutputPath(conf, new Path(args[1]));

      JobClient.runJob(conf);
   }
}
