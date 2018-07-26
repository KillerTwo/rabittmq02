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
 * ��������
 * @author lwt27
 *
 */

public class Producer6 {
	private final static String QUEUE_NAME = "hello_queue";
	public static void main(String[] args) throws Exception {
		//�������ӹ���
		ConnectionFactory factory = new ConnectionFactory();
		// �����û�������
		factory.setUsername("yduser");
		factory.setPassword("yd@user");
		factory.setVirtualHost("ydkpbmp");
		// ����rabbitMq��������ַ
		factory.setHost("10.10.10.14");
		// ������������������
		Connection connection = factory.newConnection();
		//����ŵ�
		Channel channel = connection.createChannel();
		/********************************������Ӧ************************************/
		
		//��callback queue
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
	 *  �������ݲ�������Ӧ
	 * 
	 * @param message	���͵�����
	 * @param replyQueueName	�ص�����
	 * @param channel		�ŵ�
	 * @param consumer		QueueingConsumer consumer = new QueueingConsumer(channel);
	 * @return				��Ӧ��Ϣ
	 * @throws Exception	
	 */
	public static String call(String message, String replyQueueName, Channel channel, QueueingConsumer consumer) throws Exception {     
        String response = null;
        
        
        //��װcorrelationId��replyQueue����
        BasicProperties props = new BasicProperties
                                    .Builder()
                                    .replyTo(replyQueueName)
                                    .build();
        //������Ϣ��������֮ǰ��װ�õ�����replyTo=��Ӧ���ص�������
        channel.basicPublish("", QUEUE_NAME, props, message.getBytes());
        
        while (true) {
          QueueingConsumer.Delivery delivery = consumer.nextDelivery();
          //����correlationId�Ƿ�ƥ�䣬ȷ���ǲ�����ε�����
          response = new String(delivery.getBody(),"UTF-8");
          System.out.println("[��sender�����յ�����Ӧ����Ϊ��]"+response);
        }
        
        //return response; 
      }
	
	
}
