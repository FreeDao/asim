package com.view.asim.view;

import java.util.ArrayList;
import java.util.List;

import com.view.asim.model.ChatEmoji;
import com.view.asim.util.FaceConversionUtil;

import com.view.asim.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class FaceRelativeLayout extends RelativeLayout implements
		OnItemClickListener, OnClickListener {

	private Context context;

	private OnCorpusSelectedListener mListener;

	private ViewPager vp_face;

	private ArrayList<View> pageViews;

	private LinearLayout layout_point;

	private ArrayList<ImageView> pointViews;

	private List<List<ChatEmoji>> emojis;

	private View faceView, VoiceView, AddView;
	
	private ImageView fireBtnImg, sendBtnImg, addBtnImg;

	private EditText et_sendmessage;

	private List<FaceAdapter> faceAdapters;

	private int current = 0;

	public FaceRelativeLayout(Context context) {
		super(context);
		this.context = context;
	}

	public FaceRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public FaceRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public void setOnCorpusSelectedListener(OnCorpusSelectedListener listener) {
		mListener = listener;
	}

	public interface OnCorpusSelectedListener {

		void onCorpusSelected(ChatEmoji emoji);

		void onCorpusDeleted();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		emojis = FaceConversionUtil.getInstace().emojiLists;
		onCreate();
	}

	private void onCreate() {
		initView();
		initViewPager();
		initPoint();
		initData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.chat_emoji_btn:
			hideVoiceView();
			hideAddMoreView();
			if (faceView.getVisibility() == View.VISIBLE) {
				faceView.setVisibility(View.GONE);
			} else {
				faceView.setVisibility(View.VISIBLE);
			}
			break;
			
		case R.id.chat_add_more_btn:
			hideFaceView();
			hideVoiceView();
			if (AddView.getVisibility() == View.VISIBLE) {
				AddView.setVisibility(View.GONE);
			} else {
				AddView.setVisibility(View.VISIBLE);
			}
			break;

		
		case R.id.chat_content:
			hideFaceView();
			hideAddMoreView();
			hideVoiceView();
			
		}
	}

	public boolean hideFaceView() {
		if (faceView.getVisibility() == View.VISIBLE) {
			faceView.setVisibility(View.GONE);
			return true;
		}
		return false;
	}
	
	public boolean hideVoiceView() {
		if (VoiceView.getVisibility() == View.VISIBLE) {
			VoiceView.setVisibility(View.GONE);
			return true;
		}
		return false;
	}
	
	public boolean hideAddMoreView() {
		if (AddView.getVisibility() == View.VISIBLE) {
			AddView.setVisibility(View.GONE);
			return true;
		}
		return false;
	}

	private void initView() {
		vp_face = (ViewPager) findViewById(R.id.vp_contains);
		et_sendmessage = (EditText) findViewById(R.id.chat_content);
		layout_point = (LinearLayout) findViewById(R.id.iv_image);
		et_sendmessage.setOnClickListener(this);
		et_sendmessage.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() == 0) {
					sendBtnImg.setVisibility(View.GONE);
					addBtnImg.setVisibility(View.VISIBLE);
				} else {
					sendBtnImg.setVisibility(View.VISIBLE);
					addBtnImg.setVisibility(View.GONE);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {				
			}
			
		});
		
		findViewById(R.id.chat_emoji_btn).setOnClickListener(this);
		//findViewById(R.id.chat_voice_btn).setOnClickListener(this);
		
		sendBtnImg = (ImageView) findViewById(R.id.chat_normal_sendbtn);
		addBtnImg = (ImageView) findViewById(R.id.chat_add_more_btn);
		
		addBtnImg.setOnClickListener(this);
		
		faceView = findViewById(R.id.ll_facechoose);
		AddView = findViewById(R.id.ll_add_operation);
		VoiceView = findViewById(R.id.ll_audio_record);

	}

	private void initViewPager() {
		pageViews = new ArrayList<View>();
		View nullView1 = new View(context);
		nullView1.setBackgroundColor(Color.TRANSPARENT);
		pageViews.add(nullView1);

		faceAdapters = new ArrayList<FaceAdapter>();
		for (int i = 0; i < emojis.size(); i++) {
			GridView view = new GridView(context);
			FaceAdapter adapter = new FaceAdapter(context, emojis.get(i), vp_face.getWidth());
			view.setAdapter(adapter);
			faceAdapters.add(adapter);
			view.setOnItemClickListener(this);
			view.setNumColumns(6);
			view.setBackgroundColor(Color.TRANSPARENT);
			view.setColumnWidth(vp_face.getWidth() / 6);
			view.setHorizontalSpacing(1);
			view.setVerticalSpacing(1);
			view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			view.setCacheColorHint(0);
			view.setSelector(new ColorDrawable(Color.TRANSPARENT));
			view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			view.setGravity(Gravity.CENTER);
			pageViews.add(view);
		}

		View nullView2 = new View(context);
		nullView2.setBackgroundColor(Color.TRANSPARENT);
		pageViews.add(nullView2);
	}

	private void initPoint() {

		pointViews = new ArrayList<ImageView>();
		ImageView imageView;
		for (int i = 0; i < pageViews.size(); i++) {
			imageView = new ImageView(context);
			imageView.setBackgroundResource(R.drawable.moon_page_unselected);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					new ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
							android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
			layout_point.addView(imageView, layoutParams);
			if (i == 0 || i == pageViews.size() - 1) {
				imageView.setVisibility(View.GONE);
			}
			if (i == 1) {
				imageView.setBackgroundResource(R.drawable.moon_page_selected);
			}
			pointViews.add(imageView);

		}
	}

	private void initData() {
		vp_face.setAdapter(new ViewPagerAdapter(pageViews));

		vp_face.setCurrentItem(1);
		current = 0;
		vp_face.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				current = arg0 - 1;
				drawPoint(arg0);
				if (arg0 == pointViews.size() - 1 || arg0 == 0) {
					if (arg0 == 0) {
						vp_face.setCurrentItem(arg0 + 1);
						pointViews.get(1).setBackgroundResource(R.drawable.moon_page_selected);
					} else {
						vp_face.setCurrentItem(arg0 - 1);
						pointViews.get(arg0 - 1).setBackgroundResource(
								R.drawable.moon_page_unselected);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

	}

	public void drawPoint(int index) {
		for (int i = 1; i < pointViews.size(); i++) {
			if (index == i) {
				pointViews.get(i).setBackgroundResource(R.drawable.moon_page_selected);
			} else {
				pointViews.get(i).setBackgroundResource(R.drawable.moon_page_unselected);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		ChatEmoji emoji = (ChatEmoji) faceAdapters.get(current).getItem(arg2);
		if (emoji.getId() == R.drawable.face_del_icon) {
			int selection = et_sendmessage.getSelectionStart();
			String text = et_sendmessage.getText().toString();
			if (selection > 0) {
				String text2 = text.substring(selection - 1);
				if ("]".equals(text2)) {
					int start = text.lastIndexOf("[");
					int end = selection;
					et_sendmessage.getText().delete(start, end);
					return;
				}
				et_sendmessage.getText().delete(selection - 1, selection);
			}
		}
		if (!TextUtils.isEmpty(emoji.getCharacter())) {
			if (mListener != null)
				mListener.onCorpusSelected(emoji);
			SpannableString spannableString = FaceConversionUtil.getInstace()
					.addFace(getContext(), emoji.getId(), emoji.getCharacter());
			et_sendmessage.append(spannableString);
		}

	}
}
