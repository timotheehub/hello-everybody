package fr.insa.helloeverybody.controls;

import java.util.List;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.View;
import android.widget.ListView;

public class ConversationPagerAdapter extends PagerAdapter {
	
	private List<Pair<String, ListView>> itemList;
	
	public ConversationPagerAdapter(Context context, List<Pair<String, ListView>> itemList) {
		super();
		this.itemList = itemList;
	}

	@Override
	public int getCount() {
		return itemList.size();
	}

	public int getItemPosition(Object object) {
	    return POSITION_NONE;
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
	
	@Override
    public Object instantiateItem(View collection, int position) {
		View myView = itemList.get(position).second;
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
