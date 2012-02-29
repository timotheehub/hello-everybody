package fr.insa.helloeverybody.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.Profile;

import android.content.Context;
import android.widget.SimpleAdapter;

public class SeparatedContactsListAdapter extends SeparatedListAdapter {

	private List<Map<String, String>> favoritesAttributesList;
	private List<Map<String, String>> knownAttributesList;
	private List<Map<String, String>> recommendedAttributesList;
	private List<Map<String, String>> nearMeAttributesList;
	
	private Context context;
	
	public CharSequence filter;
	
	public SeparatedContactsListAdapter(Context context) {
		super(context);
		this.context = context;
		favoritesAttributesList = new ArrayList<Map<String, String>>();
		knownAttributesList = new ArrayList<Map<String, String>>();
		recommendedAttributesList = new ArrayList<Map<String, String>>();
		nearMeAttributesList = new ArrayList<Map<String, String>>();
		doFilter("");
		setAdapter();
	}
	
	public void doFilter(CharSequence s) {
        ContactsList contactsList = ContactsList.getInstance();
		filter = s.toString().toLowerCase();
        
		updateAttributesMap(contactsList.getFavoritesList(),
				favoritesAttributesList);
		updateAttributesMap(contactsList.getKnownList(),
				knownAttributesList);
		updateAttributesMap(contactsList.getRecommendedList(),
				recommendedAttributesList);
		updateAttributesMap(contactsList.getNearMeList(),
				nearMeAttributesList);
		
		notifyDataSetChanged();
	}
	
	private void setAdapter() {
        ContactsList contactsList = ContactsList.getInstance();
        
        // Ajoute les contacts aux adaptateurs
		addSection(context.getString(R.string.favorites),
				getSimpleAdapter(favoritesAttributesList),
				getProfileIds(contactsList.getFavoritesList()));
		addSection(context.getString(R.string.known),
				getSimpleAdapter(knownAttributesList),
				getProfileIds(contactsList.getKnownList()));
		addSection(context.getString(R.string.recommended),
				getSimpleAdapter(recommendedAttributesList), 
				getProfileIds(contactsList.getRecommendedList()));
		addSection(context.getString(R.string.near_me),
				getSimpleAdapter(nearMeAttributesList),
				getProfileIds(contactsList.getNearMeList()));
	}
	
	// Retourne un adaptateur basée sur une liste
	private SimpleAdapter getSimpleAdapter(List<Map<String, String>> attributesList) {
		return new SimpleAdapter (context, attributesList, R.layout.contact_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
	}
	
	// Met a jour une liste de profile en fonction du filtre
	private void updateAttributesMap(List<Profile> profilesList,
				List<Map<String, String>> attributesList) {
		
		Map<String, String> attributesMap;
		attributesList.clear();
		List<Profile> filteredList;
		
		// Pas de filtre
		if (filter == null || filter.length() == 0) {
			filteredList = profilesList;
		}
		// Filtre
		else {
			filteredList = new ArrayList<Profile>();
			for (Profile profile : profilesList) {
				if ((profile != null)
					&& (((profile.getFirstName() != null)
							&& (profile.getFirstName().toLowerCase().contains(filter)))
					|| ((profile.getLastName() != null)
							&& (profile.getLastName().toLowerCase().contains(filter))))) {
					filteredList.add(profile);
				}
			}
		}
		
		for (Profile profile : filteredList) {
			attributesMap = new HashMap<String, String>();
			attributesMap.put("firstName", profile.getFirstName());
			attributesMap.put("lastName", profile.getLastName());
			attributesMap.put("picture", String.valueOf(profile.getAvatar()));
			attributesList.add(attributesMap);
		}
	}
	
	// Retourne la liste des identifiants
	private List<Long> getProfileIds(List<Profile> profilesList) {
		List<Long> profileIds = new ArrayList<Long>();
		
		for (Profile profile : profilesList) {
			profileIds.add(profile.getId());
		}
		
		return profileIds;
	}
}
