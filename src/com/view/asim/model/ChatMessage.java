/**
 * IM 普通消息（界面显示）
 */

package com.view.asim.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ChatMessage extends IMMessage {
	public static final String IMMESSAGE_KEY = "message.im.key";
	public static final String PROP_IM_MSGTYPE = "message.prop.im.msgtype";

	// 消息类型常量
	public static final String TIMESTAMP = "time";
	public static final String CHAT_TEXT = "text";
	public static final String CHAT_IMAGE = "image";
	public static final String CHAT_AUDIO = "audio";
	public static final String CHAT_VIDEO = "video";
	public static final String CHAT_FILE = "file";
	
	private Attachment attachment;
	
	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public ChatMessage() {
		super();
	}
	
	private ChatMessage(Parcel in) {  
        super(in);  
        this.attachment = in.readParcelable(Attachment.class.getClassLoader());
    }  
	
	
	public boolean isTextMessage() {
		if (getType().equals(CHAT_TEXT)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean isMultiMediaMessage() {
		if (!getType().equals(CHAT_TEXT) && !getType().equals(TIMESTAMP)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		String att;
		if(attachment != null) {
			att = attachment.dumps();
		}
		else {
			att = "";
		}
		return super.toString() + ", attachment: " + att;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeParcelable(attachment, flags);
	}

	public static final Parcelable.Creator<ChatMessage> CREATOR = new Parcelable.Creator<ChatMessage>() {

		@Override
		public ChatMessage createFromParcel(Parcel source) {
			return new ChatMessage(source);
		}

		@Override
		public ChatMessage[] newArray(int size) {
			return new ChatMessage[size];
		}

	};

}
