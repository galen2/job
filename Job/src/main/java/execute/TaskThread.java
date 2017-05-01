package execute;

import java.io.IOException;

import com.liequ.rabbitmq.QueueMessageHandler;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

public class TaskThread implements Runnable{

	private  QueueingConsumer _consumer = null;
	private QueueMessageHandler _handler = null;
//	private JobConfig config;
	private boolean autoAck ; 
	private String queueName ; 
	public TaskThread(QueueingConsumer consumer, QueueMessageHandler handler,boolean autoAck,String queueName ){
		this._consumer = consumer;
		this._handler = handler;
		this.autoAck = autoAck;
		this.queueName = queueName;
	}

	public void start() throws IOException{
		Channel channel = _consumer.getChannel();
		channel.basicConsume(queueName, autoAck,_consumer);
	}
	public void run() {
		while (true) {
			try {
				Delivery delivery =  _consumer.nextDelivery();
				_handler.consumer(delivery);
				if (!autoAck){
					_handler.basicAck(delivery);
				}
			} catch (ShutdownSignalException e) {
				e.printStackTrace();
			} catch (ConsumerCancelledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
