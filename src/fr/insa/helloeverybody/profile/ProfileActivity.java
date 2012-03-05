package fr.insa.helloeverybody.profile;

import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.UserProfile;
import fr.insa.helloeverybody.preferences.UserPreferencesActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ProfileActivity extends Activity {
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.profile);
        
        fillProfile();
    }
    
    
 /** Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone */
    public boolean onCreateOptionsMenu(Menu menu) {
 
        //Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML spécifier en un objet Menu
        inflater.inflate(R.menu.profil, menu);
 
        return true;
     }
    
    public void onResume() {
    	fillProfile();
    	super.onResume();
    }
 
       //Méthode qui se déclenchera au clic sur un item
      public boolean onOptionsItemSelected(MenuItem item) {
         //On regarde quel item a été cliqué grâce à son id et on déclenche une action
         switch (item.getItemId()) {
	     	case R.id.parameters:
	     		final Intent settingsActivity = new Intent(getBaseContext(), UserPreferencesActivity.class);
	            startActivity(settingsActivity);
	            return true;
            case R.id.modify:
            	// Ouvrir la fenêtre de modification du profil
            	Intent intent = new Intent().setClass(this, EditProfileActivity.class);
                startActivity(intent);
                return true;
            case R.id.logout:
            	// Déconnexion et quitter l'application
               finish();
               return true;
         }
         return false;
     }
     
   // Remplit le profil de l'utilisateur
  	private void fillProfile() {
  		
  			UserProfile userProfile = UserProfile.getInstance();
  			Profile profile = userProfile.getProfile();
  			TextView nameText = (TextView) this.findViewById(R.id.profile_name);
  			TextView ageText = (TextView) this.findViewById(R.id.profile_age);
  			TextView sexText = (TextView) this.findViewById(R.id.profile_sex);
  			TextView relationText = (TextView) this.findViewById(R.id.profile_relationship);
  			ListView interestListView = (ListView) this.findViewById(R.id.profile_interests);
  			
  			
  			if (profile.getFirstName() != null) {
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
  					ArrayAdapter<String> interestsAdapter = new ArrayAdapter<String>(this, R.layout.hobby_item, R.id.hobby, profile.getInterestsList());
  					interestListView.setAdapter(interestsAdapter);
  				}
  				
  			} else {
  				nameText.setText("Pas de profil");
  			}
  	}
}
