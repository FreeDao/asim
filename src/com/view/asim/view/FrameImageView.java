package com.view.asim.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

/**
 * 
 * @author xuweinan
 *
 */
public class FrameImageView extends GifImageView {

	private ArrayList<Bitmap> mBitmaps = new ArrayList<Bitmap>();

	public FrameImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setImagePath(ArrayList<String> paths, int duration) {
		if (paths != null) {
			try {
				release();
				if (paths.size() == 1) {
					Bitmap bitmap = BitmapFactory.decodeFile(paths.get(0));
					mBitmaps.add(bitmap);
					setImageBitmap(bitmap);
				} else {
					AnimationDrawable mAnimationDrawable = new AnimationDrawable();

					for (String path : paths) {
						Bitmap bitmap = BitmapFactory.decodeFile(path);
						mAnimationDrawable.addFrame(new BitmapDrawable(getResources(), bitmap), duration);
						mBitmaps.add(bitmap);
					}
					mAnimationDrawable.setOneShot(false);
					setImageDrawable(mAnimationDrawable);
					mAnimationDrawable.start();
				}
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setEmptyImage() {
		release();
		setImageDrawable(null);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		release();
	}

	public void release() {
		for (int i = mBitmaps.size() - 1; i >= 0; i--) {
			Bitmap bitmap = mBitmaps.get(i);
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
		}
		mBitmaps.clear();
	}
}
