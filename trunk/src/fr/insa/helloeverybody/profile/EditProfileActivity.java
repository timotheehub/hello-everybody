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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditProfileActivity extends Activity {
	
	private UserProfile userProfile;
	private Profile profile;
	private List<String> tampon;
	private Integer id;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Récupération du profil de l'utilisateur dans le téléphone
		userProfile = UserProfile.getInstance();
		profile = userProfile.getProfile();
		tampon = new ArrayList <String> ();
		id = 0;
		
		setContentView(R.layout.edit_profil);
		
		this.findViewById(R.id.edit_accept).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveProfile();
				finish();
			}
		});
		
		this.findViewById(R.id.edit_cancel).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		

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
		// Récupération des composants de l'interface
		EditText fisrtNameText = (EditText) this.findViewById(R.id.edit_first_name);
		EditText lastNameText = (EditText) this.findViewById(R.id.edit_last_name);
		EditText ageText = (EditText) this.findViewById(R.id.edit_age);
		Spinner relationshipSpinner = (Spinner) this.findViewById(R.id.edit_relationship);
		Spinner sexSpinner = (Spinner) this.findViewById(R.id.edit_sex);
		ImageButton interestAddButton = (ImageButton) findViewById(R.id.edit_add_interest);
		final EditText newInterest = (EditText) findViewById(R.id.editText4);
		final LinearLayout interestsList = (LinearLayout) findViewById(R.id.edit_interests);

		// Mise à jour des champs avec les valeurs du profil
		if (profile.getFirstName() != null) {
			fisrtNameText.setText(profile.getFirstName());
		}
		
		if (profile.getLastName() != null) {
			lastNameText.setText(profile.getLastName());
		}
		
		if (profile.getAge() != null) {
			ageText.setText(profile.getAge().toString());
		}
		
		// Mise à jour du status de la relation
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
		
		// Mise à jour du sexe
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
		
		int i;
		for (i=0; i< profile.getInterestsList().size(); i++) {
			String interest = profile.getInterestsList().get(i).toString();
			this.addInterest(interest);
			tampon.add(interest);
			id++;
		}

		//Handler de l'ajout d'un centre d'interet
		interestAddButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				LinearLayout interest = new LinearLayout(EditProfileActivity.this);
				LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				layout.setMargins(10, -10, 0, -10);
				interest.setLayoutParams(layout);
				interest.setGravity(Gravity.CENTER_VERTICAL);

				TextView child = new TextView(EditProfileActivity.this);
				child.setText(newInterest.getText().toString());
				LinearLayout.LayoutParams layout2 = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT,
						(float) 1.00);
				layout2.setMargins(0, 0, 0, 0);
				child.setLayoutParams(layout2);
				child.setTextSize(20);

				ImageButton remove = new ImageButton(EditProfileActivity.this, null);
				LinearLayout.LayoutParams layout3 = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT,
						(float) 0.00);
				layout3.setMargins(0, 0, 0, 0);
				remove.setLayoutParams(layout3);
				remove.setImageResource(R.drawable.button_rmv);
				remove.setBackgroundColor(android.R.color.transparent);
				remove.setTag((Integer) id);
				OnClickListener listener = new View.OnClickListener() {
					public void onClick(View v) {
						interestsList.removeViewAt((Integer) v.getTag());
					}
				};
				remove.setOnClickListener(listener);

				interest.addView(child);
				interest.addView(remove);
				interestsList.addView(interest);
				tampon.add(newInterest.getText().toString());
				id++;
				
			}
		});
	}
   	
	// Sauvegarde le profil de l'utilisateur
	private void saveProfile() {
		// Recupération des composants de l'interface
		EditText firstName = (EditText) this.findViewById(R.id.edit_first_name);
   		EditText lastName = (EditText) this.findViewById(R.id.edit_last_name);
   		EditText age = (EditText) this.findViewById(R.id.edit_age);
   		Spinner sex = (Spinner) this.findViewById(R.id.edit_sex);
   		Spinner situation = (Spinner) this.findViewById(R.id.edit_relationship);
   		LinearLayout interestsListLayout = (LinearLayout) findViewById(R.id.edit_interests);
   		
   		// Récupération du nom et de l'age
		profile.setFirstName(firstName.getText().toString());
		profile.setLastName(lastName.getText().toString());
		profile.setAge(Integer.parseInt(age.getText().toString()));
		
		// Récupération du sexe
		String sexString = sex.getSelectedItem().toString();
		if (sexString.equals("Homme")) {
			profile.setSexStatus(SexStatus.MAN);
		} else if (sexString.equals("Femme")) {
			profile.setSexStatus(SexStatus.WOMAN);
		}
		
		// Récupération du status de la relation
		String situationString = situation.getSelectedItem().toString();
		if (situationString.equals("Célibataire")) {
			profile.setRelationshipStatus(RelationshipStatus.SINGLE);
		} else if (situationString.equals("En couple")) {
			profile.setRelationshipStatus(RelationshipStatus.COUPLE);
		} else if (situationString.equals("Non divulguée")) {
			profile.setRelationshipStatus(RelationshipStatus.SECRET);
		}
		
		// Récupération des centres d'interets
		int i;
		List<String> interestsList = new ArrayList <String> ();
		for (i=0; i<interestsListLayout.getChildCount(); i++) {
			interestsList.add(((TextView) ((LinearLayout) interestsListLayout.getChildAt(i)).getChildAt(0)).getText().toString());
		}
		profile.setInterestsList(interestsList);
		// Sauvegarde du profil
		userProfile.setProfile(profile);
		userProfile.saveProfile();
	
		
	}
	
	private void addInterest(String interestString) {
		final LinearLayout interestsList = (LinearLayout) findViewById(R.id.edit_interests);
		LinearLayout interest = new LinearLayout(this);
		LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.setMargins(10, -10, 0, -10);
		interest.setLayoutParams(layout);
		interest.setGravity(Gravity.CENTER_VERTICAL);

		TextView child = new TextView(this);
		child.setText(interestString);
		LinearLayout.LayoutParams layout2 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT,
				(float) 1.00);
		layout2.setMargins(0, 0, 0, 0);
		child.setLayoutParams(layout2);
		child.setTextSize(20);

		ImageButton remove = new ImageButton(this, null);
		LinearLayout.LayoutParams layout3 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT,
				(float) 0.00);
		layout3.setMargins(0, 0, 0, 0);
		remove.setLayoutParams(layout3);
		remove.setImageResource(R.drawable.button_rmv);
		remove.setBackgroundColor(android.R.color.transparent);
		remove.setTag((Integer) id);
		OnClickListener listener = new View.OnClickListener() {
			public void onClick(View v) {
				interestsList.removeViewAt((Integer) v.getTag());
			}
		};
		remove.setOnClickListener(listener);

		interest.addView(child);
		interest.addView(remove);
		interestsList.addView(interest);
		
	}
}
