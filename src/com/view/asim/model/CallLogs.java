package com.view.asim.model;

import com.view.asim.util.DateUtil;

/**
 * 
 * 最近联系人显示的与某个的通话记录bean，包括：最后一条通话的类型、时长，通话记录的总数
 * 
 * @author xuweinan
 */
public class CallLogs implements Comparable<CallLogs> {

	private String id; 
	private int type;
	private String with; 
	private long time;
	private String security;
	private int totalCount;


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


	public int getTotalCount() {
		return totalCount;
	}


	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public String getSecurity() {
		return security;
	}


	public void setSecurity(String security) {
		this.security = security;
	}
	
	@Override
	public int compareTo(CallLogs another) {
		
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
