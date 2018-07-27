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
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;

import org.lwt.tools.FileUtils;
import org.lwt.tools.JsonUtil;
import org.lwt.tools.EncryptUtil;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.sun.scenario.effect.DelegateEffect;
import com.rabbitmq.client.AMQP.Basic.Deliver;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue;

/**
 * 接受端类
 * @author Administrator
 *
 */
public class Customer2 {
	//private final static String QUEUE_NAME = "hello_queue";
	
	public static void main(String[] args) throws Exception {
		//ConnectionFactory factory = new ConnectionFactory();
		String ip = "10.10.10.14";
		int port = 5672;
		String username = "yduser";
		String password = "yd@user";
		String vhost = "ydkpbmp";
		// 建立到服务器的链接
	 	Connection connection = getConnection(ip, port, username, password, vhost);
	    Channel channel = connection.createChannel();
	   
	    //声明交换器
		String exchangeName = "myexchanges02";
		channel.exchangeDeclare(exchangeName, "direct", true);
		//声明队列
		String queueName = channel.queueDeclare().getQueue();
        
		//声明routing-key
		String routingKey = "myroutingkey02";
		
		//绑定队列，通过键 routingKey 将队列和交换器绑定起来
        channel.queueBind(queueName, exchangeName, routingKey);
        // 返回向应得信道
        //Channel recvChannel = connection.createChannel();
		//recvChannel.queueDeclare(QUEUE_NAME, false, false, false, null);
        
        
        List<byte[]> byteList= new ArrayList<>();
        String path = FileUtils.class.getClassLoader().getResource("").getPath();
		path = path.substring(1, path.length());
		int i = 10;
		File file = new File(path+i*10+".txt");
		/*BasicProperties props = null;
		BasicProperties replyProps = null;*/
        //while(true) {
            //消费消息
			Map<Double, Object> sortedMap = new TreeMap<>();
            boolean autoAck = false;	// 设置为自动确认
            String consumerTag = "";
            channel.basicConsume(queueName, autoAck, consumerTag, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    BasicProperties props = properties;
                    BasicProperties replyProps = new BasicProperties()
                    		.builder().build();
                    
                    System.out.println("消费的消息体内容：");
                    
                    String bodyStr = new String(body, "UTF-8");		//接收到的消息
                    //System.out.println("Customer接收到的消息是==》"+bodyStr);
                    System.out.println(bodyStr);
                    //System.out.println("------------------------");
                    
                    
  
                    // 将收到的资源解析为map对象
                    Map<String,Object> map = JsonUtil.getMapFromJson(bodyStr);
                    
                   
                    byte[] bytes = ((String) map.get("data")).getBytes();
                    
                    
                    String recMd5 = "";
                    try {
                    	recMd5 = EncryptUtil.getMD5String(bytes);
					} catch (Exception e) {
						e.printStackTrace();
					}
                    //System.out.print(map.get("md5")+"=================>");
                   // System.out.println(recMd5);
                    String response = "is ok...";
                    if(map.get("md5").equals(recMd5)) {
                    	sortedMap.put((Double)map.get("packnum"), bytes);
                    	System.out.println("MD5校验通过。。。");
                    	byteList.add(bytes);
                    	
                        //拿到replyQueue，并绑定为routing key，发送消息
                        //channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                        //返回消息确认信息
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    	//recvChannel.basicPublish("", QUEUE_NAME, null, ((String) map.get("packnum")+"已经接受到...").getBytes("utf-8"));
                    	//System.out.println("发送了一条响应...");
                    }
                    
                    
            		//FileUtils.write2File(file, bytes);
                    
                	//System.out.println("响应给服务端的的数据是[]"+response);
                	//System.out.println("包的总数是==="+map.get("packcount"));
                    if(sortedMap.size() == (Double)map.get("packcount")) {
                    	//循环sortedMap将内容写入文件中
                    	for(Map.Entry<Double, Object> entry: sortedMap.entrySet()) {
                    		//System.out.println(entry.getKey() + "=="+ entry.getValue());
                    		FileUtils.write2File(file, (byte[])entry.getValue());
                    	}
                    	String fileMD5 = EncryptUtil.getFileMD5(file);
                    	System.out.println(map.get("allMD5"));
                    	System.out.println(fileMD5);
                    	if(map.get("allMD5").equals(fileMD5)) {		//如果最终文件md5校验通过则返回接收成功的响应。
                    		System.err.println("文件md5相等。。。");
                    		System.err.println("已经成功接收数据...");
                        	Map<String, Object> responseMap = new HashMap<>();
                        	responseMap.put("pkId", map.get("packid"));
                        	responseMap.put("msg", 0);
                        	Gson gson = new Gson();
                        	response = gson.toJson(responseMap);
                        	// 在发回响应前睡5000毫秒
                        	/*try {
    							Thread.sleep(5000);
    						} catch (InterruptedException e) {
    							e.printStackTrace();
    						}*/
                        	 // 接收到所有的包后再返回一个响应
                            //拿到replyQueue，并绑定为routing key，发送消息
                        	channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    	}else {
                    		file.delete();
                    		
                    	}
                    	
                    }

                }
            });
            
            //System.out.println("已通过验证的包数量："+byteList.size());
        //}
        
	}
	/**
	 * 解析传递的json字符串为Map对象
	 * 
	 * 
	 * @param jsonStr	json字符串
	 * @return	Map,	由json字符串解析得到的map对象
	 */
	public static Map<String, Object> getMapFromJson(String jsonStr){
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<>();
		map = gson.fromJson(jsonStr, new com.google.gson.reflect.TypeToken<Map<String,Object>>(){}.getType());
		return map;
	}
	
	/**
	 * 
	 * 获取一个链接
	 * 
	 * @param host	主机ip
	 * @param port	链接端口
	 * @param userName	链接用户名
	 * @param password	链接密码
	 * @param vhost		虚拟主机
	 * @return			Connection创建的链接
	 */
	public static Connection getConnection(String host, int port, String userName, String password, String vhost) {
		//创建链接工厂
		ConnectionFactory factory = new ConnectionFactory();
		Connection connection = null;
		// 设置用户名密码
		factory.setUsername(userName);
		factory.setPassword(password);
		factory.setVirtualHost(vhost);
		// 设置rabbitMq服务器地址
		factory.setHost(host);
		factory.setPort(port);
		// 建立到服务器的链接
		try {
			connection = factory.newConnection();
			return connection;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	
}
