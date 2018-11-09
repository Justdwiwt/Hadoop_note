# MapReduce

## 一、MapReduce概述

### 思考

求和：1+3+5+8+2+7+3+4+9+\...

### MapReduce分布式计算框架

MapReduce是一种分布式计算模型，由Google提出，主要用于搜索领域，解决海量数据的计算问题.

MR由两个阶段组成：Map和Reduce，用户只需要实现map()和reduce()两个函数，即可实现分布式计算，非常简单。

这两个函数的形参是key、value对，表示函数的输入信息。

### MapReduce框架的组成

```shel
JobTracker / ResourceManager
TaskTracker / NodeManager
Map Reduce
```

### MapReduce原理（参看图 MR原理）

## 二、Map、Reduce的执行步骤

### map任务处理

1. 读取输入文件内容，解析成key、value对。对输入文件的每一行，解析成key、value对。每一个键值对调用一次map函数。

2. 写自己的逻辑，对输入的key、value处理，转换成新的key、value输出。

3. 对输出的key、value进行分区。

4. 对相同分区的数据，按照key进行排序(默认按照字典顺序进行排序)、分组。相同key的value放到一个集合中。

5. (可选)分组后的数据进行归约。

### reduce任务处理

1. 对多个map任务的输出，按照不同的分区，通过网络copy到不同的reduce节点。这个过程并不是map将数据发送给reduce，而是reduce主动去获取数据。

2. 对多个map任务的输出进行合并、排序。写reduce函数自己的逻辑，对输入的key、value处理，转换成新的key、value输出。

3. 把reduce的输出保存到文件中。

### 案例

实现WordCount(文件：words.txt)

```java
public class WCMapper extends Mapper<LongWritable, Text, Text, LongWritable>{
	/**
	 * key -- 当前行的开始位置在整个文件中的偏移量
	 * value -- 当前行的内容
	 * context -- 环境对象
	 */
	protected void map(LongWritable key, Text value, org.apache.hadoop.mapreduce.Mapper<LongWritable,Text,Text,LongWritable>.Context context) throws java.io.IOException ,InterruptedException {
		//获取当前行
		String line = value.toString();
		//按照空格进行切割，得到当前行单词数组
		String [] wds = line.split(" ");
		//遍历这个数组，输出词频率
		for(String w : wds){
			context.write(new Text(w), new LongWritable(1));
		}
	};
}

public class WCReduce extends Reducer<Text, LongWritable, Text, LongWritable> {
	/**
	 * key 键
	 * it 集合的迭代器
	 * context 环境对象
	 */
	protected void reduce(Text key, java.lang.Iterable<LongWritable> it, org.apache.hadoop.mapreduce.Reducer<Text,LongWritable,Text,LongWritable>.Context context) throws java.io.IOException ,InterruptedException {
		//定义变量用来累加
		long count = 0;
		//循环 遍历集合，进行累加的操作，得到当前单词出现的总次数
		for(LongWritable num : it){
			count += num.get();
		}
		//输出数据，key是单词，value是在map阶段这个单词出现的总的次数
		context.write(key, new LongWritable(count));
	};
}

public class WordCount {
	public static void main(String[] args) throws Exception {
		//获取代表当前mapreduce作业的JOB对象
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);

        //指定当前程序的入口类
		job.setJarByClass(WordCount.class);
		
		//为这个job对象设置map相关的参数
		job.setMapperClass(WCMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(LongWritable.class);
		FileInputFormat.setInputPaths(job, new Path("/root/work/words.txt"));
			
		//为这个job对象设置reduce相关的参数
		job.setReducerClass(WCReduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);
		FileOutputFormat.setOutputPath(job, new Path("/root/wordcount"));
		
		//执行job
		job.waitForCompletion(true);
	}
}
```

在eclipse中使用hadoop插件开发mapreduce可能遇到的问题及解决方案：

问题1：空指针异常：

本地hadoop缺少支持包，将winutils和hadoop.dll（及其他）放置到eclips关联的hadoop/bin下，并将hadoop/bin配置到PATH环境变量中

如果还不行，就再放一份到c:/windows/system32下

问题2：不打印日志：

在mr程序下放置一个log4j.properties文件

问题3：`java.io.IOException: Could not locate executable null\bin\winutils.exe in the Hadoop binaries.`

解决方法1：

配置HADOOP_HOME环境变量,可能需要重启电脑

解决方法2：

如果不想配置环境变量，可以在代码中写上

`System.setProperty("hadoop.home.dir", "本机hadoop地址");`

## 三、MR内部执行流程

1. 客户端提交一个mr的jar包给JobClient(提交方式：hadoop jar ...)
2. JobClient通过RPC和ResourceManager进行通信，返回一个存放jar包的地址（HDFS）和jobId
3. client将jar包写入到HDFS当中(path = hdfs上的地址 + jobId)
4. 开始提交任务(任务的描述信息，不是jar, 包括jobid，jar存放的位置，配置信息等等)
5. JobTracker进行初始化任务
6. 读取HDFS上的要处理的文件，开始计算输入分片，每一个分片对应一个MapperTask
7. TaskTracker通过心跳机制领取任务（任务的描述信息）
8. 下载所需的jar，配置文件等
9. TaskTracker启动一个java child子进程，用来执行具体的任务（MapperTask或ReducerTask）
10. 将结果写入到HDFS当中

## 四、MR的序列化机制 - Writable - WritableComparable

由于集群工作过程中需要用到RPC操作，所以想要MR处理的对象的类必须可以进行序列化/反序列化操作。

Hadoop并没有使用Java原生的序列化，它的底层其实是通过AVRO实现序列化/反序列化，并且在其基础上提供了便捷API：

Writable -- WritableComparable

	write(DataOutput out)

	readFields(DataInput in)

### 案例

统计流量(文件：flow.txt) -- 自定义对象作为keyvalue

```java
// 写一个Bean实现Writable接口，实现其中的write和readFields方法，注意这两个方法中属性处理的顺序和类型
@Override
public void write(DataOutput out) throws IOException {
	out.writeUTF(tel);
	out.writeUTF(addr);
	out.writeUTF(name);
	out.writeLong(flow);
}

@Override
public void readFields(DataInput in) throws IOException {
	this.tel = in.readUTF();
	this.addr = in.readUTF();
	this.name = in.readUTF();
	this.flow = in.readLong();
}
```

此后这个类的对象就可以用于MR了.

## 五、Partitioner - 分区

分区操作是shuffle操作中的一个重要过程，作用就是将map的结果按照规则分发到不同reduce中进行处理，从而按照分区得到多个输出结果。

Partitioner是partitioner的基类，如果需要定制partitioner也需要继承该类

HashPartitioner是mapreduce的默认partitioner。
计算方法是：`which reducer=(key.hashCode() & Integer.MAX_VALUE) % numReduceTasks`

注：默认情况下，reduceTask数量为1

很多时候MR自带的分区规则并不能满足我们需求，为了实现特定的效果，可以需要自己来定义分区规则。

### 案例

改造如上统计流量案例，根据不同地区分区存放数据

```java
// 开发Partitioner代码，写一个类实现Partitioner接口，在其中描述分区规则
public static class DCPartitioner extends  Partitioner<Text, DataInfo>{
	private static Map<String,Integer> addrMap = new HashMap<String,Integer>();
	
	static{
		addrMap.put("bj", 1);
		addrMap.put("sh", 2);
		addrMap.put("sz", 3);
	}
    
	@Override
	public int getPartition(Text key, DataInfo value, int numPartitions) {
		String addr = value.getAddr().toString();
		Integer num = addrMap.get(addr);
		if(num == null){
			num = 0;
		}
		return num;
	}
}

// 在任务调度代码中，增加Partitioner配置
//设置Partitioner类
job.setPartitionerClass(DCPartitioner.class);
//指定Reducer的数量，此处通过main方法参数获取，方便测试
job.setNumReduceTasks(Integer.parseInt(args[2]));

// Partitioner将会将数据发往不同reducer，这就要求reducer的数量应该大于等于Partitioner的数量，如果少于则在执行的过程中会报错。
```

## 六、sort - 排序

Map执行过后，在数据进入reduce操作之前，数据将会按照K2进行排序，利用这个特性可以实现大数据场景下排序的需求

### 案例

计算利润，进行排序(文件：profit.txt)

此案例，需要两个MR操作，合并数据、进行排序

排序MR：

	创建Bean对象实现WritableComparable接口实现其中的write readFields compareTo方法

	在Map操作时，将Bean对象作为Key输出，从而在Reduce接受到数据时已经经过排序context.write(bean, NullWritable.get());

	而Reduce操作时，只需原样输出数据即可。 

## 七、Combiner - 	合并

每一个MapperTask可能会产生大量的输出，combiner的作用就是在MapperTask端对输出先做一次合并，以减少传输到reducerTask的数据量。

combiner是实现在Mapper端进行key的归并，combiner具有类似本地的reduce功能。

如果不用combiner，那么，所有的结果都是reduce完成，效率会相对低下。使用combiner，先完成在Mapper的本地聚合，从而提升速度。

job.setCombinerClass(WCReducer.class);

案例：改造WordCount案例，增加Combiner，从而提高效率

## 八、Shuffle(参看图：Shuffle详解、参看文档:HADOOP权威指南，第6.4章)

### Mapper

每个MapperTask有一个环形内存缓冲区，用于存储map任务的输出。默认大小100MB（io.sort.mb属性），一旦达到阀值0.8（io.sort.spill.percent）,一个后台线程把内容写到(spill)磁盘的指定目录（mapred.local.dir）下的新建的一个溢出写文件。

写磁盘前，要partition,sort,Combiner。如果有后续的数据，将会继续写入环形缓冲区中，最终写入下一个溢出文件中。

等最后记录写完，合并全部溢出写文件为一个分区且排序的文件。

如果在最终合并时，被合并的文件大于等于3个，则合并完会再执行一次Combiner，否则不会。

### Reducer

Reducer通过Http方式得到输出文件的分区。

NodeManager为分区文件运行Reduce任务。复制阶段把Map输出复制到Reducer的内存或磁盘。一但Map任务完成，Reduce就开始复制输出。

排序阶段合并map输出。然后走Reduce阶段

## 九、Mapper数量

Mapper的数量在默认情况下不可直接控制干预，Mapper的数量由输入的大小和个数决定。

在默认情况下，最终input占据了多少block，就应该启动多少个Mapper。

可以通过配置mapred.min.split.size来控制split的size的最小值。

## 十、案例

1. 求最大/小值
2. 统计考试成绩(参看：math.txt chinese.txt english.txt)
3. zebra实现