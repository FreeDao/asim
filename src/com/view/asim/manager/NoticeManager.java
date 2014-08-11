package com.view.asim.manager;


import java.util.List;
import java.util.UUID;

import com.view.asim.activity.im.ChatActivity;
import com.view.asim.activity.im.UserNoticeActivity;
import com.view.asim.activity.sip.UserCallLogsActivity;
import com.view.asim.comm.ApplicationContext;
import com.view.asim.comm.Constant;
import com.view.asim.db.DataBaseHelper;
import com.view.asim.db.DataBaseHelper.RowMapper;
import com.view.asim.model.IMMessage;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.sip.api.SipCallSession;
import com.view.asim.sip.api.SipManager;
import com.view.asim.utils.ImageUtil;
import com.view.asim.utils.StringUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.view.asim.R;

/**
 * 
 * 通知管理.
 * 
 * @author xuweinan
 */
public class NoticeManager {
	private final static String TAG = "NoticeManager";
	private DataBaseHelper mDBHelper = null;

	private String mCurrentChatUser = null;
	private Context mCntx = null;
	private NotificationManager mNotificationManager = null;

	private static class Loader {
         static NoticeManager INSTANCE = new NoticeManager();
    }
	 
	private NoticeManager() {
//		LoginConfig cfg = AppConfigManager.getInstance().getLoginConfig();
//		String name = cfg.getUsername();
//		Log.d(TAG, "init database " + name);
//
//		mDBHelper = DataBaseHelper.getInstance(name, Constant.DB_VERSION);
		
		this.mCntx = ApplicationContext.get();
		mNotificationManager = (NotificationManager) mCntx
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public static NoticeManager getInstance() {
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
		List<Notice> notices = getAllNoticeList();
		for(Notice n: notices) {
			if (!ContacterManager.contacters.containsKey(n.getWith())) {
				Log.d(TAG, n.getWith() + " does not exist in contacter list, so delete all notices of it." );
				delNoticeById(n.getId());
			}
		}
	}
	
	public void setCurrentChatUser(String u) {
		if (u != null) {
			Log.d(TAG, "current chat user " + u);
		} else {
			Log.d(TAG, "current chat user null");
		}
		this.mCurrentChatUser = u;
	}

	public String getCurrentChatUser() {
		return mCurrentChatUser;
	}

	/**
	 * 
	 * 保存消息.
	 * 
	 * @param notice
	 * @author xuweinan
	 * @update 2014-4-16
	 */
	public long saveNotice(Notice notice) {
		ContentValues contentValues = new ContentValues();
		if (StringUtil.notEmpty(notice.getContent())) {
			contentValues.put("content", StringUtil.doEmpty(notice.getContent()));
		}
		contentValues.put("readStatus", notice.getReadStatus());
		contentValues.put("dispStatus", notice.getDispStatus());
		contentValues.put("with", notice.getWith());
		contentValues.put("status", notice.getStatus());
		contentValues.put("time", notice.getTime());
		contentValues.put("avatar_img",
				ImageUtil.bitmapToByte(notice.getAvatar()));
		return mDBHelper.insert("im_notice", contentValues);
	}

	/**
	 * 
	 * 获取所有未读消息.
	 * 
	 * @return
	 * @author shimiso
	 * @update 2012-5-16 下午3:22:53
	public List<Notice> getUnReadNoticeList() {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<Notice> list = st.queryForList(new RowMapper<Notice>() {

			@Override
			public Notice mapRow(Cursor cursor, int index) {
				Notice notice = new Notice();
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setContent(cursor.getString(cursor
						.getColumnIndex("content")));
				notice.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				notice.setFrom(cursor.getString(cursor
						.getColumnIndex("notice_from")));
				notice.setTo(cursor.getString(cursor
						.getColumnIndex("notice_to")));
				notice.setNoticeType(cursor.getInt(cursor
						.getColumnIndex("type")));
				notice.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
				byte[] abs = cursor
						.getBlob(cursor.getColumnIndex("avatar_img"));
				notice.setAvatar(ImageUtil.byteToBitmap(abs));
				return notice;
			}

		}, "select * from im_notice where status=" + Notice.UNREAD + "", null);
		return list;
	}
	 */

	/**
	 * 
	 * 更新状态.
	 * 
	 * @param status
	 * @author xuweinan
	 */
	public void updateStatusById(String id, String status) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("status", status);
		mDBHelper.updateById("im_notice", id, contentValues);
	}
	
	/**
	 * 
	 * 更新时间.
	 * 
	 * @param status
	 * @author xuweinan
	 */
	public void updateTimeById(String id, String time) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("time", time);
		mDBHelper.updateById("im_notice", id, contentValues);
	}

	/**
	 * 
	 * 更新添加好友状态.
	 * 
	 * @param status
	 * @author shimiso
	 * @update 2012-5-16 下午3:22:44
	public void updateAddFriendStatus(String id, Integer status, String content) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues contentValues = new ContentValues();
		contentValues.put("status", status);
		contentValues.put("content", content);
		st.updateById("im_notice", id, contentValues);
	}
	 */

	/**
	 * 
	 * 获取未读消息的条数.
	 * 
	 * @return
	 * @author shimiso
	 * @update 2012-5-16 下午6:22:03
	public Integer getUnReadNoticeCount() {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.getCount("select _id from im_notice where status=?",
				new String[] { "" + Notice.UNREAD });
	}
	 */

	/**
	 * 
	 * 根据主键获取通知.
	 * 
	 * @param id
	 * @author xuweinan
	 */
	public Notice getNoticeById(String id) {
		return mDBHelper.queryForObject(new RowMapper<Notice>() {

			@Override
			public Notice mapRow(Cursor cursor, int index) {
				Notice notice = new Notice();
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setContent(cursor.getString(cursor.getColumnIndex("content")));
				notice.setStatus(cursor.getString(cursor.getColumnIndex("status")));
				notice.setWith(cursor.getString(cursor.getColumnIndex("with")));
				notice.setTime(cursor.getString(cursor.getColumnIndex("time")));
				notice.setDispStatus(cursor.getString(cursor.getColumnIndex("dispStatus")));
				notice.setReadStatus(cursor.getString(cursor.getColumnIndex("readStatus")));
				byte[] abs = cursor
						.getBlob(cursor.getColumnIndex("avatar_img"));
				notice.setAvatar(ImageUtil.byteToBitmap(abs));

				return notice;
			}

		}, "select * from im_notice where _id=?", new String[] { id });
	}
	
	/**
	 * 
	 * 根据名称和状态获取通知.
	 * 
	 * @param id
	 * @author xuweinan
	 */
	public List<Notice> getNoticeByWithAndStatus(String with, String status) {
		return mDBHelper.queryForList(new RowMapper<Notice>() {

			@Override
			public Notice mapRow(Cursor cursor, int index) {
				Notice notice = new Notice();
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setContent(cursor.getString(cursor.getColumnIndex("content")));
				notice.setStatus(cursor.getString(cursor.getColumnIndex("status")));
				notice.setWith(cursor.getString(cursor.getColumnIndex("with")));
				notice.setTime(cursor.getString(cursor.getColumnIndex("time")));
				notice.setDispStatus(cursor.getString(cursor.getColumnIndex("dispStatus")));
				notice.setReadStatus(cursor.getString(cursor.getColumnIndex("readStatus")));
				byte[] abs = cursor
						.getBlob(cursor.getColumnIndex("avatar_img"));
				notice.setAvatar(ImageUtil.byteToBitmap(abs));

				return notice;
			}

		}, "select * from im_notice where with=? and status=?", new String[] { with, status });
	}

	/**
	 * 
	 * 根据名称获取通知.
	 * 
	 * @param id
	 * @author xuweinan
	 */
	public List<Notice> getNoticeByWith(String with) {
		return mDBHelper.queryForList(new RowMapper<Notice>() {

			@Override
			public Notice mapRow(Cursor cursor, int index) {
				Notice notice = new Notice();
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setContent(cursor.getString(cursor.getColumnIndex("content")));
				notice.setStatus(cursor.getString(cursor.getColumnIndex("status")));
				notice.setWith(cursor.getString(cursor.getColumnIndex("with")));
				notice.setTime(cursor.getString(cursor.getColumnIndex("time")));
				notice.setDispStatus(cursor.getString(cursor.getColumnIndex("dispStatus")));
				notice.setReadStatus(cursor.getString(cursor.getColumnIndex("readStatus")));
				byte[] abs = cursor
						.getBlob(cursor.getColumnIndex("avatar_img"));
				notice.setAvatar(ImageUtil.byteToBitmap(abs));

				return notice;
			}

		}, "select * from im_notice where with=?", new String[] { with });
	}
	/**
	 * 
	 * 获取所有未读聊消息.(分类)1 好友添加 2系统 消息 3 聊天
	 * 
	 * @return
	 * @author xuweinan
	public List<Notice> getUnReadNoticeListByType(int type) {

		String sql;
		String[] str = new String[] { "" + Notice.UNREAD, "" + type };
		sql = "select * from im_notice where status=? and type=? order by notice_time desc";
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<Notice> list = st.queryForList(new RowMapper<Notice>() {

			@Override
			public Notice mapRow(Cursor cursor, int index) {
				Notice notice = new Notice();
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setContent(cursor.getString(cursor
						.getColumnIndex("content")));
				notice.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				notice.setFrom(cursor.getString(cursor
						.getColumnIndex("notice_from")));
				notice.setTo(cursor.getString(cursor
						.getColumnIndex("notice_to")));
				notice.setNoticeType(cursor.getInt(cursor
						.getColumnIndex("type")));
				notice.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
				notice.setNoticeTime(cursor.getString(cursor
						.getColumnIndex("notice_time")));
				byte[] abs = cursor
						.getBlob(cursor.getColumnIndex("avatar_img"));
				notice.setAvatar(ImageUtil.byteToBitmap(abs));

				return notice;
			}
		}, sql, str);
		return list;
	}
	 */

	/**
	 * 
	 * 获取指定状态的通知数量
	 * 
	 * @return
	 * @author xuweinan
	 */
	public Integer getNoticeCountByStatus(String status) {
		return mDBHelper.getCount(
				"select _id from im_notice where status=?",
				new String[] { status });
	}
	
	/**
	 * 
	 * 获取未读的通知数量
	 * 
	 * @return
	 * @author xuweinan
	 */
	public Integer getUnreadNoticeCount() {
		return mDBHelper.getCount(
				"select _id from im_notice where readStatus=?",
				new String[] { Notice.UNREAD });
	}

	/**
	 * 
	 * 获取来自某个好友的添加请求
	 * 
	 * @return
	 * @author shimiso
	 * @update 2012-7-5 下午1:59:53
	public Integer getUnReadNoticeCountByTypeAndFrom(int type, String from) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st
				.getCount(
						"select _id from im_notice where status=? and type=? and notice_from=?",
						new String[] { "" + Notice.UNREAD, "" + type, from });
	}
	 */

	/**
	 * 
	 * 更新某人的通知状态.
	 * 
	 * @param status
	 * @author xuweinan
	 */
	public void updateStatusByWith(String with, String status) {
		ContentValues values = new ContentValues();
		values.put("status", status);
		mDBHelper.update("im_notice", values, "with=?", new String[] { with });
	}
	
	/**
	 * 
	 * 更新所有通知的已读状态.
	 * 
	 * @param status
	 * @author xuweinan
	 */
	public void updateAllReadStatus(String readStatus) {
		ContentValues values = new ContentValues();
		values.put("readStatus", readStatus);
		mDBHelper.update("im_notice", values, null, null);
	}
	
	/**
	 * 
	 * 更新所有通知的显示状态.
	 * 
	 * @param dispStatus
	 * @author xuweinan
	 */
	public void updateAllDispStatus(String dispStatus) {
		ContentValues values = new ContentValues();
		values.put("dispStatus", dispStatus);
		mDBHelper.update("im_notice", values, null, null);
	}
	
	/**
	 * 
	 * 更新某条通知的显示状态.
	 * 
	 * @param status
	 * @author xuweinan
	 */
	public void updateDispStatusById(String id, String dispStatus) {
		ContentValues values = new ContentValues();
		values.put("dispStatus", dispStatus);
		mDBHelper.updateById("im_notice", id, values);
	}
	
	/**
	 * 
	 * 更新某条通知的已读状态.
	 * 
	 * @param status
	 * @author xuweinan
	 */
	public void updateReadStatusById(String id, String readStatus) {
		ContentValues values = new ContentValues();
		values.put("readStatus", readStatus);
		mDBHelper.updateById("im_notice", id, values);
	}

	/**
	 * 
	 * 分页获取所有聊消息.(分类)1 好友添加 2系统 消息 3 聊天 降序排列
	 * 
	 * @param isRead
	 *            0 已读 1 未读 2 全部
	 * @param type
	 *            2系统， 3聊天，1 好友添加
	 * @return
	 * @author shimiso
	 * @update 2012-7-6 下午3:22:53
	public List<Notice> getNoticeListByTypeAndPage(int type, int isRead,
			int pageNum, int pageSize) {
		int fromIndex = (pageNum - 1) * pageSize;
		StringBuilder sb = new StringBuilder();
		String[] str = null;
		sb.append("select * from im_notice where type=?");
		if (Notice.UNREAD == isRead || Notice.READ == isRead) {
			str = new String[] { "" + type, "" + isRead, "" + fromIndex,
					"" + pageSize };
			sb.append(" and status=? ");
		} else {
			str = new String[] { "" + type, "" + fromIndex, "" + pageSize };
		}
		sb.append(" order by notice_time desc limit ? , ? ");
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<Notice> list = st.queryForList(new RowMapper<Notice>() {

			@Override
			public Notice mapRow(Cursor cursor, int index) {
				Notice notice = new Notice();
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setContent(cursor.getString(cursor.getColumnIndex("content")));
				notice.setStatus(cursor.getString(cursor.getColumnIndex("status")));
				notice.setWith(cursor.getString(cursor.getColumnIndex("with")));
				notice.setTime(cursor.getString(cursor.getColumnIndex("time")));
				notice.setType(cursor.getString(cursor.getColumnIndex("type")));
				byte[] abs = cursor
						.getBlob(cursor.getColumnIndex("avatar_img"));
				notice.setAvatar(ImageUtil.byteToBitmap(abs));

				return notice;
			}
		}, sb.toString(), str);
		return list;
	}
	 */

	/**
	 * 
	 *  按照通知类型获取所有通知
	 * 
	 * @return List<Notice>
	 * @author xuweinan
	 */
	public List<Notice> getAllNoticeListByDispStatus(String dispStatus) {

		StringBuilder sb = new StringBuilder();
		sb.append("select * from im_notice where dispStatus=? order by time desc");
		String[] paras = new String[] { dispStatus };

		List<Notice> list = mDBHelper.queryForList(new RowMapper<Notice>() {

			@Override
			public Notice mapRow(Cursor cursor, int index) {
				Notice notice = new Notice();
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setContent(cursor.getString(cursor.getColumnIndex("content")));
				notice.setStatus(cursor.getString(cursor.getColumnIndex("status")));
				notice.setWith(cursor.getString(cursor.getColumnIndex("with")));
				notice.setTime(cursor.getString(cursor.getColumnIndex("time")));
				notice.setDispStatus(cursor.getString(cursor.getColumnIndex("dispStatus")));
				notice.setReadStatus(cursor.getString(cursor.getColumnIndex("readStatus")));
				byte[] abs = cursor
						.getBlob(cursor.getColumnIndex("avatar_img"));
				notice.setAvatar(ImageUtil.byteToBitmap(abs));

				return notice;
			}
		}, sb.toString(), paras);
		return list;
	}
	
	/**
	 * 
	 *  获取所有通知
	 * 
	 * @return List<Notice>
	 * @author xuweinan
	 */
	public List<Notice> getAllNoticeList() {

		StringBuilder sb = new StringBuilder();
		sb.append("select * from im_notice order by time desc");

		List<Notice> list = mDBHelper.queryForList(new RowMapper<Notice>() {

			@Override
			public Notice mapRow(Cursor cursor, int index) {
				Notice notice = new Notice();
				notice.setId(cursor.getString(cursor.getColumnIndex("_id")));
				notice.setContent(cursor.getString(cursor.getColumnIndex("content")));
				notice.setStatus(cursor.getString(cursor.getColumnIndex("status")));
				notice.setWith(cursor.getString(cursor.getColumnIndex("with")));
				notice.setTime(cursor.getString(cursor.getColumnIndex("time")));
				notice.setDispStatus(cursor.getString(cursor.getColumnIndex("dispStatus")));
				notice.setReadStatus(cursor.getString(cursor.getColumnIndex("readStatus")));
				byte[] abs = cursor
						.getBlob(cursor.getColumnIndex("avatar_img"));
				notice.setAvatar(ImageUtil.byteToBitmap(abs));

				return notice;
			}
		}, sb.toString(), null);
		return list;
	}

	/**
	 * 根据id删除记录
	 * 
	 * @author xuweinan
	 * @param noticeId
	 */
	public void delNoticeById(String noticeId) {
		mDBHelper.deleteById("im_notice", noticeId);
	}

	/**
	 * 
	 * 删除全部记录
	 * 
	 * @update 2013-4-15 下午6:33:19
	 */
	public void delAllNotice() {
		mDBHelper.execSQL("delete from im_notice");
	}

	/**
	 * 删除与某人的通知 
	 * @author xuweinan
	 * @param with
	 */
	public int delNoticeByWith(String with) {
		if (StringUtil.empty(with)) {
			return 0;
		}
		return mDBHelper.deleteByCondition("im_notice", "with=?",
				new String[] { with });
	}

	/**
	 * 好友消息通知
	 * 
	 * @param user
	 * @param msg
	 */

	public void dispatchIMMessageNotify(User user, boolean doUpdate) {
		if (user == null) {
			Log.w(TAG, "dispatchIMMessageNotify null user");
			return;
		}
		int userId = user.getName().hashCode();
		
		int unreadCount = MessageManager.getInstance().getMsgCountByWithAndReadStatus(user.getJID(), IMMessage.UNREAD);

		User fromUser = user.clone();
		Log.i(TAG, "notify new message from user:" + user + ", notify user info:" + fromUser);

		Intent notifyIntent = new Intent(mCntx, ChatActivity.class);
		notifyIntent.putExtra(User.userKey, fromUser);
		notifyIntent.putExtra(IMMessage.PROP_CHATTYPE, IMMessage.SINGLE);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		String ticker = user.getNickName() + "发来1条新消息";
		String content = "发来" + (unreadCount == 0 ? 1: unreadCount) + "条新消息";
		
		Bitmap avatar = null;
		if (user.getHeadImg() != null) {
			avatar = user.getHeadImg();
		} else {
			if (user.getGender() == null) {
				avatar = BitmapFactory.decodeResource(mCntx.getResources(),
						R.drawable.default_avatar_male);
			} else {
				avatar = BitmapFactory
						.decodeResource(
								mCntx.getResources(),
								user.getGender().equals(User.MALE) ? R.drawable.default_avatar_male
										: R.drawable.default_avatar_female);
			}

		}

		avatar = Bitmap.createScaledBitmap(avatar, 80, 80, true);

		dispatchSystemNotify(userId, avatar, user.getNickName(), content,
				ticker, notifyIntent, false, false, doUpdate);
	}

	/**
	 * 好友添加通知
	 * 
	 * @param user
	 * @param msg
	 */

	public void dispatchRosterMessageNotify(User user) {
		if (user == null) {
			Log.w(TAG, "dispatchRosterMessageNotify null user");
			return;
		}
		Intent notifyIntent = new Intent(mCntx, UserNoticeActivity.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		Bitmap avatar = null;
		if (user.getHeadImg() != null) {
			avatar = user.getHeadImg();
		} else {
			if (user.getGender() == null) {
				avatar = BitmapFactory.decodeResource(mCntx.getResources(),
						R.drawable.default_avatar_male);
			} else {
				avatar = BitmapFactory
						.decodeResource(
								mCntx.getResources(),
								user.getGender().equals(User.MALE) ? R.drawable.default_avatar_male
										: R.drawable.default_avatar_female);
			}

		}

		int unreadRosterCount = getNoticeCountByStatus(Notice.STATUS_ADD_REQUEST);

		String ticker = user.getNickName() + "请求和您成为密友";
		String content = null;
		String title = null;
		if (unreadRosterCount == 1) {
			title = user.getNickName();
			content = "请求和您成为密友";
		} else if (unreadRosterCount > 1) {
			title = "有" + unreadRosterCount + "个人希望和您成为密友";
			content = "点击进入密友推荐列表查看";
			avatar = BitmapFactory.decodeResource(mCntx.getResources(),
					R.drawable.ic_group_mainpage_chat_nor);
		} else {
			return;
		}

		avatar = Bitmap.createScaledBitmap(avatar, 80, 80, true);

		// 好友添加通知使用统一的 ID 0
		dispatchSystemNotify(0, avatar, title, content, ticker, notifyIntent, true, true, false);
	}

	/**
	 * 未接来电通知
	 * 
	 * @param user
	 * @param msg
	 */

	public void dispatchMissedCallNotify(User user) {
		if (user == null) {
			Log.w(TAG, "dispatchMissedCallNotify null user");
			return;
		}
		int userId = user.getName().hashCode();
		Log.i(TAG, "notify new missed call from user:" + user);
		
		Intent notifyIntent = new Intent(mCntx, UserCallLogsActivity.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		notifyIntent.putExtra(User.userKey, user);

		Bitmap avatar = null;
		if (user.getHeadImg() != null) {
			avatar = user.getHeadImg();
		} else {
			if (user.getGender() == null) {
				avatar = BitmapFactory.decodeResource(mCntx.getResources(),
						R.drawable.default_avatar_male);
			} else {
				avatar = BitmapFactory
						.decodeResource(
								mCntx.getResources(),
								user.getGender().equals(User.MALE) ? R.drawable.default_avatar_male
										: R.drawable.default_avatar_female);
			}

		}

		String ticker = "有来自" + user.getNickName() + "的未接来电";
		String content = null;
		String title = null;
		title = user.getNickName();
		content = "语音呼叫您，您没有接听";

		avatar = Bitmap.createScaledBitmap(avatar, 80, 80, true);

		dispatchSystemNotify(userId, avatar, title, content, ticker, notifyIntent, true, true, false);
	}
	
	public void dispatchInCallNotify(User user, int state) {
		if (user == null) {
			Log.w(TAG, "dispatchInCallNotify null user");
			return;
		}
		int userId = user.getJID().hashCode();

		Intent notifyIntent = new Intent(SipManager.ACTION_SIP_CALL_UI);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		Bitmap avatar = null;
		String gender = "他";
		if (user.getHeadImg() != null) {
			avatar = user.getHeadImg();
		} else {
			if (user.getGender() == null) {
				avatar = BitmapFactory.decodeResource(mCntx.getResources(),
						R.drawable.default_avatar_male);
			} else {
				avatar = BitmapFactory
						.decodeResource(
								mCntx.getResources(),
								user.getGender().equals(User.MALE) ? R.drawable.default_avatar_male
										: R.drawable.default_avatar_female);
			}

		}
		
		if (user.getGender().equals(User.FEMALE)) {
			gender = "她";
		}

		String ticker = null;
		String content = null;

		if (state == SipCallSession.InvState.INCOMING) {
			ticker = "有" + user.getNickName() + "的来电...";
			content = "正在呼叫您，请求语音通话...";
		}
		else if (state == SipCallSession.InvState.CALLING) {
			ticker = "开始呼叫" + user.getNickName() + "...";
			content = "正在呼叫" + gender + "，请求语音通话...";
		}
		else if (state == SipCallSession.InvState.CONFIRMED) {
			ticker = "与" + user.getNickName() + "通话中";
			content = "正在与您进行语音通话";
		}
		
		String title = null;
		title = user.getNickName();

		avatar = Bitmap.createScaledBitmap(avatar, 80, 80, true);

		dispatchSystemNotify(userId, avatar, title, content, ticker, notifyIntent, false, false, false);
	}
	
	
	
	/**
	 * 清除指定好友的消息通知
	 * 
	 * @param id
	 * @author xuweinan
	 */
	public void clearIMMessageNotify(User user) {
		if (user == null) {
			Log.w(TAG, "clearIMMessageNotify null user");
			return;
		}
		int userId = user.getName().hashCode();
		mNotificationManager.cancel(userId);
	}

	/**
	 * 清除指定好友的消息通知
	 * 
	 * @param id
	 * @author xuweinan
	 */
	public void clearIMMessageNotify(String name) {
		int userId = name.hashCode();
		mNotificationManager.cancel(userId);
	}

	/**
	 * 清除所有密信的通知
	 */
	public void clearAllMessageNotify() {
		mNotificationManager.cancelAll();
	}

	/**
	 * 清除所有的好友通知
	 */
	public void clearRosterMessageNotify() {
		mNotificationManager.cancel(0);
	}
	
	public void clearInCallNotify(User u) {
		if (u == null) {
			Log.w(TAG, "clearInCallNotify null user");
			return;
		}
		int userId = u.getJID().hashCode();
		mNotificationManager.cancel(userId);
	}

	/**
	 * 发送消息通知
	 * @param id
	 * @param avatar
	 * @param title
	 * @param content
	 * @param ticker
	 * @param intent
	 */
	protected void dispatchSystemNotify(int id, Bitmap avatar, String title,
			String content, String ticker, Intent intent, boolean autoCancel, boolean canBeClean, boolean alertOne) {

		/* 创建PendingIntent作为设置递延运行的Activity */
		PendingIntent appIntent = PendingIntent
				.getActivity(mCntx, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mCntx);

		mBuilder.setSmallIcon(R.drawable.ic_group_mainpage_chat_nor);
		mBuilder.setContentTitle(title);
		mBuilder.setContentText(content);
		mBuilder.setTicker(ticker);
		mBuilder.setLargeIcon(avatar);
		mBuilder.setDefaults(Notification.DEFAULT_SOUND);
		mBuilder.setAutoCancel(autoCancel);
		mBuilder.setOnlyAlertOnce(alertOne);
		mBuilder.setContentIntent(appIntent);

		Notification n = mBuilder.build();
		if (!canBeClean) {
			n.flags |= Notification.FLAG_NO_CLEAR;
		}
		mNotificationManager.notify(id, n);
	}

}
