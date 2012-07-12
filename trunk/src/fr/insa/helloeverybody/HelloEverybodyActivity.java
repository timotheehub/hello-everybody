package fr.insa.helloeverybody;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import fr.insa.helloeverybody.device.DatabaseManager;
import fr.insa.helloeverybody.device.DeviceHelper;
import fr.insa.helloeverybody.device.GpsHelper;
import fr.insa.helloeverybody.notifications.NotificationCenter;
import fr.insa.helloeverybody.profile.EditProfileActivity;
import fr.insa.helloeverybody.viewmodels.ContactsList;
import fr.insa.helloeverybody.viewmodels.LocalUserProfile;

public class HelloEverybodyActivity extends Activity {

	// Crée l'activité
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Vérifier qu'il y a une connection Internet
		if (!DeviceHelper.isOnline(this.getApplicationContext())) {
			Toast.makeText(this, "Connexion Internet absente ! Fermeture..;", 10).show();
			//TODO(fonctionnalité): Changer le finish
			finish();
		}

		// Initialiser les singletons
		DatabaseManager.getInstance().initDatabase(this.getApplicationContext());
		ContactsList.getInstance().initContactsList(this.getApplicationContext());
		GpsHelper.getInstance().initLocationManager(this.getApplicationContext());
		NotificationCenter.getInstance().initNotificationCenter(this.getApplicationContext());
		
		// Si le profil existe, démarrer l'application
		if (LocalUserProfile.getInstance().retrieveProfile()) {
			Intent startActivity = new Intent(this, OnstartActivity.class);
            startActivity(startActivity);
            finish();
            
        // Sinon, créer le profil
		} else {	
			Intent createProfileActivity = new Intent(this, EditProfileActivity.class);
            startActivity(createProfileActivity);
            finish();
		}
		
	}
}