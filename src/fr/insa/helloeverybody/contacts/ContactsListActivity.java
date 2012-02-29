package fr.insa.helloeverybody.contacts;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.helpers.FilterTextWatcher;
import fr.insa.helloeverybody.helpers.SeparatedContactsListAdapter;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.Profile;

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
        contactsList.getFavoritesList().clear();
        contactsList.getKnownList().clear();
        contactsList.getRecommendedList().clear();
        contactsList.getNearMeList().clear();
        
        //Creation du profil utilisateur
        //TODO: Récupération du vrai profil
        profile = new Profile();
        profile.setFirstName("Prenom");
        profile.setLastName("Nom");
        
        // Créer un listener sur le filtre
        filterTextWatcher = new FilterTextWatcher();
        filterText = (EditText) findViewById(R.id.search_box);
        filterText.addTextChangedListener(filterTextWatcher);
        
        // Création du gestionnaire des actions
        contactsActions = new ContactsActions(this, profile, this);
        
        //Demande de Login + MAJ des Contacts
        contactsActions.askLogin();
        //Lancement les timers GPS
        contactsActions.launchScheduledUpdate();
        
        // Fenetre de chargement
        loading = ProgressDialog.show(ContactsListActivity.this, "Chargement...", "Récupération des contacts", true);
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
        ContactsList contactsList = ContactsList.getInstance();
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
        	@SuppressWarnings("unchecked")
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
               Toast.makeText(ContactsListActivity.this, "Parametres Contact", Toast.LENGTH_SHORT).show();
               return true;
            case R.id.search:
            	// Ouvrir la fenetre de recherche
                Toast.makeText(ContactsListActivity.this, "Recherche", Toast.LENGTH_SHORT).show();
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
        contactsList.addProfile(new Profile(R.drawable.default_profile_icon, 
								"Arthur", "M.", true, false, false));
		Profile bobProfile = new Profile(R.drawable.sponge_bob,
								"Bob", "L'éponge", true, true, true);
		bobProfile.setAge(25);
		bobProfile.getInterestsList().add("Pêche à la méduse");
		bobProfile.getInterestsList().add("Karaté");
		bobProfile.getInterestsList().add("Bulles de savon");
		bobProfile.setJid("darksnoopy42@gmail.com");
		contactsList.addProfile(bobProfile);
	    contactsList.addProfile(new Profile(R.drawable.default_profile_icon,
								"Patrick", "L'étoile de mer", true, false, true));
	    contactsList.addProfile(new Profile(R.drawable.default_profile_icon,
								"Timothée", "L.", true, true, false));
	    
	    // Recents
		contactsList.addProfile(new Profile(R.drawable.default_profile_icon,
								"Julian", "Dos Santos", false, true, false));
		contactsList.addProfile(new Profile(R.drawable.default_profile_icon,
								"Vincent", "B.", false, true, true));
		
		// Recommandes
		contactsList.addProfile(new Profile(R.drawable.default_profile_icon,
								"Li Chen", "T.", false, false, true));
		contactsList.addProfile(new Profile(R.drawable.default_profile_icon,
								"Loïc", "T.", false, false, true));
		contactsList.addProfile(new Profile(R.drawable.default_profile_icon,
								"Raphaël", "Corral", false, false, true));
	}
}