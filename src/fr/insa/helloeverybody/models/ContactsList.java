package fr.insa.helloeverybody.models;

import java.util.LinkedList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import fr.insa.helloeverybody.models.*;

public class ContactsList {

	// Singleton
	private static ContactsList instance = null;
	
	// Attributes
	private List<Profile> favoritesList;
	private List<Profile> knownList;
	private List<Profile> recommendedList;
	private List<Profile> nearMeList;
	
	// Constructeur privee
	private ContactsList() {
		favoritesList = Collections.synchronizedList(new LinkedList<Profile>());
		knownList = Collections.synchronizedList(new LinkedList<Profile>());
		recommendedList = Collections.synchronizedList(new LinkedList<Profile>());
		nearMeList = Collections.synchronizedList(new LinkedList<Profile>()); 
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
		return favoritesList;
	}
	
	// Retourne la liste de recents
	public List<Profile> getKnownList() {
		return knownList;
	}
	
	// Retourne la liste de recommandes
	public List<Profile> getRecommendedList() {
		return recommendedList;
	}
	
	// Retourne la liste de personnes a proximite
	public List<Profile> getNearMeList() {
		return nearMeList;
	}
	
	// Retourne un profil en fonction de son identifiant
	public Profile getProfileById(Long id) {
		for (Profile profile : favoritesList) {
			if (profile.getId().equals(id)) {
				return profile;
			}
		}
		
		for (Profile profile : knownList) {
			if (profile.getId().equals(id)) {
				return profile;
			}
		}
		
		for (Profile profile : recommendedList) {
			if (profile.getId().equals(id)) {
				return profile;
			}
		}
		
		for (Profile profile : nearMeList) {
			if (profile.getId().equals(id)) {
				return profile;
			}
		}
		
		return null;
	}
	
	// Retourne un profil en fonction de son identifiant
		public Profile getProfileByJid(String jid) {
			for (Profile profile : favoritesList) {
				if (profile.getJid().equals(jid)) {
					return profile;
				}
			}
			
			for (Profile profile : knownList) {
				if (profile.getJid().equals(jid)) {
					return profile;
				}
			}
			
			for (Profile profile : recommendedList) {
				if (profile.getJid().equals(jid)) {
					return profile;
				}
			}
			
			for (Profile profile : nearMeList) {
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
				favoritesList.remove(profile);
				break;
			case KNOWN:
				knownList.remove(profile);
				break;
			case RECOMMENDED:
				recommendedList.remove(profile);
				break;
			case NEAR_ME:
				nearMeList.remove(profile);
				break;
		}
		addProfile(profile);
	}
	
	// Ajoute un profil a une liste
	public void addProfile(Profile profile) {
		switch (profile.getProfileType()) {
			case FAVORITE:
				addProfile(favoritesList, profile);
				break;
			case KNOWN:
				addProfile(knownList, profile);
				break;
			case RECOMMENDED:
				addProfile(recommendedList, profile);
				break;
			case NEAR_ME:
				addProfile(nearMeList, profile);
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
}
