package fr.insa.helloeverybody.contacts;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.helpers.FilterTextWatcher;
import fr.insa.helloeverybody.helpers.SeparatedContactsListAdapter;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.UserProfile;
import fr.insa.helloeverybody.preferences.UserPreferencesActivity;

public class ContactsListActivity extends Activity implements ContactsCallbackInterface {
	private ContactsActions contactsActions;
	private Profile profile;
	private ProgressDialog loading;
	
	private ListView contactsListView;
	private EditText filterText;
	private FilterTextWatcher filterTextWatcher;
	
    // Appel a la creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.contacts_list);
        
        // Vide les listes de profiles
        ContactsList contactsList = ContactsList.getInstance();
        contactsList.clearAllLists();
        
        // Recuperation du profil utilisateur
        profile = UserProfile.getInstance().getProfile();
        
        // Créer un listener sur le filtre
        filterTextWatcher = new FilterTextWatcher();
        filterText = (EditText) findViewById(R.id.search_box);
        filterText.addTextChangedListener(filterTextWatcher);
        
        
        // Création du gestionnaire des actions
        contactsActions = ContactsActions.getInstance(this, profile);
        contactsActions.register(this);
        contactsActions.askUpdateContacts();
        
		// Fenetre de chargement
		loading = ProgressDialog.show(ContactsListActivity.this,
				"Chargement...", "Récupération des contacts", true);
	}
    
    // Appel a l'affichage
    @Override
    public void onResume() {
    	super.onResume();
    	if (loading.isShowing() == false) {
    		updateContactsView();
    	}
    }
    
    // Appel lors de la destruction de l'activite par le systeme
    @Override
	protected void onDestroy() {
    	contactsActions.stopScheduledUpdate();
    	filterText.removeTextChangedListener(filterTextWatcher);
		super.onDestroy();
	}
    
    // Mettre a jour la liste de contacts
	public void contactsListUpdated(ArrayList<Profile> profilesList) {
		loading.dismiss();
		//Lancement les timers GPS
		contactsActions.contactsReceived();
        contactsActions.launchScheduledUpdate();
        
        ContactsList contactsList = ContactsList.getInstance();
        contactsList.clearAllLists();
		for (Profile profile : profilesList) {
			contactsList.addProfile(profile);
		}
		
		// Ajoute des faux contacts
		fillFakeList();
		
		// Affiche la liste des contacts
		updateContactsView();
		
		// Ajoute un click aux items
		final Intent intent = new Intent().setClass(this, ContactProfileActivity.class);
		contactsListView = (ListView) findViewById(R.id.contacts_list);
		contactsListView.setTextFilterEnabled(true);
		
		contactsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        		intent.putExtra("id", adapter.getItemIdAtPosition(position));
        		
        		startActivity(intent);
        	}
         });
	}

	// Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone
    public boolean onCreateOptionsMenu(Menu menu) {
 
        // Creation d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        
        // Instanciation du menu XML specifier en un objet Menu
        inflater.inflate(R.menu.contacts, menu);
 
        return true;
    }
    
	// Méthode qui se déclenchera au clic sur un item
	public boolean onOptionsItemSelected(MenuItem item) {
         // On regarde quel item a ete clique grace a son id et on declenche une action
         switch (item.getItemId()) {
            case R.id.parameters:
            	// Ouvrir la fenetre des parametres
               final Intent settingsActivity = new Intent(getBaseContext(), UserPreferencesActivity.class);
               startActivity(settingsActivity);
               return true;
            case R.id.logout:
            	// Déconnexion et quitter l'application
               finish();
               return true;
         }
         return false;
	}
	
	// Remplit les différentes listes de contacts
	private void updateContactsView() {

		final SeparatedContactsListAdapter listAdapter = new SeparatedContactsListAdapter(this);
		filterText.setText("");
		
		// Mettre a jour la ListView
		contactsListView = (ListView) findViewById(R.id.contacts_list);
		contactsListView.setAdapter(listAdapter);
        filterTextWatcher.setAdapter(listAdapter);
	}
	
	// Remplit la liste de favoris
	private void fillFakeList() {
        ContactsList contactsList = ContactsList.getInstance();
        
        // Favoris
        contactsList.addProfile(new Profile(null, 
								"Arthur", "M.", true, false, false));
		Profile bobProfile = new Profile(BitmapFactory.decodeResource(
						getResources(), R.drawable.sponge_bob),
						"Bob", "L'éponge", true, true, true);
		bobProfile.setAge(25);
		bobProfile.getInterestsList().add("Pêche à la méduse");
		bobProfile.getInterestsList().add("Karaté");
		bobProfile.getInterestsList().add("Bulles de savon");
		bobProfile.setJid("test");
		contactsList.addProfile(bobProfile);
	    contactsList.addProfile(new Profile(null, "Patrick", 
	    			"L'étoile de mer", true, false, true));
	    contactsList.addProfile(new Profile(null, "Timothée", 
	    			"L.", true, true, false));
	    
	    // Recents
	    Profile julian = new Profile(null, "Julian",
				"Dos Santos", false, true, false);
	    julian.setJid("julian");
		contactsList.addProfile(julian);
		contactsList.addProfile(new Profile(null, "Vincent", 
					"B.", false, true, true));
		
		// Recommandes
		contactsList.addProfile(new Profile(null, "Li Chen", 
					"T.", false, false, true));
		contactsList.addProfile(new Profile(null, "Loïc", 
					"T.", false, false, true));
		contactsList.addProfile(new Profile(null, "Rafael",
					"Corral", false, false, true));
	}
}