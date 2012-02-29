package fr.insa.helloeverybody.profile;

import java.util.ArrayList;

import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.R.id;
import fr.insa.helloeverybody.R.layout;
import fr.insa.helloeverybody.R.menu;
import fr.insa.helloeverybody.device.DeviceHelper;
import fr.insa.helloeverybody.helpers.InterestsAdapter;
import fr.insa.helloeverybody.models.Database;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.RelationshipStatus;
import fr.insa.helloeverybody.models.UserProfile;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditProfileActivity extends Activity {
	
	private UserProfile userProfile;
	private Profile profile;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Récupération du profil de l'utilisateur dans le téléphone
		userProfile = UserProfile.getInstance();
		DeviceHelper deviceHelper = new DeviceHelper(this.getApplicationContext());
		profile = userProfile.getProfile();
		
		setContentView(R.layout.edit_profil);

		// Méthode pour remplir les informations enregistrées pour le profil
		fillProfile();
    }
    
    
 /** M�thode qui se d�clenchera lorsque vous appuierez sur le bouton menu du t�l�phone */
    public boolean onCreateOptionsMenu(Menu menu) {
 
        //Cr�ation d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML sp�cifier en un objet Menu
        inflater.inflate(R.menu.edit_profil, menu);
 
        return true;
     }
 
       //M�thode qui se d�clenchera au clic sur un item
      public boolean onOptionsItemSelected(MenuItem item) {
         //On regarde quel item a �t� cliqu� gr�ce � son id et on d�clenche une action
         switch (item.getItemId()) {
            case R.id.accept:
            	// Enregistrement du profil
            	saveProfile();
            	Intent intent = new Intent().setClass(this, ProfileActivity.class);
                startActivity(intent);
               return true;
            case R.id.cancel:
            	// Retour au profil
               finish();
               return true;
         }
         return false;
     }

// Remplit le profil avec les informations enregistrées
	private void fillProfile() {
		// Récupération des champs
		EditText fisrtNameText = (EditText) this.findViewById(R.id.editText3);
		EditText lastNameText = (EditText) this.findViewById(R.id.editText1);
//		EditText ageText = (EditText) this.findViewById(R.id.editText2);
		Spinner spinner = (Spinner) this.findViewById(R.id.spinner2);
		ImageButton hobbyAddButton = (ImageButton) findViewById(R.id.hobbyAddButton);
		final EditText newInterest = (EditText) findViewById(R.id.editText4);
		ListView listView = (ListView) findViewById(R.id.hobbieslist);

	    // Mise à jour des champs
		if (profile.getFirstName() != null) {
			fisrtNameText.setText(profile.getFirstName());
		}
		
		if (profile.getLastName() != null) {
			lastNameText.setText(profile.getLastName());
		}
		
//		if (profile.getAge() != null) {
//			ageText.setText(profile.getAge());
//		}
		
		// Spinner
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.profile_relationship_status, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		if (profile.getRelationshipStatus() != null) {
			switch (profile.getRelationshipStatus()) {
			case SINGLE : 
				spinner.setSelection(0);
				break;
			case COUPLE :
				spinner.setSelection(1);
				break;
			case SECRET :
				spinner.setSelection(2);
				break;
			}
			
		}
		
  		// Centres d'interets
  		ListView interestListView = (ListView) findViewById(R.id.hobbieslist);
  		final InterestsAdapter interestAdapter = new InterestsAdapter(this, profile.getInterestsList());
  		interestListView.setAdapter(interestAdapter);
//  		interestListView.setOnItemClickListener(new OnItemClickListener() {
//            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
//            	String hobby = adapter.getItemAtPosition(position).toString();
//            	profile.removeHobby(hobby);
//        		interestAdapter.notifyDataSetChanged();
//        	}
//         });
  		interestListView.setOnItemLongClickListener(new OnItemLongClickListener() {
  			public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
  				String interest = adapter.getItemAtPosition(position).toString();
  				profile.removeInterest(interest);
  	    		interestAdapter.notifyDataSetChanged();
  	    		return false;
  			}
        	
  		});
  		hobbyAddButton.setOnClickListener(new View.OnClickListener() {       
  		public void onClick(View v) {
 	           profile.addInterest(newInterest.getText().toString());
 	           interestAdapter.notifyDataSetChanged();
 	       }
 	   	});
    }
   	
      // Sauvegarde le profil de l'utilisateur
	private void saveProfile() {
		EditText givenName = (EditText) this.findViewById(R.id.editText3);
   		EditText familyName = (EditText) this.findViewById(R.id.editText1);
   		EditText age = (EditText) this.findViewById(R.id.editText2);
   		Spinner situation = (Spinner) this.findViewById(R.id.spinner2);
   		ListView interestListView = (ListView) findViewById(R.id.hobbieslist);
   		
   		
		profile.setFirstName(givenName.getText().toString());
		profile.setLastName(familyName.getText().toString());
//		profile.setAge(Integer.parseInt(age.getText().toString()));
//		profile.setRelationshipStatus((RelationshipStatus) situation.getSelectedItem());
		userProfile.setProfile(profile);
		userProfile.saveProfile();
	
	//	profile.setSituation(situation.getSelectedItem().toString());
//		profile.setHobbies((ArrayList<String>) listView.getSelectedItem());
		
	}
     

}
