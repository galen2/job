package execute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liequ.rabbitmq.ConnectionManager;
import com.liequ.rabbitmq.ConsumerMessageHandler;

public class TaskManager implements Runnable {
	private static Logger LOG = LoggerFactory.getLogger(TaskManager.class);

	private  ConnectionManager connectionManager = null;
	private final String className;
	private int workThreadNum;
	private ConsumerMessageHandler _handler = null;
	private unCaughtExceptionHandler unCaughtExceptionHandler = new unCaughtExceptionHandler();
	private JobConfig _config = null;
	public TaskManager(JobConfig config){
		this.connectionManager = ConnectionManager.getInstance();
		this._config =  config;
		this.workThreadNum = _config.getWorkThreadNum();
		this.className = _config.getClassName();
	}
	
	private void startTask() throws Exception{
		for (int i = 0; i < workThreadNum; i++) {
			startNewWorkThrad();
		}
	}
	
	public void startNewWorkThrad() throws Exception{
		TaskThread thread = new TaskThread(connectionManager, _config,_handler,this);
		Thread.setDefaultUncaughtExceptionHandler(unCaughtExceptionHandler);
		thread.startWork();
	}

	public void run() { 
		try {
			initMessageHandler();
			startTask();
		} catch (Exception e) {
			LOG.error("error", e);
		}
	}

	public  void initMessageHandler() {
		try {
			_handler = (ConsumerMessageHandler) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			LOG.error("ERROR",e);
		} catch (IllegalAccessException e) {
			LOG.error("ERROR",e);
		} catch (ClassNotFoundException e) {
			LOG.error("ERROR",e);
		}
	}
}
