package org.lwt.producer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.AMQP.Queue;

/**
 * ��������
 * @author lwt27
 *
 */

public class Producer {
	//private final static String QUEUE_NAME = "hello";
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
		
		//����������
		String exchangeName = "myexchanges01";
		channel.exchangeDeclare(exchangeName, "direct", true);
		//����routing-key
		String routingKey = "myroutingkey01";
		//������Ϣ
		byte[] messageBodyBytes = "������Ϣ--Helloworold".getBytes("utf-8");
		channel.basicPublish(exchangeName, routingKey, null, messageBodyBytes);
		
		/*Queue.DeclareOk success = channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		System.out.println("����"+success.getQueue());
		System.out.println("�ͻ�������"+success.getConsumerCount());
		System.out.println("��Ϣ��"+success.getMessageCount());
		String message = "Hello World!";
		channel.basicPublish("", QUEUE_NAME, null, message.getBytes());*/
		System.out.println(" [producer] Sent '" + messageBodyBytes.toString() + "'");
		
		channel.close();
		connection.close();
	}
	
	
	
	
}
