package com.view.asim.view;

import java.util.ArrayList;
import java.util.List;

import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.model.ChatMessageItem;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;
import com.view.asim.util.FaceConversionUtil;
import com.view.asim.util.ImageUtil;


import com.view.asim.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Images;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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

	public MessageListAdapter(Context context, List<ChatMessageItem> items,
			ListView adapterList, User user) {
		this.items = items;
		this.adapterList = adapterList;
		this.user = user;

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mCntx = context;
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

	public void refreshList(List<ChatMessageItem> items) {
		this.items = items;
		this.notifyDataSetChanged();
		adapterList.setSelection(items.size() - 1);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {

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
					msg = (ChatMessage)item.getValue();
					
					holderLeftText = new ViewHolderLeftText();
					convertView = mInflater.inflate(R.layout.list_item_left_text,
							null);
					holderLeftText.ivLeftIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderLeftText.btnLeftText = (Button) convertView
							.findViewById(R.id.btn_left_text);
					spannableString = FaceConversionUtil.getInstace().
							getExpressionString(mCntx, msg.getContent(), 60);

					setAvatarImage(holderLeftText.ivLeftIcon, user);
					holderLeftText.btnLeftText.setText(spannableString);
					if (mMessageContentLongClickListener != null) {
						holderLeftText.btnLeftText.setOnLongClickListener(mMessageContentLongClickListener);
					}
					holderLeftText.btnLeftText.setTag(msg);

					convertView.setTag(holderLeftText);
					break;

				case VALUE_LEFT_IMAGE:
					msg = (ChatMessage)item.getValue();
					holderLeftImg = new ViewHolderLeftImg();
					convertView = mInflater.inflate(R.layout.list_item_left_image,
							null);
					holderLeftImg.ivLeftIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderLeftImg.ivLeftImage = (ImageView) convertView
							.findViewById(R.id.iv_left_image_thumb);
					setAvatarImage(holderLeftImg.ivLeftIcon, user);
					
					bm = BitmapFactory.decodeFile(msg.getAttachment().getThumbUri());
					holderLeftImg.ivLeftImage.setImageBitmap(bm);
					holderLeftImg.ivLeftImage.setTag(msg);
					
					if (mImageClickListener != null) {
						holderLeftImg.ivLeftImage.setOnClickListener(mImageClickListener);
					}
					
					if (mMessageContentLongClickListener != null) {
						holderLeftImg.ivLeftImage.setOnLongClickListener(mMessageContentLongClickListener);
					}
					
					convertView.setTag(holderLeftImg);
					break;
					
				case VALUE_LEFT_VIDEO:
					msg = (ChatMessage)item.getValue();
					holderLeftVideo = new ViewHolderLeftVideo();
					convertView = mInflater.inflate(R.layout.list_item_left_video,
							null);
					holderLeftVideo.ivLeftIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderLeftVideo.btnLeftVideoThumb = (ImageView) convertView
							.findViewById(R.id.iv_left_video_thumb);
					setAvatarImage(holderLeftVideo.ivLeftIcon, user);
					
					bm = BitmapFactory.decodeFile(msg.getAttachment().getThumbUri());
					holderLeftVideo.btnLeftVideoThumb.setImageBitmap(bm);
					
					holderLeftVideo.btnLeftVideoThumb.setTag(msg);
					
					if (mVideoClickListener != null) {
						holderLeftVideo.btnLeftVideoThumb.setOnClickListener(mVideoClickListener);
					}
					
					if (mMessageContentLongClickListener != null) {
						holderLeftVideo.btnLeftVideoThumb.setOnLongClickListener(mMessageContentLongClickListener);
					}
					convertView.setTag(holderLeftVideo);
					break;

				case VALUE_LEFT_AUDIO:
					msg = (ChatMessage)item.getValue();

					holderLeftAudio = new ViewHolderLeftAudio();
					convertView = mInflater.inflate(R.layout.list_item_left_audio,
							null);
					holderLeftAudio.ivLeftIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderLeftAudio.btnLeftAudio = (Button) convertView
							.findViewById(R.id.btn_left_audio);
					setAvatarImage(holderLeftAudio.ivLeftIcon, user);
					holderLeftAudio.btnLeftAudio.setText(msg.getAttachment().getAudioLength() + "''");
					
					holderLeftAudio.btnLeftAudio.setTag(msg);
					
					if (mLeftAudioClickListener != null) {
						holderLeftAudio.btnLeftAudio.setOnClickListener(mLeftAudioClickListener);
					}
					if (mMessageContentLongClickListener != null) {
						holderLeftAudio.btnLeftAudio.setOnLongClickListener(mMessageContentLongClickListener);
					}
					
					convertView.setTag(holderLeftAudio);
					break;
					
				case VALUE_LEFT_BURN:
					msg = (ChatMessage)item.getValue();

					holderLeftBurn = new ViewHolderLeftBurn();
					convertView = mInflater.inflate(R.layout.list_item_left_burn,
							null);
					holderLeftBurn.ivLeftIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderLeftBurn.btnLeftBurn = (Button) convertView
							.findViewById(R.id.btn_left_burn);
					
					setAvatarImage(holderLeftBurn.ivLeftIcon, user);					
					holderLeftBurn.btnLeftBurn.setTag(msg);
					
					if (mLeftBurnClickListener != null) {
						holderLeftBurn.btnLeftBurn.setOnClickListener(mLeftBurnClickListener);
					}
					if (mMessageContentLongClickListener != null) {
						holderLeftBurn.btnLeftBurn.setOnLongClickListener(mMessageContentLongClickListener);
					}
					
					convertView.setTag(holderLeftBurn);
					break;

				case VALUE_RIGHT_TEXT:
					msg = (ChatMessage)item.getValue();

					holderRightText= new ViewHolderRightText();
					convertView = mInflater.inflate(R.layout.list_item_right_text,
							null);
					holderRightText.ivRightIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderRightText.btnRightText = (Button) convertView
							.findViewById(R.id.btn_right_text);
					spannableString = FaceConversionUtil.getInstace().
							getExpressionString(mCntx, msg.getContent(), 60);

					setAvatarImage(holderRightText.ivRightIcon, ContacterManager.userMe);

					holderRightText.btnRightText.setText(spannableString);
					if (mMessageContentLongClickListener != null) {
						holderRightText.btnRightText.setOnLongClickListener(mMessageContentLongClickListener);
					}
					holderRightText.btnRightText.setTag(msg);
					
					convertView.setTag(holderRightText);
					break;

				case VALUE_RIGHT_IMAGE:
					msg = (ChatMessage)item.getValue();

					holderRightImg = new ViewHolderRightImg();
					convertView = mInflater.inflate(R.layout.list_item_right_image,
							null);
					holderRightImg.ivRightIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderRightImg.ivRightImage = (ImageView) convertView
							.findViewById(R.id.iv_right_image_thumb);
					setAvatarImage(holderRightImg.ivRightIcon, ContacterManager.userMe);
					
					Log.d(TAG, "msg list view pos " + position + ", path " + msg.getAttachment().getSrcUri());
					bm = ImageUtil.getImageThumbnail(msg.getAttachment().getSrcUri(), 200, 200);
					holderRightImg.ivRightImage.setImageBitmap(bm);
					
					holderRightImg.ivRightImage.setTag(msg);

					if (mImageClickListener != null) {
						holderRightImg.ivRightImage.setOnClickListener(mImageClickListener);
					}
					if (mMessageContentLongClickListener != null) {
						holderRightImg.ivRightImage.setOnLongClickListener(mMessageContentLongClickListener);
					}
					convertView.setTag(holderRightImg);
					break;

				case VALUE_RIGHT_AUDIO:
					msg = (ChatMessage)item.getValue();

					holderRightAudio=new ViewHolderRightAudio();
					convertView = mInflater.inflate(R.layout.list_item_right_audio,
							null);
					holderRightAudio.ivRightIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderRightAudio.btnRightAudio = (Button) convertView
							.findViewById(R.id.btn_right_audio);
					setAvatarImage(holderRightAudio.ivRightIcon, ContacterManager.userMe);
					holderRightAudio.btnRightAudio.setText(msg.getAttachment().getAudioLength() + "''");
					
					holderRightAudio.btnRightAudio.setTag(msg);

					if (mRightAudioClickListener != null) {
						holderRightAudio.btnRightAudio.setOnClickListener(mRightAudioClickListener);
					}
					if (mMessageContentLongClickListener != null) {
						holderRightAudio.btnRightAudio.setOnLongClickListener(mMessageContentLongClickListener);
					}
					convertView.setTag(holderRightAudio);
					break;
					
				case VALUE_RIGHT_VIDEO:
					msg = (ChatMessage)item.getValue();
					holderRightVideo = new ViewHolderRightVideo();
					convertView = mInflater.inflate(R.layout.list_item_right_video,
							null);
					holderRightVideo.ivRightIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderRightVideo.btnRightVideoThumb = (ImageView) convertView
							.findViewById(R.id.iv_right_video_thumb);
					setAvatarImage(holderRightVideo.ivRightIcon, ContacterManager.userMe);
					
					bm = ImageUtil.getVideoThumbnail(msg.getAttachment().getSrcUri(), 100, 100,
							Images.Thumbnails.MINI_KIND);
					holderRightVideo.btnRightVideoThumb.setImageBitmap(bm);
					
					holderRightVideo.btnRightVideoThumb.setTag(msg);

					if (mVideoClickListener != null) {
						holderRightVideo.btnRightVideoThumb.setOnClickListener(mVideoClickListener);
					}
					if (mMessageContentLongClickListener != null) {
						holderRightVideo.btnRightVideoThumb.setOnLongClickListener(mMessageContentLongClickListener);
					}
					convertView.setTag(holderRightVideo);
					break;
					
				case VALUE_RIGHT_BURN:
					msg = (ChatMessage)item.getValue();

					holderRightBurn = new ViewHolderRightBurn();
					convertView = mInflater.inflate(R.layout.list_item_right_burn,
							null);
					holderRightBurn.ivRightIcon = (ImageView) convertView
							.findViewById(R.id.iv_icon);
					holderRightBurn.btnRightBurn = (Button) convertView
							.findViewById(R.id.btn_right_burn);
					
					setAvatarImage(holderRightBurn.ivRightIcon, ContacterManager.userMe);					
					holderRightBurn.btnRightBurn.setTag(msg);
					
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
					holderTime.tvTimeTip.setText((String)item.getValue());
					break;				
				case VALUE_LEFT_TEXT:
					msg = (ChatMessage)item.getValue();
					holderLeftText = (ViewHolderLeftText)convertView.getTag();
					spannableString = FaceConversionUtil.getInstace().
							getExpressionString(mCntx, msg.getContent(), 60);
					setAvatarImage(holderLeftText.ivLeftIcon, user);
					holderLeftText.btnLeftText.setText(spannableString);
					holderLeftText.btnLeftText.setTag(msg);

					break;
				case VALUE_LEFT_IMAGE:
					msg = (ChatMessage)item.getValue();
					holderLeftImg = (ViewHolderLeftImg)convertView.getTag();
					setAvatarImage(holderLeftImg.ivLeftIcon, user);
					holderLeftImg.ivLeftImage.setTag(msg);

					bm = BitmapFactory.decodeFile(msg.getAttachment().getThumbUri());
					holderLeftImg.ivLeftImage.setImageBitmap(bm);
					break;
				case VALUE_LEFT_AUDIO:
					msg = (ChatMessage)item.getValue();
					holderLeftAudio = (ViewHolderLeftAudio)convertView.getTag();
					setAvatarImage(holderLeftAudio.ivLeftIcon, user);
					holderLeftAudio.btnLeftAudio.setTag(msg);

					holderLeftAudio.btnLeftAudio.setText(msg.getAttachment().getAudioLength() + "''");
					convertView.setTag(holderLeftAudio);
					break;
				case VALUE_LEFT_VIDEO:
					msg = (ChatMessage)item.getValue();
					holderLeftVideo = (ViewHolderLeftVideo)convertView.getTag();
					setAvatarImage(holderLeftVideo.ivLeftIcon, user);
					holderLeftVideo.btnLeftVideoThumb.setTag(msg);

					bm = BitmapFactory.decodeFile(msg.getAttachment().getThumbUri());
					holderLeftVideo.btnLeftVideoThumb.setImageBitmap(bm);
					convertView.setTag(holderLeftVideo);
					break;
					
				case VALUE_LEFT_BURN:
					msg = (ChatMessage)item.getValue();
					holderLeftBurn = (ViewHolderLeftBurn) convertView.getTag();
					setAvatarImage(holderLeftBurn.ivLeftIcon, user);					
					holderLeftBurn.btnLeftBurn.setTag(msg);
					convertView.setTag(holderLeftBurn);
					break;
					
				case VALUE_RIGHT_TEXT:
					msg = (ChatMessage)item.getValue();
					holderRightText = (ViewHolderRightText)convertView.getTag();
					spannableString = FaceConversionUtil.getInstace().
							getExpressionString(mCntx, msg.getContent(), 60);
					setAvatarImage(holderRightText.ivRightIcon, ContacterManager.userMe);
					holderRightText.btnRightText.setTag(msg);

					holderRightText.btnRightText.setText(spannableString);
					break;
					
				case VALUE_RIGHT_IMAGE:
					msg = (ChatMessage)item.getValue();
					holderRightImg = (ViewHolderRightImg)convertView.getTag();
					setAvatarImage(holderRightImg.ivRightIcon, ContacterManager.userMe);
					holderRightImg.ivRightImage.setTag(msg);

					bm = ImageUtil.getImageThumbnail(msg.getAttachment().getSrcUri(), 200, 200);
					holderRightImg.ivRightImage.setImageBitmap(bm);
					break;
					
				case VALUE_RIGHT_AUDIO:
					msg = (ChatMessage)item.getValue();
					holderRightAudio = (ViewHolderRightAudio)convertView.getTag();
					setAvatarImage(holderRightAudio.ivRightIcon, ContacterManager.userMe);
					holderRightAudio.btnRightAudio.setTag(msg);

					holderRightAudio.btnRightAudio.setText(msg.getAttachment().getAudioLength() + "''");
					break;
					
				case VALUE_RIGHT_VIDEO:
					msg = (ChatMessage)item.getValue();
					holderRightVideo = (ViewHolderRightVideo)convertView.getTag();

					setAvatarImage(holderRightVideo.ivRightIcon, ContacterManager.userMe);
					holderRightVideo.btnRightVideoThumb.setTag(msg);

					bm = ImageUtil.getVideoThumbnail(msg.getAttachment().getSrcUri(), 100, 100,
							Images.Thumbnails.MINI_KIND);
					holderRightVideo.btnRightVideoThumb.setImageBitmap(bm);
					break;
					
				case VALUE_RIGHT_BURN:
					msg = (ChatMessage)item.getValue();
					holderRightBurn = (ViewHolderRightBurn) convertView.getTag();
					setAvatarImage(holderRightBurn.ivRightIcon, ContacterManager.userMe);					
					holderRightBurn.btnRightBurn.setTag(msg);
					convertView.setTag(holderRightBurn);
					break;

			default:
				break;
			}
			//holder = (ViewHolder) convertView.getTag();
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
	
	private int parseMsgType(ChatMessageItem msg) {
		String t = msg.getType();
		
		if (t.equals(ChatMessage.TIMESTAMP)) {
			return VALUE_TIMESTAMP;
		}

		ChatMessage m = (ChatMessage)msg.getValue();

		if (m.getDir().equals(IMMessage.SEND)) {
			
			if (m.getDestroy().equals(IMMessage.BURN_AFTER_READ)) {
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
			if (m.getDestroy().equals(IMMessage.BURN_AFTER_READ)) {
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
	}
	
	class ViewHolderRightBurn {
		private ImageView ivRightIcon;
		private Button btnRightBurn;
	}

	class ViewHolderRightImg {
		private ImageView ivRightIcon;
		private ImageView ivRightImage;
	}

	class ViewHolderRightAudio {
		private ImageView ivRightIcon;
		private Button btnRightAudio;
	}
	
	class ViewHolderRightVideo {
		private ImageView ivRightIcon;
		private ImageView btnRightVideoThumb;
	}	
	
	class ViewHolderRightFile {
		private ImageView ivRightIcon;
		private Button btnRightFileName;
	}

	class ViewHolderLeftText {
		private ImageView ivLeftIcon;
		private Button btnLeftText;
	}
	
	class ViewHolderLeftBurn {
		private ImageView ivLeftIcon;
		private Button btnLeftBurn;
	}
	
	class ViewHolderLeftImg {
		private ImageView ivLeftIcon;
		private ImageView ivLeftImage;
	}

	class ViewHolderLeftAudio {
		private ImageView ivLeftIcon;
		private Button btnLeftAudio;
	}
	
	class ViewHolderLeftVideo {
		private ImageView ivLeftIcon;
		private ImageView btnLeftVideoThumb;
	}	
	
	class ViewHolderLeftFile {
		private ImageView ivLeftIcon;
		private Button btnLeftFileName;
	}

}
