package com.view.asim.manager;


import java.util.List;

import com.view.asim.comm.Constant;
import com.view.asim.db.DBManager;
import com.view.asim.db.SQLiteTemplate;
import com.view.asim.db.SQLiteTemplate.RowMapper;
import com.view.asim.model.CallLogs;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.SingleCallLog;
import com.view.asim.model.User;
import com.view.asim.sip.api.SipManager;
import com.view.asim.utils.StringUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

/**
 * 
 * 通话记录管理
 * 
 * @author xuweinan
 */
public class CallLogManager {
	protected static final String TAG = "CallLogManager";
	private static CallLogManager INSTANCE = null;
	private static DBManager manager = null;

	private CallLogManager(Context context, LoginConfig cfg) {
//		SharedPreferences sharedPre = context.getSharedPreferences(
//				Constant.IM_SET_PREF, Context.MODE_PRIVATE);
		String databaseName = cfg.getUsername();//sharedPre.getString(Constant.USERNAME, null);
		Log.d(TAG, "init database " + databaseName);

		manager = DBManager.getInstance(context, databaseName);
	}

	public static CallLogManager getInstance(Context context, LoginConfig cfg) {

		if (INSTANCE == null) {
			Log.d(TAG, "new CallLogManager");

			INSTANCE = new CallLogManager(context, cfg);
		}

		return INSTANCE;
	}
	
	public static CallLogManager getInstance() {
		return INSTANCE;
	}

	public void destroy() {
		INSTANCE = null;
		manager = null;
	}

	public void init() {
		if (ContacterManager.contacters == null) {
			return;
		}
		
		// 核查好友列表和消息是否对应，删除不存在的好友的消息
		List<String> users = getAllUsersFromCallLogs();
		Log.d(TAG, "find " + users.size() + " contacters in calllog list");
		for(String u: users) {
			if (!ContacterManager.contacters.containsKey(u)) {
				Log.d(TAG, u + " does not exist in contacter list, so delete all call logs of it." );
				delCallLogsByName(StringUtil.getUserNameByJid(u));
			}
		}
	}
	
	protected List<String> getAllUsersFromCallLogs() {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<String> list = st.queryForList(
				new RowMapper<String>() {
					@Override
					public String mapRow(Cursor cursor, int index) {
						return StringUtil.getJidByName(User.NAME_PREFIX + cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
					}
				},
				"select distinct " + CallLog.Calls.CACHED_NAME + " from " + SipManager.CALLLOGS_TABLE_NAME + ";",
				null);
		return list;

	}
	
	/**
	 * 
	 * 查找某人的通话记录
	 * 
	 * @param pageNum
	 *            第几页(from 1)
	 * @param pageSize
	 *            要查的记录条数
	 * @return
	 * @author xuweinan
	 */
	public List<SingleCallLog> getCalllogsByName(String name, int pageNum,
			int pageSize) {
		if (StringUtil.empty(name)) {
			return null;
		}
		
		String sql = null;
		if (AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED)) {
			sql = "select * from " + 
					SipManager.CALLLOGS_TABLE_NAME + 
					" where " + CallLog.Calls.CACHED_NAME + "=? " +
					"order by " +
					CallLog.Calls.DATE +
					" desc limit ? , ? ";
		}
		else {
			sql = "select * from " + 
					SipManager.CALLLOGS_TABLE_NAME + 
					" where " + CallLog.Calls.CACHED_NAME + "=? and security=\"plain\"" +
					"order by " +
					CallLog.Calls.DATE +
					" desc limit ? , ? ";
		}
		
		int fromIndex = (pageNum - 1) * pageSize;
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<SingleCallLog> list = st.queryForList(
				new RowMapper<SingleCallLog>() {
					@Override
					public SingleCallLog mapRow(Cursor cursor, int index) {
						SingleCallLog log = new SingleCallLog();
						log.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex(CallLog.Calls._ID))));
						log.setWith(StringUtil.getJidByName("u" + cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))));
						log.setTime(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
						log.setType(cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)));
						log.setStatusCode(cursor.getInt(cursor.getColumnIndex(SipManager.CALLLOG_STATUS_CODE_FIELD)));
						log.setDuration(cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION)));
						log.setSecurity(cursor.getString(cursor.getColumnIndex("security")));
						return log;
					}
				},
				sql,
				new String[] { "" + StringUtil.getCellphoneByName(name), "" + fromIndex, "" + pageSize });
		return list;

	}


	/**
	 * 删除与某人的通话记录 
	 * 
	 * @param fromUser
	 * 
	 * @author xuweinan
	 */
	public int delCallLogsByName(String name) {
		if (StringUtil.empty(name)) {
			return 0;
		}
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.deleteByCondition(SipManager.CALLLOGS_TABLE_NAME, CallLog.Calls.CACHED_NAME + "=?",
				new String[] { "" + StringUtil.getCellphoneByName(name) });
	}


	/**
	 * 删除一条通话记录 
	 * 
	 * @param fromUser
	 * 
	 * @author xuweinan
	 */
	public int delCallLogById(String id) {
		if (StringUtil.empty(id)) {
			return 0;
		}
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.deleteByCondition(SipManager.CALLLOGS_TABLE_NAME, CallLog.Calls._ID + "=?",
				new String[] { "" + id });
	}
	
	/**
	 * 
	 * 获取所有用户的最后一条通话记录和记录总数
	 * 
	 * @return
	 * @author xuweinan
	 */
	
	public List<CallLogs> getRecentCallLogs() {
		String sql = null;
		if (AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED)) {
			sql = "select max(" + CallLog.Calls._ID + ") as maxid, " +
					CallLog.Calls.CACHED_NAME + ", " +
					CallLog.Calls.DATE + ", " +
					CallLog.Calls.DURATION + ", " +
					CallLog.Calls.TYPE + ", " +
					"security " +
					"from " + SipManager.CALLLOGS_TABLE_NAME + 
					" group by " +
					CallLog.Calls.CACHED_NAME;
		}
		else {
			sql = "select max(" + CallLog.Calls._ID + ") as maxid, " +
					CallLog.Calls.CACHED_NAME + ", " +
					CallLog.Calls.DATE + ", " +
					CallLog.Calls.DURATION + ", " +
					CallLog.Calls.TYPE + ", " +
					"security " +
					"from " + SipManager.CALLLOGS_TABLE_NAME + 
					" where security=\"plain\" " +
					"group by " +
					CallLog.Calls.CACHED_NAME;
		}
		
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<CallLogs> list = st
				.queryForList(
						new RowMapper<CallLogs>() {

							@Override
							public CallLogs mapRow(Cursor cursor, int index) {
								CallLogs logs = new CallLogs();
								if(cursor.isNull(cursor.getColumnIndex("maxid"))) 
									return null;
								
								logs.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex("maxid"))));
								logs.setWith(StringUtil.getJidByName("u" + cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))));
								logs.setTime(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
								logs.setType(cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)));
								logs.setSecurity(cursor.getString(cursor.getColumnIndex("security")));
								return logs;
							}
						},
						sql, null);
		
		
		for (CallLogs b : list) {
			int count = 0;
			if (AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED)) {

				count = st
						.getCount(
								"select _id from " + SipManager.CALLLOGS_TABLE_NAME + " where " + CallLog.Calls.CACHED_NAME + "=?",
								new String[] { "" + StringUtil.getCellphoneByName(StringUtil.getUserNameByJid(b.getWith())) });
			}
			else {
				count = st
						.getCount(
								"select _id from " + SipManager.CALLLOGS_TABLE_NAME + " where " + CallLog.Calls.CACHED_NAME + "=? and security=\"plain\" ",
								new String[] { "" + StringUtil.getCellphoneByName(StringUtil.getUserNameByJid(b.getWith())) });
			}
			b.setTotalCount(count);
		}
		return list;
	}

}
