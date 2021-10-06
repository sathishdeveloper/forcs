package com.cs.eventproj.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs.eventproj.api.EventConstant;
import com.cs.eventproj.api.ReaderApi;
import com.cs.eventproj.exception.EnvironmentException;
import com.cs.eventproj.model.SourceEventModel;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * File based log reader implementation class
 * 
 * @author sathish
 */
public class LogFileReader implements ReaderApi{

	private static final Logger LOG = LoggerFactory.getLogger(LogFileReader.class);
	private static final String LINE_END = "}";
	private BlockingQueue<SourceEventModel> queue;
	
	public LogFileReader(BlockingQueue<SourceEventModel> queue) {
		this.queue = queue;
	}
	
	/**
	 * Reads log source data from file and queues the parsed contents to a buffer
	 * queue for downstream log process.
	 */
	@Override
	public void read() {
		// Read from a pre-configured source file (should have filled from program argument)
		String path = System.getProperty(EventConstant.SRC_FILE);
		
		/**
		 * After data read from source, the queue will get added with the data for further processing.
		 * Upon the queue is full (might caused from result processing latency), how long 
		 * this read + add process should wait - is configured as 'queueTolerence.
		 * Value is in seconds.
		 */
		final int queueTolerance = Integer.valueOf(System.getProperty(EventConstant.QUEUE_TOLERANCE, "0"));
		
		// Validation
		if(path == null || path.isBlank()) {
			throw new EnvironmentException("path not configured");
		}
		
		// Data read from source begins
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(path))){
    		List<String> lines = new LinkedList<>();
    		String line = null;
    		StringBuffer temp = new StringBuffer();

    		// Read line by line
    		while((line = reader.readLine()) != null) {
    			if(line.endsWith(LINE_END)) {
    				if(!temp.isEmpty()) {
    					lines.add(temp.toString() + line);
    					temp = new StringBuffer();
    				} else {
    					lines.add(line);
    				}
    			} else {
    				temp.append(line);
    			}
    		} // end of while-loop

    		// Transform the read lines to object model. Place each data model (each row) for downstream process
    		for(String s: lines) {
    			SourceEventModel m = new ObjectMapper().readValue(s, SourceEventModel.class);
    			LOG.trace("Placed: {}", m);
    			if(!queue.offer(m, queueTolerance, TimeUnit.SECONDS)) {
    				// The performance impact caused at downstream, so stop all the further processing and emit error
    				queue.clear();
    				LOG.error("Data read from file, but unable to pass to downstream, - {}", m.getId());
    				return;
    			}
    		} // end of for-loop
    	} catch (IOException | InterruptedException e) {
			LOG.error("Error while reading the source file.", e);
		} finally {
			// All done, place an invalid event for stop signal
    		SourceEventModel dummy = new SourceEventModel();
    		dummy.setId(null);
    		try {
				queue.put(dummy);
			} catch (InterruptedException e) {
				LOG.warn("Signal for shutting down the processor queue ended in error, {}", e.getMessage());
			}
		}
	}

}
