package client;

import java.util.Map;

import com.liequ.rabbitmq.ConsumerMessageHandler;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer.Delivery;

public class orderBase implements ConsumerMessageHandler{


	public void consumer(Delivery delivery) {
		// TODO Auto-generated method stub
		
	}

	public void basicAck(Delivery delivery) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void queueDeclare(Channel channel, String queueName,
			Map<String, Object> queueArguments) {
		// TODO Auto-generated method stub
		
	}

	
}
