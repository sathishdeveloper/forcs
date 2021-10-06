package com.cs.eventproj.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs.eventproj.api.EventConstant;
import com.cs.eventproj.api.WriterApi;
import com.cs.eventproj.model.ResultEventModel;
import com.cs.eventproj.model.SourceEventModel;

/**
 * An intermediary log processor between reader & writer, a {@link Runnable}
 * 
 * @author sathish
 */
public class LogProcessor implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(LogProcessor.class);

	private BlockingQueue<SourceEventModel> queue;
	private Map<String, SourceEventModel> processedData;
	private WriterApi writer;

	/**
	 * Constructor with queue for data processing Also initiates a writer
	 * 
	 * @param data
	 */
	public LogProcessor(BlockingQueue<SourceEventModel> data, WriterApi writer) {
		queue = data;
		processedData = new HashMap<>();
		this.writer = writer;
	}

	/**
	 * Performs - Wait for data at queue - process the data - wait again till end of
	 * the data signaled (using invalid 'event-id')
	 */
	@Override
	public void run() {
		boolean alive = true;
		SourceEventModel model = null;
		while (alive) {
			try {
				model = queue.take();
				if (model.getId() == null) {
					// Indication of stop signal
					alive = false;
					writer.write(null);
					continue;
				}
				LOG.trace("Received: {}", model);
				process(model);
			} catch (InterruptedException e) {
				LOG.error("Processing the log is interrupted. {}", e.getMessage());
				alive = false;
				writer.write(null);
			}
		}
	}

	/**
	 * Analyzes the event data for duration and transforms in to DB compatible data
	 * 
	 * @param model {@link SourceEventModel}
	 */
	private void process(SourceEventModel model) {
		SourceEventModel companionModel = null;
		if ((companionModel = processedData.get(model.getId())) != null) {
			// Companion model exists already
			long duration = 0l;
			if (EventConstant.STARTED.equals(companionModel.getState())) {
				// Already 'started' state received, compare it with end time of 'model'
				duration = model.getTimestamp() - companionModel.getTimestamp();
			} else if (EventConstant.FINISHED.equals(companionModel.getState())) {
				// Already 'finished' state exists, compare it with start time of 'model'
				duration = companionModel.getTimestamp() - model.getTimestamp();
			}
			// Start preparing result event data
			ResultEventModel result = new ResultEventModel(model.getId(), duration);
			if (duration > 4) {
				result.setAlert(true);
			}
			result.setHost(
					model.getHost() == null || model.getHost().isBlank() ? companionModel.getHost() : model.getHost());
			result.setType(
					model.getType() == null || model.getType().isBlank() ? companionModel.getType() : model.getType());
			LOG.info("Result: {}", result);

			// Cleanup the processed event, TODO: Performance intensive, but good for memory
			processedData.remove(model.getId());
			writer.write(result);
		} else {
			// Fresh event Id, store it as it is, would be used later.
			processedData.put(model.getId(), model);
		}
	}

}
