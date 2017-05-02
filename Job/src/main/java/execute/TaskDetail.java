package execute;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import j.u.Log;

import com.liequ.rabbitmq.QueueMessageHandler;
import com.liequ.rabbitmq.pool.ConnectionManager;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class TaskDetail implements Runnable {
	private static Logger log = LoggerFactory.getLogger(TaskDetail.class);

	private  ConnectionManager connectionManager = null;

	private final String brokerName;
	private final String queueName;
	private final String className;
	private boolean queueDurable;
	private boolean autoAck;
	private boolean exclusive;
	private boolean autoDelete;
	private int workThreadNum;
	private QueueMessageHandler handler;
//	private Channel channel;
	private HashMap<String, Object> queueArguments;
	
	public TaskDetail(JobConfig _config){
		this.connectionManager = ConnectionManager.getInstance();
		this.workThreadNum = _config.getWorkThreadNum();
		this.brokerName = _config.getBrokerName();
		this.queueName = _config.getQueueName();
		this.exclusive = _config.isExclusive();
		this.autoAck = _config.isAutoAck();
		this.className = _config.getClassName();
		this.autoDelete = _config.isAutoDelete();
		this.queueArguments = _config.getQueueArguments();
	}
	
	
	
	private void startTaskNoAck() throws Exception{
		for (int i = 0; i < workThreadNum; i++) {
			Channel channel = createChannel();
			QueueingConsumer consumer = new QueueingConsumer(channel);
			TaskThread thread = new TaskThread(consumer, handler, autoAck, queueName);
			new Thread(thread).start();
		}
	}

	private void startTaskAutoAck() throws Exception{
		Channel channel = createChannel();
		QueueingConsumer consumer = new QueueingConsumer(channel);
		for (int i = 0; i < workThreadNum; i++) {
			TaskThread thread = new TaskThread(consumer, handler, autoAck, queueName);
			new Thread(thread).start();
		}
	}
	
	private Channel createChannel() throws Exception{
		Channel channel = connectionManager.getChannel(brokerName);
		channel.basicQos(1);
		handler.extendChannel(channel);
		channel.queueDeclare(queueName, queueDurable, exclusive, autoDelete, queueArguments);
		return channel;
	}
	
	public void run() { 
		try {
			initMessageHandler();
			if (autoAck) {
				startTaskAutoAck();
			} else {
				startTaskNoAck();
			}
		} catch (Exception e) {
			log.error("error", e);
		}
	}

	public  void initMessageHandler() {
		try {
			handler = (QueueMessageHandler) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			Log.severe(e);
		} catch (IllegalAccessException e) {
			Log.severe(e);
		} catch (ClassNotFoundException e) {
			Log.severe(e);
		}
	}
}
