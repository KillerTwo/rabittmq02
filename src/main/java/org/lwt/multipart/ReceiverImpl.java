package org.lwt.multipart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;

import javax.jws.WebService;

import org.lwt.tools.EncryptUtil;
import org.lwt.tools.FileUtils;
import org.lwt.tools.JsonUtil;


import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;
@WebService(serviceName="receiver")
public class ReceiverImpl implements Receiver {

	/*private String ip = "10.10.10.14";
	private int port = 5672;
	private String username = "yduser";
	private String password = "yd@user";
	private String vhost = "ydkpbmp";*/
	private String ip = "192.168.1.3";
	private int port = 5672;
	private String username = "alice";
	private String password = "123456";
	private String vhost = "vhost_01";
	public ReceiverImpl() {
		
	}
	
	public ReceiverImpl(String ip, int port, String username, String password, String vhost) {
		super();
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
		this.vhost = vhost;
	}

	@Override
	public void receiver(File file) throws Exception {
		Connection connection = getConnection(ip, port, username, password, vhost);		// ������������������
	    Channel channel = connection.createChannel();
		String exchangeName = "myexchanges02";											 //����������
		channel.exchangeDeclare(exchangeName, "direct", true);
		String queueName = channel.queueDeclare().getQueue();							//��������
		String routingKey = "myroutingkey02";											//����routing-key
        channel.queueBind(queueName, exchangeName, routingKey);							//�󶨶��У�ͨ���� routingKey �����кͽ�����������
        List<byte[]> byteList= new ArrayList<>();
        /**
         * ������Ϣ
         */
		Map<Double, Object> sortedMap = new TreeMap<>();
        boolean autoAck = false;	// ����Ϊ�ֶ�ȷ��
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
                String bodyStr = new String(body, "UTF-8");						//���յ�����Ϣ
                System.out.println(bodyStr);
                Map<String,Object> map = JsonUtil.getMapFromJson(bodyStr);		// ���յ�����Դ����Ϊmap����
                byte[] bytes = ((String) map.get("data")).getBytes();			// �����յ����������ݽ���Ϊ�ֽ����飬������뵽�ļ���
                String recMd5 = "";												
                try {
                	recMd5 = EncryptUtil.getMD5String(bytes);					// ��ý��յ����ֽ������md5ֵ
				} catch (Exception e) {
					e.printStackTrace();
				}
                String response = "is ok...";									// ��Ӧ����Ϣ
                if(map.get("md5").equals(recMd5)) {
                	sortedMap.put((Double)map.get("packnum"), bytes);			// һ���м�treeMap��������������յ������ݰ�
                	System.out.println("MD5У��ͨ��������");
                	byteList.add(bytes);										// �����յ���ÿһ���ֽ�������ӵ�byteList�У�֮�������֤�ְ�������
                    //������Ϣȷ����Ϣ
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
                // ������յ������ݰ������ͷ��͵�������ͬ
                if(sortedMap.size() == (Double)map.get("packcount")) {
                	//ѭ��sortedMap������д���ļ���
                	for(Map.Entry<Double, Object> entry: sortedMap.entrySet()) {
                		FileUtils.write2File(file, (byte[])entry.getValue());
                	}
                	String fileMD5 = EncryptUtil.getFileMD5(file);
                	System.out.println(map.get("allMD5"));
                	System.out.println(fileMD5);
                	if(map.get("allMD5").equals(fileMD5)) {						//��������ļ�md5У��ͨ���򷵻ؽ��ճɹ�����Ӧ��
                    	Map<String, Object> responseMap = new HashMap<>();
                    	responseMap.put("pkId", map.get("packid"));
                    	responseMap.put("msg", 0);
                    	response = JsonUtil.getJsonFromMap(responseMap);
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
                		// ��������ļ���md5ֵ��֤��ͨ�����򽫸��ļ�ɾ���󷵻ش�����Ӧ
                		file.delete();
                	}
                }
            }
        });

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
