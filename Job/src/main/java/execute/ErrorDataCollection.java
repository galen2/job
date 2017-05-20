package execute;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liequ.rabbitmq.util.Envm;

import execute.TaskThread.Provider;

public class ErrorDataCollection extends Thread implements Task {
	private static Logger LOG = LoggerFactory.getLogger(ErrorDataCollection.class);
	private volatile boolean stop = false;
	private File file = null;
	private BufferedWriter bw = null;
	private LinkedBlockingQueue<Provider> exceptionMsgQueue = new LinkedBlockingQueue<Provider>();
	private JobConfig _config = null;

	public ErrorDataCollection(JobConfig config) {
		this._config = config;
	}

	@Override
	public void run() {
			try {
				while (!stop) {
					Provider ed = exceptionMsgQueue.take();
					String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
					String content = ed.toString();
					bw.write(date+"\t"+content+"\n");
					bw.flush();
				}
			} catch (IOException e) {
				LOG.error("Failed to write new file " + file, e);
			} catch (InterruptedException e) {
				LOG.error("InterruptedException ", e);
			} finally {
				try {
					bw.close();
				} catch (IOException e) {
				}
			}
	}

	
	@Override
	public void startUp() throws Exception {
		file = new File(Envm.ROOT, "errMsg_" + _config.getQueueName());
		if (!file.exists()) {
			file.createNewFile();
		}
		bw = new BufferedWriter(new FileWriter(file, true));
		super.start();
	}
	
	
	@Override
	public void shutDown() {
		stop = true;
	}

	public void put(Provider p) {
		exceptionMsgQueue.add(p);
	}

}
