package fr.insa.helloeverybody.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.preferences.UserPreferencesActivity;
import fr.insa.helloeverybody.viewmodels.LocalUserProfile;

public class ProfileActivity extends Activity {
	
	// Crée l'activité
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		fillProfile();
	}

    // Reprend l'activité
    public void onResume() {
    	super.onResume();
    	fillProfile();
    }
    
    // Crée le menu
    public boolean onCreateOptionsMenu(Menu menu) {
 
        //Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML spécifier en un objet Menu
        inflater.inflate(R.menu.profil, menu);
 
        return true;
     }
 
    //Méthode qui se déclenchera au clic sur un item
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	
    		// Modifier les préférences
     		case R.id.parameters:
     			final Intent settingsActivity = new Intent(getBaseContext(), UserPreferencesActivity.class);
     			startActivity(settingsActivity);
     			return true;
     			
     		// Modifier le profil
     		case R.id.modify:
     			Intent intent = new Intent().setClass(this, EditProfileActivity.class);
     			startActivity(intent);
     			// TODO(fonctionnalité): Sauvegarder le profil sur les deux parties du serveur
     			return true;
     			
     		// Quitter l'application
	        case R.id.logout:
	           finish();
	           return true;
	    }
    	
	    return false;
	}
     
    // Remplit le profil de l'utilisateur
  	private void fillProfile() {
		// Récupération des composants
		LocalUserProfile userProfile = LocalUserProfile.getInstance();
		Profile profile = userProfile.getProfile();
		TextView nameText = (TextView) findViewById(R.id.profile_name);
		TextView ageText = (TextView) findViewById(R.id.profile_age);
		TextView sexText = (TextView) findViewById(R.id.profile_sex);
		TextView relationText = (TextView) findViewById(R.id.profile_relationship);
		ListView interestListView = (ListView) findViewById(R.id.profile_interests);
		ImageView avatarView = (ImageView) findViewById(R.id.profile_avatar);
		
		// Si le profil existe mise à jour des champs renseignés
		if ((profile != null) && (profile.getFirstName() != null)) {
			if (profile.getLastName() != null) {
				nameText.setText(profile.getFirstName() + " " + profile.getLastName());
			} else {
				nameText.setText(profile.getFirstName());
			}
			
			if (profile.getAge() != null) {
  				ageText.setText(profile.getAge().toString() + " ans");
  			}
			
			if (profile.getSexStatus() != null) {
				sexText.setText(profile.getSexString());
			}
			
			if (profile.getRelationshipStatus() != null) {
				relationText.setText(profile.getRelationshipString());
			}
			
			if (profile.getInterestsList() != null) {
				ArrayAdapter<String> interestsAdapter = 
						new ArrayAdapter<String>(this, R.layout.hobby_item, 
									R.id.hobby, profile.getInterestsList());
				interestListView.setAdapter(interestsAdapter);
			}
			
			if (profile.getAvatar() != null) {
				avatarView.setImageBitmap(profile.getAvatar());
			}
			else {
				avatarView.setImageResource(Profile.DEFAULT_AVATAR);
			}
			
		} else {
			nameText.setText("Pas de profil");
		}
  	}
}
