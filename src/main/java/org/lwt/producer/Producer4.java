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
 * ��������
 * @author lwt27
 *
 */

public class Producer4 {
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
		// ���δȷ�ϵ���Ϣ��ʶ
		final SortedSet<Long> confirmSet = Collections.synchronizedSortedSet(new TreeSet<>());
		
		channel.addConfirmListener(new ConfirmListener() {
			
			//����ʧ��ʱ����(�����ط���Ϣ)
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
			//���ͳɹ�ʱ����
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
		// ������Ϣʧ��ʱ�����
		//����Ϣ��RabbitMQ�ܾ�����˵û�гɹ�Ͷ�ݵ�ʱ��mandatory����Ϊtrueʱ��
		channel.addReturnListener(new ReturnListener() {
			
			@Override
			public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
				 BasicProperties properties, byte[] body) throws IOException {
				
				 System.out.println("��Ӧ״̬��-ReplyCode��"+replyCode);
	             System.out.println("��Ӧ����-ReplyText��"+replyCode);
	             System.out.println("Exchange:"+exchange);
	             System.out.println("RouteKey"+routingKey);
	             System.out.println("Ͷ��ʧ�ܵ���Ϣ��"+ new String(body,"UTF-8") );
			}
		});
		
		String msg = "������Ϣ";
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
