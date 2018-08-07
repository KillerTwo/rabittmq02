package org.lwt.multipart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;

import javax.jws.WebService;

import org.apache.commons.codec.digest.DigestUtils;
import org.lwt.exception.TimeOutException;
import org.lwt.test.CountDown;
import org.lwt.tools.EncryptUtil;
import org.lwt.tools.FileUtils;
import org.lwt.tools.JsonUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.Basic.Return;
import com.rabbitmq.client.AMQP.BasicProperties;

/**
 *  	��װ���ݴ���ͽ��շ�����
 * @author Administrator
 *
 */
@WebService(serviceName="UpLoadFile")
@SuppressWarnings("deprecation")
public class UploadFileImpl implements UpLoadFile {
	
	
	private String username = "yduser";										// �û���
	private String password = "yd@user";									// ����
	private String vhost = "ydkpbmp";										// ��������
	private String ip = "10.10.10.14";										// ����ip
	private int port = 5672;												// ���ʶ˿�
	private Connection connection = null;									// tcp����
	private Channel channel = null;											// �ŵ�
	private String callbackQueueName = null;								// ��Ӧ������
	private boolean responseFlag = false;									// �Ƿ���Ҫ�ط������Ϊtrue����Ҫ�ط������Ϊfalse����Ҫ�ط���
	private File sourceFile = null;											// ���ϴ��ļ���File����Դ�ļ�File
	private boolean openFileFlag = true;									// ����ǽ��յ���һ�����򴴽�һ���ļ��洢���յ�������
    private File targetFile = null;											// Ŀ���ļ�	File
    private int recvPackCount = 0;											// ���ڼ������յ������ݰ�����
    private boolean isReSend = false;										// �жϽ��յ��������Ƿ�Ϊ�ط�����
    private static boolean firstRecv = false;								// ���Ϊtrue��ʾ�������ݽ���
    private static int sendTime = 0;										// �������ݵĴ���
    private List<byte[]> recvList = 
    		new ArrayList<>();												// ������Ž��յ��������ֽ������list
    private List<Map<String, Object>> recvListMap = 
    		new ArrayList<>();												// ������Ž��յ�����ʱ���Ѿ�ͨ����֤�����ݣ�����packNum,packSize,byte[]����ѭ�������ݴ浽�ļ���
    private String filenameTemp = null;
	public UploadFileImpl() {
		Connection connection = getConnection(ip, 
				port, username, password, vhost);							// ������������������
		try {
			this.channel = connection.createChannel();						//����ŵ�
			this.callbackQueueName = channel.queueDeclare().
					getQueue();												// ���ûص�����
		} catch (IOException e) {
			e.printStackTrace();
		}						
	}
	
	public UploadFileImpl(String ip, int port, 
			String username, String password, String vhost) {
		super();
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
		this.vhost = vhost;
	}
	
	
	/********************************************************/
	/*@Override
	public boolean sendData(String path) throws Exception{
		sourceFile = new File(path);
		connection = getConnection(ip, port, username, password, vhost);	// ������������������
		channel = connection.createChannel();								//����ŵ�
		callbackQueueName = channel.queueDeclare().getQueue();				// ���ûص�����
		
		*//**
		 * 	������Ӧ��Ϣ�� ���趨��ʱ���ڽ��յ���Ӧ�����ǳɹ����յ���Ӧ��
		 * 	responseFlag��־����Ϊtrue
		 * 	��ʾ����Ҫ�ط����ݣ��ڽ��յ�ʧ�ܵ���Ӧ��responseFlag��
		 * 	־����Ϊfalse��ʾ��Ҫ���·���һ������
		 *//*
		try {
			UploadFileImpl.this.channel.basicConsume(callbackQueueName, 
					new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, 
						Envelope envelope, BasicProperties properties,
						byte[] body)
						throws IOException {
					System.err.println("���յ���Ӧ��==��");
					String response = new String(body, "utf-8");
					Map<String, Object> resMap = JsonUtil.
							getMapFromJson(response);
					if("0".equals(resMap.get("status"))) {					// ������ص�״̬��Ϊ0����ճɹ�
						System.out.println("�ɹ�����");
						responseFlag = true;
						sendTime = 0;
					}
					System.out.println(response);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		*//****************************************//*
		sendTask();
		System.out.println("sendTime "+sendTime+
				" || responseFlag "+responseFlag);							// ������ɵ�һ������(�ط�����С��3����û���յ���Ӧ�����ѭ��)
		while(sendTime <= 3 && !responseFlag) {
			break;
			System.out.println("����whileѭ��������");
			if(firstRecv) {
				
				new CountDown(10, responseFlag);							// ��ʱ10����
				System.out.println("�ж��Ƿ���Ҫ�ط�������");
																			// �ڸ�����ʱ���ڽ��յ���Ӧ
				if(responseFlag) {
					firstRecv = false;
				}else {														// û���ٸ�����ʱ���ڽ��յ���Ӧ
					firstRecv = true;
				}
				if(firstRecv) {
					System.err.println("�ط����ݡ�"+sendTime+"��");
					sendTask();
				}else {
					
					System.out.println("����Ҫ�ط�������");
					return true;
				}
			}
		}
		return false;
	}*/
	@Override
	public boolean sendData(String path, String fileName) throws Exception{
		byte[] tempByte = EncryptUtil.decodeByteByBase64(path);
		sourceFile = FileUtils.bytes2File(tempByte,"C:\\RabbitMqTemp\\", fileName);
		//sourceFile = new File(path);
		connection = getConnection(ip, port, username, password, vhost);	// ������������������
		channel = connection.createChannel();								//����ŵ�
		callbackQueueName = channel.queueDeclare().getQueue();				// ���ûص�����
		
		/**
		 * 	������Ӧ��Ϣ�� ���趨��ʱ���ڽ��յ���Ӧ�����ǳɹ����յ���Ӧ��
		 * 	responseFlag��־����Ϊtrue
		 * 	��ʾ����Ҫ�ط����ݣ��ڽ��յ�ʧ�ܵ���Ӧ��responseFlag��
		 * 	־����Ϊfalse��ʾ��Ҫ���·���һ������
		 */
		try {
			UploadFileImpl.this.channel.basicConsume(callbackQueueName, 
					new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, 
						Envelope envelope, BasicProperties properties,
						byte[] body)
						throws IOException {
					System.err.println("���յ���Ӧ��==��");
					String response = new String(body, "utf-8");
					Map<String, Object> resMap = JsonUtil.
							getMapFromJson(response);
					if("0".equals(resMap.get("status"))) {					// ������ص�״̬��Ϊ0����ճɹ�
						System.out.println("�ɹ�����");
						responseFlag = true;
						sendTime = 0;
					}
					System.out.println(response);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		/****************************************/
		sendTask();
		System.out.println("sendTime "+sendTime+
				" || responseFlag "+responseFlag);							// ������ɵ�һ������(�ط�����С��3����û���յ���Ӧ�����ѭ��)
		while(sendTime <= 3 && !responseFlag) {
			System.out.println("����whileѭ��������");
			if(firstRecv) {
				
				new CountDown(10, responseFlag);							// ��ʱ10����
				System.out.println("�ж��Ƿ���Ҫ�ط�������");
																			// �ڸ�����ʱ���ڽ��յ���Ӧ
				if(responseFlag) {
					firstRecv = false;
				}else {														// û���ٸ�����ʱ���ڽ��յ���Ӧ
					firstRecv = true;
				}
				if(firstRecv) {
					System.err.println("�ط����ݡ�"+sendTime+"��");
					sendTask();
				}else {
					
					System.out.println("����Ҫ�ط�������");
					return true;
				}
			}
		}
		return false;
	}
	public void sendTask() {
		try {
			String exchangeName = "myexchanges05";							//����������
			channel.exchangeDeclare(exchangeName, "direct", true);
			String routingKey = "myroutingkey05";							//����routing-key
			List<byte[]> byteList = FileUtils.splitDemo(sourceFile);		//���ļ���֣�ÿ��Ϊ1024�ֽڣ�
			toSend(byteList, sourceFile, channel, exchangeName, 
					routingKey, callbackQueueName, null);					// ��������		
			System.out.println("�������ݽ���������");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*******************************************************/
	@Override
	public void receiver(String path, String fileName) throws Exception {
		/*byte[] tempByte = EncryptUtil.decodeByteByBase64(path);
		targetFile = FileUtils.bytes2File(tempByte,"C:\\RabbitMqTemp\\", fileName);*/
		recvPackCount = 0;
		Connection connection = getConnection(ip, port, 
				username, password, vhost);									// ������������������
	    Channel channel = connection.createChannel();						
		String exchangeName = "myexchanges05";								
		channel.exchangeDeclare(exchangeName, "direct", true);				//����������
		String queueName = channel.queueDeclare().getQueue();				//��������							
		String routingKey = "myroutingkey05";								//����routing-key
        channel.queueBind(queueName, exchangeName, routingKey);				//�󶨶��У�ͨ���� routingKey �����кͽ�����������
        /**
         * 	������Ϣ
         */
		
        boolean autoAck = false;											// ����Ϊ�ֶ�ȷ��
        String consumerTag = "";
        
        channel.basicConsume(queueName, autoAck, consumerTag,
        		new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                BasicProperties props = properties;
                BasicProperties replyProps = new BasicProperties()
                		.builder().build();
                System.err.println("���յ����ݡ�����");
                if(isReSend) {												 // �ж��Ƿ�Ϊ�ط����ݣ�������ط����ѱ����ļ�ɾ���������ط�����
                	recvList.clear();
                	recvListMap.clear();
                	if(targetFile != null && targetFile.exists()) {
                		if(targetFile.delete()) {
                			System.out.println("ɾ���Ա����ļ���"
                					+ "��ʼ�����ط������ݡ�����");
                		}
                	}
                	isReSend = false;
                }
                recvPackCount++;	                                        // ���յ��İ���������1			
                String bodyStr = new String(body,"utf-8");					//���յ�����Ϣ
                //System.out.println("���ն˵�json�ַ�����"+bodyStr);
                Map<String,Object> map = JsonUtil.
                		getMapFromJson(bodyStr);							// ���յ�����Դ����Ϊmap����													
                if(openFileFlag) {											// ����ǵ�һ�ν����ļ��򴴽�Ŀ���ļ�
                	
                	try {
                		File temp = new File(path);
                		System.err.println("����Ŀ¼������");
                		temp.mkdirs();
						targetFile = new File(path+map.
								get("fileName")+"."+map.get("ext"));			// �����ļ����·��
						if(!targetFile.exists()) {
							targetFile.createNewFile();
						}
					} catch (Exception e) {
						
						e.printStackTrace();
					}
                	openFileFlag = false;
                }
                byte[] bytes = EncryptUtil
                		.decodeByteByBase64((String)map.get("data"));		// �����յ����������ݽ���Ϊ�ֽ����飬������뵽�ļ���
                String recMd5 = "";												
                try {
                	recMd5 = EncryptUtil.getMD5String(bytes);				// ��ý��յ����ֽ������md5ֵ
				} catch (Exception e) {
					e.printStackTrace();
				}
                String response = "is ok...";								// ��Ӧ����Ϣ
                System.err.println("��ʼMD5��֤��");
                if(map.get("md5").equals(recMd5)) {
                	System.err.println("MD5��֤ͨ����");
                	recvList.add(bytes);									// ��֤ͨ�����ֽ�������ӵ�recvList��
                	Map<String, Object> writeMap = new HashMap<>();
                	writeMap.put("data", recvList.size()-1);				// ���recvList�ж������ݵ�������
                	int packNum = 0;
                	long packSize = 0;
                	try {
						packNum = (int) map.get("packnum");					// ��ǰ�������
						packSize = new Long((int) map.get("packSize"));		// ÿ�����Ĵ�С
						writeMap.put("packNum", packNum);
						writeMap.put("packSize", packSize);
						recvListMap.add(writeMap);
					} catch (Exception e) {
						System.out.println("����ת�������쳣������");
						e.printStackTrace();
					}					
	                try {
						channel.basicAck(envelope.getDeliveryTag(), 
								false);										//������Ϣȷ����Ϣ
					} catch (Exception e1) {
						e1.printStackTrace();
					}
                }
                BigDecimal size = null;						
				try {
					size = (BigDecimal)map.get("packcount");				// ���Ͷ˷��͵İ���������
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				if(recvPackCount != size.intValue() && 						// ����Ѿ����յ����һ������������������һ�����ʾ�����ˣ����Ѿ�����ļ�ɾ��
						(int)map.get("flag") == 2) {
					isReSend = true;										// ���ط���־����Ϊtrue,��ʾ�ڴ�֮���ٽ��յ�������Ϊ�ط�����
                	recvPackCount = 0;										// �����յ��İ���������Ϊ0
					if(targetFile != null && targetFile.exists()) {
						if(targetFile.delete()) {
							System.out.println("������ɾ���ļ�������");
						}
					}
                	System.out.println("����������");
                }
                if(recvPackCount == size.intValue()) {						// ������յ������ݰ������ͷ��͵�������ͬ���ʾû�ж���
                	isReSend = true;										// ���ط���־����Ϊtrue,��ʾ�ڴ�֮���ٽ��յ�������Ϊ�ط�����
                	recvPackCount = 0;										// �����յ��İ���������Ϊ0
                	System.out.println("��������ȡ�");
                	String fileMD5 = 
                			EncryptUtil.getFileMD5String(recvList);			// ��ȡrecvList���ֽ������MD5ֵ�����ļ���md5ֵ��
                	System.out.println("allMD5== "+map.get("allMD5"));
                	System.out.println("fileMD5== "+fileMD5);
                	if(map.get("allMD5").equals(fileMD5)) {					//��������ļ�md5У��ͨ���򷵻ؽ��ճɹ�����Ӧ��
                		System.err.println("all md5���...");
                    	Map<String, Object> responseMap = 
                    			new HashMap<>();
                    	responseMap.put("packid", map.get("packid"));
                    	responseMap.put("status", "0");
                    	response = JsonUtil.getJsonFromMap(responseMap);
                    	// �ڷ�����Ӧǰ˯15000����
                    	/*try {
							Thread.sleep(15000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/
                    	/**	���յ����еİ����ٷ���һ����Ӧ
                    	 *	 �õ�replyQueue������Ϊrouting key��������Ϣ
                         * 
                         * **/
                    	try {
                    		System.out.println("������Ӧ������");
							channel.basicPublish("", 
									props.getReplyTo(), replyProps, 
									response.getBytes("UTF-8"));
						} catch (Exception e) {
							e.printStackTrace();
						}finally {
							try(RandomAccessFile randomFile = 
									new RandomAccessFile(targetFile, "rw");){	// �ڴ�д���ļ�
								for (Map<String, Object> m : recvListMap) {
									FileUtils.writeToFile(randomFile, 
											recvList.get((int)m.get("data")), 
											(int)m.get("packNum"), 
											(long)m.get("packSize"));
								}
							}catch(Exception e) {
								e.printStackTrace();
							}
						}
                	}else {
                		try {
                			if(targetFile.delete()) {
                				System.out.println("ɾ���ļ��ɹ���");				// ��������ļ���md5ֵ��֤��ͨ�����򽫸��ļ�ɾ���󷵻ش�����Ӧ
                			}
						} catch (Exception e) {
							e.printStackTrace();
						}
                	}
                }
            }
        });
        System.out.println("ִ�д���������");
	}
	
	/**
	 * 	�������ݵ�rabbitMQ
	 * 	@param byteList	�ļ��İ��ֽ�����
	 * 	@param file		�ļ�file
	 * 	@param channel	RabbitMQ�ŵ�
	 * 	@param exchangeName	RabbitMQ ��������
	 * 	@param routingKey	RabbitMQ ·�ɼ�
	 * 	@return long			����һ�����а����������ʱ��ʱ��
	 */
	private static long toSend(List<byte[]> byteList, File file,
			Channel channel, String exchangeName, 
			String routingKey, String callbackQueueName,
			QueueingConsumer consumer){
		//String fileMD5 = EncryptUtil.getFileMD5(file);					//��ȡ���ϴ��ļ���MD5
		System.out.println("�������ݡ�");
		String fileMD5 = "";
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			fileMD5 = DigestUtils.md5Hex(in);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String fileName = file.getName();
		for(int i = 0; i < byteList.size(); i++) {							// �ֿ�����ÿһ���ֵ�����
			String data = getFilePack(byteList.get(i), 
					fileMD5, byteList.size(), i, fileName);
			try {
				call(data,callbackQueueName, channel,
						consumer, exchangeName, routingKey);				// ��������
			} catch (TimeOutException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		firstRecv = true;													// ��ʾ�����Ѿ����ͳɹ�
		sendTime++;															// ���ʹ�����1
		System.out.println("for�������ݽ���������");
		return System.currentTimeMillis();									// ���һ�����ݰ�������ɺ󷵻�һ��������ɵ�ʱ��
	}
	
	/**
	 * 	 ��������
	 * 
	 * 	@param message	���͵�����
	 * 	@param replyQueueName	�ص�����
	 * 	@param channel		�ŵ�
	 * 	@param consumer		
	 * 	QueueingConsumer consumer = new QueueingConsumer(channel);
	 * 	@return				��Ӧ��Ϣ
	 * 	@throws Exception	
	 */
	private static void call(String message, 
			String replyQueueName, Channel channel, 
			QueueingConsumer consumer,
			String exchangeName, String routingkey) 
					throws Exception {     
        BasicProperties props = new BasicProperties							//��װreplyQueue���ԣ��ص����У�����������Ӧ��
                                    .Builder()
                                    .replyTo(replyQueueName)
                                    .build();
        channel.basicPublish(exchangeName, 
        		routingkey, props, message.getBytes("utf-8"));				 //������Ϣ��������֮ǰ��װ�õ�����replyTo=��Ӧ���ص�������
    }
	
	/**
	 * 	��ȡ����json�ַ���
	 * 	@param bytes		ÿ���ֶε��ֽ�����
	 * 	@param fileMD5	�����ļ���MD5ֵ
	 * 	@param count		�����ļ��İ���
	 * 	@param pkSerial	����ţ���ǰ�ǵڼ�������
	 * 	@return	String	����json�ַ���
	 */
	private static String getFilePack(byte[] bytes, 
			String fileMD5, double count, int pkSerial,
			String fileName){
		Map<String, Object> map = new HashMap<>();
		String md5 = null;
		try {
			md5 = EncryptUtil.getMD5String(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] fileNames = fileName.split("\\.");
		map.put("fileName", fileNames[0]);
		map.put("ext", fileNames[1]);
		map.put("date", System.currentTimeMillis()+1);
		map.put("packid", fileMD5);											// ���ļ���md5ֵ��Ϊ����idֵ
		map.put("packcount", count);										//�����ϴ������������ֳɶ��ٸ�С��
		if(pkSerial==0) {
			map.put("flag", 0);
		}else if(pkSerial == count-1) {
			map.put("flag", 2);
		}else {
			map.put("flag", 1);
		}
		map.put("md5", md5);												// ��ǰ�����ݵ�md5
		map.put("packnum", pkSerial);										// ��ǰ�����
		map.put("date", System.currentTimeMillis());						// ���Ͱ���ʱ��
		try {
			map.put("data", EncryptUtil.encodeByBase64(bytes));
		} catch (Exception e) {
			e.printStackTrace();
		}
		map.put("packSize", new Long(1024));
		map.put("allMD5", fileMD5);
		String data = JsonUtil.getJsonFromMap(map);
		return data;
	}
	/**
	 * �����ļ�
	 * @param path
	 * @throws Exception
	 * @{@link Return} �����ļ���·��
	 */
	@Override
	public void recv(String path,String fileName) throws Exception {
		// ����һ���µ����ӳ�
				ConnectionFactory factory = new ConnectionFactory();
				// ���ӵ�ַ
				factory.setHost("10.10.10.14");
				// ��������
				factory.setUsername("yduser");
				// ��������
				factory.setPassword("yd@user");
				// ���ӵ������
				factory.setVirtualHost("ydkpbmp");
				// ����һ���µ�����
				Connection connection = factory.newConnection();
				// ����һ������
				Channel channel = connection.createChannel();
				// ����������
				String exchangeName = "myexchanges05";
				channel.exchangeDeclare(exchangeName, "direct", true);
				// ��������
				String queueName = channel.queueDeclare().getQueue();
				// ����routing-key
				String routingKey = "myroutingkey05";
				// �󶨶��У�ͨ���� routingKey �����кͽ�����������
				channel.queueBind(queueName, exchangeName, routingKey);

				// ����json����
				Gson gson = new Gson();
				// �����ص���Ϣ��װ��hashmap��
				Map<String, Object> hashmap = new HashMap<>();

				
				boolean autoAck = false;// ������һ�ļ��ı�־
				
				channel.basicConsume(queueName, autoAck, new DefaultConsumer(channel) {
					
					// ������յ��İ��������ı���
					double i = 0.0;
					
					//�½��ļ��ı�־��Ĭ��Ϊ�½��ļ���ʼ
					boolean BulidFileTag = true;

					File filename = null;

					
					
					//����ʼʱ��
					//long starTime=System.currentTimeMillis();
					
					int n=0;

					@Override
					public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
							byte[] body) throws IOException {
						
						
						
						i++;
						
						System.out.println("���յ��İ�������====��" + i);
						
						BasicProperties property = properties;
						BasicProperties replyProps = new BasicProperties.Builder().build();
						// ������Ϣ
						String bodyStr = new String(body, "UTF-8");
						
						
						// �����յ���Ϣ��װ��map��
						Map<String, Object> map = new HashMap<>();
						// ����json�ַ���
						map = gson.fromJson(bodyStr, new TypeToken<HashMap<String, Object>>() {
						}.getType());
						String m = (String) map.get("md5");
						byte[] bytes = EncryptUtil.decodeByteByBase64((String) map.get("data"));

						
						
						if (BulidFileTag) {
							filenameTemp = path + (String) map.get("fileName") + "." + (String) map.get("ext");
							filename = new File(filenameTemp);
							//�ж��ļ����Ƿ�����ظ������ļ�
							if (filename.exists() && filename.isFile()) {
								n=n+1;
								//filename.delete();
								filenameTemp = path + (String) map.get("fileName") + n + "." + (String) map.get("ext");
								filename = new File(filenameTemp);
							}
							//�½��ļ�����
							BulidFileTag = false;
						}
						
						try {
							//�Ե���������MD5����
							String datamd5 = DigestUtils.md5Hex(bytes);
							//System.out.println("��������md5����=====��" + datamd5);
							RandomAccessFile randomFile=new RandomAccessFile(filename, "rw");
							// ��֤ÿ������md5�Ƿ���ԭʼ��md5��ͬ
							if (datamd5.equals(m)) {
								System.out.println("������У��ͨ��......");
								
								FileUtils.writeToFile(randomFile, bytes, (int) ((double) map.get("packnum")),  (long) ((double) map.get("packSize")));
							}
							
							//�жϽ��յ��İ��������Ƿ��뷢�Ͱ�������һ��
							
							if (i == (double) map.get("packcount")||(double) map.get("flag")==2.0) {
								
								randomFile.close();
								
								i=0.0;
								
								System.out.println("�Ѿ����յ����еİ�......");
								FileInputStream downloadmd5 = new FileInputStream(filename);
								String filemd5 = DigestUtils.md5Hex(downloadmd5);
								//System.out.println("�����ļ��ļ��ܽ��Ϊ====��" + filemd5);
								downloadmd5.close();
								
								// ��֤��������md5�Ƿ���ԭʼ��MD5��ͬ
								if (filemd5.equals((String) map.get("allMD5"))) {
									System.out.println("��������MD5У��ͨ��......");
									hashmap.put("status", "0");
									hashmap.put("packid", (String) map.get("packid"));
									// ����ļ�·������Ӧ���ļ����ڡ��ļ����ݺ�md5У��ɹ�
									String information = gson.toJson(hashmap);
									String response = null;
									response = information;
									
									//��Ϣ�ظ�����
									channel.basicPublish("", property.getReplyTo(), replyProps, response.getBytes("UTF-8"));
									// ������Ϣȷ����Ϣ
									channel.basicAck(envelope.getDeliveryTag(), false);
									
									//�����ڴ�
									map.clear();
									hashmap.clear();
									//�������ʱ��
									//long endTime=System.currentTimeMillis();
									
									//long time=endTime-starTime;
									
									//System.out.println("===============>���������ʱ��Ϊ=====��"+time);
									
									
								} else {
									map.clear();
									hashmap.clear();
									if (filename.delete()) {
										System.out.println("�ļ�ɾ���ɹ�");
									} else {
										System.out.println("�ļ�ɾ��ʧ��");
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}finally {
							System.out.println("�Ѿ�����ȫ���ļ�......");
						}
					}

				});
	}
	
	
	/**
	 * 
	 *	 ��ȡһ������
	 * 
	 * 	@param host	����ip
	 * 	@param port	���Ӷ˿�
	 * 	@param userName	�����û���
	 * 	@param password	��������
	 * 	@param vhost		��������
	 * 	@return			Connection����������
	 */
	private static Connection getConnection(String host, 
			int port, String userName, String password, 
			String vhost) {
		ConnectionFactory factory = new ConnectionFactory();				//�������ӹ���
		Connection connection = null;
		factory.setUsername(userName);										// �����û�������
		factory.setPassword(password);
		factory.setVirtualHost(vhost);
		factory.setHost(host);												// ����rabbitMq��������ַ
		factory.setPort(port);
		try {
			connection = factory.newConnection();							// ������������������
			return connection;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return null;
	}
}
