package fr.insa.helloeverybody.device;

import java.util.ArrayList;
import java.util.List;

import fr.insa.helloeverybody.models.ContactRelationship;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.RelationshipStatus;
import fr.insa.helloeverybody.models.SexStatus;
import fr.insa.helloeverybody.viewmodels.LocalUserProfile;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseManager {
	
	public static final String TAG = "DatabaseManager";
 
	// Singleton
	private static DatabaseManager instance = null;
 
	// Base de données
	private DatabaseHelper databaseHelper;

	// Constructeur privé
	private DatabaseManager() { }
	
	// Retourne le singleton
	public synchronized static DatabaseManager getInstance(){
		if (instance == null) {
			instance = new DatabaseManager();
		}
		return DatabaseManager.instance;
	}
	
	// Initialise la base de données
	public synchronized void initDatabase(Context context) {
		this.databaseHelper = new DatabaseHelper(context);
	}
	 
	// Insére le profil utilisateur dans la base de données
	public synchronized void insertUserProfile() {
		
		// Vérifier que le profil de l'utilisateur existe
		Profile userProfile = LocalUserProfile.getInstance().getProfile();
		if (userProfile == null) {
			Log.w(TAG, "Cannot insert null user profile");
			return;
		}
		
		// Créer les valeurs de l'utilisateur
		ContentValues userProfileValues = profileToValues(userProfile);
		
		// Créer les valeurs des intérêts
		ContentValues interestValues = 
				interestToValues(userProfile.getInterestsList());
				
		// Insérer les valeurs dans la base de données
		try {
			SQLiteDatabase database = databaseHelper.getWritableDatabase();
			
			database.insert("table_userprofile", null, userProfileValues);
			database.delete("table_interests", null, null);
			if (interestValues.size() > 0) {
				database.insert("table_interests", null, interestValues);
			}
			
			database.close();
		}
		catch (Exception e) {
			Log.e(TAG, "Cannot insert user profile: " + e.getMessage());
		}
	}
 
	// Met à jour le profile utilisateur dans la base de données
	public synchronized void updateUserProfile() {
		
		// Vérifier que le profil de l'utilisateur existe
		Profile userProfile = LocalUserProfile.getInstance().getProfile();
		if (userProfile == null) {
			Log.w(TAG, "Cannot insert null user profile");
			return;
		}
		
		// Créer les valeurs de l'utilisateur
		ContentValues userProfileValues = profileToValues(userProfile);
		
		// Créer les valeurs des intérêts
		ContentValues interestValues = 
				interestToValues(userProfile.getInterestsList());
		
		// Insérer les valeurs dans la base de données
		try {
			SQLiteDatabase database = databaseHelper.getWritableDatabase();
			
			database.update("table_userprofile", userProfileValues, null, null);
			database.delete("table_interests", null, null);
			if (interestValues.size() > 0) {
				database.insert("table_interests", null, interestValues);
			}
			
			database.close();
		}
		catch (Exception e) {
			Log.e(TAG, "Cannot update user profile: " + e.getMessage());
		}
	}
	
	// Récupere le profile utilisateur depuis la base de données
	public synchronized Profile retrieveUserProfile() {
		Profile userProfile = null;
		String[] profileColumns = new String[] { "jid", "firstName", "lastName", 
				"age", "relationshipStatus", "sexStatus", "password" };
		
		// Récuperer le profil utilisateur
		try {
			SQLiteDatabase database = databaseHelper.getWritableDatabase();
			
			Cursor profileCursor = database.query("table_userprofile", 
					profileColumns, null, null, null, null, null);
			Cursor interestCursor = database.query("table_interests", 
					null, null, null, null, null, null, null);
			userProfile = cursorsToProfile(profileCursor, interestCursor);
			if (userProfile != null) {
				userProfile.setFriendsJidList(retrieveFavoritesJids(database));
			}
			
			database.close();
		} catch (Exception e) {
			Log.e(TAG, "Cannot retrieve user profile: " + e.getMessage());
		}
		
		return userProfile;
	}
	
	// Insére ou met à jour une relation avec un contact
	public void insertOrUpdateContact(Profile profile) {
		
		// Vérifier que le profil n'est pas nul
		if (profile == null) {
			return; 
		}
		
		// Récupérer le contact
		ContactRelationship contact = retrieveContact(profile.getJid());
		
		// Insérer le contact
		if (contact == null) {
			contact = new ContactRelationship(profile.getJid(), 
					profile.isFavorite(), profile.isKnown(), profile.isRecommended());
			insertContact(contact);
			
		// Ou mettre à jour le contact
		} else {
			contact.setFavorite(profile.isFavorite());
			contact.setKnown(profile.isKnown());
			contact.setRecommended(profile.isRecommended());
			updateContact(contact);
		}
	}
 
	// Insérer une relation avec un contact dans la base de données
	public synchronized void insertContact(ContactRelationship contact) {
		
		// Vérifier que le relation n'est pas nulle
		if (contact == null) {
			return;
		}
		
		// Créer les valeurs de la relation avec un contact
		ContentValues contactValues = contactToValues(contact);
		
		// Insérer la relation avec un contact
		try {
			SQLiteDatabase database = databaseHelper.getWritableDatabase();
			
			database.insert("table_contacts", null, contactValues);
			
			database.close();
		} catch (Exception e) {
			Log.e(TAG, "Cannot insert contact: " + e.getMessage());
		}
	}
	
	// Met à jour une relation avec un contact dans la base de données
	public synchronized void updateContact(ContactRelationship contact) {
		
		// Vérifier que le relation n'est pas nulle
		if (contact == null) {
			return;
		}
		
		// Créer les valeurs de la relation avec un contact
		ContentValues contactValues = contactToValues(contact);
		
		// Mettre à jour la relation avec un contact
		try {
			SQLiteDatabase database = databaseHelper.getWritableDatabase();
			
			database.update("table_contacts", contactValues, 
					"jid = \"" + contact.getJid() + "\"", null);
						
			database.close();
		} catch (Exception e) {
			Log.e(TAG, "Cannot update contact: " + e.getMessage());
		}
	}
	
	// Récupére une relation avec un contact depuis la base de données
	public synchronized ContactRelationship retrieveContact(String jid) {
		
		// Vérifier que le jid n'est pas nul
		if (jid == null) {
			return null;
		}
		
		// Variables
		ContactRelationship contact = null;
		String[] contactColumns = 
				new String[] {"jid", "favorite", "known", "recommend"};
		
		// Récupérer une relation avec un contact 
		try {
			SQLiteDatabase database = databaseHelper.getWritableDatabase();
			
			Cursor contactCursor = database.query("table_contacts", contactColumns, 
					"jid LIKE \"" + jid +"\"", null, null, null, null);
			contact = cursorToContact(contactCursor);
			 
			database.close();
		} catch (Exception e) {
			Log.e(TAG, "Cannot update contact: " + e.getMessage());
		}
		
		return contact;
	}
	
	// Récupere la liste des favoris
	private List<String> retrieveFavoritesJids(SQLiteDatabase database) {
		
		Cursor favoriteCursor = database.query("table_contacts", new String[] {"jid"}, 
				"favorite = \"" + 1 + "\"", null, null, null, null);
		return cursorToFavoriteList(favoriteCursor);
	}
	
	// Créer les valeurs d'un profil
	private ContentValues profileToValues(Profile profile) {
		
		// Créer les valeurs
		ContentValues profileValues = new ContentValues();
		profileValues.put("jid", profile.getJid());
		profileValues.put("firstName", profile.getFirstName());
		profileValues.put("lastName", profile.getLastName());
		profileValues.put("age", profile.getAge());
		profileValues.put("relationshipStatus", profile.getRelationshipString());
		profileValues.put("sexStatus", profile.getSexString());
		profileValues.put("password", profile.getPassword());
		
		// Retourner les valeurs
		return profileValues;
	}
	
	// Créer les valeurs des intérêts
	private ContentValues interestToValues(List<String> interestList) {
		
		// Créer les valeurs
		ContentValues interestValues = new ContentValues();
		for (String interest : interestList) {
			interestValues.put("interest", interest);
		}
		
		// Retourner les valeurs
		return interestValues;
	}
	
	// Créer les valeurs d'une relation avec un contact
	private ContentValues contactToValues(ContactRelationship contact) {
		
		// Créer les valeurs
		ContentValues contactValues = new ContentValues();
		contactValues.put("jid", contact.getJid());
		
		// Favori
		if (contact.getFavorite()) {
			contactValues.put("favorite", 1);
		} else {
			contactValues.put("favorite", 0);
		}
		
		// Connu
		if (contact.getKnown()) {
			contactValues.put("known", 1);
		} else {
			contactValues.put("known", 0);
		}
		
		// Recommandé
		if (contact.getRecommended()) {
			contactValues.put("recommend", 1);
		} else {
			contactValues.put("recommend", 0);
		}
		
		return contactValues;
	}
	
	// Crée un profile à partir de curseurs
	private Profile cursorsToProfile(Cursor profileCursor, Cursor interestCursor){
		
		// Vérifier que le profil existe
		if (!profileCursor.moveToFirst()) {
			profileCursor.close();
			interestCursor.close();
			return null;
		}
 		
		// Créer le profile
		Profile profile = new Profile(profileCursor.getString(0));
		profile.setFirstName(profileCursor.getString(1));
		profile.setLastName(profileCursor.getString(2));
		profile.setAge(profileCursor.getInt(3));
		String relationship = profileCursor.getString(4);
		String sex = profileCursor.getString(5);
		profile.setPassword(profileCursor.getString(6));
		
		// Statut de relation du profil
		if (relationship.equals("Célibataire")) {
			profile.setRelationshipStatus(RelationshipStatus.SINGLE);
		} else if (relationship.equals("En couple")) {
			profile.setRelationshipStatus(RelationshipStatus.COUPLE);
		} else if (relationship.equals("Non divulguée")) {
			profile.setRelationshipStatus(RelationshipStatus.SECRET);
		}
		
		// Sexe du profil
		if (sex.equals("Homme")) {
			profile.setSexStatus(SexStatus.MAN);
		} else if (sex.equals("Femme")) {
			profile.setSexStatus(SexStatus.WOMAN);
		}
		profileCursor.close();
		
		// Vérifier qu'il y a au moins un intéret
		if (!interestCursor.moveToFirst()) {
			interestCursor.close();
			return profile;
		}
		
		// Ajouter le liste des intérêts
		List<String> interestsList = new ArrayList<String>();
		do {
			interestsList.add(interestCursor.getString(0));
		} while (interestCursor.moveToNext());
		profile.setInterestsList(interestsList);
		interestCursor.close();
		
		// Retourner le profil
		return profile;
	}

	// Créer une relation avec un contact à partir d'un curseur
	private ContactRelationship cursorToContact(Cursor contactCursor) {
		
		// Vérifier qu'il y au moins un contact
		if (!contactCursor.moveToFirst()) {
			contactCursor.close();
			return null;
		}
 
		// Créer le contact
		ContactRelationship contact = new ContactRelationship();
		contact.setJid(contactCursor.getString(0));
		
		// Favori
		if (contactCursor.getInt(1) == 1) {
			contact.setFavorite(true);
		} else {
			contact.setFavorite(false);
		}
		
		// Récent
		if (contactCursor.getInt(2) == 1) {
			contact.setKnown(true);
		} else {
			contact.setKnown(false);
		}
		
		// Recommandé
		if (contactCursor.getInt(3) == 1) {
			contact.setRecommended(true);
		} else {
			contact.setRecommended(false);
		}
		contactCursor.close();
		
		// Retourner le contact
		return contact;
	}
	
	// Créer une liste de favoris à partir d'un curseur
	private List<String> cursorToFavoriteList(Cursor favoriteCursor) {

		List<String> jidList = new ArrayList<String>(); 
		
		// Vérifier qu'il y a au moins un contact
		if (!favoriteCursor.moveToFirst()) {
			favoriteCursor.close();
			return jidList;
		}
		
		// Ajouter les favoris à la liste
        do {
        	jidList.add(favoriteCursor.getString(0));
        } while (favoriteCursor.moveToNext());
        favoriteCursor.close();
        
        // Retourner la liste de favoris
        return jidList;
	}
}