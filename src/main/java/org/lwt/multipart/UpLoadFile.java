package org.lwt.multipart;

import java.io.File;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface UpLoadFile {
	/**
	 * 	�ϴ��ļ�����
	 * 
	 * 
	 * @param file 	ָ��Ҫ�ϴ����ļ�File����
	 */
	boolean sendData(@WebParam(name="file") String file, String fileName) throws Exception;
	
	void receiver(@WebParam(name="file") String file, String fileName) throws Exception;
	
	void recv(String path, String fileName) throws Exception;
}
