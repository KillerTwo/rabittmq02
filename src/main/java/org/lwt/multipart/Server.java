package org.lwt.multipart;

import javax.xml.ws.Endpoint;

/**
 * 发布服务
 * @author lwt27
 *
 */
public class Server {
	public Server() {
		UpLoadFile upLoadFile = new UploadFileImpl();
		String address = "http://10.10.10.231:8702/upload";
		Endpoint.publish(address, upLoadFile);
		/*String receiverAddress = "http://127.0.0.1:8709/recv";
		Receiver receiver = new ReceiverImpl();
		Endpoint.publish(receiverAddress, receiver);*/
		System.out.println("服务启动成功，通过http://10.10.10.14:8702/upload访问服务...");
	}
	
	public static void main(String[] args) {
		// 发布服务
		new Server();
	}
}
