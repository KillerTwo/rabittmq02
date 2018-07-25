package org.lwt.receiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.lwt.tools.FileUtils;
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
	   
	    //声明交换器
		String exchangeName = "myexchanges01";
		channel.exchangeDeclare(exchangeName, "direct", true);
		//声明队列
		String queueName = channel.queueDeclare().getQueue();
        
		//声明routing-key
		String routingKey = "myroutingkey01";
		
		//绑定队列，通过键 routingKey 将队列和交换器绑定起来
        channel.queueBind(queueName, exchangeName, routingKey);
        Gson gson = new Gson();
        while(true) {
            //消费消息
            boolean autoAck = true;	// 设置为手动确认
            String consumerTag = "";
            channel.basicConsume(queueName, autoAck, consumerTag, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    String routingKey = envelope.getRoutingKey();
                    String contentType = properties.getContentType();
                    /*System.out.println("消费的路由键：" + routingKey);
                    System.out.println("消费的内容类型：" + contentType);*/
                    //long deliveryTag = envelope.getDeliveryTag();
                    //确认消息
                    //channel.basicAck(deliveryTag, false);
                    System.out.println("消费的消息体内容：");
                    
                    String bodyStr = new String(body, "UTF-8");		//接收到的消息
                    System.out.println(bodyStr);
                    System.out.println("------------------------");
                    
                    
                    
                    Map<String,Object> map = new HashMap<>();
                    map = gson.fromJson(bodyStr, new com.google.gson.reflect.TypeToken<Map<String,Object>>(){}.getType());
                    //Byte[] bytes = (Byte[])((List)map.get("data")).toArray();
                    //System.out.println(bytes);
                    /*System.out.println("是否时list");
                    System.out.println((map.get("data") instanceof List));*/
                    byte[] bytes = ((String) map.get("data")).getBytes();
                    System.out.println(bytes);
                    String path = FileUtils.class.getClassLoader().getResource("text.txt").getPath();
            		path = path.substring(1, path.length());
            		int i = 10;
            		File file = new File(path+i*10+".txt");
            		FileOutputStream out = new FileOutputStream(file);
            		out.write(bytes);
            		
                    i++;
                    //System.out.println(map.get("data"));
                    //String md5 = TestTools.getStringMD5(new String(map.get("data"),"utf-8"));
                   /* try {
						String data = (String) map.get("data");
					} catch (Exception e) {
						e.printStackTrace();
					}*/
                    //System.out.println(map.get("data"));
                    /*System.out.println("是否为字符串");
                    System.err.println((map.get("data") instanceof String));
                    System.out.println("是否为字节数组");
                    System.err.println((map.get("data") instanceof byte[]));
                    System.out.println("是否为单个字节");
                    System.err.println((map.get("data") instanceof Byte));*/
                    //Object data = map.get("data");
					//System.out.println(data);
                    //System.out.println(data);
                    //data = ((String) data).replaceAll("[\\[\\]]", "");
                    //System.out.println(data);
                   // byte[] bytes = ((String) data).getBytes();
                    /*try {
						String md5 = TestTools.getMD5String(bytes);
						if(md5 == map.get("mad5")) {
							System.out.println("校验通过");
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
