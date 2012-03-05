package fr.insa.helloeverybody.profile;

import java.util.ArrayList;
import java.util.List;

import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.R.id;
import fr.insa.helloeverybody.R.layout;
import fr.insa.helloeverybody.R.menu;
import fr.insa.helloeverybody.device.DeviceHelper;
import fr.insa.helloeverybody.helpers.InterestsAdapter;
import fr.insa.helloeverybody.models.Database;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.RelationshipStatus;
import fr.insa.helloeverybody.models.SexStatus;
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
            	finish();
//            	Intent intent = new Intent().setClass(this, ProfileActivity.class);
//                startActivity(intent);
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
		EditText fisrtNameText = (EditText) this.findViewById(R.id.edit_first_name);
		EditText lastNameText = (EditText) this.findViewById(R.id.edit_last_name);
		EditText ageText = (EditText) this.findViewById(R.id.edit_age);
		Spinner relationshipSpinner = (Spinner) this.findViewById(R.id.edit_relationship);
		Spinner sexSpinner = (Spinner) this.findViewById(R.id.edit_sex);
		ImageButton hobbyAddButton = (ImageButton) findViewById(R.id.hobbyAddButton);
		final EditText newInterest = (EditText) findViewById(R.id.editText4);
		ListView listView = (ListView) findViewById(R.id.edit_interests);

	    // Mise à jour des champs
		if (profile.getFirstName() != null) {
			fisrtNameText.setText(profile.getFirstName());
		}
		
		if (profile.getLastName() != null) {
			lastNameText.setText(profile.getLastName());
		}
		
		if (profile.getAge() != null) {
			ageText.setText(profile.getAge().toString());
		}
		
		// Spinner
		ArrayAdapter<CharSequence> relationshipAdapter = ArrayAdapter.createFromResource(
				this, R.array.profile_relationship_status, android.R.layout.simple_spinner_item);
		relationshipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		relationshipSpinner.setAdapter(relationshipAdapter);
		if (profile.getRelationshipStatus() != null) {
			switch (profile.getRelationshipStatus()) {
			case SINGLE : 
				relationshipSpinner.setSelection(0);
				break;
			case COUPLE :
				relationshipSpinner.setSelection(1);
				break;
			case SECRET :
				relationshipSpinner.setSelection(2);
				break;
			}
			
		}
		
		// Spinner
		ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource(
				this, R.array.profile_sex_status, android.R.layout.simple_spinner_item);
		sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sexSpinner.setAdapter(sexAdapter);
		if (profile.getSexStatus() != null) {
			switch (profile.getSexStatus()) {
			case MAN : 
				sexSpinner.setSelection(0);
				break;
			case WOMAN :
				sexSpinner.setSelection(1);
				break;
			}
			
		}
		
  		// Centres d'interets
  		ListView interestListView = (ListView) findViewById(R.id.edit_interests);
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
		EditText firstName = (EditText) this.findViewById(R.id.edit_first_name);
   		EditText lastName = (EditText) this.findViewById(R.id.edit_last_name);
   		EditText age = (EditText) this.findViewById(R.id.edit_age);
   		Spinner sex = (Spinner) this.findViewById(R.id.edit_sex);
   		Spinner situation = (Spinner) this.findViewById(R.id.edit_relationship);
   		ListView interestListView = (ListView) findViewById(R.id.edit_interests);
   		
   		
		profile.setFirstName(firstName.getText().toString());
		profile.setLastName(lastName.getText().toString());
		profile.setAge(Integer.parseInt(age.getText().toString()));
		
		String sexString = sex.getSelectedItem().toString();
		if (sexString.equals("Homme")) {
			profile.setSexStatus(SexStatus.MAN);
		} else if (sexString.equals("Femme")) {
			profile.setSexStatus(SexStatus.WOMAN);
		}
		
		String situationString = situation.getSelectedItem().toString();
		if (situationString.equals("Célibataire")) {
			profile.setRelationshipStatus(RelationshipStatus.SINGLE);
		} else if (situationString.equals("En couple")) {
			profile.setRelationshipStatus(RelationshipStatus.COUPLE);
		} else if (situationString.equals("Non divulguée")) {
			profile.setRelationshipStatus(RelationshipStatus.SECRET);
		}
		
		int i;
		List<String> interestsList = new ArrayList <String> ();
		for (i=0; i<interestListView.getAdapter().getCount(); i++) {
			interestsList.add(interestListView.getItemAtPosition(i).toString());
		}
		profile.setInterestsList(interestsList);
		userProfile.setProfile(profile);
		userProfile.saveProfile();
	
//		profile.setHobbies((ArrayList<String>) listView.getSelectedItem());
		
	}
     

}
