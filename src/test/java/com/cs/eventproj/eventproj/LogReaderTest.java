package com.cs.eventproj.eventproj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;

import com.cs.eventproj.api.EventConstant;
import com.cs.eventproj.api.ReaderApi;
import com.cs.eventproj.exception.EnvironmentException;
import com.cs.eventproj.model.SourceEventModel;
import com.cs.eventproj.service.LogFileReader;

/**
 * Unit test for {@link LogFileReader}.
 */
public class LogReaderTest {

    @Test
    public void shouldReadContents() {
    	File file = new File("src/test/data/logfile.txt");
    	BlockingQueue<SourceEventModel> queue = new ArrayBlockingQueue<>(10);
    	System.out.println(file.getAbsolutePath());
    	System.setProperty(EventConstant.SRC_FILE, file.getAbsolutePath());
        ReaderApi target = new LogFileReader(queue);
        target.read();
        
        // 6 rows of data from source file (+) one dummy data for stop signal
        assertTrue("All file contents are not read", queue.size() >= 6);
    }
    
    @Test(expected = EnvironmentException.class)
    public void shouldThrowError() {
    	ReaderApi target = new LogFileReader(new ArrayBlockingQueue<>(1));
    	// Invoking 'read' method without filling the 'SRC_FILE' property should yield error.
    	target.read();
    }
    
    @Test
    public void shouldThrowErrorForEmptySource() {
    	File file = new File("src/test/data/emptysrc.txt");
    	BlockingQueue<SourceEventModel> queue = new ArrayBlockingQueue<>(1);
    	System.out.println(file.getAbsolutePath());
    	System.setProperty(EventConstant.SRC_FILE, file.getAbsolutePath());
        ReaderApi target = new LogFileReader(queue);
        target.read();
        
        // the queue should have only one 'dummy' event for stop signal
        assertTrue("Expected error result not noticed, queue size differs", queue.size() == 1);
    }
    
    @Test
    public void shouldEndForLatencyProcess() throws InterruptedException {
    	// Make the queue to allow only 'one' data to hold
    	BlockingQueue<SourceEventModel> queue = new ArrayBlockingQueue<>(1);
    	
    	/**
    	 * The source 'logfile.txt' have 6 rows of data, the queue would be filled with 
    	 * first row and do not 'take' the single content from the 'queue' 
    	 */
    	ReaderApi target = new LogFileReader(queue);
        target.read();
        Thread.sleep(500);
        /**
         * Default queue 'wait' time is '0', should throw the error immediately
         * 
         * Upon error thrown, the single data in the queue would be cleared, 
         * only one 'dummy' event data would be filled up for stop signal
         */
        assertEquals("Expected error result not noticed, queue size differs", queue.size(), 1);
        SourceEventModel resultData = null;
        assertNotNull("Stop signal event data not observed", (resultData = queue.take()));
        assertNull("Dummy event id not noticed", resultData.getId());
    }
}
