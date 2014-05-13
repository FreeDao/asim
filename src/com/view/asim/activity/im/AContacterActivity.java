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

import com.view.asim.comm.Constant;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.GroupUser;
import com.view.asim.model.IMMessage;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.util.StringUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * 联系人列表.
 * 
 * @author xuweinan
 */
public abstract class AContacterActivity extends ActivitySupport {
	private static final String TAG = "AContacterActivity";

	private ContacterReceiver receiver = null;
	protected int noticeNum = 0;// 通知数量，未读消息数量

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
		// 好友请求
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
				user = intent.getParcelableExtra(User.userKey);
				changePresenceReceive(user);
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
				boolean isSuccess = intent.getBooleanExtra(
						Constant.RECONNECT_STATE, true);
				handReConnect(isSuccess);
			}

		}
	}
	
	protected abstract void refreshList();


	/**
	 * roster添加了一个subcriber
	 * 
	 * @param user
	 */
	protected abstract void addUserReceive(User user);

	/**
	 * roster删除了一个subscriber
	 * 
	 * @param user
	 */
	protected abstract void deleteUserReceive(User user);

	/**
	 * roster中的一个subscriber的状态信息信息发生了改变
	 * 
	 * @param user
	 */
	protected abstract void changePresenceReceive(User user);

	/**
	 * roster中的一个subscriber信息更新了
	 * 
	 * @param user
	 */
	protected abstract void updateUserReceive(User user);

	/**
	 * 收到一个好友添加请求
	 * 
	 * @param subFrom
	 */
	protected abstract void subscripUserReceive(Notice notice);

	/**
	 * 有新消息进来
	 * 
	 * @param user
	 */
	protected abstract void msgReceive(IMMessage msg);



	/**
	 * 修改这个好友的昵称
	 * 
	 * @param user
	 * @param nickname
	protected void setNickname(User user, String nickname) {

		ContacterManager.setNickname(user, nickname, XmppConnectionManager
				.getInstance().getConnection());
	}
	*/

	/**
	 * 把一个好友添加到一个组中 先移除当前分组，然后添加到新分组
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
	 * 把一个好友从组中删除
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
	 * 修改一个组的组名
	 * 
	 * @param groupName
	protected void updateGroupName(String oldGroupName, String newGroupName) {
		XmppConnectionManager.getInstance().getConnection().getRoster()
				.getGroup(oldGroupName).setName(newGroupName);
	}
	 */

	/**
	 * 
	 * 这添加分组.
	 * 
	 * @param newGroupName
	 * @author shimiso
	 * @update 2012-6-28 下午3:52:41
	protected void addGroup(String newGroupName) {
		ContacterManager.addGroup(newGroupName, XmppConnectionManager
				.getInstance().getConnection());

	}
	 */

	/**
	 * 创建一个一对一聊天
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
	 * 冲连接返回
	 * 
	 * @param isSuccess
	 */
	protected abstract void handReConnect(boolean isSuccess);

	/**
	 * 判断用户名是否存在
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
