package client;

import java.io.IOException;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import execute.ConsumerMessageHandler;
import execute.TaskThread.Provider;

public class orderBase implements ConsumerMessageHandler{

	@Override
	public void queueDeclare(Channel channel, String queueName,
			Map<String, Object> queueArguments) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean consumer(Provider response) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}



	
}
