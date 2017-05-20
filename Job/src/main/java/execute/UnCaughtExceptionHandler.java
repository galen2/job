package execute;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnCaughtExceptionHandler implements UncaughtExceptionHandler{
	
	private static Logger LOG = LoggerFactory.getLogger(UnCaughtExceptionHandler.class);
	
	public void uncaughtException(Thread t, Throwable e) {
		LOG.error("未捕获异常信息:",e);
	}

}
