package com.view.asim.view;

import java.util.List;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class ViewPagerAdapter extends PagerAdapter {

    private List<View> pageViews;

    public ViewPagerAdapter(List<View> pageViews) {
        super();
        this.pageViews=pageViews;
    }

    // ??�示??��??
    @Override
    public int getCount() {
        return pageViews.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public void destroyItem(View arg0, int arg1, Object arg2) {
        ((ViewPager)arg0).removeView(pageViews.get(arg1));
    }

    /***
     * ??��??�?�?�?item�??类�??listview�????getview
     */
    @Override
    public Object instantiateItem(View arg0, int arg1) {
        ((ViewPager)arg0).addView(pageViews.get(arg1));
        return pageViews.get(arg1);
    }

	@Override
	public void finishUpdate(View arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Parcelable saveState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
		// TODO Auto-generated method stub
		
	}

}