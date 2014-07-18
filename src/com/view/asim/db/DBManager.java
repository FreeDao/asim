package com.view.asim.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * SQLite数据库管理类
 * 
 * 主要负责数据库资源的初始化,开启,关闭,以及获得DatabaseHelper帮助类操作
 * 
 * @author xuweinan
 * 
 */
public class DBManager {
	
	/**
	 * App 版本: 
	 * V0.4.28，数据库版本：V1（初始版本）
	 * V0.5.14，数据库版本：V2（修改了 im_notice 表）
	 * V0.6.25，数据库版本：V3（修改了 im_notice 和 im_msg_his 表）
	 * V0.7.15，数据库版本：V4（增加了 SIP 相关表）
	 */
	public final int version = 4;
	
	public String databaseName;

	// 本地Context对象
	private Context mContext = null;

	private static DBManager dBManager = null;

	/**
	 * 构造函数
	 * 
	 * @param mContext
	 */
	private DBManager(Context mContext) {
		super();
		this.mContext = mContext;

	}

	public static DBManager getInstance(Context mContext, String databaseName) {
		if (null == dBManager) {
			dBManager = new DBManager(mContext);
		}
		dBManager.databaseName = databaseName;
		return dBManager;
	}
	
	public static DBManager getInstance() {
		return dBManager;
	}

	/**
	 * 关闭数据库 注意:当事务成功或者一次性操作完毕时候再关闭
	 */
	public void closeDatabase(SQLiteDatabase dataBase, Cursor cursor) {
		if (null != dataBase) {
			dataBase.close();
		}
		if (null != cursor) {
			cursor.close();
		}
	}

	/**
	 * 打开数据库 注:SQLiteDatabase资源一旦被关闭,该底层会重新产生一个新的SQLiteDatabase
	 */
	public SQLiteDatabase openDatabase() {
		return getDatabaseHelper().getWritableDatabase();
	}

	/**
	 * 获取DataBaseHelper
	 * 
	 * @return
	 */
	public DataBaseHelper getDatabaseHelper() {
		return new DataBaseHelper(mContext, this.databaseName, null,
				this.version);
	}

}
