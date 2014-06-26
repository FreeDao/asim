package com.view.asim.model;

/**
 * 用于聊天会话列表中显示的项目（包括消息本身和时间戳）
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
