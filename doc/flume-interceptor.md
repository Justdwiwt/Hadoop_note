# Flume Interceptor

## Interceptor 概述

> 拦截器可以拦截 Event，允许或不允许 Event 通过，或在允许通过的时，改变 Event 内容，这种改变包括改变 Event 的体或头信息。

拦截器可以手动开发，只要实现`org.apache.flume.interceptor.Interceptor`接口，在其中编写拦截规则即可。

Flume 也内置了很多拦截器，可以直接使用。

## Timestamp Interceptor

> 时间戳拦截器，拦截到 Event 后，允许通过，但在头信息中增加时间戳头信息。

``` properties
a1.souces = r1
a1.channels = c1
a1.sources.r1.channels = c1
a1.sources.r1.type = seq
a1.sources.r1.interceptors = i1
a1.sources.r1.interceptors.i1.type = timestamp
```

``` properties
#配置Agent
a1.sources = r1
a1.sinks = k1
a1.channels = c1

#配置Source
a1.sources.r1.type = http
a1.sources.r1.port = 44444
a1.sources.r1.interceptors = i1
a1.sources.r1.interceptors.i1.type = timestamp

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

## Host Interceptor

> 主机名拦截器，拦截下 Event 后，允许通过，但在头信息中增加主机名或 IP 头信息。

``` properties
a1.sources = r1
a1.channels = c1
a1.sources.r1.interceptors = il
a1.sources.r1.interceptors.i1.type = host
```

``` properties
#配置Agent
a1.sources = r1
a1.sinks = k1
a1.channels = c1

#配置Source
a1.sources.r1.type = http
a1.sources.r1.port = 44444
a1.sources.r1.interceptors = i1
a1.sources.r1.interceptors.i1.type = host

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

## Static Interceptor

> 静态拦截器，拦截下 Event 之后，允许通过，但要增加上指定的头和值。

``` properties
a1.sources = r1
a1.channels = c1
a1.sources.r1.channels = c1
a1.sources.r1.type = seq
a1.sources.r1.interceptors = il
a1.sources.r1.interceptors.i1.type = host
a1.sources.r1.interceptors.i1.key = datacenter
a1.sources.r1.interceptors.i1.value = NEW_YORK
```

``` properties
#配置Agent
a1.sources = r1
a1.sinks = k1
a1.channels = c1

#配置Source
a1.sources.r1.type = http
a1.sources.r1.port = 44444
a1.sources.r1.interceptors = i1 i2
a1.sources.r1.interceptors.i1.type = host
a1.sources.r1.interceptors.i2.type = static
a1.sources.r1.interceptors.i2.key = country
a1.sources.r1.interceptors.i2.value = China

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

## UUID Interceptor

> UUID 拦截器，拦截下 Event 之后，允许通过，但要在头上增加上一个 UUID 唯一表示作为头。

``` properties
#配置Agent
a1.sources = r1
a1.sinks = k1
a1.channels = c1

#配置Source
a1.sources.r1.type = http
a1.sources.r1.port = 44444
a1.sources.r1.interceptors = i1 i2 i3
a1.sources.r1.interceptors.i1.type = host
a1.sources.r1.interceptors.i2.type = static
a1.sources.r1.interceptors.i2.key = country
a1.sources.r1.interceptors.i2.value = China
a1.sources.r1.interceptors.i3.type = org.apache.flume.sink.solr.morphline.UUIDInterceptor$Builder

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

## Search and Replace Interceptor

> 搜索和替换拦截器，拦截下 Event 后，通过正则匹配日志中的体，将符合正则的部分替换为指定的内容。

``` properties
#配置Agent
a1.sources = r1
a1.sinks = k1
a1.channels = c1

#配置Source
a1.sources.r1.type = http
a1.sources.r1.port = 44444
a1.sources.r1.interceptors = i1
a1.sources.r1.interceptors.i1.type = search_replace
a1.sources.r1.interceptors.i1.searchPattern = \\d
a1.sources.r1.interceptors.i1.replaceString = *

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

## Regex Filtering Interceptor

> 正则过滤拦截器，拦截下 Event 之后，利用正则匹配日志的体，根据是否匹配决定是否保留或是否取出当前 Event

``` properties
#配置Agent
a1.sources = r1
a1.sinks = k1
a1.channels = c1

#配置Source
a1.sources.r1.type = http
a1.sources.r1.port = 44444
a1.sources.r1.interceptors = i1
a1.sources.r1.interceptors.i1.type = regex_filter
a1.sources.r1.interceptors.i1.regex = ^\\d.*$

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

## Regex Extractor Interceptor

> 正则提取拦截器，拦截下 Event 后，根据正则匹配日志体中的部分内容，加入到头中用指定的名称作为键。
