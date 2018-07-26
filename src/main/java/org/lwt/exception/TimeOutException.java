package org.lwt.exception;

public class TimeOutException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public TimeOutException() {
		super();
	}
	public TimeOutException(String msg) {
		super(msg);
	}

}
