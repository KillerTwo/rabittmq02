package org.lwt.producer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.lwt.tools.FileUtils;
import org.lwt.tools.EncryptUtil;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.Queue;

/**
 * 生产者类
 * @author lwt27
 *
 */

public class Producer5 {
	private final static String QUEUE_NAME = "hello_jianghu";
	public static void main(String[] args) throws Exception {
		String ip = "192.168.1.3";
		int port = 5672;
		String username = "alice";
		String password = "123456";
		String vhost = "vhost_01";
		
		// 建立到服务器的链接
		Connection connection = getConnection(ip, port, username, password, vhost);
		//获得信道
		Channel channel = connection.createChannel();
		
		
		channel.queueDeclare(QUEUE_NAME,false,false,false,null);
		String msg = "hello";
		/*for(int i = 0; i < 10; i++ ) {
			msg += i;*/
			// 发送消息
			System.out.println("开始发送消息。。。");
			channel.basicPublish("",QUEUE_NAME, null, msg.getBytes());
			// 接收应答
			channel.basicConsume(QUEUE_NAME, true, new DefaultConsumer(channel) {
		        @Override
		        public void handleDelivery(String consumerTag,
		                                   Envelope envelope,
		                                   AMQP.BasicProperties properties,
		                                   byte[] body) throws IOException {
		           
		        	
		            System.out.println("响应的消息体内容：");
		            String bodyStr = new String(body, "UTF-8");
		            System.out.println(bodyStr);
		           
		        }
		    });
		//}
		
		
		
		if(channel != null) {
			channel.close();
		}
		if(connection != null) {
			connection.close();
		}
		
		
	}
	/**
	 * 发送数据到rabbitMQ
	 * @param byteList	文件的包字节数组
	 * @param file		文件file
	 * @param channel	RabbitMQ信道
	 * @param exchangeName	RabbitMQ 交换器名
	 * @param routingKey	RabbitMQ 路由键
	 */
	public static void toSend(List<byte[]> byteList, File file, Channel channel, String exchangeName, String routingKey){
		String fileMD5 = EncryptUtil.getFileMD5(file);	//获取待上传文件的MD5
		for(int i = 0; i < byteList.size(); i++) {
			String data = getFilePack(byteList.get(i), fileMD5, byteList.size(), i);
			// 分开发送每一部分的数据
			try {
				channel.basicPublish(exchangeName, routingKey, null, data.getBytes());
			} catch (IOException e) {
				
				e.printStackTrace();
			}
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
	public static String getFilePack(byte[] bytes, String fileMD5, int count, int pkSerial){
		Map<String, Object> map = new HashMap<>();
		String md5 = null;
		try {
			md5 = EncryptUtil.getMD5String(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
