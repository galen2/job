package client;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liequ.rabbitmq.ConsumerMessageHandler;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.QueueingConsumer.Delivery;

import execute.TaskManager;

public class orderCore implements ConsumerMessageHandler{
	private static Logger LOG = LoggerFactory.getLogger(orderCore.class);


	/*public void consumer(Delivery delivery) throws InterruptedException, ShutdownSignalException, ConsumerCancelledException {
		String content = new String(delivery.getBody());
		if (content.equals("erroTest")) {
			throw new InterruptedException();
		}
		print(new String(delivery.getBody()));
//		System.out.println("consumerMessage:"+new String(delivery.getBody()));
		
	}*/

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
	public boolean consumer(GetResponse response) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}
	
}
