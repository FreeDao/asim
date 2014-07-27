package com.view.asim.view;

import java.io.IOException;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class VideoView extends TextureView implements android.view.TextureView.SurfaceTextureListener {
	
	private final static String TAG = "VideoView";

	private MediaPlayer.OnCompletionListener mOnCompletionListener;
	private MediaPlayer.OnPreparedListener mOnPreparedListener;
	private MediaPlayer.OnErrorListener mOnErrorListener;
	private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
	private OnPlayStateListener mOnPlayStateListener;
	private MediaPlayer mMediaPlayer = null;
	private SurfaceTexture mSurfaceHolder = null;

	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	/**
	 * PlaybackCompleted??��??�????件�?�常?????��??�?�???????没�??设置�?????????��??�?就�????��?��?��??�?
	 * 并�??�????OnCompletionListener???onCompletion
	 * ()??��?????此�?��??以�?????start()??��???????��??头�????��??件�??�????�?stop()???�?MediaPlayer�???????�????�?seekTo()??��????��??�??????��??�????
	 */
	private static final int STATE_PLAYBACK_COMPLETED = 5;
	/** Released/End??��??�????�?release()??��?????以�?????End??��?? */
	private static final int STATE_RELEASED = 5;

	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;

	private int mVideoWidth;
	private int mVideoHeight;
	//	private int mSurfaceWidth;
	//	private int mSurfaceHeight;

	private float mVolumn = -1;
	private int mDuration;
	private Uri mUri;

	//	SurfaceTextureAvailable

	public VideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initVideoView();
	}

	public VideoView(Context context) {
		super(context);
		initVideoView();
	}

	public VideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initVideoView();
	}

	protected void initVideoView() {
		try {
			AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
			mVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		} catch (UnsupportedOperationException e) {

		}
		//		mTryCount = 0;
		mVideoWidth = 0;
		mVideoHeight = 0;
		setSurfaceTextureListener(this);
		//		setFocusable(true);
		//		setFocusableInTouchMode(true);
		//		requestFocus();
		mCurrentState = STATE_IDLE;
		mTargetState = STATE_IDLE;
	}

	public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
		mOnErrorListener = l;
	}

	public void setOnPlayStateListener(OnPlayStateListener l) {
		mOnPlayStateListener = l;
	}

	public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener l) {
		mOnSeekCompleteListener = l;
	}

	public static interface OnPlayStateListener {
		public void onStateChanged(boolean isPlaying);
	}

	public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
		mOnCompletionListener = l;
	}

	public void setVideoPath(String path) {
//		if (StringUtils.isNotEmpty(path) && MediaUtils.isNative(path)) {
			mTargetState = STATE_PREPARED;
			openVideo(Uri.parse(path));
//		}
	}

	public int getVideoWidth() {
		return mVideoWidth;
	}

	public int getVideoHeight() {
		return mVideoHeight;
	}

	public void reOpen() {
		mTargetState = STATE_PREPARED;
		openVideo(mUri);
	}

	public int getDuration() {
		return mDuration;
	}

	/** ???�? */
	private void tryAgain(Exception e) {
		mCurrentState = STATE_ERROR;
		e.printStackTrace();
		openVideo(mUri);
	}

	public void start() {
		mTargetState = STATE_PLAYING;
		//?????��?��??{Prepared, Started, Paused, PlaybackCompleted}
		if (mMediaPlayer != null && (mCurrentState == STATE_PREPARED || mCurrentState == STATE_PAUSED || mCurrentState == STATE_PLAYING || mCurrentState == STATE_PLAYBACK_COMPLETED)) {
			try {
				if (!isPlaying())
					mMediaPlayer.start();
				mCurrentState = STATE_PLAYING;
				if (mOnPlayStateListener != null)
					mOnPlayStateListener.onStateChanged(true);
			} catch (IllegalStateException e) {
				tryAgain(e);
			} catch (Exception e) {
				tryAgain(e);
			}
		}
	}

	public void pause() {
		mTargetState = STATE_PAUSED;
		//?????��?��??{Started, Paused}
		if (mMediaPlayer != null && (mCurrentState == STATE_PLAYING || mCurrentState == STATE_PAUSED)) {
			try {
				mMediaPlayer.pause();
				mCurrentState = STATE_PAUSED;
				if (mOnPlayStateListener != null)
					mOnPlayStateListener.onStateChanged(false);
			} catch (IllegalStateException e) {
				tryAgain(e);
			} catch (Exception e) {
				tryAgain(e);
			}
		}
	}

	public void setVolume(float volume) {
		//?????��?��??{Idle, Initialized, Stopped, Prepared, Started, Paused, PlaybackCompleted}
		if (mMediaPlayer != null && (mCurrentState == STATE_PREPARED || mCurrentState == STATE_PLAYING || mCurrentState == STATE_PAUSED || mCurrentState == STATE_PLAYBACK_COMPLETED)) {
			try {
				mMediaPlayer.setVolume(volume, volume);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setLooping(boolean looping) {
		//?????��?��??{Idle, Initialized, Stopped, Prepared, Started, Paused, PlaybackCompleted}
		if (mMediaPlayer != null && (mCurrentState == STATE_PREPARED || mCurrentState == STATE_PLAYING || mCurrentState == STATE_PAUSED || mCurrentState == STATE_PLAYBACK_COMPLETED)) {
			try {
				mMediaPlayer.setLooping(looping);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void seekTo(int msec) {
		//?????��?��??{Prepared, Started, Paused, PlaybackCompleted}
		if (mMediaPlayer != null && (mCurrentState == STATE_PREPARED || mCurrentState == STATE_PLAYING || mCurrentState == STATE_PAUSED || mCurrentState == STATE_PLAYBACK_COMPLETED)) {
			try {
				if (msec < 0)
					msec = 0;
				mMediaPlayer.seekTo(msec);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/** ??��??�?????????��??�? */
	public int getCurrentPosition() {
		int position = 0;
		//?????��?��??{Idle, Initialized, Prepared, Started, Paused, Stopped, PlaybackCompleted}
		if (mMediaPlayer != null) {
			switch (mCurrentState) {
			case STATE_PLAYBACK_COMPLETED:
				position = getDuration();
				break;
			case STATE_PLAYING:
			case STATE_PAUSED:
				try {
					position = mMediaPlayer.getCurrentPosition();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
		return position;
	}

	public boolean isPlaying() {
		//?????��?��??{Idle, Initialized, Prepared, Started, Paused, Stopped, PlaybackCompleted}
		if (mMediaPlayer != null && mCurrentState == STATE_PLAYING) {
			try {
				return mMediaPlayer.isPlaying();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/** �????release??��??以�??MediaPlayer???�??????��??使�?? */
	public void release() {
		mTargetState = STATE_RELEASED;
		mCurrentState = STATE_RELEASED;
		if (mMediaPlayer != null) {
			try {
				mMediaPlayer.release();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mMediaPlayer = null;
		}
	}

	public void openVideo(Uri uri) {
		if (uri == null || mSurfaceHolder == null || getContext() == null) {
			// not ready for playback just yet, will try again later
			if (mSurfaceHolder == null && uri != null) {
				mUri = uri;
			}
			return;
		}

		Log.i(TAG, "[LocalVideoView]openVideo...");
		
		mUri = uri;
		mDuration = 0;

		Exception exception = null;
		try {
			if (mMediaPlayer == null) {
				mMediaPlayer = new MediaPlayer();
				mMediaPlayer.setOnPreparedListener(mPreparedListener);
				mMediaPlayer.setOnCompletionListener(mCompletionListener);
				mMediaPlayer.setOnErrorListener(mErrorListener);
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
				//			mMediaPlayer.setScreenOnWhilePlaying(true);
				mMediaPlayer.setVolume(mVolumn, mVolumn);
				mMediaPlayer.setSurface(new Surface(mSurfaceHolder));
			} else {
				mMediaPlayer.reset();
			}
			mMediaPlayer.setDataSource(getContext(), uri);

			//			if (mLooping)
			//				mMediaPlayer.setLooping(true);//�??????????
			mMediaPlayer.prepareAsync();
			// we don't set the target state here either, but preserve the
			// target state that was there before.
			mCurrentState = STATE_PREPARING;
		} catch (IOException ex) {
			exception = ex;
		} catch (IllegalArgumentException ex) {
			exception = ex;
		} catch (Exception ex) {
			exception = ex;
		}
		if (exception != null) {
			exception.printStackTrace();
			mCurrentState = STATE_ERROR;
			if (mErrorListener != null)
				mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
		}
	}

	private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(android.media.MediaPlayer mp) {
			mCurrentState = STATE_PLAYBACK_COMPLETED;
			//			mTargetState = STATE_PLAYBACK_COMPLETED;
			if (mOnCompletionListener != null)
				mOnCompletionListener.onCompletion(mp);
		}
	};

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		@Override
		public void onPrepared(android.media.MediaPlayer mp) {
			//�?须�??正常??��??
			if (mCurrentState == STATE_PREPARING) {
				mCurrentState = STATE_PREPARED;
				try {
					mDuration = mp.getDuration();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}

				try {
					mVideoWidth = mp.getVideoWidth();
					mVideoHeight = mp.getVideoHeight();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}

				switch (mTargetState) {
				case STATE_PREPARED:
					if (mOnPreparedListener != null)
						mOnPreparedListener.onPrepared(mMediaPlayer);
					break;
				case STATE_PLAYING:
					start();
					break;
				}
			}
		}
	};

	private MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {

		@Override
		public void onSeekComplete(MediaPlayer mp) {
			if (mOnSeekCompleteListener != null)
				mOnSeekCompleteListener.onSeekComplete(mp);
		}
	};

	private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
		@Override
		public boolean onError(android.media.MediaPlayer mp, int framework_err, int impl_err) {
			Log.e(TAG, "[VideoView]Error:" + framework_err + "," + impl_err);
			mCurrentState = STATE_ERROR;
			//			mTargetState = STATE_ERROR;
			//FIX�????以�???????��??以�???????��??�?
			if (mOnErrorListener != null)
				mOnErrorListener.onError(mp, framework_err, impl_err);

			return true;
		}
	};

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		boolean needReOpen = (mSurfaceHolder == null);
		mSurfaceHolder = surface;
		if (needReOpen) {
			reOpen();
		}
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		//??��??失�??
		mSurfaceHolder = null;
		release();
		return true;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {

	}

	/** ???????????? */
	public boolean isPrepared() {
		//|| mCurrentState == STATE_PAUSED || mCurrentState == STATE_PLAYING
		return mMediaPlayer != null && (mCurrentState == STATE_PREPARED);
	}

	//	/** ????????��?��???????? */
	//	public boolean canStart() {
	//		return mMediaPlayer != null && (mCurrentState == STATE_PREPARED || mCurrentState == STATE_PAUSED);
	//	}

	private static final int HANDLER_MESSAGE_PARSE = 0;
	private static final int HANDLER_MESSAGE_LOOP = 1;

	private Handler mVideoHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_MESSAGE_PARSE:
				pause();
				break;
			case HANDLER_MESSAGE_LOOP:
				if (isPlaying()) {
					seekTo(msg.arg1);
					sendMessageDelayed(mVideoHandler.obtainMessage(HANDLER_MESSAGE_LOOP, msg.arg1, msg.arg2), msg.arg2);
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	/** �???��????? */
	public void pauseDelayed(int delayMillis) {
		if (mVideoHandler.hasMessages(HANDLER_MESSAGE_PARSE))
			mVideoHandler.removeMessages(HANDLER_MESSAGE_PARSE);
		mVideoHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_PARSE, delayMillis);
	}

	/** ??????并�??�???��????�任??? */
	public void pauseClearDelayed() {
		pause();
		if (mVideoHandler.hasMessages(HANDLER_MESSAGE_PARSE))
			mVideoHandler.removeMessages(HANDLER_MESSAGE_PARSE);
		if (mVideoHandler.hasMessages(HANDLER_MESSAGE_LOOP))
			mVideoHandler.removeMessages(HANDLER_MESSAGE_LOOP);
	}

	/** ??��?????�?????????? */
	public void loopDelayed(int startTime, int endTime) {
		int delayMillis = endTime - startTime;
		seekTo(startTime);
		if (!isPlaying())
			start();
		if (mVideoHandler.hasMessages(HANDLER_MESSAGE_LOOP))
			mVideoHandler.removeMessages(HANDLER_MESSAGE_LOOP);
		mVideoHandler.sendMessageDelayed(mVideoHandler.obtainMessage(HANDLER_MESSAGE_LOOP, getCurrentPosition(), delayMillis), delayMillis);
	}
}
