package org.lwt.producer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.lwt.tools.FileUtils;
import org.lwt.tools.TestTools;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.Queue;

/**
 * 生产者类
 * @author lwt27
 *
 */

public class Producer2 {
	//private final static String QUEUE_NAME = "hello";
	public static void main(String[] args) throws Exception {
		//创建链接工厂
		ConnectionFactory factory = new ConnectionFactory();
		// 设置用户名密码
		factory.setUsername("yduser");
		factory.setPassword("yd@user");
		factory.setVirtualHost("ydkpbmp");
		// 设置rabbitMq服务器地址
		factory.setHost("10.10.10.14");
		// 建立到服务器的链接
		Connection connection = factory.newConnection();
		//获得信道
		Channel channel = connection.createChannel();
		
		//声明交换器
		String exchangeName = "myexchanges01";
		channel.exchangeDeclare(exchangeName, "direct", true);
		//声明routing-key
		String routingKey = "myroutingkey01";
		//发布消息
		// 上传一个文件
		String path = Producer2.class.getClassLoader().getResource("").getPath();
		path = path.substring(1, path.length());
		File file = new File(path+"text.txt");
		
		String fileMD5 = TestTools.getFileMD5(file);	//获取待上传文件的MD5
		List<byte[]> byteList = FileUtils.splitDemo(file);	//将文件拆分（每份为1024bit）
		/*for(byte[] bytes: byteList){
			System.out.println(new String(bytes,"utf-8"));
		}*/
		for(int i = 0; i < byteList.size(); i++) {
			Map<String, Object> map = new HashMap<>();
			String md5 = TestTools.getMD5String(byteList.get(i));
			map.put("date", System.currentTimeMillis()+1);
			map.put("packid", fileMD5);	// 用文件的md5值作为包的id值
			map.put("packcount", byteList.size());	//本次上传的整个包被分成多少个小包
			if(i==0) {
				map.put("flag", 0);
			}else if(i == byteList.size()-1) {
				map.put("flag", 1);
			}else {
				map.put("flag", 2);
			}
			map.put("md5", md5);		// 当前包数据的md5
			map.put("packnum", i);	// 当前包序号
			map.put("packcount", byteList.size());
			//map.put("data", byteList.get(i));
			map.put("data", new String(byteList.get(i),"utf-8"));
			map.put("allMD5", fileMD5);
			Gson gson = new Gson();
			String data = gson.toJson(map);
			// 分开发送每一部分的数据
			channel.basicPublish(exchangeName, routingKey, null, data.getBytes());
		}
		/*
		FileInputStream in = new FileInputStream(file);
		int len;
		byte[] bytes = new byte[1024];
		while((len=in.read(bytes))!=-1) {
			channel.basicPublish(exchangeName, routingKey, null, bytes);
		}*/
		
		
		channel.close();
		connection.close();
	}
	
	
	
	
}
