# HDFS

## HDFS的特点

### HDFS概述

> Hadoop Distributed File System - Hadoop分布式文件存储系统

HDFS为了保证数据存储的**可靠性**(复本)和**读取性能**(切块)，对数据进行**切块后复制**(保证复本的数量)并存储在集群的**多个节点**中。

HDFS中存在**一个名字节点**NameNode和**多个数据节点**DataNode。

**NameNode** 

1. 存储元数据信息

2. 元数据保存在内存(保证读写效率)以及磁盘(崩溃恢复)中

3. 保存文件、block、datanode之间的映射关系

**DataNode**

1. 存储block内容
2. 存储在磁盘中
3. 维护了block id到文件的映射关系	

### HDFS优点

1. 支持超大文件

   支持超大文件。超大文件在这里指的是几百M，几百GB，甚至几TB大小的文件。一般来说hadoop的文件系统会存储TB级别或者PB级别的数据。所以在企业的应用中，数据节点有可能有上千个。

2. 检测和快速应对硬件故障

	在集群的环境中，硬件故障是常见的问题。因为有上千台服务器连接在一起，这样会导致高故障率。因此**故障检测**和自动恢复(**心跳机制**)是hdfs文件系统的一个设计目标。

3. 流式数据访问

	Hdfs的数据处理规模比较大，应用一次需要访问大量的数据，同时这些应用一般都是批量处理，而不是用户交互式处理。应用程序能以流的形式访问数据集。主要的是数据的吞吐量，而不是访问速度。

4. 简化的一致性模型

	大部分hdfs操作文件时，需要一次写入，多次读取。在hdfs中，一个文件一旦经过创建、写入、关闭后，一般就不需要修改了。这样简单的一致性模型，有利于提高吞吐量。

5. 高容错性

	**数据自动保存多个副本，副本丢失后自动恢复**

6. 可构建在廉价机器上

	构建在廉价机器上可以轻松的通过扩展机器数量来近乎线性的提高集群存储能力

### HDFS缺点

1. 低延迟数据访问

   低延迟数据。如和用户进行交互的应用，需要数据在毫秒或秒的范围内得到响应。由于hadoop针对海量数据的吞吐量做了优化，牺牲了获取数据的延迟，所以对于低延迟来说，不适合用hadoop来做。

2. 大量的小文件

   Hdfs支持超大的文件，是通过数据分布在数据节点，数据的元数据保存在名字节点上。名字节点的内存大小，决定了hdfs文件系统可保存的文件数量。虽然现在的系统内存都比较大，但大量的小文件还是会影响名字节点的性能。

3. 多用户写入文件、修改文件

   **Hdfs的文件只能有一次写入，不支持修改和追加写入（2.0版本支持追加），也不支持修改。** 只有这样数据的吞吐量才能大。

4. 不支持超强的事务

   没有像关系型数据库那样，对事务有强有力的支持。

## HDFS技术细节

### Block

> 数据块（Block）是HDFS中存储文件的最基本的存储单位。

在HDFS中，有一个特别重要的概念：数据块(Block)。前面介绍到，在HDFS中存储的文件都是超大数据的文件，我们可以把这个超大规模的文件以一个标准切分成几块，分别存储到不同的磁盘上。这个标准就称为Block。Block 默认的大小为64(128)M。这样做有以下几点好处：

1. 文件块可以保存在不同的磁盘上。在HDFS系统中，一个文件可以分成不同的Block存储在不同的磁盘上。
2. 简化存储系统。这样不需要管理文件，而是管理文件块就可以了。
3. 有利于数据的复制。在HDFS系统中，一个数据节点一般会**复制3份**

对于文件内容而言，一个文件的长度大小是size，那么从文件的０偏移开始，按照固定的大小，顺序对文件进行划分并编号，划分好的每一个块称一个Block。HDFS默认Block大小是128MB，以一个256MB文件为例，共有256/128=2个Block.

不同于普通文件系统的是，HDFS中，**如果一个文件小于一个数据块的大小，并不占用整个数据块存储空间**

### NameNode

NameNode维护着HDFS中的元数据信息，包括文件和Block之间关系的信息、Block数量信息、Block和DataNode之间的关系信息，数据格式参照如下：

​FileName replicas block-Ids id2host

​例如： /test/a.log,3,{b1,b2},[{b1:[h0,h1,h3]},{b2:[h0,h2,h4]}]

NameNode中的元数据信息存储在内存以及文件中，内存中为实时信息，文件中为数据镜像作为持久化存储使用。

* 文件包括：

	* fsimage 元数据镜像文件。存储某NameNode元数据信息，并不是实时同步内存中的数据。

	* edits 操作日志文件,记录了NameNode所要执行的操作

	* fstime 保存最近一次checkpoint的时间

当有写请求时，NameNode会首先写editlog到磁盘edits文件中，成功后才会修改内存，并向客户端返回

所以，fsimage中的数据**并不是实时的数据**，**而是在达到条件时再进行更新**，**更新过程需要SNN参与**

**NameNode的metadata信息会在启动后加载到内存中**

### SecondaryNameNode

SecondaryNameNode并不是NameNode的热备份，而是**协助着帮助NameNode进行元数据的合并**，从另外的角度来看可以提供一定的备份功能(不能保证所有的数据都能恢复,只有在数据产生合并的时候才有这种性能)，但**并不是热备**，这种合并过程可能会造成极端情况下数据丢失！可以从snn中恢复部分数据，但是无法恢复全部。

**何时触发数据合并？**

1. 根据配置文件设置的时间间隔
   fs.checkpoint.period 默认3600秒
2. 根据配置文件设置的edits log大小 
   fs.checkpoint.size 默认64MB
3. 当Hadoop被重启的时候,也会触发合并

合并过程(参看文档图)：

达到条件后 snn会将nn中的fsimage和edits文件**通过网络**拷贝过来，同时nn中会创建一个新的edits.new文件，新的读写请求会写入到这个edits.new中，在snn中将拷贝过来的fsimage和edits合并为一个新的fsimage，最后snn将合并完成的fsimage文件拷贝回nn中替换之前的fsimage，nn再将edtis.new改为edits

由于NameNode实时数据都在内存中，此处的合并指的是磁盘中的持久化的数据的处理。

判断：snn可以对元数据做一定程度的备份，但是不是热备，对不对？

思考：什么情况下可能造成NameNode元数据信息丢失?

**snn并不是nn的热备，但是能保存大部分备份数据。原因就在于edits.new中的数据丢失了就找不回来了**

通常NameNode和SNN要放置到不同机器中以此提升性能，并提供一定的元数据安全性。

### DataNode

在hadoop中，数据是存放在DataNode上面的。是以Block的形式存储的。

DataNode节点会不断向NameNode节点发送**心跳报告**。

初始化时，每个数据节点将当前存储的数据块告知NameNode节点。

通过向NameNode主动发送心跳保持与其联系（**3秒一次**）

后续DataNode节点在工作的过程中，数据节点仍会不断的更新NameNode节点与之对应的元数据信息，并接受来自NameNode节点的指令，创建、移动或者删除本地磁盘上的数据块。

如果**10分钟都没收到dn的心跳**，则认为其已经lost，并**copy其上的block到其他dn**

Replication。多复本。默认是三个。

### Block复本放置策略：

​	第一个副本：放置在上传文件的DN，如果是集群之外提交，就随机选择一台磁盘不太满，cpu不太忙的节点
​	第二个副本：放置在第一个副本不同机架的节点上
​	第三个副本：放置在与第二个副本相同机架的节点上
​	更多副本：随机节点(哪个节点比较空闲,就放到哪个节点上)
​	*机架感知策略(参看文章)*

### HDFS的shell操作

​	hadoop fs -mkdir /user/trunk
​	hadoop fs -ls /user
​	hadoop fs -lsr /user   (递归的)
​	hadoop fs -put test.txt /user/trunk
​	hadoop fs -put test.txt .  (复制到hdfs当前目录下，首先要创建当前目录)
​	hadoop fs -get /user/trunk/test.txt . (复制到本地当前目录下)
​	hadoop fs -cat /user/trunk/test.txt
​	hadoop fs -tail /user/trunk/test.txt  (查看最后1000字节)
​	hadoop fs -rm /user/trunk/test.txt
​	hadoop fs -rmdir /user/trunk
​	hadoop fs -help ls (查看ls命令的帮助文档)

### HDFS执行流程(参看文档图)

#### HDFS读流程	

1. 使用HDFS提供的客户端开发库Client，向远程的Namenode发起RPC请求；
2. Namenode会视情况返回文件的部分或者全部block列表，对于每个block，Namenode都会返回有该block拷贝的DataNode地址；
3. 客户端开发库Client会选取离客户端最接近的DataNode来读取block；如果客户端本身就是DataNode,那么将从本地直接获取数据
4. 读取完当前block的数据后，关闭与当前的DataNode连接，并为读取下一个block寻找最佳的DataNode；
5. 当读完列表的block后，且文件读取还没有结束，客户端开发库会继续向Namenode获取下一批的block列表。
6. 读取完一个block都会进行checksum验证，如果读取datanode时出现错误，客户端会通知Namenode，然后再从下一个拥有该block拷贝的datanode继续读。
7. 当文件最后一个块也都读取完成后，datanode会连接namenode告知关闭文件。

#### HDFS的写流程

1. 使用HDFS提供的客户端开发库Client，向远程的Namenode发起RPC请求；
2. Namenode会检查要创建的文件是否已经存在，创建者是否有权限进行操作(用户也有不同的权限)，成功则会为文件创建一个记录，否则会让客户端抛出异常；
3. 当客户端开始写入文件的时候，开发者会将文件切分成多个packets，并在内部以数据队列"data queue"的形式管理这些packets，并向Namenode申请新的blocks，获取用来存储replicas的合适的datanodes列表，列表的大小根据在Namenode中对replication的设置而定。
4. 开始以pipeline（管道）的形式将packet写入所 有的replicas中。开发库把packet以流的方式写入第一个datanode，该datanode把该packet存储之后，再将其传递给在此 pipeline中的下一个datanode，直到最后一个datanode，这种写数据的方式呈流水线的形式。
5. 最后一个datanode成功存储之后会返回一个ack packet，在pipeline里传递至客户端，在客户端的开发库内部维护着"ack queue"，成功收到datanode返回的ack packet后会从"ack queue"移除相应的packet。
6. 如果传输过程中，有某个datanode出现了故障，那么当前的pipeline会被关闭，出现故障的datanode会从当前的pipeline中移除， 剩余的block会继续剩下的datanode中继续以pipeline的形式传输，同时Namenode会分配一个新的datanode，保持 replicas设定的数量。

#### HDFS的删除流程

1. 先在NameNode上执行节点名字的删除。
2. 当NameNode执行delete方法时，它只标记操作涉及的需要被删除的数据块，而不会主动联系这些数据块所在的DataNode节点。
3. 当保存着这些数据块的DataNode节点向NameNode节点发送心跳时，在心跳应答里，NameNode节点会向DataNode发出指令，从而把数据删除掉。
4. 所以在执行完delete方法后的一段时间内，数据块才能被真正的删除掉。

<font color="red">注意:</font>在读写过程中,NameNode只负责地址的记录和查询,所有的数据的读写都是客户端和DataNode直接联系,这种形式的好处在于能够提高NameNode的应答速度,同时提供HDFS的线程并发的能力

**安全模式**
**在重新启动HDFS后，会立即进入安全模式**，此时不能操作hdfs中的文件，只能查看目录文件名等，读写操作都不能进行。

namenode启动时，需要载入fsimage文件到内存，同时执行edits文件中各项操作

一旦在内存中成功建立文件系统元数据的映射，则创建一个新的fsimage文件（这个步骤不需要SNN的参与）和一个空的编辑文件。

此时namenode文件系统对于客户端来说是只读的。

再此阶段NameNode收集各个DataNode的报告，当数据块达到最小复本数以上时，会被认为是“安全”的，在一定比例的数据块被确定为安全后，再经过若干时间，安全模式结束

当检测到副本数不足的数据块时，该块会被复制直到到达最小副本数，系统中数据块的位置并不是namenode维护的，而是以块列表的形式存储在datanode中。

当启动报如下错误时:

`org.apache.hadoop.dfs.SafeModeException: Cannot delete /user/hadoop/input. Name node is in safe mode`

使用如下命令退出安全模式:

`​hadoop dfsadmin -safemode leave `

六、java接口方式操作HDFS

1. Eclipse中Hadoop插件的使用

> 当遇到权限拒绝问题时，可以修改hdfs-site.xml中的配置dfs.permissions为false关闭hadoop的权限认证	

```java
//导入jar
hadoop/share/hadoop/common/*.jar
hadoop/share/hadoop/common/lib/*.jar
hadoop/hdfs/*.jar

//--下载
FileSystem fs = FileSystem.get(new URI("hdfs://xxxx:9000")new Configuration());
InputStream in = fs.open(new Path("/xxx"));//HDFS路径
OutputStream out = ..
IOUtils.copyBytes(in,out,buffersize,close);

//--上传
InputStream in = ..
FileSystem fs = FileSystem.get(new URI("hdfs://xxxx:9000")new Configuration());
OutputStream out = fs.create(new Path("..."));//hdfs路径
IOUtils.copyBytes(in,out,buffersize,close);

#问题：Permission Denied -- 权限错误
FileSystem fs = FileSystem.get(new URI("hdfs://xxxx:9000")new Configuration(),"root");

//--删除文件、文件夹(如果要删除的文件夹中有内容，必须选择递归删除)
boolean fs.delete(new Path("目标hdfs路径"),是否递归删除);

//--创建文件夹
boolean fs.mkdirs(new Path(".."));
```
