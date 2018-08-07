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
 *  	封装数据传输和接收方法，
 * @author Administrator
 *
 */
@WebService(serviceName="UpLoadFile")
@SuppressWarnings("deprecation")
public class UploadFileImpl implements UpLoadFile {
	
	
	private String username = "yduser";										// 用户名
	private String password = "yd@user";									// 密码
	private String vhost = "ydkpbmp";										// 虚拟主机
	private String ip = "10.10.10.14";										// 主机ip
	private int port = 5672;												// 访问端口
	private Connection connection = null;									// tcp连接
	private Channel channel = null;											// 信道
	private String callbackQueueName = null;								// 响应队列名
	private boolean responseFlag = false;									// 是否需要重发（如果为true则不需要重发，如果为false则需要重发）
	private File sourceFile = null;											// 待上传文件的File对象，源文件File
	private boolean openFileFlag = true;									// 如果是接收到第一个包则创建一个文件存储接收到的数据
    private File targetFile = null;											// 目标文件	File
    private int recvPackCount = 0;											// 用于计数接收到的数据包数量
    private boolean isReSend = false;										// 判断接收到的数据是否为重发数据
    private static boolean firstRecv = false;								// 如果为true表示发送数据结束
    private static int sendTime = 0;										// 发送数据的次数
    private List<byte[]> recvList = 
    		new ArrayList<>();												// 用来存放接收到的所有字节数组的list
    private List<Map<String, Object>> recvListMap = 
    		new ArrayList<>();												// 用来存放接收到数据时，已经通过验证的数据（包括packNum,packSize,byte[]），循环将内容存到文件中
    private String filenameTemp = null;
	public UploadFileImpl() {
		Connection connection = getConnection(ip, 
				port, username, password, vhost);							// 建立到服务器的链接
		try {
			this.channel = connection.createChannel();						//获得信道
			this.callbackQueueName = channel.queueDeclare().
					getQueue();												// 设置回调队列
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
		connection = getConnection(ip, port, username, password, vhost);	// 建立到服务器的链接
		channel = connection.createChannel();								//获得信道
		callbackQueueName = channel.queueDeclare().getQueue();				// 设置回调队列
		
		*//**
		 * 	接收响应消息， 在设定的时间内接收到响应并且是成功接收的响应后将
		 * 	responseFlag标志设置为true
		 * 	表示不需要重发数据，在接收到失败的响应后将responseFlag标
		 * 	志设置为false表示将要重新发送一次数据
		 *//*
		try {
			UploadFileImpl.this.channel.basicConsume(callbackQueueName, 
					new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, 
						Envelope envelope, BasicProperties properties,
						byte[] body)
						throws IOException {
					System.err.println("接收到响应：==》");
					String response = new String(body, "utf-8");
					Map<String, Object> resMap = JsonUtil.
							getMapFromJson(response);
					if("0".equals(resMap.get("status"))) {					// 如果返回的状态码为0则接收成功
						System.out.println("成功接收");
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
				" || responseFlag "+responseFlag);							// 发送完成第一次请求(重发次数小于3并且没有收到响应则进入循环)
		while(sendTime <= 3 && !responseFlag) {
			break;
			System.out.println("进入while循环。。。");
			if(firstRecv) {
				
				new CountDown(10, responseFlag);							// 延时10秒钟
				System.out.println("判断是否需要重发。。。");
																			// 在给定的时间内接收到响应
				if(responseFlag) {
					firstRecv = false;
				}else {														// 没有再给定的时间内接收到响应
					firstRecv = true;
				}
				if(firstRecv) {
					System.err.println("重发数据《"+sendTime+"》");
					sendTask();
				}else {
					
					System.out.println("不需要重发。。。");
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
		connection = getConnection(ip, port, username, password, vhost);	// 建立到服务器的链接
		channel = connection.createChannel();								//获得信道
		callbackQueueName = channel.queueDeclare().getQueue();				// 设置回调队列
		
		/**
		 * 	接收响应消息， 在设定的时间内接收到响应并且是成功接收的响应后将
		 * 	responseFlag标志设置为true
		 * 	表示不需要重发数据，在接收到失败的响应后将responseFlag标
		 * 	志设置为false表示将要重新发送一次数据
		 */
		try {
			UploadFileImpl.this.channel.basicConsume(callbackQueueName, 
					new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, 
						Envelope envelope, BasicProperties properties,
						byte[] body)
						throws IOException {
					System.err.println("接收到响应：==》");
					String response = new String(body, "utf-8");
					Map<String, Object> resMap = JsonUtil.
							getMapFromJson(response);
					if("0".equals(resMap.get("status"))) {					// 如果返回的状态码为0则接收成功
						System.out.println("成功接收");
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
				" || responseFlag "+responseFlag);							// 发送完成第一次请求(重发次数小于3并且没有收到响应则进入循环)
		while(sendTime <= 3 && !responseFlag) {
			System.out.println("进入while循环。。。");
			if(firstRecv) {
				
				new CountDown(10, responseFlag);							// 延时10秒钟
				System.out.println("判断是否需要重发。。。");
																			// 在给定的时间内接收到响应
				if(responseFlag) {
					firstRecv = false;
				}else {														// 没有再给定的时间内接收到响应
					firstRecv = true;
				}
				if(firstRecv) {
					System.err.println("重发数据《"+sendTime+"》");
					sendTask();
				}else {
					
					System.out.println("不需要重发。。。");
					return true;
				}
			}
		}
		return false;
	}
	public void sendTask() {
		try {
			String exchangeName = "myexchanges05";							//声明交换器
			channel.exchangeDeclare(exchangeName, "direct", true);
			String routingKey = "myroutingkey05";							//声明routing-key
			List<byte[]> byteList = FileUtils.splitDemo(sourceFile);		//将文件拆分（每份为1024字节）
			toSend(byteList, sourceFile, channel, exchangeName, 
					routingKey, callbackQueueName, null);					// 发送数据		
			System.out.println("发送数据结束。。。");
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
				username, password, vhost);									// 建立到服务器的链接
	    Channel channel = connection.createChannel();						
		String exchangeName = "myexchanges05";								
		channel.exchangeDeclare(exchangeName, "direct", true);				//声明交换器
		String queueName = channel.queueDeclare().getQueue();				//声明队列							
		String routingKey = "myroutingkey05";								//声明routing-key
        channel.queueBind(queueName, exchangeName, routingKey);				//绑定队列，通过键 routingKey 将队列和交换器绑定起来
        /**
         * 	消费消息
         */
		
        boolean autoAck = false;											// 设置为手动确认
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
                System.err.println("接收到数据。。。");
                if(isReSend) {												 // 判断是否为重发数据，如果是重发则将已保存文件删除，接收重发数据
                	recvList.clear();
                	recvListMap.clear();
                	if(targetFile != null && targetFile.exists()) {
                		if(targetFile.delete()) {
                			System.out.println("删除以保存文件，"
                					+ "开始接收重发的数据。。。");
                		}
                	}
                	isReSend = false;
                }
                recvPackCount++;	                                        // 接收到的包的数量加1			
                String bodyStr = new String(body,"utf-8");					//接收到的消息
                //System.out.println("接收端的json字符串："+bodyStr);
                Map<String,Object> map = JsonUtil.
                		getMapFromJson(bodyStr);							// 将收到的资源解析为map对象													
                if(openFileFlag) {											// 如果是第一次接收文件则创建目标文件
                	
                	try {
                		File temp = new File(path);
                		System.err.println("创建目录。。。");
                		temp.mkdirs();
						targetFile = new File(path+map.
								get("fileName")+"."+map.get("ext"));			// 构建文件存放路径
						if(!targetFile.exists()) {
							targetFile.createNewFile();
						}
					} catch (Exception e) {
						
						e.printStackTrace();
					}
                	openFileFlag = false;
                }
                byte[] bytes = EncryptUtil
                		.decodeByteByBase64((String)map.get("data"));		// 将接收到的数据内容解析为字节数组，方便存入到文件中
                String recMd5 = "";												
                try {
                	recMd5 = EncryptUtil.getMD5String(bytes);				// 获得接收到的字节数组的md5值
				} catch (Exception e) {
					e.printStackTrace();
				}
                String response = "is ok...";								// 响应的信息
                System.err.println("开始MD5验证。");
                if(map.get("md5").equals(recMd5)) {
                	System.err.println("MD5验证通过。");
                	recvList.add(bytes);									// 验证通过则将字节数组添加到recvList中
                	Map<String, Object> writeMap = new HashMap<>();
                	writeMap.put("data", recvList.size()-1);				// 存放recvList中对于数据的索引，
                	int packNum = 0;
                	long packSize = 0;
                	try {
						packNum = (int) map.get("packnum");					// 当前包的序号
						packSize = new Long((int) map.get("packSize"));		// 每个包的大小
						writeMap.put("packNum", packNum);
						writeMap.put("packSize", packSize);
						recvListMap.add(writeMap);
					} catch (Exception e) {
						System.out.println("类型转换出现异常。。。");
						e.printStackTrace();
					}					
	                try {
						channel.basicAck(envelope.getDeliveryTag(), 
								false);										//返回消息确认信息
					} catch (Exception e1) {
						e1.printStackTrace();
					}
                }
                BigDecimal size = null;						
				try {
					size = (BigDecimal)map.get("packcount");				// 发送端发送的包的总数量
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				if(recvPackCount != size.intValue() && 						// 如果已经接收到最后一个包，但包的数量不一致则表示丢包了，将已经存的文件删除
						(int)map.get("flag") == 2) {
					isReSend = true;										// 将重发标志设置为true,表示在此之后再接收到的数据为重发数据
                	recvPackCount = 0;										// 将接收到的包数量设置为0
					if(targetFile != null && targetFile.exists()) {
						if(targetFile.delete()) {
							System.out.println("丢包，删除文件。。。");
						}
					}
                	System.out.println("丢包。。。");
                }
                if(recvPackCount == size.intValue()) {						// 如果接收到的数据包数量和发送的数量相同则表示没有丢包
                	isReSend = true;										// 将重发标志设置为true,表示在此之后再接收到的数据为重发数据
                	recvPackCount = 0;										// 将接收到的包数量设置为0
                	System.out.println("包数量相等。");
                	String fileMD5 = 
                			EncryptUtil.getFileMD5String(recvList);			// 获取recvList中字节数组的MD5值（即文件的md5值）
                	System.out.println("allMD5== "+map.get("allMD5"));
                	System.out.println("fileMD5== "+fileMD5);
                	if(map.get("allMD5").equals(fileMD5)) {					//如果最终文件md5校验通过则返回接收成功的响应。
                		System.err.println("all md5相等...");
                    	Map<String, Object> responseMap = 
                    			new HashMap<>();
                    	responseMap.put("packid", map.get("packid"));
                    	responseMap.put("status", "0");
                    	response = JsonUtil.getJsonFromMap(responseMap);
                    	// 在发回响应前睡15000毫秒
                    	/*try {
							Thread.sleep(15000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/
                    	/**	接收到所有的包后再返回一个响应
                    	 *	 拿到replyQueue，并绑定为routing key，发送消息
                         * 
                         * **/
                    	try {
                    		System.out.println("发送响应。。。");
							channel.basicPublish("", 
									props.getReplyTo(), replyProps, 
									response.getBytes("UTF-8"));
						} catch (Exception e) {
							e.printStackTrace();
						}finally {
							try(RandomAccessFile randomFile = 
									new RandomAccessFile(targetFile, "rw");){	// 在此写入文件
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
                				System.out.println("删除文件成功。");				// 如果最终文件的md5值验证不通过，则将该文件删除后返回错误响应
                			}
						} catch (Exception e) {
							e.printStackTrace();
						}
                	}
                }
            }
        });
        System.out.println("执行次数。。。");
	}
	
	/**
	 * 	发送数据到rabbitMQ
	 * 	@param byteList	文件的包字节数组
	 * 	@param file		文件file
	 * 	@param channel	RabbitMQ信道
	 * 	@param exchangeName	RabbitMQ 交换器名
	 * 	@param routingKey	RabbitMQ 路由键
	 * 	@return long			返回一个所有包都发送完成时的时间
	 */
	private static long toSend(List<byte[]> byteList, File file,
			Channel channel, String exchangeName, 
			String routingKey, String callbackQueueName,
			QueueingConsumer consumer){
		//String fileMD5 = EncryptUtil.getFileMD5(file);					//获取待上传文件的MD5
		System.out.println("发送数据。");
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
		for(int i = 0; i < byteList.size(); i++) {							// 分开发送每一部分的数据
			String data = getFilePack(byteList.get(i), 
					fileMD5, byteList.size(), i, fileName);
			try {
				call(data,callbackQueueName, channel,
						consumer, exchangeName, routingKey);				// 发送数据
			} catch (TimeOutException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		firstRecv = true;													// 表示数据已经发送成功
		sendTime++;															// 发送次数加1
		System.out.println("for发送数据结束。。。");
		return System.currentTimeMillis();									// 最后一个数据包发送完成后返回一个发送完成的时间
	}
	
	/**
	 * 	 发送数据
	 * 
	 * 	@param message	发送的数据
	 * 	@param replyQueueName	回调队列
	 * 	@param channel		信道
	 * 	@param consumer		
	 * 	QueueingConsumer consumer = new QueueingConsumer(channel);
	 * 	@return				响应信息
	 * 	@throws Exception	
	 */
	private static void call(String message, 
			String replyQueueName, Channel channel, 
			QueueingConsumer consumer,
			String exchangeName, String routingkey) 
					throws Exception {     
        BasicProperties props = new BasicProperties							//封装replyQueue属性（回调队列，用来接收响应）
                                    .Builder()
                                    .replyTo(replyQueueName)
                                    .build();
        channel.basicPublish(exchangeName, 
        		routingkey, props, message.getBytes("utf-8"));				 //发送消息，并加上之前封装好的属性replyTo=响应（回调）队列
    }
	
	/**
	 * 	获取包的json字符串
	 * 	@param bytes		每个分段的字节内容
	 * 	@param fileMD5	整个文件的MD5值
	 * 	@param count		整个文件的包数
	 * 	@param pkSerial	包序号（当前是第几个包）
	 * 	@return	String	包的json字符串
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
		map.put("packid", fileMD5);											// 用文件的md5值作为包的id值
		map.put("packcount", count);										//本次上传的整个包被分成多少个小包
		if(pkSerial==0) {
			map.put("flag", 0);
		}else if(pkSerial == count-1) {
			map.put("flag", 2);
		}else {
			map.put("flag", 1);
		}
		map.put("md5", md5);												// 当前包数据的md5
		map.put("packnum", pkSerial);										// 当前包序号
		map.put("date", System.currentTimeMillis());						// 发送包的时间
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
	 * 接收文件
	 * @param path
	 * @throws Exception
	 * @{@link Return} 保存文件的路径
	 */
	@Override
	public void recv(String path,String fileName) throws Exception {
		// 建立一个新的链接池
				ConnectionFactory factory = new ConnectionFactory();
				// 链接地址
				factory.setHost("10.10.10.14");
				// 链接名字
				factory.setUsername("yduser");
				// 链接密码
				factory.setPassword("yd@user");
				// 链接的虚拟机
				factory.setVirtualHost("ydkpbmp");
				// 建立一个新的链接
				Connection connection = factory.newConnection();
				// 建立一个队列
				Channel channel = connection.createChannel();
				// 声明交换器
				String exchangeName = "myexchanges05";
				channel.exchangeDeclare(exchangeName, "direct", true);
				// 声明队列
				String queueName = channel.queueDeclare().getQueue();
				// 声明routing-key
				String routingKey = "myroutingkey05";
				// 绑定队列，通过键 routingKey 将队列和交换器绑定起来
				channel.queueBind(queueName, exchangeName, routingKey);

				// 定义json解析
				Gson gson = new Gson();
				// 将返回的消息封装在hashmap中
				Map<String, Object> hashmap = new HashMap<>();

				
				boolean autoAck = false;// 创建单一文件的标志
				
				channel.basicConsume(queueName, autoAck, new DefaultConsumer(channel) {
					
					// 计算接收到的包的数量的变量
					double i = 0.0;
					
					//新建文件的标志，默认为新建文件开始
					boolean BulidFileTag = true;

					File filename = null;

					
					
					//程序开始时间
					//long starTime=System.currentTimeMillis();
					
					int n=0;

					@Override
					public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
							byte[] body) throws IOException {
						
						
						
						i++;
						
						System.out.println("接收到的包的数量====》" + i);
						
						BasicProperties property = properties;
						BasicProperties replyProps = new BasicProperties.Builder().build();
						// 接收消息
						String bodyStr = new String(body, "UTF-8");
						
						
						// 将接收的消息封装在map中
						Map<String, Object> map = new HashMap<>();
						// 解析json字符串
						map = gson.fromJson(bodyStr, new TypeToken<HashMap<String, Object>>() {
						}.getType());
						String m = (String) map.get("md5");
						byte[] bytes = EncryptUtil.decodeByteByBase64((String) map.get("data"));

						
						
						if (BulidFileTag) {
							filenameTemp = path + (String) map.get("fileName") + "." + (String) map.get("ext");
							filename = new File(filenameTemp);
							//判断文件夹是否存在重复名的文件
							if (filename.exists() && filename.isFile()) {
								n=n+1;
								//filename.delete();
								filenameTemp = path + (String) map.get("fileName") + n + "." + (String) map.get("ext");
								filename = new File(filenameTemp);
							}
							//新建文件结束
							BulidFileTag = false;
						}
						
						try {
							//对单个包进行MD5加密
							String datamd5 = DigestUtils.md5Hex(bytes);
							//System.out.println("单个包的md5加密=====》" + datamd5);
							RandomAccessFile randomFile=new RandomAccessFile(filename, "rw");
							// 验证每个包的md5是否与原始的md5相同
							if (datamd5.equals(m)) {
								System.out.println("单个包校验通过......");
								
								FileUtils.writeToFile(randomFile, bytes, (int) ((double) map.get("packnum")),  (long) ((double) map.get("packSize")));
							}
							
							//判断接收到的包的数量是否与发送包的数量一致
							
							if (i == (double) map.get("packcount")||(double) map.get("flag")==2.0) {
								
								randomFile.close();
								
								i=0.0;
								
								System.out.println("已经接收到所有的包......");
								FileInputStream downloadmd5 = new FileInputStream(filename);
								String filemd5 = DigestUtils.md5Hex(downloadmd5);
								//System.out.println("整个文件的加密结果为====》" + filemd5);
								downloadmd5.close();
								
								// 验证整个包的md5是否与原始的MD5相同
								if (filemd5.equals((String) map.get("allMD5"))) {
									System.out.println("整个包的MD5校验通过......");
									hashmap.put("status", "0");
									hashmap.put("packid", (String) map.get("packid"));
									// 如果文件路径所对应的文件存在、文件内容和md5校验成功
									String information = gson.toJson(hashmap);
									String response = null;
									response = information;
									
									//消息回复队列
									channel.basicPublish("", property.getReplyTo(), replyProps, response.getBytes("UTF-8"));
									// 返回消息确认信息
									channel.basicAck(envelope.getDeliveryTag(), false);
									
									//清理内存
									map.clear();
									hashmap.clear();
									//程序结束时间
									//long endTime=System.currentTimeMillis();
									
									//long time=endTime-starTime;
									
									//System.out.println("===============>程序的运行时间为=====》"+time);
									
									
								} else {
									map.clear();
									hashmap.clear();
									if (filename.delete()) {
										System.out.println("文件删除成功");
									} else {
										System.out.println("文件删除失败");
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}finally {
							System.out.println("已经接受全部文件......");
						}
					}

				});
	}
	
	
	/**
	 * 
	 *	 获取一个链接
	 * 
	 * 	@param host	主机ip
	 * 	@param port	链接端口
	 * 	@param userName	链接用户名
	 * 	@param password	链接密码
	 * 	@param vhost		虚拟主机
	 * 	@return			Connection创建的链接
	 */
	private static Connection getConnection(String host, 
			int port, String userName, String password, 
			String vhost) {
		ConnectionFactory factory = new ConnectionFactory();				//创建链接工厂
		Connection connection = null;
		factory.setUsername(userName);										// 设置用户名密码
		factory.setPassword(password);
		factory.setVirtualHost(vhost);
		factory.setHost(host);												// 设置rabbitMq服务器地址
		factory.setPort(port);
		try {
			connection = factory.newConnection();							// 建立到服务器的链接
			return connection;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return null;
	}
}
