package com.view.asim.activity.voip;

/**
 * VoIP 通话界面
 * @author xuweinan
 */

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.avos.avoscloud.AVAnalytics;
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
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;
import com.view.asim.util.CallUtil;
import com.view.asim.util.StringUtil;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
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
	
	private final static int MODE_ENCRYPTION = 1;
	private final static int MODE_PLAIN = 2;
	private final static int MODE_LOCAL_SECURITY_CHANGED = 3;
	private final static int MODE_PEER_SECURITY_CHANGED = 4;
	
	private User mUser;
	private int mSecurityMode;
	final int SCREEN_OFF_TIMEOUT = 12000;
    private static final int QUIT_DELAY = 2000;
    private String mInitLocalUKeyState = AUKeyManager.DETACHED;
    private String mInitPeerUKeyState = AUKeyManager.DETACHED;
    
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
    //private SipCallSession callsInfo[] = null;
    private PreferencesProviderWrapper prefsWrapper;
    private PowerManager powerManager;
    private WakeLock wakeLock;
    private Timer quitTimer;
    private TimerTask quitTask = null;
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

            //runOnUiThread(new UpdateUIFromCallRunnable());
            //runOnUiThread(new UpdateUIFromMediaRunnable());
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
		
		refreshUserView(callInfo);
		
		mInCallLayout = findViewById(R.id.incall_layout);
		mCalledLayout = findViewById(R.id.called_layout);
		
		initSecuritySessionState();

		setCallState(callInfo);
        refreshViewOnAUKeyStatusChange();
	}
	
	private void initSecuritySessionState() {
		mInitLocalUKeyState = AUKeyManager.getInstance().getAUKeyStatus();
		mInitPeerUKeyState = mUser.getSecurity();
		
		if (mInitLocalUKeyState.equals(AUKeyManager.ATTACHED) && mInitPeerUKeyState.equals(AUKeyManager.ATTACHED)) {
			mSecurityMode = MODE_ENCRYPTION;
		}
		else {
			mSecurityMode = MODE_PLAIN;
		}		
		Log.i(TAG, "init local state: " + mInitLocalUKeyState + ", peer state: " + mInitPeerUKeyState + ", security mode: " + mSecurityMode);
		
	}
	
	private void refreshUserView(SipCallSession call) {
		mUser = getUser(call);
		if (mUser != null) {
			setAvatarImage(mAvatar, mUser);
			mNickname.setText(mUser.getNickName());
		}
	}
	
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
		SipCallSession call = intent.getParcelableExtra(SipManager.EXTRA_CALL_INFO);
        Log.i(TAG, "New intent is launched, call: " + call);
		
		if (call != null) {
			if (call.getCallId() == callInfo.getCallId()) {
				callInfo = call;
		        Log.i(TAG, "input call id " + call.getCallId() + " matched, update");
			}
			else {
				SipCallSession[] callsInfo = null;
				try {
					callsInfo = service.getCalls();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				
				for (SipCallSession c : callsInfo) {
					if (c.getCallId() == callInfo.getCallId()) {
						Log.i(TAG, "enum call id " + c.getCallId() + ", state " + c.getCallState() + ", code " + c.getLastStatusCode());
				        if (c.getCallState() == SipCallSession.InvState.DISCONNECTED) {
							Log.i(TAG, "input call id " + call.getCallId() + " dismatch, my call id " + callInfo.getCallId() + " disconnected, so the input call is a new incoming call, save it");
							callInfo = call;
							refreshUserView(callInfo);

							initSecuritySessionState();
							if (quitTask != null) {
								quitTask.cancel();
							}
				        }
				        else {
							Log.i(TAG, "input call id " + call.getCallId() + " dismatch, update my call id " + callInfo.getCallId());
							callInfo = c;
				        }
						super.onNewIntent(intent);
						return;
					}
				}
				
		        Log.i(TAG, "my call id is invalid, update to input call id " + call.getCallId());
				callInfo = call;
			}
		}
		else {
			Log.i(TAG, "enter in from notification click");
		}
		
		
        super.onNewIntent(intent);
    }
	
	@Override
	protected void onResume() {
		super.onResume();
        runOnUiThread(new UpdateUIFromCallRunnable());
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
			mSessionEndBtn.setVisibility(View.VISIBLE);
			mMuteBtn.setVisibility(View.VISIBLE);
			mSpeakerBtn.setVisibility(View.VISIBLE);

			NoticeManager.getInstance().dispatchInCallNotify(mUser);
			break;
			
		case SipCallSession.InvState.NULL:
		case SipCallSession.InvState.DISCONNECTED:
			mCallingTxt.setText(CallUtil.getStringByStatusCode(callInfo.getLastStatusCode()));
			mSessionEndBtn.setVisibility(View.INVISIBLE);
			mMuteBtn.setVisibility(View.INVISIBLE);
			mSpeakerBtn.setVisibility(View.INVISIBLE);
			
			NoticeManager.getInstance().clearInCallNotify(mUser);
			break;
		case SipCallSession.InvState.EARLY:
			if (callInfo.isIncoming()) {
				mInCallLayout.setVisibility(View.GONE);
				mCallingCancelBtn.setVisibility(View.GONE);
				mCalledLayout.setVisibility(View.VISIBLE);
			}
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
            SipCallSession mainCallInfo = callInfo;

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
                        if(judgeMissedCall(mainCallInfo, isIncoming)) {
    	        			NoticeManager.getInstance().dispatchMissedCallNotify(mUser);
                		}
                        
                        delayedQuit();
                        return;
    
                }
                
                Log.d(TAG, "we leave the update ui function");
            }
            
            refreshViewOnAUKeyStatusChange();
            
            proximityManager.updateProximitySensorMode();
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
        
        Log.d(TAG, "Start quit timer");
        if (quitTimer != null) {
        	try {
        		quitTask = new QuitTimerTask();
        		quitTimer.schedule(quitTask, QUIT_DELAY);
        	} catch(IllegalStateException e) {
        		e.printStackTrace();
        	}
        } else {
            finish();
        }
    }
    
    private class QuitTimerTask extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "Run quit timer");
            callLog();
            
            finish();
        }
    };
    
    private void judgeSessionStateBySecurityState(String local, String peer) {
		Log.i(TAG, "init local state: " + mInitLocalUKeyState + ", peer state: " + mInitPeerUKeyState + ", new local: " + local + ", peer: " + peer);
    	
    	if (!local.equals(mInitLocalUKeyState)) {
    		mSecurityMode = MODE_LOCAL_SECURITY_CHANGED;
    	}
    	else
    	if (!peer.equals(mInitPeerUKeyState)) {
    		mSecurityMode = MODE_PEER_SECURITY_CHANGED;
    	}
    	
    	if (mSecurityMode == MODE_LOCAL_SECURITY_CHANGED || mSecurityMode == MODE_PEER_SECURITY_CHANGED) {
    		Log.w(TAG, "local or peer ukey state changed, disconnect cur session");
    		runOnUiThread(new Runnable(){  
                @Override  
                public void run() {  
                	refreshViewOnAUKeyStatusChange();
                	if (service != null) {
                        try {
							service.hangup(callInfo.getCallId(), 0);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
                    }
                }  
            });  
    	}
    	
    }
    
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (Constant.AUKEY_STATUS_UPDATE.equals(action) || Constant.ROSTER_PRESENCE_CHANGED.equals(action) || Constant.ROSTER_UPDATED.equals(action)) {
				User newUser = null;
				String peerState = null;
				if (Constant.ROSTER_PRESENCE_CHANGED.equals(action) || Constant.ROSTER_UPDATED.equals(action)) {
					newUser = intent.getParcelableExtra(User.userKey);
					peerState = newUser.getSecurity();
				}
				else {
					peerState = mInitPeerUKeyState;
				}
				
				String localState = AUKeyManager.getInstance().getAUKeyStatus();
				
				judgeSessionStateBySecurityState(localState, peerState);
				
			}
			else if (action.equals(SipManager.ACTION_SIP_CALL_CHANGED)) {
                synchronized (callMutex) {
                	SipCallSession call = intent.getParcelableExtra(SipManager.EXTRA_CALL_INFO);
    				Log.i(TAG, "recv sip state changed broadcast: " + call.getCallId());

            		if(callInfo != null && call.getCallId() != callInfo.getCallId()) {
            			return;
            		}
        			callInfo = call;
        			runOnUiThread(new UpdateUIFromCallRunnable());
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
		if (mSecurityMode == MODE_ENCRYPTION) {
			screen.setBackgroundResource(R.drawable.account_guidance_bg);
			mCallType.setText("密话模式");
			mCallType.setVisibility(View.VISIBLE);
		}
		else if (mSecurityMode == MODE_PLAIN) {
			screen.setBackgroundResource(R.drawable.radar_background);
			mCallType.setText("明话模式");
			mCallType.setVisibility(View.VISIBLE);
		}
		else if (mSecurityMode == MODE_LOCAL_SECURITY_CHANGED) {
			mCallType.setText("本机密盾状态变化，通话中止");
			mCallType.setVisibility(View.VISIBLE);
		}
		else if (mSecurityMode == MODE_PEER_SECURITY_CHANGED) {
			mCallType.setText("对方密盾状态变化，通话中止");
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
                Log.i(TAG, "onKeyDown: Volume button pressed");
                int action = AudioManager.ADJUST_RAISE;
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    action = AudioManager.ADJUST_LOWER;
                }

                SipCallSession currentCallInfo = callInfo;
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
            
            case KeyEvent.KEYCODE_BACK:
            	moveTaskToBack(true);
            	return true;
            default:
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
        }
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

                        if (call.isBeforeConfirmed()) {
                            shouldHoldOthers = true;
                        }

                        service.answer(call.getCallId(), SipCallSession.StatusCode.OK);
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
	
	private void callLog() {
		
		if (mInitLocalUKeyState.equals(AUKeyManager.ATTACHED) && mInitPeerUKeyState.equals(AUKeyManager.ATTACHED)) {
			callInfo.setSecurity(IMMessage.ENCRYPTION);
			if (callInfo.isIncoming()) {
				AVAnalytics.onEvent(this, StringUtil.getCellphoneByName(ContacterManager.userMe.getName()), Constant.EVENT_TAG_VOIP_INCOMING_CALL_ENCRYPTED, 1);
			}
			else {
				AVAnalytics.onEvent(this, StringUtil.getCellphoneByName(ContacterManager.userMe.getName()), Constant.EVENT_TAG_VOIP_OUTGOING_CALL_ENCRYPTED, 1);
			}
		}
		else {
			callInfo.setSecurity(IMMessage.PLAIN);
			if (callInfo.isIncoming()) {
				AVAnalytics.onEvent(this, StringUtil.getCellphoneByName(ContacterManager.userMe.getName()), Constant.EVENT_TAG_VOIP_INCOMING_CALL_PLAIN, 1);
			}
			else {
				AVAnalytics.onEvent(this, StringUtil.getCellphoneByName(ContacterManager.userMe.getName()), Constant.EVENT_TAG_VOIP_OUTGOING_CALL_PLAIN, 1);
			}
		}
		
		// CallLog
		ContentValues cv = logValuesForCall(
	          this, callInfo);
	
		// Fill our own database
		getContentResolver().insert(
	          SipManager.CALLLOG_URI, cv);
	}
	
	
	private ContentValues logValuesForCall(Context context, SipCallSession call) {
		Log.w(TAG,  "save call log for " + call.getCallId() + ", status " + call.getLastStatusCode() + ", incoming " + call.isIncoming());
		ContentValues cv = new ContentValues();
		String remoteContact = call.getRemoteContact();
		
		cv.put(CallLog.Calls.NUMBER, remoteContact);
		
		Pattern p = Pattern.compile("^(?:\")?([^<\"]*)(?:\")?[ ]*(?:<)?sip(?:s)?:([^@]*@[^>]*)(?:>)?", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(remoteContact);
        String number = remoteContact;
        if (m.matches()) {
            number = m.group(2);
        }
        
        cv.put(CallLog.Calls.DATE, (callStart > 0) ? callStart : System.currentTimeMillis());
		int type = CallLog.Calls.OUTGOING_TYPE;
		int nonAcknowledge = 0; 
		if(call.isIncoming()) {
			type = CallLog.Calls.MISSED_TYPE;
			nonAcknowledge = 1;
			if(callStart > 0) {
				// Has started on the remote side, so not missed call
				type = CallLog.Calls.INCOMING_TYPE;
				nonAcknowledge = 0;
			}else if(call.getLastStatusCode() == SipCallSession.StatusCode.DECLINE ||
			        call.getLastStatusCode() == SipCallSession.StatusCode.BUSY_HERE ||
			        call.getLastReasonCode() == 200) {
				// We have intentionally declined this call or replied elsewhere
				type = CallLog.Calls.INCOMING_TYPE;
				nonAcknowledge = 0;
			}
		}

        cv.put(CallLog.Calls.TYPE, type);
        cv.put(CallLog.Calls.NEW, nonAcknowledge);
        cv.put(CallLog.Calls.DURATION, callDuration / 1000);
        cv.put(SipManager.CALLLOG_PROFILE_ID_FIELD, call.getAccId());
        cv.put(SipManager.CALLLOG_STATUS_CODE_FIELD, call.getLastStatusCode());
        cv.put(SipManager.CALLLOG_STATUS_TEXT_FIELD, call.getLastStatusComment());
        cv.put("security", call.getSecurity());

        ParsedSipContactInfos uriInfos = SipUri.parseSipContact(call.getRemoteContact());

		if(uriInfos != null) {
			cv.put(CallLog.Calls.CACHED_NAME, uriInfos.userName);
			cv.put(CallLog.Calls.CACHED_NUMBER_LABEL, "Work");
			cv.put(CallLog.Calls.CACHED_NUMBER_TYPE, "");
		}
		
		return cv;
	}
	
//	@Override
//	public void onBackPressed() {
//        onTrigger(TERMINATE_CALL);
//	}
}
