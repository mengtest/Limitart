# 简介
一个旨在帮助快速搭建Java中小型游戏服务器的框架，通信底层采用Netty4.X，数据库采用MySql相关(框架其实并未与Mysql产生太多耦合，但建议使用Mysql)，生产消费者采用Disruptor等。此框架的作用在于共同学习，少部分经过商业验证，稳定性有一定风险，请酌情考虑。
# 模块介绍
## 网络通信(net)
### 二进制服务器的链接过程
客户端与服务器建立Socket链接，服务器发送加密验证码到客户端，客户端解密后发送结果到服务器，服务器验证完毕，Socket才稳定下来，如果客户端长时间不发送正确结果，则踢掉链接。这个措施主要防止无效链接过多，增加恶意攻击服务器的成本。
### 二进制消息（Message、MessageMeta）
每个消息必须继承Message类，并且消息只支持基本类型和消息元(MessageMeta)类型以及前两者类型的List或数组，消息元类型就是所谓的消息里携带对象，List的长度不能超过一个short的长度。每个消息必须有一个唯一Id来标识，长度为一个short。消息里面的decode和encode编码必须顺序一致。二进制服务器构造的时候需要一个消息工厂(MessageFactory)，里面需要初始化消息到消息处理器(IHandler)的映射，当二进制服务器收到消息时，会去寻找相应的消息处理器作为回调，从而执行逻辑。建议在实际项目中，消息按模块分包，开发时先思考清楚需要开发的功能会用到什么消息，然后分别处理好相应的handler即可。比如：一个背包系统，对于客户端来讲基本是3个元操作，增加物品、删除物品、更新物品信息，那么定好相应的消息，客户端就只用关心这3个操作如何处理就行，其余的功能根据这3个元操作组合即可得到。
### 发送二进制消息（SendMessageUtil）
我们保留了Netty的远程Channel来作为服务器与每个用户的通道，发送消息很简单，调用SendMessageUtil里的sendMessage方法即可，需要注意的是，为了防止重复编码，在向多个Channel发送消息时，请尽量调用有List参数的重载函数。
### 二进制消息处理器（IHandler）
每个处理器必须要实现IHandler接口，然后注册进消息工厂(MessageFactory)与相应的消息(Message)所对应，IHandler被认为是单例模式，所以不要在IHandler的实现类里缓存任何非全局的数据。
### 反射构造消息工厂(MessageFactory)
在实际开发中，我们都知道按模块来区分代码是非常有必要的事，各自模块的消息注册到各自模块的消息池(IMessagePool)是有必要的，这样可以避免所有消息混在一起，修改和查看显得非常麻烦，通过调用消息工厂(MessageFactory)的createByPackage方法可以使指定包下所有的消息池(IMessagePool)组合成一个消息工厂(MessageFactory);
### 二进制服务器配置(BinaryServer)
BinaryServerConfig是构造二进制服务器(BinaryServer)必要的配置选项，主要用于确定端口、数据包限制以及链接验证码公钥(见上文链接过程中提到的验证码)。在二进制服务器(BinaryServer)中会有各种事件的回调函数，包括Netty原生回调和自定义回调：1.onServerBind 服务器绑定端口成功 2.onConnectionEffective 客户端链接验证成功，一个链接是否有效，或者说要判断此链接是否可以正式交由本服务器处理，都建议从这里开始 3.dispatchMessage 这里就是分发消息的回调了，因为不同的应用线程模型不同，所以我们需要把消息放到不同线程来执行，并且可能会对消息有统计行为，比如arpg游戏的大概模型就会按地图来分线程，那么这里就很可能会去寻找相应的地图线程来执行消息。
### 二进制客户端配置(BinaryClient)
BinaryClient的接口跟BinaryServer类似，通过构造函数传相应参数即可
## RPC服务(rpcx)
## 脚本(script)
## 消息队列(taskqueue)
## 消息队列组(taskqueuegroup)
## 数据库相关(db)
## 游戏常用集合类(collections)
## 游戏常用功能抽象(game)
## 常用工具(util)