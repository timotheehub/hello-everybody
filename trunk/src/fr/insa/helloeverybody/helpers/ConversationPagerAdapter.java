package fr.insa.helloeverybody.helpers;

import java.util.ArrayList;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ListView;

public class ConversationPagerAdapter extends PagerAdapter {
	
	private ArrayList<ListView> items;
	
	public ConversationPagerAdapter(Context context, ArrayList<ListView> items) {
		super();
		this.items = items;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view==object;
	}
	
	@Override
    public Object instantiateItem(View collection, int position) {
		View myView = items.get(position);
		((ViewPager) collection).addView(myView);
        return myView;
    }

    @Override
    public void destroyItem(View collection, int position, Object view) {
    	((ViewPager) collection).removeView((View) view);
    }
	

	@Override
	public void finishUpdate(View arg0) {}
	

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {}

}
