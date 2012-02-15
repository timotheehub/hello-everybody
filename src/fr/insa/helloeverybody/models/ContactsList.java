package fr.insa.helloeverybody.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		favoritesList = Collections.synchronizedList(new ArrayList<Profile>());
		knownList = Collections.synchronizedList(new ArrayList<Profile>());
		recommendedList = Collections.synchronizedList(new ArrayList<Profile>());
		nearMeList = Collections.synchronizedList(new ArrayList<Profile>()); 
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
}
