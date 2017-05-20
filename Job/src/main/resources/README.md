# 概述
1、因大家对已有JOB框架熟悉，且此JOB封装尽量保持目前设计风格
2、业务系统需要实现ConsumerMessageHandler接口，里面包含对消息消费及队列声明，具体实现类在conf.properties里面配置
3、需要配置conf.properties任务属性文件和rabbitmq.json文件


conf.properties文件：

#表示此JOB需要处理多少个不同任务，支持一个到多个。时候参数都已此为前缀，表示一个任务
handlers=one,two 
# broderName的值为rabbitmq.json里面brokername，表示队列所在在broker
one.brokerName=brokerNameOne
# 从哪个队列消费消息
one.queueName=simple_queue
# 实现ConsumerMessageHandler接口的业务处理类
one.className=com.order.aolai.biz.orderCore
# 消费者是否只是ACK，这里需要注意，若不是很重要消息，则尽量不要手动ACK
one.autoAck=true
# 工作线程数。不要设置过大，最好为CPU核数的2倍
one.workThreadNum=1