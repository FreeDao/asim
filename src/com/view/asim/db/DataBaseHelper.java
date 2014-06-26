package com.view.asim.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * SQLite���ݿ�İ�����
 * 
 * ����������չ��,��Ҫ�е����ݿ��ʼ���Ͱ汾����ʹ��,��������ȫ�ɺ��ĸ������
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
		// IM ��Ϣ��
		db.execSQL("CREATE TABLE [im_msg_his] ([_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [unique_id] NVARCHAR,  [status] NVARCHAR, [readStatus] NVARCHAR, [content] NVARCHAR, [with] NVARCHAR, [src] NVARCHAR, [time] NVARCHAR, [type] NVARCHAR, [dir] NVARCHAR, [chatType] NVARCHAR, [security] NVARCHAR, [destroy] NVARCHAR, [attachment] NVARCHAR);");
		// IM ֪ͨ��
		db.execSQL("CREATE TABLE [im_notice]  ([_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [dispStatus] NVARCHAR, [readStatus] NVARCHAR, [content] NVARCHAR, [with] NVARCHAR, [time] NVARCHAR, [status] NVARCHAR, [avatar_img] BLOB);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/**
		 *  Upgrade History:
		 *  1��V0.4.28 �� V0.6.25 �汾�����������ݿ�汾 V1->V3��
		 *    ���ڰ汾��ֱ������ϱ��±�
		 *  1��V0.5.14 �� V0.6.25 �汾�����������ݿ�汾 V2->V3��
		 *    ���ڰ汾��ֱ������ϱ��±�
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
