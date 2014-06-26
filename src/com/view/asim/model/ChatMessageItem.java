package com.view.asim.model;

/**
 * ��������Ự�б�����ʾ����Ŀ��������Ϣ�����ʱ�����
 * @author xuweinan
 */
public class ChatMessageItem {
	
	private String type;
	private Object value;
	private int progress;
	
	public ChatMessageItem() {
		progress = 0;
	}
	
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
}
