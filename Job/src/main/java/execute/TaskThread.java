package execute;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liequ.rabbitmq.ConnectionManager;
import com.liequ.rabbitmq.ConsumerMessageHandler;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

public class TaskThread extends Thread implements Task {
	private static Logger LOG = LoggerFactory.getLogger(TaskThread.class);
	private String queueName;
	private final String brokerName;
	private final boolean autoAck;
	private ConsumerMessageHandler _handler;
	private volatile boolean stop = false;
	private Channel channel = null;
	private  CountDownLatch _latch = null;
	private  ConnectionManager connectionManager = null;
	private AtomicInteger nackNum = new AtomicInteger();
	private final Object lockObject = new Object();
	public TaskThread(ConnectionManager connectionManager,
			JobConfig _config, ConsumerMessageHandler handler,final CountDownLatch latch) {
		this.connectionManager = connectionManager;
//		this.manager = manager;
		this.brokerName = _config.getBrokerName();
		this.queueName = _config.getQueueName();
		this.autoAck = _config.isAutoAck();
		this._handler = handler;
		this._latch = latch;
	}
	
	public void init() throws Exception {
		channel = connectionManager.getChannel(brokerName);
		channel.basicQos(1);
		Map<String, Object> queueArguments = new HashMap<String, Object>();
		_handler.queueDeclare(channel, queueName, queueArguments);
	}

	@Override
	public  void startUp () throws Exception {
		init();
		super.start();
		iniyMessageRecover();
	}
	
	public void iniyMessageRecover(){
		Thread monitor = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					lockObject.wait();
				} catch (InterruptedException e) {
					LOG.warn(e.getMessage());
				}
				
				if (nackNum.get() > 0) {
					try {
						channel.basicRecover();
					} catch (IOException e) {
						LOG.warn(e.getMessage());
					}
				}
			}
		});
		monitor.setDaemon(true);
		monitor.start();
	}
	
	public void run() {
		while (!stop) {
			try {
				GetResponse response = channel.basicGet(queueName, autoAck);
				boolean result = _handler.consumer(response);
				if (!autoAck) { 
					if (result) {
						channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
					} else {
						nackNum.incrementAndGet();
						lockObject.notify();
					}
				}
			} catch (Exception e) {
				LOG.error(e.getMessage());
			} 
		}
		
		if (_latch !=null) {
			_latch.countDown();
		}
		try {
			channel.close();
		} catch (IOException |TimeoutException e) {
			LOG.error("channel closed error", e);
		} 
	}

	@Override
	public void shutDown() {
		stop = true;
	}
	
}
