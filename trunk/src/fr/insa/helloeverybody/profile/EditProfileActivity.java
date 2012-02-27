package fr.insa.helloeverybody.profile;

import java.util.ArrayList;

import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.R.id;
import fr.insa.helloeverybody.R.layout;
import fr.insa.helloeverybody.R.menu;
import fr.insa.helloeverybody.helpers.InterestsAdapter;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.RelationshipStatus;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
	
	private Profile profileSave = new Profile("Prenom", "Nom", 23, RelationshipStatus.SINGLE, new ArrayList<String>());
	private Profile profile;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// TODO Récupération du profil de l'utilisateur dans le téléphone
		profile = new Profile("Prenom", "Nom", 23, RelationshipStatus.SINGLE, new ArrayList<String>());
		profile.addInterest("Informatique");
        profile.addInterest("Pêche");
        profile.addInterest("échecs");
        profile.addInterest("ski nautique");
          
	    setContentView(R.layout.edit_profil);
              
       // Méthode pour remplir les informations enregistrées pour le profil
       fillProfil();
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
            	
               return true;
            case R.id.cancel:
            	// Retour au profil
               finish();
               return true;
         }
         return false;
     }

      // Remplit le profil avec les informations enregistrées
   	private void fillProfil() {
   		// Récupération des champs
   		EditText givenName = (EditText) this.findViewById(R.id.editText3);
   		EditText familyName = (EditText) this.findViewById(R.id.editText1);
   		EditText age = (EditText) this.findViewById(R.id.editText2);
   		Spinner spinner = (Spinner) this.findViewById(R.id.spinner2);
   		ImageButton hobbyAddButton = (ImageButton) findViewById(R.id.hobbyAddButton);
  	    final EditText newInterest = (EditText) findViewById(R.id.editText4);
  	    ListView listView = (ListView) findViewById(R.id.hobbieslist);

  	    // Mise à jour des champs
  		givenName.setText(profile.getFirstName());
 		familyName.setText(profile.getLastName());
  		age.setText(profile.getAge().toString());
  		
  		// Spinner
  		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
  				this, R.array.situation_array, android.R.layout.simple_spinner_item);
  		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
  		spinner.setAdapter(adapter);
  		spinner.setSelection(2);

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
//		EditText givenName = (EditText) this.findViewById(R.id.editText3);
//   		EditText familyName = (EditText) this.findViewById(R.id.editText1);
//   		EditText age = (EditText) this.findViewById(R.id.editText2);
//   		Spinner situation = (Spinner) this.findViewById(R.id.spinner2);
//   		ListView interestListView = (ListView) findViewById(R.id.hobbieslist);
//   		
////		profile.setGiven_name(givenName.getText().toString());
//		profile.setFamily_name(familyName.getText().toString());
//		profile.setAge(Integer.parseInt(age.getText().toString()));
//		profile.setSituation(situation.getSelectedItem().toString());
////		profile.setHobbies((ArrayList<String>) listView.getSelectedItem());
		
	}
     

}
