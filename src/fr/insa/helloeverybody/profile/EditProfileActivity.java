package fr.insa.helloeverybody.profile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import fr.insa.helloeverybody.OnstartActivity;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.device.DeviceHelper;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.RelationshipStatus;
import fr.insa.helloeverybody.models.SexStatus;
import fr.insa.helloeverybody.viewmodels.LocalUserProfile;

public class EditProfileActivity extends Activity {
	
	private static final int SELECT_PHOTO = 100;
	private static final String TEMP_FILE_NAME = "Hello_Everybody_Temp_Avatar.jpg";
	
	private LocalUserProfile userProfile;
	private Profile profile;
	private List<String> tampon;
	private Bitmap tempAvatar;
	private boolean mustCreateProfile = false;
	
	// Crée l'activité
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Récupération du profil de l'utilisateur dans le téléphone
		userProfile = LocalUserProfile.getInstance();
		profile = userProfile.getProfile();
		tampon = new ArrayList <String> ();
		
		setContentView(R.layout.edit_profil);
		
		if (profile == null) {
			setTitle("Créer votre profil");
			profile = new Profile(DeviceHelper.generateUniqueId());
			profile.setPassword("test");
			mustCreateProfile = true;
		}
		else {
			setTitle("Modifier votre profil");
		}
		
		this.findViewById(R.id.edit_accept).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveProfile();
				if (mustCreateProfile) {
					Intent onstartActivity = new Intent(EditProfileActivity.this, OnstartActivity.class);
					startActivity(onstartActivity);
				}
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
    
    // Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone
    public boolean onCreateOptionsMenu(Menu menu) {
 
        //Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML spécifier en un objet Menu
        inflater.inflate(R.menu.edit_profil, menu);
 
        return true;
	}
 
	// Méthode qui se déclenchera au clic sur un item
	public boolean onOptionsItemSelected(MenuItem item) {
         // On regarde quel item a été cliqué grâce à son id et on déclenche une action
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
		ImageView avatarButton = (ImageView) findViewById(R.id.avatar_button);

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
		
		// Mise à jour de l'avatar
		if (profile.getAvatar() != null) {
			avatarButton.setImageBitmap(profile.getAvatar());
			tempAvatar = profile.getAvatar();
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
		
		// Mise à jour des intérêts
		for (int i=0; i< profile.getInterestsList().size(); i++) {
			String interest = profile.getInterestsList().get(i).toString();
			this.addInterest(interest);
			tampon.add(interest);
		}

		// Handler de l'ajout d'un centre d'interet
		interestAddButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				LinearLayout interest = new LinearLayout(EditProfileActivity.this);
				LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				layout.setMargins(35, -10, 0, -10);
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
				OnClickListener listener = new View.OnClickListener() {
					public void onClick(View v) {
						interestsList.removeView((View) v.getParent());
					}
				};
				remove.setOnClickListener(listener);

				interest.addView(child);
				interest.addView(remove);
				interestsList.addView(interest);
				tampon.add(newInterest.getText().toString());
				newInterest.setText("");
				
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
		
		// Récupération de l'avatar
		profile.setAvatar(tempAvatar);
		
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
		if (mustCreateProfile)
			userProfile.createProfile();
		else
			userProfile.udpateProfile();
	}
	
	// Montre la bibliothèque d'images lorsque le button avatar est cliqué
	public void avatarButtonClick(View view) {
		if (tempAvatar == null) {
			changeAvatar();
		}
		else {
			showAvatarDialog();
		}
	}
	
	// Montre les actions liées à l'avatar
	private void showAvatarDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.avatar_title));
		builder.setItems(getResources().getStringArray(R.array.avatar_actions), 
				new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int position) {
		        switch (position) {
			        case 1:
			        	removeAvatar();
			        	break;
			        case 2:
			        	changeAvatar();
			        	break;
		        }
		    }
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	// Supprime l'avatar
	private void removeAvatar() {
		tempAvatar = null;
        ImageView avatarButton = (ImageView) findViewById(R.id.avatar_button);
        avatarButton.setImageResource(Profile.DEFAULT_AVATAR);
	}
	
	// Modifie l'avatar
	private void changeAvatar() {
		final Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		photoPickerIntent.putExtra("aspectX", 1);
		photoPickerIntent.putExtra("aspectY", 1);
		photoPickerIntent.putExtra("crop", "true");
		photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, SELECT_PHOTO);  
	}
	
	// Retourne l'adresse d'un fichier temporaire
	private Uri getTempUri() {
		return Uri.fromFile(getTempFile());
	}
	
	// Retourne un fichier temporaire
	private File getTempFile() {
		File storageDirectory = Environment.getExternalStorageDirectory();
		File avatarFile = new File(storageDirectory, TEMP_FILE_NAME);
		
		try {
			avatarFile.createNewFile();
		} catch (IOException e) {
			return null;
		}
		
		return avatarFile;
	}
	
	// Affecte l'image séléctionné
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
	    super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 
 
	    if ((resultCode == RESULT_OK) && (requestCode == SELECT_PHOTO)) {  
            ImageView avatarButton = (ImageView) findViewById(R.id.avatar_button);
            File avatarFile = getTempFile();
            if (avatarFile.exists()) {
            	tempAvatar = BitmapFactory.decodeFile(avatarFile.toString());
            	avatarFile.delete();
    		}
            tempAvatar = Bitmap.createScaledBitmap(tempAvatar, 100, 100, false);
            avatarButton.setImageBitmap(tempAvatar);
	    }
	}
	
	// Ajoute un intérêt
	private void addInterest(String interestString) {
		final LinearLayout interestsList = (LinearLayout) findViewById(R.id.edit_interests);
		LinearLayout interest = new LinearLayout(this);
		LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.setMargins(35, -10, 0, -10);
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
		OnClickListener listener = new View.OnClickListener() {
			public void onClick(View v) {
				interestsList.removeView((View) v.getParent());
			}
		};
		remove.setOnClickListener(listener);

		interest.addView(child);
		interest.addView(remove);
		interestsList.addView(interest);
		
	}
}
