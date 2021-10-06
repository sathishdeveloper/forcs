package com.cs.eventproj.api;

/**
 * Constants
 *
 * @author sathish
 */
public interface EventConstant {

	String SRC_FILE = "SRC_FILE";
	String STARTED = "STARTED";
	String FINISHED = "FINISHED";
	String TABLE_NAME = "LOGEVENTS";
	String PROP_QUEUE_SIZE = "processQueueSize";
	
	/**
	 * Property for - How long should the read data from source to be put on 
	 * wait when the queue is full. value must be in seconds
	 */
	String QUEUE_TOLERANCE = "toleranceInSecs";
}
