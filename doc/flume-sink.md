# Flume Sink

## Logger Sink

> 记录指定级别的日志，通常用于调试。

## File Roll Sink

> 在本地文件系统中存储事件。每隔指定时长生成文件保存这段时间内收集到的日志信息。

1. 配置 Agent

   ``` properties
   #配置Agent
   a1.sources = r1
   a1.sinks = k1
   a1.channels = c1

   #配置Source
   a1.sources.r1.type = http
   a1.sources.r1.port = 44444

   #配置Sink
   a1.sinks.k1.type = file_roll
   a1.sinks.k1.sink.directory = /home/fresult

   #配置Channel
   a1.channels.c1.type = memory
   a1.channels.c1.capacity = 1000
   a1.channels.c1.transactionCapacity = 100

   #绑定关系
   a1.sources.r1.channels = c1
   a1.sinks.k1.channel = c1
   ```

## HDFS Sink

> 将事件写入到 Hadoop 分布式文件系统 HDFS 中。支持创建文本文件和序列化文件，这两种格式都支持压缩。

- 此版本 hadoop 必须支持`sync()`调用。

1. 配置 Agent

   ``` properties
   #配置Agent
   a1.sources = r1
   a1.sinks = k1
   a1.channels = c1

   #配置Source
   a1.sources.r1.type = http
   a1.sources.r1.port = 44444

   #配置Sink
   a1.sinks.k1.type = hdfs
   a1.sinks.k1.hdfs.path = hdfs://hadoop01:9000/flumedata
   a1.sinks.k1.hdfs.fileType = DataStream
   #配置Channel
   a1.channels.c1.type = memory
   a1.channels.c1.capacity = 1000
   a1.channels.c1.transactionCapacity = 100

   #绑定关系
   a1.sources.r1.channels = c1
   a1.sinks.k1.channel = c1
   ```

## Avro Sink

> 和 AvroSource 配置使用，是实现复杂流动的基础。

1.  实现多级流动

    a. 配置 Hadoop01

        ```properties
        #配置Agent
        a1.sources = r1
        a1.sinks = k1
        a1.channels = c1

        #配置Source
        a1.sources.r1.type = http
        a1.sources.r1.port = 44444

        #配置Agent
        a1.sinks.k1.type = avro
        a1.sinks.k1.hostname = hadoop02
        a1.sinks.k1.port = 44444

        #配置channel
        a1.channels.c1.type = memory
        a1.channels.c1.capacity = 1000
        a1.channels.c1.transactionCapacity = 100

        #绑定关系
        a1.sources.r1.channels = c1
        a1.sinks.k1.channel = c1
        ```

    b. 配置 Hadoop02

        ```properties
        #配置Agent
        a1.sources = r1
        a1.sinks = k1
        a1.channels = c1

        #配置Source
        a1.sources.r1.type = avro
        a1.sources.r1.bind = 0.0.0.0
        a1.sources.r1.port = 44444

        #配置Sink
        a1.sinks.k1.type = avro
        a1.sinks.k1.hostname = hadoop03
        a1.sinks.k1.port = 44444

        #配置channel
        a1.channels.c1.type = memory
        a1.channels.c1.capacity = 1000
        a1.channels.c1.transactionCapacity = 100

        #绑定关系
        a1.sources.r1.channels = c1
        a1.sinks.k1.channel = c1
        ```

    c. 配置 Hadoop03

        ```properties
        #配置Agent
        a1.sources = r1
        a1.sinks = k1
        a1.channels = c1

        #配置Source
        a1.sources.r1.type = avro
        a1.sources.r1.bind = 0.0.0.0
        a1.sources.r1.port = 44444

        #配置Sink
        a1.sinks.k1.type = logger

        #配置channel
        a1.channels.c1.type = memory
        a1.channels.c1.capacity = 1000
        a1.channels.c1.transactionCapacity = 100

        #绑定关系
        a1.sources.r1.channels = c1
        a1.sinks.k1.channel = c1
        ```

2.  实现扇入流动

    a. 配置 Hadoop02 Hadoop03

        ```properties
        #配置Agent
        a1.sources = r1
        a1.sinks = k1
        a1.channels = c1

        #配置Source
        a1.sources.r1.type = http
        a1.sources.r1.port = 44444

        #配置Agent
        a1.sinks.k1.type = avro
        a1.sinks.k1.hostname = hadoop01
        a1.sinks.k1.port = 44444

        #配置channel
        a1.channels.c1.type = memory
        a1.channels.c1.capacity = 1000
        a1.channels.c1.transactionCapacity = 100

        #绑定关系
        a1.sources.r1.channels = c1
        a1.sinks.k1.channel = c1
        ```

    b. 配置 Hadoop01

        ```properties
        #配置Agent
        a1.sources = r1
        a1.sinks = k1
        a1.channels = c1

        #配置Source
        a1.sources.r1.type = avro
        a1.sources.r1.bind = 0.0.0.0
        a1.sources.r1.port = 44444

        #配置Agent
        a1.sinks.k1.type = logger

        #配置channel
        a1.channels.c1.type = memory
        a1.channels.c1.capacity = 1000
        a1.channels.c1.transactionCapacity = 100

        #绑定关系
        a1.sources.r1.channels = c1
        a1.sinks.k1.channel = c1
        ```

3.  实现扇出操作

    a. 配置 Hadoop02 Hadoop03

        ```properties
        #配置Agent
        a1.sources = r1
        a1.sinks = k1
        a1.channels = c1

        #配置Source
        a1.sources.r1.type = avro
        a1.sources.r1.bind = 0.0.0.0
        a1.sources.r1.port = 44444

        #配置Sink
        a1.sinks.k1.type = logger

        #配置channel
        a1.channels.c1.type = memory
        a1.channels.c1.capacity = 1000
        a1.channels.c1.transactionCapacity = 100

        #绑定关系
        a1.sources.r1.channels = c1
        a1.sinks.k1.channel = c1
        ```

    b. 配置 Hadoop01

        ```properties
        #配置Agent
        a1.sources = r1
        a1.sinks = k1 k2
        a1.channels = c1 c2

        #配置Source
        a1.sources.r1.type = http
        a1.sources.r1.port = 44444

        #配置Sink
        a1.sinks.k1.type = avro
        a1.sinks.k1.hostname = hadoop02
        a1.sinks.k1.port = 44444

        a1.sinks.k2.type = avro
        a1.sinks.k2.hostname = hadoop03
        a1.sinks.k2.port = 44444

        #配置channel
        a1.channels.c1.type = memory
        a1.channels.c1.capacity = 1000
        a1.channels.c1.transactionCapacity = 100

        a1.channels.c2.type = memory
        a1.channels.c2.capacity = 1000
        a1.channels.c2.transactionCapacity = 100

        #绑定关系
        a1.sources.r1.channels = c1 c2
        a1.sinks.k1.channel = c1
        a1.sinks.k2.channel = c2
        ```

## Custom Sink

如果以上内置的 Sink 都不能满足需求，可以自己开发 Sink：

1. 按照 Flume 要是写一个类实现相应接口。
2. 将类打成 jar 放置到 flume 的 lib 目录下。
3. 在配置文件中通过类的全路径名加载 Sink。
