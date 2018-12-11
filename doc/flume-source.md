# Flume Source

## Avro Source

> 监听 AVRO 端口来接受来自外部 AVRO 客户端的事件流，是实现多级流动、扇出流、扇入流等效果的基础。另外也可以接受通过 flume 提供的 Avro 客户端发送的日志信息。

1. 配置 Agent

   ``` properties
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

   #配置Channel
   a1.channels.c1.type = memory
   a1.channels.c1.capacity = 1000
   a1.channels.c1.transactionCapacity = 100

   #绑定关系
   a1.sources.r1.channels = c1
   a1.sinks.k1.channel = c1
   ```

2. 启动 Agent

   ```bash
   ./flume-ng agent --conf ../conf --conf-file ../conf/template01.conf --name a1 -Dflume.root.logger=INFO,console
   ```

3. 通过 Avro-Client 测试

   ```bash
   ./flume-ng avro-client --conf ../conf --host 0.0.0.0 --port 44444 --filename /home/nums.txt
   ```

## Exec Source

> 可以将命令产生的输出作为源

1. 配置 Agent

   ``` properties
   #配置Agent
   a1.sources = r1
   a1.sinks = k1
   a1.channels = c1

   #配置Source
   a1.sources.r1.type = exec
   a1.sources.r1.command = ping www.baidu.com

   #配置Sink
   a1.sinks.k1.type = logger

   #配置Channel
   a1.channels.c1.type = memory
   a1.channels.c1.capacity = 1000
   a1.channels.c1.transactionCapacity = 100

   #绑定关系
   a1.sources.r1.channels = c1
   a1.sinks.k1.channel = c1
   ```

2. 启动 Agent

   ```bash
   ./flume-ng agent --conf ../conf --conf-file ../conf/template02.conf --name a1 -Dflume.root.logger=INFO,console
   ```

## Spooling Directory Source

> 这个 Source 允许你将将要收集的数据放置到"自动搜集"目录中。这个 Source 将监视该目录，并将解析新文件的出现。

事件处理逻辑是可插拔的，当一个文件被完全读入信道，它会被重命名或可选的直接删除。

- 放置到自动搜集目录下的文件不能修改，如果修改，则 flume 会报错。
- 不能产生重名的文件，如果有重名的文件被放置进来，则 flume 会报错。

## NetCat Source

> 一个 NetCat Source 用来监听一个指定端口，并将接收到的数据的每一行转换为一个事件。

## Sequence Generator Source -- 序列发生器源

> 一个简单的序列发生器，不断的产生事件，值是从 0 开始每次递增 1。

1. 配置 Agent

   ``` properties
   #配置Agent
   a1.sources = r1
   a1.sinks = k1
   a1.channels = c1

   #配置Source
   a1.sources.r1.type = seq

   #配置Sink
   a1.sinks.k1.type = logger

   #配置Channel
   a1.channels.c1.type = memory
   a1.channels.c1.capacity = 1000
   a1.channels.c1.transactionCapacity = 100

   #绑定关系
   a1.sources.r1.channels = c1
   a1.sinks.k1.channel = c1
   ```

## HTTP Source

> 此 Source 接受 HTTP 的 GET 和 POST 请求作为 Flume 的事件。其中 GET 方式应该只用于试验。

需要提供一个可插拔的"处理器"来将请求转换为事件对象，这个处理器必须实现 HTTPSourceHandler 接口。

常见的 Handler：

```json
[
  {
    "headers": {
      "timestamp": "434324343",
      "host": "random_host.example.com"
    },
    "body": "random_body"
  },
  {
    "headers": {
      "namenode": "namenode.example.com",
      "datanode": "random_datanode.example.com"
    },
    "body": "really_random_body"
  }
]
```

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
   a1.sinks.k1.type = logger

   #配置Channel
   a1.channels.c1.type = memory
   a1.channels.c1.capacity = 1000
   a1.channels.c1.transactionCapacity = 100

   #绑定关系
   a1.sources.r1.channels = c1
   a1.sinks.k1.channel = c1
   ```

2. 通过 curl 命令测试

   ```bash
   curl -X POST -d '[{ "headers" :{"a" : "a1","b" : "b1"},"body" : "hello~http~flume~"}]' http://hadoop01:44444
   ```

## Custom Source

如果以上内置的 Source 都不能满足需求，可以自己开发 Source：

1. 按照 Flume 要是写一个类实现相应接口。
2. 将类打成 jar 放置到 flume 的 lib 目录下。
3. 在配置文件中通过类的全路径名加载 Source。
