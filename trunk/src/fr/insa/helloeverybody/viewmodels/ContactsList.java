package fr.insa.helloeverybody.viewmodels;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;

import fr.insa.helloeverybody.interfaces.ContactListener;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.ProfileType;

/* Classe qui contient la liste des contacts filtrés et non filtrés
 * TODO(performance): Utiliser TreeSet
-----------------------------------------------------------------------------*/
public class ContactsList implements OnSharedPreferenceChangeListener {

	public static final String KEY_DISTANCE_PREFERENCE = "distance_preference";
	public static final String KEY_FILTER_AGE = "filter_age";
	public static final String KEY_AGE_FROM = "age_from";
	public static final String KEY_AGE_TO = "age_to";
	
	// Singleton
	private static ContactsList instance = null;
	
	// Variables
	private List<ContactListener> listenerList;
	private List<Profile> allFavoritesList;
	private List<Profile> allKnownList;
	private List<Profile> allRecommendedList;
	private List<Profile> allNearMeList;
	private List<Profile> filteredFavoritesList;
	private List<Profile> filteredKnownList;
	private List<Profile> filteredRecommendedList;
	private List<Profile> filteredNearMeList;
	private SharedPreferences sharedPreferences;
	private int maximalDistance;
	private boolean isAgeFiltered;
	private int minAge;
	private int maxAge;
	
	// Constructeur privé
	private ContactsList() {
		listenerList = Collections.synchronizedList(new LinkedList<ContactListener>());
		allFavoritesList = new LinkedList<Profile>();
		allKnownList = new LinkedList<Profile>();
		allRecommendedList = new LinkedList<Profile>();
		allNearMeList = new LinkedList<Profile>();
		filteredFavoritesList = new LinkedList<Profile>();
		filteredKnownList = new LinkedList<Profile>();
		filteredRecommendedList = new LinkedList<Profile>();
		filteredNearMeList = new LinkedList<Profile>(); 
	}
	
	// Retourne le singleton de maniere protegee
	public static synchronized ContactsList getInstance() {
		if (instance == null) {
			instance = new ContactsList();
		}
		return instance;
	}
	
	// Initialisation
	public void initContactsList(Context context) {
		sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(context);
		
		updateMaximalDistance();
		updateIsAgeFiltered();
		updateMinAge();
		updateMaxAge();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	// Destruction
	public void destroyContactsList() {
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	
	/* Gestion des contacts et des filtres
	-------------------------------------------------------------------------*/
	// Retourne la liste de favoris
	public List<Profile> getFavoritesList() {
		return new ArrayList<Profile>(filteredFavoritesList);
	}
	
	// Retourne la liste de recents
	public List<Profile> getKnownList() {
		return new ArrayList<Profile>(filteredKnownList);
	}
	
	// Retourne la liste de recommandes
	public List<Profile> getRecommendedList() {
		return new ArrayList<Profile>(filteredRecommendedList);
	}
	
	// Retourne la liste de personnes a proximite
	public List<Profile> getNearMeList() {
		return new ArrayList<Profile>(filteredNearMeList);
	}
	
	// Retourne la liste des profiles
	public List<Profile> getProfilesList() {
		List<Profile> profilesList = getFavoritesList();
		profilesList.addAll(getKnownList());
		profilesList.addAll(getRecommendedList());
		profilesList.addAll(getNearMeList());
		
		return profilesList;
	}
	
	// Retourne un profil en fonction de son identifiant
	public Profile getProfileByJid(String jid) {
		for (Profile profile : allFavoritesList) {
			if (profile.getJid().equals(jid)) {
				return profile;
			}
		}
		
		for (Profile profile : allKnownList) {
			if (profile.getJid().equals(jid)) {
				return profile;
			}
		}
		
		for (Profile profile : allRecommendedList) {
			if (profile.getJid().equals(jid)) {
				return profile;
			}
		}
		
		for (Profile profile : allNearMeList) {
			if (profile.getJid().equals(jid)) {
				return profile;
			}
		}
		
		return null;
	}
	
	// Change un contact d'une liste à une autre
	public void update(Profile profile, ProfileType previousProfileType) {
		switch (previousProfileType) {
			case FAVORITE:
				filteredFavoritesList.remove(profile);
				allFavoritesList.remove(profile);
				break;
			case KNOWN:
				filteredKnownList.remove(profile);
				allKnownList.remove(profile);
				break;
			case RECOMMENDED:
				filteredRecommendedList.remove(profile);
				allRecommendedList.remove(profile);
				break;
			case NEAR_ME:
				filteredNearMeList.remove(profile);
				allNearMeList.remove(profile);
				break;
		}
		addProfileInternal(profile);
		fireContactListUpdated();
	}
	
	// Ajouter une liste de profile
	public void addProfileList(List<Profile> profileList) {
		if (profileList == null) {
			return;
		}
		
		for (Profile profile : profileList) {
			addProfileInternal(profile);
		}
		fireContactListUpdated();
	}
	
	// Ajoute un profil
	public void addProfile(Profile profile) {
		addProfileInternal(profile);
		fireContactListUpdated();
	}
	
	// Ajoute un profil
	private void addProfileInternal(Profile profile) {
		switch (profile.getProfileType()) {
			case FAVORITE:
				addProfileInOrder(allFavoritesList, profile);
				if (doesRespectFilter(profile)) {
					addProfileInOrder(filteredFavoritesList, profile);
				}
				break;
			case KNOWN:
				addProfileInOrder(allKnownList, profile);
				if (doesRespectFilter(profile)) {
					addProfileInOrder(filteredKnownList, profile);
				}
				break;
			case RECOMMENDED:;
				addProfileInOrder(allRecommendedList, profile);
				if (doesRespectFilter(profile)) {
					addProfileInOrder(filteredRecommendedList, profile);
				}
				break;
			case NEAR_ME:
				addProfileInOrder(allNearMeList, profile);
				if (doesRespectFilter(profile)) {
					addProfileInOrder(filteredNearMeList, profile);
				}
				break;
		}
	}
	
	// Ajouter un profil selon l'ordre alphabétique
	private void addProfileInOrder(List<Profile> profileList, Profile profile) {
		// Trouver la position d'insertion
		int insertPosition = 0;
		Iterator<Profile> it = profileList.iterator();
		while (it.hasNext()) {
			Profile nextProfile = it.next();
			if (profile.compareTo(nextProfile) > 0) {
				insertPosition++;
			}
		}
		
		// Inserer l'element
		profileList.add(insertPosition, profile);
	}

	// Supprime un profil selon son Jid
	public boolean removeProfileByJid(String jid) {		
		for (Profile profile : allFavoritesList) {
			if (profile.getJid().equals(jid)) {
				filteredFavoritesList.remove(profile);
				if (allFavoritesList.remove(profile)) {
					fireContactListUpdated();
					return true;
				}
			}
		}
		
		for (Profile profile : allKnownList) {
			if (profile.getJid().equals(jid)) {
				filteredKnownList.remove(profile);
				if (allKnownList.remove(profile)) {
					fireContactListUpdated();
					return true;
				}
			}
		}
		
		for (Profile profile : allRecommendedList) {
			if (profile.getJid().equals(jid)) {
				filteredRecommendedList.remove(profile);
				if (allRecommendedList.remove(profile)) {
					fireContactListUpdated();
					return true;
				}
			}
		}
		
		for (Profile profile : allNearMeList) {
			if (profile.getJid().equals(jid)) {
				filteredNearMeList.remove(profile);
				return allNearMeList.remove(profile);
			}
		}
		
		return false;
	}
	
	// Efface toutes les listes
	public void clearAllLists() {
		allFavoritesList.clear();
		allKnownList.clear();
		allRecommendedList.clear();
		allNearMeList.clear();
		filteredFavoritesList.clear();
		filteredKnownList.clear();
		filteredRecommendedList.clear();
		filteredNearMeList.clear();
		fireContactListUpdated();
	}

	// Modifie une preference
	public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
		if (key.equals(KEY_DISTANCE_PREFERENCE)) {
			updateMaximalDistance();
		}
		else if (key.equals(KEY_FILTER_AGE)) {
			updateIsAgeFiltered();
		}
		else if (key.equals(KEY_AGE_FROM)) {
			updateMinAge();
		}
		else if (key.equals(KEY_AGE_TO)) {
			updateMaxAge();
		}
		filterLists();
		fireContactListUpdated();
	}
	
	// Construit les listes filtrées
	private void filterLists() {
		filterList(allFavoritesList, filteredFavoritesList);
		filterList(allKnownList, filteredKnownList);
		filterList(allRecommendedList, filteredRecommendedList);
		filterList(allNearMeList, filteredNearMeList);
	}
	
	// Construit une liste filtrée
	private void filterList(List<Profile> allProfilesList,
				List<Profile> filteredProfilesList) {
		// Supprimer les anciens profiles
		filteredProfilesList.clear();
		
		// Ajouter les profiles s'il passe le filtre
		for (Profile profile : allProfilesList) {
			if (doesRespectFilter(profile)) {
				filteredProfilesList.add(profile);
			}
		}
	}
	
	// Retourne vrai si le profil respect le filtre
	private boolean doesRespectFilter(Profile profile) {
		return (profile.getDistance() < maximalDistance)
				&& ((isAgeFiltered == false)
						|| ((profile.getAge() >= minAge) 
								&& (profile.getAge() <= maxAge)));
	}
	
	// Met a jour la distance maximal
	private void updateMaximalDistance() {
		maximalDistance = Integer.parseInt(sharedPreferences
				.getString(KEY_DISTANCE_PREFERENCE, "5000"));
	}
	
	// Met a jour le filtre des âges
	private void updateIsAgeFiltered() {
		isAgeFiltered = sharedPreferences.getBoolean(KEY_FILTER_AGE, false);
	}
	
	// Met a jour l'age minimal
	private void updateMinAge() {
		minAge = sharedPreferences.getInt(KEY_AGE_FROM, 18);
	}
	
	// Met a jour l'age maximal
	private void updateMaxAge() {
		maxAge = sharedPreferences.getInt(KEY_AGE_TO, 25);
	}

	
	/* Gestion des listeners des événements liés aux contacts
	-------------------------------------------------------------------------*/
	public void addContactListener(ContactListener listener) {
		listenerList.add(listener);
	}
	
	public void removeContactListener(ContactListener listener) {
		listenerList.remove(listener);
	}
	
	private void fireContactListUpdated() {
		synchronized (listenerList) {
			for (ContactListener listener : listenerList){
				listener.onContactListUpdated();
			}
		}
	}
}

