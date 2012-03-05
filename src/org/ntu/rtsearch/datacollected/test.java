package org.ntu.rtsearch.datacollected;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class test {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
//		try {
//			BufferedReader br = new BufferedReader(new FileReader("/home/image/user_info"));
//			String line;
//			System.out.println(br.readLine());
//			
//			if(br.readLine() == null)
//				System.out.println("null");
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		int b = 1;
		float a = b;
		System.out.println(a / 2);
		
		try {
			new test().testA();
		
		} finally {
			System.out.println("end");
		}
	}
	
	public void testA() throws InterruptedException {
		System.out.println("fs");
		TimeUnit.SECONDS.sleep(10);
	}
	
	public final void finalize(){
		System.out.println("ss");
	}

}
