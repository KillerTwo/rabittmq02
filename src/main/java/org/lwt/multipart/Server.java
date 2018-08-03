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
		String address = "http://127.0.0.1:8702/upload";
		Endpoint.publish(address, upLoadFile);

		/*String receiverAddress = "http://127.0.0.1:8709/recv";
		Receiver receiver = new ReceiverImpl();
		Endpoint.publish(receiverAddress, receiver);*/
	}
	
	public static void main(String[] args) {
		// 发布服务
		new Server();
	}
}
