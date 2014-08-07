/**
 *  IM ������Ϣ
 */

package com.view.asim.model;

import java.util.Date;

import com.view.asim.utils.DateUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class CtrlMessage extends IMMessage {
	public static final String CTRLMESSAGE_KEY = "message.ctrl.key";

	public static final String PROP_CTRL_MSGTYPE = "message.prop.ctrl.msgtype";
	
	// ��Ϣ���ͳ���
	// Ⱥ������
	public static final String CTRL_GROUP_INVITE = "group_invite";
	// �˳�Ⱥ��
	public static final String CTRL_GROUP_QUIT = "group_quit";
	// Ⱥ������Ϣ����
	public static final String CTRL_GROUP_UPDATE = "group_update";
	// �Ự��Կ����
	public static final String CTRL_SEK_EXCHANGE = "sek_exchange";
	// ͼƬ����֪ͨ
	public static final String CTRL_SEND_IMG = "image";
	// ��������֪ͨ
	public static final String CTRL_SEND_AUDIO = "audio";
	// ��Ƶ����֪ͨ
	public static final String CTRL_SEND_VIDEO = "video";
	// �ļ�����֪ͨ
	public static final String CTRL_SEND_FILE = "file";
	// Զ��������Ϣ
	public static final String REMOTE_DESTROY = "remote_destroy";

	
	public CtrlMessage() {
		super();
	}
	
	private CtrlMessage(Parcel in) {  
        super(in);  
    }  
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
	}

	public static final Parcelable.Creator<CtrlMessage> CREATOR = new Parcelable.Creator<CtrlMessage>() {

		@Override
		public CtrlMessage createFromParcel(Parcel source) {
			return new CtrlMessage(source);
		}

		@Override
		public CtrlMessage[] newArray(int size) {
			return new CtrlMessage[size];
		}

	};
}
