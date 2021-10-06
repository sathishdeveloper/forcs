package com.cs.eventproj.eventproj;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.BeforeClass;
import org.junit.Test;

import com.cs.eventproj.api.ReaderApi;
import com.cs.eventproj.api.WriterApi;
import com.cs.eventproj.service.LogServiceFactory;
import com.cs.eventproj.service.LogServiceFactory.ReaderType;
import com.cs.eventproj.service.LogServiceFactory.WriterType;

/**
 * Test for {@link LogServiceFactory}
 * 
 * @author sathish
 */
public class LogServiceFactoryTest {

	private static LogServiceFactory target;
	
	@BeforeClass
	public static void init() {
		target = LogServiceFactory.instance();
	}
	
	@Test
	public void shouldGetValidReader() {
		ReaderApi reader = target.getReader(ReaderType.FILE_READER);
		assertNotNull("Reader have not returned a valid type", reader);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void shouldThrowErrorInvalidReaderType() {
		target.getReader(null);
	}
	
	@Test
	public void shouldGetValidWriter() {
		WriterApi writer = target.getWriter(WriterType.FILE_DB_WRITER);
		assertNotNull("Writer have returned an invalid result", writer);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void shouldThrowErrorInvalidWriterType() {
		target.getWriter(null);
	}
	
	@Test
	public void shouldProcessFlow() {
		ReaderApi readerMock = mock(ReaderApi.class);
		target.process(readerMock, mock(WriterApi.class));
		verify(readerMock, times(1)).read();
	}
}
