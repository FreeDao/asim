package com.view.asim.activity.im;


import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.jivesoftware.smack.XMPPException;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.comm.Constant;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;
import com.view.asim.utils.FileUtil;
import com.view.asim.utils.ImageUtil;
import com.view.asim.utils.StringUtil;
import com.view.asim.view.EndAnimationDrawable;
import com.view.asim.view.OnAnimationChangeListener;
import com.view.asim.worker.ExpiryTimerListener;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.view.asim.R;

/**
 * 
 * 用户资料查看.
 * 
 * @author xuweinan
 */
public class ImagePreviewActivity extends ActivitySupport{
	public static final String TAG = "ImagePreviewActivity";
	public static final int timer = 5;
			
	private ChatMessage message = null;
	private ImageView mPreviewImg = null;
	private TextView mProgressTxt = null;
	private TextView mBackTxtBtn = null;
	private ProgressBar mSpinner = null;
	private ImageButton mSaveBtn = null;
	private Button mTimerBtn = null;

	private DisplayImageOptions options;
	private WeakReference<Bitmap> downloadImage = null;
	private BurnTimerThread burnThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.image_preview);
		init();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(message.getAttachment().getSrcUri() != null && message.getAttachment().getSrcUri().length() > 0) {
			loadingLocalImage();
		}
		else {
			loadingAirImage();
		}
	}
	
	private void loadingLocalImage() {
		Log.d(TAG, "loading local image " + message.getAttachment().getSrcUri());
		imageLoader.displayImage("file://" + message.getAttachment().getSrcUri(), mPreviewImg, options, new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				mSpinner.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				Log.d(TAG, "image load failed: " + failReason.getType());
				mSpinner.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				mSpinner.setVisibility(View.GONE);
				startBurnTimer();
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				mSpinner.setVisibility(View.GONE);
			}
		});
	}
	
	private void loadingAirImage() {
		Log.d(TAG, "loading air image " + message.getAttachment().getKey());

		final String downloadUrl = "http://" + Constant.FILE_STORAGE_HOST + "/" + message.getAttachment().getKey();
		
		// 先显示缩略图和进度指示
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

				imageLoader.loadImage(downloadUrl, null, options, new ImageLoadingListener() {
					@Override
					public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
						Log.d(TAG, "image load failed: " + failReason.getType());
					}

					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						mSaveBtn.setVisibility(View.VISIBLE);
						mProgressTxt.setVisibility(View.GONE);
						mPreviewImg.setImageBitmap(loadedImage);
						//downloadImage = new WeakReference<Bitmap>(loadedImage);
						
						String saveName = FileUtil.getImagePathByWith(message.getWith()) + FileUtil.genRecvVideoName(message.getWith());
						ImageUtil.saveImage(loadedImage, saveName);
						message.getAttachment().setSrcUri(saveName);
						MessageManager.getInstance().updateIMMessage(message);
						
						startBurnTimer();
					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {				
					}

					@Override
					public void onLoadingStarted(String imageUri, View view) {
						
					}
				},
					
				new ImageLoadingProgressListener() {
					@Override
					public void onProgressUpdate(String imageUri, View view,
							int current, int total) {
						int ratio = current * 100 / total;
						if (ratio % 10 == 0) {
							mProgressTxt.setText(ratio + "%");
						}
					}
				});
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				mSpinner.setVisibility(View.GONE);
			}
		});

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
	
	private void startBurnTimer() {
		if (!message.getDestroy().equals(IMMessage.NEVER_BURN) && message.getDir().equals(IMMessage.RECV)) {
			mSaveBtn.setVisibility(View.GONE);
			mTimerBtn.setVisibility(View.VISIBLE);
			burnThread = new BurnTimerThread(timer, new ExpiryTimerListener() {
				
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
			            	mTimerBtn.setText(getResources().getString(R.string.destory));
			            	Burn();
			            }    
			        });				
				}

				@Override
				public void onCancel() {
					
				}
				
			});
			burnThread.start();
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
	public void onBackPressed() {
		if (message.getDestroy().equals(IMMessage.SHOULD_BURN)) {
			if (burnThread != null) {
				burnThread.cancel();
			}
			burnMessage();
			
		}
		super.onBackPressed();
	}
	
	@Override
	public void onStop() {
		if (message.getDestroy().equals(IMMessage.SHOULD_BURN)) {
			if (burnThread != null) {
				burnThread.cancel();
			}
			burnMessage();
			
		}
		super.onStop();
	}
	
	private void burnMessage() {
		if (message.getDir().equals(IMMessage.RECV)) {

    	Log.d(TAG, "remove message id " + message.getId() + ", " + message.getContent());
    	showToast(getResources().getString(R.string.message_had_destoried));
	    	
			Intent intent = new Intent();
			intent.setAction(Constant.DESTROY_RECEIPTS_ACTION);
			intent.putExtra(Constant.DESTROY_RECEIPTS_KEY_MESSAGE, message);
			sendBroadcast(intent);
			
	    	MessageManager.getInstance().delChatHisById(message.getId());
		}
	}
}
