package org.lwt.producer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.lwt.tools.FileUtils;
import org.lwt.tools.TestTools;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.Queue;

/**
 * ��������
 * @author lwt27
 *
 */

public class Producer2 {
	//private final static String QUEUE_NAME = "hello";
	public static void main(String[] args) throws Exception {
		//�������ӹ���
		ConnectionFactory factory = new ConnectionFactory();
		// �����û�������
		factory.setUsername("yduser");
		factory.setPassword("yd@user");
		factory.setVirtualHost("ydkpbmp");
		// ����rabbitMq��������ַ
		factory.setHost("10.10.10.14");
		// ������������������
		Connection connection = factory.newConnection();
		//����ŵ�
		Channel channel = connection.createChannel();
		
		//����������
		String exchangeName = "myexchanges01";
		channel.exchangeDeclare(exchangeName, "direct", true);
		//����routing-key
		String routingKey = "myroutingkey01";
		//������Ϣ
		// �ϴ�һ���ļ�
		String path = Producer2.class.getClassLoader().getResource("").getPath();
		path = path.substring(1, path.length());
		File file = new File(path+"text.txt");
		
		String fileMD5 = TestTools.getFileMD5(file);	//��ȡ���ϴ��ļ���MD5
		List<byte[]> byteList = FileUtils.splitDemo(file);	//���ļ���֣�ÿ��Ϊ1024bit��
		/*for(byte[] bytes: byteList){
			System.out.println(new String(bytes,"utf-8"));
		}*/
		for(int i = 0; i < byteList.size(); i++) {
			Map<String, Object> map = new HashMap<>();
			String md5 = TestTools.getMD5String(byteList.get(i));
			map.put("date", System.currentTimeMillis()+1);
			map.put("packid", fileMD5);	// ���ļ���md5ֵ��Ϊ����idֵ
			map.put("packcount", byteList.size());	//�����ϴ������������ֳɶ��ٸ�С��
			if(i==0) {
				map.put("flag", 0);
			}else if(i == byteList.size()-1) {
				map.put("flag", 1);
			}else {
				map.put("flag", 2);
			}
			map.put("md5", md5);		// ��ǰ�����ݵ�md5
			map.put("packnum", i);	// ��ǰ�����
			map.put("packcount", byteList.size());
			//map.put("data", byteList.get(i));
			map.put("data", new String(byteList.get(i),"utf-8"));
			map.put("allMD5", fileMD5);
			Gson gson = new Gson();
			String data = gson.toJson(map);
			// �ֿ�����ÿһ���ֵ�����
			channel.basicPublish(exchangeName, routingKey, null, data.getBytes());
		}
		/*
		FileInputStream in = new FileInputStream(file);
		int len;
		byte[] bytes = new byte[1024];
		while((len=in.read(bytes))!=-1) {
			channel.basicPublish(exchangeName, routingKey, null, bytes);
		}*/
		
		
		channel.close();
		connection.close();
	}
	
	
	
	
}
