package org.lwt.multipart;

import java.io.File;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface UpLoadFile {
	/**
	 * 上传文件对象
	 * 
	 * 
	 * @param file 	指定要上传的文件File对象
	 */
	void sendData(@WebParam(name="file") String file) throws Exception;
	
	void receiver(@WebParam(name="file") String file) throws Exception;
	
}
