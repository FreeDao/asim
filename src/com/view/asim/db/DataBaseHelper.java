package com.view.asim.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * SQLite数据库的帮助类
 * 
 * 该类属于扩展类,主要承担数据库初始化和版本升级使用,其他核心全由核心父类完成
 * 
 * @author xuweinan
 * 
 */
public class DataBaseHelper extends SDCardSQLiteOpenHelper {

	public DataBaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// IM 消息表
		db.execSQL("CREATE TABLE [im_msg_his] ([_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [unique_id] NVARCHAR,  [status] NVARCHAR, [readStatus] NVARCHAR, [content] NVARCHAR, [with] NVARCHAR, [src] NVARCHAR, [time] NVARCHAR, [type] NVARCHAR, [dir] NVARCHAR, [chatType] NVARCHAR, [security] NVARCHAR, [destroy] NVARCHAR, [attachment] NVARCHAR);");
		// IM 通知表
		db.execSQL("CREATE TABLE [im_notice]  ([_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [dispStatus] NVARCHAR, [readStatus] NVARCHAR, [content] NVARCHAR, [with] NVARCHAR, [time] NVARCHAR, [status] NVARCHAR, [avatar_img] BLOB);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/**
		 *  Upgrade History:
		 *  1、V0.4.28 到 V0.6.25 版本的升级（数据库版本 V1->V3）
		 *    早期版本，直接清除老表建新表
		 *  1、V0.5.14 到 V0.6.25 版本的升级（数据库版本 V2->V3）
		 *    早期版本，直接清除老表建新表
		 *    
		 */
		
		db.execSQL("DROP TABLE [im_notice]");
		db.execSQL("CREATE TABLE [im_notice]  ([_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [dispStatus] NVARCHAR, [readStatus] NVARCHAR, [content] NVARCHAR, [with] NVARCHAR, [time] NVARCHAR, [status] NVARCHAR, [avatar_img] BLOB);");
		db.execSQL("DROP TABLE [im_msg_his]");
		db.execSQL("CREATE TABLE [im_msg_his] ([_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [unique_id] NVARCHAR,  [status] NVARCHAR, [readStatus] NVARCHAR, [content] NVARCHAR, [with] NVARCHAR, [src] NVARCHAR, [time] NVARCHAR, [type] NVARCHAR, [dir] NVARCHAR, [chatType] NVARCHAR, [security] NVARCHAR, [destroy] NVARCHAR, [attachment] NVARCHAR);");

	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
