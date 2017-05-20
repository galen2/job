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
	
	
	boolean  consumer(Provider response);
	
}
