package fr.insa.helloeverybody.controls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.helpers.AsciiUtils;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.viewmodels.ContactList;

import android.content.Context;

/* Adaptateur pour les listes de contacts capable de les filtrer
-----------------------------------------------------------------------------*/
public class SeparatedContactsListAdapter extends SeparatedListAdapter {

	private List<Profile> filteredFavoritesList;
	private List<Profile> filteredKnownList;
	private List<Profile> filteredRecommendedList;
	private List<Profile> filteredNearMeList;
	
	private Context context;
	
	public List<CharSequence> filter;
	
	// Constructeur public
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
	
	// Filtre les listes de contacts
	public void doFilter(CharSequence s) {
        filter.clear();
        
        // Creer la liste de filtre
        StringTokenizer st = new StringTokenizer(
        		AsciiUtils.convertNonAscii(s.toString().toLowerCase()));
        while (st.hasMoreTokens()) {
        	filter.add(st.nextToken());
        }
        
        // Mettre à jour le filtre
        update();
	}
	
	// Met à jour le filtre
	public void update() {

        // Mettre à jour les listes d'identifiants
        ContactList contactsList = ContactList.getInstance();
        idList.clear();
        idList.add(HEADER_ID);
		idList.addAll(getFilteredList(contactsList.getFavoritesList(),
				filteredFavoritesList));
		idList.add(HEADER_ID);
		idList.addAll(getFilteredList(contactsList.getKnownList(),
				filteredKnownList));
		idList.add(HEADER_ID);
		idList.addAll(getFilteredList(contactsList.getRecommendedList(),
				filteredRecommendedList));
		idList.add(HEADER_ID);
		idList.addAll(getFilteredList(contactsList.getNearMeList(),
				filteredNearMeList));
		
		// Notifier les modifications
		notifyDataSetChanged();
	}
	
	// Initialise l'adpatateur
	private void setAdapter() {
		addSection(context.getString(R.string.favorites),
				getContactAdapter(filteredFavoritesList),
				getProfileIds(filteredFavoritesList));
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
	private ContactAdapter getContactAdapter(List<Profile> profilesList) {
		return new ContactAdapter(context,  R.layout.contact_item, profilesList);
	}
	
	// Met a jour une liste de profile en fonction du filtre
	private List<String> getFilteredList(List<Profile> profilesList,
				List<Profile> filteredProfilesList) {
		
		filteredProfilesList.clear();
		
		// Vérifier qu'il y a un filtre
		if (filter == null || filter.size() == 0) {
			filteredProfilesList.addAll(profilesList);
			return getProfileIds(filteredProfilesList);
		}
		
		// Filtrer les résultats 
		for (Profile profile : profilesList) {
			if (profile != null) {
				
				// Récupérer le nom
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
				
				// Appliquer le filtre	
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
		
		return getProfileIds(filteredProfilesList);
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
