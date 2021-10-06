package com.cs.eventproj.exception;

/**
 * Invalid operation exception
 * 
 * @author sathish
 */
public class InvalidOperateException extends RuntimeException {

	private static final long serialVersionUID = -6735956477574744824L;

	public InvalidOperateException(String causeMessage) {
		super(causeMessage);
	}

}
