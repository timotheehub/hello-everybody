package fr.insa.helloeverybody.helpers;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ListView;

public class ConversationPagerAdapter extends PagerAdapter {
	
	private LinkedHashMap<String,ListView> items;
	
	public ConversationPagerAdapter(Context context, LinkedHashMap<String,ListView> items) {
		super();
		this.items = items;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	public int getItemPosition(Object object) {
	    return POSITION_NONE;
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view==object;
	}
	
	@Override
    public Object instantiateItem(View collection, int position) {
		ArrayList<ListView> list = new ArrayList<ListView>(items.values());
		View myView = list.get(position);
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
	
	public int findPage(String roomName) {
		ArrayList<String> list = new ArrayList<String>(items.keySet());
		for (int i = 0 ; i < list.size() ; i++ ) {
			if (list.get(i).equals(roomName)) {
				return i;
			}
		}
		return -1;
	}
	
	public String findRoomName(int page) {
		ArrayList<String> list = new ArrayList<String>(items.keySet());
		return list.isEmpty()?null:list.get(page);
	}
}
