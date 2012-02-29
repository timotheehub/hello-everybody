package fr.insa.helloeverybody.models;


public class UserProfile {

	// Singleton
	private static UserProfile instance = new UserProfile();
	private Profile profile;
		
	/** private constructor pour être sûr que personne ne pourra créer une instance **/
	private UserProfile(){

	}
	
	public void init(Long id) {
		Database db = Database.getInstance();
		db.open();
		this.profile = db.retrieveProfile(id);
		db.close();
	}

	public static UserProfile getInstance(){
		return UserProfile.instance;
	}

	public void saveProfile() {
		Database db = Database.getInstance();
		db.open();
		db.insertProfile(this.profile);
		db.close();
	}
	
	public Profile getProfile() {
		return this.profile;
	}

	public void setProfile(Profile profile2) {
		this.profile = profile2;
		
	}
	
	public void retrieve(Long id){
		Database db = Database.getInstance();
		db.open();
		this.profile = db.retrieveProfile(id);
		db.close();
	}
	
}
	