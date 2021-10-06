package com.cs.eventproj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs.eventproj.api.EventConstant;
import com.cs.eventproj.api.ReaderApi;
import com.cs.eventproj.api.WriterApi;
import com.cs.eventproj.service.LogServiceFactory;
import com.cs.eventproj.service.LogServiceFactory.ReaderType;
import com.cs.eventproj.service.LogServiceFactory.WriterType;

/**
 * Main application class for CS log analyzing service
 *
 * @author sathish
 */
public class LogAnalyserApp {
	private static final Logger LOG = LoggerFactory.getLogger(LogAnalyserApp.class);

	/**
	 * Main method to initiate the process
	 * 
	 * @param args - single argument containing the path of source log file
	 */
	public static void main(String[] args) {
		if (args.length < 1 || args[0] == null || args[0].isBlank()) {
			LOG.error("Invocation argument invalid, 'java LogAnalyserApp /path/to/logsource.txt'");
			return;
		}
		System.setProperty(EventConstant.SRC_FILE, args[0]);
		new LogAnalyserApp().process();
	}

	private void process() {
		WriterApi writer = LogServiceFactory.instance().getWriter(WriterType.FILE_DB_WRITER);
		ReaderApi reader = LogServiceFactory.instance().getReader(ReaderType.FILE_READER);
		LogServiceFactory.instance().process(reader, writer);
	}
}
