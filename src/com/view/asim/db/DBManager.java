package com.view.asim.db;

import com.view.asim.comm.Constant;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * SQLite���ݿ������
 * 
 * ��Ҫ�������ݿ���Դ�ĳ�ʼ��,����,�ر�,�Լ����DatabaseHelper���������
 * 
 * @author xuweinan
 * 
 */
public class DBManager {
	
	public final int version = Constant.DB_VERSION;
	
	public String databaseName;

	// ����Context����
	private Context mContext = null;

	private static DBManager dBManager = null;

	/**
	 * ���캯��
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
	 * �ر����ݿ� ע��:������ɹ�����һ���Բ������ʱ���ٹر�
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
	 * �����ݿ� ע:SQLiteDatabase��Դһ�����ر�,�õײ�����²���һ���µ�SQLiteDatabase
	 */
	public SQLiteDatabase openDatabase() {
		return getDatabaseHelper().getWritableDatabase();
	}

	/**
	 * ��ȡDataBaseHelper
	 * 
	 * @return
	 */
	public DataBaseHelper getDatabaseHelper() {
		return new DataBaseHelper(mContext, this.databaseName, null,
				this.version);
	}

}
