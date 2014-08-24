package com.view.asim.model;

/**
 * 复杂 IM 消息中携带的附件
 */

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Parcel;
import android.os.Parcelable;

public class Attachment implements Parcelable{

	/**
	 * 本地 URI：
	 * 对于相机拍摄发送，此参数必须存在，指向相机拍摄生成的照片保存的位置（对应好友的 image 目录中）
	 * 对于发送的图片或视频，此参数必须存在，指向资源文件原先存在的位置
	 * 对于接收的图片或视频，此参数为可选，未完整下载前为空，完整下载成功后为下载保存的位置（对应好友的 image/video 目录中）
	 * 对于接收的语音，此参数必须存在，为下载后保存的位置（对应好友的 audio 目录中）
	 * 对于发送的语音，此参数必须存在，为采集语音后保存的位置（对应好友的 audio 目录中）
	 */
	private String srcUri;
	
	/** 
	 * 缩略图 URI：(仅限于图片和视频）
	 * 对于发送的图片，此参数忽略
	 * 对于发送的视频，此参数为视频缩略图 URI，视频缩略图是根据原视频文件新生成的图片保存的位置（cache 目录中）
	 * 对于接收的图片，此参数为从服务器下载的缩略图保存在本地的 URI（对应好友的 image 目录中）
	 * 对于接收的视频，此参数为从服务器下载的缩略图保存在本地的 URI（对应好友的 video 目录中）
	 */
	private String thumbUri;
	
	// 录音音频时长
	private int audioLength; 

	// 服务器 Hash
	private String hash;
	
	// 服务器 缩略图 Hash（目前在采集视频时，会将视频文件和缩略图一起上传服务器）
	private String thumbHash;

	// 服务器 Key
	private String key;
	
	// 服务器 缩略图 Key （目前在采集视频时，会将视频文件和缩略图一起上传服务器）
	private String thumbKey;	

	// 文件类型(MIME)
	private String mimeType;

	// 宽度（图片专用）
	private String width;
	
	// 高度（图片专用）
	private String height;
	
	// 大小
	private String size;

	public Attachment() {
		super();
		srcUri = "";
		thumbUri = "";
		audioLength = 0;
		hash = "";
		thumbHash = "";
		key = "";
		thumbKey = "";
		mimeType = "";
		width = "";
		height = "";
		size = "";
	}
	

	public String getThumbHash() {
		return thumbHash;
	}

	public void setThumbHash(String thumbHash) {
		this.thumbHash = thumbHash;
	}

	public String getThumbKey() {
		return thumbKey;
	}

	public void setThumbKey(String thumbKey) {
		this.thumbKey = thumbKey;
	}

	public String getSrcUri() {
		return srcUri;
	}

	public void setSrcUri(String srcUri) {
		this.srcUri = srcUri;
	}

	public String getThumbUri() {
		return thumbUri;
	}

	public void setThumbUri(String thumbUri) {
		this.thumbUri = thumbUri;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
	

	public int getAudioLength() {
		return audioLength;
	}

	public void setAudioLength(int audioLength) {
		this.audioLength = audioLength;
	}
	
	public String dumps() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("hash", hash);
			obj.put("thumbHash", thumbHash);
			obj.put("key", key);
			obj.put("thumbKey", thumbKey);
			obj.put("mimeType", mimeType);
			obj.put("width", width);
			obj.put("height", height);
			obj.put("size", size);
			obj.put("srcUri", srcUri);
			obj.put("thumbUri", thumbUri);
			obj.put("audioLength", audioLength);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return obj.toString();
	}
	
	// 从 DB 中保存的序列化结果生成对象
	public static Attachment loads(String json) {
		Attachment att = new Attachment();
		
		if (json == null || json.length() == 0) {
			return null;
		}
		
		try {  
		    JSONTokener jsonParser = new JSONTokener(json);  
		    JSONObject resp = (JSONObject) jsonParser.nextValue();
		    
		    att.setHash(resp.getString("hash"));
		    att.setThumbHash(resp.getString("thumbHash"));
		    att.setKey(resp.getString("key"));
		    att.setThumbKey(resp.getString("thumbKey"));
		    att.setMimeType(resp.getString("mimeType"));
		    att.setWidth(resp.getString("width"));
		    att.setHeight(resp.getString("height"));
		    att.setSize(resp.getString("size"));
		    att.setSrcUri(resp.getString("srcUri"));
		    att.setThumbUri(resp.getString("thumbUri"));
		    att.setAudioLength(resp.getInt("audioLength"));

		} catch (Exception e) {  
			e.printStackTrace();
			return null;
		}

		return att;	
	}
	
	// 从七牛的上传响应中生成对象
	public static Attachment loads(JSONObject json) {
		Attachment att = new Attachment();
		
		if (json == null || json.length() == 0) {
			return null;
		}
		
		try {  
		    att.setHash(json.getString("hash"));
		    att.setKey(json.getString("key"));
		    att.setMimeType(json.getString("mimeType"));
		    att.setWidth(json.getString("width"));
		    att.setHeight(json.getString("height"));
		    att.setSize(json.getString("size"));

		} catch (JSONException e) {  
			e.printStackTrace();
			return null;
		}

		return att;	
	}
	
	protected Attachment(Parcel in) {  
		this.srcUri = in.readString();
		this.thumbUri = in.readString();
		this.hash = in.readString();
		this.thumbHash = in.readString();
		this.key = in.readString();
		this.thumbKey = in.readString();
		this.mimeType = in.readString();
		this.width = in.readString();
		this.height = in.readString();
		this.size = in.readString();
		this.audioLength = in.readInt();
    }  
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(srcUri);
		dest.writeString(thumbUri);
		dest.writeString(hash);
		dest.writeString(thumbHash);
		dest.writeString(key);
		dest.writeString(thumbKey);
		dest.writeString(mimeType);
		dest.writeString(width);
		dest.writeString(height);
		dest.writeString(size);
		dest.writeInt(audioLength);
	}

	public static final Parcelable.Creator<Attachment> CREATOR = new Parcelable.Creator<Attachment>() {

		@Override
		public Attachment createFromParcel(Parcel source) {
			return new Attachment(source);
		}

		@Override
		public Attachment[] newArray(int size) {
			return new Attachment[size];
		}

	};

}
