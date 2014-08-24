package com.view.asim.service;


import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

import com.view.asim.activity.im.BurnMsgViewActivity;
import com.view.asim.activity.im.UserConflictActivity;
import com.view.asim.comm.ApplicationContext;
import com.view.asim.comm.Constant;
import com.view.asim.dbg.LogcatHelper;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.AppConfigManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.utils.DateUtil;
import com.view.asim.utils.StringUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.view.asim.R;

/**
 * 
 * ��ϵ�˷���.
 * 
 * @author xuweinan
 */
public class IMContactService extends Service {
	
	private static final String TAG = "IMContactService";

	private Context context;

	@Override
	public void onCreate() {
		Log.d(TAG, "service create");

		context = this;
		super.onCreate();
		initRoster();
		
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(Constant.AUKEY_STATUS_UPDATE);
		mFilter.addAction(Constant.ACTION_RECONNECT_STATE);

		registerReceiver(reConnectionBroadcastReceiver, mFilter);

	}
	
	BroadcastReceiver reConnectionBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (Constant.ACTION_RECONNECT_STATE.equals(action)) {
				String status = intent.getStringExtra(
						Constant.RECONNECT_STATE);
				if(status.equals(XmppConnectionManager.DISCONNECTED)) {
					Log.i(TAG, "disconnect succ, uninit roster listener");
					unInitRoster();
				}
				else if(status.equals(XmppConnectionManager.CONNECTED)) {
					Log.i(TAG, "connection succ, init roster listener");
					initRoster();
				}
			}
			else if (Constant.AUKEY_STATUS_UPDATE.equals(action)) {
				Log.i(TAG, "aukey state changed, notify im server");
				
				updateMyUserInfo();
			}
		}

	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//init();
		//return START_NOT_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}
	
	// ������Ϣ����������
	public void updateMyUserInfo() {
		new UserInfoUpdateThread().start();
	}

	// �û�������Ϣ�����߳�
	private class UserInfoUpdateThread extends Thread {
		
		@Override
		public void run() {
			Log.d(TAG, "UserInfoUpdateThread");
			if (ContacterManager.userMe != null) {
				ContacterManager.userMe.setSecurity(AUKeyManager.getInstance().getAUKeyStatus());
				
				XMPPConnection conn = XmppConnectionManager.getInstance().getConnection();
				if (conn != null) {
					ContacterManager.saveUserVCard(conn, ContacterManager.userMe);
//					Presence presence = new Presence(Presence.Type.available);
//					presence.setStatus("update");
//					conn.sendPacket(presence);
					XmppConnectionManager.getInstance().UpdateAvailable(false);

					/*
					Collection<RosterEntry> rosters = conn.getRoster()
							.getEntries();
					for (RosterEntry rosterEntry : rosters) {
						Log.d(TAG, "presence updated to " + rosterEntry.getUser());
						presence.setTo(rosterEntry.getUser());
						conn.sendPacket(presence);
					}
					*/
				}
			}
		}
	}
	
	
	/**
	 * ���һ�����������������������
	 */
	private void addSubscriptionListener() {
		PacketFilter filter = new PacketFilter() {
			@Override
			public boolean accept(Packet packet) {
				//Log.d(TAG, "packet: " + packet);
				
				if (packet instanceof RosterPacket) {
					Collection<RosterPacket.Item> items = ((RosterPacket) packet).getRosterItems();
					for(RosterPacket.Item item: items) {
						Log.d(TAG, "roster item:" + item.getUser() + ", " + item.getName() + "," + item.getItemStatus() + "," + item.getItemType());
					}
					
				}
				
				if (packet instanceof Presence) {
					Presence presence = (Presence) packet;
					Log.d(TAG, "presence packet type " + presence.getType() + ", status " + presence.getStatus() + ", from " + presence.getFrom());
					
					if (presence.getType().equals(Presence.Type.subscribe) || 
						presence.getType().equals(Presence.Type.unsubscribe) ||
						presence.getType().equals(Presence.Type.subscribed) ||
						presence.getType().equals(Presence.Type.available) ||
						presence.getType().equals(Presence.Type.unavailable)
						) {
						return true;
					}
				}
				return false;
			}
		};
		XmppConnectionManager.getInstance().getConnection()
				.addPacketListener(subscriptionPacketListener, filter);
	}

	private void removeSubscriptionListener() {
		if (XmppConnectionManager.getInstance().getConnection() != null) {
			XmppConnectionManager.getInstance().getConnection()
				.removePacketListener(subscriptionPacketListener);
		}
	}
	
	private void unInitRoster() {
		if (XmppConnectionManager.getInstance().getConnection() != null ) {
	
			removeSubscriptionListener();
			if (XmppConnectionManager.getInstance().getRoster() != null) {
				XmppConnectionManager.getInstance().getRoster().removeRosterListener(rosterListener);
			}
		}
	}
	/**
	 * ��ʼ�������� ��������ʱ�����»�����
	 */
	private void initRoster() {
		if (XmppConnectionManager.getInstance().getConnection() != null ) {
			addSubscriptionListener();
			XmppConnectionManager.getInstance().getRoster().addRosterListener(rosterListener);
		}
	}

	private PacketListener subscriptionPacketListener = new PacketListener() {

		@Override
		public void processPacket(Packet packet) {
			Presence presence = (Presence) packet;

			String from = packet.getFrom().split("@")[0];
			if (presence.getType().equals(Presence.Type.available) && from.equals(ContacterManager.userMe.getName())) {
				Log.i(TAG, "recv presence packet " + presence.getType() + " from me");
				String newResource = packet.getFrom().split("/")[1];
				String oldResource = AppConfigManager.getInstance().getResource();
				if (!newResource.equals(oldResource)) {

					long myLoginTime = AppConfigManager.getInstance().getLoginTime();
					String other = (String) presence.getProperty(IMMessage.PROP_TIME);
					if (other != null) {
						long otherLoginTime = Long.parseLong(other);
						Log.i(TAG, "login on other device, my login time " + myLoginTime + ", other login time" + otherLoginTime);
						if(myLoginTime < otherLoginTime) {
							Intent intent = new Intent();
							intent.setClass(context, UserConflictActivity.class);
							intent.putExtra(User.userResourceKey, newResource);
					        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					        startActivity(intent); 
						}
					}
				}
				else {
					Log.i(TAG, "I logined on my device, ignore the presence!");
				}
			}

			/*
			// �յ�������Ϣ����֪ͨ
			if (presence.getType().equals(Presence.Type.available) && presence.getStatus().equals("update")) {
				Log.d(TAG, "receive available packet from " + packet.getFrom());

				Intent intent = new Intent();
				intent.setAction(Constant.ROSTER_PRESENCE_CHANGED);
				String subscriber = presence.getFrom().substring(0,
						presence.getFrom().indexOf("/"));
				RosterEntry entry = roster.getEntry(subscriber);
				
				if (ContacterManager.contacters.containsKey(subscriber)) {
					// ��״̬�ı�֮ǰ��user�㲥��ȥ
					intent.putExtra(User.userKey,
							ContacterManager.contacters.get(subscriber));
					ContacterManager.contacters.put(subscriber,
							ContacterManager.getUserByRosterEntry(XmppConnectionManager.getInstance().getConnection(),
									entry, roster));
					sendBroadcast(intent);
				}
			}
			// �յ������������
			else */ if (presence.getType().equals(Presence.Type.subscribe)) {
				Log.d(TAG, "receive subscribe packet from " + packet.getFrom());

				userAddFriendRequest(packet);
			}
			// �յ�����ȷ�������Ӧ
			else if (presence.getType().equals(Presence.Type.subscribed)) {
				Log.d(TAG, "receive subscribed packet from " + packet.getFrom());

				//RosterEntry entry = XmppConnectionManager.getInstance().getConnection().getRoster().getEntry(packet.getFrom());

				//Log.d(TAG, "receive a new roster subscribed message for " + packet.getFrom() + 
				//		", item status is " + entry.getStatus().toString() +
				//		", item type is " + entry.getType().toString());

				userAddFriendConfirm(packet);
			}
			// �յ�����ɾ������
			else if (presence.getType().equals(Presence.Type.unsubscribe)) {
				Log.d(TAG, "receive unsubscribe packet from " + packet.getFrom());
				
				// TODO: �յ��Է�ɾ���Լ�����Ϣ���Լ�Ҳɾ����֮�йص�֪ͨ�� IM ��Ϣ���Ƿ����
				NoticeManager.getInstance().delNoticeByWith(packet.getFrom());
				MessageManager.getInstance().delChatHisByName(packet.getFrom());
			}

		}
	};
	
	
	private void userAddFriendConfirm(Packet packet) {
		NoticeManager noticeManager = NoticeManager
				.getInstance();
		
		List<Notice> notices = noticeManager.getNoticeByWith(packet.getFrom());
		if (notices == null || notices.size() == 0 ) {
			// �յ����ѵ� subscribed ��Ϣ������֮ǰû����ӹ��ú��ѣ�����
			Log.e(TAG, "you have not added " + packet.getFrom() + " to your friend list");
			return;
		}
		else if (notices.size() == 1) {
			Notice n = notices.get(0);
			Intent intent = new Intent();
			intent.setAction(Constant.ROSTER_SUBSCRIPTION);
			intent.putExtra(Notice.noticeKey, n);

			// �յ��Է��� subscribed ��Ӧ��˵��������������������������ô֪ͨ��Ϣ�����Ͳ������� STATUS_ADD_REQUEST
			if(n.getStatus().equals(Notice.STATUS_ADD_REQUEST)) {
				Log.e(TAG, "receive add confirm from " + packet.getFrom() + ", but the notice status is STATUS_ADD_REQUEST");

			}
			// �������ӳɹ���� subscribed�����账��
			else if (n.getStatus().equals(Notice.STATUS_COMPLETE)) {
				Log.d(TAG, "receive subscribed from " + packet.getFrom() + ", who has been your friend.");

			}
			// �������̣�������Ӻ�������󣬳ɹ��յ��Է���ͬ����Ӧ
			else {
				Log.d(TAG, "receive subscribed from " + packet.getFrom() + " successfully");

				noticeManager.updateStatusById(notices.get(0).getId(), Notice.STATUS_COMPLETE);
				noticeManager.updateDispStatusById(notices.get(0).getId(), Notice.DISPLAY);
				noticeManager.updateTimeById(notices.get(0).getId(), DateUtil.getCurDateStr());
				
				ContacterManager.loadAndUpdateContacter(n.getWith());

				sendBroadcast(intent);
			}
			
		}
		else {
			// ͬһ�����Ѳ�Ӧ�����ظ����������
			Log.e(TAG, "the notice count from user " + packet.getFrom() + " is more than one!");
			return;
		}			
	}
	

	private void userAddFriendRequest(Packet packet) {

		NoticeManager noticeManager = NoticeManager
				.getInstance();
		
		List<Notice> notices = null;
		
		notices = noticeManager.getNoticeByWith(packet.getFrom());
		
		if (notices == null || notices.size() == 0 ) {
			// ������µ�������������µ�֪ͨ��Ϣ
			Notice notice = new Notice();
			notice.setReadStatus(Notice.UNREAD);
			notice.setWith(packet.getFrom());
			notice.setStatus(Notice.STATUS_ADD_REQUEST);
			notice.setDispStatus(Notice.DISPLAY);
			notice.setTime(DateUtil.getCurDateStr());

			User user = ContacterManager.getUserByName(XmppConnectionManager.getInstance().getConnection(), 
					StringUtil.getUserNameByJid(packet.getFrom()));
			
			notice.setContent(user.getNickName());
			
			if (user.getHeadImg() != null) {
				notice.setAvatar(user.getHeadImg());
			} else {
				if (user.getGender() == null) {
					notice.setAvatar(BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar_male));
					
				} else {
					if (user.getGender().equals(User.MALE)) {
						notice.setAvatar(BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar_male));
					}
					else {
						notice.setAvatar(BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar_female));
					}
				}
			}
			
			long noticeId = noticeManager.saveNotice(notice);
			if (noticeId != -1) {
				Intent intent = new Intent();
				intent.setAction(Constant.ROSTER_SUBSCRIPTION);
				notice.setId(String.valueOf(noticeId));
				intent.putExtra(Notice.noticeKey, notice);
				sendBroadcast(intent);
				
				NoticeManager.getInstance().dispatchRosterMessageNotify(user);
			}
		}
		else if (notices.size() == 1) {
			Notice n = notices.get(0);
			Intent intent = new Intent();
			intent.setAction(Constant.ROSTER_SUBSCRIPTION);
			intent.putExtra(Notice.noticeKey, n);
			
			// ������ظ���������󣬸���ԭ֪ͨ��Ϣ��ʱ����Ѷ�״̬
			// ������������ϲ��÷�������Ϊ������ӵ�һ���Ѿ��������ƣ��������ظ���Ӻ���
			if(n.getStatus().equals(Notice.STATUS_ADD_REQUEST)) {
				Log.e(TAG, "recv more add friend requests from " + packet.getFrom());

				noticeManager.updateTimeById(notices.get(0).getId(), DateUtil.getCurDateStr());
				noticeManager.updateReadStatusById(notices.get(0).getId(), Notice.UNREAD);
				sendBroadcast(intent);
			}
			// ����� subscribed ֮��� subscribe����ʾ˫����ӣ����账��
			else if (n.getStatus().equals(Notice.STATUS_COMPLETE)) {
				Log.e(TAG, "recv more add friend request from " + packet.getFrom());
				
				ContacterManager.sendSubscribe(Presence.Type.subscribed, n.getWith());
				noticeManager.updateTimeById(notices.get(0).getId(), DateUtil.getCurDateStr());
			}
			// ���˫��ͬʱ��Է�������Ӻ������󣬸��Զ��������շ���ȥ�������������յ��Է����������������������Ϊ�Է�ͬ�����������
			else {
				Log.e(TAG, "recv add friend request from " + packet.getFrom() + " when waitting for the confirm");

				noticeManager.updateStatusById(notices.get(0).getId(), Notice.STATUS_COMPLETE);
				noticeManager.updateDispStatusById(notices.get(0).getId(), Notice.DISPLAY);
				noticeManager.updateTimeById(notices.get(0).getId(), DateUtil.getCurDateStr());
				ContacterManager.loadAndUpdateContacter(n.getWith());

				sendBroadcast(intent);
			}
		}
		else {
			// ͬһ�����Ѳ�Ӧ�����ظ����������
			Log.e(TAG, "the notice count from user " + packet.getFrom() + " is more than one!");
			return;
		}			
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		unregisterReceiver(reConnectionBroadcastReceiver);
		try {
			unInitRoster();
		} catch (RuntimeException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		super.onDestroy();
	}

	private RosterListener rosterListener = new RosterListener() {

		@Override
		public void presenceChanged(Presence presence) {
			Intent intent = new Intent();
			intent.setAction(Constant.ROSTER_PRESENCE_CHANGED);
			String subscriber = presence.getFrom().substring(0,
					presence.getFrom().indexOf("/"));
			RosterEntry entry = XmppConnectionManager.getInstance().getRoster().getEntry(subscriber);
			Log.d(TAG, "presenceChanged: " + subscriber + ", status: " + presence.getType());

			if (ContacterManager.contacters.containsKey(subscriber)) {
				// ��״̬�ı�֮ǰ��user�㲥��ȥ
				User user = ContacterManager.getUserByRosterEntry(XmppConnectionManager.getInstance().getConnection(),
						entry, XmppConnectionManager.getInstance().getRoster());

				intent.putExtra(User.userKey, user);
				
				if(presence.getType().equals(Presence.Type.unavailable)) {
					Log.w(TAG, "user " + user.getName() + " offline, remove the aukey attached flag");
					user.setSecurity(AUKeyManager.DETACHED);
					intent.putExtra(User.userPresenceKey, User.OFFLINE);
				}
				else {
					intent.putExtra(User.userPresenceKey, User.ONLINE);
				}
				
				ContacterManager.contacters.put(subscriber, user);
				sendBroadcast(intent);
			}
		}

		@Override
		public void entriesUpdated(Collection<String> addresses) {
			for (String address : addresses) {
				Log.d(TAG, "entriesUpdated: " + address);

				
				Intent intent = new Intent();
				intent.setAction(Constant.ROSTER_UPDATED);
				// ���״̬�ı��entry
				RosterEntry userEntry = XmppConnectionManager.getInstance().getRoster().getEntry(address);
				
				User user = ContacterManager
						.getUserByRosterEntry(XmppConnectionManager.getInstance().getConnection(),
								userEntry, XmppConnectionManager.getInstance().getRoster());
				intent.putExtra(User.userKey, user);

				if (ContacterManager.contacters.containsKey(address)) {
					ContacterManager.contacters.put(address, user);
					sendBroadcast(intent);
				}
			}
		}

		@Override
		public void entriesDeleted(Collection<String> addresses) {
			for (String address : addresses) {
				Log.d(TAG, "entriesDeleted: " + address);

				Intent intent = new Intent();
				intent.setAction(Constant.ROSTER_DELETED);
				User user = null;
				if (ContacterManager.contacters.containsKey(address)) {
					user = ContacterManager.contacters.get(address);
					ContacterManager.contacters.remove(address);
					intent.putExtra(User.userKey, user);
					sendBroadcast(intent);
				}
			}
		}

		@Override
		public void entriesAdded(Collection<String> addresses) {
			for (String address : addresses) {
				Log.d(TAG, "entriesAdded: " + address);

			
				/*
				Intent intent = new Intent();
				intent.setAction(Constant.ROSTER_ADDED);
				RosterEntry userEntry = roster.getEntry(address);
				User user = ContacterManager
						.getUserByRosterEntry(XmppConnectionManager.getInstance().getConnection(),
								userEntry, roster);
				ContacterManager.contacters.put(address, user);
				intent.putExtra(User.userKey, user);
				sendBroadcast(intent);
				
				*/
			}
		}
	};

}
