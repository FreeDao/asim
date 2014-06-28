package com.view.asim.service;

import java.io.File;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.OfflineMessageManager;

import com.view.asim.comm.Constant;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.activity.im.ChatActivity;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.CtrlMessage;
import com.view.asim.model.GroupUser;
import com.view.asim.model.IMMessage;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.util.DateUtil;
import com.view.asim.util.StringUtil;
import com.view.asim.worker.BaseHandler;
import com.view.asim.worker.GroupInfoUpdateHandler;
import com.view.asim.worker.InviteGroupChatHandler;
import com.view.asim.worker.MessageRecvResultListener;
import com.view.asim.worker.MessageSentResultListener;
import com.view.asim.worker.FileSentResultListener;
import com.view.asim.worker.OfflineMessageRecvResultListener;
import com.view.asim.worker.QuitGroupChatHandler;
import com.view.asim.worker.RecvFileMsgHandler;
import com.view.asim.worker.RecvOfflineMsgHandler;
import com.view.asim.worker.RecvTextMsgHandler;
import com.view.asim.worker.SendGroupTextMsgHandler;
import com.view.asim.worker.SendTextMsgHandler;
import com.view.asim.worker.SendGroupFileMsgHandler;
import com.view.asim.worker.SendFileMsgHandler;
import com.view.asim.worker.RemoteDestroyMessageHandler;

import com.view.asim.worker.Worker;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import com.view.asim.R;

/**
 * 
 * 聊天服务.
 * 
 * @author xuweinan
 */
public class IMChatService extends Service {
	private static final String TAG = "IMChatService";
	private Context context;
	private ChatReceiver receiver = null;

	private Worker mRecvMsgWorker = null;
	private Worker mSendMsgWorker = null;
	

	@Override
	public void onCreate() {
		Log.d(TAG, "service create");
		context = this;
		super.onCreate();
		mRecvMsgWorker = new Worker();
		mRecvMsgWorker.initilize("IM Receive Message Worker");
		
		mSendMsgWorker = new Worker();
		mSendMsgWorker.initilize("IM Send Message Worker");
		
		//initChatManager();
		initBroadcastReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;

		//return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mSendMsgWorker.destroy();
		mRecvMsgWorker.destroy();
		unregisterReceiver(receiver);
	}
	
	private void initBroadcastReceiver() {
		receiver = new ChatReceiver();
		
		IntentFilter filter = new IntentFilter();

		filter.addAction(Constant.SEND_FILE_ACTION);
		filter.addAction(Constant.SEND_MESSAGE_ACTION);
		filter.addAction(Constant.REMOTE_DESTROY_ACTION);
		filter.addAction(Constant.RECV_OFFLINE_MSG_ACTION);
		filter.addAction(Constant.ACTION_RECONNECT_STATE);

		registerReceiver(receiver, filter);
	}

	private class ChatReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Log.d(TAG, "recv broadcast action :" + intent.getAction());
			
			String action = intent.getAction();
			BaseHandler handler = null;
			
			if (action.equals(Constant.REMOTE_DESTROY_ACTION)) {
				final ChatMessage remoteMsg = (ChatMessage) intent.getParcelableExtra(Constant.REMOTE_DESTROY_KEY_MESSAGE);
				handler = (RemoteDestroyMessageHandler) new RemoteDestroyMessageHandler(remoteMsg);
			}
			else if (action.equals(Constant.SEND_FILE_ACTION)) {
				final ChatMessage fileMsg = (ChatMessage) intent.getParcelableExtra(Constant.SEND_FILE_KEY_MESSAGE);
				if (fileMsg.getChatType().equals(IMMessage.SINGLE)) {					
					handler = (SendFileMsgHandler) new SendFileMsgHandler(context, fileMsg, 
							new FileSentResultListener() {
								@Override
								public void onSentResult(ChatMessage msgSent) {
									MessageManager.getInstance().updateIMMessage(msgSent);
									Intent intent = new Intent(Constant.SEND_FILE_RESULT_ACTION);
									intent.putExtra(Constant.SEND_FILE_KEY_MESSAGE, msgSent);
									sendBroadcast(intent);
								}

								@Override
								public void onSentProgress(long cur, long total) {
									int ratio = (int)(cur * 100 / total);
									if(ratio % 20 == 0) {
										Intent intent = new Intent(Constant.FILE_PROGRESS_ACTION);
										intent.putExtra(Constant.SEND_FILE_KEY_MESSAGE, fileMsg);
										intent.putExtra(Constant.SEND_FILE_KEY_PROGRESS, ratio);
										sendBroadcast(intent);
									}
								}
						
					});
				}
				else {
					handler = (SendGroupFileMsgHandler) new SendGroupFileMsgHandler(context, fileMsg,
							new FileSentResultListener() {
								@Override
								public void onSentResult(ChatMessage msgSent) {
									MessageManager.getInstance().updateIMMessage(msgSent);

									Intent intent = new Intent(Constant.SEND_FILE_RESULT_ACTION);
									intent.putExtra(Constant.SEND_FILE_KEY_MESSAGE, msgSent);
									sendBroadcast(intent);
								}

								@Override
								public void onSentProgress(long cur, long total) {
									long ratio = cur * 100 / total;
									if(ratio % 20 == 0) {
										Intent intent = new Intent(Constant.FILE_PROGRESS_ACTION);
										intent.putExtra(Constant.SEND_FILE_KEY_MESSAGE, fileMsg);
										intent.putExtra(Constant.SEND_FILE_KEY_PROGRESS, ratio);
										sendBroadcast(intent);
									}
								}
					});
				}

				
			}
			else if (action.equals(Constant.SEND_MESSAGE_ACTION)) {
				final ChatMessage textMsg = (ChatMessage) intent.getParcelableExtra(Constant.SEND_MESSAGE_KEY_MESSAGE);

				if (textMsg.getChatType().equals(IMMessage.SINGLE)) {
					handler = (SendTextMsgHandler) new SendTextMsgHandler(textMsg, 
							new MessageSentResultListener() {
								@Override
								public void onSentResult(ChatMessage msgSent) {
									MessageManager.getInstance().updateIMMessage(msgSent);

									Intent intent = new Intent(Constant.SEND_MESSAGE_RESULT_ACTION);
									intent.putExtra(Constant.SEND_MESSAGE_KEY_MESSAGE, msgSent);
									sendBroadcast(intent);
								}
						
					});
				}
				else {
					handler = (SendGroupTextMsgHandler) new SendGroupTextMsgHandler(textMsg, 
							new MessageSentResultListener() {
								@Override
								public void onSentResult(ChatMessage msgSent) {
									MessageManager.getInstance().updateIMMessage(msgSent);

									Intent intent = new Intent(Constant.SEND_MESSAGE_RESULT_ACTION);
									intent.putExtra(Constant.SEND_MESSAGE_KEY_MESSAGE, msgSent);
									sendBroadcast(intent);
								}
						
					});
				}

			}
			else if (action.equals(Constant.RECV_OFFLINE_MSG_ACTION)) {
				/* dealOfflineMsg();
				XmppConnectionManager.getInstance().getConnection()
					.sendPacket(new Presence(Presence.Type.available));
					*/
			}
			else if (action.equals(Constant.GROUP_INVITE_ACTION)) {
				GroupUser grp = (GroupUser) intent.getParcelableExtra(Constant.GROUP_ACTION_KEY_INFO);
				handler = (InviteGroupChatHandler) new InviteGroupChatHandler(grp);
			}
			else if (action.equals(Constant.GROUP_QUIT_ACTION)) {
				GroupUser grp = (GroupUser) intent.getParcelableExtra(Constant.GROUP_ACTION_KEY_INFO);
				handler = (QuitGroupChatHandler) new QuitGroupChatHandler(grp);
			}
			else if (action.equals(Constant.GROUP_QUIT_ACTION)) {
				GroupUser grp = (GroupUser) intent.getParcelableExtra(Constant.GROUP_ACTION_KEY_INFO);
				handler = (GroupInfoUpdateHandler) new GroupInfoUpdateHandler(grp);
			}
			else if (Constant.ACTION_RECONNECT_STATE.equals(action)) {
				String status = intent.getStringExtra(
						Constant.RECONNECT_STATE);
				if(status.equals(XmppConnectionManager.DISCONNECTED)) {
					Log.i(TAG, "disconn succ, uninit chat listener");
					unInitChatManager();
				}
				else if(status.equals(XmppConnectionManager.CONNECTED)) {
					Log.i(TAG, "connection succ, init chat listener");
					initChatManager();
				}
			}
			
			if (handler != null) {
				mSendMsgWorker.addHandler(handler);
			}

		}
	}
	
	/**
	 * 
	 * 处理离线消息.
	 * 
	 * @author xuweinan
	 */
	private void dealOfflineMsg() {
		BaseHandler handler = null;
		try {
			
			handler = (RecvOfflineMsgHandler) new RecvOfflineMsgHandler( 
					new OfflineMessageRecvResultListener() {
						@Override
						public void onRecvResult(List<Message> msgs) {
							
							BaseHandler h = null;
							for (Message m : msgs) {
								org.jivesoftware.smack.packet.Message message = m;
								Log.i(TAG, "Received offline msg from [" + message.getFrom()
										+ "] message: " + message.getBody());
								
								if (message != null && message.getBody() != null
										&& !message.getBody().equals("null")) {

									
									String type = (String) message.getProperty(IMMessage.PROP_TYPE);
									if (type.equals(IMMessage.PROP_TYPE_CHAT)) {
										h = (RecvTextMsgHandler) new RecvTextMsgHandler(message, 
												new MessageRecvResultListener() {
													@Override
													public void onRecvResult(ChatMessage msgRecv) {
														
														MessageManager.getInstance().saveIMMessage(msgRecv);

														Intent intent = new Intent(Constant.NEW_MESSAGE_ACTION);
														intent.putExtra(ChatMessage.IMMESSAGE_KEY, msgRecv);
														sendBroadcast(intent);
														
													}
										});
									}
									else {
										String msgType = (String) message.getProperty(CtrlMessage.PROP_CTRL_MSGTYPE);
										
										if (msgType.contains("group")) {
											Intent intent = new Intent(Constant.NEW_CTRL_MESSAGE_ACTION);
											intent.putExtra(CtrlMessage.CTRLMESSAGE_KEY, message.getBody());
											sendBroadcast(intent);
										}
										else if (msgType.equals(CtrlMessage.CTRL_SEK_EXCHANGE)) {
											// TODO:
										}
										else if (msgType.equals(CtrlMessage.REMOTE_DESTROY)) {
											String id = (String) message.getProperty(CtrlMessage.PROP_ID);
											Log.i(TAG, "recv remote destroy command for message unique id:" + id);
											MessageManager.getInstance().delMessageByUniqueId(id);

											Intent intent = new Intent(Constant.NEW_MESSAGE_ACTION);
											sendBroadcast(intent);
										}
										else {
											h = (RecvFileMsgHandler) new RecvFileMsgHandler(context, message, 
													new MessageRecvResultListener() {
														@Override
														public void onRecvResult(ChatMessage msgRecv) {
															
															MessageManager.getInstance().saveIMMessage(msgRecv);

															Intent intent = new Intent(Constant.NEW_MESSAGE_ACTION);
															intent.putExtra(ChatMessage.IMMESSAGE_KEY, msgRecv);
															sendBroadcast(intent);
															
														}
											});
										}
										
									}
									
									mRecvMsgWorker.addHandler(h);
								}
							}
						}

			});
			mRecvMsgWorker.addHandler(handler);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void unInitChatManager() {
		try {
			XMPPConnection conn = XmppConnectionManager.getInstance()
					.getConnection();
			if(conn != null)
				conn.removePacketListener(pListener);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initChatManager() {
		try {
			XMPPConnection conn = XmppConnectionManager.getInstance()
					.getConnection();
			conn.addPacketListener(pListener, new OrFilter(
					new MessageTypeFilter(Message.Type.chat), 
					new MessageTypeFilter(Message.Type.normal)));
			
		} catch(Exception e) {
			e.printStackTrace();
			stopSelf();
		}
	}
	
	private void updateGroupInfo(String info) {
		GroupUser grp = GroupUser.loads(info);
		if (ContacterManager.groupUsers.containsKey(grp.getName())) {
			ContacterManager.groupUsers.remove(grp.getName());
		}
		ContacterManager.groupUsers.put(grp.getName(), grp);
		ContacterManager.userMe.delGroup(grp);
		ContacterManager.userMe.addGroup(grp);
		ContacterManager.saveUserVCard(XmppConnectionManager.getInstance().getConnection(), 
				ContacterManager.userMe);
	}

	PacketListener pSysListener = new PacketListener() {

		@Override
		public void processPacket(Packet arg0) {
			Log.d(TAG, "normal packet: " + arg0);

		}
	};
	
	PacketListener pListener = new PacketListener() {

		@Override
		public void processPacket(Packet arg0) {
			
			Log.d(TAG, "packet: " + arg0);

			Message message = (Message) arg0;
			BaseHandler handler = null;

			String from = message.getFrom().substring(0,
					message.getFrom().indexOf("/"));
			long noticeId = -1;

			if (message != null && message.getBody() != null
					&& !message.getBody().equals("null")) {
				
				if(!ContacterManager.contacters.containsKey(from)) {
					Log.d(TAG, "recv message from unknown user " + from);
					return;
				}
				
				String type = (String) message.getProperty(IMMessage.PROP_TYPE);
				if (type.equals(IMMessage.PROP_TYPE_CHAT)) {
					handler = (RecvTextMsgHandler) new RecvTextMsgHandler(message, 
							new MessageRecvResultListener() {
								@Override
								public void onRecvResult(ChatMessage msgRecv) {
									
									long newId = MessageManager.getInstance().saveIMMessage(msgRecv);
									msgRecv.setId("" + newId);

									Intent intent = new Intent(Constant.NEW_MESSAGE_ACTION);
									intent.putExtra(ChatMessage.IMMESSAGE_KEY, msgRecv);
									sendBroadcast(intent);
									
								}
					});
				}
				else {
					String msgType = (String) message.getProperty(CtrlMessage.PROP_CTRL_MSGTYPE);
					
					if (msgType.contains("group")) {
						
						updateGroupInfo(message.getBody());
						Intent intent = new Intent(Constant.NEW_CTRL_MESSAGE_ACTION);
						intent.putExtra(CtrlMessage.CTRLMESSAGE_KEY, message.getBody());
						sendBroadcast(intent);
					}
					else if (msgType.equals(CtrlMessage.CTRL_SEK_EXCHANGE)) {
						// TODO:
					}
					else if (msgType.equals(CtrlMessage.REMOTE_DESTROY)) {
						String id = (String) message.getProperty(CtrlMessage.PROP_ID);
						Log.i(TAG, "recv remote destroy command for message unique id:" + id);
						MessageManager.getInstance().delMessageByUniqueId(id);

						Intent intent = new Intent(Constant.NEW_MESSAGE_ACTION);
						sendBroadcast(intent);
					}
					else {
						handler = (RecvFileMsgHandler) new RecvFileMsgHandler(context, message, 
								new MessageRecvResultListener() {
									@Override
									public void onRecvResult(ChatMessage msgRecv) {
										
										long newId = MessageManager.getInstance().saveIMMessage(msgRecv);
										msgRecv.setId("" + newId);
										Intent intent = new Intent(Constant.NEW_MESSAGE_ACTION);
										intent.putExtra(ChatMessage.IMMESSAGE_KEY, msgRecv);
										sendBroadcast(intent);
										
									}
						});
					}
					
				}
				
				if (handler != null) {
					mRecvMsgWorker.addHandler(handler);
				}
				
				// 生成通知
				NoticeManager noticeManager = NoticeManager.getInstance();
				String name = noticeManager.getCurrentChatUser();
				User curUser = null;
				if(name != null) {
					if (ContacterManager.contacters.containsKey(StringUtil.getJidByName(name, 
							XmppConnectionManager.getInstance().getConnection().getServiceName()))) {
						curUser = ContacterManager.contacters.get(StringUtil.getJidByName(name, 
								XmppConnectionManager.getInstance().getConnection().getServiceName()));
						Log.d(TAG, "cur user is " + curUser );

					}
					else {
						// TODO: group user

						return;
					}
				}
				
				/*
				Notice notice = new Notice();
				notice.setTitle("会话信息");
				notice.setNoticeType(Notice.CHAT_MSG);
				notice.setContent(message.getBody());
				notice.setFrom(from);
				notice.setStatus(Notice.UNREAD);
				notice.setNoticeTime((String) message.getProperty(IMMessage.PROP_TIME));
				noticeId = noticeManager.saveNotice(notice);
				
				
				if (noticeId != -1) {
					notice.setId(String.valueOf(noticeId));

					if (curUser != null && curUser.getJID().equals(from)) {
						Log.d(TAG, "new message notice from " + from + " won't dispatch");
						return;
						
					} else {
						Log.d(TAG, "new message notice from " + from + " dispatch");

						User u = ContacterManager.contacters.get(from);
						NoticeManager.getInstance().dispatchIMMessageNotify(u);
					}
				}
				*/
				
				if (curUser != null && curUser.getJID().equals(from)) {
					Log.d(TAG, "new message notice from " + from + " won't dispatch");
					return;
					
				} else {
					Log.d(TAG, "new message notice from " + from + " dispatch");

					User u = ContacterManager.contacters.get(from);
					NoticeManager.getInstance().dispatchIMMessageNotify(u);
				}


			}

		}

	};


}
