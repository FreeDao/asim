package com.view.asim.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Ⱥ���飨�����û���
 * 
 * @author  xuweinan
 * 
 */
public class GroupUser implements Parcelable {

	/**
	 * ��user������intent��ʱ��key
	 */
	public static final String groupUserKey = "aim_group_user";
	public static final String firstCreateKey = "aim_group_first_create";


	// Ⱥ��
	protected String name = null;

	// Ⱥ�ǳ�
	protected String nickName = null;
	
	// Ⱥ���û���
	protected String ownerName = null;

	// Ⱥ���û����б�
	protected ArrayList<String> groupUsers = null;
	
	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public List<String> getGroupUsers() {
		return groupUsers;
	}

	public void setGroupUsers(ArrayList<String> groupUsers) {
		this.groupUsers = groupUsers;
	}

	public void addMember(User u) {
		if (u != null) {
			groupUsers.add(u.getName());
		}
	}
	
	public void delMember(User u) {
		if (u != null) {
			for (String gu: groupUsers) {
				if (gu.equals(u.getName())) {
					groupUsers.remove(gu);
				}
			}
		}
	}

	public GroupUser() {
		super();
		groupUsers = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	
	public static GroupUser loads(String json) {
		GroupUser tempGrp = null;
		
		if (json == null || json.length() == 0) {
			return null;
		}
		
		try {  
		    JSONTokener jsonParser = new JSONTokener(json);  
		    JSONObject group = (JSONObject) jsonParser.nextValue();
		    
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
		    
		} catch (JSONException e) {  
			e.printStackTrace();
			return null;
		}

		return tempGrp;
	}
	
	public String dumps() {
		JSONArray members = null;
		JSONObject group = null;
		try {
			group = new JSONObject();
			
			group.put("name", this.name);
			group.put("nickname", this.nickName);
			group.put("ownerName", this.ownerName);
			
			for(String m: this.groupUsers) {
				members = new JSONArray();
				members.put(m);
			}
			
			group.put("groupUsers", members);
				
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return group.toString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
	
		String str = "GroupUser: name = " + name + 
					", nickname = " + nickName + 
					", ownerName = " + ownerName +
					", member[";
		for (String m: groupUsers) {
			str += m + " ";
		}
		
		return str;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(nickName);
		dest.writeString(ownerName);
		dest.writeSerializable(groupUsers);

	}

	public static final Parcelable.Creator<GroupUser> CREATOR = new Parcelable.Creator<GroupUser>() {

		@Override
		public GroupUser createFromParcel(Parcel source) {
			GroupUser u = new GroupUser();
			u.name = source.readString();
			u.nickName = source.readString();
			u.ownerName = source.readString();
			u.groupUsers = (ArrayList<String>)source.readSerializable();
			return u;
		}

		@Override
		public GroupUser[] newArray(int size) {
			return new GroupUser[size];
		}

	};

	@Override
	public GroupUser clone() {
		GroupUser user = new GroupUser();
		user.setName(GroupUser.this.name);
		user.setNickName(GroupUser.this.nickName);
		user.setOwnerName(GroupUser.this.ownerName);
		user.setGroupUsers(GroupUser.this.groupUsers);

		return user;
	}

}
