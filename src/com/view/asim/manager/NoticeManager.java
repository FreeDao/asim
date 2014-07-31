package com.view.asim.manager;

import java.util.List;
import java.util.UUID;

import com.csipsimple.api.SipManager;
import com.view.asim.comm.Constant;
import com.view.asim.activity.im.ChatActivity;
import com.view.asim.activity.im.UserNoticeActivity;
import com.view.asim.activity.voip.UserCallLogsActivity;
import com.view.asim.db.DBManager;
import com.view.asim.db.SQLiteTemplate;
import com.view.asim.db.SQLiteTemplate.RowMapper;
import com.view.asim.model.IMMessage;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.util.ImageUtil;
import com.view.asim.util.StringUtil;

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
import android.util.Log;
import com.view.asim.R;

/**
 * 
 * ֪ͨ����.
 * 
 * @author xuweinan
 */
public class NoticeManager {
	private final static String TAG = "NoticeManager";
	private static NoticeManager noticeManager = null;
	private static DBManager manager = null;
	private String mCurrentChatUser = null;
	private Context mCntx = null;
	private NotificationManager mNotificationManager = null;

	private NoticeManager(Context context, LoginConfig cfg) {
//		SharedPreferences sharedPre = context.getSharedPreferences(
//				Constant.IM_SET_PREF, Context.MODE_PRIVATE);
//		String databaseName = sharedPre.getString(Constant.USERNAME, null);
		String databaseName = cfg.getUsername();
		Log.d(TAG, "init database " + databaseName);
		manager = DBManager.getInstance(context, databaseName);

		this.mCntx = context;
		mNotificationManager = (NotificationManager) mCntx
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public static NoticeManager getInstance(Context context, LoginConfig cfg) {

		if (noticeManager == null) {
			noticeManager = new NoticeManager(context, cfg);
			//noticeManager.init();
		}

		return noticeManager;
	}
	
	public static NoticeManager getInstance() {
		return noticeManager;
	}
	
	public void destroy() {
		noticeManager = null;
		manager = null;
	}

	public void init() {
		if (ContacterManager.contacters == null) {
			return;
		}
		
		// �˲�����б����Ϣ�Ƿ��Ӧ��ɾ�������ڵĺ��ѵ���Ϣ
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
	 * ������Ϣ.
	 * 
	 * @param notice
	 * @author xuweinan
	 * @update 2014-4-16
	 */
	public long saveNotice(Notice notice) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
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
		return st.insert("im_notice", contentValues);
	}

	/**
	 * 
	 * ��ȡ����δ����Ϣ.
	 * 
	 * @return
	 * @author shimiso
	 * @update 2012-5-16 ����3:22:53
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
	 * ����״̬.
	 * 
	 * @param status
	 * @author xuweinan
	 */
	public void updateStatusById(String id, String status) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues contentValues = new ContentValues();
		contentValues.put("status", status);
		st.updateById("im_notice", id, contentValues);
	}
	
	/**
	 * 
	 * ����ʱ��.
	 * 
	 * @param status
	 * @author xuweinan
	 */
	public void updateTimeById(String id, String time) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues contentValues = new ContentValues();
		contentValues.put("time", time);
		st.updateById("im_notice", id, contentValues);
	}

	/**
	 * 
	 * ������Ӻ���״̬.
	 * 
	 * @param status
	 * @author shimiso
	 * @update 2012-5-16 ����3:22:44
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
	 * ��ȡδ����Ϣ������.
	 * 
	 * @return
	 * @author shimiso
	 * @update 2012-5-16 ����6:22:03
	public Integer getUnReadNoticeCount() {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.getCount("select _id from im_notice where status=?",
				new String[] { "" + Notice.UNREAD });
	}
	 */

	/**
	 * 
	 * ����������ȡ֪ͨ.
	 * 
	 * @param id
	 * @author xuweinan
	 */
	public Notice getNoticeById(String id) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.queryForObject(new RowMapper<Notice>() {

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
	 * �������ƺ�״̬��ȡ֪ͨ.
	 * 
	 * @param id
	 * @author xuweinan
	 */
	public List<Notice> getNoticeByWithAndStatus(String with, String status) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.queryForList(new RowMapper<Notice>() {

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
	 * �������ƻ�ȡ֪ͨ.
	 * 
	 * @param id
	 * @author xuweinan
	 */
	public List<Notice> getNoticeByWith(String with) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.queryForList(new RowMapper<Notice>() {

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
	 * ��ȡ����δ������Ϣ.(����)1 ������� 2ϵͳ ��Ϣ 3 ����
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
	 * ��ȡָ��״̬��֪ͨ����
	 * 
	 * @return
	 * @author xuweinan
	 */
	public Integer getNoticeCountByStatus(String status) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.getCount(
				"select _id from im_notice where status=?",
				new String[] { status });
	}
	
	/**
	 * 
	 * ��ȡδ����֪ͨ����
	 * 
	 * @return
	 * @author xuweinan
	 */
	public Integer getUnreadNoticeCount() {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.getCount(
				"select _id from im_notice where readStatus=?",
				new String[] { Notice.UNREAD });
	}

	/**
	 * 
	 * ��ȡ����ĳ�����ѵ��������
	 * 
	 * @return
	 * @author shimiso
	 * @update 2012-7-5 ����1:59:53
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
	 * ����ĳ�˵�֪ͨ״̬.
	 * 
	 * @param status
	 * @author xuweinan
	 */
	public void updateStatusByWith(String with, String status) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues values = new ContentValues();
		values.put("status", status);
		st.update("im_notice", values, "with=?", new String[] { with });
	}
	
	/**
	 * 
	 * ��������֪ͨ���Ѷ�״̬.
	 * 
	 * @param status
	 * @author xuweinan
	 */
	public void updateAllReadStatus(String readStatus) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues values = new ContentValues();
		values.put("readStatus", readStatus);
		st.update("im_notice", values, null, null);
	}
	
	/**
	 * 
	 * ��������֪ͨ����ʾ״̬.
	 * 
	 * @param dispStatus
	 * @author xuweinan
	 */
	public void updateAllDispStatus(String dispStatus) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues values = new ContentValues();
		values.put("dispStatus", dispStatus);
		st.update("im_notice", values, null, null);
	}
	
	/**
	 * 
	 * ����ĳ��֪ͨ����ʾ״̬.
	 * 
	 * @param status
	 * @author xuweinan
	 */
	public void updateDispStatusById(String id, String dispStatus) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues values = new ContentValues();
		values.put("dispStatus", dispStatus);
		st.updateById("im_notice", id, values);
	}
	
	/**
	 * 
	 * ����ĳ��֪ͨ���Ѷ�״̬.
	 * 
	 * @param status
	 * @author xuweinan
	 */
	public void updateReadStatusById(String id, String readStatus) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues values = new ContentValues();
		values.put("readStatus", readStatus);
		st.updateById("im_notice", id, values);
	}

	/**
	 * 
	 * ��ҳ��ȡ��������Ϣ.(����)1 ������� 2ϵͳ ��Ϣ 3 ���� ��������
	 * 
	 * @param isRead
	 *            0 �Ѷ� 1 δ�� 2 ȫ��
	 * @param type
	 *            2ϵͳ�� 3���죬1 �������
	 * @return
	 * @author shimiso
	 * @update 2012-7-6 ����3:22:53
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
	 *  ����֪ͨ���ͻ�ȡ����֪ͨ
	 * 
	 * @return List<Notice>
	 * @author xuweinan
	 */
	public List<Notice> getAllNoticeListByDispStatus(String dispStatus) {

		StringBuilder sb = new StringBuilder();
		sb.append("select * from im_notice where dispStatus=? order by time desc");
		String[] paras = new String[] { dispStatus };

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
	 *  ��ȡ����֪ͨ
	 * 
	 * @return List<Notice>
	 * @author xuweinan
	 */
	public List<Notice> getAllNoticeList() {

		StringBuilder sb = new StringBuilder();
		sb.append("select * from im_notice order by time desc");

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
	 * ����idɾ����¼
	 * 
	 * @author xuweinan
	 * @param noticeId
	 */
	public void delNoticeById(String noticeId) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		st.deleteById("im_notice", noticeId);
	}

	/**
	 * 
	 * ɾ��ȫ����¼
	 * 
	 * @update 2013-4-15 ����6:33:19
	 */
	public void delAllNotice() {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		st.execSQL("delete from im_notice");
	}

	/**
	 * ɾ����ĳ�˵�֪ͨ 
	 * @author xuweinan
	 * @param with
	 */
	public int delNoticeByWith(String with) {
		if (StringUtil.empty(with)) {
			return 0;
		}
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.deleteByCondition("im_notice", "with=?",
				new String[] { with });
	}

	/**
	 * ������Ϣ֪ͨ
	 * 
	 * @param user
	 * @param msg
	 */

	public void dispatchIMMessageNotify(User user) {
		int userId = user.getName().hashCode();
		int unreadCount = MessageManager.getInstance().getMsgCountByWithAndReadStatus(user.getJID(), IMMessage.UNREAD);

		User fromUser = user.clone();
		Log.i(TAG, "notify new message from user:" + user + ", notify user info:" + fromUser);

		Intent notifyIntent = new Intent(mCntx, ChatActivity.class);
		notifyIntent.putExtra(User.userKey, fromUser);
		notifyIntent.putExtra(IMMessage.PROP_CHATTYPE, IMMessage.SINGLE);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		String ticker = user.getNickName() + "����1������Ϣ";
		String content = "����" + (unreadCount + 1) + "������Ϣ";
		
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
				ticker, notifyIntent, true);
	}

	/**
	 * �������֪ͨ
	 * 
	 * @param user
	 * @param msg
	 */

	public void dispatchRosterMessageNotify(User user) {

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

		String ticker = user.getNickName() + "���������Ϊ����";
		String content = null;
		String title = null;
		if (unreadRosterCount == 1) {
			title = user.getNickName();
			content = "���������Ϊ����";
		} else if (unreadRosterCount > 1) {
			title = "��" + unreadRosterCount + "����ϣ��������Ϊ����";
			content = "������������Ƽ��б�鿴";
			avatar = BitmapFactory.decodeResource(mCntx.getResources(),
					R.drawable.ic_group_mainpage_chat_nor);
		} else {
			return;
		}

		avatar = Bitmap.createScaledBitmap(avatar, 80, 80, true);

		// �������֪ͨʹ��ͳһ�� ID 0
		dispatchSystemNotify(0, avatar, title, content, ticker, notifyIntent, true);
	}

	/**
	 * δ������֪ͨ
	 * 
	 * @param user
	 * @param msg
	 */

	public void dispatchMissedCallNotify(User user) {
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

		String ticker = "������" + user.getNickName() + "��δ������";
		String content = null;
		String title = null;
		title = user.getNickName();
		content = "��������������û�н���";

		avatar = Bitmap.createScaledBitmap(avatar, 80, 80, true);

		dispatchSystemNotify(userId, avatar, title, content, ticker, notifyIntent, true);
	}
	
	public void dispatchInCallNotify(User user) {
		int userId = user.getJID().hashCode();

		Intent notifyIntent = new Intent(SipManager.ACTION_SIP_CALL_UI);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

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

		String ticker = "��" + user.getNickName() + "ͨ����";
		String content = null;
		String title = null;
		title = user.getNickName();
		content = "����������������ͨ��";

		avatar = Bitmap.createScaledBitmap(avatar, 80, 80, true);

		dispatchSystemNotify(userId, avatar, title, content, ticker, notifyIntent, false);
	}
	
	
	
	/**
	 * ���ָ�����ѵ���Ϣ֪ͨ
	 * 
	 * @param id
	 * @author xuweinan
	 */
	public void clearIMMessageNotify(User user) {
		int userId = user.getName().hashCode();
		mNotificationManager.cancel(userId);
	}

	/**
	 * ���ָ�����ѵ���Ϣ֪ͨ
	 * 
	 * @param id
	 * @author xuweinan
	 */
	public void clearIMMessageNotify(String name) {
		int userId = name.hashCode();
		mNotificationManager.cancel(userId);
	}

	/**
	 * ����������ŵ�֪ͨ
	 */
	public void clearAllMessageNotify() {
		mNotificationManager.cancelAll();
	}

	/**
	 * ������еĺ���֪ͨ
	 */
	public void clearRosterMessageNotify() {
		mNotificationManager.cancel(0);
	}
	
	public void clearInCallNotify(User u) {
		int userId = u.getJID().hashCode();
		mNotificationManager.cancel(userId);
	}

	/**
	 * ������Ϣ֪ͨ
	 * @param id
	 * @param avatar
	 * @param title
	 * @param content
	 * @param ticker
	 * @param intent
	 */
	protected void dispatchSystemNotify(int id, Bitmap avatar, String title,
			String content, String ticker, Intent intent, boolean canBeClean) {

		/* ����PendingIntent��Ϊ���õ������е�Activity */
		PendingIntent appIntent = PendingIntent
				.getActivity(mCntx, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification.Builder mBuilder = new Notification.Builder(mCntx);

		mBuilder.setSmallIcon(R.drawable.ic_group_mainpage_chat_nor);
		mBuilder.setContentTitle(title);
		mBuilder.setContentText(content);
		mBuilder.setTicker(ticker);
		mBuilder.setLargeIcon(avatar);
		mBuilder.setDefaults(Notification.DEFAULT_SOUND);
		mBuilder.setAutoCancel(true);
		mBuilder.setContentIntent(appIntent);

		Notification n = mBuilder.build();
		if (!canBeClean) {
			n.flags |= Notification.FLAG_NO_CLEAR;
		}
		mNotificationManager.notify(id, n);
	}

}
