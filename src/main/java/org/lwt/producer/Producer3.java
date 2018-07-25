package org.lwt.producer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.lwt.tools.TestTools;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.AMQP.Queue;

/**
 * 生产者类
 * @author lwt27
 *
 */

public class Producer3 {
	private final static String QUEUE_NAME = "hello_confirm";
	public static void main(String[] args) throws Exception {
		//创建链接工厂
		ConnectionFactory factory = new ConnectionFactory();
		// 设置用户名密码
		factory.setUsername("alice");
		factory.setPassword("123456");
		factory.setVirtualHost("vhost_01");
		// 设置rabbitMq服务器地址
		factory.setHost("192.168.1.3");
		factory.setPort(5672);
		// 建立到服务器的链接
		Connection connection = factory.newConnection();
		//获得信道
		Channel channel = connection.createChannel();
		
		channel.queueDeclare(QUEUE_NAME,false,false,false,null);
		channel.confirmSelect();
		
		String msg = "Hello World...";
		System.out.println(" [producer] Sent '" +"" + "'");
		
		channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
		System.err.println("----------------------------");
		
		//确认
		if(!channel.waitForConfirms(0)) {
			System.out.println("信息发送失败");
		}else {
			System.out.println("信息发送成功");
		}
		
		
		
		
		channel.close();
		connection.close();
	}
	
	
	
	
}
