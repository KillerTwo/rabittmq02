package org.lwt.producer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

import javax.management.Query;

import org.lwt.exception.TimeOutException;
import org.lwt.tools.FileUtils;
import org.lwt.tools.TestTools;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.google.gson.Gson;

/**
 * 生产者类
 * @author lwt27
 *
 */

public class Producer2 {
	//private final static String QUEUE_NAME = "hello_queue";
	public static void main(String[] args) throws Exception {
		/*String ip = "192.168.1.3";
		int port = 5672;
		String username = "alice";
		String password = "123456";
		String vhost = "vhost_01";*/
		String ip = "10.10.10.14";
		int port = 5672;
		String username = "yduser";
		String password = "yd@user";
		String vhost = "ydkpbmp";
		
		// 建立到服务器的链接
		Connection connection = getConnection(ip, port, username, password, vhost);
		//获得信道
		Channel channel = connection.createChannel();
		// 设置回调队列
		/********************************************************/
		String callbackQueueName = channel.queueDeclare().getQueue();
		System.out.println(callbackQueueName);
		QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(callbackQueueName, true, consumer);
		/********************************************************/
		
		//声明交换器
		String exchangeName = "myexchanges02";
		channel.exchangeDeclare(exchangeName, "direct", true);
		//声明routing-key
		String routingKey = "myroutingkey02";
		//发布消息
		
		// 上传一个文件
		String path = Producer2.class.getClassLoader().getResource("").getPath();
		path = path.substring(1, path.length());
		File file = new File(path+"text.txt");
		
		/* 用来维护一个已经发送数据包的唯一id的队列，如果对于的包id收到了接收端的确认消息，
		 * 则将该确认消息对于的包id从队列中中删除，如果等待3秒中后还未收到对应的确认消息，则
		 * 重新发一次该id对应的数据包
		 * */
		final Queue<Integer> sendQueue = new LinkedList<>();
		
		
		
		List<byte[]> byteList = FileUtils.splitDemo(file);	//将文件拆分（每份为1024字节）
		System.out.println("包的数量== "+byteList.size());
		toSend(byteList, file, channel, exchangeName, routingKey, sendQueue, callbackQueueName, consumer);	// 发送数据
		
		
		
		
		
		
		
		
		
		
		
	}
	/**
	 * 发送数据到rabbitMQ
	 * @param byteList	文件的包字节数组
	 * @param file		文件file
	 * @param channel	RabbitMQ信道
	 * @param exchangeName	RabbitMQ 交换器名
	 * @param routingKey	RabbitMQ 路由键
	 */
	public static void toSend(List<byte[]> byteList, File file,
			Channel channel, String exchangeName, 
			String routingKey, final Queue<Integer> sendQueue, String callbackQueueName,
			QueueingConsumer consumer){
		
		String fileMD5 = TestTools.getFileMD5(file);	//获取待上传文件的MD5
		
		String fileName = file.getName();
		for(int i = 0; i < byteList.size(); i++) {
			sendQueue.offer(i);
			/**
			 * map 用来存放已经发送的ID和对应的发送时间，即{"packnum" = packnum,sendTime=date}
			 * 如果5秒后没有收到对应该包的响应，怎将该报重新发一遍
			 * 
			 * 在发送的时候对应的数据存入map之中
			 */
			
			// map.put("packnum",i);
			String data = getFilePack(byteList.get(i), fileMD5, byteList.size(), i, fileName);
			// 分开发送每一部分的数据
			try {
				System.out.println("发送数据...");
				//map.put("sendTime", System.currentTimeMillis());
				call(data,callbackQueueName,channel,consumer,exchangeName,routingKey);		// 发送数据之后接收一个响应
				
			} catch (TimeOutException e) {
				System.out.println("接收响应超时了，要在此处重发...");
				//call(data,callbackQueueName,channel,consumer,exchangeName,routingKey);
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 *  发送数据并接收响应
	 * 
	 * @param message	发送的数据
	 * @param replyQueueName	回调队列
	 * @param channel		信道
	 * @param consumer		QueueingConsumer consumer = new QueueingConsumer(channel);
	 * @return				响应信息
	 * @throws Exception	
	 */
	public static void call(String message, String replyQueueName, Channel channel, QueueingConsumer consumer,
			String exchangeName, String routingkey) throws Exception {     
        String response = null;
        
        
        //封装correlationId和replyQueue属性
        BasicProperties props = new BasicProperties
                                    .Builder()
                                    .replyTo(replyQueueName)
                                    .build();
        //发送消息，并加上之前封装好的属性replyTo=响应（回调）队列
        channel.basicPublish(exchangeName, routingkey, props, message.getBytes("utf-8"));
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        long timeout = 0;
        while ((endTime - startTime) < 1000) {
        	System.out.println("等待时间是="+(endTime - startTime));
        	System.out.println("接收响应循环...");
	        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	        if(delivery.getBody() != null) {
	        	// 如果在规定的时间内接收到响应
	        	
	        	response = new String(delivery.getBody(),"UTF-8");
		        System.out.println("[《sender》接收到的响应内容为：]"+response);
		        break;
	        }else {
	        	// 如果超过1秒没收到响应，则抛出异常，重发没有收到响应的包
	        	if(timeout > 1000) {
	        		System.out.println("接收响应超时");
	        		throw new TimeOutException("超时没有收到响应");
	        	}else {
	        		System.out.println("继续读取响应时间");
	        		// 读取当前时间
	        		endTime = System.currentTimeMillis();
	        		//计算当前时间和初始时间之间的间隔
	        		timeout = endTime - startTime;
	        	}
	        }
	        System.out.println("timeout is "+timeout);
        }
        
       
      }
	
	
	/**
	 * 发送单条数据
	 * 
	 * @param channel	Channel对象
	 * @param exchangeName	exchange名称
	 * @param routingKey	路由键
	 * @param bytes			发送的数据的字节数组
	 * @param fileMD5		整个文件的md5值
	 * @param serial		当前包的序号
	 * @param count			包总数
	 */
	public static void sendSigle(Channel channel, String exchangeName, 
			String routingKey, byte[] bytes, 
			String fileMD5, int serial, int count, String fileName) {
		String data = getFilePack(bytes, fileMD5, count, serial, fileName);
		try {
			channel.basicPublish(exchangeName, routingKey, null, data.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 获取包的json字符串
	 * @param bytes		每个分段的字节内容
	 * @param fileMD5	整个文件的MD5值
	 * @param count		整个文件的包数
	 * @param pkSerial	包序号（当前是第几个包）
	 * @return	String	包的json字符串
	 */
	public static String getFilePack(byte[] bytes, String fileMD5, double count, int pkSerial, String fileName){
		Map<String, Object> map = new HashMap<>();
		String md5 = null;
		try {
			md5 = TestTools.getMD5String(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] fileNames = fileName.split("\\.");
		map.put("fileName", fileNames[0]);
		map.put("date", System.currentTimeMillis()+1);
		map.put("packid", fileMD5);	// 用文件的md5值作为包的id值
		map.put("packcount", count);	//本次上传的整个包被分成多少个小包
		if(pkSerial==0) {
			map.put("flag", 0);
		}else if(pkSerial == count-1) {
			map.put("flag", 1);
		}else {
			map.put("flag", 2);
		}
		map.put("md5", md5);		// 当前包数据的md5
		map.put("packnum", pkSerial);	// 当前包序号
		
		try {
			map.put("data", new String(bytes,"utf-8"));
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
		map.put("allMD5", fileMD5);
		Gson gson = new Gson();
		String data = gson.toJson(map);
		return data;
	}
	/**
	 * 
	 * 获取一个链接
	 * 
	 * @param host	主机ip
	 * @param port	链接端口
	 * @param userName	链接用户名
	 * @param password	链接密码
	 * @param vhost		虚拟主机
	 * @return			Connection创建的链接
	 */
	public static Connection getConnection(String host, int port, String userName, String password, String vhost) {
		//创建链接工厂
		ConnectionFactory factory = new ConnectionFactory();
		Connection connection = null;
		// 设置用户名密码
		factory.setUsername(userName);
		factory.setPassword(password);
		factory.setVirtualHost(vhost);
		// 设置rabbitMq服务器地址
		factory.setHost(host);
		factory.setPort(port);
		// 建立到服务器的链接
		try {
			connection = factory.newConnection();
			return connection;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
}
