package execute;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liequ.rabbitmq.exception.ConfigException;


public class StartUp {
	private static Logger LOG = LoggerFactory.getLogger(StartUp.class);

	public static void main(String[] args) {
		try {
			ArrayList<JobConfig> configs = JobConfig.parseJobConfig();
			for (JobConfig config : configs) {
				TaskManager task = new TaskManager(config);
				new Thread(task).start();
			}
		} catch (ConfigException e) {
			LOG.error("Invalid config, exiting abnormally", e);
            System.err.println("Invalid config, exiting abnormally");
            System.exit(2);
		} catch (Exception e) {
			LOG.error("Unexpected exception, exiting abnormally", e);
            System.exit(1);
		}
//		LOG.info("Exiting normally");
//	    System.exit(0);
	}
}
