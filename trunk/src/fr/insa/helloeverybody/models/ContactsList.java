package fr.insa.helloeverybody.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import java.util.LinkedList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class ContactsList implements OnSharedPreferenceChangeListener {

	public static final String KEY_DISTANCE_PREFERENCE = "distance_preference";
	public static final String KEY_FILTER_AGE = "filter_age";
	public static final String KEY_AGE_FROM = "age_from";
	public static final String KEY_AGE_TO = "age_to";
	
	// Singleton
	private static ContactsList instance = null;
	
	// Attributes
	private List<Profile> allFavoritesList;
	private List<Profile> allKnownList;
	private List<Profile> allRecommendedList;
	private List<Profile> allNearMeList;
	private LinkedList<Profile> filteredFavoritesList;
	private List<Profile> filteredKnownList;
	private List<Profile> filteredRecommendedList;
	private List<Profile> filteredNearMeList;
	private SharedPreferences sharedPreferences;
	private int maximalDistance;
	private boolean isAgeFiltered;
	private int minAge;
	private int maxAge;
	
	// Constructeur privee
	private ContactsList() {
		allFavoritesList = new LinkedList<Profile>();
		allKnownList = new LinkedList<Profile>();
		allRecommendedList = new LinkedList<Profile>();
		allNearMeList = new LinkedList<Profile>();
		filteredFavoritesList = new LinkedList<Profile>();
		filteredKnownList = new LinkedList<Profile>();
		filteredRecommendedList = new LinkedList<Profile>();
		filteredNearMeList = new LinkedList<Profile>(); 
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
	
	// Retourne le singleton de maniere protegee
	public static synchronized ContactsList getInstance() {
		if (instance == null) {
			instance = new ContactsList();
		}
		return instance;
	}
	
	// Retourne la liste de favoris
	public List<Profile> getFavoritesList() {
		return new LinkedList<Profile>(filteredFavoritesList);
	}
	
	// Retourne la liste de recents
	public List<Profile> getKnownList() {
		return new LinkedList<Profile>(filteredKnownList);
	}
	
	// Retourne la liste de recommandes
	public List<Profile> getRecommendedList() {
		return new LinkedList<Profile>(filteredRecommendedList);
	}
	
	// Retourne la liste de personnes a proximite
	public List<Profile> getNearMeList() {
		return new LinkedList<Profile>(filteredNearMeList);
	}
	
	// Retourne un profil en fonction de son identifiant
	public Profile getProfileById(Long id) {
		for (Profile profile : allFavoritesList) {
			if (profile.getId().equals(id)) {
				return profile;
			}
		}
		
		for (Profile profile : allKnownList) {
			if (profile.getId().equals(id)) {
				return profile;
			}
		}
		
		for (Profile profile : allRecommendedList) {
			if (profile.getId().equals(id)) {
				return profile;
			}
		}
		
		for (Profile profile : allNearMeList) {
			if (profile.getId().equals(id)) {
				return profile;
			}
		}
		
		return null;
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
		addProfile(profile);
	}
	
	// Ajoute un profil a une liste
	public void addProfile(Profile profile) {
		switch (profile.getProfileType()) {
			case FAVORITE:
				addProfile(allFavoritesList, profile);
				if (doesRespectFilter(profile)) {
					addProfile(filteredFavoritesList, profile);
				}
				break;
			case KNOWN:
				addProfile(allKnownList, profile);
				if (doesRespectFilter(profile)) {
					addProfile(filteredKnownList, profile);
				}
				break;
			case RECOMMENDED:;
				addProfile(allRecommendedList, profile);
				if (doesRespectFilter(profile)) {
					addProfile(filteredRecommendedList, profile);
				}
				break;
			case NEAR_ME:
				addProfile(allNearMeList, profile);
				if (doesRespectFilter(profile)) {
					addProfile(filteredNearMeList, profile);
				}
				break;
		}
	}
	
	// Ajouter un profil selon l'ordre alphabétique
	private void addProfile(List<Profile> profileList, Profile profile) {
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
						|| ((profile.getAge() >= minAge) && (profile.getAge() <= maxAge)));
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
}

