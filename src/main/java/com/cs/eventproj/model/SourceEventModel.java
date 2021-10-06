package com.cs.eventproj.model;


public class SourceEventModel extends AbstractEventModel{
	private String state;
	private long timestamp;

	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	@Override
	public String toString() {
		return String.format("id:%s, state:%s, type:%s, host:%s, timestamp:%d", getId(), state, getType(), getHost(), timestamp);
	}
}
