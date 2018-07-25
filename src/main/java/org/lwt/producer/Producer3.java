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
 * ��������
 * @author lwt27
 *
 */

public class Producer3 {
	private final static String QUEUE_NAME = "hello_confirm";
	public static void main(String[] args) throws Exception {
		//�������ӹ���
		ConnectionFactory factory = new ConnectionFactory();
		// �����û�������
		factory.setUsername("alice");
		factory.setPassword("123456");
		factory.setVirtualHost("vhost_01");
		// ����rabbitMq��������ַ
		factory.setHost("192.168.1.3");
		factory.setPort(5672);
		// ������������������
		Connection connection = factory.newConnection();
		//����ŵ�
		Channel channel = connection.createChannel();
		
		channel.queueDeclare(QUEUE_NAME,false,false,false,null);
		channel.confirmSelect();
		
		String msg = "Hello World...";
		System.out.println(" [producer] Sent '" +"" + "'");
		
		channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
		System.err.println("----------------------------");
		
		//ȷ��
		if(!channel.waitForConfirms(0)) {
			System.out.println("��Ϣ����ʧ��");
		}else {
			System.out.println("��Ϣ���ͳɹ�");
		}
		
		
		
		
		channel.close();
		connection.close();
	}
	
	
	
	
}
