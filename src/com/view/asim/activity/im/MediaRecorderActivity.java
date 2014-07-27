package com.view.asim.activity.im;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.view.asim.R;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.util.ConvertToUtils;
import com.view.asim.util.FileUtil;
import com.view.asim.util.NetworkUtils;
import com.view.asim.view.ProgressView;
import com.view.asim.view.ThemeRadioButton;
import com.yixia.camera.FFMpegUtils;
import com.yixia.camera.MediaRecorder;
import com.yixia.camera.MediaRecorder.OnErrorListener;
import com.yixia.camera.MediaRecorder.OnPreparedListener;
import com.yixia.camera.MediaRecorderFilter;
import com.yixia.camera.VCamera;
import com.yixia.camera.model.MediaObject;
import com.yixia.camera.model.MediaObject.MediaPart;
import com.yixia.camera.util.DeviceUtils;
import com.yixia.camera.util.FileUtils;
import com.yixia.camera.util.StringUtils;
import com.yixia.camera.view.CameraNdkView;

/**
 * 短视频录制
 * @author xuweinan
 *
 */
public class MediaRecorderActivity extends ActivitySupport implements OnErrorListener, OnClickListener, OnPreparedListener {
	private final static String TAG = "MediaRecorderActivity";
	private final static int[] FILTER_ICONS = new int[] { R.drawable.filter_original, R.drawable.filter_black_white, R.drawable.filter_sharpen, R.drawable.filter_old_film, R.drawable.filter_edge, R.drawable.filter_anti_color, R.drawable.filter_radial, R.drawable.filter_8bit, R.drawable.filter_lomo };
	private final static String[] FILTER_VALUES = new String[] { MediaRecorderFilter.CAMERA_FILTER_NO, MediaRecorderFilter.CAMERA_FILTER_BLACKWHITE, MediaRecorderFilter.CAMERA_FILTER_SHARPEN, MediaRecorderFilter.CAMERA_FILTER_OLD_PHOTOS, MediaRecorderFilter.CAMERA_FILTER_NEON_LIGHT, MediaRecorderFilter.CAMERA_FILTER_ANTICOLOR, MediaRecorderFilter.CAMERA_FILTER_PASS_THROUGH, MediaRecorderFilter.CAMERA_FILTER_MOSAICS, MediaRecorderFilter.CAMERA_FILTER_REMINISCENCE };
	public final static int REQUEST_CODE_IMPORT_IMAGE = 999;
	public final static int REQUEST_CODE_IMPORT_VIDEO = 998;
	public final static int REQUEST_CODE_IMPORT_VIDEO_EDIT = 997;
	public final static int RECORD_TIME_MAX = 10 * 1000;
	public final static int RECORD_TIME_MIN = 3 * 1000;
	public final static String VIDEO_PATH = "video.path";
	public final static String VIDEO_THUMB_PATH = "video.thumb.path";

	private CheckedTextView mRecordDelete, mRecordDelay, mRecordFilter;
	private ProgressView mProgressView;
	private CameraNdkView mSurfaceView;
	private TextView mTitleText;
	private Button mTitleNext;
	private ImageView mPressText;
	private RadioGroup mRecordFilterContainer;
	private View mRecordFilterLayout;

	private MediaRecorderFilter mMediaRecorder;
	private MediaObject mMediaObject;
	private int mWindowWidth;
	private volatile boolean mPressedStatus, mReleased, mStartEncoding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// ??叉?㈤??灞?
		
		mWindowWidth = DeviceUtils.getScreenWidth(this);
		setContentView(R.layout.activity_media_recorder);

		mSurfaceView = (CameraNdkView) findViewById(R.id.record_preview);
		mProgressView = (ProgressView) findViewById(R.id.record_progress);
		mTitleText = (TextView) findViewById(R.id.title_text);
		mTitleNext = (Button) findViewById(R.id.capture_next_btn);
		mRecordDelay = (CheckedTextView) findViewById(R.id.record_delay);
		mRecordDelete = (CheckedTextView) findViewById(R.id.record_delete);
		mRecordFilter = (CheckedTextView) findViewById(R.id.record_filter);
		mRecordFilter.setChecked(true);
		mPressText = (ImageView) findViewById(R.id.record_tips_text);
		mRecordFilterContainer = (RadioGroup) findViewById(R.id.record_filter_container);
		mRecordFilterLayout = findViewById(R.id.record_filter_layout);

		findViewById(R.id.record_layout).setOnTouchListener(mOnSurfaceViewTouchListener);
		mTitleNext.setOnClickListener(this);
		mRecordDelete.setOnClickListener(this);
		mRecordFilter.setOnClickListener(this);
		mRecordDelay.setOnClickListener(this);

		mSurfaceView.getLayoutParams().height = mWindowWidth;
		//mSurfaceView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		findViewById(R.id.record_layout).getLayoutParams().height = mWindowWidth;
		mProgressView.invalidate();
		
		loadFilter();
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (mMediaRecorder == null)
			initMediaRecorder();
		else {
			mMediaRecorder.setSurfaceHolder(mSurfaceView.getHolder());
			mMediaRecorder.prepare();
		}
		checkStatus();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mMediaRecorder != null && !mReleased) {
			mMediaRecorder.release();
		}
	}

	@Override
	public void onBackPressed() {
		if (mRecordDelete.isChecked()) {
			cancelDelete();
			return;
		}

		if (mMediaObject != null && mMediaObject.getDuration() > 1) {
			new AlertDialog.Builder(this).setTitle("提示").setMessage(R.string.record_camera_exit_dialog_message).setNegativeButton("确认", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mMediaObject.delete();
					exitWithResult(RESULT_CANCELED);
				}

			}).setPositiveButton("取消", null).setCancelable(false).show();
			return;
		}

		if (mMediaObject != null)
			mMediaObject.delete();
		super.onBackPressed();
	}

	private void initMediaRecorder() {
		mMediaRecorder = new MediaRecorderFilter();
		mMediaRecorder.setOnErrorListener(this);
		mMediaRecorder.setOnPreparedListener(this);
		mMediaRecorder.setVideoBitRate(NetworkUtils.isWifiAvailable(this) ? 
				MediaRecorder.VIDEO_BITRATE_MEDIUM : MediaRecorder.VIDEO_BITRATE_NORMAL);
		mMediaRecorder.setSurfaceView(mSurfaceView);
		String key = FileUtil.genCaptureMicroVideoName();
		Log.i(TAG, "key: " + key + ", video cache path:" + VCamera.getVideoCachePath());
		mMediaObject = mMediaRecorder.setOutputDirectory(key, VCamera.getVideoCachePath() + key);
		if (mMediaObject != null) {
			mMediaRecorder.prepare();
			mMediaRecorder.setCameraFilter(MediaRecorderFilter.CAMERA_FILTER_NO);
			mProgressView.setData(mMediaObject);
		} else {
			Toast.makeText(this, R.string.record_camera_init_faild, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	private View.OnTouchListener mOnSurfaceViewTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mMediaRecorder == null || mMediaObject == null) {
				return false;
			}

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:

				if (cancelDelete())
					return true;

				if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
					return true;
				}

				mTitleText.setText(String.format("已录制 %.1f 秒", mMediaObject.getDuration() / 1000F));

				startRecord();

				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				if (mPressedStatus) {
					stopRecord();

					if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
						mTitleNext.performClick();
					}
					
					if (mMediaObject.getDuration() < RECORD_TIME_MIN) {
						Toast.makeText(MediaRecorderActivity.this, R.string.record_video_too_short, Toast.LENGTH_SHORT).show();
					}
					
				}

				//mTitleText.setText(R.string.record_camera_title);
				break;
			}
			return true;
		}

	};

	private void startRecord() {
		mPressedStatus = true;

		if (mMediaRecorder != null) {
			mMediaRecorder.startRecord();
		}

		if (mHandler != null) {
			mHandler.sendEmptyMessage(HANDLE_INVALIDATE_PROGRESS);
			mHandler.sendEmptyMessageDelayed(HANDLE_STOP_RECORD, RECORD_TIME_MAX - mMediaObject.getDuration());
		}

		mHandler.removeMessages(HANDLE_SHOW_TIPS);
		mHandler.sendEmptyMessage(HANDLE_SHOW_TIPS);
		mRecordDelete.setEnabled(false);
		if (!mRecordDelay.isChecked())
			mRecordDelay.setEnabled(false);

		mPressText.setImageResource(R.drawable.record_tips_pause);
	}

	private void stopRecord() {
		mPressedStatus = false;
		mPressText.setImageResource(R.drawable.record_tips_press);

		if (mMediaRecorder != null)
			mMediaRecorder.stopRecord();

		mHandler.removeMessages(HANDLE_STOP_RECORD);

		mRecordDelay.setChecked(false);
		mRecordDelay.setEnabled(true);
		mRecordDelete.setEnabled(true);
	}

	private boolean cancelDelete() {
		if (mMediaObject != null) {
			MediaPart part = mMediaObject.getCurrentPart();
			if (part != null && part.remove) {
				part.remove = false;
				mRecordDelete.setChecked(false);

				if (mProgressView != null)
					mProgressView.invalidate();

				return true;
			}
		}
		return false;
	}

	private static final int HANDLE_INVALIDATE_PROGRESS = 0;
	private static final int HANDLE_STOP_RECORD = 1;
	private static final int HANDLE_SHOW_TIPS = 2;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_INVALIDATE_PROGRESS:
				if (mMediaObject != null && !isFinishing()) {
					if (mProgressView != null)
						mProgressView.invalidate();
					if (mPressedStatus)
						mTitleText.setText(String.format("已录制 %.1f 秒", mMediaObject.getDuration() / 1000F));
					if (mPressedStatus)
						sendEmptyMessageDelayed(0, 30);
				}
				break;
			case HANDLE_SHOW_TIPS:
				if (mMediaRecorder != null && !isFinishing()) {
					int duration = checkStatus();

					if (mPressedStatus) {
						if (duration < RECORD_TIME_MAX) {
							sendEmptyMessageDelayed(HANDLE_SHOW_TIPS, 200);
						} else {
							sendEmptyMessageDelayed(HANDLE_SHOW_TIPS, 500);
						}
					}
				}
				break;
			case HANDLE_STOP_RECORD:
				stopRecord();
				startEncoding();
				break;
			}
		}
	};

	private void startEncoding() {
		if (FileUtils.showFileAvailable() < 200) {
			Toast.makeText(this, R.string.record_camera_check_available_faild, Toast.LENGTH_SHORT).show();
			return;
		}

		if (!isFinishing() && mMediaRecorder != null && mMediaObject != null && !mStartEncoding) {
			mStartEncoding = true;

			new AsyncTask<Void, Void, Boolean>() {

				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					showProgress("", getString(R.string.record_camera_progress_message));
				}

				@Override
				protected Boolean doInBackground(Void... params) {
					boolean result = FFMpegUtils.videoTranscoding(mMediaObject, mMediaObject.getOutputVideoPath(), mWindowWidth, false);
					
					if (result && mMediaRecorder != null) {
						mMediaRecorder.release();
						mReleased = true;
					}
					result = FFMpegUtils.captureThumbnails(mMediaObject.getOutputVideoPath(), mMediaObject.getOutputVideoThumbPath(), "200x200", "0");

					return result;
				}

				@Override
				protected void onCancelled() {
					super.onCancelled();
					mStartEncoding = false;
				}

				@Override
				protected void onPostExecute(Boolean result) {
					super.onPostExecute(result);
					hideProgress();
					mMediaObject.delete();
					if (result) {
						exitWithResult(RESULT_OK);
					} else {
						Toast.makeText(MediaRecorderActivity.this, R.string.record_video_transcoding_faild, Toast.LENGTH_SHORT).show();
						exitWithResult(RESULT_CANCELED);
					}
					mStartEncoding = false;
				}
			}.execute();
		}
	}
	
	private void exitWithResult(int result) {
		Intent intent = new Intent();
        intent.putExtra(VIDEO_PATH, mMediaObject.getOutputVideoPath());
        intent.putExtra(VIDEO_THUMB_PATH, mMediaObject.getOutputVideoThumbPath());
        
        setResult(result, intent);
        finish();
	}

	private int checkStatus() {
		int duration = 0;
		if (!isFinishing() && mMediaObject != null) {
			duration = mMediaObject.getDuration();
			if (duration < RECORD_TIME_MIN) {
				if (mTitleNext.getVisibility() != View.INVISIBLE)
					mTitleNext.setVisibility(View.INVISIBLE);
			} else {
				if (mTitleNext.getVisibility() != View.VISIBLE) {
					mTitleNext.setVisibility(View.VISIBLE);
					mTitleNext.setText(R.string.record_camera_next);
				}
			}
		}
		return duration;
	}

	@Override
	public void onVideoError(int what, int extra) {
		Log.e(TAG, "[MediaRecorderActvity]onVideoError: what" + what + " extra:" + extra);
	}

	@Override
	public void onAudioError(int what, String message) {
		Log.e(TAG, "[MediaRecorderActvity]onAudioError: what" + what + " message:" + message);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MediaRecorderActivity.this, R.string.record_camera_open_audio_faild, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();

		if (id != R.id.record_delete) {
			if (mMediaObject != null) {
				MediaPart part = mMediaObject.getCurrentPart();
				if (part != null) {
					if (part.remove) {
						part.remove = false;
						mRecordDelete.setChecked(false);
						if (mProgressView != null)
							mProgressView.invalidate();
					}
				}
			}
		}

		if (id != R.id.record_filter) {
			if (mRecordFilter.isChecked()) {
				mRecordFilterLayout.setVisibility(View.GONE);
				mRecordFilter.setChecked(false);
			}
		}

		switch (v.getId()) {
		case R.id.capture_next_btn:
			startEncoding();
			break;
		case R.id.record_delete:
			if (mMediaObject != null) {
				MediaPart part = mMediaObject.getCurrentPart();
				if (part != null) {
					if (part.remove) {
						part.remove = false;
						mMediaObject.removePart(part, true);
						mRecordDelete.setChecked(false);
					} else {
						part.remove = true;
						mRecordDelete.setChecked(true);
					}
				}
				if (mProgressView != null)
					mProgressView.invalidate();

				checkStatus();
			}
			break;
		case R.id.record_filter:
			if (mRecordFilter.isChecked()) {
				mRecordFilterLayout.setVisibility(View.GONE);
				mRecordFilter.setChecked(false);
			} else {
				loadFilter();
				mRecordFilterLayout.setVisibility(View.VISIBLE);
				mRecordFilter.setChecked(true);
			}
			break;
		}
	}


	private void loadFilter() {
		if (!isFinishing() && mRecordFilterContainer.getChildCount() == 0) {
			final String[] filterNames = getResources().getStringArray(R.array.record_filter);
			int leftMargin = ConvertToUtils.dipToPX(this, 10);
			LayoutInflater mInflater = LayoutInflater.from(this);
			for (int i = 0; i < FILTER_ICONS.length; i++) {
				ThemeRadioButton filterView = (ThemeRadioButton) mInflater.inflate(R.layout.view_radio_item, null);
				filterView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						int index = ConvertToUtils.toInt(v.getTag().toString());
						if (mMediaRecorder != null)
							mMediaRecorder.setCameraFilter(FILTER_VALUES[index]);
					}
				});
				filterView.setCompoundDrawablesWithIntrinsicBounds(0, FILTER_ICONS[i], 0, 0);
				filterView.setText(filterNames[i]);
				filterView.setTag(i);
				RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
				lp.leftMargin = leftMargin;
				mRecordFilterContainer.addView(filterView, lp);
			}

			mRecordFilterContainer.getChildAt(0).performClick();
		}
	}

	@Override
	public void onPrepared() {
		if (mMediaRecorder != null) {
			mMediaRecorder.autoFocus(new AutoFocusCallback() {

				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					if (success) {

					}
				}
			});
		}
	}
}
