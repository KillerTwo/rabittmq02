package org.lwt.receiver;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

@SuppressWarnings("deprecation")
public class Conmuser5 {
	private final static String QUEUE_NAME = "hello_queue";
	
	
	public static void main(String[] args) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		// �����û�������
		factory.setUsername("yduser");
		factory.setPassword("yd@user");
		factory.setVirtualHost("ydkpbmp");
		// ����rabbitMq��������ַ
		factory.setHost("10.10.10.14");
	    factory.setPort(5672);
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    // ����������Ϣ�Ķ���
	    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	  //һ��ֻ����һ����Ϣ
      // channel.basicQos(1);
       QueueingConsumer consumer = new QueueingConsumer(channel);
     //������ϢӦ�����
       channel.basicConsume(QUEUE_NAME, false, consumer);
       
       
       
       while (true) {
           String response = null;
           
           
           QueueingConsumer.Delivery delivery = consumer.nextDelivery();
           
           //�õ�correlationId����
           BasicProperties props = delivery.getProperties();
           BasicProperties replyProps = new BasicProperties
                                            .Builder()
                                            .build();
           
           try {
             String message = new String(delivery.getBody(),"UTF-8");
             //int n = Integer.parseInt(message);
     
             System.out.println(" [ ��conmuser�����յ�����Ϣ�ǣ�]" + message + ")");
             response = "==>" + "ok...";
           } catch (Exception e){
             System.out.println(" [.] " + e.toString());
             response = "error...";
           }
           finally {  
             //�õ�replyQueue������Ϊrouting key��������Ϣ
             channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
             //������Ϣȷ����Ϣ
             channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
           }
         }
         
     
       
      
	  
   

	 
	}
	
}
