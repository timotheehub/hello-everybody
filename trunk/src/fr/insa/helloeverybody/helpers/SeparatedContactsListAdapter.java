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

	private List<Profile> filteredFavoritesList;
	private List<Profile> filteredKnownList;
	private List<Profile> filteredRecommendedList;
	private List<Profile> filteredNearMeList;
	
	private Context context;
	
	public List<CharSequence> filter;
	
	public SeparatedContactsListAdapter(Context context) {
		super(context);
		this.context = context;
		filteredFavoritesList = new ArrayList<Profile>();
		filteredKnownList = new ArrayList<Profile>();
		filteredRecommendedList = new ArrayList<Profile>();
		filteredNearMeList = new ArrayList<Profile>();
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
				filteredFavoritesList));
		ids.add(HEADER_ID);
		ids.addAll(updateAttributesList(contactsList.getKnownList(),
				filteredKnownList));
		ids.add(HEADER_ID);
		ids.addAll(updateAttributesList(contactsList.getRecommendedList(),
				filteredRecommendedList));
		ids.add(HEADER_ID);
		ids.addAll(updateAttributesList(contactsList.getNearMeList(),
				filteredNearMeList));
		
		notifyDataSetChanged();
	}
	
	private void setAdapter() {
        ContactsList contactsList = ContactsList.getInstance();
        
        // Ajoute les contacts aux adaptateurs
		addSection(context.getString(R.string.favorites),
				getContactAdapter(filteredFavoritesList),
				getProfileIds(contactsList.getFavoritesList()));
		addSection(context.getString(R.string.known),
				getContactAdapter(filteredKnownList),
				getProfileIds(contactsList.getKnownList()));
		addSection(context.getString(R.string.recommended),
				getContactAdapter(filteredRecommendedList), 
				getProfileIds(contactsList.getRecommendedList()));
		addSection(context.getString(R.string.near_me),
				getContactAdapter(filteredNearMeList),
				getProfileIds(contactsList.getNearMeList()));
	}
	
	// Retourne un adaptateur basée sur une liste
	private ContactAdapter getContactAdapter(List<Profile> profilesList) {
		return new ContactAdapter(context,  R.layout.contact_item, profilesList);
	}
	
	// Met a jour une liste de profile en fonction du filtre
	private List<Long> updateAttributesList(List<Profile> profilesList,
				List<Profile> filteredProfilesList) {
		
		Map<String, String> attributesMap;
		filteredProfilesList.clear();
		
		// Pas de filtre
		if (filter == null || filter.size() == 0) {
			filteredProfilesList.addAll(profilesList);
		}
		// Filtre
		else {
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
						filteredProfilesList.add(profile);
					}
				}
			}
		}
		
		return getProfileIds(filteredProfilesList);
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
