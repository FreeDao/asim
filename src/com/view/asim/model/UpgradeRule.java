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
import android.util.Log;

/**
 * OTA 升级规则
 * 
 * @author  xuweinan
 * 
 */
public class UpgradeRule implements Parcelable {

	/**
	 * 将user保存在intent中时的key
	 */
	public static final String upgradeRuleKey = "ota_upgrade_rule";

	private static final String TAG = "UpgradeRule";

	protected String srcVer = null;
	protected String tgtVer = null;
	protected String name = null;
	protected String size = null;
	protected String sha1 = null;

	public String getSrcVer() {
		return srcVer;
	}

	public void setSrcVer(String srcVer) {
		this.srcVer = srcVer;
	}

	public String getTgtVer() {
		return tgtVer;
	}

	public void setTgtVer(String tgtVer) {
		this.tgtVer = tgtVer;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getSha1() {
		return sha1;
	}

	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}

	public UpgradeRule() {
		super();
	}
	
	public static List<UpgradeRule> loads(String json) {
		UpgradeRule tempRule = null;
		List<UpgradeRule> tempRules = new ArrayList<UpgradeRule>();
		
		if (json == null || json.length() == 0) {
			return null;
		}
		
		try {  
		    //JSONTokener jsonParser = new JSONTokener(json);  
		    //JSONArray rules = (JSONArray) jsonParser.nextValue();
			Log.d(TAG, "json: " + json);
			
			JSONArray rules = new JSONArray(json);
			
	    	for(int i = 0; i < rules.length(); i++) {
	    		JSONObject rule = rules.getJSONObject(i);
			    tempRule = new UpgradeRule();
			    tempRule.setSrcVer(rule.getString("src_ver"));
			    tempRule.setTgtVer(rule.getString("tgt_ver"));
			    tempRule.setName(rule.getString("name"));
			    tempRule.setSize(rule.getString("size"));
			    tempRule.setSha1(rule.getString("sha1"));
			    tempRules.add(tempRule);
	    	}
		} catch (JSONException e) {  
			e.printStackTrace();
			return null;
		}

		return tempRules;
	}

	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
	
		String str = "UpgradeRule: srcVer = " + srcVer + 
					", tgtVer = " + tgtVer + 
					", name = " + name +
					", size = " + size +
					", sha1 = " + sha1;
		return str;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(srcVer);
		dest.writeString(tgtVer);
		dest.writeString(name);
		dest.writeString(size);
		dest.writeString(sha1);
	}

	public static final Parcelable.Creator<UpgradeRule> CREATOR = new Parcelable.Creator<UpgradeRule>() {

		@Override
		public UpgradeRule createFromParcel(Parcel source) {
			UpgradeRule u = new UpgradeRule();
			u.srcVer = source.readString();
			u.tgtVer = source.readString();
			u.name = source.readString();
			u.size = source.readString();
			u.sha1 = source.readString();

			return u;
		}

		@Override
		public UpgradeRule[] newArray(int size) {
			return new UpgradeRule[size];
		}

	};

	@Override
	public UpgradeRule clone() {
		UpgradeRule user = new UpgradeRule();
		user.setSrcVer(UpgradeRule.this.srcVer);
		user.setTgtVer(UpgradeRule.this.tgtVer);
		user.setName(UpgradeRule.this.name);
		user.setSize(UpgradeRule.this.size);
		user.setSha1(UpgradeRule.this.sha1);

		return user;
	}

}
