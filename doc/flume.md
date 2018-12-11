# Flume

## Flume 概述

> flume 是分布式的，可靠的，用于从不同的来源有效收集、聚集和移动大量的日志数据用以集中式管理的系统。是 apache 的一个顶级项目。

## Flume 的安装配置

1. 下载 flume

   [http://flume.apache.org/](http://flume.apache.org/)

   - flume0.9x 和 flume1.x 版本互不兼容

2. 安装 jdk，配置环境变量
3. 解压安装

## Flume 的基本概念

### Flume Event - Flume 事件

> 被定义为一个具有有效荷载的字节数据流和可选的字符串属性集。

一条日志在 flume 中会被转换成一个 JSON 格式的串来传递，这个 JSON 串就是一个 FlumeEvent，具体的格式为{header:{头信息},body:日志内容}，所以简单来说，一条日志在一个 Flume 就对应一个 JSON 串，即，一个 FlumeEvent。

### Flume Agent- Flume 代理

> 是一个进程承载从外部源事件流到下一个目的地的过程。包含 Source Channel 和 Sink。

是 Flume 的基本组件，一个 Agent 就是一个基本的日志收集单元，由 Source Channel 和 Sink 组成。多个 Agent 之间还可以连接 形成复杂的日志流动的网络。

### Source - 数据源

> 消耗外部传递给他的事件，外部源将数据按照 Flume Source 能识别的格式将 Flume 事件发送给 Flume Source

Agent 中的组件，负责连接到数据源，获取数据原发送来的数据，将数据转换为 FlumeEvent 存储到 Channel 中。

### Channel - 数据通道

> 是一个被动的存储，用来保持事件，直到由一个 Flume Sink 消耗。

连接 Source 和 Sink 的通道，Source 将 Event 写入 Channel，Sink 从 Channel 中消费 Event。本质上是一种存储结构，用来缓冲 Source 和 Sink 之间速度不一致的问题，提供中间缓冲存储能力。

### Sink - 数据汇聚点

> 代表外部数据存放位置。发送 flume 中的事件到指定的外部目标。

连接到目的地，从 Channel 中消费 Event，并将 Event 中的数据发送给目的地。

## Flume 的特点

### 复杂流动

Flume 允许用户进行多级流动到最终目的地，也允许扇出流(一到多)、扇入流(多到一)的流动和故障转移、失败处理。

### 可靠性

事务型的数据传递，保证数据的可靠性。

### 可恢复

通道可以以内存或文件的方式实现，内存更快，但是不可恢复，文件比较慢但提供了可恢复性。

## Flume 入门案例

1. 配置 Agent

   ``` properties
   #example.conf：单节点Flume配置
   #命名Agent a1的组件
   a1.sources  =  r1
   a1.sinks  =  k1
   a1.channels  =  c1

   #描述/配置Source
   a1.sources.r1.type  =  netcat
   a1.sources.r1.bind  =  0.0.0.0
   a1.sources.r1.port  =  44444

   #描述Sink
   a1.sinks.k1.type  =  logger

   #描述内存Channel
   a1.channels.c1.type  =  memory
   a1.channels.c1.capacity  =  1000
   a1.channels.c1.transactionCapacity  =  100

   #为Channle绑定Source和Sink
   a1.sources.r1.channels  =  c1
   a1.sinks.k1.channel  =  c1
   ```

2. 启动 agent

   ```bash
   ./flume-ng agent --conf ../conf --conf-file ../conf/example.conf --name a1 -Dflume.root.logger=INFO,console
   ```

3. 通过 Telnet 工具连接 Flume 发送数据进行测试
