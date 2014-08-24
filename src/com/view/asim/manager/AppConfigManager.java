package com.view.asim.manager;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.view.asim.comm.ApplicationContext;
import com.view.asim.comm.Constant;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.Server;
import com.view.asim.model.UpgradeRule;
import com.view.asim.utils.ImageUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * App 全局配置
 * @author xuweinan
 */
public class AppConfigManager {
	
	private static final String TAG = "AppConfigManager";
	private SharedPreferences mImPref;
	private SharedPreferences mSipPref;
	private Context mCntx;
	private LoginConfig mLoginCfg;


	private static class Loader {
        static AppConfigManager INSTANCE = new AppConfigManager();
    }
	 
	public static AppConfigManager getInstance() {
		return Loader.INSTANCE;
	}
	
	private AppConfigManager() {
		mCntx = ApplicationContext.get();
		mImPref = mCntx.getSharedPreferences(Constant.IM_SET_PREF, 0);
		mSipPref = mCntx.getSharedPreferences(Constant.SIP_SET_PREF, 0);
		LoadLoginConfig();
	}

	public synchronized SharedPreferences getImPref() {
		return mImPref;
	}

	public synchronized SharedPreferences getSipPref() {
		return mSipPref;
	}
	
	public synchronized void saveLoginConfig() {
		if (mLoginCfg.getServer() != null) {
			mImPref.edit()
					.putString(Constant.XMPP_HOST, mLoginCfg.getServer().getXmppHost())
					.commit();
			mImPref.edit()
					.putInt(Constant.XMPP_PORT, mLoginCfg.getServer().getXmppPort()).commit();
			mImPref.edit()
					.putString(Constant.XMPP_SERVICE_NAME,
							mLoginCfg.getServer().getXmppServiceName()).commit();
			mSipPref.edit()
					.putString(Constant.SIP_HOST, mLoginCfg.getServer().getSipHost())
						.commit();
			mSipPref.edit()
					.putInt(Constant.SIP_PORT, mLoginCfg.getServer().getSipPort())
					.commit();
			mSipPref.edit()
					.putString(Constant.STUN_HOST, mLoginCfg.getServer().getStunHost())
					.commit();		

		}
		
		mImPref.edit()
				.putString(Constant.USERNAME, mLoginCfg.getUsername())
				.commit();
		mImPref.edit()
				.putString(Constant.PASSWORD, mLoginCfg.getPassword())
				.commit();
		mImPref.edit()
				.putBoolean(Constant.IS_ONLINE, mLoginCfg.isOnline())
				.commit();
		mImPref.edit()
				.putString(Constant.DATA_ROOT_PATH, mLoginCfg.getRootPath())
				.commit();
		mImPref.edit()
				.putString(Constant.APP_UUID, mLoginCfg.getUuid())
				.commit();
		
	}

	public synchronized void LoadLoginConfig() {
		mLoginCfg = null;
		mLoginCfg = new LoginConfig();
		
		Server srv = new Server();
		srv.setXmppHost(mImPref.getString(Constant.XMPP_HOST, null));
		srv.setXmppPort(mImPref.getInt(Constant.XMPP_PORT, -1));
		srv.setXmppServiceName(mImPref.getString(Constant.XMPP_SERVICE_NAME, null));
		srv.setSipHost(mSipPref.getString(Constant.SIP_HOST, null));
		srv.setSipPort(mSipPref.getInt(Constant.SIP_PORT, -1));
		srv.setStunHost(mSipPref.getString(Constant.STUN_HOST, null));

		mLoginCfg.setServer(srv);
		mLoginCfg.setUsername(mImPref.getString(Constant.USERNAME, null));
		mLoginCfg.setPassword(mImPref.getString(Constant.PASSWORD, null));
		mLoginCfg.setRootPath(mImPref.getString(Constant.DATA_ROOT_PATH, null));
		mLoginCfg.setUuid(mImPref.getString(Constant.APP_UUID, getUUID().toString()));
	}
	
	public synchronized long getLoginTime() {
		return mLoginCfg.getLoginTime();
	}
	
	public synchronized void setLoginTime(long time) {
		mLoginCfg.setLoginTime(time);
	}
	
	public synchronized String getUsername() {
		return mLoginCfg.getUsername();
	}
	
	public synchronized String getPassword() {
		return mLoginCfg.getPassword();
	}
	
	public synchronized boolean isOnline() {
		return mLoginCfg.isOnline();
	}
	
	public synchronized String getRootPath() {
		return mLoginCfg.getRootPath();
	}
	
	public synchronized Server getServer() {
		return mLoginCfg.getServer();
	}

	public synchronized String getResource() {
    	String resource = android.os.Build.MANUFACTURER + "." + android.os.Build.DEVICE + "." + mLoginCfg.getUuid();
		return resource;
	}
	
	public synchronized void setUsername(String name) {
		mLoginCfg.setUsername(name);
	}

	public synchronized void setPassword(String pwd) {
		mLoginCfg.setPassword(pwd);
	}

	public synchronized void setOnline(boolean isOnline) {
		mLoginCfg.setOnline(isOnline);
	}

	public synchronized void setRootPath(String path) {
		mLoginCfg.setRootPath(path);
	}
	
	private UUID getUUID() {
		UUID uuid = UUID.randomUUID();
        String androidId = Secure.getString(mCntx.getContentResolver(), Secure.ANDROID_ID);  		
		try {  
            if (!"9774d56d682e549c".equals(androidId)) {  
                uuid = UUID.nameUUIDFromBytes(androidId.getBytes("UTF-8"));  
            } else {  
                final String deviceId = ((TelephonyManager) mCntx.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();  
                uuid = (deviceId != null) ? UUID.nameUUIDFromBytes(deviceId.getBytes("UTF-8")) : uuid;  
            }  
        } catch (UnsupportedEncodingException e) {  
            throw new RuntimeException(e);  
        }  
		
		return uuid;
	}
	
	public synchronized void resolvServer() {
		String resolvApiUrls[] = {
			Constant.SLAVE_DISC_SERVER_URL,
			Constant.MAIN_DISC_SERVER_URL
		};
		
		String result = null;
		Server srv = null;
		
		for (String url: resolvApiUrls) {
			byte[] fileBytes = ImageUtil.getBytesFromUrl(url, Constant.DISCOVERY_TIMEOUT);
			if (fileBytes == null) {
				Log.e(TAG, "resolv server from discovery server failed(" + url + ")");
				continue;
			}
			
			result = new String(fileBytes);
			srv = Server.loads(result); 
			if (srv == null) {
				Log.e(TAG, "parse server info from discovery server(" + url + ")" + " failed(" + result + ")");
				continue;
			}
			
			Log.i(TAG, "resolv server info from discovery server(" + url + ")" + " succ(" + srv + ")");
			break;
		}
		
		if (srv != null) {
			mLoginCfg.setServer(srv);
		}
	}
	
}
