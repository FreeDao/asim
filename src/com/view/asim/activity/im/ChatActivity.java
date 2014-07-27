package com.view.asim.activity.im;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPException;

import com.csipsimple.api.ISipService;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.view.asim.util.SoundMeter;
import com.view.asim.activity.LoginActivity;
import com.view.asim.activity.MainActivity;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.ChatMessageItem;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.GroupUser;
import com.view.asim.model.IMMessage;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.util.DateUtil;
import com.view.asim.util.FileUtil;
import com.view.asim.util.StringUtil;
import com.view.asim.view.FaceRelativeLayout;
import com.view.asim.view.MessageListAdapter;


import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import com.view.asim.R;

public class ChatActivity extends AChatActivity implements SensorEventListener {
	private static final String TAG = "ChatActivity";
	private static final String STATE_PAUSE_ON_SCROLL = "STATE_PAUSE_ON_SCROLL";
	private static final String STATE_PAUSE_ON_FLING = "STATE_PAUSE_ON_FLING";

	private AudioManager audioManager;
    private SensorManager mSensorManager;
    private Sensor mProximitySensor;
	private TextView mNetworkFailedTxt;

	private boolean playAudioNow = false;
	private TextView titleBack;
	private MessageListAdapter adapter = null;
	private List<ChatMessageItem> mChatItems = null;
	private EditText messageInput = null;
	private ImageView messageSendBtn = null;
	private ImageView voiceRecordBtn = null;
	private ImageView destroyBtn = null;
	
	private View addImgView = null;
	private View addVideoView = null;
	private View takePictureView = null;
	private View voiceCallView = null;
	private View addFileView = null;
	private View addVCardView = null;
	
	private FaceRelativeLayout faceLayout;
	private boolean isShort = false;
	private ImageView img1, sc_img1;
	private ImageView chatting_mode_btn, volume;
	private AnimationDrawable volumeIcon;

	private ChatMessage mForwardMsg = null;
	private String mDestroy = IMMessage.NEVER_BURN;
	private File mTempCaptureImgFile = null;

	private ImageButton userInfo;
	private ListView listView;
	private LinearLayout del_re;
	private int flag = 1;
	private View rcChat_popup;
	private View voiceView;
	private Handler mHandler = new Handler();
	private long startVoiceT, endVoiceT;

	private String voiceName;

	private LinearLayout voice_rcd_hint_loading, voice_rcd_hint_rcding, voice_rcd_hint_tooshort;
	private SoundMeter mSensor;
	
	NoticeManager mNoticeManager = null;
	ClipboardManager mClipMan = null;
	private MediaPlayer mMediaPlayer = new MediaPlayer();

	protected boolean pauseOnScroll = false;
	protected boolean pauseOnFling = true;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.chat);
		init();
	}

	private void init() {
		getEimApplication().addActivity(this);
		audioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		mNoticeManager = NoticeManager.getInstance();
		
		mChatItems = new ArrayList<ChatMessageItem>();
		
		mClipMan = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		mNetworkFailedTxt = (TextView) findViewById(R.id.network_failed_txt);

		titleBack = (TextView) findViewById(R.id.title_back_btn);
		titleBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		del_re = (LinearLayout) this.findViewById(R.id.del_re);
		rcChat_popup = this.findViewById(R.id.rcChat_popup);
		voice_rcd_hint_rcding = (LinearLayout) this
				.findViewById(R.id.voice_rcd_hint_rcding);
		voice_rcd_hint_loading = (LinearLayout) this
				.findViewById(R.id.voice_rcd_hint_loading);
		voice_rcd_hint_tooshort = (LinearLayout) this
				.findViewById(R.id.voice_rcd_hint_tooshort);
		mSensor = new SoundMeter();
		
		faceLayout = (FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout);
		faceLayout.setActivityContext(this);
		voiceView = findViewById(R.id.ll_audio_record);

		titleBack.setText(mUser.getNickName());

		userInfo = (ImageButton) findViewById(R.id.user_info);
		userInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent();
				intent.putExtra(User.userKey, mUser);
				intent.putExtra(UserInfoActivity.modeKey, UserInfoActivity.FRIEND_MODE);
				intent.setClass(ChatActivity.this, UserInfoActivity.class);
				startActivity(intent);
			}
		});

		listView = (ListView) findViewById(R.id.chat_list);
		listView.setCacheColorHint(0);
		adapter = new MessageListAdapter(ChatActivity.this, mChatItems,
				listView, mUser, imageLoader);
		img1 = (ImageView) this.findViewById(R.id.img1);
		sc_img1 = (ImageView) this.findViewById(R.id.sc_img1);
		volume = (ImageView) this.findViewById(R.id.volume);

		listView.setAdapter(adapter);
		
		adapter.setMessageContentLongClickListener(mChatMessageOnLongClickListener);  
		
		adapter.setSentFailedClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ChatMessage msg = (ChatMessage) v.getTag();
				showResendDialog(msg);
			}
		});

		adapter.setLeftAudioClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ChatMessage msg = (ChatMessage) v.getTag();
				Drawable[] drawables = ((Button)v).getCompoundDrawables();
				
				if (playAudioNow) {
					volumeIcon.stop();
				}

				volumeIcon = (AnimationDrawable) drawables[0];
				playMusic(msg.getAttachment().getSrcUri());
			}
		});
		
		adapter.setRightAudioClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ChatMessage msg = (ChatMessage) v.getTag();
				Drawable[] drawables = ((Button)v).getCompoundDrawables();
				
				if (playAudioNow) {
					volumeIcon.stop();
				}

				volumeIcon = (AnimationDrawable) drawables[2];
				playMusic(msg.getAttachment().getSrcUri()) ;
			}
		});
		
		adapter.setImageClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ChatMessage msg = (ChatMessage) v.getTag();

				Intent intent = new Intent();
				intent.putExtra(ChatMessage.IMMESSAGE_KEY, msg);
				intent.setClass(context, ImagePreviewActivity.class);
				startActivity(intent);
			}
		});
		
		adapter.setVideoClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ChatMessage msg = (ChatMessage) v.getTag();
				Intent intent = new Intent();
				intent.putExtra(ChatMessage.IMMESSAGE_KEY, msg);
				intent.setClass(context, VideoPreviewActivity.class);
				startActivity(intent);
			}
		});  
		
		adapter.setRightBurnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ChatMessage msg = (ChatMessage) v.getTag();
				Intent intent = new Intent();

				if (msg.getType().equals(ChatMessage.CHAT_IMAGE)) {
					intent.setClass(context, ImagePreviewActivity.class);
					intent.putExtra(ChatMessage.IMMESSAGE_KEY, msg);
				} 
				else if (msg.getType().equals(ChatMessage.CHAT_VIDEO)) {
					intent.setClass(context, VideoPreviewActivity.class);
					intent.putExtra(ChatMessage.IMMESSAGE_KEY, msg);
				}
				else {
					intent.setClass(context, BurnMsgViewActivity.class);
					intent.putExtra(ChatMessage.IMMESSAGE_KEY, msg);
				}
	            startActivityForResult(intent, Constant.REQCODE_BURN_AFTER_READ); 
			}
		}); 
		
		adapter.setLeftBurnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ChatMessage msg = (ChatMessage) v.getTag();
				Intent intent = new Intent();

				if (msg.getType().equals(ChatMessage.CHAT_IMAGE)) {
					intent.setClass(context, ImagePreviewActivity.class);
					intent.putExtra(ChatMessage.IMMESSAGE_KEY, msg);
					
				} else if (msg.getType().equals(ChatMessage.CHAT_VIDEO)) {
					intent.setClass(context, VideoPreviewActivity.class);
					intent.putExtra(ChatMessage.IMMESSAGE_KEY, msg);
					
				} else {
					intent.setClass(context, BurnMsgViewActivity.class);
					intent.putExtra(ChatMessage.IMMESSAGE_KEY, msg);
				}
	            startActivityForResult(intent, Constant.REQCODE_BURN_AFTER_READ); 
			}
		});
		
		voiceRecordBtn = (ImageView) findViewById(R.id.voice_record_img);
		voiceRecordBtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});
		
		findViewById(R.id.chat_voice_btn).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				closeInput();
				faceLayout.hideFaceView();
				faceLayout.hideAddMoreView();
				if (voiceView.getVisibility() == View.VISIBLE) {
					voiceView.setVisibility(View.GONE);
				} else {
					voiceView.setVisibility(View.VISIBLE);
				}
			}
			
		});
		
		messageInput = (EditText) findViewById(R.id.chat_content);
		messageSendBtn = (ImageView) findViewById(R.id.chat_normal_sendbtn);
		messageSendBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String message = messageInput.getText().toString();
				if ("".equals(message)) {

				} else {

					try {
						sendMessage(message, mDestroy);
						messageInput.setText("");
					} catch (Exception e) {
						showToast("信息发送失败");
						e.printStackTrace();
						messageInput.setText(message);
					}
					
					faceLayout.hideFaceView();
					faceLayout.hideAddMoreView();
					faceLayout.hideVoiceView();

				}
			}
		});
		
		destroyBtn = (ImageView) findViewById(R.id.destroy_btn);
		destroyBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mDestroy.equals(IMMessage.NEVER_BURN)) {
					showToast("阅后即焚模式");
					mDestroy = IMMessage.BURN_AFTER_READ;
					v.setBackgroundResource(R.drawable.image_burn_icon_check);
					v.setAlpha((float) 1.0);

				}
				else {
					showToast("普通聊天模式");

					mDestroy = IMMessage.NEVER_BURN;
					v.setBackgroundResource(R.drawable.image_burn_icon_default);
					v.setAlpha((float) 0.4);
					
				}
			}
		});
		
		addImgView = findViewById(R.id.ll_add_img_btn);
		addImgView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
	        	// 激活系统图库，选择一张图片  
	            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  
	            intent.setType("image/*");  
	            startActivityForResult(intent, Constant.REQCODE_IMAGE_PICK);
			}
		});
		
		addVideoView = findViewById(R.id.ll_add_video_btn);
		addVideoView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
	        	// 调用 VCamera 采集短视频（10s）  
				Intent intent = new Intent();
				intent.setClass(context, MediaRecorderActivity.class);
				startActivityForResult(intent, Constant.REQCODE_CAPTURE_VIDEO);			
			}
		});
		
		addVCardView = findViewById(R.id.ll_send_vcard_btn);
		addVCardView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showToast("本功能将在下一版本发布，敬请期待:)");

			}
		});
		
		voiceCallView = findViewById(R.id.ll_voice_call_btn);
		voiceCallView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
                try {
					service.makeCall(StringUtil.getCellphoneByName(mUser.getJID()), (int) ContacterManager.userMe.getSipAccountId());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		
		addFileView = findViewById(R.id.ll_send_file_btn);
		addFileView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showToast("本功能将在下一版本发布，敬请期待:)");

			}
		});
		
		takePictureView = findViewById(R.id.ll_take_picture_btn);
		takePictureView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
	        	// 激活相机  
	            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
	    		
	    		String fileSaveName = FileUtil.genCaptureImageName(getWith());
	    		
	    		mTempCaptureImgFile = new File(FileUtil.getImagePathByWith(getWith()) + fileSaveName);  
	    		Log.d(TAG, "start capture image from camera: " + mTempCaptureImgFile);
                // 从文件中创建uri  
                Uri uri = Uri.fromFile(mTempCaptureImgFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                
	            startActivityForResult(intent, Constant.REQCODE_TAKE_PICTURE); 
			}
		});
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Uri uri = null;
		if (resultCode == RESULT_OK) {
			faceLayout.hideFaceView();
			faceLayout.hideVoiceView();
			faceLayout.hideAddMoreView();
			
			switch(requestCode){
				case Constant.REQCODE_BURN_AFTER_READ:
		        	ChatMessage msg = data.getParcelableExtra(ChatMessage.IMMESSAGE_KEY);
		        	Log.d(TAG, "remove message id " + msg.getId() + ", " + msg.getContent());
		        	showToast("消息已销毁");
		        	MessageManager.getInstance().delChatHisById(msg.getId());
		        	refreshMessage();
					
					
					break;
				
		        case Constant.REQCODE_IMAGE_PICK:
		            if (data != null) {  
		                // 得到图片的全路径  
		                uri = data.getData();  
		                Log.d(TAG, "send image from local: " + uri.toString());
		                
		                if(judgeMediaFileSize(uri)) {

			                try {
								sendImage(uri, mDestroy);
							} catch (Exception e) {
								e.printStackTrace();
								return;
							}  
		                }
		            }   
		            
		            break;
		        	
		        case Constant.REQCODE_CAPTURE_VIDEO:
		        	String videoPath = data.getStringExtra(MediaRecorderActivity.VIDEO_PATH);
		        	String thumbPath = data.getStringExtra(MediaRecorderActivity.VIDEO_THUMB_PATH);
		        	
	                Log.d(TAG, "capture micro video from local: " + videoPath + ", thumb: " + thumbPath);

		        	try {
						sendVideo(videoPath, thumbPath, mDestroy);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					} 
		        	break;
		        	
		        case Constant.REQCODE_TAKE_PICTURE:
	                // 得到图片的全路径  
	                uri = Uri.fromFile(mTempCaptureImgFile);
	                Log.d(TAG, "capture a picutre " + uri.getPath());
	                try {
						sendImage(uri, mDestroy);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}  
		        	break;
		        	
		        case Constant.REQCODE_FILE_PICK:
		        	if (data != null) {  
		                // 得到图片的全路径  
		                uri = data.getData();  
		                try {
							sendFile(uri, mDestroy);
						} catch (Exception e) {
							e.printStackTrace();
							return;
						}  
		            }  
		        	break;
		        	
		        case Constant.REQCODE_SELECT_USERS:
		        	Intent intent = null;
	                ArrayList<User> selectUsers = (ArrayList<User>)data.getSerializableExtra(User.userListKey);  
	                if (selectUsers.size() == 0) {
	                	return;
	                } else if (selectUsers.size() == 1) {
	                	mForwardMsg.setWith(selectUsers.get(0).getJID());
	            		intent = new Intent(context, ChatActivity.class);
	            		intent.putExtra(User.userKey, selectUsers.get(0));
	            		intent.putExtra(ChatMessage.IMMESSAGE_KEY, mForwardMsg);

	            		startActivity(intent);
	            		finish();
	            	
	                	return;
	                	
	                } else {
	                	// 生成新的群聊组
	                	GroupUser grp = ContacterManager.createGroupUserByMember(selectUsers);
	                	ContacterManager.groupUsers.remove(grp.getName());
	                	ContacterManager.groupUsers.put(grp.getName(), grp);
	                	ContacterManager.userMe.addGroup(grp);
	                	
	                	intent = new Intent(Constant.GROUP_INVITE_ACTION);
	            		intent.putExtra(Constant.GROUP_ACTION_KEY_INFO, grp);
	            		sendBroadcast(intent);
	            		
	                	mForwardMsg.setWith(grp.getName());
	                	mForwardMsg.setChatType(IMMessage.GROUP);
	                	intent = new Intent(context, ChatActivity.class);
	            		intent.putExtra(User.userKey, selectUsers.get(0));
	            		intent.putExtra(ChatMessage.IMMESSAGE_KEY, mForwardMsg);

	            		startActivity(intent);
	            		finish();	                
	            	}
	                break;
			}
		}
    }
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		pauseOnScroll = savedInstanceState.getBoolean(STATE_PAUSE_ON_SCROLL, false);
		pauseOnFling = savedInstanceState.getBoolean(STATE_PAUSE_ON_FLING, true);
	}

	private void applyScrollListener() {
		listView.setOnScrollListener(new PauseOnScrollListener(imageLoader, pauseOnScroll, pauseOnFling));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(STATE_PAUSE_ON_SCROLL, pauseOnScroll);
		outState.putBoolean(STATE_PAUSE_ON_FLING, pauseOnFling);
	}
	
	
	OnLongClickListener mChatMessageOnLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			
			final View targetView = v;
			new AlertDialog.Builder(ChatActivity.this).
			setItems(new String[] { "复制", "转发", "销毁" }, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = null;
					if (which == 0) {
						/*
						String content = ((Button)targetView).getText().toString();
			        	mClipMan.setText(content);
			        	*/
						showToast("本功能将在下一版本发布，敬请期待:)");

					}
					else if (which == 1){
						/*
						mForwardMsg = (ChatMessage)targetView.getTag();
						intent = new Intent();
						intent.setClass(context, GroupAddUserActivity.class);
						startActivityForResult(intent, Constant.REQCODE_SELECT_USERS);
						*/
						showToast("本功能将在下一版本发布，敬请期待:)");

					}
					else {
						ChatMessage msg = (ChatMessage)targetView.getTag();
						Log.d(TAG, "remove message id " + msg.getId() + ", " + msg.getContent());
			        	MessageManager.getInstance().delChatHisById(msg.getId());
			        	refreshMessage();
			        	if(msg.getDir().equals(IMMessage.SEND)) {
			        		showRemoteDestroyDialog(msg);
			        	}
					}
					
				}
			}).
			show();
			return false;
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& ((FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout))
						.hideFaceView()) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private ChatMessageItem createChatMessageItem(String timestamp) {
		ChatMessageItem item = new ChatMessageItem();
		item.setType(ChatMessage.TIMESTAMP);
		item.setValue((Object)timestamp);
		return item;
	}
	
	private ChatMessageItem createChatMessageItem(ChatMessage msg) {
		ChatMessageItem item = new ChatMessageItem();
		item.setType(msg.getType());
		item.setValue((Object)msg);
		return item;		
	}
	
	private void playMusic(String name) {
		
		Log.d(TAG, "play audio file: " + name);
		try {
			playAudioNow = true;
			
			volumeIcon.start();
			
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(name);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					volumeIcon.stop();
					playAudioNow = false;
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void showResendDialog(final ChatMessage msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.resend_msg_confim))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								msg.setStatus(IMMessage.INPROGRESS);
								Intent intent = new Intent();
								intent.setAction(Constant.SEND_MESSAGE_ACTION);
								intent.putExtra(Constant.SEND_MESSAGE_KEY_MESSAGE, msg);
								sendBroadcast(intent);

								// 刷新视图
								refreshMessage();
							}
						})
				.setNegativeButton(getResources().getString(R.string.no),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void showRemoteDestroyDialog(final ChatMessage msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.remote_destroy_msg_confim))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								Intent intent = new Intent();
								intent.setAction(Constant.REMOTE_DESTROY_ACTION);
								intent.putExtra(Constant.REMOTE_DESTROY_KEY_MESSAGE, msg);
								sendBroadcast(intent);

								// 刷新视图
								refreshMessage();
							}
						})
				.setNegativeButton(getResources().getString(R.string.no),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	protected void refreshChatMessageList() {

		int i = 0;
		int j = 0;
		Long tm1, tm2;
		ChatMessage msg1 = null;
		ChatMessage msg2 = null;
		ChatMessageItem item = null;
		List<ChatMessage> allMsgs = getMessages();
		Collections.sort(allMsgs);
		
		if (allMsgs == null) {
			return;
		}
		
		mChatItems.clear();
		
		while(i < allMsgs.size()) {
			msg1 = allMsgs.get(i);
			tm1 = DateUtil.str2Calendar(msg1.getTime()).getTimeInMillis();
			item = createChatMessageItem(DateUtil.getMDHM(tm1));
			mChatItems.add(item);
			item = createChatMessageItem(msg1);
			mChatItems.add(item);
			
			for (j = i + 1; j < allMsgs.size(); j++) {
				msg2 = allMsgs.get(j);
				tm2 = DateUtil.str2Calendar(msg2.getTime()).getTimeInMillis();
				if (tm2 - tm1 <= 1000 * 60 * 5) { // 五分钟以内归为一组
					item = createChatMessageItem(msg2);
					mChatItems.add(item);					
					continue;
				}
				else {
					break;
				}
			}
			i = j;
		}
	}

	@Override
	protected void receiveNewMessage(ChatMessage message) {
		Log.d(TAG, "receiveNewMessage: " + message);

	}

	@Override
	protected void refreshMessage() {
		super.refreshMessage();
		
//		Log.d(TAG, "refresh chat msg list start on: " + DateUtil.getCurDateStr());
//
//		refreshChatMessageList();
//		
//		Log.d(TAG, "refresh chat msg list end on: " + DateUtil.getCurDateStr());
//
//		adapter.refreshList(mChatItems);

		refreshMessageView();
		
		// 更新某人所有通知
		MessageManager.getInstance().updateReadStatus(mUser.getJID(), IMMessage.READ);
		
		refreshViewOnAUKeyStatusChange();
		
		// 更新网络状态界面
		refreshConnStatusView();

	}
	
	@Override
	protected void refreshMessageView() {
		Log.d(TAG, "refresh chat msg list start on: " + DateUtil.getCurDateStr());

		refreshChatMessageList();
		
		Log.d(TAG, "refresh chat msg list end on: " + DateUtil.getCurDateStr());

		adapter.refreshList(mChatItems);
	}
	
	protected void refreshConnStatusView() {
		if (XmppConnectionManager.getInstance().getConnection().isConnected()) {
			mNetworkFailedTxt.setVisibility(View.GONE);
		} else {
			mNetworkFailedTxt.setVisibility(View.VISIBLE);
		}
	}
	
	private void refreshViewOnAUKeyStatusChange() {
		View titleBar = findViewById(R.id.main_head);
		ImageView aukeyOnlineImg = (ImageView) findViewById(R.id.aukey_online_img);
		if (AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED)) {
			titleBar.setBackgroundColor(getResources().getColor(R.color.grayblack));
			titleBack.setTextColor(getResources().getColor(R.color.white));
			titleBack.setBackgroundResource(R.drawable.title_clickable_background_black);
			userInfo.setBackgroundResource(R.drawable.title_clickable_background_black);
			//aukeyOnlineImg.setVisibility(View.VISIBLE);
			//findViewById(R.id.aukey_online_img).setVisibility(View.VISIBLE);
			listView.setBackgroundResource(R.drawable.account_guidance_bg);
			findViewById(R.id.head_body_divide).setVisibility(View.GONE);

		}
		else {
			titleBar.setBackgroundColor(getResources().getColor(R.color.white6));
			titleBack.setTextColor(getResources().getColor(R.color.darkgray));
			titleBack.setBackgroundResource(R.drawable.title_clickable_background);
			userInfo.setBackgroundResource(R.drawable.title_clickable_background);

			//aukeyOnlineImg.setVisibility(View.GONE);
			//findViewById(R.id.aukey_online_img).setVisibility(View.GONE);
			listView.setBackgroundColor(getResources().getColor(R.color.chatgray));
			findViewById(R.id.head_body_divide).setVisibility(View.VISIBLE);

		}
			
	}

	@Override
	protected void onResume() {
		super.onResume();
		closeInput();

		applyScrollListener();

		refreshMessage();
		
		if (mChatType.equals(IMMessage.SINGLE)) {
			mNoticeManager.setCurrentChatUser(mUser.getName());
			mNoticeManager.clearIMMessageNotify(mUser.getName());
		}
		else {
			mNoticeManager.setCurrentChatUser(mGroup.getName());
			mNoticeManager.clearIMMessageNotify(mGroup.getName());
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		mNoticeManager.setCurrentChatUser(null);
	}

	@Override
	protected void sendMessageResult(ChatMessage message) {
		
	}

	@Override
	protected void sendFileResult(ChatMessage message) {
		
	}

	@Override
	protected void sendFileProgressUpdate(ChatMessage message, int ratio) {
		ChatMessageItem newItem = createChatMessageItem(message);
		newItem.setProgress(ratio);
		adapter.refreshItem(newItem);
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (voiceView.getVisibility() == View.VISIBLE) {
			
			int[] location = new int[2];
			voiceRecordBtn.getLocationInWindow(location); // 获取在当前窗口内的绝对坐标
			int btn_rc_Y = location[1];
			int btn_rc_X = location[0];
			int[] del_location = new int[2];
			del_re.getLocationInWindow(del_location);
			int del_Y = del_location[1];
			int del_x = del_location[0];
			
			if (event.getAction() == MotionEvent.ACTION_DOWN && flag == 1) {
				
				if (event.getY() > btn_rc_Y && event.getX() > btn_rc_X) {//判断手势按下的位置是否是语音录制按钮的范围内
					voiceRecordBtn.setBackgroundResource(R.drawable.new_start_audio_record_pressed);
					rcChat_popup.setVisibility(View.VISIBLE);
					voice_rcd_hint_loading.setVisibility(View.VISIBLE);
					voice_rcd_hint_rcding.setVisibility(View.GONE);
					voice_rcd_hint_tooshort.setVisibility(View.GONE);
					mHandler.postDelayed(new Runnable() {
						public void run() {
							if (!isShort) {
								voice_rcd_hint_loading.setVisibility(View.GONE);
								voice_rcd_hint_rcding.setVisibility(View.VISIBLE);
							}
						}
					}, 300);
					img1.setVisibility(View.VISIBLE);
					del_re.setVisibility(View.GONE);
					startVoiceT = Calendar.getInstance().getTimeInMillis();
					voiceName = FileUtil.genCaptureAudioName(getWith());
					start(voiceName);
					flag = 2;
				}
			} 
			else if (event.getAction() == MotionEvent.ACTION_UP && flag == 2) {
				
				//松开手势时执行录制完成
				voiceRecordBtn.setBackgroundResource(R.drawable.new_start_audio_record_normal);
				if (event.getY() >= del_Y
						&& event.getY() <= del_Y + del_re.getHeight()
						&& event.getX() >= del_x
						&& event.getX() <= del_x + del_re.getWidth()) {
					rcChat_popup.setVisibility(View.GONE);
					img1.setVisibility(View.VISIBLE);
					del_re.setVisibility(View.GONE);
					stop();
					flag = 1;
					File file = new File(FileUtil.getAudioPathByWith(getWith()) + voiceName);
					if (file.exists()) {
						file.delete();
					}
				} else {

					voice_rcd_hint_rcding.setVisibility(View.GONE);
					stop();
					endVoiceT = Calendar.getInstance().getTimeInMillis();
					flag = 1;
					int time = (int) ((endVoiceT - startVoiceT) / 1000);
					if (time < 1) {
						isShort = true;
						voice_rcd_hint_loading.setVisibility(View.GONE);
						voice_rcd_hint_rcding.setVisibility(View.GONE);
						voice_rcd_hint_tooshort.setVisibility(View.VISIBLE);
						mHandler.postDelayed(new Runnable() {
							public void run() {
								voice_rcd_hint_tooshort
										.setVisibility(View.GONE);
								rcChat_popup.setVisibility(View.GONE);
								isShort = false;
							}
						}, 500);
						return false;
					}
					
					rcChat_popup.setVisibility(View.GONE);
					
					
					try {
						String fileName = FileUtil.getAudioPathByWith(getWith()) + voiceName;
						sendFile(Uri.parse(fileName), time, mDestroy);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					faceLayout.hideFaceView();
					faceLayout.hideAddMoreView();
					faceLayout.hideVoiceView();
				}
			}
			if (event.getY() < btn_rc_Y) {//手势按下的位置不在语音录制按钮的范围内
				Animation mLitteAnimation = AnimationUtils.loadAnimation(this,
						R.anim.cancel_rc);
				Animation mBigAnimation = AnimationUtils.loadAnimation(this,
						R.anim.cancel_rc2);
				img1.setVisibility(View.GONE);
				del_re.setVisibility(View.VISIBLE);
				del_re.setBackgroundResource(R.drawable.voice_rcd_cancel_bg);
				if (event.getY() >= del_Y
						&& event.getY() <= del_Y + del_re.getHeight()
						&& event.getX() >= del_x
						&& event.getX() <= del_x + del_re.getWidth()) {
					del_re.setBackgroundResource(R.drawable.voice_rcd_cancel_bg_focused);
					sc_img1.startAnimation(mLitteAnimation);
					sc_img1.startAnimation(mBigAnimation);
				}
			} else {

				img1.setVisibility(View.VISIBLE);
				del_re.setVisibility(View.GONE);
				del_re.setBackgroundResource(0);
			}
			}
			return super.onTouchEvent(event);
		}

		private static final int POLL_INTERVAL = 300;

		private Runnable mSleepTask = new Runnable() {
			public void run() {
				stop();
			}
		};
		private Runnable mPollTask = new Runnable() {
			public void run() {
				double amp = mSensor.getAmplitude();
				updateDisplay(amp);
				mHandler.postDelayed(mPollTask, POLL_INTERVAL);

			}
		};

		private void start(String name) {
			String path = FileUtil.getAudioPathByWith(getWith());
			FileUtil.createDir(new File(path));
			String fileName = path + name;
			mSensor.start(fileName);
			mHandler.postDelayed(mPollTask, POLL_INTERVAL);
		}

		private void stop() {
			mHandler.removeCallbacks(mSleepTask);
			mHandler.removeCallbacks(mPollTask);
			mSensor.stop();
			volume.setImageResource(R.drawable.voice_amp1);
		}

		private void updateDisplay(double signalEMA) {
			
			switch ((int) signalEMA) {
			case 0:
			case 1:
				volume.setImageResource(R.drawable.voice_amp1);
				break;
			case 2:
			case 3:
				volume.setImageResource(R.drawable.voice_amp2);
				break;
			case 4:
			case 5:
				volume.setImageResource(R.drawable.voice_amp3);
				break;
			case 6:
			case 7:
				volume.setImageResource(R.drawable.voice_amp4);
				break;
			case 8:
			case 9:
				volume.setImageResource(R.drawable.voice_amp5);
				break;
			case 10:
			case 11:
				volume.setImageResource(R.drawable.voice_amp6);
				break;
			default:
				volume.setImageResource(R.drawable.voice_amp6);
				break;
			}
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
	        float range = event.values[0];

	        if (range == mProximitySensor.getMaximumRange()) {
	            audioManager.setMode(AudioManager.MODE_NORMAL);
	            if (playAudioNow) {
	            	showToast("语音切换到外放模式");
	            }
	        } else {
	        	audioManager.setMode(AudioManager.MODE_IN_CALL);
	        	if (playAudioNow) {
	            	showToast("语音切换到听筒模式");
	            }	             
	        }
	   }

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {			
		}

		// 网络状态变化通知处理
		@Override
		protected void handReConnect(String status) {
			if (status.equals(XmppConnectionManager.CONNECTED) && XmppConnectionManager.getInstance().getConnection().isConnected()) {
				mNetworkFailedTxt.setVisibility(View.GONE);
			} else {
				mNetworkFailedTxt.setVisibility(View.VISIBLE);
			}
		}


}