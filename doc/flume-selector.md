# Flume Selector

## Selector 概述

Selector 即 Flume 中的选择器，主要用在实现扇出过程中实现按照指定方式分发数据。

选择器可以工作在 复制 多路复用(路由)模式下，默认情况下，不配置 Selector，则扇出采用复制机制。

## Selector 实现复制

> 不配置 Selector，默认在扇出时就是复制方式。

## Selector 实现多路复用

> 可以手动配置 Selector，并将其设置为多路复用模式，实现在扇出的过程中按照指定规则分发数据

``` properties
a1.sources = r1
a1.channels = c1 c2 c3 c4
a1.sources.r1.selector.type = multiplexing
a1.sources.r1.selector.header = state
a1.sources.r1.selector.mapping.CZ = c1
a1.sources.r1.selector.mapping.US = c2 c3
a1.sources.r1.selector.default = c4
```

1. 配置 Hadoop02 Hadoop03

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

   #配置channel
   a1.channels.c1.type = memory
   a1.channels.c1.capacity = 1000
   a1.channels.c1.transactionCapacity = 100

   #绑定关系
   a1.sources.r1.channels = c1
   a1.sinks.k1.channel = c1
   ```

2. 配置 Hadoop01

   ``` properties
   #配置Agent
   a1.sources = r1
   a1.sinks = k1 k2
   a1.channels = c1 c2

   #配置Source
   a1.sources.r1.type = http
   a1.sources.r1.port = 44444
   a1.sources.r1.selector.type = multiplexing
   a1.sources.r1.selector.header = gender
   a1.sources.r1.selector.mapping.male = c1
   a1.sources.r1.selector.mapping.female = c2
   a1.sources.r1.selector.default = c1

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
