package org.lwt.multipart;

import java.io.File;

import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * �����ļ����浽ָ��·��
 * 
 * @author lwt27
 *
 */
@WebService
public interface Receiver {
	/**
	 * �����ļ����浽fileָ����·����
	 * @param file	��Ž����ļ���·��
	 * @throws Exception
	 */
	void receiver(@WebParam(name="file") File file) throws Exception;
}
