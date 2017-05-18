package execute;

import java.lang.Thread.UncaughtExceptionHandler;

public class UnCaughtExceptionHandler implements UncaughtExceptionHandler{
	public void uncaughtException(Thread t, Throwable e) {
		System.out.println("未捕获异常信息:"+e.getMessage());
	}


}
