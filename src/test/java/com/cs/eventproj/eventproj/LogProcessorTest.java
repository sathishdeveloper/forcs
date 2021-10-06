package com.cs.eventproj.eventproj;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.cs.eventproj.api.EventConstant;
import com.cs.eventproj.api.WriterApi;
import com.cs.eventproj.model.ResultEventModel;
import com.cs.eventproj.model.SourceEventModel;
import com.cs.eventproj.service.FileDbResultWriter;
import com.cs.eventproj.service.LogProcessor;
import com.cs.eventproj.service.LogServiceFactory;

/**
 * Test for {@link LogProcessor}
 * 
 * @author sathish
 */
public final class LogProcessorTest {

	private static LogServiceFactory mockedFactory;
	private WriterApi writerMock;

	@BeforeClass
	public static void staticInit() {
		mockedFactory = mock(LogServiceFactory.class);
		MockedStatic<LogServiceFactory> staticMock = Mockito.mockStatic(LogServiceFactory.class);
		staticMock.when(() -> LogServiceFactory.instance()).thenReturn(mockedFactory);
	}

	@Before
	public void init() {
		writerMock = mock(FileDbResultWriter.class);
		when(mockedFactory.getWriter(Mockito.any())).thenReturn(writerMock);
	}

	@Test
	public void shouldProcessValidData() throws InterruptedException {
		// GIVEN data & mocks
		BlockingQueue<SourceEventModel> queue = new ArrayBlockingQueue<>(10);

		// Data-1 with ID-1
		SourceEventModel event1 = new SourceEventModel();
		event1.setId("1");
		event1.setTimestamp(TimeUnit.SECONDS.toMillis(10));
		event1.setState(EventConstant.STARTED);
		queue.put(event1);

		// Data-2 with ID-1
		SourceEventModel event2 = new SourceEventModel();
		event2.setId("1");
		event2.setTimestamp(TimeUnit.SECONDS.toMillis(12));
		event2.setState(EventConstant.FINISHED);
		queue.put(event2);

		// Data to shutdown the process
		queue.put(new SourceEventModel());

		// WHEN processor tested
		LogProcessor target = new LogProcessor(queue, writerMock);
		// Yes, no need to 'start a thread' for sake of testing, 'run' should suffice
		// the purpose here
		target.run();

		// THEN verify
		Mockito.verify(writerMock, times(1)).write(ArgumentMatchers.any(ResultEventModel.class));
		Mockito.verify(writerMock, times(1)).write(ArgumentMatchers.isNull());
	}

	@Test
	public void shouldNotProcessUnmatched() throws InterruptedException {
		// GIVEN data & mocks
		BlockingQueue<SourceEventModel> queue = new ArrayBlockingQueue<>(10);

		// Data-1 with ID-1
		SourceEventModel event1 = new SourceEventModel();
		event1.setId("1");
		event1.setTimestamp(TimeUnit.SECONDS.toMillis(10));
		event1.setState(EventConstant.STARTED);
		queue.put(event1);

		// Data-2 with ID-2
		SourceEventModel event2 = new SourceEventModel();
		event2.setId("2");
		event2.setTimestamp(TimeUnit.SECONDS.toMillis(12));
		event2.setState(EventConstant.FINISHED);
		queue.put(event2);

		// Data to shutdown the process
		queue.put(new SourceEventModel());

		// WHEN processor tested
		LogProcessor target = new LogProcessor(queue, writerMock);
		// Yes, no need to 'start a thread' for sake of testing, 'run' should suffice
		// the purpose here
		target.run();

		// THEN verify
		Mockito.verify(writerMock, times(0)).write(ArgumentMatchers.any(ResultEventModel.class));
		Mockito.verify(writerMock, times(1)).write(ArgumentMatchers.isNull());
	}
}
