package fr.insa.helloeverybody;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import fr.insa.helloeverybody.contacts.ContactsListActivity;
import fr.insa.helloeverybody.conversations.ConversationsListActivity;
import fr.insa.helloeverybody.device.DeviceHelper;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.ConversationsList;
import fr.insa.helloeverybody.models.Database;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.UserProfile;
import fr.insa.helloeverybody.profile.EditProfileActivity;
import fr.insa.helloeverybody.profile.ImageSaver;
import fr.insa.helloeverybody.profile.ProfileActivity;

public class HelloEverybodyActivity extends Activity {
	public final static int CONVERSATION_LAUCHED = 1;
	public final static int DECONNECTION = 2;
	private static View convTabView=null;

	/** Called when the activity is first created. */

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Test de connectivité
		 */
		DeviceHelper deviceHelper = new DeviceHelper(this);
		if (!deviceHelper.isOnline()) {
			Toast.makeText(this, "Connexion Internet absente ! Fermeture..;", 10);
			//TODO : Changer le finish
			finish();
		}

		Database.getInstance().initDatabase(this.getApplicationContext());
		ContactsList.getInstance().initContactsList(this.getApplicationContext());
		
		UserProfile userProfile = UserProfile.getInstance();		
		userProfile.retrieve(0L);
		if (userProfile.getProfile() == null) {
			/*Profile profile = new Profile();
			profile.setAvatar(ImageSaver.getAvatar());
			profile.setFirstName("Julian");
			profile.setJid(new DeviceHelper(this.getApplicationContext()).generateUniqueId());
			profile.setPassword("test");
			userProfile.setProfile(profile);
			userProfile.saveProfile();*/
			
			Intent newProfileActivity = new Intent(this, EditProfileActivity.class);
            startActivity(newProfileActivity);
            finish();
		}
		else {	
			Intent newProfileActivity = new Intent(this, OnstartActivity.class);
            startActivity(newProfileActivity);
            finish();
		}
	}
	
	

}