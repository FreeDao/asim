package com.view.asim.activity.im;


import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.comm.Constant;
import com.view.asim.manager.MessageManager;
import com.view.asim.model.Attachment;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.utils.FileUtil;
import com.view.asim.utils.ImageUtil;
import com.view.asim.view.VideoView;
import com.view.asim.view.VideoView.OnPlayStateListener;
import com.view.asim.worker.ExpiryTimerListener;
import com.view.asim.worker.FileRecvResultListener;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.view.asim.R;
import com.yixia.camera.util.DeviceUtils;

/**
 * 
 * �??�?????件�??�??.
 * 
 * @author xuweinan
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class VideoPreviewActivity extends ActivitySupport implements OnCompletionListener, OnPreparedListener, FileRecvResultListener{
	public static final String TAG = "VideoPreviewActivity";
	public static final int timer = 5;
			
	private ChatMessage message = null;
	private ImageView mPreviewImg = null;
	private TextView mProgressTxt = null;
	private TextView mBackTxtBtn = null;
	private ProgressBar mSpinner = null;
	private ImageButton mSaveBtn = null;
	private Button mTimerBtn = null;
	private VideoView mVideoView;

	private DisplayImageOptions options;
	private WeakReference<Bitmap> downloadImage = null;
	private int mWindowWidth;
	private BurnTimerThread burnThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.video_preview);
		init();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(message.getAttachment().getSrcUri() != null && message.getAttachment().getSrcUri().length() > 0) {
			loadingLocalVideo();
		}
		else {
			loadingAirVideo();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (mVideoView != null) {
			if (mVideoView.isPlaying()) {
				mVideoView.pause();
			}
		}
	}
	
	private void loadingLocalVideo() {
		Log.d(TAG, "loading local video " + message.getAttachment().getSrcUri());
		mVideoView.setVideoPath(message.getAttachment().getSrcUri());
		mVideoView.setVisibility(View.VISIBLE);
		mVideoView.start();
	}
	
	@Override
	public void onPrepared(android.media.MediaPlayer mp) {
		if (!isFinishing()) {
			mVideoView.start();
			//mVideoView.setLooping(true);
		}
	}
	
	private boolean downloadSourceVideo() {
		final String downloadUrl = "http://" + Constant.FILE_STORAGE_HOST + "/" + message.getAttachment().getKey();

		String fileSavePath = FileUtil.getVideoPathByWith(message.getWith());
		File dir = new File(fileSavePath);
		FileUtil.createDir(dir);
		
		String saveName = fileSavePath + message.getAttachment().getKey();
		byte[] fileBytes = ImageUtil.getBytesFromUrl(downloadUrl, Long.parseLong(message.getAttachment().getSize()),
				Constant.FILE_DOWNLOAD_TIMEOUT, this);
		if (fileBytes == null) {
	        Log.e(TAG, "download video file failed");
	        return false;
		}
		
		try {
			ImageUtil.saveFile(this, fileBytes, saveName);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		message.getAttachment().setSrcUri(saveName);
		MessageManager.getInstance().updateIMMessage(message);

		return true;
    }
	
	private void loadingAirVideo() {
		Log.d(TAG, "loading air video " + message.getAttachment().getKey());
		mVideoView.setVisibility(View.GONE);
		mPreviewImg.setVisibility(View.VISIBLE);

		// ?????�示缩�?��?��??�??�?????�??
		imageLoader.displayImage("file://" + message.getAttachment().getThumbUri(), mPreviewImg, options, new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				mSpinner.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				mSpinner.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				mSpinner.setVisibility(View.GONE);
				mProgressTxt.setVisibility(View.VISIBLE);

				new AsyncTask<Void, Void, Boolean>() {

					@Override
					protected Boolean doInBackground(Void... params) {
						return downloadSourceVideo();
					}

					@Override
					protected void onPostExecute(Boolean result) {
						super.onPostExecute(result);
						mPreviewImg.setVisibility(View.GONE);
						mProgressTxt.setVisibility(View.GONE);
						mVideoView.setVideoPath(message.getAttachment().getSrcUri());
						mVideoView.setVisibility(View.VISIBLE);
						mVideoView.start();
						
					}
				}.execute();
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				mSpinner.setVisibility(View.GONE);
			}
		});

	}
	
	// ?????��??�?????计�?�线�??
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
	
	private void startBurnTimer() {
		if (message.getDestroy().equals(IMMessage.BURN_AFTER_READ)) {
			mSaveBtn.setVisibility(View.GONE);
			mTimerBtn.setVisibility(View.VISIBLE);
			new BurnTimerThread(timer, new ExpiryTimerListener() {
				
				@Override
				public void onTick(int sec) {
					final int t = sec;
					runOnUiThread(new Runnable()    
			        {    
			            public void run()    
			            {    
			            	mTimerBtn.setText("" + t);
			            }    
			    
			        });
				}
	
				@Override
				public void onEnd() {
					runOnUiThread(new Runnable()    
			        {    
			            public void run()    
			            {    
			            	mTimerBtn.setText("???�??");
			            	Burn();
			            }    
			        });				
				}

				@Override
				public void onCancel() {
					
				}
				
			}).start();
		}
	}
	
	private void Burn() {

		burnMessage();
        finish();			        
	}

	private void init() {
		getEimApplication().addActivity(this);
		message = (ChatMessage) getIntent().getParcelableExtra(ChatMessage.IMMESSAGE_KEY);
		
		mPreviewImg = (ImageView) findViewById(R.id.preview_img);
		mVideoView = (VideoView) findViewById(R.id.video_preview);
		mVideoView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		mVideoView.setOnPreparedListener(this);
		mVideoView.setOnCompletionListener(this);
		mWindowWidth = DeviceUtils.getScreenWidth(this);
		mVideoView.getLayoutParams().height = mWindowWidth;

		mProgressTxt = (TextView) findViewById(R.id.progress_txt);
		mSpinner = (ProgressBar) findViewById(R.id.loading);
		mBackTxtBtn = (TextView) findViewById(R.id.title_back_btn);
		mBackTxtBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mTimerBtn = (Button) findViewById(R.id.timer_btn);
		mSaveBtn = (ImageButton) findViewById(R.id.save_btn);
		mSaveBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (downloadImage != null) {
					Bitmap bm = downloadImage.get();
					if (bm != null) {
						//ContentResolver cr = getContentResolver();
						//String uriStr = MediaStore.Images.Media.insertImage(cr, bm, "", "");
						Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					    File f = new File(message.getAttachment().getSrcUri());
					    Uri contentUri = Uri.fromFile(f);
					    mediaScanIntent.setData(contentUri);
					    sendBroadcast(mediaScanIntent);
						/*
						Uri STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI;
						String IMAGE_MIME_TYPE = "image/png";

						String picPath = getFilePathByContentResolver(ImagePreviewActivity.this, Uri.parse(uriStr));
				        ContentValues values = new ContentValues(4);
				        values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
				        values.put(Images.Media.MIME_TYPE, "image/png");
				        values.put(Images.Media.ORIENTATION, 0);
				        values.put(Images.Media.DATA, picPath);
				                  
				        cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
				        */
					}
				}
			}
		});
		
		options = new DisplayImageOptions.Builder()
			//.showImageOnFail(R.drawable.image_download_fail_icon)
			.resetViewBeforeLoading(true)
			.cacheOnDisc(true)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.considerExifParams(true)
			.displayer(new FadeInBitmapDisplayer(300))
			.build();
	}

	@Override
	public void onRecvResult(boolean result) {
	}

	@Override
	public void onRecvProgress(long cur, long total) {
		final int ratio = (int) (cur * 100 / total);
		if (ratio % 10 == 0) {
			runOnUiThread(new Runnable() {  
                @Override  
                public void run() {  
                	mProgressTxt.setText(ratio + "%");                
                }  
            }); 			
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		startBurnTimer();
	}
	
	@Override
	public void onBackPressed() {
		if (message.getDestroy().equals(IMMessage.BURN_AFTER_READ)) {
			if (burnThread != null) {
				burnThread.cancel();
			}
			burnMessage();
			
		}
		super.onBackPressed();
	}
	
	@Override
	public void onStop() {
		if (message.getDestroy().equals(IMMessage.BURN_AFTER_READ)) {
			if (burnThread != null) {
				burnThread.cancel();
			}
			burnMessage();
			
		}
		super.onStop();
	}
	
	private void burnMessage() {
    	Log.d(TAG, "remove message id " + message.getId() + ", " + message.getContent());
    	showToast("�?????已�??�??");
    	MessageManager.getInstance().delChatHisById(message.getId());
	}
	
}
