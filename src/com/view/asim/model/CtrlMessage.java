/**
 *  IM 控制消息
 */

package com.view.asim.model;

import java.util.Date;

import com.view.asim.utils.DateUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class CtrlMessage extends IMMessage {
	public static final String CTRLMESSAGE_KEY = "message.ctrl.key";

	public static final String PROP_CTRL_MSGTYPE = "message.prop.ctrl.msgtype";
	
	// 消息类型常量
	// 群聊邀请
	public static final String CTRL_GROUP_INVITE = "group_invite";
	// 退出群聊
	public static final String CTRL_GROUP_QUIT = "group_quit";
	// 群聊组信息更新
	public static final String CTRL_GROUP_UPDATE = "group_update";
	// 会话密钥交换
	public static final String CTRL_SEK_EXCHANGE = "sek_exchange";
	// 图片发送通知
	public static final String CTRL_SEND_IMG = "image";
	// 语音发送通知
	public static final String CTRL_SEND_AUDIO = "audio";
	// 视频发送通知
	public static final String CTRL_SEND_VIDEO = "video";
	// 文件发送通知
	public static final String CTRL_SEND_FILE = "file";
	// 远程销毁消息
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
