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
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;
import com.view.asim.util.FaceConversionUtil;
import com.view.asim.util.ImageUtil;
import com.view.asim.util.StringUtil;
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
	public static final int timer = 5;
	
	private ChatMessage message = null;
			
	private TextView title = null;
	private ImageView previewImage = null;
	private ImageView burnImage = null;
	private ImageView play = null;
	private TextView previewText;
	private Button confirm = null;
	private Context context;

	private MediaPlayer mMediaPlayer = new MediaPlayer();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Make us non-modal, so that others can receive touch events.
	    getWindow().setFlags(LayoutParams.FLAG_NOT_FOCUSABLE, LayoutParams.FLAG_NOT_FOCUSABLE);

		setContentView(R.layout.custom_dialog);
		init();
	}

	private void init() {
		getEimApplication().addActivity(this);
		message = (ChatMessage) getIntent().getParcelableExtra(ChatMessage.IMMESSAGE_KEY);
		
    	Intent intent = new Intent();
        intent.putExtra(ChatMessage.IMMESSAGE_KEY, message);

        setResult(RESULT_OK, intent);

        context = this;

		burnImage = (ImageView) findViewById(R.id.burn_image);
		
		title = (TextView) findViewById(R.id.title);
		previewImage = (ImageView) findViewById(R.id.preview_image);
		play = (ImageView) findViewById(R.id.preview_video_play);
		previewText = (TextView) findViewById(R.id.preview_text);
		confirm = (Button) findViewById(R.id.cfm_btn);
		confirm.setText("即将销毁");
		
		if (message.getType().equals(ChatMessage.CHAT_TEXT)) {
			previewImage.setVisibility(View.GONE);
			play.setVisibility(View.GONE);
			previewText.setVisibility(View.VISIBLE);
			title.setText("文字消息");
			
			SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(context, message.getContent(), 50);

			previewText.setText(spannableString);
		}
		else if (message.getType().equals(ChatMessage.CHAT_IMAGE)) {
			previewImage.setVisibility(View.VISIBLE);
			play.setVisibility(View.INVISIBLE);
			previewText.setVisibility(View.INVISIBLE);
			title.setText("图片消息");
			Bitmap bitmap;
			if (message.getAttachment().getSrcUri().length() > 0)
				bitmap = ImageUtil.getImageThumbnail(message.getAttachment().getSrcUri(), 200, 200);
			else 
				bitmap = ImageUtil.getImageThumbnail(message.getAttachment().getThumbUri(), 200, 200);

			previewImage.setImageBitmap(bitmap);
			previewImage.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.putExtra(ChatMessage.IMMESSAGE_KEY, message);
					intent.setClass(context, ImagePreviewActivity.class);
					startActivity(intent);
				}
			});  
			
		}
		else if (message.getType().equals(ChatMessage.CHAT_AUDIO)) {
			previewImage.setVisibility(View.INVISIBLE);
			play.setVisibility(View.VISIBLE);
			previewText.setVisibility(View.INVISIBLE);
			title.setText("语音消息");
			play.setImageResource(R.drawable.btn_chat_voicemessage_n);
			String uri = message.getAttachment().getSrcUri();

			if (uri != null) {
				playMusic(uri);
			}

			/*
			play.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					String uri = message.getAttachment().getSrcUri();

					if (uri != null) {
						playMusic(uri) ;
					}
				}
			});  
			*/
		}
		else if (message.getType().equals(ChatMessage.CHAT_VIDEO)) {
			previewImage.setVisibility(View.VISIBLE);
			play.setVisibility(View.VISIBLE);
			previewText.setVisibility(View.INVISIBLE);
			title.setText("视频消息");
			
			Bitmap bitmap = ImageUtil.getVideoThumbnail(message.getAttachment().getSrcUri(), 100, 100,
					Images.Thumbnails.MINI_KIND);
			previewImage.setImageBitmap(bitmap);
			play.setOnClickListener(new View.OnClickListener() {
	
				@Override
				public void onClick(View v) {
					String path = message.getAttachment().getSrcUri();
	
					Intent intent = new Intent(Intent.ACTION_VIEW);
			        String type = "video/*";
			        Uri uri = Uri.parse(path);
			        intent.setDataAndType(uri, type);
			        startActivity(intent);
				}
			});  
		}
		
		
	}
	
	@Override
	public void onBackPressed() {
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (!message.getType().equals(ChatMessage.CHAT_AUDIO)) {
			new BurnTimerThread(timer, new ExpiryTimerListener() {
	
				@Override
				public void onTick(int sec) {
					final int t = sec;
					runOnUiThread(new Runnable()    
			        {    
			            public void run()    
			            {    
			            	confirm.setText("" + t);
			            }    
			    
			        });
				}
	
				@Override
				public void onEnd() {
					runOnUiThread(new Runnable()    
			        {    
			            public void run()    
			            {    
			            	confirm.setText("销毁");
			            	Burn();
			            }    
			        });				
				}
				
			}).start();
			confirm.setText("" + timer);

		}
	}
	// 自动销毁倒计时线程
	private class BurnTimerThread extends Thread {
		private int expiry = -1;
		private ExpiryTimerListener listener;
		
		public BurnTimerThread(int sec, ExpiryTimerListener listener) {
			this.expiry = sec;
			this.listener = listener; 
		}
		
		@Override
		public void run() {
			Log.d(TAG, "BurnTimerThread");
			while(expiry > 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				expiry = expiry - 1;
				listener.onTick(expiry);
			}
			listener.onEnd();
		}
	}
		
		
	private void Burn() {

		previewImage.setVisibility(View.INVISIBLE);
		play.setVisibility(View.INVISIBLE);
		previewText.setVisibility(View.INVISIBLE);
		
		EndAnimationDrawable burnAnim = new EndAnimationDrawable(
				(AnimationDrawable) context.getResources().getDrawable(R.drawable.burn_anim)
				, new OnAnimationChangeListener() {
			
			@Override
			public void onAnimationEnd() {
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
					Burn();
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}