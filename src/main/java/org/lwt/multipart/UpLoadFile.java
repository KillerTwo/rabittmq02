package org.lwt.multipart;

import java.io.File;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface UpLoadFile {
	/**
	 * 	上传文件对象
	 * 
	 * 
	 * @param file 	指定要上传的文件File对象
	 */
	boolean sendData(@WebParam(name="file") String file, String fileName) throws Exception;
	
	void receiver(@WebParam(name="file") String file, String fileName) throws Exception;
	
	void recv(String path, String fileName) throws Exception;
}
