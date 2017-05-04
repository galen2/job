package execute;

import j.u.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liequ.rabbitmq.QueueMessageHandler;
import com.liequ.rabbitmq.pool.ConnectionManager;

public class TaskManager implements Runnable {
	private static Logger log = LoggerFactory.getLogger(TaskManager.class);

	private  ConnectionManager connectionManager = null;
	private final String className;
	private int workThreadNum;
	private QueueMessageHandler _handler = null;
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
			log.error("error", e);
		}
	}

	public  void initMessageHandler() {
		try {
			_handler = (QueueMessageHandler) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			Log.severe(e);
		} catch (IllegalAccessException e) {
			Log.severe(e);
		} catch (ClassNotFoundException e) {
			Log.severe(e);
		}
	}
}
