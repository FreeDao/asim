package com.view.asim.db;

import java.util.ArrayList;
import java.util.List;

import com.view.asim.comm.ApplicationContext;
import com.view.asim.sip.api.SipManager;
import com.view.asim.sip.api.SipProfile;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.CallLog;
import android.util.Log;

/**
 * SQLite���ݿ�İ�����
 * 
 * ����������չ��,��Ҫ�е����ݿ��ʼ���Ͱ汾����ʹ��,��������ȫ�ɺ��ĸ������
 * 
 * @author xuweinan
 * 
 */
public class DataBaseHelper extends SDCardSQLiteOpenHelper {
	private static final String TAG = "DataBaseHelper";
	protected String mPrimaryKey = "_id";

	private static String mDBName = "";
	private static int mDBVersion = -1;
		
	private static class Loader {
        static DataBaseHelper INSTANCE = new DataBaseHelper();
    }
	 
	public static DataBaseHelper getInstance(String name, int ver) {
		if (name == null || name.equals("") || ver <= 0) {
			Log.w(TAG, "db name " + name + ", ver " + ver + ", invalid!");
			return null;
		}
		mDBName = name;
		mDBVersion = ver;
		return Loader.INSTANCE;
	}
	
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
	        + SipManager.CALLLOG_STATUS_TEXT_FIELD    + " TEXT,"
			+ "security" + " TEXT"
		+");";

	private DataBaseHelper() {
		super(ApplicationContext.get(), mDBName, null, mDBVersion);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// IM ��Ϣ��
		db.execSQL(TABLE_IM_MSG_CREATE);
		// IM ֪ͨ��
		db.execSQL(TABLE_IM_NOTICE_CREATE);
		// VoIP �û���
		db.execSQL(TABLE_SIP_ACCOUNT_CREATE);
		// VoIP ͨ����¼��
		db.execSQL(TABLE_SIP_CALLLOGS_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/**
		 *  Upgrade History:
		 *    ���ڰ汾��ֱ������ϱ��±�
		 */
		if (oldVersion < 4) {
			db.execSQL("DROP TABLE IF EXISTS [im_notice]");
			db.execSQL("DROP TABLE IF EXISTS [im_msg_his]");
			db.execSQL("DROP TABLE IF EXISTS " + SipProfile.ACCOUNTS_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + SipManager.CALLLOGS_TABLE_NAME);
		}
		onCreate(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
	
	/**
	 * ִ��һ��sql���
	 * 
	 * @param name
	 * @param tel
	 */
	public synchronized void execSQL(String sql) {
		SQLiteDatabase dataBase = null;
		try {
			dataBase = getWritableDatabase();
			dataBase.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ִ��һ��sql���
	 * 
	 * @param name
	 * @param tel
	 */
	public synchronized void execSQL(String sql, Object[] bindArgs) {
		SQLiteDatabase dataBase = null;
		try {
			dataBase = getWritableDatabase();
			dataBase.execSQL(sql, bindArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �����ݿ���в���һ������
	 * 
	 * @param table
	 *            ����
	 * @param content
	 *            �ֶ�ֵ
	 */
	public synchronized long insert(String table, ContentValues content) {
		SQLiteDatabase dataBase = null;
		try {
			dataBase = getWritableDatabase();
			// insert������һ���������ݿ�������ڶ����������CONTENTΪ��ʱ������в���һ��NULL,����������Ϊ���������
			return dataBase.insert(table, null, content);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * ����ɾ��ָ����������
	 * 
	 * @param ids
	 */
	public synchronized void deleteByIds(String table, Object... primaryKeys) {
		SQLiteDatabase dataBase = null;

		try {
			if (primaryKeys.length > 0) {
				StringBuilder sb = new StringBuilder();
				for (@SuppressWarnings("unused")
				Object id : primaryKeys) {
					sb.append("?").append(",");
				}
				sb.deleteCharAt(sb.length() - 1);
				dataBase = getWritableDatabase();
				dataBase.execSQL("delete from " + table + " where "
						+ mPrimaryKey + " in(" + sb + ")",
						primaryKeys);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����ĳһ���ֶκ�ֵɾ��һ������, �� name="jack"
	 * 
	 * @param table
	 * @param field
	 * @param value
	 * @return ����ֵ����0��ʾɾ���ɹ�
	 */
	public synchronized int deleteByField(String table, String field, String value) {
		SQLiteDatabase dataBase = null;
		try {
			dataBase = getWritableDatabase();
			return dataBase.delete(table, field + "=?", new String[] { value });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * ��������ɾ������
	 * 
	 * @param table
	 *            ����
	 * @param whereClause
	 *            ��ѯ��� ��������?
	 * @param whereArgs
	 *            ����ֵ
	 * @return ����ֵ����0��ʾɾ���ɹ�
	 */
	public synchronized int deleteByCondition(String table, String whereClause,
			String[] whereArgs) {
		SQLiteDatabase dataBase = null;

		try {
			dataBase = getWritableDatabase();
			return dataBase.delete(table, whereClause, whereArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * ��������ɾ��һ������
	 * 
	 * @param table
	 * @param id
	 * @return ����ֵ����0��ʾɾ���ɹ�
	 */
	public synchronized int deleteById(String table, String id) {
		SQLiteDatabase dataBase = null;

		try {
			dataBase = getWritableDatabase();
			return deleteByField(table, mPrimaryKey, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * ������������һ������
	 * 
	 * @param table
	 * @param id
	 * @param values
	 * @return ����ֵ����0��ʾ���³ɹ�
	 */
	public synchronized int updateById(String table, String id, ContentValues values) {
		SQLiteDatabase dataBase = null;

		try {
			dataBase = getWritableDatabase();
			return dataBase.update(table, values, mPrimaryKey + "=?",
					new String[] { id });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * ��������
	 * 
	 * @param table
	 * @param values
	 * @param whereClause
	 * @param whereArgs
	 * @return ����ֵ����0��ʾ���³ɹ�
	 */
	public synchronized int update(String table, ContentValues values, String whereClause,
			String[] whereArgs) {
		SQLiteDatabase dataBase = null;

		try {
			dataBase = getWritableDatabase();
			return dataBase.update(table, values, whereClause, whereArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * ���������鿴ĳ�������Ƿ����
	 * 
	 * @param table
	 * @param id
	 * @return
	 */
	public synchronized Boolean isExistsById(String table, String id) {
		SQLiteDatabase dataBase = null;

		try {
			dataBase = getReadableDatabase();
			return isExistsByField(table, mPrimaryKey, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ����ĳ�ֶ�/ֵ�鿴ĳ�������Ƿ����
	 * 
	 * @param status
	 * @return
	 */
	public synchronized Boolean isExistsByField(String table, String field, String value) {
		SQLiteDatabase dataBase = null;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ").append(table).append(" WHERE ")
				.append(field).append(" =?");
		try {
			dataBase = getReadableDatabase();
			return isExistsBySQL(sql.toString(), new String[] { value });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ʹ��SQL���鿴ĳ�������Ƿ����
	 * 
	 * @param sql
	 * @param selectionArgs
	 * @return
	 */
	public synchronized Boolean isExistsBySQL(String sql, String[] selectionArgs) {
		SQLiteDatabase dataBase = null;

		Cursor cursor = null;
		try {
			dataBase = getReadableDatabase();
			cursor = dataBase.rawQuery(sql, selectionArgs);
			if (cursor.moveToFirst()) {
				return (cursor.getInt(0) > 0);
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		cursor.close();
		return null;
	}

	/**
	 * ��ѯһ������
	 * 
	 * @param rowMapper
	 * @param sql
	 * @param args
	 * @return
	 */
	public synchronized <T> T queryForObject(RowMapper<T> rowMapper, String sql,
			String[] args) {
		SQLiteDatabase dataBase = null;

		Cursor cursor = null;
		T object = null;
		try {
			dataBase = getReadableDatabase();
			cursor = dataBase.rawQuery(sql, args);
			if (cursor.moveToFirst()) {
				object = rowMapper.mapRow(cursor, cursor.getCount());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		cursor.close();
		return object;
	}

	/**
	 * ��ѯ
	 * 
	 * @param rowMapper
	 * @param sql
	 * @param startResult
	 *            ��ʼ���� ע:��һ����¼����Ϊ0
	 * @param maxResult
	 *            ����
	 * @return
	 */
	public synchronized <T> List<T> queryForList(RowMapper<T> rowMapper, String sql,
			String[] selectionArgs) {
		SQLiteDatabase dataBase = null;

		Cursor cursor = null;
		List<T> list = null;
		try {
			dataBase = getReadableDatabase();
			cursor = dataBase.rawQuery(sql, selectionArgs);
			list = new ArrayList<T>();
			while (cursor.moveToNext()) {
				if (rowMapper.mapRow(cursor, cursor.getPosition()) != null)
					list.add(rowMapper.mapRow(cursor, cursor.getPosition()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		cursor.close();

		return list;
	}

	/**
	 * ��ҳ��ѯ
	 * 
	 * @param rowMapper
	 * @param sql
	 * @param startResult
	 *            ��ʼ���� ע:��һ����¼����Ϊ0
	 * @param maxResult
	 *            ����
	 * @return
	 */
	public synchronized <T> List<T> queryForList(RowMapper<T> rowMapper, String sql,
			int startResult, int maxResult) {
		SQLiteDatabase dataBase = null;

		Cursor cursor = null;
		List<T> list = null;
		try {
			dataBase = getReadableDatabase();
			cursor = dataBase.rawQuery(sql + " limit ?,?", new String[] {
					String.valueOf(startResult), String.valueOf(maxResult) });
			list = new ArrayList<T>();
			while (cursor.moveToNext()) {
				list.add(rowMapper.mapRow(cursor, cursor.getPosition()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		cursor.close();

		return list;
	}

	/**
	 * ��ȡ��¼��
	 * 
	 * @return
	 */
	public synchronized Integer getCount(String sql, String[] args) {
		SQLiteDatabase dataBase = null;

		Cursor cursor = null;
		try {
			dataBase = getReadableDatabase();
			cursor = dataBase.rawQuery("select count(*) from (" + sql + ")",
					args);
			if (cursor.moveToNext()) {
				return cursor.getInt(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		cursor.close();

		return 0;
	}

	/**
	 * ��ҳ��ѯ
	 * 
	 * @param rowMapper
	 * @param table
	 *            �����ı�
	 * @param columns
	 *            ����Ҫ�����е���������ɵ��ַ������飬����null�᷵�����е��С�
	 * @param selection
	 *            ��ѯ�����Ӿ䣬�൱��select���where�ؼ��ֺ���Ĳ��֣��������Ӿ�����ʹ��ռλ��"?"
	 * @param selectionArgs
	 *            ��Ӧ��selection�����ռλ����ֵ��ֵ�������е�λ����ռλ��������е�λ�ñ���һ�£�����ͻ����쳣
	 * @param groupBy
	 *            �Խ�������з����group by��䣨������GROUP BY�ؼ��֣�������null�����Խ�������з���
	 * @param having
	 *            �Բ�ѯ��Ľ�������й���,����null�򲻹���
	 * @param orderBy
	 *            �Խ�������������order by��䣨������ORDER BY�ؼ��֣�������null���Խ����ʹ��Ĭ�ϵ�����
	 * @param limit
	 *            ָ��ƫ�����ͻ�ȡ�ļ�¼�����൱��select���limit�ؼ��ֺ���Ĳ���,���Ϊnull�򷵻�������
	 * @return
	 */
	public synchronized <T> List<T> queryForList(RowMapper<T> rowMapper, String table,
			String[] columns, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy, String limit) {
		
		SQLiteDatabase dataBase = null;

		List<T> list = null;
		Cursor cursor = null;
		try {
			dataBase = getReadableDatabase();
			cursor = dataBase.query(table, columns, selection, selectionArgs,
					groupBy, having, orderBy, limit);
			list = new ArrayList<T>();
			while (cursor.moveToNext()) {
				list.add(rowMapper.mapRow(cursor, cursor.getPosition()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		cursor.close();

		return list;
	}

	/**
	 * Get Primary Key
	 * 
	 * @return
	 */
	public synchronized String getPrimaryKey() {
		return mPrimaryKey;
	}

	/**
	 * Set Primary Key
	 * 
	 * @param primaryKey
	 */
	public synchronized void setPrimaryKey(String primaryKey) {
		this.mPrimaryKey = primaryKey;
	}

	/**
	 * 
	 * @author 
	 * 
	 * @param <T>
	 */
	public interface RowMapper<T> {
		/**
		 * 
		 * @param cursor
		 *            �α�
		 * @param index
		 *            �±�����
		 * @return
		 */
		public T mapRow(Cursor cursor, int index);
	}

	/**
	 * �ر����ݿ�
	 */
	public synchronized void closeDatabase(Cursor cursor) {
		super.close();

		if (null != cursor) {
			cursor.close();
		}
	}
}
