package com.view.asim.model;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;

import org.jivesoftware.smack.packet.RosterPacket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.view.asim.R;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用户
 * 
 * @author  xuweinan
 * 
 */
public class User implements Parcelable {

	/**
	 * 将user保存在intent中时的key
	 */
	public static final String userKey = "aim_user";
	public static final String userListKey = "aim_user_list";
	
	public static final String VCARD_FIELD_GENDER = "sex";
	public static final String VCARD_FIELD_GLOBALID = "global_id";
	public static final String VCARD_FIELD_LOCATION = "REGION";
	public static final String VCARD_FIELD_REMARK = "remark";
	public static final String VCARD_FIELD_PUBLICKEY = "public_key";
	public static final String VCARD_FIELD_PRIVATEKEY = "private_key";
	public static final String VCARD_FIELD_GROUPINFO = "group_list";


	public static final String MALE = "male";
	public static final String FEMALE = "female";
	public static final String NAME_PREFIX = "u";

	// 用户名(手机号生成)
	protected String name = null;
	
	// 用户 JID
	protected String JID = null;
	
	// 昵称
	protected String nickName = null;
	
	// 用户 remark 信息
	protected String remark = null;
	
	// 性别
	protected String gender = null;

	// 手机通讯录中的名称
	protected String contactName = null;

	// 地区
	protected String location = null;
	
	// 密信 ID
	protected String globalID = null;
	
	// 头像
	protected Bitmap headImg = null;

	// 昵称拼音
	protected String namepy = null;
	protected String nameFirstChar = null;
	protected String sortLetters = null;  
	protected String namecode = null;

	// 非对称密钥
	protected String publicKey = null;
	protected String privateKey = null;
	
	protected long sipAccountId = 0; 

	// 所属群聊组信息
	protected ArrayList<GroupUser> groupList = null;


	public User() {
		super();
		groupList = new ArrayList<GroupUser>();
	}
	
	
	public long getSipAccountId() {
		return sipAccountId;
	}

	public void setSipAccountId(long sipAccountId) {
		this.sipAccountId = sipAccountId;
	}


	public void addGroup(GroupUser grp) {
		if (grp != null) {
			groupList.add(grp);
		}
	}

	public void delGroup(GroupUser grp) {
		if (grp != null) {
			for (GroupUser gu: groupList) {
				if (gu.getName().equals(grp.getName())) {
					groupList.remove(gu);
				}
			}
		}
	}
	public ArrayList<GroupUser> getGroupList() {
		return groupList;
	}

	public void setGroupList(ArrayList<GroupUser> groupList) {
		this.groupList = groupList;
	}
	
	public void loadGroupList(String json) {
		ArrayList<GroupUser> tempGroupList = new ArrayList<GroupUser>();
		GroupUser tempGrp = null;
		
		if (json == null || json.length() == 0) {
			return;
		}
		
		try {  
		    JSONTokener jsonParser = new JSONTokener(json);  
		    JSONObject grpList = (JSONObject) jsonParser.nextValue();
		    
		    JSONArray groups = grpList.getJSONArray("groupList"); 
		    for(int i = 0; i < groups.length(); i++) {
		    	JSONObject group = groups.getJSONObject(i);
		    	
		    	tempGrp = new GroupUser();
		    	tempGrp.setName(group.getString("name"));
		    	tempGrp.setNickName(group.getString("nickname"));
		    	tempGrp.setOwnerName(group.getString("ownerName"));
		    	
		    	ArrayList<String> members = new ArrayList<String>();
		    	
		    	JSONArray mbs = group.getJSONArray("groupUsers");
		    	for(int j = 0; j < mbs.length(); j++) {
		    		members.add(mbs.getString(j));
		    	}
		    	tempGrp.setGroupUsers(members);
		    	tempGroupList.add(tempGrp);
		    }
		    
		} catch (JSONException e) {  
			e.printStackTrace();
			return;
		}
		this.groupList = tempGroupList;
	}
	
	public String dumpGroupList() {
		JSONArray members = null;
		JSONArray groups = new JSONArray();
		JSONObject group = null;
		JSONObject grpList = new JSONObject();;
		try {
			for(GroupUser grp: groupList) {
				group = new JSONObject();
				
				group.put("name", grp.getName());
				group.put("nickname", grp.getNickName());
				group.put("ownerName", grp.getOwnerName());
				
				for(String m: grp.getGroupUsers()) {
					members = new JSONArray();
					members.put(m);
				}
				
				group.put("groupUsers", members);
				groups.put(group);
				
			}
			grpList.put("groupList", groups);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return grpList.toString();
	}
	
	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJID() {
		return JID;
	}

	public void setJID(String jID) {
		JID = jID;
	}
	
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getGlobalID() {
		return globalID;
	}

	public void setGlobalID(String globalID) {
		this.globalID = globalID;
	}

	public Bitmap getHeadImg() {
		return headImg;
	}

	public void setHeadImg(Bitmap headImg) {
		this.headImg = headImg;
	}
	
	public String getNamepy() {
		return namepy;
	}

	public void setNamepy(String namepy) {
		this.namepy = namepy;
	}

	public String getNameFirstChar() {
		return nameFirstChar;
	}

	public void setNameFirstChar(String nameFirstChar) {
		this.nameFirstChar = nameFirstChar;
	}

	public String getSortLetters() {
		return sortLetters;
	}

	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}

	public String getNamecode() {
		return namecode;
	}

	public void setNamecode(String namecode) {
		this.namecode = namecode;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
	
		return "User: name = " + name + 
				", JID = " + JID +
				", sip_account_id = " + sipAccountId + 
				", nickname = " + nickName + 
				", remark = " + remark +
				", gender = " + gender +
				", contactName = " + contactName +
				", location = " + location +
				", globalID = " + globalID +
				", namepy = " + namepy +
				", nameFirstChar = " + nameFirstChar +
				", sortLetters = " + sortLetters +
				", namecode = " + namecode +
				", " + (headImg != null ? "has avatar" : "has no avatar" +
				", " + (publicKey != null ? "has public key" : "has no public key") + 
				", " + (privateKey != null ? "has private key" : "has no private key") +
				", in " + groupList.size() + " groups.");
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(JID);
		dest.writeLong(sipAccountId);
		dest.writeString(nickName);
		dest.writeString(remark);
		dest.writeString(gender);
		dest.writeString(contactName);
		dest.writeString(location);
		dest.writeString(globalID);
		dest.writeParcelable(headImg, flags);
		dest.writeString(namepy);
		dest.writeString(nameFirstChar);
		dest.writeString(sortLetters);
		dest.writeString(namecode);
		dest.writeString(publicKey);
		dest.writeString(privateKey);
		dest.writeSerializable(groupList);

	}

	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

		@Override
		public User createFromParcel(Parcel source) {
			User u = new User();
			u.name = source.readString();
			u.JID = source.readString();
			u.sipAccountId = source.readLong();
			u.nickName = source.readString();
			u.remark = source.readString();
			u.gender = source.readString();
			u.contactName = source.readString();
			u.location = source.readString();
			u.globalID = source.readString();
			u.headImg = source.readParcelable(Bitmap.class.getClassLoader());
			u.namepy = source.readString();
			u.nameFirstChar = source.readString();
			u.sortLetters = source.readString();
			u.namecode = source.readString();
			u.publicKey = source.readString();
			u.privateKey = source.readString();
			u.groupList = (ArrayList<GroupUser>)source.readSerializable();
			
			return u;
		}

		@Override
		public User[] newArray(int size) {
			return new User[size];
		}

	};

	@Override
	public User clone() {
		User user = new User();
		user.setName(User.this.name);
		user.setJID(User.this.JID);
		user.setSipAccountId(User.this.sipAccountId);
		user.setNickName(User.this.nickName);
		user.setRemark(User.this.remark);
		user.setGender(User.this.gender);
		user.setContactName(User.this.contactName);
		user.setLocation(User.this.location);
		user.setGlobalID(User.this.globalID);
		user.setHeadImg(User.this.headImg);
		user.setNamepy(User.this.namepy);
		user.setNameFirstChar(User.this.nameFirstChar);
		user.setSortLetters(User.this.sortLetters);
		user.setNamecode(User.this.namecode);
		user.setPublicKey(User.this.publicKey);
		user.setPrivateKey(User.this.privateKey);
		user.setGroupList(User.this.groupList);

		return user;
	}

}
