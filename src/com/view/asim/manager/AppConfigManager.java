package com.view.asim.manager;

import com.view.asim.comm.ApplicationContext;
import com.view.asim.comm.Constant;
import com.view.asim.model.LoginConfig;

import android.content.Context;
import android.content.SharedPreferences;

public class AppConfigManager {
	
	private SharedPreferences mImPref;
	private SharedPreferences mSipPref;
	private Context mCntx;


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
	}

	public synchronized SharedPreferences getImPref() {
		return mImPref;
	}

	public synchronized SharedPreferences getSipPref() {
		return mSipPref;
	}
	
	public synchronized void saveLoginConfig(LoginConfig loginConfig) {
		mImPref.edit()
				.putString(Constant.XMPP_HOST, loginConfig.getXmppHost())
				.commit();
		mImPref.edit()
				.putInt(Constant.XMPP_PORT, loginConfig.getXmppPort()).commit();
		mImPref
				.edit()
				.putString(Constant.XMPP_SERVICE_NAME,
						loginConfig.getXmppServiceName()).commit();
		mImPref.edit()
				.putString(Constant.USERNAME, loginConfig.getUsername())
				.commit();
		mImPref.edit()
				.putString(Constant.PASSWORD, loginConfig.getPassword())
				.commit();
		mImPref.edit()
				.putBoolean(Constant.IS_ONLINE, loginConfig.isOnline())
				.commit();
		mImPref.edit()
				.putString(Constant.DATA_ROOT_PATH, loginConfig.getRootPath())
				.commit();
	}

	public synchronized LoginConfig getLoginConfig() {
		LoginConfig loginConfig = new LoginConfig();
		loginConfig.setXmppHost(mImPref.getString(Constant.XMPP_HOST,
				Constant.IM_SERVICE_HOST));
		loginConfig.setXmppPort(mImPref.getInt(Constant.XMPP_PORT,
				Constant.IM_SERVICE_PORT));
		loginConfig.setUsername(mImPref.getString(Constant.USERNAME, null));
		loginConfig.setPassword(mImPref.getString(Constant.PASSWORD, null));
		loginConfig.setXmppServiceName(mImPref.getString(
				Constant.XMPP_SERVICE_NAME,
				Constant.IM_SERVICE_NAME));
		loginConfig.setRootPath(mImPref.getString(Constant.DATA_ROOT_PATH, null));
		return loginConfig;
	}

	public synchronized boolean getUserOnlineState() {
		return mImPref.getBoolean(Constant.IS_ONLINE, true);
	}

	public synchronized void setUserOnlineState(boolean isOnline) {
		mImPref.edit().putBoolean(Constant.IS_ONLINE, isOnline).commit();
	}
	
	public synchronized String getDataRootPath() {
		return mImPref.getString(Constant.DATA_ROOT_PATH, null);
	}

	public synchronized void setDataRootPath(String path) {
		mImPref.edit().putString(Constant.DATA_ROOT_PATH, path).commit();

	}
	
	
}
