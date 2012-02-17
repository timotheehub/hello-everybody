package fr.insa.helloeverybody.contacts;

import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.Profile;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactProfileActivity extends Activity {
	
	private Profile profile;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.profile);
	    
	    long profileId = getIntent().getExtras().getLong("id");
	    
	    profile = ContactsList.getInstance().getProfileById(profileId);
	    
	    if (profile != null) {
	    	fillProfile();
	    }
	}
	
	
	// Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone
	public boolean onCreateOptionsMenu(Menu menu) {
	 
		//Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
		MenuInflater inflater = getMenuInflater();
		//Instanciation du menu XML spécifier en un objet Menu
	    inflater.inflate(R.menu.profil, menu);
	 
	    return true;
	}
	 
	// Méthode qui se déclenchera au clic sur un item
	public boolean onOptionsItemSelected(MenuItem item) {
	    // On regarde quel item a été cliqué grâce à son id et on déclenche une action
		switch (item.getItemId()) {
			case R.id.modify:
				// Ouvrir la fenêtre des paramètres
				Toast.makeText(ContactProfileActivity.this, "Modifier", Toast.LENGTH_SHORT).show();
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
		// Nom
		TextView nameView = (TextView) findViewById(R.id.profile_name);
		nameView.setText(profile.getFirstName() + " " + profile.getLastName());
		
		// Infos
		TextView infosView = (TextView) findViewById(R.id.profile_infos);
		infosView.setText(profile.getAge().toString() +  " ans - " + profile.getSexString());
		
		// Avatar
		ImageView avatarView = (ImageView) findViewById(R.id.profile_avatar);
		avatarView.setImageResource(profile.getAvatar());
		
		// Situation
		TextView relationshipView = (TextView) findViewById(R.id.profile_relationship);
		relationshipView.setText(profile.getRelationshipString());
		
		// Centres d'interets
		ListView hobbiesView = (ListView) findViewById(R.id.profile_hobby);
        ArrayAdapter<String> hobbiesAdapter = new ArrayAdapter<String>(this,
        		R.layout.hobby_item, R.id.hobby, profile.getInterestsList());
        hobbiesView.setAdapter(hobbiesAdapter);
		
	}
}
