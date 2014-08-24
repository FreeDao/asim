package com.view.asim.model;

/**
 * ���� IM ��Ϣ��Я���ĸ���
 */

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Parcel;
import android.os.Parcelable;

public class Attachment implements Parcelable{

	/**
	 * ���� URI��
	 * ����������㷢�ͣ��˲���������ڣ�ָ������������ɵ���Ƭ�����λ�ã���Ӧ���ѵ� image Ŀ¼�У�
	 * ���ڷ��͵�ͼƬ����Ƶ���˲���������ڣ�ָ����Դ�ļ�ԭ�ȴ��ڵ�λ��
	 * ���ڽ��յ�ͼƬ����Ƶ���˲���Ϊ��ѡ��δ��������ǰΪ�գ��������سɹ���Ϊ���ر����λ�ã���Ӧ���ѵ� image/video Ŀ¼�У�
	 * ���ڽ��յ��������˲���������ڣ�Ϊ���غ󱣴��λ�ã���Ӧ���ѵ� audio Ŀ¼�У�
	 * ���ڷ��͵��������˲���������ڣ�Ϊ�ɼ������󱣴��λ�ã���Ӧ���ѵ� audio Ŀ¼�У�
	 */
	private String srcUri;
	
	/** 
	 * ����ͼ URI��(������ͼƬ����Ƶ��
	 * ���ڷ��͵�ͼƬ���˲�������
	 * ���ڷ��͵���Ƶ���˲���Ϊ��Ƶ����ͼ URI����Ƶ����ͼ�Ǹ���ԭ��Ƶ�ļ������ɵ�ͼƬ�����λ�ã�cache Ŀ¼�У�
	 * ���ڽ��յ�ͼƬ���˲���Ϊ�ӷ��������ص�����ͼ�����ڱ��ص� URI����Ӧ���ѵ� image Ŀ¼�У�
	 * ���ڽ��յ���Ƶ���˲���Ϊ�ӷ��������ص�����ͼ�����ڱ��ص� URI����Ӧ���ѵ� video Ŀ¼�У�
	 */
	private String thumbUri;
	
	// ¼����Ƶʱ��
	private int audioLength; 

	// ������ Hash
	private String hash;
	
	// ������ ����ͼ Hash��Ŀǰ�ڲɼ���Ƶʱ���Ὣ��Ƶ�ļ�������ͼһ���ϴ���������
	private String thumbHash;

	// ������ Key
	private String key;
	
	// ������ ����ͼ Key ��Ŀǰ�ڲɼ���Ƶʱ���Ὣ��Ƶ�ļ�������ͼһ���ϴ���������
	private String thumbKey;	

	// �ļ�����(MIME)
	private String mimeType;

	// ��ȣ�ͼƬר�ã�
	private String width;
	
	// �߶ȣ�ͼƬר�ã�
	private String height;
	
	// ��С
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
	
	// �� DB �б�������л�������ɶ���
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
	
	// ����ţ���ϴ���Ӧ�����ɶ���
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
