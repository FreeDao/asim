package com.view.asim.activity.im;


import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import com.view.asim.activity.ActivitySupport;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.UserManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;
import com.view.asim.utils.FaceConversionUtil;
import com.view.asim.utils.ImageUtil;
import com.view.asim.utils.StringUtil;
import com.view.asim.view.EndAnimationDrawable;
import com.view.asim.view.OnAnimationChangeListener;
import com.view.asim.worker.ExpiryTimerListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.view.asim.R;

/**
 * 
 * 阅后即焚信息查看
 * 
 * @author xuweinan
 */
public class BurnMsgViewActivity extends ActivitySupport {
	public static final String TAG = "BurnMsgViewActivity";
	public static final int timer = 10;
	
	private ChatMessage message = null;
			
	private TextView title = null;
	private ImageView burnImage = null;
	private ImageView voicePlayImage = null;
	
	private TextView previewText;
	private Button confirm = null;
	private Context context;
	
	private boolean musicPlaying = false;
	private boolean destroyTimerStarting = false;

	private MediaPlayer mMediaPlayer = new MediaPlayer();
	private BurnTimerThread burnThread = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    //getWindow().setFlags(LayoutParams.FLAG_NOT_FOCUSABLE, LayoutParams.FLAG_NOT_FOCUSABLE);

		setContentView(R.layout.custom_dialog);
		init();
	}

	private void init() {
		getEimApplication().addActivity(this);
		message = (ChatMessage) getIntent().getParcelableExtra(ChatMessage.IMMESSAGE_KEY);
		
        context = this;

		burnImage = (ImageView) findViewById(R.id.burn_image);
		voicePlayImage = (ImageView) findViewById(R.id.voice_play_img);
		
		title = (TextView) findViewById(R.id.title);
		previewText = (TextView) findViewById(R.id.preview_text);
		
		if (message.getType().equals(ChatMessage.CHAT_TEXT)) {
			previewText.setVisibility(View.VISIBLE);
			title.setText("文字消息");
			
			SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(context, message.getContent(), 50);

			previewText.setText(spannableString);
		}
		else if (message.getType().equals(ChatMessage.CHAT_AUDIO)) {
			previewText.setVisibility(View.INVISIBLE);
			title.setText("语音消息");
		}
		
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
		message = (ChatMessage) intent.getParcelableExtra(ChatMessage.IMMESSAGE_KEY);
        super.onNewIntent(intent);
	}
	
//	@Override
//	public void onBackPressed() {
//		if (message.getDestroy().equals(IMMessage.BURN_AFTER_READ)) {
//			if (burnThread != null) {
//				burnThread.cancel();
//			}
//			burnMessage();
//			
//		}
//		super.onBackPressed();
//	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (!message.getType().equals(ChatMessage.CHAT_AUDIO)) {
			if (!destroyTimerStarting) {
				burnThread = new BurnTimerThread(timer, new ExpiryTimerListener() {
		
					@Override
					public void onTick(int sec) {
						final int t = sec;
						runOnUiThread(new Runnable()    
				        {    
				            public void run()    
				            {    
				    			title.setText("文字消息(" + t + ")");
				            }    
				    
				        });
					}
		
					@Override
					public void onEnd() {
						destroyTimerStarting = false;

						runOnUiThread(new Runnable()    
				        {    
				            public void run()    
				            {    
				            	title.setText("销毁");
				            	burnAnim();
				            }    
				        });				
					}

					@Override
					public void onCancel() {
					}
					
				});
				burnThread.start();
				destroyTimerStarting = true;
			}

		} else {
			String uri = message.getAttachment().getSrcUri();

			if (!musicPlaying) {
				if (uri != null) {
					playMusic(uri);
					musicPlaying = true;
				}
				playTrackAnim();
			}
		}
	}
	
	// 自动销毁倒计时线程
	private class BurnTimerThread extends Thread {
		private int expiry = -1;
		private ExpiryTimerListener listener;
		private boolean canceled = false;
		
		public BurnTimerThread(int sec, ExpiryTimerListener listener) {
			this.expiry = sec;
			this.listener = listener; 
		}
		
		public void cancel() {
			canceled = true;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "BurnTimerThread start");
			while(expiry > 0) {
				if (canceled) {
					listener.onCancel();
					Log.d(TAG, "BurnTimerThread cancel");
					return;
				}
				listener.onTick(expiry);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				expiry = expiry - 1;
			}
			listener.onEnd();
			Log.d(TAG, "BurnTimerThread end");

		}
	}
	
	private void playTrackAnim() {

		previewText.setVisibility(View.INVISIBLE);
		burnImage.setVisibility(View.INVISIBLE);
		
		AnimationDrawable playAnim = (AnimationDrawable) context.getResources().getDrawable(R.drawable.burn_voice_play_anim);
		
		playAnim.setOneShot(false);
		
		voicePlayImage.setBackgroundDrawable(playAnim);
		voicePlayImage.setVisibility(View.VISIBLE);
		playAnim.start();
	}
		
		
	private void burnAnim() {

		previewText.setVisibility(View.INVISIBLE);
		voicePlayImage.setVisibility(View.INVISIBLE);
		
		EndAnimationDrawable burnAnim = new EndAnimationDrawable(
				(AnimationDrawable) context.getResources().getDrawable(R.drawable.burn_anim)
				, new OnAnimationChangeListener() {
			
			@Override
			public void onAnimationEnd() {
				burnMessage();
		        finish();			        
			}
	    });
		
	    burnAnim.setOneShot(true);
		
		burnImage.setBackgroundDrawable(burnAnim);
		burnImage.setVisibility(View.VISIBLE);
		burnAnim.start();
	}
	
	private void playMusic(String name) {
		try {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(name);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					burnThread = new BurnTimerThread(timer / 2, new ExpiryTimerListener() {
						
						@Override
						public void onTick(int sec) {
							final int t = sec;
							runOnUiThread(new Runnable()    
					        {    
					            public void run()    
					            {    
					    			title.setText("语音消息(" + t + ")");
					            }    
					    
					        });
						}
			
						@Override
						public void onEnd() {
							runOnUiThread(new Runnable()    
					        {    
					            public void run()    
					            {    
					            	title.setText("销毁");
					            	burnAnim();
					            }    
					        });				
						}

						@Override
						public void onCancel() {
							// TODO Auto-generated method stub
							
						}
						
					});
					burnThread.start();
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void onPause() {
		if (message.getDestroy().equals(IMMessage.BURN_AFTER_READ)) {
			if (burnThread == null) {
				burnMessage();
			}
			else
			if (burnThread != null && destroyTimerStarting) {
				burnThread.cancel();
				burnMessage();
			}
			
			destroyTimerStarting = false;
		}
		super.onPause();
	}
	
	private void burnMessage() {
    	Log.d(TAG, "remove message id " + message.getId() + ", " + message.getContent());
    	showToast("消息已销毁");
    	MessageManager.getInstance().delChatHisById(message.getId());
	}
	
}