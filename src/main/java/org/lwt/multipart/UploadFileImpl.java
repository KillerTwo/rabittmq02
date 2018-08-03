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


import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

@WebService(serviceName="UpLoadFile")
public class UploadFileImpl implements UpLoadFile {
	
	/*private String ip = "192.168.1.3";
	private int port = 5672;
	private String username = "alice";
	private String password = "123456";
	private String vhost = "vhost_01";*/
	private String username = "yduser";
	private String password = "yd@user";
	private String vhost = "ydkpbmp";
	private String ip = "10.10.10.14";
	private int port = 5672;
	private Connection connection = null;
	private Channel channel = null;
	private String callbackQueueName = null;
	private boolean responseFlag = false;		// �Ƿ���Ҫ�ط������Ϊtrue����Ҫ�ط������Ϊfalse����Ҫ�ط���
	private File sourceFile = null;						// ���ϴ��ļ���File����Դ�ļ�File
	private boolean openFileFlag = true;	// ����ǽ��յ���һ�����򴴽�һ���ļ��洢���յ�������
    private File targetFile = null;			// Ŀ���ļ�	File
    private int recvPackCount = 0;				// ���ڼ������յ������ݰ�����
    private boolean isReSend = false;			// �жϽ��յ��������Ƿ�Ϊ�ط�����
    private static boolean firstRecv = false;			// ���Ϊtrue��ʾ�������ݽ���
    private static int sendTime = 0;					// �������ݵĴ���
    private List<byte[]> recvList = new ArrayList<>();		// ������Ž��յ��������ֽ������list
    private List<Map<String, Object>> recvListMap = new ArrayList<>();		// ������Ž��յ�����ʱ���Ѿ�ͨ����֤�����ݣ�����packNum,packSize,byte[]����ѭ�������ݴ浽�ļ���
	public UploadFileImpl() {
		Connection connection = getConnection(ip, port, username, password, vhost);		// ������������������
		try {
			this.channel = connection.createChannel();									//����ŵ�
			this.callbackQueueName = channel.queueDeclare().getQueue();					// ���ûص�����
		} catch (IOException e) {
			e.printStackTrace();
		}						
	}
	
	public UploadFileImpl(String ip, int port, String username, String password, String vhost) {
		super();
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
		this.vhost = vhost;
	}
	
	/*@Override
	public void sendData(String path) throws Exception {
		File file = new File(path);
		Connection connection = getConnection(ip, port, username, password, vhost);		// ������������������
		Channel channel = connection.createChannel();						//����ŵ�
		String callbackQueueName = channel.queueDeclare().getQueue();		// ���ûص�����
		
		*//**
		 * ������Ӧ��Ϣ�� ���趨��ʱ���ڽ��յ�ʱ���responseFlag��־����Ϊtrue
		 * ��ʾ����Ҫ�ط����ݣ��ڽ��յ�ʧ�ܵ���Ӧ��responseFlag��־����Ϊfalse
		 * ��ʾ��Ҫ���·���һ������
		 *//*
		channel.basicConsume(callbackQueueName, new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				
				System.out.println("���յ���Ӧ��==��");
				
				String response = new String(body, "utf-8");
				Map<String, Object> resMap = JsonUtil.getMapFromJson(response);
				
				// ������ص�״̬��Ϊ0����ճɹ�
				if("0".equals(resMap.get("status"))) {
					System.out.println("�ɹ�����");
					responseFlag = true;
				}
				System.out.println(response);
				System.out.println(resMap);
			}
		});
		
		//String exchangeName = "myexchanges02";					//����������
		String exchangeName = "myexchanges05";		
		channel.exchangeDeclare(exchangeName, "direct", true);
		//String routingKey = "myroutingkey02";					//����routing-key
		String routingKey = "myroutingkey05";	
		List<byte[]> byteList = FileUtils.splitDemo(file);	//���ļ���֣�ÿ��Ϊ1024�ֽڣ�
		System.out.println("��������== "+byteList.size());
		long finishTime = toSend(byteList, file, channel, exchangeName, routingKey, callbackQueueName, null);	// ��������
		
		isTimeOut(responseFlag, finishTime, 5000);							// ȫ��������ɺ󣬿�ʼ�ж��Ƿ��ڹ涨��ʱ���ڽ��յ���Ӧ		
		System.out.println(responseFlag);
		*//**
		 * �ڳ�ʱû���յ���Ӧ��ֻ�ط�һ�����ݣ�
		 * ����ط�һ��֮��û���յ���Ӧ��ֹͣ�������ݷ���
		 *//*
		if(!responseFlag) {									//��������ȴ�ʱ�仹û���յ���Ӧ����Ӧ�����·�������
			System.out.println("������Ӧ��ʱ����Ҫ���·������ݡ�����");
			finishTime = toSend(byteList, file, channel, exchangeName, routingKey, callbackQueueName, null);	// ��������
			isTimeOut(responseFlag, finishTime, 5000);					// ȫ��������ɺ󣬿�ʼ�ж��Ƿ��ڹ涨��ʱ���ڽ��յ���Ӧ		
		}
	}*/
	/********************************************************/
	@Override
	public void sendData(String path) throws Exception{
		sourceFile = new File(path);
		connection = getConnection(ip, port, username, password, vhost);		// ������������������
		channel = connection.createChannel();									//����ŵ�
		callbackQueueName = channel.queueDeclare().getQueue();					// ���ûص�����
		/****************************************/
		/**
		 * ������Ӧ��Ϣ�� ���趨��ʱ���ڽ��յ���Ӧ�����ǳɹ����յ���Ӧ��responseFlag��־����Ϊtrue
		 * ��ʾ����Ҫ�ط����ݣ��ڽ��յ�ʧ�ܵ���Ӧ��responseFlag��־����Ϊfalse
		 * ��ʾ��Ҫ���·���һ������
		 */
		try {
			UploadFileImpl.this.channel.basicConsume(callbackQueueName, new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
						throws IOException {
					System.err.println("���յ���Ӧ��==��");
					String response = new String(body, "utf-8");
					Map<String, Object> resMap = JsonUtil.getMapFromJson(response);
					// ������ص�״̬��Ϊ0����ճɹ�
					if("0".equals(resMap.get("status"))) {
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
		
		/*// ����һ����ʱ��
		Timer timer = new Timer();
		// ������������
		ReSendTask sendTask = new ReSendTask(timer);
		// ������ʱ���񣬴ӵ�ǰʱ�俪ʼ���㣬10���Ӻ�ִ��һ�ζ�ʱ��
		timer.schedule(sendTask, 10);
		timer.cancel();*/
		sendTask();
		// ������ɵ�һ������(�ط�����С��3����û���յ���Ӧ�����ѭ��)
		System.out.println("sendTime "+sendTime+" || responseFlag "+responseFlag);
		while(sendTime <= 3 && !responseFlag) {
			System.out.println("����whileѭ��������");
			if(firstRecv) {
				
				new CountDown(10, responseFlag);								// ��ʱ10����
				System.out.println("�ж��Ƿ���Ҫ�ط�������");
				// �ڸ�����ʱ���ڽ��յ���Ӧ
				if(responseFlag) {
					firstRecv = false;
				}else {									// û���ٸ�����ʱ���ڽ��յ���Ӧ
					firstRecv = true;
				}
				if(firstRecv) {
					System.err.println("�ط����ݡ�"+sendTime+"��");
					sendTask();
				}else {
					System.out.println("����Ҫ�ط�������");
					break;
				}
			}
		}
	}
	
	public void sendTask() {
		try {
			String exchangeName = "myexchanges05";						//����������
			channel.exchangeDeclare(exchangeName, "direct", true);
			String routingKey = "myroutingkey05";						//����routing-key
			List<byte[]> byteList = FileUtils.splitDemo(sourceFile);	//���ļ���֣�ÿ��Ϊ1024�ֽڣ�
			//System.out.println("��������== "+byteList.size());
			toSend(byteList, sourceFile, channel, exchangeName, routingKey, callbackQueueName, null);	// ��������		
			System.out.println("�������ݽ���������");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ����һ����ʱ���ࣨ�ڲ��ࣩ��������Ϣ
	 * ÿ��5�������û���յ���Ӧ�ͷ���һ����Ϣ
	 * @author Administrator
	 *
	 */
	private class ReSendTask extends TimerTask{
		private Timer timer;
		//private boolean responseFlag;
		public ReSendTask(Timer timer) {
	    	super();
	        this.timer = timer;
	    }
		int count = 3; 		// ����ط�count��
		@Override
		public void run() {
			System.out.println("�������ݶ�ʱ��������");
			// ������յ���Ӧ���߳������ط������������ʱ����
			if(responseFlag || count <= 0) {
				this.timer.cancel(); 		//������ʱ����
				return ;
			}else {
				count--;						// count����1
				System.err.println("���͵� ��"+count+"��������");
				
				try {
					String exchangeName = "myexchanges05";						//����������
					channel.exchangeDeclare(exchangeName, "direct", true);
					String routingKey = "myroutingkey05";						//����routing-key
					List<byte[]> byteList = FileUtils.splitDemo(sourceFile);	//���ļ���֣�ÿ��Ϊ1024�ֽڣ�
					//System.out.println("��������== "+byteList.size());
					toSend(byteList, sourceFile, channel, exchangeName, routingKey, callbackQueueName, null);	// ��������		
					System.out.println("�������ݽ���������");
					this.timer.cancel();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	/*******************************************************/
	@Override
	public void receiver(String path) throws Exception {
		recvPackCount = 0;
		Connection connection = getConnection(ip, port, username, password, vhost);		// ������������������
	    Channel channel = connection.createChannel();
		//String exchangeName = "myexchanges02";											 //����������
		String exchangeName = "myexchanges05";
		channel.exchangeDeclare(exchangeName, "direct", true);
		String queueName = channel.queueDeclare().getQueue();							//��������
		//String routingKey = "myroutingkey02";											//����routing-key
		String routingKey = "myroutingkey05";	
        channel.queueBind(queueName, exchangeName, routingKey);							//�󶨶��У�ͨ���� routingKey �����кͽ�����������
        //List<byte[]> byteList= new ArrayList<>();
        /**
         * ������Ϣ
         */
		//Map<Object, Object> sortedMap = new TreeMap<>();
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
                // �ж��Ƿ�Ϊ�ط����ݣ�������ط����ѱ����ļ�ɾ���������ط�����
                if(isReSend) {
                	recvList.clear();
                	if(targetFile != null && targetFile.exists()) {
                		if(targetFile.delete()) {
                			System.out.println("ɾ���Ա����ļ�����ʼ�����ط������ݡ�����");
                		}
                	}
                	isReSend = false;
                }
                recvPackCount++;	                                                // ���յ��İ���������1			
                String bodyStr = new String(body,"utf-8");						//���յ�����Ϣ
                //System.out.println("���ն˵�json�ַ�����"+bodyStr);
                Map<String,Object> map = JsonUtil.getMapFromJson(bodyStr);		// ���յ�����Դ����Ϊmap����
                // ����ǵ�һ�ν����ļ��򴴽�Ŀ���ļ�
                if(openFileFlag) {
                	targetFile = new File(path+map.get("fileName")+"."+map.get("ext"));		// �����ļ����·��
                	openFileFlag = false;
                }
                //byte[] bytes = ((String) map.get("data")).getBytes("utf-8");	// �����յ����������ݽ���Ϊ�ֽ����飬������뵽�ļ���
                byte[] bytes = EncryptUtil.decodeByteByBase64((String)map.get("data"));
                String recMd5 = "";												
                try {
                	recMd5 = EncryptUtil.getMD5String(bytes);					// ��ý��յ����ֽ������md5ֵ
				} catch (Exception e) {
					e.printStackTrace();
				}
             
                String response = "is ok...";									// ��Ӧ����Ϣ
                if(map.get("md5").equals(recMd5)) {
                	recvList.add(bytes);										// ��֤ͨ�����ֽ�������ӵ�recvList��
                	Map<String, Object> writeMap = new HashMap<>();
                	//writeMap.put("data", bytes);
                	writeMap.put("data", recvList.size()-1);					// ���recvList�ж������ݵ�������
                	int packNum = 0;
                	long packSize = 0;
                	try {
						packNum = (int) map.get("packnum");						// ��ǰ�������
						packSize = new Long((int) map.get("packSize"));					// ÿ�����Ĵ�С
						writeMap.put("packNum", packNum);
						writeMap.put("packSize", packSize);
						recvListMap.add(writeMap);
					} catch (Exception e) {
						System.out.println("����ת�������쳣������");
						e.printStackTrace();
					}					
	            	try {
						//sortedMap.put((Integer)map.get("packnum"), bytes);
	            		//System.err.println("д��� "+recvPackCount+"��byte[]");
						/*FileUtils.write2File(targetFile, bytes, packNum, packSize);*/
					} catch (Exception e1) {
						e1.printStackTrace();
					}			// һ���м�treeMap��������������յ������ݰ�
	                //������Ϣȷ����Ϣ
	            	
	                try {
						channel.basicAck(envelope.getDeliveryTag(), false);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
                }
                
                BigDecimal size = null;						
				try {
					size = (BigDecimal)map.get("packcount");		// ���Ͷ˷��͵İ���������
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				// ����Ѿ����յ����һ������������������һ�����ʾ�����ˣ����Ѿ�����ļ�ɾ��
				if(recvPackCount != size.intValue() && (int)map.get("flag") == 2) {
					isReSend = true;													// ���ط���־����Ϊtrue,��ʾ�ڴ�֮���ٽ��յ�������Ϊ�ط�����
                	recvPackCount = 0;													// �����յ��İ���������Ϊ0
					if(targetFile != null && targetFile.exists()) {
						if(targetFile.delete()) {
							System.out.println("������ɾ���ļ�������");
						}
					}
                	System.out.println("����������");
                }
				// ������յ������ݰ������ͷ��͵�������ͬ���ʾû�ж���
                if(recvPackCount == size.intValue()) {
                	isReSend = true;					// ���ط���־����Ϊtrue,��ʾ�ڴ�֮���ٽ��յ�������Ϊ�ط�����
                	recvPackCount = 0;					// �����յ��İ���������Ϊ0
                	System.out.println("��������ȡ�");
                	
                	/*String fileMD5 = EncryptUtil.getFileMD5(targetFile);*/
                	String fileMD5 = EncryptUtil.getFileMD5String(recvList);		// ��ȡrecvList���ֽ������MD5ֵ�����ļ���md5ֵ��
                	//recvList.clear();
                	System.out.println("allMD5== "+map.get("allMD5"));
                	System.out.println("fileMD5== "+fileMD5);
                	
                	if(map.get("allMD5").equals(fileMD5)) {						//��������ļ�md5У��ͨ���򷵻ؽ��ճɹ�����Ӧ��
                		System.err.println("all md5���...");
                    	Map<String, Object> responseMap = new HashMap<>();
                    	responseMap.put("packid", map.get("packid"));
                    	responseMap.put("status", "0");
                    	response = JsonUtil.getJsonFromMap(responseMap);
                    	
                    	// �ڷ�����Ӧǰ˯15000����
                    	/*try {
							Thread.sleep(15000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/
                    	 // ���յ����еİ����ٷ���һ����Ӧ
                        //�õ�replyQueue������Ϊrouting key��������Ϣ
                    	try {
                    		System.out.println("������Ӧ������");
							channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
						} catch (Exception e) {
							e.printStackTrace();
						}finally {
							// �ڴ�д���ļ�
							try(RandomAccessFile randomFile = new RandomAccessFile(targetFile, "rw");){
								for (Map<String, Object> m : recvListMap) {
									FileUtils.writeToFile(randomFile, recvList.get((int)m.get("data")), (int)m.get("packNum"), (long)m.get("packSize"));
								}
							}catch(Exception e) {
								e.printStackTrace();
							}
						}
                	}else {
                		// ��������ļ���md5ֵ��֤��ͨ�����򽫸��ļ�ɾ���󷵻ش�����Ӧ
                		try {
                			//System.out.println("MD5�����ɾ���ļ�");
                			
                			//sortedMap.clear();
                			if(targetFile.delete()) {
                				System.out.println("ɾ���ļ��ɹ���");
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
	 * �������ݵ�rabbitMQ
	 * @param byteList	�ļ��İ��ֽ�����
	 * @param file		�ļ�file
	 * @param channel	RabbitMQ�ŵ�
	 * @param exchangeName	RabbitMQ ��������
	 * @param routingKey	RabbitMQ ·�ɼ�
	 * @return long			����һ�����а����������ʱ��ʱ��
	 */
	private static long toSend(List<byte[]> byteList, File file,
			Channel channel, String exchangeName, 
			String routingKey, String callbackQueueName,
			QueueingConsumer consumer){
		//String fileMD5 = EncryptUtil.getFileMD5(file);	//��ȡ���ϴ��ļ���MD5
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
		//System.out.println("send fileMD5: "+ fileMD5);
		String fileName = file.getName();
		// �ֿ�����ÿһ���ֵ�����
		for(int i = 0; i < byteList.size(); i++) {
			String data = getFilePack(byteList.get(i), fileMD5, byteList.size(), i, fileName);
			try {
				call(data,callbackQueueName,channel,consumer,exchangeName,routingKey);		// ��������
			} catch (TimeOutException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		firstRecv = true;	// ��ʾ�����Ѿ����ͳɹ�
		sendTime++;			// ���ʹ�����1
		System.out.println("for�������ݽ���������");
		// ���һ�����ݰ�������ɺ󷵻�һ��������ɵ�ʱ��
		return System.currentTimeMillis();
	}
	
	/**
	 *  ��������
	 * 
	 * @param message	���͵�����
	 * @param replyQueueName	�ص�����
	 * @param channel		�ŵ�
	 * @param consumer		QueueingConsumer consumer = new QueueingConsumer(channel);
	 * @return				��Ӧ��Ϣ
	 * @throws Exception	
	 */
	private static void call(String message, String replyQueueName, Channel channel, QueueingConsumer consumer,
			String exchangeName, String routingkey) throws Exception {     

        //��װreplyQueue���ԣ��ص����У�����������Ӧ��
        BasicProperties props = new BasicProperties
                                    .Builder()
                                    .replyTo(replyQueueName)
                                    .build();
        //������Ϣ��������֮ǰ��װ�õ�����replyTo=��Ӧ���ص�������
        channel.basicPublish(exchangeName, routingkey, props, message.getBytes("utf-8"));
    }
	
	
	
	
	
	
	/**
	 * ��ȡ����json�ַ���
	 * @param bytes		ÿ���ֶε��ֽ�����
	 * @param fileMD5	�����ļ���MD5ֵ
	 * @param count		�����ļ��İ���
	 * @param pkSerial	����ţ���ǰ�ǵڼ�������
	 * @return	String	����json�ַ���
	 */
	private static String getFilePack(byte[] bytes, String fileMD5, double count, int pkSerial, String fileName){
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
		map.put("packid", fileMD5);							// ���ļ���md5ֵ��Ϊ����idֵ
		map.put("packcount", count);						//�����ϴ������������ֳɶ��ٸ�С��
		if(pkSerial==0) {
			map.put("flag", 0);
		}else if(pkSerial == count-1) {
			map.put("flag", 2);
		}else {
			map.put("flag", 1);
		}
		map.put("md5", md5);								// ��ǰ�����ݵ�md5
		map.put("packnum", pkSerial);						// ��ǰ�����
		map.put("date", System.currentTimeMillis());		// ���Ͱ���ʱ��
		try {
			map.put("data", EncryptUtil.encodeByBase64(bytes));
		} catch (Exception e) {
			e.printStackTrace();
		}
		map.put("packSize", new Long(1024));
		map.put("allMD5", fileMD5);
		
		String data = JsonUtil.getJsonFromMap(map);
		//System.out.println("���Ͷ�json�ַ�����"+data);
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
	private static Connection getConnection(String host, int port, String userName, String password, String vhost) {
		
		ConnectionFactory factory = new ConnectionFactory();	//�������ӹ���
		Connection connection = null;
		factory.setUsername(userName);							// �����û�������
		factory.setPassword(password);
		factory.setVirtualHost(vhost);
		factory.setHost(host);									// ����rabbitMq��������ַ
		factory.setPort(port);
		try {
			connection = factory.newConnection();				// ������������������
			return connection;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return null;
	}
}
