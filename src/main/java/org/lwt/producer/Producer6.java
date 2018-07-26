package org.lwt.producer;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue;

/**
 * 生产者类
 * @author lwt27
 *
 */

public class Producer6 {
	private final static String QUEUE_NAME = "hello_queue";
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
		/********************************接收响应************************************/
		
		//绑定callback queue
		//BasicProperties props = new BasicProperties.Builder().replyTo(callbackQueueName).build();
		//channel.basicPublish("", QUEUE_NAME, props, "hello world".getBytes());
		
		String callbackQueueName = channel.queueDeclare().getQueue();
		System.out.println(callbackQueueName);
		QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(callbackQueueName, true, consumer);
	    /*************************************************************************/
		
	    call("hello world... this is request...",callbackQueueName,channel,consumer);
	    
	    
	    
		
		
		
		/*channel.close();
		connection.close();*/
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
	public static String call(String message, String replyQueueName, Channel channel, QueueingConsumer consumer) throws Exception {     
        String response = null;
        
        
        //封装correlationId和replyQueue属性
        BasicProperties props = new BasicProperties
                                    .Builder()
                                    .replyTo(replyQueueName)
                                    .build();
        //发送消息，并加上之前封装好的属性replyTo=响应（回调）队列
        channel.basicPublish("", QUEUE_NAME, props, message.getBytes());
        
        while (true) {
          QueueingConsumer.Delivery delivery = consumer.nextDelivery();
          //检验correlationId是否匹配，确定是不是这次的请求
          response = new String(delivery.getBody(),"UTF-8");
          System.out.println("[《sender》接收到的响应内容为：]"+response);
        }
        
        //return response; 
      }
	
	
}
