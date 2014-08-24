package com.view.asim.activity.im;


import java.util.List;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;

import com.view.asim.activity.ActivitySupport;
import com.view.asim.activity.sip.UserCallLogsActivity;
import com.view.asim.comm.Constant;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.GroupUser;
import com.view.asim.model.IMMessage;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.utils.StringUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * ��ϵ���б�.
 * 
 * @author xuweinan
 */
public abstract class AContacterActivity extends ActivitySupport {
	private static final String TAG = "AContacterActivity";

	private ContacterReceiver receiver = null;
	protected int noticeNum = 0;// ֪ͨ������δ����Ϣ����

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	private void init() {
		receiver = new ContacterReceiver();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();

		filter.addAction(Constant.ROSTER_ADDED);
		filter.addAction(Constant.ROSTER_DELETED);
		filter.addAction(Constant.ROSTER_PRESENCE_CHANGED);
		filter.addAction(Constant.ROSTER_UPDATED);
		filter.addAction(Constant.ROSTER_SUBSCRIPTION);
		// ��������
		filter.addAction(Constant.NEW_MESSAGE_ACTION);
		filter.addAction(Constant.ACTION_SYS_MSG);

		filter.addAction(Constant.ACTION_RECONNECT_STATE);
		filter.addAction(Constant.AUKEY_STATUS_UPDATE);

		registerReceiver(receiver, filter);
		super.onResume();
	}

	private class ContacterReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			User user = null;
			Notice notice = null;

			if (Constant.AUKEY_STATUS_UPDATE.equals(action)) {
				refreshList();
			}
			else if (Constant.ROSTER_ADDED.equals(action)) {
				user = intent.getParcelableExtra(User.userKey);
				addUserReceive(user);
			}

			else if (Constant.ROSTER_DELETED.equals(action)) {
				user = intent.getParcelableExtra(User.userKey);
				deleteUserReceive(user);
			}

			else if (Constant.ROSTER_PRESENCE_CHANGED.equals(action)) {
				if(MessageManager.getInstance().isInit()){
					user = intent.getParcelableExtra(User.userKey);
					changePresenceReceive(user);
				}
			}

			else if (Constant.ROSTER_UPDATED.equals(action)) {
				user = intent.getParcelableExtra(User.userKey);
				updateUserReceive(user);
			}

			else if (Constant.ROSTER_SUBSCRIPTION.equals(action)) {
				notice = intent.getParcelableExtra(Notice.noticeKey);
				subscripUserReceive(notice);
			} 
			
			else if (Constant.NEW_MESSAGE_ACTION.equals(action)) {				
				ChatMessage message = intent
						.getParcelableExtra(ChatMessage.IMMESSAGE_KEY);				
				msgReceive(message);
			} 
			
			else if (Constant.ACTION_RECONNECT_STATE.equals(action)) {
				String status = intent.getStringExtra(
						Constant.RECONNECT_STATE);
				handReConnect(status);
			}

		}
	}
	
	protected abstract void refreshList();


	/**
	 * roster�����һ��subcriber
	 * 
	 * @param user
	 */
	protected abstract void addUserReceive(User user);

	/**
	 * rosterɾ����һ��subscriber
	 * 
	 * @param user
	 */
	protected abstract void deleteUserReceive(User user);

	/**
	 * roster�е�һ��subscriber��״̬��Ϣ��Ϣ�����˸ı�
	 * 
	 * @param user
	 */
	protected abstract void changePresenceReceive(User user);

	/**
	 * roster�е�һ��subscriber��Ϣ������
	 * 
	 * @param user
	 */
	protected abstract void updateUserReceive(User user);

	/**
	 * �յ�һ�������������
	 * 
	 * @param subFrom
	 */
	protected abstract void subscripUserReceive(Notice notice);

	/**
	 * ������Ϣ����
	 * 
	 * @param user
	 */
	protected abstract void msgReceive(IMMessage msg);



	/**
	 * �޸�������ѵ��ǳ�
	 * 
	 * @param user
	 * @param nickname
	protected void setNickname(User user, String nickname) {

		ContacterManager.setNickname(user, nickname, XmppConnectionManager
				.getInstance().getConnection());
	}
	*/

	/**
	 * ��һ��������ӵ�һ������ ���Ƴ���ǰ���飬Ȼ����ӵ��·���
	 * 
	 * @param user
	 * @param groupName
	protected void addUserToGroup(final User user, final String groupName) {

		if (null == user) {
			return;
		}
		if (StringUtil.notEmpty(groupName) && Constant.ALL_FRIEND != groupName
				&& Constant.NO_GROUP_FRIEND != groupName) {
			ContacterManager.addUserToGroup(user, groupName,
					XmppConnectionManager.getInstance().getConnection());
		}
	}
	 */

	/**
	 * ��һ�����Ѵ�����ɾ��
	 * 
	 * @param user
	 * @param groupName
	protected void removeUserFromGroup(User user, String groupName) {

		if (null == user) {
			return;
		}
		if (StringUtil.notEmpty(groupName)
				&& !Constant.ALL_FRIEND.equals(groupName)
				&& !Constant.NO_GROUP_FRIEND.equals(groupName))
			ContacterManager.removeUserFromGroup(user, groupName,
					XmppConnectionManager.getInstance().getConnection());

	}
	 */



	/**
	 * �޸�һ���������
	 * 
	 * @param groupName
	protected void updateGroupName(String oldGroupName, String newGroupName) {
		XmppConnectionManager.getInstance().getConnection().getRoster()
				.getGroup(oldGroupName).setName(newGroupName);
	}
	 */

	/**
	 * 
	 * ����ӷ���.
	 * 
	 * @param newGroupName
	 * @author shimiso
	 * @update 2012-6-28 ����3:52:41
	protected void addGroup(String newGroupName) {
		ContacterManager.addGroup(newGroupName, XmppConnectionManager
				.getInstance().getConnection());

	}
	 */

	/**
	 * ����һ��һ��һ����
	 * 
	 * @param user
	 */
	protected void createChat(String with) {
		if (ContacterManager.contacters.containsKey(with)) {
			Intent intent = new Intent(context, ChatActivity.class);
			intent.putExtra(IMMessage.PROP_CHATTYPE, IMMessage.SINGLE);
			intent.putExtra(User.userKey, ContacterManager.contacters.get(with));
			startActivity(intent);
		}
		else if (ContacterManager.groupUsers.containsKey(with)) {
			Intent intent = new Intent(context, ChatActivity.class);
			intent.putExtra(IMMessage.PROP_CHATTYPE, IMMessage.GROUP);
			intent.putExtra(User.userKey, ContacterManager.groupUsers.get(with));
			startActivity(intent);
		}
		else {
			Log.e(TAG, "create chat with unknow name " + with);
		}
	}

	
	/**
	 * ��ʾһ�����ѵ�����ͨ����¼
	 * 
	 * @param user
	 */
	protected void showUserCallLogs(String with) {
		if (ContacterManager.contacters.containsKey(with)) {
			Intent intent = new Intent(context, UserCallLogsActivity.class);
			intent.putExtra(User.userKey, ContacterManager.contacters.get(with));
			startActivity(intent);
		}
		else {
			Log.e(TAG, "create chat with unknow name " + with);
		}
	}
	
	/**
	 * �����ӷ���
	 * 
	 * @param isSuccess
	 */
	protected abstract void handReConnect(String status);

	/**
	 * �ж��û����Ƿ����
	 * 
	 * @param userName
	 * @param groups
	 * @return
	protected boolean isExitJid(String userJid, List<MRosterGroup> groups) {
		for (MRosterGroup g : groups) {
			List<User> users = g.getUsers();
			if (users != null && users.size() > 0) {
				for (User u : users) {
					if (u.getJID().equals(userJid)) {
						return true;
					}
				}
			}
		}

		return false;
	}
		 */

}
