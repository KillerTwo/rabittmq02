package org.lwt.test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;



public class TestClazz {
	/**
	 *  测试类
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		/*Map<Integer, Object> map = new TreeMap<>();
		String path = TestClazz.class.getResource("100.txt").getPath();
		path = path.substring(1, path.length());
		System.out.println(path);
		File file = new File(path);
		if(file.exists() && file.isFile()) {
			if(file.delete()) {
				System.out.println("删除文件成功...");
			}
			
		}*/
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			Integer count = 1;
			Calendar cal = Calendar.getInstance();
			boolean flag = true;
			@Override
			public void run() {
				if(!flag) {
					this.cancel();
					System.gc();
					return ;
				}
				count--;
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
				System.out.println("当前时间为："+format.format(new Date()));
				flag = false;
				
			}
		}, new Date(), 3000);
		System.out.println("timer开启一个新的线程执行任务");
		/*map.put(0, "data0");
		map.put(3, "data3");
		map.put(1, "data1");
		map.put(10, "data10");
		
		map.put(2, "data2");
		map.put(9, "data9");
		map.put(4, "data4");
		map.put(6, "data6");
		
		map.put(5, "data5");
		map.put(7, "data7");
		
		for(Map.Entry<Integer, Object> entry: map.entrySet()) {
			System.out.println(entry.getKey() + "=="+ entry.getValue());
			//FileUtils.write2File(file, entry.getValue());
		}*/
		new CountDown(10,false);
	}
	
	 

	
}


