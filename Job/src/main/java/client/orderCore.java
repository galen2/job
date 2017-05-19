package client;


import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.QueueingConsumer.Delivery;

import execute.ConsumerMessageHandler;
import execute.TaskThread.Provider;

public class orderCore implements ConsumerMessageHandler{
	private static Logger LOG = LoggerFactory.getLogger(orderCore.class);

	public void basicAck(Delivery delivery) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void queueDeclare(Channel channel, String queueName,
			Map<String, Object> queueArguments) throws IOException {
		channel.queueDeclare(queueName, false, false, false, null);
	}

	public void print(String message){
		LOG.info("线程{},消费内容{}", new Object[]{Thread.currentThread().getName(),message});
	}

	@Override
	public boolean consumer(Provider response) throws IOException {
		String content = response.getContent();
		if (content.endsWith("nack")) {
			return false;
		} else {
			print(content);
			return true;
		}
	}
	
	
}
