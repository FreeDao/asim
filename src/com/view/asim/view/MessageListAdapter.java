package com.view.asim.view;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.model.ChatMessageItem;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;
import com.view.asim.utils.FaceConversionUtil;
import com.view.asim.utils.ImageUtil;


import com.view.asim.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Images;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * 聊天会话界面适配器
 * 
 * @author xuweinan
 * */
public class MessageListAdapter extends BaseAdapter {
	private static final String TAG = "MessageListAdapter";

	public static final String KEY = "key";
	public static final String VALUE = "value";

	public static final int MAX_TYPE = 0x30;
	
	public static final int VALUE_TIMESTAMP = 0;
	
	public static final int VALUE_LEFT_TEXT = 0x10;
	public static final int VALUE_LEFT_IMAGE = 0x11;
	public static final int VALUE_LEFT_AUDIO = 0x12;
	public static final int VALUE_LEFT_VIDEO = 0x13;
	public static final int VALUE_LEFT_FILE = 0x14;
	public static final int VALUE_LEFT_BURN = 0x15;
	
	public static final int VALUE_RIGHT_TEXT = 0x20;
	public static final int VALUE_RIGHT_IMAGE = 0x21;
	public static final int VALUE_RIGHT_AUDIO = 0x22;
	public static final int VALUE_RIGHT_VIDEO = 0x23;
	public static final int VALUE_RIGHT_FILE = 0x24;
	public static final int VALUE_RIGHT_BURN = 0x25;
	
	public static final int MAX_IMAGE_THUMBNAIL_WIDTH = 200;
	public static final int MAX_IMAGE_THUMBNAIL_HEIGHT = 200;
	public static final int MAX_VIDEO_THUMBNAIL_WIDTH = 200;
	public static final int MAX_VIDEO_THUMBNAIL_HEIGHT = 200;
	
	private DisplayImageOptions options;
	private ImageLoader imageLoader;

	private LayoutInflater mInflater;
	private ListView adapterList;
	private List<ChatMessageItem> items;
	private Context mCntx;
	private User user;
	
	private OnLongClickListener mMessageContentLongClickListener = null;
	private OnClickListener mLeftAudioClickListener = null;
	private OnClickListener mRightAudioClickListener = null;
	private OnClickListener mVideoClickListener = null;
	private OnClickListener mImageClickListener = null;
	private OnClickListener mFileClickListener = null;
	private OnClickListener mLeftBurnClickListener = null;
	private OnClickListener mRightBurnClickListener = null;
	private OnClickListener mSentFailedClickListener = null;

	public MessageListAdapter(Context context, List<ChatMessageItem> items,
			ListView adapterList, User user, ImageLoader loader) {
		this.items = items;
		this.adapterList = adapterList;
		this.user = user;

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mCntx = context;
		
		imageLoader = loader;
		options = new DisplayImageOptions.Builder()
			//.showImageForEmptyUri(R.drawable.image_download_fail_icon)
			//.showImageOnFail(R.drawable.image_download_fail_icon)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.considerExifParams(true)
			.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
			.build();
	}

	@Override
	public int getCount() {
		return items == null ? 0 : items.size();
	}

	@Override
	public Object getItem(int arg0) {
		return items.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	/**
	 * 更新整个 ListView
	 * @param items
	 */
	public void refreshList(List<ChatMessageItem> items) {
		this.items = items;
		this.notifyDataSetChanged();
		adapterList.setSelection(items.size() - 1);
	}
	
	/**
	 * 更新 List 中单个 View（仅限于 IM 消息）
	 * @param item
	 */
	public void refreshItem(ChatMessageItem item) {
		int index = -1;
		int i = -1;
		
		for (i = 0; i < items.size(); i++) {
			if (!items.get(i).getType().equals(ChatMessage.TIMESTAMP)) {
				ChatMessage m = (ChatMessage) items.get(i).getValue();
				if (m.getId().equals(((ChatMessage)item.getValue()).getId())) {
					index = i;
					break;
				}
			}
		}
		
		if (index == -1) {
			Log.d(TAG, "cannot find the view which needs to be updated: " + (ChatMessage)item.getValue());
			return;
		}
		
		int visiblePosition = adapterList.getFirstVisiblePosition(); 
		View view = adapterList.getChildAt(index - visiblePosition);
		items.set(index, item);
		getView(index, view, null);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {

		ImageSize is = null;
		Bitmap bm = null;
		SpannableString spannableString;
		ChatMessage msg = null;
		ChatMessageItem item = items.get(position);
		int type = getItemViewType(position);
		
		ViewHolderTime holderTime = null;
		ViewHolderRightText holderRightText = null;
		ViewHolderRightImg holderRightImg = null;
		ViewHolderRightAudio holderRightAudio = null;
		ViewHolderRightVideo holderRightVideo = null;
		ViewHolderRightFile holderRightFile = null;
		ViewHolderRightBurn holderRightBurn = null;

		ViewHolderLeftText holderLeftText = null;
		ViewHolderLeftImg holderLeftImg = null;
		ViewHolderLeftAudio holderLeftAudio = null;
		ViewHolderLeftVideo holderLeftVideo = null;
		ViewHolderLeftFile holderLeftFile = null;
		ViewHolderLeftBurn holderLeftBurn = null;

		if (convertView == null) {
			switch (type) {

				case VALUE_TIMESTAMP:
					holderTime = new ViewHolderTime();
					convertView = mInflater.inflate(R.layout.list_item_time_tip,
							null);
					holderTime.tvTimeTip = (TextView) convertView
							.findViewById(R.id.tv_time_tip);
					holderTime.tvTimeTip.setText((String)item.getValue());
					convertView.setTag(holderTime);				
					break;
					
				case VALUE_LEFT_TEXT:
					holderLeftText = new ViewHolderLeftText();
					convertView = mInflater.inflate(R.layout.list_item_left_text,
							null);
					holderLeftText.ivLeftIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderLeftText.btnLeftText = (Button) convertView
							.findViewById(R.id.btn_left_text);
					holderLeftText.ivSecurityImg = (ImageView) convertView
							.findViewById(R.id.security_img);
					convertView.setTag(holderLeftText);
					if (mMessageContentLongClickListener != null) {
						holderLeftText.btnLeftText.setOnLongClickListener(mMessageContentLongClickListener);
					}

					break;

				case VALUE_LEFT_IMAGE:
					holderLeftImg = new ViewHolderLeftImg();
					convertView = mInflater.inflate(R.layout.list_item_left_image,
							null);
					holderLeftImg.ivLeftIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderLeftImg.ivLeftImage = (ImageView) convertView
							.findViewById(R.id.iv_left_image_thumb);
					holderLeftImg.ivSecurityImg = (ImageView) convertView
							.findViewById(R.id.security_img);
					
					if (mImageClickListener != null) {
						holderLeftImg.ivLeftImage.setOnClickListener(mImageClickListener);
					}
					
					if (mMessageContentLongClickListener != null) {
						holderLeftImg.ivLeftImage.setOnLongClickListener(mMessageContentLongClickListener);
					}
					convertView.setTag(holderLeftImg);
					break;
					
				case VALUE_LEFT_VIDEO:
					holderLeftVideo = new ViewHolderLeftVideo();
					convertView = mInflater.inflate(R.layout.list_item_left_video,
							null);
					holderLeftVideo.ivLeftIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderLeftVideo.btnLeftVideoThumb = (ImageView) convertView
							.findViewById(R.id.iv_left_video_thumb);	
					holderLeftVideo.ivSecurityImg = (ImageView) convertView
							.findViewById(R.id.security_img);

					if (mVideoClickListener != null) {
						holderLeftVideo.btnLeftVideoThumb.setOnClickListener(mVideoClickListener);
					}
					
					if (mMessageContentLongClickListener != null) {
						holderLeftVideo.btnLeftVideoThumb.setOnLongClickListener(mMessageContentLongClickListener);
					}
					convertView.setTag(holderLeftVideo);
					break;

				case VALUE_LEFT_AUDIO:
					holderLeftAudio = new ViewHolderLeftAudio();
					convertView = mInflater.inflate(R.layout.list_item_left_audio,
							null);
					holderLeftAudio.ivLeftIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderLeftAudio.btnLeftAudio = (Button) convertView
							.findViewById(R.id.btn_left_audio);
					holderLeftAudio.ivSecurityImg = (ImageView) convertView
							.findViewById(R.id.security_img);
					
					if (mLeftAudioClickListener != null) {
						holderLeftAudio.btnLeftAudio.setOnClickListener(mLeftAudioClickListener);
					}
					if (mMessageContentLongClickListener != null) {
						holderLeftAudio.btnLeftAudio.setOnLongClickListener(mMessageContentLongClickListener);
					}
					convertView.setTag(holderLeftAudio);
					break;
					
				case VALUE_LEFT_BURN:
					holderLeftBurn = new ViewHolderLeftBurn();
					convertView = mInflater.inflate(R.layout.list_item_left_burn,
							null);
					holderLeftBurn.ivLeftIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderLeftBurn.btnLeftBurn = (ImageView) convertView
							.findViewById(R.id.btn_left_burn);
					holderLeftBurn.ivSecurityImg = (ImageView) convertView
							.findViewById(R.id.security_img);
					
					if (mLeftBurnClickListener != null) {
						holderLeftBurn.btnLeftBurn.setOnClickListener(mLeftBurnClickListener);
					}
					if (mMessageContentLongClickListener != null) {
						holderLeftBurn.btnLeftBurn.setOnLongClickListener(mMessageContentLongClickListener);
					}
					convertView.setTag(holderLeftBurn);
					break;

				case VALUE_RIGHT_TEXT:
					holderRightText= new ViewHolderRightText();
					convertView = mInflater.inflate(R.layout.list_item_right_text,
							null);
					holderRightText.ivRightIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderRightText.btnRightText = (Button) convertView
							.findViewById(R.id.btn_right_text);
					holderRightText.ivFailedImg = (ImageView) convertView.findViewById(R.id.sent_failed_img);
					holderRightText.ivProgressImg = (ImageView) convertView.findViewById(R.id.sending_progress_img);
					holderRightText.ivSecurityImg = (ImageView) convertView.findViewById(R.id.security_img);

					if (mSentFailedClickListener != null) {
						holderRightText.ivFailedImg.setOnClickListener(mSentFailedClickListener);
					}
					if (mMessageContentLongClickListener != null) {
						holderRightText.btnRightText.setOnLongClickListener(mMessageContentLongClickListener);
					}
					convertView.setTag(holderRightText);
					break;

				case VALUE_RIGHT_IMAGE:
					holderRightImg = new ViewHolderRightImg();
					convertView = mInflater.inflate(R.layout.list_item_right_image,
							null);
					holderRightImg.ivRightIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderRightImg.ivRightImage = (ImageView) convertView
							.findViewById(R.id.iv_right_image_thumb);
					holderRightImg.ivFailedImg = (ImageView) convertView.findViewById(R.id.sent_failed_img);
					holderRightImg.ivProgressImg = (ImageView) convertView.findViewById(R.id.sending_progress_img);
					holderRightImg.tvProgressTxt = (TextView) convertView.findViewById(R.id.progress_txt);
					holderRightImg.ivSecurityImg = (ImageView) convertView.findViewById(R.id.security_img);

					if (mSentFailedClickListener != null) {
						holderRightImg.ivFailedImg.setOnClickListener(mSentFailedClickListener);
					}
					if (mImageClickListener != null) {
						holderRightImg.ivRightImage.setOnClickListener(mImageClickListener);
					}
					if (mMessageContentLongClickListener != null) {
						holderRightImg.ivRightImage.setOnLongClickListener(mMessageContentLongClickListener);
					}
					convertView.setTag(holderRightImg);
					break;

				case VALUE_RIGHT_AUDIO:
					holderRightAudio = new ViewHolderRightAudio();
					convertView = mInflater.inflate(R.layout.list_item_right_audio,
							null);
					holderRightAudio.ivRightIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderRightAudio.btnRightAudio = (Button) convertView
							.findViewById(R.id.btn_right_audio);
					holderRightAudio.ivFailedImg = (ImageView) convertView.findViewById(R.id.sent_failed_img);
					holderRightAudio.ivProgressImg = (ImageView) convertView.findViewById(R.id.sending_progress_img);
					holderRightAudio.ivSecurityImg = (ImageView) convertView.findViewById(R.id.security_img);
					holderRightAudio.ivSecurityImg = (ImageView) convertView.findViewById(R.id.security_img);

					if (mSentFailedClickListener != null) {
						holderRightAudio.ivFailedImg.setOnClickListener(mSentFailedClickListener);
					}
					if (mRightAudioClickListener != null) {
						holderRightAudio.btnRightAudio.setOnClickListener(mRightAudioClickListener);
					}
					if (mMessageContentLongClickListener != null) {
						holderRightAudio.btnRightAudio.setOnLongClickListener(mMessageContentLongClickListener);
					}
					convertView.setTag(holderRightAudio);
					break;
					
				case VALUE_RIGHT_VIDEO:
					holderRightVideo = new ViewHolderRightVideo();
					convertView = mInflater.inflate(R.layout.list_item_right_video,
							null);
					holderRightVideo.ivRightIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderRightVideo.btnRightVideoThumb = (ImageView) convertView
							.findViewById(R.id.iv_right_video_thumb);
					holderRightVideo.ivFailedImg = (ImageView) convertView.findViewById(R.id.sent_failed_img);
					holderRightVideo.ivProgressImg = (ImageView) convertView.findViewById(R.id.sending_progress_img);
					holderRightVideo.tvProgressTxt = (TextView) convertView.findViewById(R.id.progress_txt);
					holderRightVideo.ivSecurityImg = (ImageView) convertView.findViewById(R.id.security_img);

					if (mSentFailedClickListener != null) {
						holderRightVideo.ivFailedImg.setOnClickListener(mSentFailedClickListener);
					}
					if (mVideoClickListener != null) {
						holderRightVideo.btnRightVideoThumb.setOnClickListener(mVideoClickListener);
					}
					if (mMessageContentLongClickListener != null) {
						holderRightVideo.btnRightVideoThumb.setOnLongClickListener(mMessageContentLongClickListener);
					}
					convertView.setTag(holderRightVideo);
					break;
					
				case VALUE_RIGHT_BURN:
					holderRightBurn = new ViewHolderRightBurn();
					convertView = mInflater.inflate(R.layout.list_item_right_burn,
							null);
					holderRightBurn.ivRightIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderRightBurn.btnRightBurn = (ImageView) convertView
							.findViewById(R.id.btn_right_burn);
					holderRightBurn.ivFailedImg = (ImageView) convertView.findViewById(R.id.sent_failed_img);
					holderRightBurn.ivProgressImg = (ImageView) convertView.findViewById(R.id.sending_progress_img);
					holderRightBurn.ivSecurityImg = (ImageView) convertView.findViewById(R.id.security_img);

					if (mSentFailedClickListener != null) {
						holderRightBurn.ivFailedImg.setOnClickListener(mSentFailedClickListener);
					}
					if (mRightBurnClickListener != null) {
						holderRightBurn.btnRightBurn.setOnClickListener(mRightBurnClickListener);
					}
					if (mMessageContentLongClickListener != null) {
						holderRightBurn.btnRightBurn.setOnLongClickListener(mMessageContentLongClickListener);
					}
					convertView.setTag(holderRightBurn);
					break;

				default:
					break;
			}
		} else {		
			switch (type) {
				case VALUE_TIMESTAMP:
					holderTime = (ViewHolderTime)convertView.getTag();
					break;
					
				case VALUE_LEFT_TEXT:
					holderLeftText = (ViewHolderLeftText)convertView.getTag();
					break;
					
				case VALUE_LEFT_IMAGE:
					holderLeftImg = (ViewHolderLeftImg)convertView.getTag();
					break;
					
				case VALUE_LEFT_AUDIO:
					holderLeftAudio = (ViewHolderLeftAudio)convertView.getTag();
					break;
					
				case VALUE_LEFT_VIDEO:
					holderLeftVideo = (ViewHolderLeftVideo)convertView.getTag();
					break;
					
				case VALUE_LEFT_BURN:
					holderLeftBurn = (ViewHolderLeftBurn) convertView.getTag();
					break;
					
				case VALUE_RIGHT_TEXT:
					holderRightText = (ViewHolderRightText)convertView.getTag();
					break;
					
				case VALUE_RIGHT_IMAGE:
					holderRightImg = (ViewHolderRightImg)convertView.getTag();
					break;
					
				case VALUE_RIGHT_AUDIO:
					holderRightAudio = (ViewHolderRightAudio)convertView.getTag();
					break;
					
				case VALUE_RIGHT_VIDEO:
					holderRightVideo = (ViewHolderRightVideo)convertView.getTag();
					break;
					
				case VALUE_RIGHT_BURN:
					holderRightBurn = (ViewHolderRightBurn) convertView.getTag();
					break;

				default:
					break;
			}
		}
		
		switch (type) {
			case VALUE_TIMESTAMP:
				holderTime.tvTimeTip.setText((String)item.getValue());
				break;
				
			case VALUE_LEFT_TEXT:
				msg = (ChatMessage)item.getValue();
				if (msg.getContent() != null) {
					spannableString = FaceConversionUtil.getInstace().
							getExpressionString(mCntx, msg.getContent(), 60);
					holderLeftText.btnLeftText.setText(spannableString);
				}
				else {
					holderLeftText.btnLeftText.setText("");
				}
				setAvatarImage(holderLeftText.ivLeftIcon, user);
				if (msg.getSecurity().equals(IMMessage.ENCRYPTION)) {
					holderLeftText.ivSecurityImg.setVisibility(View.VISIBLE);
				}
				else {
					holderLeftText.ivSecurityImg.setVisibility(View.GONE);
				}

				holderLeftText.btnLeftText.setTag(msg);
				break;
				
			case VALUE_LEFT_IMAGE:
				msg = (ChatMessage)item.getValue();
				setAvatarImage(holderLeftImg.ivLeftIcon, user);
				final ImageView leftImage = holderLeftImg.ivLeftImage;
				is = new ImageSize(MAX_IMAGE_THUMBNAIL_WIDTH, MAX_IMAGE_THUMBNAIL_HEIGHT);
				String imgPath;
				try {
					imgPath = msg.getAttachment().getThumbUri();
				} catch (Exception e) {
					e.printStackTrace();
					imgPath = "";
				}
				imageLoader.loadImage("file://" + imgPath, is, options, new SimpleImageLoadingListener() {
				    @Override
				    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						Log.d(TAG, "loader img bitmap width " + loadedImage.getWidth() + ", height " + loadedImage.getHeight());
						
						LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
								leftImage.getPaddingLeft() + loadedImage.getWidth() + leftImage.getPaddingRight() , 
								leftImage.getPaddingTop() + loadedImage.getHeight() + leftImage.getPaddingBottom());
						leftImage.setLayoutParams(layoutParams);
						new RoundedBitmapDisplayer(15).display(loadedImage, new ImageViewAware(leftImage), null);
						
				    }
				});
				if (msg.getSecurity().equals(IMMessage.ENCRYPTION)) {
					holderLeftImg.ivSecurityImg.setVisibility(View.VISIBLE);
				}
				else {
					holderLeftImg.ivSecurityImg.setVisibility(View.GONE);
				}

				
				holderLeftImg.ivLeftImage.setTag(msg);
				break;
				
			case VALUE_LEFT_AUDIO:
				msg = (ChatMessage)item.getValue();
				setAvatarImage(holderLeftAudio.ivLeftIcon, user);
				holderLeftAudio.btnLeftAudio.setText(msg.getAttachment().getAudioLength() + "''");
				if (msg.getSecurity().equals(IMMessage.ENCRYPTION)) {
					holderLeftAudio.ivSecurityImg.setVisibility(View.VISIBLE);
				}
				else {
					holderLeftAudio.ivSecurityImg.setVisibility(View.GONE);
				}

				
				holderLeftAudio.btnLeftAudio.setTag(msg);
				
				break;
				
			case VALUE_LEFT_VIDEO:
				msg = (ChatMessage)item.getValue();
				setAvatarImage(holderLeftVideo.ivLeftIcon, user);
				
				final ImageView leftVideoThumb = holderLeftVideo.btnLeftVideoThumb;
				is = new ImageSize(MAX_IMAGE_THUMBNAIL_WIDTH, MAX_IMAGE_THUMBNAIL_HEIGHT);
				
				String vPath;
				try {
					vPath = msg.getAttachment().getThumbUri();
				} catch (Exception e) {
					e.printStackTrace();
					vPath = "";
				}
				imageLoader.loadImage("file://" + vPath, is, options, new SimpleImageLoadingListener() {
				    @Override
				    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						Log.d(TAG, "loader img bitmap width " + loadedImage.getWidth() + ", height " + loadedImage.getHeight());
						
						RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
								leftVideoThumb.getPaddingLeft() + loadedImage.getWidth() + leftVideoThumb.getPaddingRight() , 
								leftVideoThumb.getPaddingTop() + loadedImage.getHeight() + leftVideoThumb.getPaddingBottom());
						leftVideoThumb.setLayoutParams(layoutParams);
						new RoundedBitmapDisplayer(15).display(loadedImage, new ImageViewAware(leftVideoThumb), null);
						
				    }
				});
				
				if (msg.getSecurity().equals(IMMessage.ENCRYPTION)) {
					holderLeftVideo.ivSecurityImg.setVisibility(View.VISIBLE);
				}
				else {
					holderLeftVideo.ivSecurityImg.setVisibility(View.GONE);
				}

				holderLeftVideo.btnLeftVideoThumb.setTag(msg);
				break;
				
			case VALUE_LEFT_BURN:
				msg = (ChatMessage)item.getValue();
				setAvatarImage(holderLeftBurn.ivLeftIcon, user);
				
				if(msg.getDestroy().equals(IMMessage.SHOULD_BURN)) {
					holderLeftBurn.btnLeftBurn.setImageResource(R.drawable.chat_class_burn_left_default);
				}
				else {
					holderLeftBurn.btnLeftBurn.setImageResource(R.drawable.image_burn_left_readed);
				}
				
				if (msg.getSecurity().equals(IMMessage.ENCRYPTION)) {
					holderLeftBurn.ivSecurityImg.setVisibility(View.VISIBLE);
				}
				else {
					holderLeftBurn.ivSecurityImg.setVisibility(View.GONE);
				}

				holderLeftBurn.btnLeftBurn.setTag(msg);
				break;
				
			case VALUE_RIGHT_TEXT:
				msg = (ChatMessage)item.getValue();
				if(msg.getStatus().equals(IMMessage.ERROR)) {
					AnimationDrawable anim = (AnimationDrawable) holderRightText.ivProgressImg.getDrawable();
					anim.stop();

					holderRightText.ivProgressImg.setVisibility(View.GONE);
					holderRightText.ivFailedImg.setVisibility(View.VISIBLE);
				}
				else if(msg.getStatus().equals(IMMessage.INPROGRESS)) {
					holderRightText.ivFailedImg.setVisibility(View.GONE);
					holderRightText.ivProgressImg.setVisibility(View.VISIBLE);
					AnimationDrawable anim = (AnimationDrawable) holderRightText.ivProgressImg.getDrawable();
					if(!anim.isRunning()) {
						anim.start();
					}
				}
				else {
					AnimationDrawable anim = (AnimationDrawable) holderRightText.ivProgressImg.getDrawable();
					anim.stop();

					holderRightText.ivFailedImg.setVisibility(View.GONE);
					holderRightText.ivProgressImg.setVisibility(View.GONE);
				}
				if (msg.getSecurity().equals(IMMessage.ENCRYPTION)) {
					holderRightText.ivSecurityImg.setVisibility(View.VISIBLE);
				}
				else {
					holderRightText.ivSecurityImg.setVisibility(View.GONE);
				}

				if (msg.getContent() != null) {
					spannableString = FaceConversionUtil.getInstace().
							getExpressionString(mCntx, msg.getContent(), 60);
					holderRightText.btnRightText.setText(spannableString);
				}
				else {
					holderRightText.btnRightText.setText("");
				}
				spannableString = FaceConversionUtil.getInstace().
						getExpressionString(mCntx, msg.getContent(), 60);
				setAvatarImage(holderRightText.ivRightIcon, ContacterManager.userMe);
				holderRightText.btnRightText.setTag(msg);
				holderRightText.ivFailedImg.setTag(msg);

				break;
				
			case VALUE_RIGHT_IMAGE:
				msg = (ChatMessage)item.getValue();
				if(msg.getStatus().equals(IMMessage.ERROR)) {
					AnimationDrawable anim = (AnimationDrawable) holderRightImg.ivProgressImg.getDrawable();
					anim.stop();

					holderRightImg.ivProgressImg.setVisibility(View.GONE);
					holderRightImg.tvProgressTxt.setVisibility(View.GONE);
					holderRightImg.ivFailedImg.setVisibility(View.VISIBLE);
				}
				else if(msg.getStatus().equals(IMMessage.INPROGRESS)) {
					holderRightImg.ivFailedImg.setVisibility(View.GONE);
					holderRightImg.ivProgressImg.setVisibility(View.VISIBLE);
					holderRightImg.tvProgressTxt.setText(item.getProgress() + "%");

					AnimationDrawable anim = (AnimationDrawable) holderRightImg.ivProgressImg.getDrawable();
					if(!anim.isRunning()) {
						anim.start();
					}
				}
				else {
					AnimationDrawable anim = (AnimationDrawable) holderRightImg.ivProgressImg.getDrawable();
					anim.stop();

					holderRightImg.ivFailedImg.setVisibility(View.GONE);
					holderRightImg.ivProgressImg.setVisibility(View.GONE);
					holderRightImg.tvProgressTxt.setVisibility(View.GONE);
				}
				
				if (msg.getSecurity().equals(IMMessage.ENCRYPTION)) {
					holderRightImg.ivSecurityImg.setVisibility(View.VISIBLE);
				}
				else {
					holderRightImg.ivSecurityImg.setVisibility(View.GONE);
				}

				
				setAvatarImage(holderRightImg.ivRightIcon, ContacterManager.userMe);
				Log.d(TAG, "msg list view pos " + position + ", path " + msg.getAttachment().getSrcUri());
				final ImageView rightImage = holderRightImg.ivRightImage;
				is = new ImageSize(MAX_IMAGE_THUMBNAIL_WIDTH, MAX_IMAGE_THUMBNAIL_HEIGHT);
				imageLoader.loadImage("file://" + msg.getAttachment().getSrcUri(), is, options, new SimpleImageLoadingListener() {
				    @Override
				    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						Log.d(TAG, "loader img bitmap width " + loadedImage.getWidth() + ", height " + loadedImage.getHeight());
						
						RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
								rightImage.getPaddingLeft() + loadedImage.getWidth() + rightImage.getPaddingRight() , 
								rightImage.getPaddingTop() + loadedImage.getHeight() + rightImage.getPaddingBottom());
						rightImage.setLayoutParams(layoutParams);
						new RoundedBitmapDisplayer(15).display(loadedImage, new ImageViewAware(rightImage), null);
						
				    }
				});
				holderRightImg.ivRightImage.setTag(msg);
				holderRightImg.ivFailedImg.setTag(msg);

				break;
				
			case VALUE_RIGHT_AUDIO:
				msg = (ChatMessage)item.getValue();
				if(msg.getStatus().equals(IMMessage.ERROR)) {
					AnimationDrawable anim = (AnimationDrawable) holderRightAudio.ivProgressImg.getDrawable();
					anim.stop();

					holderRightAudio.ivProgressImg.setVisibility(View.GONE);
					holderRightAudio.ivFailedImg.setVisibility(View.VISIBLE);
				}
				else if(msg.getStatus().equals(IMMessage.INPROGRESS)) {
					holderRightAudio.ivFailedImg.setVisibility(View.GONE);
					holderRightAudio.ivProgressImg.setVisibility(View.VISIBLE);
					
					AnimationDrawable anim = (AnimationDrawable) holderRightAudio.ivProgressImg.getDrawable();
					if(!anim.isRunning()) {
						anim.start();
					}
				}
				else {
					AnimationDrawable anim = (AnimationDrawable) holderRightAudio.ivProgressImg.getDrawable();
					anim.stop();

					holderRightAudio.ivFailedImg.setVisibility(View.GONE);
					holderRightAudio.ivProgressImg.setVisibility(View.GONE);
				}
				
				if (msg.getSecurity().equals(IMMessage.ENCRYPTION)) {
					holderRightAudio.ivSecurityImg.setVisibility(View.VISIBLE);
				}
				else {
					holderRightAudio.ivSecurityImg.setVisibility(View.GONE);
				}
				setAvatarImage(holderRightAudio.ivRightIcon, ContacterManager.userMe);
				holderRightAudio.btnRightAudio.setText(msg.getAttachment().getAudioLength() + "''");
				holderRightAudio.btnRightAudio.setTag(msg);		
				holderRightAudio.ivFailedImg.setTag(msg);				

				break;
				
			case VALUE_RIGHT_VIDEO:
				msg = (ChatMessage)item.getValue();
				if(msg.getStatus().equals(IMMessage.ERROR)) {
					AnimationDrawable anim = (AnimationDrawable) holderRightVideo.ivProgressImg.getDrawable();
					anim.stop();

					holderRightVideo.ivProgressImg.setVisibility(View.GONE);
					holderRightVideo.tvProgressTxt.setVisibility(View.GONE);
					holderRightVideo.ivFailedImg.setVisibility(View.VISIBLE);
				}
				else if(msg.getStatus().equals(IMMessage.INPROGRESS)) {
					holderRightVideo.ivFailedImg.setVisibility(View.GONE);
					holderRightVideo.ivProgressImg.setVisibility(View.VISIBLE);
					holderRightVideo.tvProgressTxt.setText(item.getProgress() + "%");

					AnimationDrawable anim = (AnimationDrawable) holderRightVideo.ivProgressImg.getDrawable();
					if(!anim.isRunning()) {
						anim.start();
					}
				}
				else {
					AnimationDrawable anim = (AnimationDrawable) holderRightVideo.ivProgressImg.getDrawable();
					anim.stop();

					holderRightVideo.ivFailedImg.setVisibility(View.GONE);
					holderRightVideo.ivProgressImg.setVisibility(View.GONE);
					holderRightVideo.tvProgressTxt.setVisibility(View.GONE);
				}
				
				if (msg.getSecurity().equals(IMMessage.ENCRYPTION)) {
					holderRightVideo.ivSecurityImg.setVisibility(View.VISIBLE);
				}
				else {
					holderRightVideo.ivSecurityImg.setVisibility(View.GONE);
				}

				setAvatarImage(holderRightVideo.ivRightIcon, ContacterManager.userMe);
				final ImageView rightVideoThumb = holderRightVideo.btnRightVideoThumb;
				is = new ImageSize(MAX_IMAGE_THUMBNAIL_WIDTH, MAX_IMAGE_THUMBNAIL_HEIGHT);
				imageLoader.loadImage("file://" + msg.getAttachment().getThumbUri(), is, options, new SimpleImageLoadingListener() {
				    @Override
				    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						Log.d(TAG, "loader img bitmap width " + loadedImage.getWidth() + ", height " + loadedImage.getHeight());
						
						RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
								rightVideoThumb.getPaddingLeft() + loadedImage.getWidth() + rightVideoThumb.getPaddingRight() , 
								rightVideoThumb.getPaddingTop() + loadedImage.getHeight() + rightVideoThumb.getPaddingBottom());
						rightVideoThumb.setLayoutParams(layoutParams);
						new RoundedBitmapDisplayer(15).display(loadedImage, new ImageViewAware(rightVideoThumb), null);
						
				    }
				});
				holderRightVideo.btnRightVideoThumb.setTag(msg);
				holderRightVideo.ivFailedImg.setTag(msg);

				break;
				
			case VALUE_RIGHT_BURN:
				msg = (ChatMessage)item.getValue();
				if(msg.getStatus().equals(IMMessage.ERROR)) {
					AnimationDrawable anim = (AnimationDrawable) holderRightBurn.ivProgressImg.getDrawable();
					anim.stop();

					holderRightBurn.ivProgressImg.setVisibility(View.GONE);
					//holderRightBurn.tvProgressTxt.setVisibility(View.GONE);
					holderRightBurn.ivFailedImg.setVisibility(View.VISIBLE);
				}
				else if(msg.getStatus().equals(IMMessage.INPROGRESS)) {
					holderRightBurn.ivFailedImg.setVisibility(View.GONE);
					holderRightBurn.ivProgressImg.setVisibility(View.VISIBLE);
					//holderRightBurn.tvProgressTxt.setText(item.getProgress() + "%");
					
					AnimationDrawable anim = (AnimationDrawable) holderRightBurn.ivProgressImg.getDrawable();
					if(!anim.isRunning()) {
						anim.start();
					}
				}
				else {
					AnimationDrawable anim = (AnimationDrawable) holderRightBurn.ivProgressImg.getDrawable();
					anim.stop();

					holderRightBurn.ivFailedImg.setVisibility(View.GONE);
					holderRightBurn.ivProgressImg.setVisibility(View.GONE);
					//holderRightBurn.tvProgressTxt.setVisibility(View.GONE);
				}
				
				if (msg.getSecurity().equals(IMMessage.ENCRYPTION)) {
					holderRightBurn.ivSecurityImg.setVisibility(View.VISIBLE);
				}
				else {
					holderRightBurn.ivSecurityImg.setVisibility(View.GONE);
				}
				
				if(msg.getDestroy().equals(IMMessage.SHOULD_BURN)) {
					holderRightBurn.btnRightBurn.setImageResource(R.drawable.chat_class_burn_right_default);
				}
				else {
					holderRightBurn.btnRightBurn.setImageResource(R.drawable.image_burn_right_readed);
				}
				
				setAvatarImage(holderRightBurn.ivRightIcon, ContacterManager.userMe);					
				holderRightBurn.btnRightBurn.setTag(msg);
				holderRightBurn.ivFailedImg.setTag(msg);

				break;

			default:
				break;
		}
		
		return convertView;
	}
	
	
	protected void setAvatarImage(ImageView v, User u) {
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
	
	public int getViewPositionByMessage(ChatMessage m) {
		int i = 0;
		for(i = 0; i < items.size(); i++) {
			if (!items.get(i).getType().equals(ChatMessage.TIMESTAMP)) {
				ChatMessage cm = (ChatMessage) items.get(i).getValue();
				if (cm.getId().equals(m.getId())) {
					return i;
				}
				
			}
		}
		return -1;
	}
	
	public void setMessageContentLongClickListener(OnLongClickListener listener) {
		this.mMessageContentLongClickListener = listener;
	}
	
	public void setImageClickListener(OnClickListener listener) {

		this.mImageClickListener = listener;
	}
	
	public void setLeftAudioClickListener(OnClickListener listener) {

		this.mLeftAudioClickListener = listener;
	}
	
	public void setRightAudioClickListener(OnClickListener listener) {

		this.mRightAudioClickListener = listener;
	}
	
	public void setVideoClickListener(OnClickListener listener) {

		this.mVideoClickListener = listener;
	}
	
	public void setFileClickListener(OnClickListener listener) {

		this.mFileClickListener = listener;
	}
	
	public void setLeftBurnClickListener(OnClickListener listener) {

		this.mLeftBurnClickListener = listener;
	}
	
	public void setRightBurnClickListener(OnClickListener listener) {

		this.mRightBurnClickListener = listener;
	}
	
	public void setSentFailedClickListener(OnClickListener listener) {

		this.mSentFailedClickListener = listener;
	}
	
	private int parseMsgType(ChatMessageItem msg) {
		String t = msg.getType();
		
		if (t.equals(ChatMessage.TIMESTAMP)) {
			return VALUE_TIMESTAMP;
		}

		ChatMessage m = (ChatMessage)msg.getValue();

		if (m.getDir().equals(IMMessage.SEND)) {
			
			if (m.getDestroy().equals(IMMessage.SHOULD_BURN) || m.getDestroy().equals(IMMessage.BURNED)) {
				return VALUE_RIGHT_BURN;
			}
			
			if (t.equals(ChatMessage.CHAT_TEXT)) {
				return VALUE_RIGHT_TEXT;
			}
			else if (t.equals(ChatMessage.CHAT_IMAGE)) {
				return VALUE_RIGHT_IMAGE;
			}
			else if (t.equals(ChatMessage.CHAT_AUDIO)) {
				return VALUE_RIGHT_AUDIO;
			}
			else if (t.equals(ChatMessage.CHAT_VIDEO)) {
				return VALUE_RIGHT_VIDEO;
			}
			else {
				return VALUE_RIGHT_FILE;
			}
		}
		else {
			if (m.getDestroy().equals(IMMessage.SHOULD_BURN) || m.getDestroy().equals(IMMessage.BURNED)) {
				return VALUE_LEFT_BURN;
			}
			
			if (t.equals(ChatMessage.CHAT_TEXT)) {
				return VALUE_LEFT_TEXT;
			}
			else if (t.equals(ChatMessage.CHAT_IMAGE)) {
				return VALUE_LEFT_IMAGE;
			}
			else if (t.equals(ChatMessage.CHAT_AUDIO)) {
				return VALUE_LEFT_AUDIO;
			}
			else if (t.equals(ChatMessage.CHAT_VIDEO)) {
				return VALUE_LEFT_VIDEO;
			}
			else {
				return VALUE_LEFT_FILE;
			}
		}
	}

	@Override
	public int getItemViewType(int position) {

		ChatMessageItem msg = items.get(position);
		int type = parseMsgType(msg);
		return type;
	}
	
	@Override
	public int getViewTypeCount() {
		return MAX_TYPE;
	}

	class ViewHolderTime {
		private TextView tvTimeTip;
	}

	class ViewHolderRightText {
		private ImageView ivRightIcon;
		private Button btnRightText;
		private ImageView ivFailedImg;
		private ImageView ivProgressImg;
		private ImageView ivSecurityImg;
	}
	
	class ViewHolderRightBurn {
		private ImageView ivRightIcon;
		private ImageView btnRightBurn;
		private ImageView ivFailedImg;
		private ImageView ivProgressImg;
		private TextView tvProgressTxt;
		private ImageView ivSecurityImg;
	}

	class ViewHolderRightImg {
		private ImageView ivRightIcon;
		private ImageView ivRightImage;
		private ImageView ivFailedImg;
		private ImageView ivProgressImg;
		private TextView tvProgressTxt;
		private ImageView ivSecurityImg;
	}

	class ViewHolderRightAudio {
		private ImageView ivRightIcon;
		private Button btnRightAudio;
		private ImageView ivFailedImg;
		private ImageView ivProgressImg;
		private ImageView ivSecurityImg;
	}
	
	class ViewHolderRightVideo {
		private ImageView ivRightIcon;
		private ImageView btnRightVideoThumb;
		private ImageView ivFailedImg;
		private ImageView ivProgressImg;
		private TextView tvProgressTxt;
		private ImageView ivSecurityImg;
	}	
	
	class ViewHolderRightFile {
		private ImageView ivRightIcon;
		private Button btnRightFileName;
		private ImageView ivFailedImg;
		private ImageView ivProgressImg;
		private TextView tvProgressTxt;
		private ImageView ivSecurityImg;
	}

	class ViewHolderLeftText {
		private ImageView ivLeftIcon;
		private Button btnLeftText;
		private ImageView ivSecurityImg;
	}
	
	class ViewHolderLeftBurn {
		private ImageView ivLeftIcon;
		private ImageView btnLeftBurn;
		private ImageView ivSecurityImg;
	}
	
	class ViewHolderLeftImg {
		private ImageView ivLeftIcon;
		private ImageView ivLeftImage;
		private ImageView ivSecurityImg;
	}

	class ViewHolderLeftAudio {
		private ImageView ivLeftIcon;
		private Button btnLeftAudio;
		private ImageView ivSecurityImg;
	}
	
	class ViewHolderLeftVideo {
		private ImageView ivLeftIcon;
		private ImageView btnLeftVideoThumb;
		private ImageView ivSecurityImg;
	}	
	
	class ViewHolderLeftFile {
		private ImageView ivLeftIcon;
		private Button btnLeftFileName;
		private ImageView ivSecurityImg;
	}
	

}
