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


public class Conmuser {
	private final static String QUEUE_NAME = "hello";
	
	public static void main(String[] args) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername("alice");
		factory.setPassword("123456");
		factory.setVirtualHost("vhost_01");
	    factory.setHost("192.168.1.3");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	   
	    //����������
		String exchangeName = "myexchanges01";
		channel.exchangeDeclare(exchangeName, "direct", true);
		//��������
		String queueName = channel.queueDeclare().getQueue();
        
		//����routing-key
		String routingKey = "myroutingkey01";
		
		//�󶨶��У�ͨ���� routingKey �����кͽ�����������
        channel.queueBind(queueName, exchangeName, routingKey);
	    
        while(true) {
            //������Ϣ
            boolean autoAck = false;
            String consumerTag = "";
            channel.basicConsume(queueName, autoAck, consumerTag, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    String routingKey = envelope.getRoutingKey();
                    String contentType = properties.getContentType();
                    System.out.println("���ѵ�·�ɼ���" + routingKey);
                    System.out.println("���ѵ��������ͣ�" + contentType);
                    long deliveryTag = envelope.getDeliveryTag();
                    //ȷ����Ϣ
                    channel.basicAck(deliveryTag, false);
                    System.out.println("���ѵ���Ϣ�����ݣ�");
                    String bodyStr = new String(body, "UTF-8");
                    System.out.println(bodyStr);

                }
            });
        }
    	

	 
	}
	
}
