package execute;

import java.io.IOException;
import java.util.Map;

import com.rabbitmq.client.Channel;

import execute.TaskThread.Provider;

public interface ConsumerMessageHandler {
	
	/**
	 * e.g:
	 * channel.queueDeclare(queueName, queueDurable, exclusive, autoDelete, queueArguments);
	 * @param channel
	 * @param queueName
	 * @param queueArguments
	 */
	void queueDeclare(Channel channel, String queueName, Map<String, Object> queueArguments) throws IOException;
	
	/**
	 * 消费来自MQ推送消息，可自定义对消息消费及管理做处理
	 * eg：
	 * .手动创新新的线程并发消费
	 * .对实例失败的消息可手动插入数据库或者写本地文件等等,以便后续做补偿处理
	 * @param delivery
	 */
	boolean  consumer(Provider response) throws IOException;
	
//	void basicAck(GetResponse response)throws IOException;
}
