package com.view.asim.model;

import com.view.asim.util.DateUtil;

/**
 * 
 * �����ϵ����ʾ����ĳ���������¼bean������ �յ�ĳ���˵����һ����Ϣ��ȫ�����ݣ��յ�ĳ��δ����Ϣ�������ܺ�
 * 
 * @author xuweinan
 */
public class ChatHisBean implements Comparable<ChatHisBean> {
	/*
	public static final int ADD_FRIEND = 1;// ��������
	public static final int SYS_MSG = 2; // ϵͳ��Ϣ
	public static final int CHAT_MSG = 3;// ������Ϣ
	
	public static final int READ = 0;
	public static final int UNREAD = 1;
	*/

	private String id; 
	private String chatType;

	private String content;
	private String from; 
	private String with; 
	private String time; // ���֪ͨʱ��
	private String destroy;
	private String type;
	private String dir;
	private int unreadCount;// �յ�δ����Ϣ������
	
	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}

	public String getDestroy() {
		return destroy;
	}


	public void setDestroy(String destroy) {
		this.destroy = destroy;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}

	public String getChatType() {
		return chatType;
	}


	public void setChatType(String chatType) {
		this.chatType = chatType;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}


	public String getFrom() {
		return from;
	}


	public void setFrom(String from) {
		this.from = from;
	}


	public String getWith() {
		return with;
	}


	public void setWith(String with) {
		this.with = with;
	}


	public String getTime() {
		return time;
	}


	public void setTime(String time) {
		this.time = time;
	}


	public int getUnreadCount() {
		return unreadCount;
	}


	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}


	@Override
	public int compareTo(ChatHisBean another) {
		
		if (another == null || another.getTime() == null) {
			return 0;
		}
		
		long o1Time = DateUtil.str2Calendar(this.time).getTimeInMillis();
		long o2Time = DateUtil.str2Calendar(another.getTime()).getTimeInMillis();
		
		if(o1Time > o2Time)
			return -1;
		if(o1Time < o2Time)
			return 1;
		return 0;
	}

}
