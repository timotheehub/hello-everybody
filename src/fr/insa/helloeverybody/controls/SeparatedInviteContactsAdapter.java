package fr.insa.helloeverybody.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.viewmodels.ContactList;

/* Adaptateur pour les listes de contacts que l'on peut inviter
-----------------------------------------------------------------------------*/
public class SeparatedInviteContactsAdapter extends SeparatedListAdapter {

	private Context context;
	
	// Constructeur public
	public SeparatedInviteContactsAdapter(Context context, Set<String> memberJidSet) {
		super(context);
		this.context = context;
		setAdapter(memberJidSet);
	}
	
	// Initialise l'adaptateur
	private void setAdapter(Set<String> memberJidSet) {
		
		// Récuperer les listes de contacts
        ContactList contactList = ContactList.getInstance();
        List<Profile> filteredFavoriteList = getFilteredProfileList(
        		contactList.getFavoritesList(), memberJidSet);
        List<Profile> filteredKnownList = getFilteredProfileList(
        		contactList.getKnownList(), memberJidSet);
        List<Profile> filteredRecommendedList = getFilteredProfileList(
        		contactList.getRecommendedList(), memberJidSet);
        List<Profile> filteredNearMeList = getFilteredProfileList(
        		contactList.getNearMeList(), memberJidSet);
        
        // Ajouter les conversations aux adaptateurs
        addSection(context.getString(R.string.favorites),
				getContactAdapter(filteredFavoriteList),
				getProfileIds(filteredFavoriteList));
		addSection(context.getString(R.string.known),
				getContactAdapter(filteredKnownList),
				getProfileIds(filteredKnownList));
		addSection(context.getString(R.string.recommended),
				getContactAdapter(filteredRecommendedList), 
				getProfileIds(filteredRecommendedList));
		addSection(context.getString(R.string.near_me),
				getContactAdapter(filteredNearMeList),
				getProfileIds(filteredNearMeList));
	}
	
	// Retourne un adaptateur basée sur une liste
	private ContactAdapter getContactAdapter(List<Profile> profileList) {
		return new ContactAdapter(context, R.layout.contact_item, profileList);
	}
	
	// Filtre la liste de contacts en retirer les membres de la conversation
	private List<Profile> getFilteredProfileList(List<Profile> profileList,
				Set<String> memberJidSet) {
		List<Profile> filteredList = new ArrayList<Profile>();
		for (Profile profile : profileList) {
			if (!memberJidSet.contains(profile.getJid())) {
				filteredList.add(profile);
			}
		}
		
		return filteredList;
	}
	
	// Retourne la liste des identifiants
	private List<String> getProfileIds(List<Profile> profilesList) {
		List<String> profileIds = new ArrayList<String>();
		
		for (Profile profile : profilesList) {
			profileIds.add(profile.getJid());
		}
		
		return profileIds;
	}
}
