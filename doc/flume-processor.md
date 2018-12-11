# Flume Processor

## Processor 概述

> Flume 用于实现失败恢复负载均衡的组件。

在企业级开发中，通常有多个客户端 Agent 来收集数据，发送给中心服务器 Agent，中心服务器 Agent 要承载若干客户端 Agent 发送的数据，负载较高，且中心服务器 Agent 如果只有一个，会造成单节点故障风险。

所以在企业级开发中，中心服务器 Agent 往往不止一个，由若干跟协同工作，此时客户端 Agent 如何分配数据给中新服务器 Agent 就成了问题。

Processor 就是用来这个过程中实现数据分配的失败恢复和负载均衡的组件。

需要为多个中心服务器配置 Sink，将这些 Sink 组成 SinkGroup 组，再为这个组配置 Processor，指定处理机制和其他参数。

之后将这些 Sink 连接到同一个 Channel，Processor 可以通过改变 Channel 的指向，将数据根据规则实现分发。

- processor 可以工作在**失败恢复**和**负载均衡**两种模式下。

## Processor 的失败恢复机制

> 失败恢复机制下，Processor 将会维护一个 sink 们的优先表。sink 们可以被配置一个优先级，数字越大优先级越高。事件将永远将只会发往优先级最高的 Sink。只要有一个 Sink 存活，整个过程仍然可以进行。

如果没有指定优先级，则优先级顺序取决于 sink 们的配置顺序，先配置的默认优先级高于后配置的。

``` properties
a1.sinkgroups = g1
a1.sinkgroups.g1.sinks = k1 k2
a1.sinkegroups.g1.processos.type = failover
a1.sinkegroups.g1.processos.priority.k1 = 5
a1.sinkegroups.g1.processos.priority.k2 = 10
a1.sinkegroups.g1.processos.maxpenalty = 10000
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
   a1.channels = c1

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

   a1.sinkgroups = g1
   a1.sinkgroups.g1.sinks = k1 k2
   a1.sinkgroups.g1.processor.type = failover
   a1.sinkgroups.g1.processor.priority.k1 = 5
   a1.sinkgroups.g1.processor.priority.k2 = 10
   a1.sinkgroups.g1.processor.maxpenalty = 10000

   #配置Channel
   a1.channels.c1.type = memory
   a1.channels.c1.capacity = 1000
   a1.channels.c1.transactionCapacity = 100

   #绑定关系
   a1.sources.r1.channels = c1
   a1.sinks.k1.channel = c1
   a1.sinks.k2.channel = c1
   ```

## Processor 的负载均衡机制

> Processor 的负载均衡机制提供了在多个 sink 之间实现负载均衡的能力。

它维护了一个活动 sink 的索引列表。

通过 Processor 动态切换 channel 在 SinkGroup 中对 Sink 的指向，实现数据的负载均衡方式分发。

支持**轮询**或**随机方式**的负载均衡，默认值是**轮询方式**，可以通过配置指定。

负载均衡模式下，如果某个中心服务器宕机，则 Processor 会将该中心服务器 Sink 剔除 SinkGroup 组，并将之前发送失败的数据发给其他仍然存活的 Sink，所以可以认为 Processor 的负载均衡机制自带失败恢复的能力。

``` properties
a1.sinkgroups = g1
a1.sinkgroups.g1.sinks = k1 k2
a1.sinkgroups.g1.processor.type = load_balance
a1.sinkgroups.g1.processor.backoff = true
a1.sinkgroups.g1.processor.selector = random
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
   a1.sinks.k1.channel = c
   ```

2. 配置 Hadoop01

   ``` properties
   #配置Agent
   a1.sources = r1
   a1.sinks = k1 k2
   a1.channels = c1

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

   a1.sinkgroups = g1
   a1.sinkgroups.g1.sinks = k1 k2
   a1.sinkgroups.g1.processor.type = load_balance
   a1.sinkgroups.g1.processor.backoff = true
   a1.sinkgroups.g1.processor.selector = round_robin

   #配置Channel
   a1.channels.c1.type = memory
   a1.channels.c1.capacity = 1000
   a1.channels.c1.transactionCapacity = 100

   #绑定关系
   a1.sources.r1.channels = c1
   a1.sinks.k1.channel = c1
   a1.sinks.k2.channel = c1
   ```
