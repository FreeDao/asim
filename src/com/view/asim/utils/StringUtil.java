package com.view.asim.utils;

import android.provider.SyncStateContract.Constants;

import com.view.asim.comm.Constant;
import com.view.asim.manager.AppConfigManager;
import com.view.asim.model.Server;
import com.view.asim.model.User;

public class StringUtil {
	/**
	 * ������ַ���
	 * 
	 * @param str
	 * @return String
	 */
	public static String doEmpty(String str) {
		return doEmpty(str, "");
	}

	/**
	 * ������ַ���
	 * 
	 * @param str
	 * @param defaultValue
	 * @return String
	 */
	public static String doEmpty(String str, String defaultValue) {
		if (str == null || str.equalsIgnoreCase("null")
				|| str.trim().equals("") || str.trim().equals("����ѡ��")) {
			str = defaultValue;
		} else if (str.startsWith("null")) {
			str = str.substring(4, str.length());
		}
		return str.trim();
	}

	/**
	 * ��ѡ��
	 */
	final static String PLEASE_SELECT = "��ѡ��...";

	public static boolean notEmpty(Object o) {
		return o != null && !"".equals(o.toString().trim())
				&& !"null".equalsIgnoreCase(o.toString().trim())
				&& !"undefined".equalsIgnoreCase(o.toString().trim())
				&& !PLEASE_SELECT.equals(o.toString().trim());
	}

	public static boolean empty(Object o) {
		return o == null || "".equals(o.toString().trim())
				|| "null".equalsIgnoreCase(o.toString().trim())
				|| "undefined".equalsIgnoreCase(o.toString().trim())
				|| PLEASE_SELECT.equals(o.toString().trim());
	}

	public static boolean num(Object o) {
		int n = 0;
		try {
			n = Integer.parseInt(o.toString().trim());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		if (n > 0) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean decimal(Object o) {
		double n = 0;
		try {
			n = Double.parseDouble(o.toString().trim());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		if (n > 0.0) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isNumeric(String str){
		for (int i = str.length(); --i >= 0;) {   
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * ��JID�����û���
	 * 
	 * @param Jid
	 * @return
	 */
	public static String getUserNameByJid(String Jid) {
		if (empty(Jid)) {
			return null;
		}
		if (!Jid.contains("@")) {
			return Jid;
		}
		return Jid.split("@")[0];
	}

	/**
	 * ���û�������JID
	 * 
	 * @param jidFor
	 *            ����//��ahic.com.cn
	 * @param userName
	 * @return
	 */
	public static String getJidByName(String userName, String jidFor) {
		if (empty(jidFor) || empty(jidFor)) {
			return null;
		}
		return userName + "@" + jidFor;
	}

	/**
	 * ���û�������JID,ʹ��Ĭ������ahic.com.cn
	 * 
	 * @param userName
	 * @return
	 */
	public static String getJidByName(String userName) {
		Server srv = AppConfigManager.getInstance().getServer();

		return getJidByName(userName, srv.getXmppServiceName());
	}

	/**
	 * ���û��������ֻ���
	 * 
	 * @param userName
	 * @return
	 */
	public static String getCellphoneByName(String userName) {
		if (empty(userName)) {
			return null;
		}
		return userName.split(User.NAME_PREFIX)[1];
	}
	
	/**
	 * �����ֻ��������û���
	 * 
	 * @param userName
	 * @return
	 */
	public static String getJidByCellphone(String cellphone) {
		if (empty(cellphone)) {
			return null;
		}
		String name = getNameByCellphone(cellphone);
		
		return getJidByName(name);
	}
	
	/**
	 * �����ֻ��������û���
	 * 
	 * @param userName
	 * @return
	 */
	public static String getNameByCellphone(String cellphone) {
		if (empty(cellphone)) {
			return null;
		}
		return User.NAME_PREFIX + cellphone;
	}
	
	public static String getImJidByVoipUserName(String voipUserName) {
		if (empty(voipUserName)) {
			return null;
		}
		Server srv = AppConfigManager.getInstance().getServer();

		return User.NAME_PREFIX + voipUserName.split("@")[0] + srv.getXmppServiceName();
	}
	
	/**
	 * ���ݸ�����ʱ���ַ����������� �� ʱ �� ��
	 * 
	 * @param allDate
	 *            like "yyyy-MM-dd hh:mm:ss SSS"
	 * @return
	 */
	public static String getMonthTomTime(String allDate) {
		return allDate.substring(5, 19);
	}

	/**
	 * ���ݸ�����ʱ���ַ����������� �� ʱ �� �µ�����
	 * 
	 * @param allDate
	 *            like "yyyy-MM-dd hh:mm:ss SSS"
	 * @return
	 */
	public static String getMonthTime(String allDate) {
		return allDate.substring(5, 16);
	}
}
