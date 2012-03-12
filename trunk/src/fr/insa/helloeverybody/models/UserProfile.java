package fr.insa.helloeverybody.models;

import fr.insa.helloeverybody.profile.ImageSaver;


public class UserProfile {

	// Singleton
	private static UserProfile instance = new UserProfile();
	private Profile profile;
		
	/** private constructor pour être sûr que personne ne pourra créer une instance **/
	private UserProfile(){

	}
	
	public void init() {
		Database db = Database.getInstance();
		db.open();
		this.profile = db.retrieveProfile();
		db.close();
	}

	public static UserProfile getInstance(){
		return UserProfile.instance;
	}

	public void saveProfile() {
		Database db = Database.getInstance();
		db.open();
		db.updateProfile(this.profile);
		db.close();
		ImageSaver.saveAvatar(this.profile.getAvatar());
	}
	
	public void createProfile() {
		Database db = Database.getInstance();
		db.open();
		db.insertProfile(this.profile);
		db.close();
		ImageSaver.saveAvatar(this.profile.getAvatar());
	}
	
	public Profile getProfile() {
		return this.profile;
	}

	public void setProfile(Profile profile2) {
		this.profile = profile2;
		
	}
	
	public void retrieve(){
		Database db = Database.getInstance();
		db.open();
		this.profile = db.retrieveProfile();
		db.close();
		if (profile != null) {
			profile.setAvatar(ImageSaver.getAvatar());
		}
	}
	
}
	