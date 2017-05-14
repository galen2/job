package execute;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liequ.rabbitmq.ConnectionManager;
import com.liequ.rabbitmq.ConsumerMessageHandler;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

public class TaskThread extends Thread {
	private static Logger log = LoggerFactory.getLogger(TaskThread.class);
	private QueueingConsumer _consumer = null;
	private String queueName;
	private TaskManager manager = null;
	private final String brokerName;
	private boolean autoAck;
	private ConsumerMessageHandler _handler;

	private  ConnectionManager connectionManager = null;
	public TaskThread(ConnectionManager connectionManager,
			JobConfig _config, ConsumerMessageHandler handler,TaskManager manager) {
		this.manager = manager;
		this.brokerName = _config.getBrokerName();
		this.queueName = _config.getQueueName();
		this.autoAck = _config.isAutoAck();
		this._handler = handler;
	}

	
	public void consumer() throws Exception {
		Channel channel = connectionManager.getChannel(brokerName);
		channel.basicQos(1);
		Map<String, Object> queueArguments = new HashMap<String, Object>();
		_handler.queueDeclare(channel, queueName, queueArguments);
		channel.basicConsume(queueName, autoAck, _consumer);
	}

	public  void startWork () throws Exception{
		consumer();
		super.start();
	}
	public void run() {
		try {
			while (true) {
				Delivery delivery = _consumer.nextDelivery();
				_handler.consumer(delivery);
				if (!autoAck) {
					_handler.basicAck(delivery);
				}
			}
		} catch (ShutdownSignalException 
					|ConsumerCancelledException
						|InterruptedException e) {
			log.error(e.getMessage());
		} finally {
			try {
				_consumer.getChannel().close();
				try {
					manager.startNewWorkThrad();
				} catch (Exception e) {
					log.error("open a new task fail When the thread exits", e);
				}
			} catch (IOException | TimeoutException e) {
				log.error(e.getMessage());
			} 
		}
	}
}
