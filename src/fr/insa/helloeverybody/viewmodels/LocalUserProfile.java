package fr.insa.helloeverybody.viewmodels;

import fr.insa.helloeverybody.device.DatabaseManager;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.profile.ImageSaver;


public class LocalUserProfile {

	// Singleton
	private static LocalUserProfile instance = null;
	private Profile profile;
		
	// Constructeur privé
	private LocalUserProfile(){
		profile = null;
	}
	
	// Retourne le singleton
	public static synchronized LocalUserProfile getInstance(){
		if (instance == null) {
			instance = new LocalUserProfile();
		}
		return LocalUserProfile.instance;
	}
	
	// Retourne le profil
	public Profile getProfile() {
		return profile;
	}

	// Fixe la valeur du profil
	public void setProfile(Profile profile) {
		this.profile = profile;
	}
	
	// Récupère le profil depuis la base de données
	public boolean retrieveProfile(){
		// Vérifier que le profil existe
		this.profile = DatabaseManager.getInstance().retrieveUserProfile();
		if (profile == null) {
			return false;
		}
		
		// Récupèrer l'avatar
		profile.setAvatar(ImageSaver.getAvatar());
		return true;
	}
	
	// Insert le profil dans la base de données
	public void createProfile() {
		DatabaseManager.getInstance().insertUserProfile();
		ImageSaver.saveAvatar(profile.getAvatar());
	}

	// Met à jour le profile dans la base de données
	public void udpateProfile() {
		DatabaseManager.getInstance().updateUserProfile();
		ImageSaver.saveAvatar(profile.getAvatar());
	}
}
	