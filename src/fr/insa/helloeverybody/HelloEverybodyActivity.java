package fr.insa.helloeverybody;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import fr.insa.helloeverybody.device.DeviceHelper;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.Database;
import fr.insa.helloeverybody.models.UserProfile;
import fr.insa.helloeverybody.profile.EditProfileActivity;

public class HelloEverybodyActivity extends Activity {
	public final static int CONVERSATION_LAUCHED = 1;
	public final static int DECONNECTION = 2;

	/** Called when the activity is first created. */

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Test de connectivité
		 */
		DeviceHelper deviceHelper = new DeviceHelper(this);
		if (!deviceHelper.isOnline()) {
			Toast.makeText(this, "Connexion Internet absente ! Fermeture..;", 10).show();
			//TODO : Changer le finish
			finish();
		}

		Database.getInstance().initDatabase(this.getApplicationContext());
		ContactsList.getInstance().initContactsList(this.getApplicationContext());
		
		UserProfile userProfile = UserProfile.getInstance();		
		userProfile.retrieve();
		if (userProfile.getProfile() == null) {
			Intent newProfileActivity = new Intent(this, EditProfileActivity.class);
            startActivity(newProfileActivity);
            finish();
		} else {	
			Intent newProfileActivity = new Intent(this, OnstartActivity.class);
            startActivity(newProfileActivity);
            finish();
		}
	}
	
	

}