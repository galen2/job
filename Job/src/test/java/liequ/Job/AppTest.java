package liequ.Job;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
	public static void run(){
		System.out.println("run begin");

		two();
		System.out.println("run end");

	}
	public static void two(){
		System.out.println("two begin");
		three();
		System.out.println("two end");
	}
	public static void three(){
		System.exit(3);
		System.out.println("three");
	}
	
	public static void main(String[] args) {
		run();
	}
}
