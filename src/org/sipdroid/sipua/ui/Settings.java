/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * 
 * This file is part of Sipdroid (http://www.sipdroid.org)
 * 
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.sipdroid.sipua.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.sipdroid.codecs.Codecs;
import org.sipdroid.media.RtpStreamReceiver;
import org.sipdroid.sipua.SipdroidEngine;
import org.zoolu.sip.provider.SipStack;

import com.view.asim.manager.NoticeManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/**
 * SIP ²ÎÊýÅäÖÃÀà
 * @author xuweinan
 *
 */
public class Settings {
	// Current settings handler
	private static SharedPreferences settings;
	// Context definition
	private Context context = null;
	private static Settings INSTANCE = null;

	// List of profile files available on the SD card
	private String[] profileFiles = null;
	// Which profile file to delete
	private int profileToDelete;

	// IDs of the menu items
	private static final int MENU_IMPORT = 0;
	private static final int MENU_DELETE = 1;
	private static final int MENU_EXPORT = 2;

	// All possible values of the PREF_PREF preference (see bellow) 
	public static final String VAL_PREF_PSTN = "PSTN";
	public static final String VAL_PREF_SIP = "SIP";
	public static final String VAL_PREF_SIPONLY = "SIPONLY";
	public static final String VAL_PREF_ASK = "ASK";

	/*-
	 * ****************************************
	 * **** HOW TO USE SHARED PREFERENCES *****
	 * ****************************************
	 * 
	 * If you need to check the existence of the preference key
	 *   in this class:		contains(PREF_USERNAME)
	 *   in other classes:	PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).contains(Settings.PREF_USERNAME) 
	 * If you need to check the existence of the key or check the value of the preference
	 *   in this class:		getString(PREF_USERNAME, "").equals("")
	 *   in other classes:	PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(Settings.PREF_USERNAME, "").equals("")
	 * If you need to get the value of the preference
	 *   in this class:		getString(PREF_USERNAME, DEFAULT_USERNAME)
	 *   in other classes:	PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(Settings.PREF_USERNAME, Settings.DEFAULT_USERNAME)
	 */

	// Name of the keys in the Preferences XML file
	public static final String PREF_USERNAME = "username";
	public static final String PREF_PASSWORD = "password";
	public static final String PREF_SERVER = "server";
	public static final String PREF_DOMAIN = "domain";
	public static final String PREF_FROMUSER = "fromuser";
	public static final String PREF_PORT = "port";
	public static final String PREF_PROTOCOL = "protocol";
	public static final String PREF_WLAN = "wlan";
	public static final String PREF_3G = "3g";
	public static final String PREF_EDGE = "edge";
	public static final String PREF_VPN = "vpn";
	public static final String PREF_PREF = "pref";
	public static final String PREF_AUTO_ON = "auto_on";
	public static final String PREF_AUTO_ONDEMAND = "auto_on_demand";
	public static final String PREF_AUTO_HEADSET = "auto_headset";
	public static final String PREF_MWI_ENABLED = "MWI_enabled";
	public static final String PREF_REGISTRATION = "registration";
	public static final String PREF_NOTIFY = "notify";
	public static final String PREF_NODATA = "nodata";
	public static final String PREF_SIPRINGTONE = "sipringtone";
	public static final String PREF_SEARCH = "search";
	public static final String PREF_EXCLUDEPAT = "excludepat";
	public static final String PREF_EARGAIN = "eargain";
	public static final String PREF_MICGAIN = "micgain";
	public static final String PREF_HEARGAIN = "heargain";
	public static final String PREF_HMICGAIN = "hmicgain";
	public static final String PREF_OWNWIFI = "ownwifi";
	public static final String PREF_STUN = "stun";
	public static final String PREF_STUN_SERVER = "stun_server";
	public static final String PREF_STUN_SERVER_PORT = "stun_server_port";
	
	// MMTel configurations (added by mandrajg)
	public static final String PREF_MMTEL = "mmtel";
	public static final String PREF_MMTEL_QVALUE = "mmtel_qvalue";
	
	// Call recording preferences.
	public static final String PREF_CALLRECORD = "callrecord";
	
	public static final String PREF_PAR = "par";
	public static final String PREF_IMPROVE = "improve";
	public static final String PREF_POSURL = "posurl";
	public static final String PREF_POS = "pos";
	public static final String PREF_CALLBACK = "callback";
	public static final String PREF_CALLTHRU = "callthru";
	public static final String PREF_CALLTHRU2 = "callthru2";
	public static final String PREF_CODECS = "codecs_new";
	public static final String PREF_DNS = "dns";
	public static final String PREF_VQUALITY = "vquality";
	public static final String PREF_MESSAGE = "vmessage";
	public static final String PREF_BLUETOOTH = "bluetooth";
	public static final String PREF_KEEPON = "keepon";
	public static final String PREF_SELECTWIFI = "selectwifi";
	public static final String PREF_ACCOUNT = "account";
	
	// Default values of the preferences
	public static final String	DEFAULT_USERNAME = "";
	public static final String	DEFAULT_PASSWORD = "";
	public static final String	DEFAULT_SERVER = "112.124.32.193";
	public static final String	DEFAULT_DOMAIN = "";
	public static final String	DEFAULT_FROMUSER = "";
	public static final String	DEFAULT_PORT = "" + SipStack.default_port;
	public static final String	DEFAULT_PROTOCOL = "udp";
	public static final boolean	DEFAULT_WLAN = true;
	public static final boolean	DEFAULT_3G = true;
	public static final boolean	DEFAULT_EDGE = true;
	public static final boolean	DEFAULT_VPN = false;
	public static final String	DEFAULT_PREF = VAL_PREF_SIP;
	public static final boolean	DEFAULT_AUTO_ON = false;
	public static final boolean	DEFAULT_AUTO_ONDEMAND = false;
	public static final boolean	DEFAULT_AUTO_HEADSET = false;
	public static final boolean	DEFAULT_MWI_ENABLED = true;
	public static final boolean DEFAULT_REGISTRATION = true;
	public static final boolean	DEFAULT_NOTIFY = false;
	public static final boolean	DEFAULT_NODATA = false;
	public static final String	DEFAULT_SIPRINGTONE = "";
	public static final String	DEFAULT_SEARCH = "";
	public static final String	DEFAULT_EXCLUDEPAT = "";
	public static final float	DEFAULT_EARGAIN = (float) 0.25;
	public static final float	DEFAULT_MICGAIN = (float) 0.25;
	public static final float	DEFAULT_HEARGAIN = (float) 0.25;
	public static final float	DEFAULT_HMICGAIN = (float) 1.0;
	public static final boolean	DEFAULT_OWNWIFI = false;
	public static final boolean	DEFAULT_STUN = false;
	public static final String	DEFAULT_STUN_SERVER = "stun.ekiga.net";
	public static final String	DEFAULT_STUN_SERVER_PORT = "3478";
	
	// MMTel configuration (added by mandrajg)
	public static final boolean	DEFAULT_MMTEL = false;
	public static final String	DEFAULT_MMTEL_QVALUE = "1.00";	

	// Call recording preferences.
	public static final boolean DEFAULT_CALLRECORD = false;
	
	public static final boolean	DEFAULT_PAR = false;
	public static final boolean	DEFAULT_IMPROVE = false;
	public static final String	DEFAULT_POSURL = "";
	public static final boolean	DEFAULT_POS = false;
	public static final boolean	DEFAULT_CALLBACK = false;
	public static final boolean	DEFAULT_CALLTHRU = false;
	public static final String	DEFAULT_CALLTHRU2 = "";
	public static final String	DEFAULT_CODECS = null;
	public static final String	DEFAULT_DNS = "";
	public static final String  DEFAULT_VQUALITY = "low";
	public static final boolean DEFAULT_MESSAGE = false;
	public static final boolean DEFAULT_BLUETOOTH = false;
	public static final boolean DEFAULT_KEEPON = false;
	public static final boolean DEFAULT_SELECTWIFI = false;
	public static final int     DEFAULT_ACCOUNT = 0;

	// An other preference keys (not in the Preferences XML file)
	public static final String PREF_OLDVALID = "oldvalid";
	public static final String PREF_SETMODE = "setmode";
	public static final String PREF_OLDVIBRATE = "oldvibrate";
	public static final String PREF_OLDVIBRATE2 = "oldvibrate2";
	public static final String PREF_OLDPOLICY = "oldpolicy";
	public static final String PREF_OLDRING = "oldring";
	public static final String PREF_AUTO_DEMAND = "auto_demand";
	public static final String PREF_WIFI_DISABLED = "wifi_disabled";
	public static final String PREF_ON_VPN = "on_vpn";
	public static final String PREF_NODEFAULT = "nodefault";
	public static final String PREF_NOPORT = "noport";
	public static final String PREF_ON = "on";
	public static final String PREF_PREFIX = "prefix";
	public static final String PREF_COMPRESSION = "compression";
	//public static final String PREF_RINGMODEx = "ringmodeX";
	//public static final String PREF_VOLUMEx = "volumeX";

	// Default values of the other preferences
	public static final boolean	DEFAULT_OLDVALID = false;
	public static final boolean	DEFAULT_SETMODE = false;
	public static final int		DEFAULT_OLDVIBRATE = 0;
	public static final int		DEFAULT_OLDVIBRATE2 = 0;
	public static final int		DEFAULT_OLDPOLICY = 0;
	public static final int		DEFAULT_OLDRING = 0;
	public static final boolean	DEFAULT_AUTO_DEMAND = false;
	public static final boolean	DEFAULT_WIFI_DISABLED = false;
	public static final boolean DEFAULT_ON_VPN = false;
	public static final boolean	DEFAULT_NODEFAULT = false;
	public static final boolean DEFAULT_NOPORT = false;
	public static final boolean	DEFAULT_ON = false;
	public static final String	DEFAULT_PREFIX = "";
	public static final String	DEFAULT_COMPRESSION = null;
	//public static final String	DEFAULT_RINGTONEx = "";
	//public static final String	DEFAULT_VOLUMEx = "";

	public static float getEarGain() {
		try {
			return Float.valueOf(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(Receiver.headset > 0 ? PREF_HEARGAIN : PREF_EARGAIN, "" + DEFAULT_EARGAIN));
		} catch (NumberFormatException i) {
			return DEFAULT_EARGAIN;
		}			
	}

	public static float getMicGain() {
		if (Receiver.headset > 0 || Receiver.bluetooth > 0) {
			try {
				return Float.valueOf(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(PREF_HMICGAIN, "" + DEFAULT_HMICGAIN));
			} catch (NumberFormatException i) {
				return DEFAULT_HMICGAIN;
			}			
		}

		try {
			return Float.valueOf(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(PREF_MICGAIN, "" + DEFAULT_MICGAIN));
		} catch (NumberFormatException i) {
			return DEFAULT_MICGAIN;
		}			
	}

	private Settings(Context cntx) {
		this.context = cntx;
		setDefaultValues();
	}

	public static Settings getInstance(Context cntx) {

		if (INSTANCE == null) {
			INSTANCE = new Settings(cntx);
		}

		return INSTANCE;
	}
	
	public static Settings getInstance() {
		return INSTANCE;
	}
	
	private void setDefaultValues() {
		settings = PreferenceManager.getDefaultSharedPreferences(context);
		for (int i = 0; i < SipdroidEngine.LINES; i++) {
			String j = (i != 0 ? "" + i : "");
			if (settings.getString(PREF_SERVER+j, "").equals("")) {
				Editor edit = settings.edit();

				edit.putString(PREF_PORT+j, DEFAULT_PORT);
				edit.putString(PREF_SERVER+j, DEFAULT_SERVER);
				edit.putString(PREF_PREF+j, DEFAULT_PREF);				
				edit.putString(PREF_PROTOCOL+j, DEFAULT_PROTOCOL);
				edit.commit();
	        	Receiver.engine(context).updateDNS();
			}
		}
		if (settings.getString(PREF_STUN_SERVER, "").equals("")) {
			Editor edit = settings.edit();

			edit.putString(PREF_STUN_SERVER, DEFAULT_STUN_SERVER);
			edit.putString(PREF_STUN_SERVER_PORT, DEFAULT_STUN_SERVER_PORT);				
			edit.commit();
		}
		Codecs.check();
	}

    public static String getProfileNameString(SharedPreferences s) {
    	String provider = s.getString(PREF_SERVER, DEFAULT_SERVER);

    	if (! s.getString(PREF_DOMAIN, "").equals("")) {
    		provider = s.getString(PREF_DOMAIN, DEFAULT_DOMAIN);
    	}

    	return s.getString(PREF_USERNAME, DEFAULT_USERNAME) + "@" + provider;
    }

}