# Hive

## Hive简介

Hive是基于Hadoop的一个数据仓库工具。

可以将结构化的数据文件映射为一张表，并提供完整的sql查询功能，可以将sql语句转换为MapReduce任务进行运行。

其优点是学习成本低，可以通过类SQL语句快速实现MapReduce统计，不必开发专门的MapReduce应用，十分适合数据仓库的统计分析。

**Hive是建立在 Hadoop 上的数据仓库基础构架。**

它提供了一系列的工具，可以用来进行数据提取、转化、加载（ETL Extract-Transform-Load ），这是一种可以存储、查询和分析存储在 Hadoop 中的大规模数据的机制。Hive 定义了简单的类 SQL 查询语言，称为 HiveQL，它允许熟悉 SQL 的用户查询数据。

### HQL

> Hive通过类SQL的语法，来进行分布式的计算。

Hive在执行的过程中会将HQL转换为MapReduce去执行，所以Hive其实是基于Hadoop的一种分布式计算框架，底层仍然是MapReduce，所以它本质上还是一种离线大数据分析工具。

**数据仓库的特征**

1. 数据仓库是多个异构数据源所集成的。
2. 数据仓库存储的一般是历史数据。 
3. 数据库是为捕获数据而设计，数据仓库是为分析数据而设计。
4. 数据仓库是时变的，数据存储从历史的角度提供信息。即数据仓库中的关键结构都隐式或显示地包含时间元素。
5. 数据仓库是弱事务的，因为数据仓库存的是历史数据，一般都读（分析）数据场景。

* 数据库属于OLTP系统。（Online Transaction Processing）联机事务处理系统。涵盖了企业大部分的日常操作，如购物、库存、制造、银行、工资、注册、记账等。
    * OLTP是面向用户的、用于程序员的事务处理以及客户的查询处理。
    * OLTP系统的访问由于要保证原子性，所以有事务机制和恢复机制。
* 数据仓库属于OLAP系统。（Online Analytical Processing）联机分析处理系统。
    * OLAP是面向市场的，用于知识工人（经理、主管和数据分析人员）的数据分析。
    * OLAP通常会集成多个异构数据源的数据，数量巨大。
    * OLAP系统一般存储的是历史数据，所以大部分都是只读操作，不需要事务。

### 适用场景

Hive 构建在基于静态批处理的Hadoop 之上，Hadoop 通常都有较高的延迟并且在作业提交和调度的时候需要大量的开销。
**因此，Hive 并不能够在大规模数据集上实现低延迟快速的查询。**
因此，Hive 并不适合那些需要低延迟的应用，例如，联机事务处理(OLTP)。

Hive 查询操作过程严格遵守Hadoop MapReduce 的作业执行模型，Hive 将用户的HiveQL 语句通过解释器转换为MapReduce 作业提交到Hadoop 集群上，Hadoop 监控作业执行过程，然后返回作业执行结果给用户。Hive 并非为联机事务处理而设计，Hive 并不提供实时的查询和基于行级的数据更新操作。

**Hive 的最佳使用场合是大数据集的批处理作业，例如，网络日志分析。**

## 安装

1. 安装对应版本的JDK
2. 安装Hadoop
3. 配置JDK和Hadoop的环境变量
4. 解压Hive安装包
5. 启动Hadoop的HDFS和Yarn
6. 启动Hive
   
   ```bash
   sh hive
   ```

## 安装MySQL

1. 卸载自带mysql

    ```
    rpm -qa | grep mysql
    rpm -ev --nodeps mysql-libs-5.1.71-1.el6.x86_64
    ```

2. 新增mysql用户组，并创建mysql用户

    ```bash
    groupadd mysql
    useradd -r -g mysql mysql
    ```

3. 安装mysql server rpm包和client包

    ```bash
    rpm -ivh MySQL-server-5.6.29-1.linux_glibc2.5.x86_64.rpm
    rpm -ivh MySQL-client-5.6.29-1.linux_glibc2.5.x86_64.rpm
    ```

4. 修改my.cnf

    ```bash
    vim /usr/my.cnf
    
    [client]
    default-character-set=utf8
    [mysql]
    default-character-set=utf8
    [mysqld]
    character_set_server=utf8
    ```
    
5. 将mysqld加入系统服务，并随机启动

    ```bash
    # /etc/init.d 是linux的一个特殊目录，放在这个目录的命令会随linux开机而启动.
    cp /usr/share/mysql/mysql.server /etc/init.d/mysqld
    ```

6. 启动mysqld

    ```bash
    service mysqld start
    ```

7. 查看初始生成的密码

    ```bash
    vim /root/.mysql_secret
    ```

8. 修改初始密码

    ```bash
    mysqladmin -u root -p password root
    ```

9. 进入mysql数据库

    ```bash
    mysql -u root -p
    # password = root
    ```

## Hive依赖MySQL

1. 删除hdfs中的/user/hive
    
    ```bash
    hadoop fs -rmr /user/hive
    ```

2. 将mysql驱动jar包上传到hive安装目录的lib目录下

3. 编辑新的配置文件：hive-site.xml

    ```bash
    cd conf
    vim hive-site.xml
    ```

    ```xml
    <configuration>
    
        <property>
            <name>javax.jdo.option.ConnectionURL</name>
            <value>jdbc:mysql://hadoop01:3306/hive?createDatabaseIfNotExist=true</value>
        </property>
        
        <property>
            <name>javax.jdo.option.ConnectionDriverName</name>
            <value>com.mysql.jdbc.Driver</value>
        </property>
        
        <property>
            <name>javax.jdo.option.ConnectionUserName</name> 
            <value>root</value>
        </property>
        
        <property>
            <name>javax.jdo.option.ConnectionPassword</name>
            <value>root</value>
        </property>
    
    </configuration>
    ```

4. 进入到mysql数据库，进行权限分配

    ```mysql
    grant all privileges on *.* to 'root'@'hadoop01' identified by 'root' with grant option;
    flush privileges;
    ```

5. 开启远程登录权限

    ```mysql
    grant all privileges on *.* to 'name'@'%' identified by 'password' with grant option;
    flush privileges;
    ```

6. 查看数据库的用户

    ```mysql
    select distinct concat('User:''',user,'''@''',host,''';')as query from mysql.user;
    ```

7. 创建hive数据库

    ```mysql
    # hive要求存储元数据的字符集必须是iso8859-1
    create database hive character set latin1;
    ```

## Hive的内部表和外部表

> 在查看元数据信息时，有一张TBLS表，其中有一个字段属性：TBL_TYPE:MANAGED_TABLE(MANAGED_TABLE 表示内部表)。

**内部表的概念**

先在hive里建一张表，然后向这个表插入数据（用insert可以插入数据，也可以通过加载外部文件方式来插入数据），这样的表称之为hive的内部表。

**外部表的概念**

HDFS里已经有数据了，比如有一个2.txt文件，里面存储了这样的一些数据：

```
1 jary
2 rose
```

通过hive创建一张表stu来管理这个文件数据。则stu这样表称之为外部表。

* hive外部表管理的是HDFS里的某一个目录下的文件数据。所以，做这个实验，要先HDFS创建一个目录节点，然后把数据文件上传到这个目录节点下。

**创建外部表的命令：**

```sql
create external table stu (id int,name string) row format delimited fields terminated by '' location '/目录路径'
```

hive无论是内部表或外部表，当向HDFS对应的目录节点下追加文件时（只要格式符合），hive都可以把数据管理进来。

**内部表和外部标的区别**

通过hive执行：drop  table stu 。

* 如果stu是一个内部表，则HDFS对应的目录节点会被删除。
* 如果stu是一个外部表，HDFS对应的目录节点不会删除。

## Hive分区表

**概念**

Hive也支持分区表,对数据进行分区可以提高查询时的效率。

普通表和分区表区别：有大量数据增加的需要建分区表。

**语法**

```sql
create table book (id int, name string) partitioned by (category string) 
row format delimited fields terminated by '\t';
```

* 在创建分区表时，partitioned字段可以不在字段列表中。生成的表中自动就会具有该字段。category 是自定义的字段。

**分区表加载数据**

```sql
load data local inpath '/home/cn.txt' overwrite into table book partition (category='cn');
load data local inpath './book_english.txt' overwrite into table book partition (category='en');
-- 经检查发现分区也是一个目录

-- 查询book目录下的所有数据
select * from book;

-- 只查询 cn分区的数据
select * from book where category='cn';
```

**通过创建目录来增加分区**

```sql
-- 这行命令的作用是在元数据Dock表里创建对应的元数据信息
ALTER TABLE book add  PARTITION (category = 'jp') location '/user/hive/warehouse/park.db/book/category=jp';
```

### 分区命令

**显示分区**

```sql
show partitions iteblog;
```

**添加分区**

```sql
alter table book add partition (category='jp') location '/user/hive/warehouse/test.db/book/category=jp';

-- or

msck repair table book;
```

**删除分区**

```sql
alter table book drop partition(category='cn')
```

**修改分区**

```sql
alter table book partition(category='french') rename to partition (category='hh');
```

## Hive 数据类型

**常用的基本数据类型**

| 基本数据类型 | 所占字节 |
|--------------|----------|
| int          | &nbsp;   |
| boolean      | &nbsp;   |
| float        | &nbsp;   |
| double       | &nbsp;   |
| string       | &nbsp;   |

**复杂数据类型**

| 复杂数据类型 | 说明                                                                                      |
|--------------|-------------------------------------------------------------------------------------------|
| array        | array类型是由一系列相同数据类型的元素组成。并且可以通过下标来进行访问。注意:下标从0开始计 |
| map          | map包含key-value 键值对，可以通过key来访问元素                                            |
| struct       | struct 可以包含不同数据类型元素。相当于一个对象结构。可以通过 对象.属性 来访问            |

### 案例

#### array

```sql
-- 元数据：
-- 100,200,300 
-- 200,300,500

-- 建表语句：
create external table ex(vals array<int>) row format delimited fields terminated by '\t' collection items terminated by ',' location '/ex';

-- 查询每行数组的个数，查询语句：
select  size(vals) from ex;

-- 注：hive 内置函数不具备查询某个具体行的数组元素。需要自定义函数来实现，但这样的需求在实际开发里很少，所以不需要在意。
```

```sql
-- 元数据：
-- 100,200,300  tom,jary
-- 200,300,500  rose,jack

-- 建表语句：
create external table ex1(info1 array<int>,info2 array<string>) row format delimited fields terminated by '\t' collection items terminated by ',' location '/ex';
```

#### map

```sql
-- 元数据：
-- tom,23
-- rose,25
-- jary,28

-- 建表语句：
create external table m1 (vals map<string,int>) row format delimited fields terminated by '\t' map keys terminated by ',' location '/map';

-- 查询语句：
select vals['tom'] from m1;
```

```sql
-- 要求查询tom这个人都浏览了哪些网站，并且为null的值不显示
-- 源数据（分隔符为空格）：
-- tom 192.168.234.21
-- rose 192.168.234.21
-- tom 192.168.234.22
-- jary 192.168.234.21
-- tom 192.168.234.24
-- tom 192.168.234.21
-- rose 192.168.234.21
-- tom 192.168.234.22
-- jary 192.168.234.21
-- tom 192.168.234.22
-- tom 192.168.234.23

-- 建表语句 
create external table ex (vals map<string,string>) row format delimited fields terminated by '/t' map keys terminated by ' ' location '/ex';

-- 注意：map类型，列的分割符必须是\t

-- 查询语句
select vals['tom'] from ex where vals['tom'] is not null;

-- 如果想做去重工作，可以调用distinct内置函数
select distinct(ip) from (select vals['tom'] ip from ex where vals['tom'] is not null)ex1;select distinct(vals['tom']) from m2 where vals['tom'] is not null;
```

#### struct

```sql
-- 元数据：
-- tom 23
-- rose 22
-- jary 26
-- 建表语句：
create external table ex (vals struct<name:string,age:int>)row format delimited collection items terminated by ' '  location '/ex';

-- 查询语句：
select vals.age from ex where vals.name='tom';
```

## Hive explode

> explode 命令可以将行数据，按指定规则切分出多行。

**案例一，利用split执行切分规则**

```
有如下数据：
100,200,300
200,300,500

要将上面两行数据根据逗号拆分成多行（每个数字占一行）
```

**实现步骤**

1. 准备元数据
2. 上传HDFS，并创建对应的外部表
    
    ```sql
    -- 用explode做行切分，注意表里只有一列，并且行数据是string类型，因为只有字符类型才能做切分。
    create external table ex1 (num string) location '/ex';
    ```

3. 通过explode指令来做行切分

    ```sql
    select explode(split(num,',')) from ex1;
    ```

## Hive常用字符串操作函数

| 返回类型 | 函数名                                                    | 描述                                                                                                                                                |
|----------|-----------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| int      | length(string A)                                          | 返回字符串A的长度                                                                                                                                   |
| &nbsp;   | &nbsp;                                                    | select length(weoirjewo);                                                                                                                           |
| &nbsp;   | &nbsp;                                                    | select length(name) from stu;                                                                                                                       |
| &nbsp;   | &nbsp;                                                    | 此函数在实际工作，可以用于校验手机号，身份号等信息的合法性                                                                                          |
| string   | reverse(string A)                                         | 返回字符串A的反转结果                                                                                                                               |
| &nbsp;   | &nbsp;                                                    | select reverse('abcd');                                                                                                                             |
| &nbsp;   | &nbsp;                                                    | select length(name) from stu;                                                                                                                       |
| string   | concat(string A, string B…)                               | 字符串连接函数                                                                                                                                      |
| &nbsp;   | &nbsp;                                                    | select concat ('a','b');                                                                                                                            |
| &nbsp;   | &nbsp;                                                    | select concat(id,name) from stu;                                                                                                                    |
| &nbsp;   | &nbsp;                                                    | select concat(id,',',name) from stu;                                                                                                                |
| string   | concat_ws(string SEP, string A, string B…)                | 带分隔符字符串连接函数：concat_ws                                                                                                                   |
| &nbsp;   | &nbsp;                                                    | select concat_ws('.','www','baidu','com');                                                                                                          |
| string   | substr                                                    | substr,substring                                                                                                                                    |
| &nbsp;   | &nbsp;                                                    | select substr('abcde',2);从第二个截，截到结尾                                                                                                       |
| &nbsp;   | &nbsp;                                                    | select substr('abcde',1,3);从第一个截，截三个长度                                                                                                   |
| &nbsp;   | &nbsp;                                                    | select substr('wfeww',-2);从尾部截，截两个长度                                                                                                      |
| &nbsp;   | &nbsp;                                                    | 可以用于比如截取身份证后几位操作                                                                                                                    |
| string   | upper(string a)                                           | 转大写                                                                                                                                              |
| &nbsp;   | ucase(string a)                                           | &nbsp;                                                                                                                                              |
| string   | lower(string a)                                           | 转小写                                                                                                                                              |
| &nbsp;   | lcase(string a)                                           | &nbsp;                                                                                                                                              |
| string   | trim(string a)                                            | 去空格                                                                                                                                              |
| &nbsp;   | &nbsp;                                                    | select trim (' fwoei ');                                                                                                                            |
| string   | ltrim(string a)                                           | 左边去空格函数                                                                                                                                      |
| string   | rtrim(string a)                                           | 右边去空格函数                                                                                                                                      |
| string   | regexp_replace(string A, string B, string C)              | 将字符串A中的符合java正则表达式B的部分替换为C。注意，在有些情况下要使用转义字符,对需要转义的字符，用[]，比如[*]，类似oracle中的regexp_replace函数。 |
| string   | regexp_extract(string subject, string pattern, int index) | 将字符串subject按照pattern正则表达式的规则拆分，返回index指定的字符                                                                                 |
| &nbsp;   | &nbsp;                                                    | select regexp_extract('foothebar', 'foo(.*)(bar)', 1)；                                                                                             |
| &nbsp;   | &nbsp;                                                    | select regexp_extract('foothebar', 'foo(.*)(bar)', 2)；                                                                                             |
| &nbsp;   | &nbsp;                                                    | select regexp_extract('foothebar', 'foo(.*)(bar)', 0)；                                                                                             |
| string   | repeat(string str, int n)                                 | 返回重复n次后的str字符串                                                                                                                            |
| &nbsp;   | &nbsp;                                                    | select repeat('abc',5)                                                                                                                              |
| array    | split(string str, string pat)                             | 分割字符串函数: split                                                                                                                               |
| &nbsp;   | &nbsp;                                                    | 按照pat字符串分割str，会返回分割后的字符串数组                                                                                                      |
| &nbsp;   | &nbsp;                                                    | select split('abtcdtef','t')；                                                                                                                      |
| &nbsp;   | &nbsp;                                                    | ["ab","cd","ef"]                                                                                                                                    |

## Hive的UDF

> 如果hive的内置函数不够用，我们也可以自己定义函数来使用，这样的函数称为hive的用户自定义函数，简称UDF。

**实现步骤**

1. 新建java工程，导入hive相关包，导入hive相关的lib。
2. 创建类继承UDF
3. 自己编写一个evaluate方法，返回值和参数任意。

    ```java
    import org.apache.hadoop.hive.ql.exec.UDF;
    
    public class ToUpperUDF extends UDF {
        public String evaluate(String str) {
            return str.toUpperCase();
        }
    }
    ```

4. 为了能让mapreduce处理，String要用Text处理。
5. 将写好的类打成jar包，上传到linux中。
6. 在hive命令行下，向hive注册UDF：`add jar /xxxx/xxxx.jar`
7. 在hive命令行下，为当前udf起一个名字：`create temporary function fname as '类的全路径名';`
8. 之后就可以在hql中使用该自定义函数了。
