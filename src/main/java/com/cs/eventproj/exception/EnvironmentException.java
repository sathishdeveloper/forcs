package com.cs.eventproj.exception;

/**
 * Application {@link RuntimeException} that reports environment related issues
 * 
 * @author sathish
 */
public class EnvironmentException  extends RuntimeException{
	
	private static final long serialVersionUID = 1612507527234758673L;

	public EnvironmentException(String causeMessage) {
		super(causeMessage);
	}

}
