package execute;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liequ.rabbitmq.exception.ConfigException;

public class StartUp {
	private static Logger LOG = LoggerFactory.getLogger(StartUp.class);
	private static ArrayList<Task> taskList = new ArrayList<Task>(8);

	public  void start(String[] args) {
		CountDownLatch latch = null;
		try {
			LOG.info("starting begined");
			addShutDonwHook();
			ArrayList<JobConfig> configs = JobConfig.parseJobConfig();
			latch = new CountDownLatch(JobConfig.getTotalWorkThreadNum());
			for (JobConfig config : configs) {
				TaskManager task = new TaskManager(config,latch);
				task.startUp();
				taskList.add(task);
			}
		} catch (ConfigException e) {
			latch = null;
			LOG.error("Invalid config, exiting abnormally", e);
			System.err.println("Invalid config, exiting abnormally");
            System.exit(1);
		} catch (StartException e){
			latch = null;
			LOG.error("startException, exiting abnormally", e);
			System.err.println("startException, exiting abnormally");
			System.exit(1);
		} catch (Throwable e) {
			latch = null;
			LOG.error("Unexpected exception, exiting abnormally", e);
			System.err.println("Unexpected exception, exiting abnormally");
            System.exit(1);
		}
		
		LOG.info("starting successfully");
		if (latch !=null ) {
			try {
				latch.await();
			} catch (InterruptedException e) {
			}
		}
		LOG.info("Exiting normally");
		System.exit(0);
	}
	
	public static void addShutDonwHook(){
		Runtime.getRuntime().addShutdownHook(new Thread() {  
            @Override  
            public void run() {  
            	if (taskList.size() > 0) {
            		LOG.info("shutDown workThread ......");
            		for (Task task :taskList) {
            			task.shutDown();
            		}
            	}
            }  
        });  
	}
}
