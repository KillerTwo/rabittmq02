package org.lwt.producer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.lwt.exception.TimeOutException;
import org.lwt.tools.FileUtils;
import org.lwt.tools.JsonUtil;
import org.lwt.tools.EncryptUtil;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;

/**
 * 生产者类
 * @author lwt27
 *
 */
@SuppressWarnings("deprecation")
public class Producer2a {
	private static boolean responseFlag = false;
	public static void main(String[] args) throws Exception {
		/*String ip = "192.168.1.3";
		int port = 5672;
		String username = "alice";
		String password = "123456";
		String vhost = "vhost_01";*/
		String ip = "10.10.10.14";
		int port = 5672;
		String username = "yduser";
		String password = "yd@user";
		String vhost = "ydkpbmp";
		
		
		Connection connection = getConnection(ip, port, username, password, vhost);		// 建立到服务器的链接
		
		Channel channel = connection.createChannel();						//获得信道
		
		/********************************************************/
		String callbackQueueName = channel.queueDeclare().getQueue();		// 设置回调队列
		
		/********************************************************/
	    // 
		/**
		 * 接收响应消息， 在设定的时间内接收到时间后将responseFlag标志设置为true
		 * 表示不需要重发数据，在接收到失败的响应后将responseFlag标志设置为false
		 * 表示将要重新发送一次数据
		 */
		channel.basicConsume(callbackQueueName, new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				responseFlag = true;
				System.out.println("接收到响应：==》");
				
				String response = new String(body,"utf-8");
				Map<String, Object> resMap = JsonUtil.getMapFromJson(response);
				System.out.println(response);
				System.out.println(resMap);
			}
		});
		
		String exchangeName = "myexchanges02";					//声明交换器
		channel.exchangeDeclare(exchangeName, "direct", true);
		String routingKey = "myroutingkey02";					//声明routing-key
		
		
		// 上传一个文件
		/*********************获取一个文件路径和对应的File对象（在测试过程中使用，真实环境中将使用用户传递的参数）********************/
		String path = Producer2a.class.getClassLoader().getResource("").getPath();
		path = path.substring(1, path.length());
		File file = new File(path+"text.txt");
		/****************************************/
		List<byte[]> byteList = FileUtils.splitDemo(file);	//将文件拆分（每份为1024字节）
		System.out.println("包的数量== "+byteList.size());
		long finishTime = toSend(byteList, file, channel, exchangeName, routingKey, callbackQueueName, null);	// 发送数据
		
		isTimeOut(finishTime,2000);							// 全部发送完成后，开始判断是否在规定的时间内接收到响应		
		System.out.println(responseFlag);
		/**
		 * 在超时没接收到响应后只重发一次数据，
		 * 如果重发一次之后还没有收到响应则停止本次数据发送
		 */
		if(!responseFlag) {									//如果超过等待时间还没有收到响应，则应该重新发送数据
			System.out.println("接收响应超时，需要重新发送数据。。。");
			finishTime = toSend(byteList, file, channel, exchangeName, routingKey, callbackQueueName, null);	// 发送数据
			isTimeOut(finishTime, 2000);					// 全部发送完成后，开始判断是否在规定的时间内接收到响应		
		}
		
	}
	
	/**
	 *	 判断是否在规定时间内还没有接收到响应
	 * @param finishTime	发送完成的时间
	 * @param delayTime		最多等待的时间，如果超过该时间还没有接收到
	 * 响应则重发数据包
	 */
	public static void isTimeOut(long finishTime,long delayTime) {
		long currentTime = System.currentTimeMillis();
		while((currentTime - finishTime) < delayTime) {
			if(responseFlag) {
				System.out.println("收到响应，发送结束...");
				break;
			}
			currentTime = System.currentTimeMillis();
		}
	}
	
	/**
	 * 发送数据到rabbitMQ
	 * @param byteList	文件的包字节数组
	 * @param file		文件file
	 * @param channel	RabbitMQ信道
	 * @param exchangeName	RabbitMQ 交换器名
	 * @param routingKey	RabbitMQ 路由键
	 * @return long			返回一个所有包都发送完成时的时间
	 */
	public static long toSend(List<byte[]> byteList, File file,
			Channel channel, String exchangeName, 
			String routingKey, String callbackQueueName,
			QueueingConsumer consumer){
		String fileMD5 = EncryptUtil.getFileMD5(file);	//获取待上传文件的MD5
		String fileName = file.getName();
		// 分开发送每一部分的数据
		for(int i = 0; i < byteList.size(); i++) {
			String data = getFilePack(byteList.get(i), fileMD5, byteList.size(), i, fileName);
			try {
				call(data,callbackQueueName,channel,consumer,exchangeName,routingKey);		// 发送数据
			} catch (TimeOutException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 最后一个数据包发送完成后返回一个发送完成的时间
		return System.currentTimeMillis();
	}
	
	/**
	 *  发送数据
	 * 
	 * @param message	发送的数据
	 * @param replyQueueName	回调队列
	 * @param channel		信道
	 * @param consumer		QueueingConsumer consumer = new QueueingConsumer(channel);
	 * @return				响应信息
	 * @throws Exception	
	 */
	public static void call(String message, String replyQueueName, Channel channel, QueueingConsumer consumer,
			String exchangeName, String routingkey) throws Exception {     

        //封装replyQueue属性（回调队列，用来接收响应）
        BasicProperties props = new BasicProperties
                                    .Builder()
                                    .replyTo(replyQueueName)
                                    .build();
        //发送消息，并加上之前封装好的属性replyTo=响应（回调）队列
        channel.basicPublish(exchangeName, routingkey, props, message.getBytes("utf-8"));
    }
	
	/**
	 * 
	 * 重发数据包
	 * 
	 * @param channel	信道
	 * @param replyQueueName	回调queue
	 * @param packnum			包id
	 * @param exchangeName		交换器名
	 * @param routingkey		路由键
	 * @param message			要重发的数据信息
	 */
	public static void reSend(Channel channel,String replyQueueName,int packnum,
			String exchangeName, String routingkey,String message) {
		//封装correlationId和replyQueue属性
        BasicProperties props = new BasicProperties
                                    .Builder()
                                    .replyTo(replyQueueName)
                                    .build();
        //发送消息，并加上之前封装好的属性replyTo=响应（回调）队列
        try {
			channel.basicPublish(exchangeName, routingkey, props, message.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 读取响应
	 * 
	 * 
	 * @param consumer	QueueingConsumer对象
	 * @param tempMap	Map<String, Object>
	 */
	public static void recevRes(QueueingConsumer consumer, Map<String, Object> tempMap) {
		String response = null;
	    long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        long timeout = 0;
        while ((endTime - startTime) < 1000) {
        	System.out.println("等待时间是="+(endTime - startTime));
        	System.out.println("接收响应循环...");
	        QueueingConsumer.Delivery delivery = null;
			try {
				delivery = consumer.nextDelivery();
				
				if(delivery.getBody() != null) {
		        	// 如果在规定的时间内接收到响应
		        	
		        	response = new String(delivery.getBody(),"UTF-8");
			        System.out.println("[《sender》接收到的响应内容为：]"+response);
			        //接收到响应，将tempmap中对应的包id删除
			        // (将tempmap中对应的sendtime设置为0）
			        break;
		        }else {
		        	// 如果超过1秒没收到响应，则抛出异常，重发没有收到响应的包
		        	if(timeout > 1000) {
		        		System.out.println("接收响应超时");
		        		throw new TimeOutException("超时没有收到响应");
		        	}else {
		        		System.out.println("继读取响应时间");
		        		// 读取当前时间
		        		endTime = System.currentTimeMillis();
		        		//计算当前时间和初始时间之间的间隔
		        		timeout = endTime - startTime;
		        	}
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
	        System.out.println("timeout is "+timeout);
        }
	}
	
	/**
	 * 发送单条数据
	 * 
	 * @param channel	Channel对象
	 * @param exchangeName	exchange名称
	 * @param routingKey	路由键
	 * @param bytes			发送的数据的字节数组
	 * @param fileMD5		整个文件的md5值
	 * @param serial		当前包的序号
	 * @param count			包总数
	 */
	public static void sendSigle(Channel channel, String exchangeName, 
			String routingKey, byte[] bytes, 
			String fileMD5, int serial, int count,
			String fileName) {
		String data = getFilePack(bytes, fileMD5, count, serial, fileName);
		try {
			channel.basicPublish(exchangeName, routingKey, null, data.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取包的json字符串
	 * @param bytes		每个分段的字节内容
	 * @param fileMD5	整个文件的MD5值
	 * @param count		整个文件的包数
	 * @param pkSerial	包序号（当前是第几个包）
	 * @return	String	包的json字符串
	 */
	public static String getFilePack(byte[] bytes, String fileMD5, double count, int pkSerial, String fileName){
		Map<String, Object> map = new HashMap<>();
		String md5 = null;
		try {
			md5 = EncryptUtil.getMD5String(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] fileNames = fileName.split("\\.");
		map.put("ext", fileNames[1]);
		map.put("fileName", fileNames[0]);
		map.put("date", System.currentTimeMillis()+1);
		map.put("packid", fileMD5);							// 用文件的md5值作为包的id值
		map.put("packcount", count);						//本次上传的整个包被分成多少个小包
		if(pkSerial==0) {
			map.put("flag", 0);
		}else if(pkSerial == count-1) {
			map.put("flag", 2);
		}else {
			map.put("flag", 1);
		}
		map.put("md5", md5);								// 当前包数据的md5
		map.put("packnum", pkSerial);						// 当前包序号
		map.put("date", System.currentTimeMillis());		// 发送包的时间
		try {
			map.put("data", new String(bytes,"utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		map.put("allMD5", fileMD5);
		/*Gson gson = new Gson();
		String data = gson.toJson(map);*/
		String data = JsonUtil.getJsonFromMap(map);
		return data;
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
		
		ConnectionFactory factory = new ConnectionFactory();	//创建链接工厂
		Connection connection = null;
		factory.setUsername(userName);							// 设置用户名密码
		factory.setPassword(password);
		factory.setVirtualHost(vhost);
		factory.setHost(host);									// 设置rabbitMq服务器地址
		factory.setPort(port);
		try {
			connection = factory.newConnection();				// 建立到服务器的链接
			return connection;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
