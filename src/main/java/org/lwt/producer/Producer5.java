package org.lwt.producer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.lwt.tools.FileUtils;
import org.lwt.tools.EncryptUtil;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.Queue;

/**
 * ��������
 * @author lwt27
 *
 */

public class Producer5 {
	private final static String QUEUE_NAME = "hello_jianghu";
	public static void main(String[] args) throws Exception {
		String ip = "192.168.1.3";
		int port = 5672;
		String username = "alice";
		String password = "123456";
		String vhost = "vhost_01";
		
		// ������������������
		Connection connection = getConnection(ip, port, username, password, vhost);
		//����ŵ�
		Channel channel = connection.createChannel();
		
		
		channel.queueDeclare(QUEUE_NAME,false,false,false,null);
		String msg = "hello";
		/*for(int i = 0; i < 10; i++ ) {
			msg += i;*/
			// ������Ϣ
			System.out.println("��ʼ������Ϣ������");
			channel.basicPublish("",QUEUE_NAME, null, msg.getBytes());
			// ����Ӧ��
			channel.basicConsume(QUEUE_NAME, true, new DefaultConsumer(channel) {
		        @Override
		        public void handleDelivery(String consumerTag,
		                                   Envelope envelope,
		                                   AMQP.BasicProperties properties,
		                                   byte[] body) throws IOException {
		           
		        	
		            System.out.println("��Ӧ����Ϣ�����ݣ�");
		            String bodyStr = new String(body, "UTF-8");
		            System.out.println(bodyStr);
		           
		        }
		    });
		//}
		
		
		
		if(channel != null) {
			channel.close();
		}
		if(connection != null) {
			connection.close();
		}
		
		
	}
	/**
	 * �������ݵ�rabbitMQ
	 * @param byteList	�ļ��İ��ֽ�����
	 * @param file		�ļ�file
	 * @param channel	RabbitMQ�ŵ�
	 * @param exchangeName	RabbitMQ ��������
	 * @param routingKey	RabbitMQ ·�ɼ�
	 */
	public static void toSend(List<byte[]> byteList, File file, Channel channel, String exchangeName, String routingKey){
		String fileMD5 = EncryptUtil.getFileMD5(file);	//��ȡ���ϴ��ļ���MD5
		for(int i = 0; i < byteList.size(); i++) {
			String data = getFilePack(byteList.get(i), fileMD5, byteList.size(), i);
			// �ֿ�����ÿһ���ֵ�����
			try {
				channel.basicPublish(exchangeName, routingKey, null, data.getBytes());
			} catch (IOException e) {
				
				e.printStackTrace();
			}
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
	public static String getFilePack(byte[] bytes, String fileMD5, int count, int pkSerial){
		Map<String, Object> map = new HashMap<>();
		String md5 = null;
		try {
			md5 = EncryptUtil.getMD5String(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		map.put("date", System.currentTimeMillis()+1);
		map.put("packid", fileMD5);	// ���ļ���md5ֵ��Ϊ����idֵ
		map.put("packcount", count);	//�����ϴ������������ֳɶ��ٸ�С��
		if(pkSerial==0) {
			map.put("flag", 0);
		}else if(pkSerial == count-1) {
			map.put("flag", 1);
		}else {
			map.put("flag", 2);
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
