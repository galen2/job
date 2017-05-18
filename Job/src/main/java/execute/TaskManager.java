package execute;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liequ.rabbitmq.ConnectionManager;

public class TaskManager implements Task {
	private static Logger LOG = LoggerFactory.getLogger(TaskManager.class);
	private  ConnectionManager connectionManager = null;
	private final String className;
	private int workThreadNum;
	private ConsumerMessageHandler _handler = null;
	private UnCaughtExceptionHandler unCaughtExceptionHandler = new UnCaughtExceptionHandler();
	private JobConfig _config = null;
	private CountDownLatch latch = null;
	private ArrayList<Task> taskList = new ArrayList<Task>();
	public TaskManager(JobConfig config, CountDownLatch latch){
		this.connectionManager = ConnectionManager.getInstance();
		this._config =  config;
		this.workThreadNum = _config.getWorkThreadNum();
		this.className = _config.getClassName();
		this.latch = latch;
	}
	
	private void startTask() throws Exception{
		for (int i = 0; i < workThreadNum; i++) {
			create();
		}
	}
	
	public void create() throws Exception{
		TaskThread thread = new TaskThread(connectionManager, _config,_handler,latch);
		Thread.setDefaultUncaughtExceptionHandler(unCaughtExceptionHandler);
		thread.startUp();
		taskList.add(thread);
	}

	@Override
	public void startUp() throws Exception {
		try {
			initMessageHandler();
			startTask();
		} catch (Exception e) {
			throw new StartException("initialize error", e);
		}
	}
	@Override
	public void shutDown() {
		for (Task task : taskList ) {
			task.shutDown();
		}
	}
	
	public  void initMessageHandler() throws ReflectiveOperationException {
		try {
			_handler = (ConsumerMessageHandler) Class.forName(className).newInstance();
		} catch (InstantiationException |IllegalAccessException |ClassNotFoundException e) {
			LOG.error("ERROR",e);
			throw e;
		}
	}
}
