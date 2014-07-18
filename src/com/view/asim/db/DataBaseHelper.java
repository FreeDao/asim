package com.view.asim.db;

import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipProfile;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.provider.CallLog;

/**
 * SQLite数据库的帮助类
 * 
 * 该类属于扩展类,主要承担数据库初始化和版本升级使用,其他核心全由核心父类完成
 * 
 * @author xuweinan
 * 
 */
public class DataBaseHelper extends SDCardSQLiteOpenHelper {
	
	private static DataBaseHelper INSTANCE = null;
	
	// Creation sql command
	private static final String TABLE_IM_MSG_CREATE = "CREATE TABLE IF NOT EXISTS "
			+ "[im_msg_his]"
			+ "([_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
			" [unique_id] NVARCHAR, " +
			" [status] NVARCHAR," +
			" [readStatus] NVARCHAR, " +
			" [content] NVARCHAR, " +
			" [with] NVARCHAR, " +
			" [src] NVARCHAR, " +
			" [time] NVARCHAR, " +
			" [type] NVARCHAR, " +
			" [dir] NVARCHAR, " +
			" [chatType] NVARCHAR, " +
			" [security] NVARCHAR, " +
			" [destroy] NVARCHAR, " +
			" [attachment] NVARCHAR);";
	
	private static final String TABLE_IM_NOTICE_CREATE = "CREATE TABLE IF NOT EXISTS" +
			" [im_notice] " +
			" ([_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
			" [dispStatus] NVARCHAR," +
			" [readStatus] NVARCHAR," +
			" [content] NVARCHAR," +
			" [with] NVARCHAR," +
			" [time] NVARCHAR," +
			" [status] NVARCHAR," +
			" [avatar_img] BLOB);";
	
	private static final String TABLE_SIP_ACCOUNT_CREATE = "CREATE TABLE IF NOT EXISTS "
		+ SipProfile.ACCOUNTS_TABLE_NAME
		+ " ("
			+ SipProfile.FIELD_ID+ 				" INTEGER PRIMARY KEY AUTOINCREMENT,"

			// Application relative fields
			+ SipProfile.FIELD_ACTIVE				+ " INTEGER,"
			+ SipProfile.FIELD_WIZARD				+ " TEXT,"
			+ SipProfile.FIELD_DISPLAY_NAME		+ " TEXT,"

			// Here comes pjsua_acc_config fields
			+ SipProfile.FIELD_PRIORITY 			+ " INTEGER," 
			+ SipProfile.FIELD_ACC_ID 				+ " TEXT NOT NULL,"
			+ SipProfile.FIELD_REG_URI				+ " TEXT," 
			+ SipProfile.FIELD_MWI_ENABLED 		+ " BOOLEAN,"
			+ SipProfile.FIELD_PUBLISH_ENABLED 	+ " INTEGER," 
			+ SipProfile.FIELD_REG_TIMEOUT 		+ " INTEGER," 
			+ SipProfile.FIELD_KA_INTERVAL 		+ " INTEGER," 
			+ SipProfile.FIELD_PIDF_TUPLE_ID 		+ " TEXT,"
			+ SipProfile.FIELD_FORCE_CONTACT 		+ " TEXT,"
			+ SipProfile.FIELD_ALLOW_CONTACT_REWRITE + " INTEGER,"
			+ SipProfile.FIELD_CONTACT_REWRITE_METHOD + " INTEGER,"
			+ SipProfile.FIELD_CONTACT_PARAMS 		+ " TEXT,"
			+ SipProfile.FIELD_CONTACT_URI_PARAMS	+ " TEXT,"
			+ SipProfile.FIELD_TRANSPORT	 		+ " INTEGER," 
	        + SipProfile.FIELD_DEFAULT_URI_SCHEME           + " TEXT," 
			+ SipProfile.FIELD_USE_SRTP	 			+ " INTEGER," 
			+ SipProfile.FIELD_USE_ZRTP	 			+ " INTEGER," 

			// Proxy infos
			+ SipProfile.FIELD_PROXY				+ " TEXT,"
			+ SipProfile.FIELD_REG_USE_PROXY		+ " INTEGER,"

			// And now cred_info since for now only one cred info can be managed
			// In future release a credential table should be created
			+ SipProfile.FIELD_REALM 				+ " TEXT," 
			+ SipProfile.FIELD_SCHEME 				+ " TEXT," 
			+ SipProfile.FIELD_USERNAME				+ " TEXT," 
			+ SipProfile.FIELD_DATATYPE 			+ " INTEGER," 
			+ SipProfile.FIELD_DATA 				+ " TEXT,"
			+ SipProfile.FIELD_AUTH_INITIAL_AUTH + " INTEGER," 
	        + SipProfile.FIELD_AUTH_ALGO      + " TEXT,"
			
			
			+ SipProfile.FIELD_SIP_STACK 			+ " INTEGER," 
			+ SipProfile.FIELD_VOICE_MAIL_NBR		+ " TEXT,"
			+ SipProfile.FIELD_REG_DELAY_BEFORE_REFRESH	+ " INTEGER," 
			
			+ SipProfile.FIELD_TRY_CLEAN_REGISTERS	+ " INTEGER,"
			
			+ SipProfile.FIELD_USE_RFC5626          + " INTEGER DEFAULT 1,"
			+ SipProfile.FIELD_RFC5626_INSTANCE_ID  + " TEXT,"
			+ SipProfile.FIELD_RFC5626_REG_ID       + " TEXT,"
			
            + SipProfile.FIELD_VID_IN_AUTO_SHOW          + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_VID_OUT_AUTO_TRANSMIT     + " INTEGER DEFAULT -1,"
            
            + SipProfile.FIELD_RTP_PORT                  + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_RTP_ENABLE_QOS            + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_RTP_QOS_DSCP              + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_RTP_BOUND_ADDR            + " TEXT,"
            + SipProfile.FIELD_RTP_PUBLIC_ADDR           + " TEXT,"
            + SipProfile.FIELD_ANDROID_GROUP             + " TEXT,"
            + SipProfile.FIELD_ALLOW_VIA_REWRITE         + " INTEGER DEFAULT 0,"
            + SipProfile.FIELD_ALLOW_SDP_NAT_REWRITE + " INTEGER  DEFAULT 0,"
            + SipProfile.FIELD_SIP_STUN_USE              + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_MEDIA_STUN_USE            + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_ICE_CFG_USE               + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_ICE_CFG_ENABLE            + " INTEGER DEFAULT 0,"
            + SipProfile.FIELD_TURN_CFG_USE              + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_TURN_CFG_ENABLE           + " INTEGER DEFAULT 0,"
            + SipProfile.FIELD_TURN_CFG_SERVER           + " TEXT,"
            + SipProfile.FIELD_TURN_CFG_USER             + " TEXT,"
            + SipProfile.FIELD_TURN_CFG_PASSWORD         + " TEXT,"
            + SipProfile.FIELD_IPV6_MEDIA_USE            + " INTEGER DEFAULT 0,"
            + SipProfile.FIELD_WIZARD_DATA                       + " TEXT"
			
		+ ");";
	
	private static final String TABLE_SIP_CALLLOGS_CREATE = "CREATE TABLE IF NOT EXISTS "
		+ SipManager.CALLLOGS_TABLE_NAME
		+ " ("
			+ CallLog.Calls._ID					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ CallLog.Calls.CACHED_NAME			+ " TEXT,"
			+ CallLog.Calls.CACHED_NUMBER_LABEL	+ " TEXT,"
			+ CallLog.Calls.CACHED_NUMBER_TYPE	+ " INTEGER,"
			+ CallLog.Calls.DATE				+ " INTEGER,"
			+ CallLog.Calls.DURATION			+ " INTEGER,"
			+ CallLog.Calls.NEW					+ " INTEGER,"
			+ CallLog.Calls.NUMBER				+ " TEXT,"
			+ CallLog.Calls.TYPE				+ " INTEGER,"
	        + SipManager.CALLLOG_PROFILE_ID_FIELD     + " INTEGER,"
	        + SipManager.CALLLOG_STATUS_CODE_FIELD    + " INTEGER,"
	        + SipManager.CALLLOG_STATUS_TEXT_FIELD    + " TEXT"
			+ " [security] NVARCHAR"
		+");";

	public DataBaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// IM 消息表
		db.execSQL(TABLE_IM_MSG_CREATE);
		// IM 通知表
		db.execSQL(TABLE_IM_NOTICE_CREATE);
		// VoIP 用户表
		db.execSQL(TABLE_SIP_ACCOUNT_CREATE);
		// VoIP 通话记录表
		db.execSQL(TABLE_SIP_CALLLOGS_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/**
		 *  Upgrade History:
		 *    早期版本，直接清除老表建新表
		 */
		if (oldVersion < 4) {
			db.execSQL("DROP TABLE IF EXISTS [im_notice]");
			db.execSQL("DROP TABLE IF EXISTS [im_msg_his]");
			db.execSQL("DROP TABLE IF EXISTS " + SipProfile.ACCOUNTS_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + SipManager.CALLLOGS_TABLE_NAME);
	
			onCreate(db);
		}
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
