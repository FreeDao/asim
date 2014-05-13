package com.view.asim.activity.im;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;

import com.view.asim.activity.ActivitySupport;
import com.view.asim.comm.Constant;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.UserManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.IMMessage;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.util.DateUtil;
import com.view.asim.util.StringUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.view.asim.R;

/**
 * 
 * 用户资料查看.
 * 
 * @author xuweinan
 */
public class UserInfoActivity extends ActivitySupport {
	public static final String TAG = "UserInfoActivity";
	public static final String modeKey = "show_mode";
	
	public static final int FRIEND_MODE = 0;
	public static final int STRANDER_MODE = 1;
	
	private int mShowType = 0;
	
	private User mUser = null;
	private TextView mBackTxtBtn;
	private ImageView mAvatarImg;
	private TextView mNicknameTxt;
	private TextView mNameTxt;
	private TextView mRemarkTxt;
	private TextView mLocationTxt;
	private TextView mCellphoneTxt;

	private LinearLayout mFriendOperLayout;
	private Button mAddUserBtn;
	private Button mChatBtn;
	private Button mVoiceBtn;
	private ImageButton mRmvUserBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.user_info);
		init();
	}

	private void init() {
		getEimApplication().addActivity(this);

		mUser = (User)getIntent().getParcelableExtra(User.userKey);
		mShowType = getIntent().getIntExtra(modeKey, 0);

		Log.d(TAG, "user:" + mUser + ", mode: " + mShowType);
		
		mBackTxtBtn = (TextView) findViewById(R.id.title_back_btn);
		mBackTxtBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mAvatarImg = (ImageView) findViewById(R.id.user_avatar_img);
		mNicknameTxt = (TextView) findViewById(R.id.user_nickname_txt);
		mRemarkTxt = (TextView) findViewById(R.id.user_remark_txt);
		mLocationTxt = (TextView) findViewById(R.id.user_location_txt);
		mCellphoneTxt = (TextView) findViewById(R.id.user_cellphone_txt);

		mFriendOperLayout = (LinearLayout) findViewById(R.id.friend_oper_layout);
		mAddUserBtn = (Button) findViewById(R.id.adduser_btn);
		mChatBtn = (Button) findViewById(R.id.chat_btn);
		mVoiceBtn = (Button) findViewById(R.id.voice_btn);
		mRmvUserBtn = (ImageButton) findViewById(R.id.rmv_user_btn);
		
		mChatBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createChat(mUser.getJID());
			}
		});

		mAddUserBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				NoticeManager noticeManager = NoticeManager
						.getInstance();
				
				List<Notice> notices = null;
				
				notices = noticeManager.getNoticeByWith(mUser.getJID());
				
				if (notices == null || notices.size() == 0 ) {
					// 全新的添加好友请求
					Log.d(TAG, "add friend request for new user " + mUser.getJID());

					try {
						ContacterManager.createSubscriber(mUser.getJID(), mUser.getNickName(), null);
					} catch (XMPPException xe) {
						xe.printStackTrace();
						return;
					}
					
					Notice notice = new Notice();
					notice.setReadStatus(Notice.READ);
					notice.setWith(mUser.getJID());
					notice.setStatus(Notice.STATUS_WAIT_FOR_ACCEPT);
					notice.setDispStatus(Notice.DISPLAY);
					notice.setTime(DateUtil.getCurDateStr());
					notice.setContent(mUser.getNickName());
					
					if (mUser.getHeadImg() != null) {
						notice.setAvatar(mUser.getHeadImg());
					} else {
						if (mUser.getGender() == null) {
							notice.setAvatar(BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar_male));
							
						} else {
							if (mUser.getGender().equals(User.MALE)) {
								notice.setAvatar(BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar_male));
							}
							else {
								notice.setAvatar(BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar_female));
							}
						}
					}
					
					NoticeManager.getInstance().saveNotice(notice);
				}
				else if (notices.size() == 1) {
					Notice n = notices.get(0);

					// 如果该好友已经主动添加过我，我此时又尝试添加他，则直接同意他之前的请求
					if(n.getStatus().equals(Notice.STATUS_ADD_REQUEST)) {
						Log.d(TAG, "you want to add " + mUser.getJID() + 
								" to friend list, but its request sent to you.");
						
						ContacterManager.sendSubscribe(Presence.Type.subscribed, mUser.getJID());
						ContacterManager.sendSubscribe(Presence.Type.subscribe, mUser.getJID());

						noticeManager.updateStatusById(n.getId(), Notice.STATUS_COMPLETE);
						noticeManager.updateDispStatusById(n.getId(), Notice.DISPLAY);

						
						new ContacterUpdateThread(mUser.getJID()).start();
					}
					// 该好友已经在好友列表里，又搜索他去添加好友，不应该出现这种场景，在之前搜索环节已经过滤
					else if (n.getStatus().equals(Notice.STATUS_COMPLETE)) {
						Log.e(TAG, "the user " + mUser.getJID() + " has been your friend.");
					}
					// 之前已经添加过该好友，正在等待其同意，此时又搜索到他尝试重复添加
					else {
						Log.e(TAG, "you have waited for the user " + mUser.getJID() + " confirm, do not try to add it again.");
						noticeManager.updateTimeById(n.getId(), DateUtil.getCurDateStr());
					}
					
				}
				else {
					// 同一个好友不应该有重复的添加请求
					Log.e(TAG, "the notice count from user " + mUser.getJID() + " is more than one!");
				}
				
				finish();
			}
		});

		mRmvUserBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDeleteDialog();
			}
		});
		
		mAvatarImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra(User.userKey, mUser);
				intent.setClass(context, UserAvatarActivity.class);
				startActivity(intent);
			}
		});
		
		setUserInfoView();
	}
	
	private class ContacterUpdateThread extends Thread {
		private String user = null;
		
		public ContacterUpdateThread(String jid) {
			user = jid;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "ContacterUpdateThread");
			ContacterManager.loadAndUpdateContacter(user);
		}
	}
	
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

	protected void showDeleteDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				getResources().getString(R.string.delete_user_confim))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {

								try {
									ContacterManager.removeSubscriber(mUser.getJID());
								} catch (XMPPException e) {
									Log.e(TAG, "", e);
								}

								NoticeManager.getInstance().setCurrentChatUser(null);
								NoticeManager.getInstance().clearIMMessageNotify(mUser);

								// 删除数据库
								NoticeManager.getInstance()
										.delNoticeByWith(mUser.getJID());
								MessageManager.getInstance()
										.delChatHisByName(mUser.getJID());
								finish();

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
	
	protected void setUserInfoView() {
		if (mUser.getNickName() == null) {
			mNicknameTxt.setVisibility(View.GONE);
		} else {
			mNicknameTxt.setText(mUser.getNickName());
		}
		
		if (mUser.getGender() == null) {
			mNicknameTxt.setCompoundDrawables(null, null, null, null);
			
		} else {
			Drawable maleIcon = getResources().getDrawable(R.drawable.business_card_male_icon);
			maleIcon.setBounds(0, 0, maleIcon.getMinimumWidth(), maleIcon.getMinimumHeight());
			Drawable femaleIcon = getResources().getDrawable(R.drawable.business_card_female_icon);
			femaleIcon.setBounds(0, 0, femaleIcon.getMinimumWidth(), femaleIcon.getMinimumHeight());
			if (mUser.getGender().equals(User.MALE)) {
				
				mNicknameTxt.setCompoundDrawables(null, null, maleIcon, null);
			}
			else {
				mNicknameTxt.setCompoundDrawables(null, null, femaleIcon, null);
			}
		}
		
		mRemarkTxt.setText(mUser.getRemark());
		mLocationTxt.setText(mUser.getLocation());
		
		String s = StringUtil.getCellphoneByName(mUser.getName());
		if (mUser.getContactName() != null) {
			s += " (" + mUser.getContactName() + ")";
		}
		mCellphoneTxt.setText(s);
		
		if (mUser.getHeadImg() != null) {
			mAvatarImg.setImageBitmap(mUser.getHeadImg());
		} else {
			if (mUser.getGender() == null) {
				mAvatarImg.setImageResource(R.drawable.default_avatar_male);
				
			} else {
				if (mUser.getGender().equals(User.MALE)) {
					mAvatarImg.setImageResource(R.drawable.default_avatar_male);
				}
				else {
					mAvatarImg.setImageResource(R.drawable.default_avatar_female);
				}
			}
		}
		
		if(mShowType == FRIEND_MODE) {
			mFriendOperLayout.setVisibility(View.VISIBLE);
			mAddUserBtn.setVisibility(View.GONE);
		}
		else {
			mFriendOperLayout.setVisibility(View.GONE);
			mAddUserBtn.setVisibility(View.VISIBLE);
		}
		
	}
}
