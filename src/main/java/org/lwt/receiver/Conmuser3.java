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


public class Conmuser3 {
	private final static String QUEUE_NAME = "hello_confirm";
	
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
	    
      
	    channel.basicConsume(QUEUE_NAME, false, new DefaultConsumer(channel) {
	        @Override
	        public void handleDelivery(String consumerTag,
	                                   Envelope envelope,
	                                   AMQP.BasicProperties properties,
	                                   byte[] body) throws IOException {
	           
	        	
	            System.out.println("���ѵ���Ϣ�����ݣ�");
	            String bodyStr = new String(body, "UTF-8");
	            System.out.println(bodyStr);
	            /*try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}*/
	            long deliveryTag = envelope.getDeliveryTag();
	            System.err.println(deliveryTag);
	            //ȷ����Ϣ
	            channel.basicNack(deliveryTag, false,true);
	            System.out.println("�Ѿ�����ȷ����");
	        }
	    });
   

	 
	}
	
}
