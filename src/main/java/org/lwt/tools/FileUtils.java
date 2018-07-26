package org.lwt.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.SequenceInputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class FileUtils {
	
	
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
		String fileName = "test.txt";
		String[] fileNames = fileName.split("\\.");
		System.out.println(fileNames[0]);
		/*List<byte[]> bytes = splitDemo(file);
		
		System.out.println(bytes.size());
		System.out.println(System.currentTimeMillis());
		System.out.println(System.currentTimeMillis());
		System.out.println(System.currentTimeMillis());
		
		Date date = new Date();
		System.err.println(date.getTime());
		date = new Date();
		System.err.println(date.getTime());
		date = new Date();
		System.err.println(date.getTime());*/
		
		
		
	}
	
	/**
	 * 
	 * 分割文件
	 * 
	 * 返回一个包含字节数组的list
	 * @throws IOException
	 */
	public static List<byte[]> splitDemo(File sourceFile)throws IOException
    {
		/*String path = FileUtils.class.getClassLoader().getResource("").getPath();
		path = path.substring(1, path.length());*/
		/*****************************************/
        FileInputStream fis = new FileInputStream(sourceFile);
        //FileOutputStream fos = null;	//要在循环内部创建FileOutputStream对象
        byte[] buf = new byte[1024];	//将文件分割成1k大小的碎片
        //int len,count = 0;
        int len;
        List<byte[]> packList = new ArrayList<>();
        while((len=fis.read(buf))!=-1)
        {
        	//将每一个小文件存储到磁盘
            /*fos = new FileOutputStream(path+(count++)+".part");
            fos.write(buf,0,len);
            fos.flush();
            fos.close();*/
        	byte[] temp = new byte[buf.length];
        	temp = Arrays.copyOf(buf, len);
            packList.add(temp);
        }
        fis.close();
        return packList;
    }
	/**
	 * 合并文件
	 * 
	 * 
	 * @throws IOException
	 */
	public static void sequenceDemo()throws IOException
    {
        FileInputStream fis = null;
        FileOutputStream fos = new FileOutputStream("2.avi");
        ArrayList<FileInputStream> al = new ArrayList<FileInputStream>();//Vector效率低 
        int count = 0;
        File dir = new File("split");//利用File遍历文件夹下的文件
        File[] files = dir.listFiles();
        for(int x=0;x<files.length;x++)
        {
            al.add(new FileInputStream(files[x]));
        }
        final Iterator<FileInputStream> it = al.iterator();//ArrayList本身没有枚举方法，通过迭代器来实现
        Enumeration<FileInputStream>  en= new Enumeration<FileInputStream>()//匿名内部类，复写枚举接口下的两个方法
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
        byte[] buf = new byte[1024*1024];	//定义1M的缓存区
        while((count=sis.read(buf))!=-1)
        {
            fos.write(buf,0,count);
        }
        sis.close();
        fos.close();
    }
	
	/**
	 * 获取文件大小
	 * 
	 * 
	 * @param file需要获取的文件的大小
	 * @return	long 文件大小
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
	 * 写入文件（以追加的方式写入）
	 * @param file		目标文件file对象
	 * @param bytes 	待写入的文件字节数组
	 */
	public static void write2File(File file, byte[] bytes) {   
        RandomAccessFile randomFile = null;  
        try {     
            // 打开一个随机访问文件流，按读写方式     
            randomFile = new RandomAccessFile(file, "rw");     
            // 文件长度，字节数     
            long fileLength = randomFile.length();     
            // 将写文件指针移到文件尾。     
            randomFile.seek(fileLength);
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
	
}
