package com.view.asim.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * SQLite数据库的帮助类
 * 
 * 该类属于扩展类,主要承担数据库初始化和版本升级使用,其他核心全由核心父类完成
 * 
 * @author shimiso
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
		db.execSQL("CREATE TABLE [im_msg_his] ([_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [status] NVARCHAR, [readStatus] NVARCHAR, [content] NVARCHAR, [with] NVARCHAR, [src] NVARCHAR, [time] NVARCHAR, [type] NVARCHAR, [dir] NVARCHAR, [chatType] NVARCHAR, [security] NVARCHAR, [destroy] NVARCHAR, [attachment] NVARCHAR);");
		// IM 通知表
		db.execSQL("CREATE TABLE [im_notice]  ([_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [dispStatus] NVARCHAR, [readStatus] NVARCHAR, [content] NVARCHAR, [with] NVARCHAR, [time] NVARCHAR, [status] NVARCHAR, [avatar_img] BLOB);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
