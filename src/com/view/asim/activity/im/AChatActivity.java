package com.view.asim.activity.im;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipManager;
import com.view.asim.comm.Constant;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.Attachment;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.GroupUser;
import com.view.asim.model.IMMessage;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.util.DateUtil;
import com.view.asim.worker.MessageSentResultListener;
import com.view.asim.worker.UISendVideoHandler;
import com.view.asim.worker.Worker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * 聊天对话.
 * 
 * @author xuweinan
 */
public abstract class AChatActivity extends ActivitySupport {
	private static final String TAG = "AChatActivity";

	private Map<String, ChatMessage> mMessagePool = null;
	protected GroupUser mGroup = null; 
	protected User mUser = null;
	private static int pageSize = 500;
	private ChatMessage mInitialMsg = null;
	protected String mChatType = null;
	protected Worker mWorker = null; 
    protected ISipService service;
    protected ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ISipService.Stub.asInterface(arg1);
            /*
             * timings.addSplit("Service connected"); if(configurationService !=
             * null) { timings.dumpToLog(); }
             */
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
        }
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mChatType = getIntent().getStringExtra(IMMessage.PROP_CHATTYPE);
		Log.d(TAG, "chat type is " + mChatType);
		
		if (mChatType == null || mChatType.equals(IMMessage.SINGLE)) {
			mChatType = IMMessage.SINGLE;
			mUser = getIntent().getParcelableExtra(User.userKey);
			if (mUser == null)
				throw new RuntimeException("user is null at chat activity");
			Log.i(TAG, "create chat view for user:" + mUser);
			
		}
		else {
			mGroup = getIntent().getParcelableExtra(GroupUser.groupUserKey);
			if (mGroup == null)
				throw new RuntimeException("group user is null at chat activity");
			
		}
		
		// 从别的界面带过来的消息（如创建群组后的第一条消息、加好友成功后的第一条消息、转发的消息等），直接发出去
		mInitialMsg = getIntent().getParcelableExtra(ChatMessage.IMMESSAGE_KEY);
		if(mInitialMsg != null) {
			try {
				if (mInitialMsg.getType().equals(ChatMessage.CHAT_TEXT)) {
					sendMessage(mInitialMsg);
				} else {
					sendFile(mInitialMsg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		mWorker = new Worker();
		mWorker.initilize("Chat Activity Worker");

		mMessagePool = new HashMap<String, ChatMessage>();
		/*
		chat = XmppConnectionManager.getInstance().getConnection()
				.getChatManager().createChat(mUser.getJID(), null);
		*/
		
        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        serviceIntent.setPackage(getPackageName());
        bindService(serviceIntent, connection,
                Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.NEW_MESSAGE_ACTION);
		filter.addAction(Constant.SEND_FILE_RESULT_ACTION);
		filter.addAction(Constant.FILE_PROGRESS_ACTION);
		filter.addAction(Constant.SEND_MESSAGE_RESULT_ACTION);
		filter.addAction(Constant.NEW_CTRL_MESSAGE_ACTION);
		filter.addAction(Constant.AUKEY_STATUS_UPDATE);
		filter.addAction(Constant.ACTION_RECONNECT_STATE);

		registerReceiver(receiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mWorker.destroy();
		unbindService(connection);
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (Constant.AUKEY_STATUS_UPDATE.equals(action)) {
				refreshMessage();
			}
			else
			if (Constant.NEW_MESSAGE_ACTION.equals(action)) {

				ChatMessage message = intent
						.getParcelableExtra(ChatMessage.IMMESSAGE_KEY);
				if (message != null) {
					mMessagePool.remove(message.getId());
					mMessagePool.put(message.getId(), message);
				}
				receiveNewMessage(message);
				refreshMessage();
			}
			else
			if (Constant.SEND_FILE_RESULT_ACTION.equals(action)) {
				ChatMessage message = intent
						.getParcelableExtra(Constant.SEND_FILE_KEY_MESSAGE);
				mMessagePool.remove(message.getId());
				mMessagePool.put(message.getId(), message);
				sendFileResult(message);
				refreshMessage();
			}
			else
			if (Constant.FILE_PROGRESS_ACTION.equals(action)) {
				ChatMessage message = intent
						.getParcelableExtra(Constant.SEND_FILE_KEY_MESSAGE);
				int ratio = intent.getIntExtra(Constant.SEND_FILE_KEY_PROGRESS, 100);
				
				sendFileProgressUpdate(message, ratio);
			}
			else
			if (Constant.SEND_MESSAGE_RESULT_ACTION.equals(action)) {
				ChatMessage message = intent
						.getParcelableExtra(Constant.SEND_MESSAGE_KEY_MESSAGE);
				mMessagePool.remove(message.getId());
				mMessagePool.put(message.getId(), message);
				sendMessageResult(message);
				refreshMessage();
			}
			else if (Constant.ACTION_RECONNECT_STATE.equals(action)) {
				String status = intent.getStringExtra(Constant.RECONNECT_STATE);
				handReConnect(status);
			}
		}

	};

	protected abstract void handReConnect(String status);
	protected abstract void receiveNewMessage(ChatMessage message);
	protected abstract void sendMessageResult(ChatMessage message);
	protected abstract void sendFileResult(ChatMessage message);
	protected abstract void sendFileProgressUpdate(ChatMessage message, int ratio);

	protected void refreshMessage() {
		Log.d(TAG, "refresh msg pool start on: " + DateUtil.getCurDateStr());

		List<ChatMessage> messages = MessageManager.getInstance()
				.getMessageListByName(getWith(), 1, pageSize);
		mMessagePool.clear();
		for (ChatMessage msg: messages) {
			
			/* 清除收到的错误消息 */
			if (msg.getDir().equals(IMMessage.RECV)) {
				if (msg.getStatus().equals(IMMessage.ERROR) || 
				   (msg.isMultiMediaMessage() && msg.getAttachment() == null)) {
					Log.w(TAG, "message invalid in DB:" + msg);
					MessageManager.getInstance().delChatHisById(msg.getId());
					continue;
				}
			}
			
			mMessagePool.put(msg.getId(), msg);
		}
		Log.d(TAG, "refresh msg pool end on: " + DateUtil.getCurDateStr());

	}
	

	protected List<ChatMessage> getMessages() {
		boolean needEncr = AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED);

		if (mMessagePool == null)
			throw new RuntimeException("message pool is null");

		List<ChatMessage> msgList = new ArrayList<ChatMessage>();

		for (String key : mMessagePool.keySet()) {
			ChatMessage m = mMessagePool.get(key);
			
			/* 当前是明信模式，密信不显示 */
			if (!needEncr && m.getSecurity().equals(IMMessage.ENCRYPTION)) {
				continue;
			}
			
			msgList.add(m);
		}
		Collections.sort(msgList);
		return msgList;
	}


	protected void sendMessage(String messageContent, String destroy) throws Exception {
		boolean needEncr = AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED);

		String time = DateUtil.getCurDateStr();
		
		ChatMessage newMessage = new ChatMessage();
		newMessage.setDir(IMMessage.SEND);
		newMessage.setFrom(ContacterManager.userMe.getJID());
		newMessage.setType(ChatMessage.CHAT_TEXT);
		newMessage.setDestroy(destroy);
		newMessage.setContent(messageContent);
		newMessage.setTime(time);
		newMessage.setSecurity(needEncr ? IMMessage.ENCRYPTION: IMMessage.PLAIN);
		newMessage.setChatType(mChatType);
		newMessage.setWith(getWith());
		newMessage.setUniqueId(String.valueOf(newMessage.hashCode()));
		
		Log.d(TAG, "send text msg to " + newMessage.getWith() + ", " + messageContent + ", code:" + newMessage.getUniqueId());

		long msgId = MessageManager.getInstance().saveIMMessage(newMessage);
		newMessage.setId("" + msgId);
		
		MessageManager.getInstance().updateReadStatus(msgId, IMMessage.READ);
		
		mMessagePool.put(newMessage.getId(), newMessage);

		Intent intent = new Intent();
		intent.setAction(Constant.SEND_MESSAGE_ACTION);
		intent.putExtra(Constant.SEND_MESSAGE_KEY_MESSAGE, newMessage);
		sendBroadcast(intent);
		
		// 刷新视图
		refreshMessage();

	}
	
	protected void sendMessage(ChatMessage msg) throws Exception {

		msg.setStatus(IMMessage.INPROGRESS);
		long msgId = MessageManager.getInstance().saveIMMessage(msg);
		msg.setId("" + msgId);
		
		mMessagePool.put(msg.getId(), msg);

		Intent intent = new Intent(Constant.SEND_MESSAGE_ACTION);
		intent.putExtra(Constant.SEND_MESSAGE_KEY_MESSAGE, msg);
		sendBroadcast(intent);
		
		// 刷新视图
		refreshMessage();

	}
	
	protected String getMediaAbsolutePath(Uri uri) {
		String name = null;
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(uri, null, null, null, null);
		

		if (cursor != null) {
			cursor.moveToFirst();
			// Column 2 = _display_name
			int columnIdx = cursor.getColumnIndex("_data");
			name = cursor.getString(columnIdx);
			for(int i = 0; i < cursor.getColumnCount(); i++) {
				Log.d(TAG, "cur " + i + ", " + cursor.getColumnName(i) + ", " + cursor.getString(i));
			}
		}
		else {
			name = uri.getPath();
		}
		
		Log.d(TAG, "uri path=" + uri.getPath() + ", absolute path=" + name);
		
		return name;
	}
	
	protected boolean judgeMediaFileSize(Uri uri) {
		String absolutePath = getMediaAbsolutePath(uri);
		File mediaFile = new File(absolutePath);
        if (mediaFile.length() > Constant.SEND_MEDIA_FILE_SIZE_LIMIT) {
            Log.e(TAG, "send media file " +  absolutePath + ", size " + mediaFile.length() + " out of limit.");
            showToast("只支持发送不超过 10M 大小的媒体文件");
        	return false;
        }
		
        return true;
	}
	
	protected void sendImage(Uri imgUri, String destroy) throws Exception {
		String absoluteUri = getMediaAbsolutePath(imgUri);
		
		boolean needEncr = AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED);

		String time = DateUtil.getCurDateStr();
		
		ChatMessage newMessage = new ChatMessage();
		newMessage.setDir(IMMessage.SEND);
		newMessage.setFrom(ContacterManager.userMe.getJID());
		newMessage.setType(ChatMessage.CHAT_IMAGE);
		newMessage.setDestroy(destroy);
		newMessage.setContent("你发了一张图片");
		newMessage.setTime(time);
		newMessage.setSecurity(needEncr ? IMMessage.ENCRYPTION: IMMessage.PLAIN);

		newMessage.setChatType(mChatType);
		newMessage.setWith(getWith());

		Attachment att = new Attachment();
		att.setSrcUri(absoluteUri);
		newMessage.setAttachment(att);
		
		newMessage.setUniqueId(String.valueOf(newMessage.hashCode()));

		long msgId = MessageManager.getInstance().saveIMMessage(newMessage);
		newMessage.setId("" + msgId);
		
		mMessagePool.put(newMessage.getId(), newMessage);

		Intent intent = new Intent(Constant.SEND_FILE_ACTION);
		intent.putExtra(Constant.SEND_FILE_KEY_MESSAGE, newMessage);
		sendBroadcast(intent);
		
		// 刷新视图
		refreshMessage();

	}
		
	protected void sendVideo(Uri videoUri, String destroy) throws Exception {
		String absoluteUri = getMediaAbsolutePath(videoUri);

		UISendVideoHandler handler = new UISendVideoHandler(getWith(), absoluteUri, destroy, mChatType, 
				new MessageSentResultListener() {

					@Override
					public void onSentResult(ChatMessage msgSent) {
						long msgId = MessageManager.getInstance().saveIMMessage(msgSent);
						msgSent.setId("" + msgId);
						
						mMessagePool.put(msgSent.getId(), msgSent);
						Intent intent = new Intent(Constant.SEND_FILE_ACTION);
						intent.putExtra(Constant.SEND_FILE_KEY_MESSAGE, msgSent);
						sendBroadcast(intent);
						
						runOnUiThread(new Runnable()    
				        {    
				            public void run()    
				            {    
								// 刷新视图
								refreshMessage();
				            }    
				    
				        });
					}
			
		});
		
		mWorker.addHandler(handler);
	}
	
	// 发送文件（语音）
	protected void sendFile(Uri fileUri, int duration, String destroy) throws Exception {
		String absoluteUri = getMediaAbsolutePath(fileUri);

		boolean needEncr = AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED);

		String time = DateUtil.getCurDateStr();
		
		ChatMessage newMessage = new ChatMessage();
		newMessage.setDir(IMMessage.SEND);
		newMessage.setFrom(ContacterManager.userMe.getJID());
		newMessage.setType(ChatMessage.CHAT_AUDIO);
		newMessage.setDestroy(destroy);
		newMessage.setContent("你发了一段语音");
		newMessage.setTime(time);
		newMessage.setSecurity(needEncr ? IMMessage.ENCRYPTION: IMMessage.PLAIN);

		newMessage.setChatType(mChatType);
		newMessage.setWith(getWith());
		
		Attachment att = new Attachment();
		att.setSrcUri(absoluteUri);
		att.setAudioLength(duration);
		newMessage.setAttachment(att);
		newMessage.setUniqueId(String.valueOf(newMessage.hashCode()));

		long msgId = MessageManager.getInstance().saveIMMessage(newMessage);
		newMessage.setId("" + msgId);
		
		mMessagePool.put(newMessage.getId(), newMessage);

		Intent intent = new Intent(Constant.SEND_FILE_ACTION);
		intent.putExtra(Constant.SEND_FILE_KEY_MESSAGE, newMessage);
		sendBroadcast(intent);
		
		// 刷新视图
		refreshMessage();

	}
	
	// 发送文件（其他）
	protected void sendFile(Uri fileUri, String destroy) throws Exception {
		String absoluteUri = getMediaAbsolutePath(fileUri);

		boolean needEncr = AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED);

		String time = DateUtil.getCurDateStr();
		
		ChatMessage newMessage = new ChatMessage();
		newMessage.setDir(IMMessage.SEND);
		newMessage.setFrom(ContacterManager.userMe.getJID());
		newMessage.setType(ChatMessage.CHAT_FILE);
		newMessage.setDestroy(destroy);
		newMessage.setContent("你发了一个文件");
		newMessage.setTime(time);
		newMessage.setSecurity(needEncr ? IMMessage.ENCRYPTION: IMMessage.PLAIN);

		newMessage.setChatType(mChatType);
		newMessage.setWith(getWith());
		
		Attachment att = new Attachment();
		att.setSrcUri(absoluteUri);
		newMessage.setAttachment(att);
		
		long msgId = MessageManager.getInstance().saveIMMessage(newMessage);
		newMessage.setId("" + msgId);
		
		mMessagePool.put(newMessage.getId(), newMessage);

		Intent intent = new Intent(Constant.SEND_FILE_ACTION);
		intent.putExtra(Constant.SEND_FILE_KEY_MESSAGE, newMessage);
		sendBroadcast(intent);
		
		// 刷新视图
		refreshMessage();

	}
	
	protected void sendFile(ChatMessage msg) throws Exception {

		long msgId = MessageManager.getInstance().saveIMMessage(msg);
		msg.setId("" + msgId);
		
		mMessagePool.put(msg.getId(), msg);

		Intent intent = new Intent(Constant.SEND_FILE_ACTION);
		intent.putExtra(Constant.SEND_FILE_KEY_MESSAGE, msg);
		sendBroadcast(intent);
		
		// 刷新视图
		refreshMessage();

	}

	/**
	 * 下滑加载信息,true 返回成功，false 数据已经全部加载，全部查完了，
	 * 
	 * @param message
	protected Boolean addNewMessage() {
		List<ChatMessage> newMsgList = MessageManager.getInstance(context)
				.getMessageListByName(mUser.getJID(), message_pool.size(), pageSize);
		if (newMsgList != null && newMsgList.size() > 0) {
			message_pool.addAll(newMsgList);
			Collections.sort(message_pool);
			return true;
		}
		return false;
	}
	
	protected void resh() {
		// 刷新视图
		refreshMessage();
	}
	*/
	protected String getWith() {
		if (mChatType.equals(IMMessage.SINGLE)) {
			return mUser.getJID();
		}
		else {
			return mGroup.getName();
		}
	}
}
