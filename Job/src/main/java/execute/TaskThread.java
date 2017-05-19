package execute;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liequ.rabbitmq.ConnectionManager;
import com.liequ.rabbitmq.util.Envm;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

@SuppressWarnings("resource")
public class TaskThread extends Thread implements Task {
	private static Logger LOG = LoggerFactory.getLogger(TaskThread.class);
	private String queueName;
	private final String brokerName;
	private final boolean autoAck;
	private ConsumerMessageHandler _handler;
	private volatile boolean stop = false;
	private Channel channel = null;
	private CountDownLatch _latch = null;
	private ConnectionManager connectionManager = null;
	// private AtomicInteger nackNum = new AtomicInteger();
	private volatile boolean nack = false;
	private LinkedBlockingQueue<Provider> exceptionMsgQueue = new LinkedBlockingQueue<Provider>();

	// private final Object lockObject = new Object();
	public TaskThread(ConnectionManager connectionManager, JobConfig _config,
			ConsumerMessageHandler handler, final CountDownLatch latch) {
		this.connectionManager = connectionManager;
		// this.manager = manager;
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
	public void startUp() throws Exception {
		init();
		super.start();
		initErrorDataCollect();
	}

	public void initErrorDataCollect() throws IOException {
		final File file = new File(Envm.ROOT, "errMsg_" + queueName);
		if (!file.exists()) {
			file.createNewFile();
		}

		final BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));

		Thread monitor = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (!stop) {
						Provider ed = exceptionMsgQueue.take();
						bw.append(new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
						bw.write(ed.toString());
						bw.newLine();
						bw.flush();
					}
				} catch (IOException e) {
					LOG.error("Failed to write new file " + file, e);
				} catch (InterruptedException e) {
					LOG.error("InterruptedException ", e);
					e.printStackTrace();
				} finally {
					try {
						bw.close();
					} catch (IOException e) {
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
				if (response == null) {
					idleWork();
				} else {
					Provider provicer = new Provider(queueName, response
							.getEnvelope().getDeliveryTag(),
							response.getBody(), response.getProps());
					if (LOG.isDebugEnabled()) {
						LOG.debug("fetchMessage {} ", provicer.toString());
					}

					boolean success = _handler.consumer(provicer);
					if (!autoAck) {
						if (success) {
							channel.basicAck(response.getEnvelope()
									.getDeliveryTag(), false);
						} else {
							nack = true;
						}
					}
				}
			} catch (IOException io) {
				LOG.error("exception", io);
				destory();
				return;
			} catch (Throwable e) {
				LOG.error("Unexpected exception", e);
			}
		}

		destory();
	}

	private void idleWork() {
		try {
			if (nack) {
				channel.basicRecover();
				nack = false;
			} else {
				Thread.sleep(3000);
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage());
		}
	}

	private void destory() {
		if (_latch != null) {
			_latch.countDown();
		}
		try {
			if (channel != null) {
				channel.close();
			}

		} catch (IOException | TimeoutException e) {
			LOG.error("channel closed error", e);
		}
	}

	public static class Provider {
		private final String _queueName;
		private final AMQP.BasicProperties _properties;
		private final String _content;
		private final long _msgTag;

		public Provider(String queueName, long msgTag, byte[] body,
				AMQP.BasicProperties properties) {
			_queueName = queueName;
			_msgTag = msgTag;
			_content = new String(body);
			_properties = properties;
		}

		public BasicProperties getProperties() {
			return _properties;
		}

		public String getQueueName() {
			return _queueName;
		}

		public String getContent() {
			return _content;
		}

		public long getMsgTag() {
			return _msgTag;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Delivery(queueName=").append(_queueName);
			sb.append(", msgTag=").append(_msgTag);
			sb.append(", content=").append(_content);
			sb.append(")");
			return sb.toString();
		}
	}

	@Override
	public void shutDown() {
		stop = true;
	}

}
