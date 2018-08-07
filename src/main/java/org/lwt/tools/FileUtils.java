package org.lwt.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.SequenceInputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
/**
 * 	文件操作类
 * 	@author Administrator
 *
 */
public class FileUtils {
	
	private static MessageDigest mMessageDigest = null;
	public static void main(String[] args) throws Exception {
		
		/*String path = FileUtils.class.getClassLoader().getResource("").getPath();
		path = path.substring(1, path.length());
		File file = new File(path+"test01.txt");
		OutputStream out = new FileOutputStream(file);
		//byte[] bytes = new byte[1024];
		out.write("hello".getBytes());
		out.flush();
		out.close();
		System.out.println(path);*/
		
		/*String path = FileUtils.class.getClassLoader().getResource("text.txt").getPath();
		path = path.substring(1, path.length());
		System.out.println(path);
		File file = new File(path);
		
		splitDemo(file);*/
		/*String path = FileUtils.class.getClassLoader().getResource("text.txt").getPath();
		path = path.substring(1, path.length());
		System.out.println(path);
		File file = new File(path);
		byte[] bytess = new byte[1024];
		FileInputStream in = new FileInputStream(file);
		int len;
		
		while(-1 != (len = in.read(bytess, 0, bytess.length))) {
			String md5 = TestTools.getMD5String(bytess);
			System.out.println(md5);
			break;
		}*/
		/*String fileName = "test.txt";
		String[] fileNames = fileName.split("\\.");
		System.out.println(fileNames[0]);*/
		/*List<byte[]> bytes = splitDemo(file);
		
		System.out.println(bytes.size());
		System.out.println(System.currentTimeMillis());
		System.out.println(System.currentTimeMillis());
		System.out.println(System.currentTimeMillis());
		string[] strs = 
		Date date = new Date();
		System.err.println(date.getTime());
		date = new Date();
		System.err.println(date.getTime());
		date = new Date();
		System.err.println(date.getTime());*/
		
		/*String path = "C:\\Users\\Administrator\\Documents\\test\\list.rar";
		File file = new File(path);*/
		//String md5 = EncryptUtil.getFileMD5(file);
		/*String md5 = getFileMD5String(file);
		System.out.println("md5== "+md5);*/
		/*String str = hello(file);
		System.out.println(str);*/
		//System.out.println(md5);
		/*System.out.println(file.hashCode());*/
		/*if (file.delete()) {
			System.out.println("删除文件成功...");
		}*/
		
		/*byte[] bytess = new byte[1024];
		FileInputStream in = new FileInputStream(file);
		int len;
		File file01 = new File("C:\\Users\\Administrator\\Documents\\test\\recv\\list.rar");*/
		/*FileOutputStream out = new FileOutputStream(file01);
		while(-1 != (len = in.read(bytess, 0, bytess.length))) {
			//String md5 = TestTools.getMD5String(bytess);
			//System.out.println(md5);
			//break;
			//String sigleMd5 = EncryptUtil.getMD5String(bytess);
			//System.out.println("sigleMd5");
			out.write(bytess);
		}
		System.out.println("文件传输完成。");*/
		//String md501 = getFileMD5String(file01);
		//String md501 = EncryptUtil.getFileMD5(file01);
		//System.out.println("md501== "+ md501);
		
		/*String[] strs = new String[1024];
		for(int i = 0; i < 6; i++) {
			strs[i] = ""+i*10;
		}
		System.out.println(strs.length);*/
		String path = "C:\\Users\\Administrator\\Documents\\test\\test03.txt";
		String path01 = "E:\\package\\test03.txt";
		File file01 = new File("C:\\Users\\Administrator\\Documents\\test\\recv\\mysql.zip");
		
		File file = new File(path);
		
		FileInputStream fi = new FileInputStream(file);
		byte[] buffer = new byte[(int) file.length()];
		System.out.println(file.length());
		File file02 = bytes2File(buffer,path01, "test03.txt");
		System.out.println(file02.length());
		/*FileInputStream in = new FileInputStream(file);
		ByteBuffer[] buffers = ByteBuffer.allocate(capacity);*/
		
		/*String md51 = DigestUtils.md5Hex(new FileInputStream(file));
		List<byte[]> list = splitDemo(file);
		RandomAccessFile randomFile = new RandomAccessFile(file01,"rw");
		FileChannel inChannel = new RandomAccessFile(file, "r").getChannel();
		FileChannel outChannel = randomFile.getChannel();
		
		System.err.println("new18 ...");
		int i = list.size()-1;
		
		long startTime = System.currentTimeMillis();
		while(i >= 0) {
			writeToFile(randomFile, list.get(i), i, 1024);
			i--;
		}
		randomFile.close();
		try(FileChannel in = new FileInputStream(file).getChannel();
				FileChannel out = new FileOutputStream(file01).getChannel()){
			MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
			out.write(buf);
			buf.force();			// 强制写入内存
			System.out.println("文件写入完成。。。");
			in.close();
			clean(buf);
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		long endTime = System.currentTimeMillis();
		System.out.println("写入文件花费的时间是："+(endTime-startTime));	
			
		
		for (byte[] bs : list) {
			
			write2File(file01, bs, i, 1024);
			i++;
			
			String asB64 = Base64.getEncoder().encodeToString(bs);
			//System.err.println("原加密 ： "+asB64);
			byte[] asBytes = Base64.getDecoder().decode(asB64); 
			//System.out.println("原字节："+bs);
			System.out.println("转换后字节："+asBytes);
			if(bs.equals(bs)) {
				System.err.println(true);
			}else {
				System.err.println(false);
			}
			String s1 = new String(bs);
			//System.err.println("[==] "+s1);
			Map<String, Object> map = new HashMap<>();
			//System.err.println("++++ "+bs);
			//System.err.println("]---[ "+s1.getBytes("utf-8"));
			if(s1.getBytes("utf-8").equals(bs)) {
				System.err.println(true);
			}else {
				System.err.println(false);
			}
			//String encodeStr = EncryptUtil.encodeByBase64(new String(bs,"utf-8"));
			//map.put("data", new String(bs,"utf-8"));
			map.put("data", asB64);
			
			map.put("id", i);
			
			//System.out.println("发送的map: "+map);
			//System.out.println(new String(bs,"utf-8"));
			String jsonStr = JsonUtil.getJsonFromMap(map);
			//System.out.println("jsonStr= "+jsonStr);
			
			System.out.println("发送的json: "+jsonStr);
			
			byte[] bytes = jsonStr.getBytes("utf-8");
			
			
			
			String resStr = new String(bytes,"utf-8");
			
			System.out.println("接收的json: "+jsonStr);
			Map<String, Object> resMap = JsonUtil.getMapFromJson(resStr);
			//Map<String, Object> resMap = JsonUtil.getMapFromJson(jsonStr);
			//System.err.println("接收到的map: "+resMap);
			System.out.println("接收到的map: "+resMap);
			String sourceData = (String) resMap.get("data");
			System.err.println("==>"+sourceData);
			System.err.println("==>||"+new String(EncryptUtil.decodeByBase64(sourceData)));
			byte[] data = EncryptUtil.decodeByBase64(sourceData).getBytes("utf-8");
			//System.out.println("接收的data："+data);
			//System.err.println("== "+jsonStr);
			//Map<String, Object> resMap = JsonUtil.getMapFromJson(jsonStr);
			//String sourceData = (String) resMap.get("data");
			//byte[] data =  sourceData.getBytes("utf-8");
			String sourceData = (String) map.get("data");
			byte[] data =  sourceData.getBytes("utf-8");
			write2File(file01, (byte[])map.get("data"));
			System.out.println(((String) resMap.get("data")).getBytes());
			write2File(file01, (byte[])resMap.get("data"));
			//write2File(file01, asBytes);
			//writeToFile(data,file01);
			
			String sourceData = (String) resMap.get("data");
			write2File(file01, Base64.getDecoder().decode(sourceData));
			
			String sourceData = (String) map.get("data");
			System.out.println(Base64.getDecoder().decode(sourceData));
			write2File(file01, Base64.getDecoder().decode(sourceData));
			i++;
		}
		
		System.out.println(list.size());
		for (byte[] bs : list) {
			write2File(file01, bs);
		}
		String md52 = DigestUtils.md5Hex(new FileInputStream(file01));
		System.out.println("md51= "+md51);
		System.out.println("md52= "+md52);*/
		/*String str = "{\"age\":\"24\",\"name\":\"cool_summer_moon\"}";
	    JSONObject  jsonObject = JSONObject.parseObject(str);
	    //json对象转Map
	    Map<String,Object> map = (Map<String,Object>)jsonObject;
	    System.out.println("map对象是：" + map);
	    Object object = map.get("age");
	    System.out.println("age的值是"+object);
	    System.out.println(map);*/
		//org.apache.commons.io.FileUtils
		
	}
	/**
	 * 	关闭MappedByteBUffer
	 * 	@param buffer
	 */
	private static void clean(final MappedByteBuffer buffer) {
		if (buffer == null) {
			return;
		}
		buffer.force();
		AccessController.doPrivileged(new PrivilegedAction<Object>() {								//Privileged特权
			@Override
			public Object run() {
				try {
					// System.out.println(buffer.getClass().getName());
					Method getCleanerMethod = buffer.getClass().
							getMethod("cleaner", new Class[0]);
					getCleanerMethod.setAccessible(true);
					sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.
							invoke(buffer, new Object[0]);
					cleaner.clean();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}

	/**
	 * 	自定义获取md5值
	 * 	@param file
	 * 	@return 指定文件的md5值
	 */
	public static String getFileMD5String(File file) {
		 try {
		        mMessageDigest = MessageDigest.getInstance("MD5");
		    } catch (NoSuchAlgorithmException e) {
		        e.printStackTrace();
		}
        try {
            InputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) > 0) {
                mMessageDigest.update(buffer, 0, length);
            }
            fis.close();
            return new BigInteger(1, mMessageDigest.digest()).toString(16);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
	}
	
	/**
	 * 	自定义获取md5值
	 * 	@param file
	 * 	@return 加密后的字符串
	 */
	public static String getFileMD5String(List<byte[]> bytes) {
		 try {
		        mMessageDigest = MessageDigest.getInstance("MD5");
		    } catch (NoSuchAlgorithmException e) {
		        e.printStackTrace();
		}
        try {
            for (byte[] bs : bytes) {
            	mMessageDigest.update(bs);
			}
            return new BigInteger(1, mMessageDigest.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
	}
	
	/****************************************/
	public static String hello(File file) {
		System.out.println("hello");
		FileInputStream in = null;
		FileChannel ch = null;
		try {
			in = new FileInputStream(file);
			ch = in.getChannel();
			return "try";
		} catch (FileNotFoundException e) {
			return "";
			
		}finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 
	 * 	分割文件
	 * 
	 * 	返回一个包含字节数组的list
	 * 	@throws IOException
	 */
	public static List<byte[]> splitDemo(File sourceFile)
			throws IOException
    {
		
		/*****************************************/
        FileInputStream fis = new FileInputStream(sourceFile);
        byte[] buf = new byte[1024];										//将文件分割成1k大小的碎片
        List<byte[]> packList = new ArrayList<>();
        
       
        
        int len;
        while((len=fis.read(buf,0,buf.length))!=-1)
        {
        	byte[] temp = new byte[len];
        	temp = Arrays.copyOf(buf, len);
            packList.add(temp);
        }
        
        /*int i = 0;
        int size = (int) inChannel.size();
        int count = 0;
        System.out.println("文件长度 "+size);
        System.out.println("多少个数组："+size/1024);
        byte[] list = new byte[1024];
        while(mapped.hasRemaining()) {
        	count++;
        	if(i >= 1024 || count >= size) {
        		byte[] temp = new byte[i];
            	temp = Arrays.copyOf(list, i);
                packList.add(temp);
        		i=0;
        		continue;
        	}
        	//System.out.println(i);
        	list[i] = mapped.get();
        	i++;
        }
        System.out.println("列表数量："+packList.size());*/
        fis.close();
        //clean(mapped); 		//关闭mappedbytebuffer
        return packList;
    }
	
	/**
	 * 	合并文件
	 * 
	 * 
	 * 	@throws IOException
	 */
	public static void sequenceDemo()throws IOException
    {
        FileInputStream fis = null;
        FileOutputStream fos = new FileOutputStream("2.avi");
        ArrayList<FileInputStream> al = new ArrayList<FileInputStream>();	//Vector效率低 
        int count = 0;
        File dir = new File("split");										//利用File遍历文件夹下的文件
        File[] files = dir.listFiles();
        for(int x=0;x<files.length;x++)
        {
            al.add(new FileInputStream(files[x]));
        }
        final Iterator<FileInputStream> it = al.iterator();					//ArrayList本身没有枚举方法，通过迭代器来实现
        Enumeration<FileInputStream>  en= 
        		new Enumeration<FileInputStream>()							//匿名内部类，复写枚举接口下的两个方法
        {
            public boolean hasMoreElements(){
                return it.hasNext();
            }
            public FileInputStream nextElement()
            {
                return it.next();
            }
            
        };
        SequenceInputStream sis = new SequenceInputStream(en);
        byte[] buf = new byte[1024*1024];									//定义1M的缓存区
        while((count=sis.read(buf))!=-1)
        {
            fos.write(buf,0,count);
        }
        sis.close();
        fos.close();
    }
	
	/**
	 * 	获取文件大小
	 * 
	 * 
	 * 	@param file需要获取的文件的大小
	 * 	@return	long 文件大小
	 */
	public static long getFileSize(File file) {
		FileChannel fileChannel = null;
		FileInputStream in = null;
		if(file.exists() && file.isFile()) {
			try {
				in = new FileInputStream(file);
				
				fileChannel = in.getChannel();
				
				return fileChannel.size();
			} catch (Exception e) {
				
				e.printStackTrace();
			}finally {
				if(in != null) {
					try {
						in.close();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
				}
			}
		}
		return -1;
	}
	
	/**
	 * 	写入文件（以追加的方式写入）
	 * 	@param file		目标文件file对象
	 * 	@param bytes 	待写入的文件字节数组
	 */
	public static void write2File(File file, byte[] bytes) {   
        RandomAccessFile randomFile = null;  
        try {     
            randomFile = new RandomAccessFile(file, "rw");     				// 打开一个随机访问文件流，按读写方式     
            long fileLength = randomFile.length();     						// 文件长度，字节数 
            randomFile.seek(fileLength);									// 将写文件指针移到文件尾。     
            randomFile.write(bytes);
            //randomFile.writeBytes(content);      
        } catch (IOException e) {     
            e.printStackTrace();     
        } finally{  
            if(randomFile != null){  
                try {  
                    randomFile.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
    }
	
	/**
	 * 	写入文件（以追加的方式写入），将文件写入到指定的位置
	 * 	@param file		目标文件file对象
	 * 	@param bytes 	待写入的文件字节数组
	 * 	@param packnum	当前是第几个包，从0开始计数
	 * 	@param packSize	每个包的大小，单位为字节
	 */
	public static void write2File(File file, byte[] bytes, 
			int packnum, long packSize) {   
        RandomAccessFile randomFile = null; 
        long filePointer = 0;
        try {     
                 
            randomFile = new RandomAccessFile(file, "rw");    				// 打开一个随机访问文件流，按读写方式 
            filePointer = packnum * packSize;
            
            randomFile.seek(filePointer);									// 将写文件指针移到filePointer为进行文件写入。     
            randomFile.write(bytes);
        } catch (IOException e) {     
            e.printStackTrace();     
        } finally{  
            if(randomFile != null){  
                try {  
                    randomFile.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
    }
	
	/**
	 * 	写入文件（以追加的方式写入），将文件写入到指定的位置
	 * 	@param randomFile		目标文件RandomAccessFile对象
	 * 	@param bytes 	待写入的文件字节数组
	 * 	@param packnum	当前是第几个包，从0开始计数
	 * 	@param packSize	每个包的大小，单位为字节
	 */
	public static void writeToFile(RandomAccessFile randomFile,
			byte[] bytes, int packnum, long packSize) {   
        FileChannel channel = null;
        long filePointer = 0;
        try {     
            channel = randomFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            buffer.put(bytes);
            buffer.flip();
            filePointer = packnum * packSize;
            
            randomFile.seek(filePointer);									// 将写文件指针移到filePointer为进行文件写入。     	
            channel.write(buffer);
        } catch (IOException e) {     
            e.printStackTrace();     
        } finally{
        	if(channel != null) {
        		try {
					//channel.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
        }  
    }
	
	/**
	 * 	写入文件
	 * @param sourceFile	源文件File对象
	 * @param targetFile	目标文件File对象
	 */
	public static void writeToFile(File sourceFile, File targetFile) {
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		try {
			in = new BufferedInputStream(new FileInputStream(sourceFile));
	        out = new BufferedOutputStream(new FileOutputStream(targetFile));
	        byte[] bytes = new byte[1024];
	        int n = -1;
	        while ((n = in.read(bytes,0,bytes.length)) != -1) {
	            String str = new String(bytes,0,n,"utf-8");
	            out.write(bytes, 0, n);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
	        try {
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param bytes		源文件的字节数组
	 * @param targetFile	目标文件File对象
	 */
	public static void writeToFile(byte[] bytes, File targetFile) {
		BufferedOutputStream out = null;
		try {
	        out = new BufferedOutputStream(new FileOutputStream(targetFile));
	        out.write(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			
	        try {
				out.flush();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
	        try {
				out.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
	}
	/**
	 * 将字节数组转换为File对象
	 * @param bytes
	 * @return File对象
	 */
	public static File bytes2File(byte[] bytes, String path, String fileName) {
		File file = new File(path+fileName);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		OutputStream output = null;
		BufferedOutputStream bufferedOutput = null;
		try {
			output = new FileOutputStream(file);
			bufferedOutput = new BufferedOutputStream(output);
			bufferedOutput.write(bytes);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(bufferedOutput != null) {
					bufferedOutput.close();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(output != null) {
					output.close();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return file;

		
	}
	
	
}
