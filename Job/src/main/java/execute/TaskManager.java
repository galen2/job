package execute;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskManager implements Task {
	private static Logger LOG = LoggerFactory.getLogger(TaskManager.class);
	private ConsumerMessageHandler _handler = null;
	private UnCaughtExceptionHandler unCaughtExceptionHandler = new UnCaughtExceptionHandler();
	private JobConfig _config = null;
	private CountDownLatch latch = null;
	private ArrayList<Task> taskList = new ArrayList<Task>();
	private ErrorDataCollection edc = null;

	public TaskManager(JobConfig config, CountDownLatch latch){
		this._config =  config;
		this.latch = latch;
	}
	
	private void startTask() throws Exception{
		for (int i = 0; i < _config.getWorkThreadNum(); i++) {
			create();
		}
	}
	
	public void create() throws Exception{
		TaskThread thread = new TaskThread(_config,_handler,latch,edc);
		Thread.setDefaultUncaughtExceptionHandler(unCaughtExceptionHandler);
		thread.startUp();
		taskList.add(thread);
	}

	private void initErroDataCollect() throws Exception{
		edc = new ErrorDataCollection(_config);
		edc.startUp();
	}
	@Override
	public void startUp() throws Exception {
		try {
			initErroDataCollect();
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
			_handler = (ConsumerMessageHandler) Class.forName(_config.getClassName()).newInstance();
		} catch (InstantiationException |IllegalAccessException |ClassNotFoundException e) {
			throw e;
		}
	}
}
