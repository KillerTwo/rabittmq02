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
		// 设置用户名密码
		factory.setUsername("yduser");
		factory.setPassword("yd@user");
		factory.setVirtualHost("ydkpbmp");
		// 设置rabbitMq服务器地址
		factory.setHost("10.10.10.14");
	    factory.setPort(5672);
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    // 声明接收消息的队列
	    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	  //一次只接收一条消息
      // channel.basicQos(1);
       QueueingConsumer consumer = new QueueingConsumer(channel);
     //开启消息应答机制
       channel.basicConsume(QUEUE_NAME, false, consumer);
       
       
       
       while (true) {
           String response = null;
           
           
           QueueingConsumer.Delivery delivery = consumer.nextDelivery();
           
           //拿到correlationId属性
           BasicProperties props = delivery.getProperties();
           BasicProperties replyProps = new BasicProperties
                                            .Builder()
                                            .build();
           
           try {
             String message = new String(delivery.getBody(),"UTF-8");
             //int n = Integer.parseInt(message);
     
             System.out.println(" [ 《conmuser》接收到的消息是：]" + message + ")");
             response = "==>" + "ok...";
           } catch (Exception e){
             System.out.println(" [.] " + e.toString());
             response = "error...";
           }
           finally {  
             //拿到replyQueue，并绑定为routing key，发送消息
             channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
             //返回消息确认信息
             channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
           }
         }
         
     
       
      
	  
   

	 
	}
	
}
