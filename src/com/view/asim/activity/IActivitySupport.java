package com.view.asim.activity;

import com.view.asim.model.LoginConfig;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Activity����֧����ӿ�.
 * 
 * @author xuweinan
 */
public interface IActivitySupport {
	/**
	 * 
	 * ��ȡEimApplication.
	 * 
	 * @author xuweinan
	 */
	public abstract AsimApplication getEimApplication();

	/**
	 * 
	 * ��ֹ����.
	 * 
	 * @author xuweinan
	 */
	public abstract void stopService();

	/**
	 * 
	 * ��������.
	 * 
	 * @author xuweinan
	 */
	public abstract void startService();

	/**
	 * 
	 * У������-���û������͵�������,������true.
	 * 
	 * @return
	 * @author xuweinan
	 */
	public abstract boolean validateInternet();

	/**
	 * 
	 * У������-���û������ͷ���true.
	 * 
	 * @return
	 * @author xuweinan
	 */
	public abstract boolean hasInternetConnected();

	/**
	 * 
	 * �˳�Ӧ��.
	 * 
	 * @author xuweinan
	 */
	public abstract void isExit();

	/**
	 * 
	 * �ж�GPS�Ƿ��Ѿ�����.
	 * 
	 * @return
	 * @author xuweinan
	 */
	public abstract boolean hasLocationGPS();

	/**
	 * 
	 * �жϻ�վ�Ƿ��Ѿ�����.
	 * 
	 * @return
	 * @author xuweinan
	 */
	public abstract boolean hasLocationNetWork();

	/**
	 * 
	 * ��ʾtoast.
	 * 
	 * @param text
	 *            ����
	 * @param longint
	 *            ������ʾ�೤ʱ��
	 * @author xuweinan
	 */
	public abstract void showToast(String text, int longint);

	/**
	 * 
	 * ��ʱ����ʾtoast.
	 * 
	 * @param text
	 * @author xuweinan
	 */
	public abstract void showToast(String text);

	/**
	 * 
	 * ��ȡ������.
	 * 
	 * @return
	 * @author xuweinan
	 */
	public abstract ProgressDialog getProgressDialog();

	/**
	 * 
	 * ���ص�ǰActivity������.
	 * 
	 * @return
	 * @author xuweinan
	 */
	public abstract Context getContext();

	/**
	 * 
	 * ��ȡ��ǰ��¼�û���SharedPreferences����.
	 * 
	 * @return
	 * @author xuweinan
	 */
	public SharedPreferences getLoginUserSharedPre();

	/**
	 * 
	 * �����û�����.
	 * 
	 * @param loginConfig
	 * @author xuweinan
	 */
	public void saveLoginConfig(LoginConfig loginConfig);

	/**
	 * 
	 * ��ȡ�û�����.
	 * 
	 * @param loginConfig
	 * @author xuweinan
	 */
	public LoginConfig getLoginConfig();

	/**
	 * 
	 * �û��Ƿ����ߣ���ǰ�����Ƿ������ɹ���
	 * 
	 * @param loginConfig
	 * @author xuweinan
	 */
	public boolean getUserOnlineState();

	/**
	 * �����û�����״̬ true ���� false ������
	 * 
	 * @param isOnline
	 * @author xuweinan
	 */
	public void setUserOnlineState(boolean isOnline);
	
	/**
	 * ��ȡ App �������ݵĸ�Ŀ¼
	 * @return
	 * @author xuweinan
	 */
	public String getDataRootPath();
	
	/**
	 * ���� App �������ݵĸ�Ŀ¼
	 * @param path
	 * @author xuweinan
	 */
	public void setDataRootPath(String path);
}
