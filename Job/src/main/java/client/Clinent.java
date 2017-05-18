package client;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Clinent {
	private static Logger LOG = LoggerFactory.getLogger(Clinent.class);

	public static void main(String[] args) {
		final LinkedList<String> list = new LinkedList<String>();
		list.add("12");
		for (int i = 0 ; i < 3 ; i ++){
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
		}
		
		/*try {
			throw new NoClassDefFoundError("");
		} catch (Throwable e) {
			System.out.println("eee");
			// TODO: handle exception
		} 
		catch (Exception e) {
			System.out.println("eee");
			// TODO: handle exception
		} 
		LOG.info("333");*/
	}
}
