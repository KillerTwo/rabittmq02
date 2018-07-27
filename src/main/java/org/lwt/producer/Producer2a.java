package org.lwt.producer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

import javax.management.Query;

import org.lwt.exception.TimeOutException;
import org.lwt.tools.FileUtils;
import org.lwt.tools.JsonUtil;
import org.lwt.tools.EncryptUtil;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.google.gson.Gson;

/**
 * ��������
 * @author lwt27
 *
 */
@SuppressWarnings("deprecation")
public class Producer2a {
	//private final static String QUEUE_NAME = "hello_queue";
	final static List<Map<String, Object>> reSendList = new ArrayList<>();
	final static Map<String, Object> reSendMap = new HashMap<>();
	final static Map<String, Object> sendTimeMap = new HashMap<>();
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
		
		// ������������������
		Connection connection = getConnection(ip, port, username, password, vhost);
		//����ŵ�
		Channel channel = connection.createChannel();
		// ���ûص�����
		/********************************************************/
		String callbackQueueName = channel.queueDeclare().getQueue();
		/*System.out.println(callbackQueueName);
		QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(callbackQueueName, true, consumer);*/
		/********************************************************/
	    // ������Ӧ��Ϣ
		channel.basicConsume(callbackQueueName, new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				responseFlag = true;
				System.out.println("���յ���Ӧ��==��");
				
				String response = new String(body,"utf-8");
				Map<String, Object> resMap = JsonUtil.getMapFromJson(response);
				System.out.println(response);
				System.out.println(resMap);
			}
			
			
		});
		//����������
		String exchangeName = "myexchanges02";
		channel.exchangeDeclare(exchangeName, "direct", true);
		//����routing-key
		String routingKey = "myroutingkey02";
		//������Ϣ
		
		// �ϴ�һ���ļ�
		String path = Producer2a.class.getClassLoader().getResource("").getPath();
		path = path.substring(1, path.length());
		File file = new File(path+"text.txt");
		
		/* ����ά��һ���Ѿ��������ݰ���Ψһid�Ķ��У�������ڵİ�id�յ��˽��ն˵�ȷ����Ϣ��
		 * �򽫸�ȷ����Ϣ���ڵİ�id�Ӷ�������ɾ��������ȴ�3���к�δ�յ���Ӧ��ȷ����Ϣ����
		 * ���·�һ�θ�id��Ӧ�����ݰ�
		 * */
		final Queue<Integer> sendQueue = new LinkedList<>();
		
		
		
		List<byte[]> byteList = FileUtils.splitDemo(file);	//���ļ���֣�ÿ��Ϊ1024�ֽڣ�
		System.out.println("��������== "+byteList.size());
		long finishTime = toSend(byteList, file, channel, exchangeName, routingKey, sendQueue, callbackQueueName, null);	// ��������
		// ȫ��������ɺ󣬿�ʼ�ж��Ƿ��ڹ涨��ʱ���ڽ��յ���Ӧ
		//long currentTime = System.currentTimeMillis();
		isTimeOut(finishTime,2000);
		/*while((currentTime - finishTime) < 10000) {
			if(responseFlag) {
				System.out.println("�յ���Ӧ�����ͽ���...");
				break;
			}
			currentTime = System.currentTimeMillis();
		}*/
		System.out.println(responseFlag);
		if(!responseFlag) {		//��������ȴ�ʱ�仹û���յ���Ӧ����Ӧ�����·�������
			System.out.println("������Ӧ��ʱ����Ҫ���·������ݡ�����");
			finishTime = toSend(byteList, file, channel, exchangeName, routingKey, sendQueue, callbackQueueName, null);	// ��������
			isTimeOut(finishTime, 2000);
		}
		
	}
	/**
	 *	 �ж��Ƿ��ڹ涨ʱ���ڻ�û�н��յ���Ӧ
	 * @param finishTime	������ɵ�ʱ��
	 */
	public static void isTimeOut(long finishTime,long delayTime) {
		long currentTime = System.currentTimeMillis();
		while((currentTime - finishTime) < delayTime) {
			if(responseFlag) {
				System.out.println("�յ���Ӧ�����ͽ���...");
				break;
			}
			currentTime = System.currentTimeMillis();
		}
	}
	
	/**
	 * �������ݵ�rabbitMQ
	 * @param byteList	�ļ��İ��ֽ�����
	 * @param file		�ļ�file
	 * @param channel	RabbitMQ�ŵ�
	 * @param exchangeName	RabbitMQ ��������
	 * @param routingKey	RabbitMQ ·�ɼ�
	 * @return long			����һ�����а���������ɵ�ʱ��
	 */
	public static long toSend(List<byte[]> byteList, File file,
			Channel channel, String exchangeName, 
			String routingKey, final Queue<Integer> sendQueue, String callbackQueueName,
			QueueingConsumer consumer){
		
		String fileMD5 = EncryptUtil.getFileMD5(file);	//��ȡ���ϴ��ļ���MD5
		
		String fileName = file.getName();
		for(int i = 0; i < byteList.size(); i++) {
			sendQueue.offer(i);
			/**
			 * tempmap ��������Ѿ����͵�ID�Ͷ�Ӧ�ķ���ʱ�䣬��{"packnum" = packnum,sendTime=date}
			 * ���5���û���յ���Ӧ�ð�����Ӧ�������ñ����·�һ��
			 * 
			 * �ڷ��͵�ʱ���Ӧ�����ݴ���tempmap֮��
			 */
			
			// map.put("packnum",i);
			String data = getFilePack(byteList.get(i), fileMD5, byteList.size(), i, fileName);
			// �ֿ�����ÿһ���ֵ�����
			try {
				
				call(data,callbackQueueName,channel,consumer,exchangeName,routingKey);		// ��������֮�����һ����Ӧ
				Map<String, Object> reSendMap = new HashMap<>();
				
				reSendMap.put("packnum", i);
				reSendList.add(reSendMap);
				
				sendTimeMap.put(String.valueOf(i), System.currentTimeMillis());
				
			} catch (TimeOutException e) {
				System.out.println("������Ӧ��ʱ�ˣ�Ҫ�ڴ˴��ط�...");
				try {
					
					call(data,callbackQueueName,channel,consumer,exchangeName,routingKey);
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		// ���һ�����ݰ�������ɺ󷵻�һ��������ɵ�ʱ��
		return System.currentTimeMillis();
	}
	
	/**
	 *  �������ݲ�������Ӧ
	 * 
	 * @param message	���͵�����
	 * @param replyQueueName	�ص�����
	 * @param channel		�ŵ�
	 * @param consumer		QueueingConsumer consumer = new QueueingConsumer(channel);
	 * @return				��Ӧ��Ϣ
	 * @throws Exception	
	 */
	public static void call(String message, String replyQueueName, Channel channel, QueueingConsumer consumer,
			String exchangeName, String routingkey) throws Exception {     
        String response = null;
        
        
        //��װcorrelationId��replyQueue����
        BasicProperties props = new BasicProperties
                                    .Builder()
                                    .replyTo(replyQueueName)
                                    .build();
        //������Ϣ��������֮ǰ��װ�õ�����replyTo=��Ӧ���ص�������
        channel.basicPublish(exchangeName, routingkey, props, message.getBytes("utf-8"));
        
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        long timeout = 0;

        
       
      }
	
	/**
	 * 
	 * �ط����ݰ�
	 * 
	 * @param channel	�ŵ�
	 * @param replyQueueName	�ص�queue
	 * @param packnum			��id
	 * @param exchangeName		��������
	 * @param routingkey		·�ɼ�
	 * @param message			Ҫ�ط���������Ϣ
	 */
	public static void reSend(Channel channel,String replyQueueName,int packnum,
			String exchangeName, String routingkey,String message) {
		//��װcorrelationId��replyQueue����
        BasicProperties props = new BasicProperties
                                    .Builder()
                                    .replyTo(replyQueueName)
                                    .build();
        //������Ϣ��������֮ǰ��װ�õ�����replyTo=��Ӧ���ص�������
        try {
			channel.basicPublish(exchangeName, routingkey, props, message.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * ��ȡ��Ӧ
	 * 
	 * 
	 * @param consumer
	 * @param tempMap
	 */
	public static void recevRes(QueueingConsumer consumer, Map<String, Object> tempMap) {
		String response = null;
	    long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        long timeout = 0;
        while ((endTime - startTime) < 1000) {
        	System.out.println("�ȴ�ʱ����="+(endTime - startTime));
        	System.out.println("������Ӧѭ��...");
	        QueueingConsumer.Delivery delivery = null;
			try {
				delivery = consumer.nextDelivery();
				
				if(delivery.getBody() != null) {
		        	// ����ڹ涨��ʱ���ڽ��յ���Ӧ
		        	
		        	response = new String(delivery.getBody(),"UTF-8");
			        System.out.println("[��sender�����յ�����Ӧ����Ϊ��]"+response);
			        //���յ���Ӧ����tempmap�ж�Ӧ�İ�idɾ��
			        // (��tempmap�ж�Ӧ��sendtime����Ϊ0��
			        break;
		        }else {
		        	// �������1��û�յ���Ӧ�����׳��쳣���ط�û���յ���Ӧ�İ�
		        	if(timeout > 1000) {
		        		System.out.println("������Ӧ��ʱ");
		        		throw new TimeOutException("��ʱû���յ���Ӧ");
		        	}else {
		        		System.out.println("������ȡ��Ӧʱ��");
		        		// ��ȡ��ǰʱ��
		        		endTime = System.currentTimeMillis();
		        		//���㵱ǰʱ��ͳ�ʼʱ��֮��ļ��
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
	 * ���͵�������
	 * 
	 * @param channel	Channel����
	 * @param exchangeName	exchange����
	 * @param routingKey	·�ɼ�
	 * @param bytes			���͵����ݵ��ֽ�����
	 * @param fileMD5		�����ļ���md5ֵ
	 * @param serial		��ǰ�������
	 * @param count			������
	 */
	public static void sendSigle(Channel channel, String exchangeName, 
			String routingKey, byte[] bytes, 
			String fileMD5, int serial, int count, String fileName) {
		String data = getFilePack(bytes, fileMD5, count, serial, fileName);
		try {
			channel.basicPublish(exchangeName, routingKey, null, data.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * ��ȡ����json�ַ���
	 * @param bytes		ÿ���ֶε��ֽ�����
	 * @param fileMD5	�����ļ���MD5ֵ
	 * @param count		�����ļ��İ���
	 * @param pkSerial	����ţ���ǰ�ǵڼ�������
	 * @return	String	����json�ַ���
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
		map.put("fileName", fileNames[0]);
		map.put("date", System.currentTimeMillis()+1);
		map.put("packid", fileMD5);	// ���ļ���md5ֵ��Ϊ����idֵ
		map.put("packcount", count);	//�����ϴ������������ֳɶ��ٸ�С��
		if(pkSerial==0) {
			map.put("flag", 0);
		}else if(pkSerial == count-1) {
			map.put("flag", 2);
		}else {
			map.put("flag", 1);
		}
		map.put("md5", md5);		// ��ǰ�����ݵ�md5
		map.put("packnum", pkSerial);	// ��ǰ�����
		
		try {
			map.put("data", new String(bytes,"utf-8"));
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
		map.put("allMD5", fileMD5);
		Gson gson = new Gson();
		String data = gson.toJson(map);
		return data;
	}
	/**
	 * 
	 * ��ȡһ������
	 * 
	 * @param host	����ip
	 * @param port	���Ӷ˿�
	 * @param userName	�����û���
	 * @param password	��������
	 * @param vhost		��������
	 * @return			Connection����������
	 */
	public static Connection getConnection(String host, int port, String userName, String password, String vhost) {
		//�������ӹ���
		ConnectionFactory factory = new ConnectionFactory();
		Connection connection = null;
		// �����û�������
		factory.setUsername(userName);
		factory.setPassword(password);
		factory.setVirtualHost(vhost);
		// ����rabbitMq��������ַ
		factory.setHost(host);
		factory.setPort(port);
		// ������������������
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
