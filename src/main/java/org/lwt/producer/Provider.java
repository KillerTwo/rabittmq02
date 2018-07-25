package org.lwt.producer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.Queue;

/**
 * 生产者类
 * @author lwt27
 *
 */

public class Provider {
	//private final static String QUEUE_NAME = "hello";
	public static void main(String[] args) throws Exception {
		//创建链接工厂
		ConnectionFactory factory = new ConnectionFactory();
		// 设置用户名密码
		factory.setUsername("alice");
		factory.setPassword("123456");
		factory.setVirtualHost("vhost_01");
		// 设置rabbitMq服务器地址
		factory.setHost("192.168.1.3");
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
		byte[] messageBodyBytes = "发布消息--Helloworold".getBytes("utf-8");
		channel.basicPublish(exchangeName, routingKey, null, messageBodyBytes);
		
		/*Queue.DeclareOk success = channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		System.out.println("队列"+success.getQueue());
		System.out.println("客户端数量"+success.getConsumerCount());
		System.out.println("消息数"+success.getMessageCount());
		String message = "Hello World!";
		channel.basicPublish("", QUEUE_NAME, null, message.getBytes());*/
		System.out.println(" [producer] Sent '" + messageBodyBytes.toString() + "'");
		
		channel.close();
		connection.close();
	}
	
	
	
	
}
