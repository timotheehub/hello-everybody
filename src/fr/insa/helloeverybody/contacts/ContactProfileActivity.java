package fr.insa.helloeverybody.contacts;

import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.models.*;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ContactProfileActivity extends Activity {
	
	private Profile profile;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.contact_profile);
	    
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
	    inflater.inflate(R.menu.contact_profile, menu);
	 
	    return true;
	}
	 
	// Méthode qui se déclenchera au clic sur un item
	public boolean onOptionsItemSelected(MenuItem item) {
	    // On regarde quel item a été cliqué grâce à son id et on déclenche une action
		switch (item.getItemId()) {
			case R.id.chat:
				ConversationsList.getInstance().sendInvitation(profile.getJid());
				setKnown();
				return true;
				
			case R.id.favorites:
				// Met/retire des favoris
				setFavorites();
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
		// Favori
		ImageButton favoriteButton = (ImageButton) findViewById(R.id.favorite_button); 
		if (profile.isFavorite()) {
			favoriteButton.setImageResource(R.drawable.star_big_on);
		}
		else {
			favoriteButton.setImageResource(R.drawable.star_big_off);
		}
		
		// Nom
		TextView nameView = (TextView) findViewById(R.id.profile_name);
		nameView.setText(profile.getFirstName() + " " + profile.getLastName());
		
		// Infos
		TextView infosView = (TextView) findViewById(R.id.profile_infos);
		infosView.setText(profile.getAge() +  " ans - " + profile.getSexString());
		
		// Avatar
		ImageView avatarView = (ImageView) findViewById(R.id.profile_avatar);
		Bitmap avatarBitmap = profile.getAvatar();
		if (avatarBitmap != null) {
			avatarView.setImageBitmap(avatarBitmap);
		}
		else {
			avatarView.setImageResource(Profile.DEFAULT_AVATAR);
		}
		
		// Situation
		TextView relationshipView = (TextView) findViewById(R.id.profile_relationship);
		relationshipView.setText(profile.getRelationshipString());
		
		// Centres d'interets
		ListView hobbiesView = (ListView) findViewById(R.id.profile_hobby);
        ArrayAdapter<String> hobbiesAdapter = new ArrayAdapter<String>(this,
        		R.layout.hobby_item, R.id.hobby, profile.getInterestsList());
        hobbiesView.setAdapter(hobbiesAdapter);
		
	}
	
	// Met/retire des favoris
	public void favoriteButtonClick(View view) {
		setFavorites();
	}
	
	// Met/retire des favoris
	private void setFavorites() {
		ImageButton favoriteButton = (ImageButton) findViewById(R.id.favorite_button); 
		ProfileType previousProfileType = profile.getProfileType();
		
		if (profile.isFavorite()) {
			profile.setFavorite(false);
			favoriteButton.setImageResource(R.drawable.star_big_off);
		}
		else {
			profile.setFavorite(true);
			favoriteButton.setImageResource(R.drawable.star_big_on);
		}
		
		Database db = Database.getInstance();
		db.open();
			Contact contact = db.retrieveContact(profile.getJid());
			if (contact != null) {
				contact.setFavorite(profile.isFavorite());
				db.updateContact(contact);
			} else {
				contact = new Contact(profile.getJid(), profile.isFavorite(), false, false);
				db.insertContact(contact);
			}
		db.close();

		ContactsList.getInstance().update(profile, previousProfileType);
	}
	
	// Met en connu au lancement d'une conversation
	private void setKnown() {
		ProfileType previousProfileType = profile.getProfileType();
		if (!profile.isKnown()) {
			profile.setKnown(true);
		
			Database db = Database.getInstance();
			db.open();
				Contact contact = db.retrieveContact(profile.getJid());
				if (contact != null) {
					contact.setKnown(true);
					db.updateContact(contact);
				} else {
					contact = new Contact(profile.getJid(), false, true, false);
					db.insertContact(contact);
				}
			db.close();

			ContactsList.getInstance().update(profile, previousProfileType);
		}
	}
}
