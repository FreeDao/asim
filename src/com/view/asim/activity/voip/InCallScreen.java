package com.view.asim.activity.voip;

/**
 * VoIP 通话界面
 * @author xuweinan
 */

import java.util.Timer;
import java.util.TimerTask;

import com.csipsimple.api.ISipService;
import com.csipsimple.api.MediaState;
import com.csipsimple.api.SipCallSession;
import com.csipsimple.api.SipConfigManager;
import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipUri;
import com.csipsimple.api.SipCallSession.StatusCode;
import com.csipsimple.api.SipUri.ParsedSipContactInfos;
import com.csipsimple.service.SipService;
import com.csipsimple.utils.CallsUtils;
import com.csipsimple.utils.PreferencesProviderWrapper;
import com.csipsimple.utils.keyguard.KeyguardWrapper;
import com.view.asim.R;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.activity.voip.CallProximityManager.ProximityDirector;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.model.User;
import com.view.asim.util.CallUtil;
import com.view.asim.util.StringUtil;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.provider.CallLog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class InCallScreen extends ActivitySupport implements ProximityDirector, IOnCallActionTrigger{
	
	private final static String TAG = "InCallScreen";
	
	private User mUser;
	final int SCREEN_OFF_TIMEOUT = 12000;
    private static final int QUIT_DELAY = 2000;
    private String mUKeyState = AUKeyManager.DETACHED;
    
    private boolean isIncoming = false;
    private long callStart = 0;
    private long callDuration = 0;
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
	public static boolean started;
	SensorManager sensorManager;
    Sensor proximitySensor;
    int oldTimeout;
    boolean first;
    
    private SipCallSession callInfo = null;
    private PreferencesProviderWrapper prefsWrapper;
    private PowerManager powerManager;
    private WakeLock wakeLock;
    private Timer quitTimer;
    private CallProximityManager proximityManager;
    private KeyguardWrapper keyguardManager;
    private boolean useAutoDetectSpeaker = false;
    private Object callMutex = new Object();
    private MediaState lastMediaState;
    private boolean mMute = false;
    private boolean mSpeakerOn = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.sip_voice_call);
		
		init();
	}
	
	/**
     * Service binding
     */
    private boolean serviceConnected = false;
    private ISipService service;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ISipService.Stub.asInterface(arg1);
            //callsInfo = service.getCalls();
            serviceConnected = true;

            runOnUiThread(new UpdateUIFromCallRunnable());
            runOnUiThread(new UpdateUIFromMediaRunnable());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceConnected = false;
            //callsInfo = null;
        }
    };
	
	private void init() {
		callInfo = getIntent().getParcelableExtra(SipManager.EXTRA_CALL_INFO);
        prefsWrapper = new PreferencesProviderWrapper(this);
        bindService(new Intent(this, SipService.class), connection, Context.BIND_AUTO_CREATE);

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
                "com.csipsimple.onIncomingCall");
        wakeLock.setReferenceCounted(false);
        
        proximityManager = new CallProximityManager(this, this);
        keyguardManager = KeyguardWrapper.getKeyguardManager(this);
        
        if (prefsWrapper.getPreferenceBooleanValue(SipConfigManager.PREVENT_SCREEN_ROTATION)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        if (quitTimer == null) {
            quitTimer = new Timer("Quit-timer");
        }
        
        useAutoDetectSpeaker = prefsWrapper.getPreferenceBooleanValue(SipConfigManager.AUTO_DETECT_SPEAKER);
        proximityManager.startTracking();
        takeKeyEvents(true);
        
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.AUKEY_STATUS_UPDATE);
		filter.addAction(Constant.ROSTER_UPDATED);
		filter.addAction(Constant.ROSTER_PRESENCE_CHANGED);
		filter.addAction(SipManager.ACTION_SIP_CALL_CHANGED);
		filter.addAction(SipManager.ACTION_SIP_MEDIA_CHANGED);

		registerReceiver(receiver, filter);
        
		mCallingTxt = (TextView) findViewById(R.id.call_status_txt);
		mSessionDuration = (Chronometer) findViewById(R.id.call_duration_chronometer);
		mAvatar = (ImageView) findViewById(R.id.user_avatar_img);
		mNickname = (TextView) findViewById(R.id.user_nickname_txt);
		mCallType = (TextView) findViewById(R.id.call_type_txt);

		mCallingCancelBtn = (Button) findViewById(R.id.calling_cancel_btn);
		mCallingCancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	            onTrigger(TERMINATE_CALL);
			}
		});
		
		mSessionEndBtn = (Button) findViewById(R.id.incall_stop_btn);
		mSessionEndBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	            onTrigger(TERMINATE_CALL);
			}
		});
		
		mAcceptBtn = (Button) findViewById(R.id.accept_btn);
		mAcceptBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	            onTrigger(TAKE_CALL);
			}
		});
		
		mRejectBtn = (Button) findViewById(R.id.reject_btn);
		mRejectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	            onTrigger(REJECT_CALL);
			}
		});
		
		mMuteBtn = (ImageButton) findViewById(R.id.mute_btn);
		mMuteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMute) {
		            onTrigger(MUTE_OFF);
				}
				else {
		            onTrigger(MUTE_ON);
				}

			}
		});
		
		mSpeakerBtn = (ImageButton) findViewById(R.id.speaker_btn);
		mSpeakerBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSpeakerOn) {
		            onTrigger(SPEAKER_OFF);
				}
				else {
		            onTrigger(SPEAKER_ON);
				}

			}
		});
		
		mUser = getUser(callInfo);
		if (mUser != null) {
			setAvatarImage(mAvatar, mUser);
			mNickname.setText(mUser.getNickName());
		}
		
		mInCallLayout = findViewById(R.id.incall_layout);
		mCalledLayout = findViewById(R.id.called_layout);
		
		mUKeyState = callInfo.getSecurity();

		setCallState(callInfo);
	}
	
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        Log.i(TAG, "New intent is launched");
        super.onNewIntent(intent);
    }
	
	@Override
	protected void onResume() {
		super.onResume();
        runOnUiThread(new UpdateUIFromCallRunnable());
        
        refreshViewOnAUKeyStatusChange();

	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
    @Override
    protected void onDestroy() {

        if (quitTimer != null) {
            quitTimer.cancel();
            quitTimer.purge();
            quitTimer = null;
        }
        try {
            unbindService(connection);
        } catch (Exception e) {
            // Just ignore that
        }
        service = null;
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        proximityManager.stopTracking();
        proximityManager.release(0);
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
        	e.printStackTrace();
        }
        
        super.onDestroy();
    }
    
	public void setMediaState(MediaState mediaState) {
		lastMediaState = mediaState;

        // Mic
        if(lastMediaState == null) {
        	mMute = false;
        }else {
        	mMute = lastMediaState.isMicrophoneMute;
        }
		if (mMute) {
			mMuteBtn.setImageResource(R.drawable.voip_mute_icon_pressed);
		}
		else {
			mMuteBtn.setImageResource(R.drawable.voip_mute_icon_normal);
		}      

        // Speaker
        Log.i(TAG, ">> Speaker " + lastMediaState);
        if(lastMediaState == null) {
            mSpeakerOn = false;
        }else {
            Log.d(TAG, ">> Speaker " + lastMediaState.isSpeakerphoneOn);
            mSpeakerOn = lastMediaState.isSpeakerphoneOn;
        }
		if(mSpeakerOn) {
			mSpeakerBtn.setImageResource(R.drawable.voip_speaker_icon_pressed);
		}
		else {
			mSpeakerBtn.setImageResource(R.drawable.voip_speaker_icon_normal);
		}
		
	}
    
	private void setCallState(SipCallSession callInfo) {
		if(callInfo == null) {
			Log.i(TAG, "callinfo is null");
			return;
		}
		updateElapsedTimer(callInfo);
		
		int state = callInfo.getCallState();
		Log.i(TAG, "Call Mode is : " + state);
		
		mCallingTxt.setText(CallsUtils.getStringCallState(callInfo, InCallScreen.this));
		mCallingTxt.setVisibility(View.VISIBLE);
		
		switch (state) {
		case SipCallSession.InvState.INCOMING:
			mInCallLayout.setVisibility(View.GONE);
			mCallingCancelBtn.setVisibility(View.GONE);
			mCalledLayout.setVisibility(View.VISIBLE);
			break;
			
		case SipCallSession.InvState.CALLING:
			mInCallLayout.setVisibility(View.GONE);
			mCallingCancelBtn.setVisibility(View.VISIBLE);
			mCalledLayout.setVisibility(View.GONE);
			break;
			
		case SipCallSession.InvState.CONNECTING:
		case SipCallSession.InvState.CONFIRMED:
			mInCallLayout.setVisibility(View.VISIBLE);
			mCallingCancelBtn.setVisibility(View.GONE);
			mCalledLayout.setVisibility(View.GONE);
			mCallingTxt.setVisibility(View.GONE);
			break;
			
		case SipCallSession.InvState.NULL:
		case SipCallSession.InvState.DISCONNECTED:
			mCallingTxt.setText(CallUtil.getStringByStatusCode(callInfo.getLastStatusCode()));
			mSessionEndBtn.setVisibility(View.INVISIBLE);
			mMuteBtn.setVisibility(View.INVISIBLE);
			mSpeakerBtn.setVisibility(View.INVISIBLE);
			break;
			
		case SipCallSession.InvState.EARLY:
		default:
			break;
		}
		
	}
	
    private void updateElapsedTimer(SipCallSession callInfo) {

        if (callInfo == null) {
        	mSessionDuration.stop();
        	mSessionDuration.setVisibility(View.VISIBLE);
        	callDuration = System.currentTimeMillis() - callStart;
            return;
        }

        mSessionDuration.setBase(callInfo.getConnectStart());
        
        int state = callInfo.getCallState();
        switch (state) {
            case SipCallSession.InvState.INCOMING:
            case SipCallSession.InvState.CALLING:
            case SipCallSession.InvState.EARLY:
            case SipCallSession.InvState.CONNECTING:
            	mSessionDuration.setVisibility(View.GONE);
                break;
            case SipCallSession.InvState.CONFIRMED:
                Log.v(TAG, "we start the timer now ");
                if(callInfo.isLocalHeld()) {
                	mSessionDuration.stop();
                	callDuration = System.currentTimeMillis() - callStart;
                	mSessionDuration.setVisibility(View.GONE);
                }else {
                	mSessionDuration.start();
                	mSessionDuration.setVisibility(View.VISIBLE);
                	callStart = System.currentTimeMillis();
                }
                break;
            case SipCallSession.InvState.NULL:
            case SipCallSession.InvState.DISCONNECTED:
            	mSessionDuration.stop();
            	if (callStart > 0) {
            		callDuration = System.currentTimeMillis() - callStart;
            	}
            	mSessionDuration.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }

    }
    
    private class UpdateUIFromCallRunnable implements Runnable {
        
        @Override
        public void run() {
            // Current call is the call emphasis by the UI.
            SipCallSession mainCallInfo = callInfo;
    
            int mainsCalls = 0;
            int heldsCalls = 0;
            /*

            synchronized (callMutex) {
                if (callsInfo != null) {
                    for (SipCallSession callInfo : callsInfo) {
                        Log.w(TAG,
                                "We have a call " + callInfo.getCallId() + " / " + callInfo.getCallState()
                                        + "/" + callInfo.getMediaStatus());
        
                        if (!callInfo.isAfterEnded()) {
                            if (callInfo.isLocalHeld()) {
                                heldsCalls++;
                            } else {
                                mainsCalls++;
                            }
                        }
                        mainCallInfo = getPrioritaryCall(callInfo, mainCallInfo);
                    }
                }
            }
            
            if ((mainsCalls + heldsCalls) >= 1) {
                setCallState(mainCallInfo);
            } else {
                setCallState(null);
            }
            */

            //mainCallInfo = callsInfo[0];
            setCallState(mainCallInfo);
            
            if (mainCallInfo != null) {
                Log.w(TAG, "Active call is " + mainCallInfo.getCallId());
                Log.w(TAG, "Update ui from call " + mainCallInfo.getCallId() + " state "
                        + CallsUtils.getStringCallState(mainCallInfo, InCallScreen.this)
                        + ", incoming " + mainCallInfo.isIncoming());
                int state = mainCallInfo.getCallState();
    
                switch (state) {
                    case SipCallSession.InvState.INCOMING:
                    case SipCallSession.InvState.EARLY:
                    case SipCallSession.InvState.CALLING:
                    case SipCallSession.InvState.CONNECTING:
                    	isIncoming = mainCallInfo.isIncoming();
                    	
                        Log.d(TAG, "Acquire wake up lock");
                        if (wakeLock != null && !wakeLock.isHeld()) {
                            wakeLock.acquire();
                        }
                        break;
                    case SipCallSession.InvState.CONFIRMED:
                        break;
                    case SipCallSession.InvState.NULL:
                    case SipCallSession.InvState.DISCONNECTED:
                        Log.d(TAG, "Active call " + mainCallInfo.getCallId() + " session is disconnected or null wait for quit..." + mainCallInfo.getLastStatusCode()
                        		+ ", incoming:" + isIncoming);
                        printCallLog();
                        
                        if(judgeMissedCall(mainCallInfo, isIncoming)) {
    	        			NoticeManager.getInstance().dispatchMissedCallNotify(mUser);
                		}
                        
                        delayedQuit();
                        return;
    
                }
                
                Log.d(TAG, "we leave the update ui function");
            }
            
            proximityManager.updateProximitySensorMode();
            /*
            if (heldsCalls + mainsCalls == 0) {
                delayedQuit();
            }
            */
        }
    }

    private boolean judgeMissedCall(SipCallSession call, boolean isIncoming) {
    	Log.w(TAG, "call id " + call.getCallId() + ", incoming " + isIncoming + ", duration " + callDuration + ", status " + call.getLastStatusCode() + ", reason " + call.getLastReasonCode());
    	boolean isMissedCall = false;
    	if(isIncoming) {
    		isMissedCall = true;
			if(callDuration > 0) {
				isMissedCall = false;
			}else if(call.getLastStatusCode() == SipCallSession.StatusCode.DECLINE ||
			        call.getLastStatusCode() == SipCallSession.StatusCode.BUSY_HERE ||
			        call.getLastReasonCode() == 200) {
				isMissedCall = false;
			}
		}
    	
    	return isMissedCall;
    }

	private class UpdateUIFromMediaRunnable implements Runnable {
	    @Override
	    public void run() {
	        setMediaState(lastMediaState);
	        proximityManager.updateProximitySensorMode();
	    }
	}
	
	
	/*
    private SipCallSession getPrioritaryCall(SipCallSession call1, SipCallSession call2) {
        // We prefer the not null
        if (call1 == null) {
            return call2;
        } else if (call2 == null) {
            return call1;
        }
        // We prefer the one not terminated
        if (call1.isAfterEnded()) {
            return call2;
        } else if (call2.isAfterEnded()) {
            return call1;
        }
        // We prefer the one not held
        if (call1.isLocalHeld()) {
            return call2;
        } else if (call2.isLocalHeld()) {
            return call1;
        }
        // We prefer the older call 
        // to keep consistancy on what will be replied if new call arrives
        return (call1.getCallStart() > call2.getCallStart()) ? call2 : call1;
    }
    
    private SipCallSession getActiveCallInfo() {
        SipCallSession currentCallInfo = null;
        if (callsInfo == null) {
            return null;
        }
        for (SipCallSession callInfo : callsInfo) {
            currentCallInfo = getPrioritaryCall(callInfo, currentCallInfo);
        }
        return currentCallInfo;
    }
    */
	
    private synchronized void delayedQuit() {

        if (wakeLock != null && wakeLock.isHeld()) {
            Log.d(TAG, "Releasing wake up lock");
            wakeLock.release();
        }
        
        proximityManager.release(0);
        
//        activeCallsGrid.setVisibility(View.VISIBLE);
//        inCallControls.setVisibility(View.GONE);

        Log.d(TAG, "Start quit timer");
        if (quitTimer != null) {
            quitTimer.schedule(new QuitTimerTask(), QUIT_DELAY);
        } else {
            finish();
        }
    }
    
    private class QuitTimerTask extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "Run quit timer");
            finish();
        }
    };
    
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (Constant.AUKEY_STATUS_UPDATE.equals(action) || Constant.ROSTER_PRESENCE_CHANGED.equals(action) || Constant.ROSTER_UPDATED.equals(action)) {
//				if (!mUKeyState.equals(AUKeyManager.getInstance().getAUKeyStatus())) {
//					Log.w(TAG, "askey state changed (" + mUKeyState + " -> " + AUKeyManager.getInstance().getAUKeyStatus() + ").");
//					onTrigger(TERMINATE_CALL);
//				}
				mUKeyState = intent.getStringExtra(Constant.AUKEY_STATUS_KEY);
				mUser = getUser(callInfo);
				refreshViewOnAUKeyStatusChange();
			}
			else if (action.equals(SipManager.ACTION_SIP_CALL_CHANGED)) {
//                if (service != null) {
//                    try {
                        synchronized (callMutex) {
                            //callsInfo = service.getCalls();
                        	SipCallSession call = intent.getParcelableExtra(SipManager.EXTRA_CALL_INFO);
                        	
                    		if(callInfo != null && call.getCallId() != callInfo.getCallId()) {
                    			return;
                    		}
                			callInfo = call;
                			runOnUiThread(new UpdateUIFromCallRunnable());

//                        }
//                    } catch (RemoteException e) {
//                        Log.e(TAG, "Not able to retrieve calls");
//                    }
                        }
            } else if (action.equals(SipManager.ACTION_SIP_MEDIA_CHANGED)) {
                if (service != null) {
                    MediaState mediaState;
                    try {
                        mediaState = service.getCurrentMediaState();
                        Log.d(TAG, "Media update ...." + mediaState.isSpeakerphoneOn);
                        synchronized (callMutex) {
                            if (!mediaState.equals(lastMediaState)) {
                                lastMediaState = mediaState;
                                runOnUiThread(new UpdateUIFromMediaRunnable());
                            }   
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "Can't get the media state ", e);
                    }
                }
            }
		}
	};
	
	private void refreshViewOnAUKeyStatusChange() {
		View screen = findViewById(R.id.call_screen_layout);
		if (AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED) && mUser.getSecurity().equals(AUKeyManager.ATTACHED)) {
			screen.setBackgroundResource(R.drawable.account_guidance_bg);
			mCallType.setText("密话模式");
			mCallType.setVisibility(View.VISIBLE);
		}
		else {
			screen.setBackgroundResource(R.drawable.radar_background);
			mCallType.setText("明话模式");
			mCallType.setVisibility(View.VISIBLE);
		}
			
	}
	
	private User getUser(SipCallSession call) {
		if (call != null) {
            ParsedSipContactInfos uriInfos = SipUri.parseSipContact(call.getRemoteContact());
            Log.w(TAG, "sip account username: " + uriInfos.userName);
			return ContacterManager.contacters.get(StringUtil.getJidByCellphone(uriInfos.userName));
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
	
	private void printCallLog() {
        String infos = "";
        String natType = "";
		try {
			infos = service.showCallInfosDialog(callInfo.getCallId());
	        natType = service.getLocalNatType();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Log.i(TAG, "call log: " + infos + ", nat type: " + natType);
	}
	
	@Override
	public void onStop() {
		super.onStop();
        keyguardManager.lock();
	}
	
	@Override
	public void onStart() {
		super.onStart();
        keyguardManager.unlock();
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "Key down : " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                //
                // Volume has been adjusted by the user.
                //
                Log.i(TAG, "onKeyDown: Volume button pressed");
                int action = AudioManager.ADJUST_RAISE;
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    action = AudioManager.ADJUST_LOWER;
                }

                // Detect if ringing
                SipCallSession currentCallInfo = callInfo;
                // If not any active call active
                if (currentCallInfo == null && serviceConnected) {
                    break;
                }

                if (service != null) {
                    try {
                        service.adjustVolume(currentCallInfo, action, AudioManager.FLAG_SHOW_UI);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Can't adjust volume", e);
                    }
                }

                return true;
            case KeyEvent.KEYCODE_CALL:
            case KeyEvent.KEYCODE_ENDCALL:
            case KeyEvent.KEYCODE_SEARCH:
                return true;
            default:
                // Nothing to do
        }
        return super.onKeyDown(keyCode, event);
    }
    
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "Key up : " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_CALL:
            case KeyEvent.KEYCODE_SEARCH:
            case KeyEvent.KEYCODE_ENDCALL:
            	return true;

        }
        return super.onKeyUp(keyCode, event);
    }
	
    @Override
    public boolean shouldActivateProximity() {

        if(lastMediaState != null) {
            if(lastMediaState.isBluetoothScoOn) {
                return false;
            }
            if(lastMediaState.isSpeakerphoneOn && ! useAutoDetectSpeaker) {
                return false;
            }
        }
        
        if (callInfo == null) {
            return false;
        }

        boolean isValidCallState = true;
//        int count = 0;
//        for (SipCallSession callInfo : callsInfo) {
            if(callInfo.mediaHasVideo()) {
                return false;
            }
            if(!callInfo.isAfterEnded()) {
                int state = callInfo.getCallState();
                
                isValidCallState &= (
                        (state == SipCallSession.InvState.CONFIRMED) ||
                        (state == SipCallSession.InvState.CONNECTING) ||
                        (state == SipCallSession.InvState.CALLING) ||
                        (state == SipCallSession.InvState.EARLY && !callInfo.isIncoming())
                        );
//                count ++;
            }
//        }
//        if(count == 0) {
//            return false;
//        }

        return isValidCallState;
    }

    @Override
    public void onProximityTrackingChanged(boolean acquired) {
        if(useAutoDetectSpeaker && service != null) {
            if(acquired) {
                if(lastMediaState == null || lastMediaState.isSpeakerphoneOn) {
                    try {
                        service.setSpeakerphoneOn(false);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Can't run speaker change");
                    }
                }
            }else {
                if(lastMediaState == null || !lastMediaState.isSpeakerphoneOn) {
                    try {
                        service.setSpeakerphoneOn(true);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Can't run speaker change");
                    }
                }
            }
        }
    }


	@Override
	public void onTrigger(int whichAction) {

		SipCallSession call = callInfo;
		
        if (whichAction == TAKE_CALL || whichAction == REJECT_CALL || whichAction == DONT_TAKE_CALL ||
            whichAction == TERMINATE_CALL || whichAction == DETAILED_DISPLAY || 
            whichAction == TOGGLE_HOLD || whichAction == START_RECORDING ||
            whichAction == STOP_RECORDING || whichAction == DTMF_DISPLAY ||
            whichAction == XFER_CALL || whichAction == TRANSFER_CALL ||
            whichAction == START_VIDEO || whichAction == STOP_VIDEO ) {
            if (call == null) {
                Log.e(TAG, "Try to do an action on a null call !!!");
                return;
            }
            if (call.getCallId() == SipCallSession.INVALID_CALL_ID) {
                Log.e(TAG, "Try to do an action on an invalid call !!!");
                return;
            }
        }

        // Reset proximity sensor timer
        proximityManager.restartTimer();
        
        try {
            switch (whichAction) {
                case TAKE_CALL: {
                    if (service != null) {
                        Log.w(TAG, "Answer call " + call.getCallId());

                        boolean shouldHoldOthers = false;

                        // Well actually we should be always before confirmed
                        if (call.isBeforeConfirmed()) {
                            shouldHoldOthers = true;
                        }

                        service.answer(call.getCallId(), SipCallSession.StatusCode.OK);

//                        // if it's a ringing call, we assume that user wants to
//                        // hold other calls
//                        if (shouldHoldOthers && callsInfo != null) {
//                            for (SipCallSession callInfo : callsInfo) {
//                                // For each active and running call
//                                if (SipCallSession.InvState.CONFIRMED == callInfo.getCallState()
//                                        && !callInfo.isLocalHeld()
//                                        && callInfo.getCallId() != call.getCallId()) {
//
//                                    Log.d(TAG, "Hold call " + callInfo.getCallId());
//                                    service.hold(callInfo.getCallId());
//
//                                }
//                            }
//                        }
                    }
                    break;
                }
                case DONT_TAKE_CALL: {
                    if (service != null) {
                        service.hangup(call.getCallId(), StatusCode.BUSY_HERE);
                    }
                    break;
                }
                case REJECT_CALL:
                case TERMINATE_CALL: {
                    if (service != null) {
                        service.hangup(call.getCallId(), 0);
                    }
                    break;
                }
                case MUTE_ON:
                case MUTE_OFF: {
                    if (service != null) {
                        service.setMicrophoneMute((whichAction == MUTE_ON) ? true : false);
                    }
                    break;
                }
                case SPEAKER_ON:
                case SPEAKER_OFF: {
                    if (service != null) {
                        Log.d(TAG, "Manually switch to speaker");
                        useAutoDetectSpeaker = false;
                        service.setSpeakerphoneOn((whichAction == SPEAKER_ON) ? true : false);
                    }
                    break;
                }
                case BLUETOOTH_ON:
                case BLUETOOTH_OFF: {
                    if (service != null) {
                        service.setBluetoothOn((whichAction == BLUETOOTH_ON) ? true : false);
                    }
                    break;
                }
                case TOGGLE_HOLD: {
                    if (service != null) {
                        if (call.getMediaStatus() == SipCallSession.MediaState.LOCAL_HOLD ||
                                call.getMediaStatus() == SipCallSession.MediaState.NONE) {
                            service.reinvite(call.getCallId(), true);
                        } else {
                            service.hold(call.getCallId());
                        }
                    }
                    break;
                }
                case START_RECORDING :{
                    if(service != null) {
                        service.startRecording(call.getCallId(), SipManager.BITMASK_ALL);
                    }
                    break;
                }
                case STOP_RECORDING : {
                    if(service != null) {
                        service.stopRecording(call.getCallId());
                    }
                    break;
                }
                case DTMF_DISPLAY:
                case DETAILED_DISPLAY:
                case MEDIA_SETTINGS:
                case XFER_CALL:
                case TRANSFER_CALL:
                case ADD_CALL:
                case START_VIDEO :
                case STOP_VIDEO :
                case ZRTP_TRUST :
                case ZRTP_REVOKE :
                	break;
                	
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Was not able to call service method", e);
        }
    }
	
	@Override
	public void onBackPressed() {
        onTrigger(TERMINATE_CALL);
	}
}
