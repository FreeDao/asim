package com.view.asim.activity;

import com.view.asim.model.LoginConfig;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Activity帮助支持类接口.
 * 
 * @author xuweinan
 */
public interface IActivitySupport {
	/**
	 * 
	 * 获取EimApplication.
	 * 
	 * @author xuweinan
	 */
	public abstract AsimApplication getEimApplication();

	/**
	 * 
	 * 终止服务.
	 * 
	 * @author xuweinan
	 */
	public abstract void stopService();

	/**
	 * 
	 * 开启服务.
	 * 
	 * @author xuweinan
	 */
	public abstract void startService();

	/**
	 * 
	 * 校验网络-如果没有网络就弹出设置,并返回true.
	 * 
	 * @return
	 * @author xuweinan
	 */
	public abstract boolean validateInternet();

	/**
	 * 
	 * 校验网络-如果没有网络就返回true.
	 * 
	 * @return
	 * @author xuweinan
	 */
	public abstract boolean hasInternetConnected();

	/**
	 * 
	 * 退出应用.
	 * 
	 * @author xuweinan
	 */
	public abstract void isExit();

	/**
	 * 
	 * 判断GPS是否已经开启.
	 * 
	 * @return
	 * @author xuweinan
	 */
	public abstract boolean hasLocationGPS();

	/**
	 * 
	 * 判断基站是否已经开启.
	 * 
	 * @return
	 * @author xuweinan
	 */
	public abstract boolean hasLocationNetWork();

	/**
	 * 
	 * 显示toast.
	 * 
	 * @param text
	 *            内容
	 * @param longint
	 *            内容显示多长时间
	 * @author xuweinan
	 */
	public abstract void showToast(String text, int longint);

	/**
	 * 
	 * 短时间显示toast.
	 * 
	 * @param text
	 * @author xuweinan
	 */
	public abstract void showToast(String text);

	/**
	 * 
	 * 获取进度条.
	 * 
	 * @return
	 * @author xuweinan
	 */
	public abstract ProgressDialog getProgressDialog();

	/**
	 * 
	 * 返回当前Activity上下文.
	 * 
	 * @return
	 * @author xuweinan
	 */
	public abstract Context getContext();

	/**
	 * 
	 * 获取当前登录用户的SharedPreferences配置.
	 * 
	 * @return
	 * @author xuweinan
	 */
	public SharedPreferences getLoginUserSharedPre();

	/**
	 * 
	 * 保存用户配置.
	 * 
	 * @param loginConfig
	 * @author xuweinan
	 */
	public void saveLoginConfig(LoginConfig loginConfig);

	/**
	 * 
	 * 获取用户配置.
	 * 
	 * @param loginConfig
	 * @author xuweinan
	 */
	public LoginConfig getLoginConfig();

	/**
	 * 
	 * 用户是否在线（当前网络是否重连成功）
	 * 
	 * @param loginConfig
	 * @author xuweinan
	 */
	public boolean getUserOnlineState();

	/**
	 * 设置用户在线状态 true 在线 false 不在线
	 * 
	 * @param isOnline
	 * @author xuweinan
	 */
	public void setUserOnlineState(boolean isOnline);
	
	/**
	 * 获取 App 保存数据的根目录
	 * @return
	 * @author xuweinan
	 */
	public String getDataRootPath();
	
	/**
	 * 保存 App 保存数据的根目录
	 * @param path
	 * @author xuweinan
	 */
	public void setDataRootPath(String path);
}
