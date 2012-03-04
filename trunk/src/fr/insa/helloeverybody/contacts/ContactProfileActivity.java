package fr.insa.helloeverybody.contacts;

import java.util.ArrayList;
import java.util.List;

import fr.insa.helloeverybody.HelloEverybodyActivity;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.conversations.ConversationActivity;
import fr.insa.helloeverybody.models.*;
import fr.insa.helloeverybody.smack.ChatService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
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
	    inflater.inflate(R.menu.profil, menu);
	 
	    return true;
	}
	 
	// Méthode qui se déclenchera au clic sur un item
	@SuppressWarnings("static-access")
	public boolean onOptionsItemSelected(MenuItem item) {
	    // On regarde quel item a été cliqué grâce à son id et on déclenche une action
		switch (item.getItemId()) {
			case R.id.chat:
				// TODO : Un truc propre pour lancer une conversation
				//ChatService.GetChatService().newChat(String.valueOf(profile.getJid()), String.valueOf(profile.getId()));
				List<Conversation> pendingConversations = ConversationsList.getInstance().getPendingList();
				final Conversation conversation = new Conversation(false, profile.getId(), "Conv Test");
				pendingConversations.add(conversation);
				Intent intent = new Intent().setClass(this, ConversationActivity.class);
				intent.putExtra("id", conversation.getId());
				startActivity(intent);
				return true;
				
			case R.id.modify:
				// Ouvrir la fenêtre des paramètres
				Toast.makeText(ContactProfileActivity.this, "Modifier",
						Toast.LENGTH_SHORT).show();
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
	
	// Met/retire favori
	public void favoriteButtonClick(View view) {
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

		ContactsList.getInstance().update(profile, previousProfileType);
	}
}
