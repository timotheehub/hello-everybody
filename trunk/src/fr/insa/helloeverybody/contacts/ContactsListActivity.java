package fr.insa.helloeverybody.contacts;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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
import fr.insa.helloeverybody.models.Contact;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.Database;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.UserProfile;
import fr.insa.helloeverybody.preferences.UserPreferencesActivity;

public class ContactsListActivity extends Activity implements ContactsCallbackInterface {
	private static String TAG = "ContactsList";
	
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
        new Thread() {
        	@Override
        	public void run() {
        		try {
					sleep(10000);
					ContactsListActivity.this.runOnUiThread(new Runnable() {
						public void run() {
					        contactsActions = ContactsActions.getInstance(getApplicationContext(), profile);
					        contactsActions.register(ContactsListActivity.this);
					        contactsActions.askUpdateContacts();
						}
					});
				} catch (InterruptedException e) {
					Log.e(TAG, e.getMessage(), e);
				}
        	}
        }.start();
        
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
        if (profilesList != null) {
			for (Profile profile : profilesList) {
				contactsList.addProfile(profile);
			}
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
	
	public void contactWentOnline(String jid) {
		Log.d(TAG, "JID : " + jid + " went online");
	}

	public void contactWentOffline(String jid) {
		Log.d(TAG, "JID : " + jid + " went offline");
	}

	public void contactAdded(String jid) {
		Log.d(TAG, "JID : " + jid + " was added to the roster");
	}

	public void contactDeleted(String jid) {
		Log.d(TAG, "JID : " + jid + " went deleted from the roster");
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
        Database db = Database.getInstance();
        db.open();
        
        // Favoris
        Profile profile;
        profile = new Profile(null, "Arthur", "M.");
        profile.setJid("abma74331485947");
        Contact contact = db.retrieveContact(profile.getJid());
        if (contact != null) {
        	profile.setContact(contact);
        } else {
        	contact = new Contact(profile);
        	db.insertContact(contact);
        }
        
        contactsList.addProfile(profile);
        
        
		Profile bobProfile = new Profile(BitmapFactory.decodeResource(
						getResources(), R.drawable.sponge_bob),
						"Bob", "L'éponge");
		bobProfile.setAge(25);
		bobProfile.getInterestsList().add("Pêche à la méduse");
		bobProfile.getInterestsList().add("Karaté");
		bobProfile.getInterestsList().add("Bulles de savon");
		bobProfile.setJid("bbma74331485568");
		Contact bobContact = db.retrieveContact(bobProfile.getJid());
		if (bobContact != null) {
			bobProfile.setContact(bobContact);
		} else {
        	contact = new Contact(bobProfile);
        	db.insertContact(bobContact);
        }
		contactsList.addProfile(bobProfile);
		
		
		Profile profile2;
		profile2 = new Profile(null, "Patrick", "L'étoile de mer");
		profile2.setJid("cbva74331485947");
		Contact contact2 = db.retrieveContact(profile2.getJid());
		if (contact2 != null) {
			profile2.setContact(contact2);
		} else {
        	contact2 = new Contact(profile2);
        	db.insertContact(contact2);
        }
		contactsList.addProfile(profile2);
	
		
		Profile profile3;
		profile3 = new Profile(null, "Timothée", "L.");
		profile3.setJid("dbma74331485947");
		Contact contact3 = db.retrieveContact(profile3.getJid());
		if (contact3 != null) {
			profile3.setContact(contact3);
		} else {
        	contact3 = new Contact(profile3);
        	db.insertContact(contact3);
        }
		contactsList.addProfile(profile3);

		Profile profile4;
		profile4 = new Profile(null, "Julian", "Dos Santos");
		profile4.setJid("ebma74441485947");
		Contact contact4 = db.retrieveContact(profile4.getJid());
		if (contact4 != null) {
			profile4.setContact(contact4);
		} else {
        	contact4 = new Contact(profile4);
        	db.insertContact(contact4);
        }
		contactsList.addProfile(profile4);
		
		Profile profile5;
		profile5 = new Profile(null, "Vincent", "B.");
		profile5.setJid("fbma74551485947");
		Contact contact5 = db.retrieveContact(profile5.getJid());
		if (contact5 != null) {
			profile5.setContact(contact5);
		} else {
        	contact5 = new Contact(profile5);
        	db.insertContact(contact5);
        }
		contactsList.addProfile(profile5);
		
		// Recommandes
		
		
		Profile profile6;
		profile6 = new Profile(null, "Li Chen", "T.");
		profile6.setJid("gbma74661485947");
		Contact contact6 = db.retrieveContact(profile6.getJid());
		if (contact6 != null) {
			profile6.setContact(contact6);
		} else {
        	contact6 = new Contact(profile6);
        	db.insertContact(contact6);
        }
		contactsList.addProfile(profile6);
		
		
		Profile profile7;
		profile7 = new Profile(null, "Loïc", "T.");
		profile7.setJid("hbma74771485947");
		Contact contact7 = db.retrieveContact(profile7.getJid());
		if (contact7 != null) {
			profile7.setContact(contact7);
		} else {
        	contact7 = new Contact(profile7);
        	db.insertContact(contact7);
        }
		contactsList.addProfile(profile7);
		
		
		Profile profile8;
		profile8 = new Profile(null, "Rafael", "Corral");
		profile8.setJid("jbma74881485947");
		Contact contact8 = db.retrieveContact(profile8.getJid());
		if (contact8 != null) {
			profile8.setContact(contact8);
		} else {
        	contact8 = new Contact(profile8);
        	db.insertContact(contact8);
        }
		contactsList.addProfile(profile8);
		
		db.close();
	}
}