package org.lwt.test;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
/**
 * 	计时器类
 * 	用来即使重发时间，
 * 	如果在指定时间内还没有接收到数据则重发数据
 * 	如果在时间内接收到数据则停止计时器
 * 	@author Administrator
 *
 */
public class CountDown {
	private int limitSec;
	private int curSec;
	private boolean responseFlag;
	public CountDown(int limitSec, boolean responseFlag) throws InterruptedException{
		this.limitSec = limitSec;
		this.curSec = limitSec;
		this.responseFlag = responseFlag;
		//System.out.println("count down from "+limitSec+" s ");
		if(!responseFlag) {																			// 如果在进入计时器之前就没有收到响应则不进计时器，否则不尽兴计时
			Timer timer = new Timer();
			timer.schedule(new TimerTask(){
				public void run(){
					//System.out.println("Time remians "+ --curSec +" s");
					if(responseFlag) {
						return ;
					}
				}
			},0,1000);
			TimeUnit.SECONDS.sleep(limitSec);
			timer.cancel();
		}
	}

}
