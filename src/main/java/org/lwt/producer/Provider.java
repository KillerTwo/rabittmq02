package org.lwt.producer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.Queue;

/**
 * ��������
 * @author lwt27
 *
 */

public class Provider {
	//private final static String QUEUE_NAME = "hello";
	public static void main(String[] args) throws Exception {
		//�������ӹ���
		ConnectionFactory factory = new ConnectionFactory();
		// �����û�������
		factory.setUsername("alice");
		factory.setPassword("123456");
		factory.setVirtualHost("vhost_01");
		// ����rabbitMq��������ַ
		factory.setHost("192.168.1.3");
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
