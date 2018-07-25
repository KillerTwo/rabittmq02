package org.lwt.receiver;

import java.io.IOException;
import java.text.CharacterIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.lwt.tools.TestTools;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.Basic.Deliver;
import com.rabbitmq.client.AMQP.Queue;


public class Customer {
	private final static String QUEUE_NAME = "hello";
	
	public static void main(String[] args) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername("yduser");
		factory.setPassword("yd@user");
		factory.setVirtualHost("ydkpbmp");
	    factory.setHost("10.10.10.14");
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
        Gson gson = new Gson();
        while(true) {
            //������Ϣ
            boolean autoAck = true;	// ����Ϊ�ֶ�ȷ��
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
                    //channel.basicAck(deliveryTag, false);
                    System.out.println("���ѵ���Ϣ�����ݣ�");
                    
                    String bodyStr = new String(body, "UTF-8");		//���յ�����Ϣ
                    //System.out.println(bodyStr);
                    System.out.println("------------------------");
                    
                    Map<String,Object> map = new HashMap<>();
                    map = gson.fromJson(bodyStr, new com.google.gson.reflect.TypeToken<Map<String,Object>>(){}.getType());
                    System.out.println(map.get("data"));
                    //String md5 = TestTools.getStringMD5(new String(map.get("data"),"utf-8"));
                    //String data = "";
                    System.out.println(map);
                    
					
                    /*System.out.println(data);
                    data = data.replaceAll("[\\[\\]]", "");
                    System.out.println(data);
                    byte[] bytes = data.getBytes();*/
                    /*try {
						String md5 = TestTools.getMD5String(bytes);
						if(md5 == map.get("mad5")) {
							System.out.println("У��ͨ��");
						}
						
					} catch (Exception e) {
						
						e.printStackTrace();
					}*/
                    /*System.out.println(map.get("data"));
                    System.out.println(map.get("md5"));*/
                    

                }
            });
        }
    	
	}
	
}
