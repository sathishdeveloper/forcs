package com.cs.eventproj.api;

import com.cs.eventproj.model.ResultEventModel;

/**
 * Processed log event writer
 * 
 * @author sathish
 */
public interface WriterApi {

	/**
	 * Writes data to destination
	 * 
	 * @param data {@link ResultEventModel}
	 */
	void write(ResultEventModel data);
}
