# data-flow
### (目前还未完全开发完成, 待测试和完善以及文档编写)
## 介绍
一个数据处理框架, 可从一个或多个源端将数据处理并同步至一个或多个目标端, 全配置化同步策略, 借助于SpringEL表达式, 支持强大的逻辑处理

## 依赖环境
- Java: 基于Java SE 8+ 开发
- MongoDB: 用于存储程序的配置文件, 或者也可自行实现配置的增删改方式
- Redis
- Kafka: 若消息从kafka接入(例如canal, ogg等工具)或者需要写入目标为Kafka, 则需要配置kafka
- MySQL: 写入目标为MySQL时需要
- 其他: 若数据需要写入到Oracle, SQLServer, Rocket, Rabbit等存储介质时, 需要对应配置

## 使用教程
- data-flow-starter模块作为spring starter引入其他项目中, 可高度定制化
- data-flow-server模块可直接启动, 使用了默认的配置化数据处理方案

## 架构介绍

### 核心概念
- <b>流(Flow)</b>  
  数据流, 一条流中可包含多个节点, 数据会一一经过这些节点去处理, 除第一个节点外, 其余每一个节点的输入都是上一个节点的输出, 在每个节点中, 都可以访问到原始数据(SourceEntity), 都可以进行转换、服务调用、导出操作，多个流之间互不干扰, 一条数据可并行进入多个流处理
- <b>SourceEntity(ink.andromeda.dataflow.core.SourceEntity)</b>  
  原始数据的抽象, 所有输入的数据都需要转换为SourceEntity, 字段及含义如下:  
  - `id`: long类型, 保留字段, 暂未使用到
  - `key`: String类型, 业务含义的主键, 保留字段, 暂未使用到
  - `source`: String类型, 源名称, 一般对应于一个数据源实例
  - `schema`: String类型, 一般指数据库名称
  - `name`: String类型, 一般指表名称
  - `data`: Map<String, Object>类型, 数据的载体
  - `before`: Map<String, Object>类型, 在canal, ogg场景下当前数据的上一状态
  - `timestamp`: long类型, 数据的发送时间戳
  - `opType`: String类型, 数据的更新类型, 例如UPDATE,DELETE,INSERT
  说明: source, schema, name也可视使用场景不同而赋予其他意义, 默认的描述含义是在关系型数据库的同步的场景下。
- <b>TransferEntity(ink.andromeda.dataflow.core.TransferEntity)</b>  
  数据在数据流节点中转化时的中间结果, 字段含义与SourceEntity类似
### 自定义Bean
todo

### 附录
#### Docker下的Canal搭建
todo