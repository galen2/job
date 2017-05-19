package client;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Clinent {
	private static Logger LOG = LoggerFactory.getLogger(Clinent.class);

	public static void main(String[] args) throws IOException {
		String ss = DateFormat.getInstance().format(new Date());
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
		System.out.println(ss);
		//		File file2 = new File(Envm.ROOT.concat("db"));
		/*final LinkedList<String> list = new LinkedList<String>();
		list.add("12");
		LinkedBlockingQueue queue = new LinkedBlockingQueue();
		queue.add(123);
		queue.add(3);
		try {
			queue.take();
		} catch (InterruptedException e) {
		}*/
		/*for (int i = 0 ; i < 3 ; i ++){
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					System.out.println("3333");
					System.out.println("3333");
					System.out.println(list.size());
					list.add("333");
					
				}
			});
			thread.start();
		}*/
	}
}
