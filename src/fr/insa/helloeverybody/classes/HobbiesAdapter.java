package fr.insa.helloeverybody.classes;

import java.util.List;

import fr.insa.helloeverybody.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class HobbiesAdapter extends BaseAdapter {

	List<String> hobbies;
	LayoutInflater inflater;

	public HobbiesAdapter(Context context,List<String> hobbies) {
		inflater = LayoutInflater.from(context);
		this.hobbies = hobbies;

	}
	
	public int getCount() {
		return hobbies.size();
	}

	public Object getItem(int location) {
		return hobbies.get(location);
	}

	public long getItemId(int location) {
		return location;
	}

	private class ViewHolder {
		TextView hobby;
		}

	public View getView(int location, View convertView, ViewGroup parent) {

	ViewHolder holder;
	if(convertView == null) {
		holder = new ViewHolder();
		convertView = inflater.inflate(R.layout.hobby_edit_item, null);
		holder.hobby = (TextView)convertView.findViewById(R.id.hobby);
		convertView.setTag(holder);
	} else {
		holder = (ViewHolder) convertView.getTag();
	}
	holder.hobby.setText(hobbies.get(location));
	return convertView;

	}

}


