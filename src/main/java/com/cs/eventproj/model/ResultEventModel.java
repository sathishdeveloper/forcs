package com.cs.eventproj.model;

public class ResultEventModel extends AbstractEventModel{

	private long duration;
	private boolean alert;

	public ResultEventModel() {
		// TODO Auto-generated constructor stub
	}
	
	public ResultEventModel(String id, long duration) {
		setId(id);
		this.duration = duration;
	}
	
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public boolean isAlert() {
		return alert;
	}
	public void setAlert(boolean alert) {
		this.alert = alert;
	}
	
	@Override
	public String toString() {
		return String.format("id:%s, duration:%dms, type:%s, host:%s, alert:%b", getId(), duration, getType(), getHost(), alert);
	}
}
