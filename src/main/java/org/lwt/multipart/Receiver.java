package org.lwt.multipart;

import java.io.File;

import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * 接收文件并存到指定路径
 * 
 * @author lwt27
 *
 */
@WebService
public interface Receiver {
	/**
	 * 接收文件并存到file指定的路径下
	 * @param file	存放接收文件的路径
	 * @throws Exception
	 */
	void receiver(@WebParam(name="file") File file) throws Exception;
}
