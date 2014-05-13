package com.view.asim.model;

import java.io.Serializable;
import java.util.Date;

import com.view.asim.comm.Constant;
import com.view.asim.util.DateUtil;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * 
 * ֪ͨ����
 * 
 * @author xuweinan
 */
public class Notice implements Parcelable, Comparable<Notice> {

	public static final String noticeKey = "aim_notify";

	public static final String READ = "read";
	public static final String UNREAD = "unread"; 

	public static final String DISPLAY = "display";
	public static final String HIDE = "hide"; 

	// ״̬����Ӻ��ѵȴ��Է�ͬ��
	public static final String STATUS_WAIT_FOR_ACCEPT = "wait_accept";
	// ״̬���յ��Է���Ӻ���������Լ�ͬ��
	public static final String STATUS_ADD_REQUEST = "add_request";
	// ״̬����Ӻ��ѳɹ����
	public static final String STATUS_COMPLETE = "complete";
	
	private String id;			// ����
	private String content; 	// ���ݣ�Ԥ����
	private String status;		// ״̬
	private String with;		// ����JID
	private String time;		// ֪ͨʱ��
	private String dispStatus;  // ��ʾ״̬
	private String readStatus;  // �Ѷ�״̬
	private Bitmap avatar;
	
	public String getDispStatus() {
		return dispStatus;
	}

	public void setDispStatus(String dispStatus) {
		this.dispStatus = dispStatus;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getReadStatus() {
		return readStatus;
	}

	public void setReadStatus(String readStatus) {
		this.readStatus = readStatus;
	}

	public Bitmap getAvatar() {
		return avatar;
	}

	public void setAvatar(Bitmap avatar) {
		this.avatar = avatar;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(content);
		dest.writeString(status);
		dest.writeString(with);
		dest.writeString(time);
		dest.writeString(dispStatus);
		dest.writeString(readStatus);
		dest.writeParcelable(avatar, flags);
	}

	public static final Parcelable.Creator<Notice> CREATOR = new Parcelable.Creator<Notice>() {

		@Override
		public Notice createFromParcel(Parcel source) {
			Notice n = new Notice();
			n.setId(source.readString());
			n.setContent(source.readString());
			n.setStatus(source.readString());
			n.setWith(source.readString());
			n.setTime(source.readString());
			n.setDispStatus(source.readString());
			n.setReadStatus(source.readString());
			n.setAvatar((Bitmap)source.readParcelable(Bitmap.class.getClassLoader()));
			return n;
		}

		@Override
		public Notice[] newArray(int size) {
			return new Notice[size];
		}

	};

	
	@Override
	public int compareTo(Notice oth) {
		
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
