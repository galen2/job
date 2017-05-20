package execute;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liequ.rabbitmq.ConnectionManager;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
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
	private CountDownLatch _latch = null;
	private volatile boolean nack = false;
	private final ErrorDataCollection _errDataColl;

	public TaskThread(JobConfig _config, ConsumerMessageHandler handler, final CountDownLatch latch,
			ErrorDataCollection errDataColl) {
		this.brokerName = _config.getBrokerName();
		this.queueName = _config.getQueueName();
		this.autoAck = _config.isAutoAck();
		this._handler = handler;
		this._latch = latch;
		this._errDataColl = errDataColl;
	}

	public void createChanel() throws Exception {
		if (channel != null) 
			channel.close();
		channel = ConnectionManager.getInstance().getChannel(brokerName);
		channel.basicQos(1);
		Map<String, Object> queueArguments = new HashMap<String, Object>();
		_handler.queueDeclare(channel, queueName, queueArguments);
	}

	@Override
	public void startUp() throws Exception {
		createChanel();
		super.start();
	}

	public void run() {

		while (!stop) {
			GetResponse response = null;
			
			try {
				response = channel.basicGet(queueName, autoAck);
			} catch (IOException e) {
				handlerIOException(e);
				continue;
			}

			if (response == null) {
				idleWork();
			} else {
				Provider provicer = new Provider(queueName, response.getEnvelope().getDeliveryTag(), response.getBody(),
						response.getProps());
				if (LOG.isDebugEnabled()) {
					LOG.debug("fetchMessage {} ", provicer.toString());
				}
				
				boolean success = true;
				try {
					 success = _handler.consumer(provicer);
				} catch (Throwable e) {
					// 业务异常如空指针等，记录到文件，自动ACK，保证下面的消息可被消费
					LOG.error("bussiness exception", e);
					collectErrorData(provicer);
				} finally{
					if (!autoAck) {
						basicAck(success, provicer);
					}
				}
			}
		}

		destory();
	}

	private void basicAck(boolean success, Provider provicer) {
		if (success) {
			try {
				channel.basicAck(provicer.getMsgTag(), false);
			} catch (IOException e) {
				collectErrorData(provicer);
				handlerIOException(e);
			}
		} else {
			nack = true;
		}
	}

	private void handlerIOException(IOException e) {
		try {
			createChanel();
		} catch (Exception e1) {
			shutDown();
		}
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

	private void collectErrorData(Provider provicer) {
		if (provicer != null) {
			_errDataColl.put(provicer);
		}
	}

	
	public static class Provider {
		private final String _queueName;
		private final AMQP.BasicProperties _properties;
		private final String _content;
		private final long _msgTag;

		public Provider(String queueName, long msgTag, byte[] body, AMQP.BasicProperties properties) {
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
			sb.append("Provider(queueName=").append(_queueName);
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
