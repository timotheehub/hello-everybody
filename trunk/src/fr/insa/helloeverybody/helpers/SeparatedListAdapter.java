package fr.insa.helloeverybody.helpers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.insa.helloeverybody.R;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

public class SeparatedListAdapter extends BaseAdapter
{
	public Map<String, Adapter> sections;
	public ArrayAdapter<String> headers;
	public List<Long> ids;
	public final static int TYPE_SECTION_HEADER = 0;
	protected final static Long HEADER_ID = -1L;

	public SeparatedListAdapter(Context context)
	{
		sections = new LinkedHashMap<String, Adapter>();
		ids = new ArrayList<Long>();
		headers = new ArrayAdapter<String>(context, R.layout.list_header);
	}

	public void addSection(String section, Adapter adapter, List<Long> idsList)
	{
		this.headers.add(section);
		ids.add(HEADER_ID);
		this.sections.put(section, adapter);
		ids.addAll(idsList);
	}

	public Object getItem(int position)
	{
		for (String section : sections.keySet())
		{
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) return section;
			if (position < size) return adapter.getItem(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	public int getCount()
	{
		// total together all sections, plus one for each section header
		int total = 0;
		for (Adapter adapter : sections.values())
			total += adapter.getCount() + 1;
		return total;
	}

	@Override
	public int getViewTypeCount()
	{
		// assume that headers count as one, then total all sections
		int total = 1;
		for (Adapter adapter : sections.values()) 
			total += adapter.getViewTypeCount();
		return total;
	}

	@Override
	public int getItemViewType(int position)
	{
		int type = 1;
		for (Adapter adapter : sections.values())
		{
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) return TYPE_SECTION_HEADER;
			if (position < size) return type + adapter.getItemViewType(position - 1);

			// otherwise jump into next section
			position -= size;
			type += adapter.getViewTypeCount();
		}
		return -1;
	}

	public boolean areAllItemsSelectable()
	{
		return false;
	}

	@Override
	public boolean isEnabled(int position)
	{
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		int sectionnum = 0;
		for (Adapter adapter : sections.values())
		{
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) return headers.getView(sectionnum, convertView, parent);
			if (position < size) return adapter.getView(position - 1, convertView, parent);

			// otherwise jump into next section
			position -= size;
			sectionnum++;
		}
		return null;
	}

	public long getItemId(int position)
	{
		if ((position >= 0) && (position < ids.size())) {
			return ids.get(position);
		}
		return position;
	}
}