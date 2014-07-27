package com.view.asim.model;

import com.view.asim.util.DateUtil;

/**
 * 
 * 一条通话记录
 * 
 * @author xuweinan
 */
public class SingleCallLog implements Comparable<SingleCallLog> {

	private String id; 
	private String with; 
	private long time;
	private String security;
	private int type;
	private int duration;
	private int statusCode;

	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}


	public void setType(int type) {
		this.type = type;
	}

	public String getWith() {
		return with;
	}


	public void setWith(String with) {
		this.with = with;
	}


	public long getTime() {
		return time;
	}


	public void setTime(long time) {
		this.time = time;
	}

	public int getDuration() {
		return duration;
	}


	public void setDuration(int duration) {
		this.duration = duration;
	}


	public int getStatusCode() {
		return statusCode;
	}


	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getSecurity() {
		return security;
	}


	public void setSecurity(String security) {
		this.security = security;
	}

	@Override
	public int compareTo(SingleCallLog another) {
		
		if (another == null || another.getTime() == 0) {
			return 0;
		}
		
		long o1Time = this.time;
		long o2Time = another.getTime();
		
		if(o1Time > o2Time)
			return -1;
		if(o1Time < o2Time)
			return 1;
		return 0;
	}

}
