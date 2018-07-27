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
 * ���ܶ���
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
		// ������������������
	 	Connection connection = getConnection(ip, port, username, password, vhost);
	    Channel channel = connection.createChannel();
	   
	    //����������
		String exchangeName = "myexchanges02";
		channel.exchangeDeclare(exchangeName, "direct", true);
		//��������
		String queueName = channel.queueDeclare().getQueue();
        
		//����routing-key
		String routingKey = "myroutingkey02";
		
		//�󶨶��У�ͨ���� routingKey �����кͽ�����������
        channel.queueBind(queueName, exchangeName, routingKey);
        // ������Ӧ���ŵ�
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
            //������Ϣ
			Map<Double, Object> sortedMap = new TreeMap<>();
            boolean autoAck = false;	// ����Ϊ�Զ�ȷ��
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
                    
                    System.out.println("���ѵ���Ϣ�����ݣ�");
                    
                    String bodyStr = new String(body, "UTF-8");		//���յ�����Ϣ
                    //System.out.println("Customer���յ�����Ϣ��==��"+bodyStr);
                    System.out.println(bodyStr);
                    //System.out.println("------------------------");
                    
                    
  
                    // ���յ�����Դ����Ϊmap����
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
                    	System.out.println("MD5У��ͨ��������");
                    	byteList.add(bytes);
                    	
                        //�õ�replyQueue������Ϊrouting key��������Ϣ
                        //channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                        //������Ϣȷ����Ϣ
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    	//recvChannel.basicPublish("", QUEUE_NAME, null, ((String) map.get("packnum")+"�Ѿ����ܵ�...").getBytes("utf-8"));
                    	//System.out.println("������һ����Ӧ...");
                    }
                    
                    
            		//FileUtils.write2File(file, bytes);
                    
                	//System.out.println("��Ӧ������˵ĵ�������[]"+response);
                	//System.out.println("����������==="+map.get("packcount"));
                    if(sortedMap.size() == (Double)map.get("packcount")) {
                    	//ѭ��sortedMap������д���ļ���
                    	for(Map.Entry<Double, Object> entry: sortedMap.entrySet()) {
                    		//System.out.println(entry.getKey() + "=="+ entry.getValue());
                    		FileUtils.write2File(file, (byte[])entry.getValue());
                    	}
                    	String fileMD5 = EncryptUtil.getFileMD5(file);
                    	System.out.println(map.get("allMD5"));
                    	System.out.println(fileMD5);
                    	if(map.get("allMD5").equals(fileMD5)) {		//��������ļ�md5У��ͨ���򷵻ؽ��ճɹ�����Ӧ��
                    		System.err.println("�ļ�md5��ȡ�����");
                    		System.err.println("�Ѿ��ɹ���������...");
                        	Map<String, Object> responseMap = new HashMap<>();
                        	responseMap.put("pkId", map.get("packid"));
                        	responseMap.put("msg", 0);
                        	Gson gson = new Gson();
                        	response = gson.toJson(responseMap);
                        	// �ڷ�����Ӧǰ˯5000����
                        	/*try {
    							Thread.sleep(5000);
    						} catch (InterruptedException e) {
    							e.printStackTrace();
    						}*/
                        	 // ���յ����еİ����ٷ���һ����Ӧ
                            //�õ�replyQueue������Ϊrouting key��������Ϣ
                        	channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    	}else {
                    		file.delete();
                    		
                    	}
                    	
                    }

                }
            });
            
            //System.out.println("��ͨ����֤�İ�������"+byteList.size());
        //}
        
	}
	/**
	 * �������ݵ�json�ַ���ΪMap����
	 * 
	 * 
	 * @param jsonStr	json�ַ���
	 * @return	Map,	��json�ַ��������õ���map����
	 */
	public static Map<String, Object> getMapFromJson(String jsonStr){
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<>();
		map = gson.fromJson(jsonStr, new com.google.gson.reflect.TypeToken<Map<String,Object>>(){}.getType());
		return map;
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
