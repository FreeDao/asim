package org.sipdroid.sipua.ui;

/**
 * VoIP 通话界面
 * @author xuweinan
 */

import java.util.HashMap;

import org.sipdroid.media.RtpStreamReceiver;
import org.sipdroid.media.RtpStreamSender;
import org.sipdroid.sipua.UserAgent;
import org.sipdroid.sipua.phone.Call;
import org.sipdroid.sipua.phone.Phone;

import com.view.asim.R;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.model.User;
import com.view.asim.util.DateUtil;
import com.view.asim.util.StringUtil;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;

public class InCallScreen extends ActivitySupport implements SensorEventListener {
	
	private final static String TAG = "InCallScreen";
	
	final int MSG_ANSWER = 1;
	final int MSG_ANSWER_SPEAKER = 2;
	final int MSG_BACK = 3;
	final int MSG_TICK = 4;
	final int MSG_POPUP = 5;
	final int MSG_ACCEPT = 6;
	final int MSG_ACCEPT_FORCE = 7;
	
	final int SCREEN_OFF_TIMEOUT = 12000;
	
	private TextView mCallingTxt;
	private Chronometer mSessionDuration;
	private ImageView mAvatar;
	private TextView mNickname, mCallType;
	private Button mCallingCancelBtn;
	private Button mSessionEndBtn;
	private Button mAcceptBtn;
	private Button mRejectBtn;
	private ImageButton mMuteBtn;
	private ImageButton mSpeakerBtn;
	private View mCalledLayout, mInCallLayout;
	private Context context = this;
	
	public static boolean started;
	SensorManager sensorManager;
    Sensor proximitySensor;
    int oldTimeout;
    boolean first;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.sip_voice_call);
		
		init();
	}
	
	private void init() {
		mCallingTxt = (TextView) findViewById(R.id.call_status_txt);
		mSessionDuration = (Chronometer) findViewById(R.id.call_duration_chronometer);
		mAvatar = (ImageView) findViewById(R.id.user_avatar_img);
		mNickname = (TextView) findViewById(R.id.user_nickname_txt);
		mCallType = (TextView) findViewById(R.id.call_type_txt);

		mCallingCancelBtn = (Button) findViewById(R.id.calling_cancel_btn);
		mCallingCancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				reject();
			}
		});
		
		mSessionEndBtn = (Button) findViewById(R.id.incall_stop_btn);
		mSessionEndBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				reject();
			}
		});
		
		mAcceptBtn = (Button) findViewById(R.id.accept_btn);
		mAcceptBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				answer();
			}
		});
		
		mRejectBtn = (Button) findViewById(R.id.reject_btn);
		mRejectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				reject();
			}
		});
		
		mMuteBtn = (ImageButton) findViewById(R.id.mute_btn);
		mMuteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean muted = Receiver.engine(context).togglemute();
				if (muted) {
					mMuteBtn.setImageResource(R.drawable.voip_mute_icon_pressed);
				}
				else {
					mMuteBtn.setImageResource(R.drawable.voip_mute_icon_normal);
				}
			}
		});
		
		mSpeakerBtn = (ImageButton) findViewById(R.id.speaker_btn);
		mSpeakerBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Receiver.engine(context).speaker(RtpStreamReceiver.speakermode == AudioManager.MODE_NORMAL?
						AudioManager.MODE_IN_CALL:AudioManager.MODE_NORMAL);
				if(RtpStreamReceiver.speakermode == AudioManager.MODE_NORMAL) {
					mSpeakerBtn.setImageResource(R.drawable.voip_speaker_icon_pressed);
				}
				else {
					mSpeakerBtn.setImageResource(R.drawable.voip_speaker_icon_normal);
				}
			}
		});
		
		mInCallLayout = findViewById(R.id.incall_layout);
		mCalledLayout = findViewById(R.id.called_layout);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if(!android.os.Build.BRAND.equalsIgnoreCase("archos"))
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "call state " + Receiver.call_state);
		refreshViewOnAUKeyStatusChange();
		
		User u = getUser(Receiver.call_state);
		if (u != null) {
			setAvatarImage(mAvatar, u);
			mNickname.setText(u.getNickName());
		}
		switch (Receiver.call_state) {
		case UserAgent.UA_STATE_INCOMING_CALL:
			mInCallLayout.setVisibility(View.GONE);
			mCallingCancelBtn.setVisibility(View.GONE);
			mCalledLayout.setVisibility(View.VISIBLE);
			break;
			
		case UserAgent.UA_STATE_INCALL:
			mInCallLayout.setVisibility(View.VISIBLE);
			mCallingCancelBtn.setVisibility(View.GONE);
			mCalledLayout.setVisibility(View.GONE);
			mCallingTxt.setVisibility(View.GONE);
			mSessionDuration.start();
			mSessionDuration.setVisibility(View.VISIBLE);
			break;

		case UserAgent.UA_STATE_OUTGOING_CALL:
			mInCallLayout.setVisibility(View.GONE);
			mCallingCancelBtn.setVisibility(View.VISIBLE);
			mCalledLayout.setVisibility(View.GONE);
			break;
			
		case UserAgent.UA_STATE_IDLE:
			mSessionDuration.stop();
			moveBack();
			break;		
		}
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.AUKEY_STATUS_UPDATE);

		registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (Constant.AUKEY_STATUS_UPDATE.equals(action)) {
				refreshViewOnAUKeyStatusChange();
			}
		}
	};
	
	private void refreshViewOnAUKeyStatusChange() {
		View screen = findViewById(R.id.call_screen_layout);
		if (AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED)) {
			screen.setBackgroundResource(R.drawable.account_guidance_bg);
			mCallType.setText("小密在线，密话模式");
			mCallType.setVisibility(View.VISIBLE);
		}
		else {
			screen.setBackgroundResource(R.drawable.radar_background);
			mCallType.setText("小密离线，明话模式");
			mCallType.setVisibility(View.VISIBLE);
		}
			
	}
	
	private User getUser(int state) {
		if (Receiver.ccCall != null) {
			String addr = null;
			if (state == UserAgent.UA_STATE_OUTGOING_CALL) {
				addr = "u" + Receiver.ccCall.getEarliestConnection().getAddress();
			}
			else if (state == UserAgent.UA_STATE_INCOMING_CALL){
				addr = StringUtil.getJidByName("u" + Receiver.ccCall.getEarliestConnection().getAddress(), getLoginConfig().getXmppHost());
			}
			else {
				return null;
			}
			return ContacterManager.contacters.get(addr);
			
		}
		return null;
	}
	
	private void setAvatarImage(ImageView v, User u) {
		if (u.getHeadImg() != null) {
			v.setImageBitmap(u.getHeadImg());
		} else {
			if (u.getGender() == null) {
				v.setImageResource(R.drawable.default_avatar_male);
				
			} else {
				if (u.getGender().equals(User.MALE)) {
					v.setImageResource(R.drawable.default_avatar_male);
				}
				else {
					v.setImageResource(R.drawable.default_avatar_female);
				}
			}
		
		}
	}
	
	void moveBack() {
		/*
		if (Receiver.ccConn != null && !Receiver.ccConn.isIncoming()) {
			// after an outgoing call don't fall back to the contact
			// or call log because it is too easy to dial accidentally from there
	        startActivity(Receiver.createHomeIntent());
		}
		*/
		onStop();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if (Receiver.call_state == UserAgent.UA_STATE_IDLE)
			finish();
		started = false;
		sensorManager.unregisterListener(this);

	}
	
	@Override
	public void onStart() {
		super.onStart();
	    started = true;
	    first = true;
	    sensorManager.registerListener(this,proximitySensor,SensorManager.SENSOR_DELAY_NORMAL);

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_DOWN:
        case KeyEvent.KEYCODE_VOLUME_UP:
        	if (Receiver.call_state == UserAgent.UA_STATE_INCOMING_CALL) {
        		Receiver.stopRingtone();
        		return true;
        	}
        	RtpStreamReceiver.adjust(keyCode,true);
        	return true;
        }
        
        return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_DOWN:
        case KeyEvent.KEYCODE_VOLUME_UP:
        	RtpStreamReceiver.adjust(keyCode,false);
        	return true;
        }
		Receiver.pstn_time = 0;
		return false;
	}
	
	public void reject() {
		if (Receiver.ccCall != null) {
			Receiver.stopRingtone();
			Receiver.ccCall.setState(Call.State.DISCONNECTED);
		}
        (new Thread() {
			public void run() {
        		Receiver.engine(context).rejectcall();
			}
		}).start();   	
    }
	
	public void answer() {
        (new Thread() {
			public void run() {
				Receiver.engine(context).answercall();
			}
		}).start();   
		if (Receiver.ccCall != null) {
			Receiver.ccCall.setState(Call.State.ACTIVE);
			Receiver.ccCall.base = SystemClock.elapsedRealtime();
		}
	}
	
	void setScreenBacklight(float a) {
        WindowManager.LayoutParams lp = getWindow().getAttributes(); 
        lp.screenBrightness = a; 
        getWindow().setAttributes(lp);		
	}
	
	void screenOff(boolean off) {
        ContentResolver cr = getContentResolver();
        
        if (proximitySensor != null)
        	return;
        if (off) {
        	if (oldTimeout == 0) {
        		oldTimeout = Settings.System.getInt(cr, Settings.System.SCREEN_OFF_TIMEOUT, 60000);
	        	Settings.System.putInt(cr, Settings.System.SCREEN_OFF_TIMEOUT, SCREEN_OFF_TIMEOUT);
        	}
        } else {
        	if (oldTimeout == 0 && Settings.System.getInt(cr, Settings.System.SCREEN_OFF_TIMEOUT, 60000) == SCREEN_OFF_TIMEOUT)
        		oldTimeout = 60000;
        	if (oldTimeout != 0) {
	        	Settings.System.putInt(cr, Settings.System.SCREEN_OFF_TIMEOUT, oldTimeout);
	        	oldTimeout = 0;
        	}
        }
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (first) {
			first = false;
			return;
		}
		float distance = event.values[0];
        boolean active = (distance >= 0.0 && distance < PROXIMITY_THRESHOLD && distance < event.sensor.getMaximumRange());
		if (Receiver.call_state == UserAgent.UA_STATE_HOLD)
			active = false;
        pactive = active;
        pactivetime = SystemClock.elapsedRealtime();
        if (!active) {
     		//mHandler.sendEmptyMessageDelayed(MSG_ACCEPT, 1000);
     		return;
        }
        //mHandler.removeMessages(MSG_ACCEPT);
        setScreenBacklight((float) 0.1);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        closeOptionsMenu();
        ContentResolver cr = getContentResolver();
		if (!hapticset) {
			haptic = Settings.System.getInt(cr, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1);
			hapticset = true;
		}
		Settings.System.putInt(cr, Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
	}
	
	static final float PROXIMITY_THRESHOLD = 5.0f;
	public static boolean pactive;
	public static long pactivetime;
	static int haptic;
	static boolean hapticset;

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}
