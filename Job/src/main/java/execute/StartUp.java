package execute;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liequ.rabbitmq.exception.ConfigException;


public class StartUp {
	private static Logger log = LoggerFactory.getLogger(StartUp.class);

	public static void main(String[] args) {
		try {
			ArrayList<JobConfig> configs = JobConfig.parseJobConfig();
			for (JobConfig config : configs) {
				TaskManager task = new TaskManager(config);
				new Thread(task).start();
			}
		} catch (ConfigException e) {
			log.error("configError", e);
			e.printStackTrace();
		} catch (Exception e) {
			log.error("error", e);
		}
	}
}
