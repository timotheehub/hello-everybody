package fr.insa.helloeverybody.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
	
	public List<CharSequence> filter;
	
	public SeparatedContactsListAdapter(Context context) {
		super(context);
		this.context = context;
		favoritesAttributesList = new ArrayList<Map<String, String>>();
		knownAttributesList = new ArrayList<Map<String, String>>();
		recommendedAttributesList = new ArrayList<Map<String, String>>();
		nearMeAttributesList = new ArrayList<Map<String, String>>();
		filter = new ArrayList<CharSequence>();
		doFilter("");
		setAdapter();
	}
	
	public void doFilter(CharSequence s) {
        ContactsList contactsList = ContactsList.getInstance();
        filter.clear();
        
        // Creer la liste de filtre
        StringTokenizer st = new StringTokenizer(
        		AsciiUtils.convertNonAscii(s.toString().toLowerCase()));
        while (st.hasMoreTokens()) {
        	filter.add(st.nextToken());
        }
        
        // Met a jour les list
        ids.clear();
        ids.add(HEADER_ID);
		ids.addAll(updateAttributesList(contactsList.getFavoritesList(),
				favoritesAttributesList));
		ids.add(HEADER_ID);
		ids.addAll(updateAttributesList(contactsList.getKnownList(),
				knownAttributesList));
		ids.add(HEADER_ID);
		ids.addAll(updateAttributesList(contactsList.getRecommendedList(),
				recommendedAttributesList));
		ids.add(HEADER_ID);
		ids.addAll(updateAttributesList(contactsList.getNearMeList(),
				nearMeAttributesList));
		
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
	private List<Long> updateAttributesList(List<Profile> profilesList,
				List<Map<String, String>> attributesList) {
		
		Map<String, String> attributesMap;
		attributesList.clear();
		List<Profile> filteredList;
		
		// Pas de filtre
		if (filter == null || filter.size() == 0) {
			filteredList = profilesList;
		}
		// Filtre
		else {
			filteredList = new ArrayList<Profile>();
			for (Profile profile : profilesList) {
				if (profile != null) {
					// Recupere le nom
					String firstName = null;
					String lastName = null;
					if (profile.getFirstName() != null) {
						firstName = AsciiUtils.convertNonAscii(
								profile.getFirstName().toLowerCase());
					}
					if (profile.getLastName() != null) {
						lastName = AsciiUtils.convertNonAscii(
								profile.getLastName().toLowerCase());
					}
					
					// Applique le filtre	
					boolean doesRespectFilter = (firstName != null) || (lastName != null);
					Iterator<CharSequence> it = filter.iterator();
					while (it.hasNext() && doesRespectFilter) {
						CharSequence filterSequence = it.next();
						if (((firstName == null) 
								|| (firstName.contains(filterSequence) == false))
							&& ((lastName == null)
									|| (lastName.contains(filterSequence) == false))) {
							doesRespectFilter = false;
						}
					}
					
					// Ajoute le profil s'il respecte le filtre
					if (doesRespectFilter) {
						filteredList.add(profile);
					}
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
		
		return getProfileIds(filteredList);
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
