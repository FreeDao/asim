package com.view.asim.model;

import java.util.Date;

/**
 * 通用消息对象
 */

import com.view.asim.util.DateUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class IMMessage implements Parcelable, Comparable<IMMessage> {
	
	public static final String PROP_TYPE = "message.prop.type";
	public static final String PROP_TIME = "message.prop.time";
	public static final String PROP_WITH = "message.prop.with";
	public static final String PROP_SECURITY = "message.prop.security";
	public static final String PROP_DESTROY = "message.prop.destroy";
	public static final String PROP_CHATTYPE = "message.prop.chattype";
	
	public static final String PROP_TYPE_CHAT = "message.prop.type.chat";
	public static final String PROP_TYPE_CTRL = "message.prop.type.ctrl";
	
	// 消息状态常量
	public static final String SUCCESS = "success";
	public static final String ERROR = "error";
	
	// 已读状态常量
	public static final String READ = "read";
	public static final String UNREAD = "unread";
	
	// 消息方向常量
	public static final String SEND = "send_message";
	public static final String RECV = "recv_message";
	
	// 安全类型常量
	public static final String PLAIN = "plain";
	public static final String ENCRYPTION = "encryption";
	
	// 销毁类型常
	public static final String NEVER_BURN = "never_burn";
	public static final String BURN_AFTER_READ = "burn_after_read";
	
	public static final String SINGLE = "single";
	public static final String GROUP = "group";

	private String id;

	// 消息状态（成功/失败）
	private String status;

	// 消息归属对象(个人或者群组)
	private String with;

	// 消息发出者
	private String from;

	// 消息方向
	private String dir;
	
	// 消息类型
	private String type;
	
	// 消息内容
	private String content;
	
	// 消息发生时间
	private String time;
	
	// 聊天类型：群聊/单人
	private String chatType;

	// 安全类型
	private String security;
	
	// 销毁类型
	private String destroy;
	
	public String getChatType() {
		return chatType;
	}


	public void setChatType(String chatObjType) {
		this.chatType = chatObjType;
	}
	
	public String getDestroy() {
		return destroy;
	}


	public void setDestroy(String destroy) {
		this.destroy = destroy;
	}

	public String getSecurity() {
		return security;
	}

	public void setSecurity(String security) {
		this.security = security;
	}

	public IMMessage() {
		this.status = SUCCESS;
	}

	public String getId() {
		return id;
	}



	public void setId(String id) {
		this.id = id;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
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



	public String getContent() {
		return content;
	}



	public void setContent(String content) {
		this.content = content;
	}



	public String getTime() {
		return time;
	}



	public void setTime(String time) {
		this.time = time;
	}

	protected IMMessage(Parcel in) {  
		this.id = in.readString();
		this.type = in.readString();
		this.dir = in.readString();
		this.status = in.readString();
		this.time = in.readString();
		this.with = in.readString();
		this.from = in.readString();
		this.content = in.readString();
		this.chatType = in.readString();
		this.destroy = in.readString();
		this.security = in.readString();
    }  
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(type);
		dest.writeString(dir);
		dest.writeString(status);
		dest.writeString(time);
		dest.writeString(with);
		dest.writeString(from);
		dest.writeString(content);
		dest.writeString(chatType);
		dest.writeString(destroy);
		dest.writeString(security);
	}

	public static final Parcelable.Creator<IMMessage> CREATOR = new Parcelable.Creator<IMMessage>() {

		@Override
		public IMMessage createFromParcel(Parcel source) {
			return new IMMessage(source);
		}

		@Override
		public IMMessage[] newArray(int size) {
			return new IMMessage[size];
		}

	};

	/**
	 * 按时间降序排列
	 */
	@Override
	public int compareTo(IMMessage oth) {
		
		if (null == this.getTime() || null == oth.getTime()) {
			return 0;
		}
		Date da1 = DateUtil.str2Date(this.getTime());
		Date da2 = DateUtil.str2Date(oth.getTime());
		if (da1.before(da2)) {
			return -1;
		}
		if (da2.before(da1)) {
			return 1;
		}
	
		return 0;
	}
}
