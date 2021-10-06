package com.cs.eventproj.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.cs.eventproj.api.EventConstant;
import com.cs.eventproj.api.ReaderApi;
import com.cs.eventproj.api.WriterApi;
import com.cs.eventproj.exception.EnvironmentException;
import com.cs.eventproj.model.SourceEventModel;

/**
 * Factory for fetching log readers, writers.
 * 
 * @author sathish
 */
public class LogServiceFactory {

	private static final LogServiceFactory _instance = new LogServiceFactory();
	private BlockingQueue<SourceEventModel> queue;

	/**
	 * Private constructor
	 */
	private LogServiceFactory() {
		init();
	}

	/**
	 * @return gets instance of this factory
	 */
	public static LogServiceFactory instance() {
		return _instance;
	}

	/**
	 * Supported reader types
	 */
	public enum ReaderType {
		FILE_READER
	}

	/**
	 * Supported writer types
	 */
	public enum WriterType {
		FILE_DB_WRITER
	}

	/**
	 * Gets a corresponding reader for the {@link ReaderType} Also, initiates the
	 * corresponding log processor for the reader
	 * 
	 * @param type {@link ReaderType}
	 * @return {@link ReaderApi}
	 */
	public ReaderApi getReader(ReaderType type) {
		if (type == ReaderType.FILE_READER) {
			return new LogFileReader(queue);
		}

		throw new UnsupportedOperationException("invalid reader type");
	}

	/**
	 * Gets a writer for the {@link WriterType}
	 * 
	 * @param type {@link WriterType}
	 * @return {@link WriterApi}
	 */
	public WriterApi getWriter(WriterType type) {
		if (type == WriterType.FILE_DB_WRITER) {
			return new FileDbResultWriter();
		}

		throw new UnsupportedOperationException("invalid writer type");
	}

	/**
	 * Process the data from reader to writer
	 * 
	 * @param reader {@link ReaderApi}
	 * @param writer {@link WriterApi}
	 */
	public void process(ReaderApi reader, WriterApi writer) {
		new Thread(new LogProcessor(queue, writer)).start();
		reader.read();
	}

	/**
	 * Initiates the - Configuration property definitions - Configures queue size
	 * for handling between reader and processor, default consumer queue length,
	 * could be overridden in the config.properties
	 */
	private void init() {
		Properties props = new Properties();
		try (InputStream ip = getClass().getClassLoader().getResourceAsStream("config.properties");) {
			props.load(ip);
		} catch (IOException e) {
			throw new EnvironmentException(e.getMessage());
		}
		String size = props.getProperty(EventConstant.PROP_QUEUE_SIZE, "1");
		int queueSize = Integer.valueOf(size);
		queue = new ArrayBlockingQueue<>(queueSize);

		// Set tolerance for blocking queue wait period
		String waitTimeSecs = props.getProperty(EventConstant.QUEUE_TOLERANCE, "5");
		System.setProperty(EventConstant.QUEUE_TOLERANCE, waitTimeSecs);
	}
}
