package execute;
import java.util.ArrayList;
import java.util.Properties;

import com.liequ.rabbitmq.exception.ConfigException;
import com.liequ.rabbitmq.util.PropertiesManager;

public class JobConfig {
	
	private String brokerName;
	private String queueName;
	private String className;
	private boolean autoAck;
	private int workThreadNum;
	private static int totalWorkThreadNum;

	public static  ArrayList<JobConfig> parseJobConfig() throws ConfigException{
		ArrayList<JobConfig> configs = new ArrayList<JobConfig>(4);
		Properties prop = PropertiesManager.getProperties("conf.properties");
		
		String handlerStr = prop.getProperty("handlers");
		String[] handlers = handlerStr.split(",");
		if (handlers.length == 0) {
			throw new ConfigException("handlers must be setted");
		}
		
		for (String handler : handlers){
			JobConfig confg = new JobConfig();
			
			String bname = prop.getProperty(handler+".brokerName");
			if (bname == null){
				throw new ConfigException(handler+".brokerName"+" must be seted");
			}
			confg.brokerName = bname;
			
			String qname = prop.getProperty(handler+".queueName");
			if (qname == null){
				throw new ConfigException(handler+".queueName"+" must be seted");
			}
			confg.queueName = qname;
			
			String cname = prop.getProperty(handler+".className");
			if (cname == null){
				throw new ConfigException(handler+".className"+" must be seted");
			}
			confg.className = cname;
			
			
			String _autoAck = prop.getProperty(handler+".autoAck");
			if (_autoAck == null){
				throw new ConfigException(handler+".autoAck"+" must be seted");
			}
			confg.autoAck = Boolean.valueOf(_autoAck);
			
			
			
			String _workThreadNum = prop.getProperty(handler+".workThreadNum");
			if (_workThreadNum == null){
				throw new ConfigException(handler+".workThreadNum"+" must be seted");
			}
			confg.workThreadNum = Integer.valueOf(_workThreadNum);
			totalWorkThreadNum += confg.workThreadNum;
			configs.add(confg);
		}
		return configs;
	}


	public int getWorkThreadNum() {
		return workThreadNum;
	}


	public static int getTotalWorkThreadNum() {
		return totalWorkThreadNum;
	}


	public boolean isAutoAck() {
		return autoAck;
	}


	public String getBrokerName() {
		return brokerName;
	}


	public String getQueueName() {
		return queueName;
	}


	public String getClassName() {
		return className;
	}
	
}
