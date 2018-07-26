package org.lwt.receiver;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.Basic.Deliver;
import com.rabbitmq.client.AMQP.Queue;


public class Conmuser4 {
	private final static String QUEUE_NAME = "hello_jianghu";
	
	public static void main(String[] args) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername("alice");
		factory.setPassword("123456");
		factory.setVirtualHost("vhost_01");
	    factory.setHost("192.168.1.3");
	    factory.setPort(5672);
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	   
	    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	    
	    boolean flag = false;
	    channel.basicConsume(QUEUE_NAME, true, new DefaultConsumer(channel) {
	        @Override
	        public void handleDelivery(String consumerTag,
	                                   Envelope envelope,
	                                   AMQP.BasicProperties properties,
	                                   byte[] body) throws IOException {
	           
	        	
	            System.out.println("consumer���ѵ���Ϣ�����ݣ�");
	            String bodyStr = new String(body, "UTF-8");
	            
	            System.out.println(bodyStr);
	            // ����ͬ�Ķ����з���ȷ����Ϣ
	            if(bodyStr != null) {
	            	channel.basicPublish("",QUEUE_NAME, null, new String("ȷ����Ϣ").getBytes("utf-8"));
	            }
	            
	          
	        }
	    });
	   
	    
	 
	}
	
}
