package org.lwt.producer;

import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

import org.lwt.tools.EncryptUtil;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue;

/**
 * 生产者类
 * @author lwt27
 *
 */

public class Producer4 {
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
		// 存放未确认的消息标识
		final SortedSet<Long> confirmSet = Collections.synchronizedSortedSet(new TreeSet<>());
		
		channel.addConfirmListener(new ConfirmListener() {
			
			//发送失败时调用(可以重发消息)
			@Override
			public void handleNack(long deliveryTag, boolean multiple) throws IOException {
				if(multiple) {
					System.out.println("-----handleNack---multiple"+"---"+deliveryTag);
					confirmSet.headSet(deliveryTag+1).clear();
				}else {
					System.out.println("-----handleNack---multiple false"+"---"+deliveryTag);
					confirmSet.remove(deliveryTag);
				}
				
			}
			//发送成功时调用
			@Override
			public void handleAck(long deliveryTag, boolean multiple) throws IOException {
				if(multiple) {
					System.out.println("-----handleAck---multiple"+"---"+deliveryTag);
					confirmSet.headSet(deliveryTag+1).clear();
				}else {
					System.out.println("-----handleAck---multiple false"+"---"+deliveryTag);
					confirmSet.remove(deliveryTag);
				}
				
			}
		});
		// 发送消息失败时会调用
		//当消息被RabbitMQ拒绝或者说没有成功投递的时候（mandatory设置为true时）
		channel.addReturnListener(new ReturnListener() {
			
			@Override
			public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
				 BasicProperties properties, byte[] body) throws IOException {
				
				 System.out.println("响应状态码-ReplyCode："+replyCode);
	             System.out.println("响应内容-ReplyText："+replyCode);
	             System.out.println("Exchange:"+exchange);
	             System.out.println("RouteKey"+routingKey);
	             System.out.println("投递失败的消息："+ new String(body,"UTF-8") );
			}
		});
		
		String msg = "发送消息";
		int i = 1;
		while(i < 10) {
			msg +=i*10;
			System.err.println(msg);
			long seqNo = channel.getNextPublishSeqNo();
			//channel.basicPublish("", QUEUE_NAME, null, msg.getBytes("utf-8"));
			channel.basicPublish("", QUEUE_NAME, true,true, MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes("utf-8"));
			confirmSet.add(seqNo);
			i++;
		}
		
		
		
	}
	
	
	
	
}
