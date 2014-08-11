package com.view.asim.manager;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.view.asim.comm.Constant;
import com.view.asim.db.DataBaseHelper;
import com.view.asim.db.DataBaseHelper.RowMapper;
import com.view.asim.model.Attachment;
import com.view.asim.model.ChatHisBean;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.utils.StringUtil;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

/**
 * 
 * 消息历史记录，
 * 
 * @author xuweinan
 */
public class MessageManager {
	protected static final String TAG = "MessageManager";
	private DataBaseHelper mDBHelper = null;

	 private static class Loader {
         static MessageManager INSTANCE = new MessageManager();
     }
	 
	private MessageManager() {
//		LoginConfig cfg = AppConfigManager.getInstance().getLoginConfig();
//		String name = cfg.getUsername();
//		Log.d(TAG, "init database " + name);
//
//		mDBHelper = DataBaseHelper.getInstance(name, Constant.DB_VERSION);
	}

	public static MessageManager getInstance() {
		return Loader.INSTANCE;
	}

	public void destroy() {
		if (mDBHelper != null) {
			mDBHelper.closeDatabase(null);
			mDBHelper = null;
		}
	}
	
	public void init(DataBaseHelper helper) {
		mDBHelper = helper;
		
		if (ContacterManager.contacters == null) {
			return;
		}
		
		// 核查好友列表和消息是否对应，删除不存在的好友的消息
		List<String> users = getAllUsersFromMessages();
		Log.d(TAG, "find " + users.size() + " contacters in message list");
		for(String u: users) {
			if (!ContacterManager.contacters.containsKey(u)) {
				Log.d(TAG, u + " does not exist in contacter list, so delete all messages of it." );
				delChatHisByName(u);
			}
		}
	}
	
	protected List<String> getAllUsersFromMessages() {
		List<String> list = mDBHelper.queryForList(
				new RowMapper<String>() {
					@Override
					public String mapRow(Cursor cursor, int index) {
						return cursor.getString(cursor.getColumnIndex("with"));
					}
				},
				"select distinct with from im_msg_his;",
				null);
		return list;

	}
	
	/**
	 * 
	 * 保存消息.
	 * [status] NVARCHAR, 
	 * [readStatus] NVARCHAR
	 * [content] NVARCHAR, 
	 * [user] NVARCHAR, 
	 * [group] NVARCHAR, 
	 * [src] NVARCHAR, 
	 * [type] NVARCHAR, 
	 * [dir] NVARCHAR, 
	 * [chatType] NVARCHAR, 
	 * [security] NVARCHAR, 
	 * [destroy] NVARCHAR, 
	 * [attachment] NVARCHAR,
	 * 
	 * @param msg
	 * @author xuweinan
	 * @update 2012-5-16 下午3:23:15
	 */
	public long saveIMMessage(ChatMessage msg) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("unique_id", msg.getUniqueId());
		contentValues.put("status", msg.getStatus());
		contentValues.put("readStatus", msg.getReadStatus());
		contentValues.put("content", msg.getContent());
		contentValues.put("with", msg.getWith());
		contentValues.put("src", msg.getFrom());
		contentValues.put("time", msg.getTime());
		contentValues.put("type", msg.getType());
		contentValues.put("dir", msg.getDir());
		contentValues.put("chatType", msg.getChatType());
		contentValues.put("security", msg.getSecurity());
		contentValues.put("destroy", msg.getDestroy());
		if (msg.getAttachment() != null) {
			contentValues.put("attachment", msg.getAttachment().dumps());
		}
		return mDBHelper.insert("im_msg_his", contentValues);
	}
	
	/**
	 * 
	 * 更新消息.
	 * 
	 * @param msg
	 * @author xuweinan
	 * @update 2012-5-16 下午3:23:15
	 */
	public void updateIMMessage(ChatMessage msg) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("unique_id", msg.getUniqueId());
		contentValues.put("status", msg.getStatus());
		contentValues.put("content", msg.getContent());
		contentValues.put("with", msg.getWith());
		contentValues.put("src", msg.getFrom());
		contentValues.put("time", msg.getTime());
		contentValues.put("type", msg.getType());
		contentValues.put("dir", msg.getDir());
		contentValues.put("chatType", msg.getChatType());
		contentValues.put("security", msg.getSecurity());
		contentValues.put("destroy", msg.getDestroy());
		if (msg.getAttachment() != null) {
			contentValues.put("attachment", msg.getAttachment().dumps());
		}
		
		mDBHelper.updateById("im_msg_his", msg.getId(), contentValues);
	}

	/**
	 * 
	 * 更新状态.
	 * 
	 * @author xuweinan
	 */
	public void updateReadStatus(long id, String readStatus) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("readStatus", readStatus);
		mDBHelper.updateById("im_msg_his", "" + id, contentValues);
	}
		
	/**
	 * 
	 * 更新状态.
	 * 
	 * @author xuweinan
	 */
	public void updateReadStatus(String name, String readStatus) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("readStatus", readStatus);
		mDBHelper.update("im_msg_his", contentValues, "with=?", new String[] { ""
				+ name });
	}

	/**
	 * 
	 * 查找与某人的聊天记录聊天记录
	 * 
	 * [status] NVARCHAR, 
	 * [readStatus] NVARCHAR
	 * [content] NVARCHAR, 
	 * [from] NVARCHAR, 
	 * [to] NVARCHAR, 
	 * [time] NVARCHAR, 
	 * [type] NVARCHAR, 
	 * [dir] NVARCHAR, 
	 * [chatType] NVARCHAR, 
	 * [security] NVARCHAR, 
	 * [destroy] NVARCHAR, 
	 * [attachment] NVARCHAR,
	 * 
	 * @param pageNum
	 *            第几页(from 1)
	 * @param pageSize
	 *            要查的记录条数
	 * @return
	 * @author xuweinan
	 */
	public List<ChatMessage> getMessageListByName(String name, int pageNum,
			int pageSize) {
		if (StringUtil.empty(name)) {
			return null;
		}
		int fromIndex = (pageNum - 1) * pageSize;
		List<ChatMessage> list = mDBHelper.queryForList(
				new RowMapper<ChatMessage>() {
					@Override
					public ChatMessage mapRow(Cursor cursor, int index) {
						ChatMessage msg = new ChatMessage();
						msg.setId(cursor.getString(cursor.getColumnIndex("_id")));
						msg.setUniqueId(cursor.getString(cursor.getColumnIndex("unique_id")));
						msg.setStatus(cursor.getString(cursor.getColumnIndex("status")));
						msg.setReadStatus(cursor.getString(cursor.getColumnIndex("readStatus")));
						msg.setContent(cursor.getString(cursor.getColumnIndex("content")));
						msg.setWith(cursor.getString(cursor.getColumnIndex("with")));
						msg.setFrom(cursor.getString(cursor.getColumnIndex("src")));
						msg.setTime(cursor.getString(cursor.getColumnIndex("time")));
						msg.setType(cursor.getString(cursor.getColumnIndex("type")));
						msg.setDir(cursor.getString(cursor.getColumnIndex("dir")));
						msg.setChatType(cursor.getString(cursor.getColumnIndex("chatType")));
						msg.setSecurity(cursor.getString(cursor.getColumnIndex("security")));
						msg.setDestroy(cursor.getString(cursor.getColumnIndex("destroy")));
						if (cursor.getString(cursor.getColumnIndex("attachment")) != null) {
							msg.setAttachment(Attachment.loads(cursor.getString(cursor.getColumnIndex("attachment"))));
						}

						return msg;
					}
				},
				"select * from im_msg_his where with=? order by time asc limit ? , ? ",
				new String[] { "" + name, "" + fromIndex, "" + pageSize });
		return list;

	}
	
	/**
	 * 根据 uniqueId 删除一条消息 
	 * 
	 * @param  unique_id
	 * 
	 * @author xuweinan
	 */
	public int delMessageByUniqueId(String id) {
		if (StringUtil.empty(id)) {
			return 0;
		}
		return mDBHelper.deleteByCondition("im_msg_his", "unique_id=?",
				new String[] { "" + id });
	}
	

	/**
	 * 
	 * 查找与某人的聊天记录总数
	 * 
	 * @return
	 * @author xuweinan
	 * @update 2012-7-2 上午9:31:04
	 */
	public int getMsgCountByWithAndReadStatus(String with, String readStatus) {
		if (StringUtil.empty(with)) {
			return 0;
		}
		
		String sqlCmd = null;
		String[] paras = null;
		if(readStatus == null) {
			sqlCmd = "select _id from im_msg_his where with=?";
			paras = new String[] { with };
		}
		else {
			sqlCmd = "select _id from im_msg_his where with=? and readStatus=?";
			paras = new String[] { with, readStatus };
					
		}
		return mDBHelper.getCount(sqlCmd, paras);

	}


	/**
	 * 删除与某人的聊天记录 
	 * 
	 * @param fromUser
	 * 
	 * @author xuweinan
	 */
	public int delChatHisByName(String name) {
		if (StringUtil.empty(name)) {
			return 0;
		}
		return mDBHelper.deleteByCondition("im_msg_his", "with=?",
				new String[] { "" + name });
	}


	/**
	 * 删除一条聊天记录 
	 * 
	 * @param fromUser
	 * 
	 * @author xuweinan
	 */
	public int delChatHisById(String id) {
		if (StringUtil.empty(id)) {
			return 0;
		}
		return mDBHelper.deleteByCondition("im_msg_his", "_id=?",
				new String[] { "" + id });
	}
	
	/**
	 * 
	 * 获取最近聊天人聊天最后一条消息和未读消息总数
	 * 
	 * @return
	 * @author xuweinan
	 */
	
	public List<ChatHisBean> getRecentContactsWithLastMsg() {
		String sql = null;
		if (AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED)) {
			sql = "select max(_id) as maxid, type, content, time, with, src, chatType, security, destroy, dir from im_msg_his group by with order by time desc";
			//"select m.[_id],m.[content],m.[time],m.[with],m.[src],m.[chatType] from im_msg_his m join (select with, max(_id) as mt from im_msg_his group by with) as tem on tem.mt=m._id and tem.with=m.with ";
		}
		else {
			sql = "select max(_id) as maxid, type, content, time, with, src, chatType, security, destroy, dir from im_msg_his where security=\"plain\" group by with order by time desc";
			//"select m.[_id],m.[content],m.[time],m.[with],m.[src],m.[chatType] from im_msg_his m join (select with, max(_id) as mt from im_msg_his group by with) as tem on tem.mt=m._id and tem.with=m.with where security=\"plain\" ";
		}
		
		List<ChatHisBean> list = mDBHelper
				.queryForList(
						new RowMapper<ChatHisBean>() {

							@Override
							public ChatHisBean mapRow(Cursor cursor, int index) {
								ChatHisBean notice = new ChatHisBean();
								if(cursor.isNull(cursor.getColumnIndex("maxid"))) 
									return null;
								
								notice.setId(cursor.getString(cursor.getColumnIndex("maxid")));
								notice.setFrom(cursor.getString(cursor.getColumnIndex("src")));
								notice.setWith(cursor.getString(cursor.getColumnIndex("with")));
								notice.setTime(cursor.getString(cursor.getColumnIndex("time")));
								notice.setChatType(cursor.getString(cursor.getColumnIndex("chatType")));
								notice.setDir(cursor.getString(cursor.getColumnIndex("dir")));
								notice.setDestroy(cursor.getString(cursor.getColumnIndex("destroy")));
								notice.setType(cursor.getString(cursor.getColumnIndex("type")));
								String chatType = cursor.getString(cursor.getColumnIndex("chatType"));
								if (chatType.equals(IMMessage.SINGLE)) {
									notice.setContent(cursor.getString(cursor.getColumnIndex("content")));
								}
								else {
									User u = ContacterManager.contacters.get(notice.getWith());
									notice.setContent(u.getNickName() + "：" + cursor.getString(cursor.getColumnIndex("content")));
								}

								return notice;
							}
						},
						sql, null);
		
		
		for (ChatHisBean b : list) {
			int count = mDBHelper
					.getCount(
							"select _id from im_msg_his where readStatus=? and with=?",
							new String[] { "" + IMMessage.UNREAD, b.getWith() });
			b.setUnreadCount(count);
		}
		return list;
	}

}
