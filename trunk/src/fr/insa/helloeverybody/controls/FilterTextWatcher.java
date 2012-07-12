package fr.insa.helloeverybody.controls;

import android.text.Editable;
import android.text.TextWatcher;

public class FilterTextWatcher implements TextWatcher {
	
	private SeparatedContactsListAdapter adapter;
	
	public FilterTextWatcher() { }
	
	public SeparatedContactsListAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(SeparatedContactsListAdapter adapter) {
		this.adapter = adapter;
	}

	public void afterTextChanged(Editable s) {
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (adapter != null) {
			adapter.doFilter(s);
		}
	}

}
