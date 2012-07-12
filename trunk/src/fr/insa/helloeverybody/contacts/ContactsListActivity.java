package fr.insa.helloeverybody.contacts;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.controls.FilterTextWatcher;
import fr.insa.helloeverybody.controls.SeparatedContactsListAdapter;
import fr.insa.helloeverybody.device.DatabaseManager;
import fr.insa.helloeverybody.interfaces.ContactListener;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.ProfileType;
import fr.insa.helloeverybody.preferences.UserPreferencesActivity;
import fr.insa.helloeverybody.smack.XmppContactsManager;
import fr.insa.helloeverybody.smack.XmppRoomManager;
import fr.insa.helloeverybody.viewmodels.ContactsList;

/* Activité qui affiche la liste les contacts
-----------------------------------------------------------------------------*/
public class ContactsListActivity extends Activity implements ContactListener {	

	// Constantes
	private static final int START_CONVERSATION_ID = 1;
	private static final int MODIFY_FAVORITE_ID = 2;
	private static final int MODIFY_TYPE_ID = 3;
	
	// Attributs pour le téléchargement
	private DownloaderThread downloaderThread;
	private ContactListDownloader contactListDownloader;
	
	// Attributs pour l'interface utilisateur
	private ProgressDialog loading;
	private ListView contactsListView;
	private EditText filterText;
	private FilterTextWatcher filterTextWatcher;
	private SeparatedContactsListAdapter contactListAdapter;
	
    // Crée l'activité
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.contacts_list);
        
        // Créer un listener sur le filtre
        filterTextWatcher = new FilterTextWatcher();
        filterText = (EditText) findViewById(R.id.search_box);
        filterText.addTextChangedListener(filterTextWatcher);
		
		// Créer l'adaptateur
		contactListAdapter = new SeparatedContactsListAdapter(this);
		contactsListView = (ListView) findViewById(R.id.contacts_list);
		contactsListView.setAdapter(contactListAdapter);
		filterTextWatcher.setAdapter(contactListAdapter);
		
		// Gérer les clics sur les contacts
		final Intent intent = new Intent().setClass(this, ContactProfileActivity.class);
		contactsListView = (ListView) findViewById(R.id.contacts_list);
		contactsListView.setTextFilterEnabled(true);
		
		contactsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            	if (contactListAdapter == null) {
            		return;
            	}
        		intent.putExtra("jid", contactListAdapter.getStringId(position));
        		startActivity(intent);
        	}
        });
		
		// Gérer les clics longs sur les contacts
		registerForContextMenu(contactsListView);
        
		// Afficher la fenêtre de chargement
		loading = ProgressDialog.show(ContactsListActivity.this,
				"Chargement...", "Récupération des contacts", true);
        
        // Télécharger la liste de contacts
        contactListDownloader = new ContactListDownloader();
        contactListDownloader.startDownloadContactList();
    }
    
    // Démarre l'activité
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	// TODO(architecture): ajouter le listener avant de lancer le téléchargement
		ContactsList.getInstance().addContactListener(this);
		updateContactsView();
    }

    // Arrête l'activité
    @Override
    protected void onStop() {
    	super.onStop();
		ContactsList.getInstance().removeContactListener(this);
    }
    
    // Détruit l'activité
    @Override
	protected void onDestroy() {
		super.onDestroy();
    	contactListDownloader.stopDownloadContactList();
    	filterText.removeTextChangedListener(filterTextWatcher);
    	if (downloaderThread != null) {
    		downloaderThread.stop();
    	}
	}
     
    // Crée la liste d'actions pour les clics longs
    @Override  
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {  
	    super.onCreateContextMenu(menu, view, menuInfo);  
	    
	    if (view.getId() == R.id.contacts_list) {
	        AdapterView.AdapterContextMenuInfo info = 
	        		(AdapterView.AdapterContextMenuInfo) menuInfo;
	        Profile profile = (Profile) 
	        		contactsListView.getItemAtPosition(info.position);
	        menu.setHeaderTitle(profile.getFullName());
	        
	        // Ajouter la liste des actions possibles
	        menu.add(Menu.NONE, START_CONVERSATION_ID, 0, 
	        		getResources().getString(R.string.chat));
	        if  (profile.isFavorite()) {
	        	menu.add(Menu.NONE, MODIFY_FAVORITE_ID, 1, 
	        			getResources().getString(R.string.favori_delete));
	        } else {
	        	menu.add(Menu.NONE, MODIFY_FAVORITE_ID, 1, 
	        			getResources().getString(R.string.favori_add));
	        	if (profile.isKnown()) { 
	        		menu.add(Menu.NONE, MODIFY_TYPE_ID, 2, 
	        				getResources().getString(R.string.known_delete));
	        	}
	        	else if (profile.isRecommended()) {
	        		menu.add(Menu.NONE, MODIFY_TYPE_ID, 2, 
	        				getResources().getString(R.string.recommend_delete));
	        	}
	        }
		}
    }   
    
    // Exécute l'action choisie après un clic long
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    	int menuItemIndex = item.getItemId();
    	Profile profile = (Profile) contactsListView.getItemAtPosition(info.position);
    	ProfileType previousProfileType = profile.getProfileType();
	  
    	// Créer un chat
    	if (menuItemIndex == START_CONVERSATION_ID) {
    		XmppRoomManager.getInstance().createPrivateRoom(profile.getJid());
    		profile.setKnown(true);
    		
    	// Ajouter ou retirer des favoris
    	} else if (menuItemIndex == MODIFY_FAVORITE_ID) {
    		if (profile.isFavorite()) {
    			profile.setFavorite(false);
    		} else {
    			profile.setFavorite(true);
    		}
    		
    	// Retirer des connus ou des recommandés
    	} else if (menuItemIndex == MODIFY_TYPE_ID) {
    		if (profile.isKnown()) {
    			profile.setKnown(false);
    		} else {
    			profile.setRecommended(false);
    		} 
    	}
    	
    	// Mettre le contact à jour
    	DatabaseManager.getInstance().insertOrUpdateContact(profile);
    	ContactsList.getInstance().update(profile, previousProfileType);
    	this.updateContactsView();
    	
    	return true;
    }
    
	// Crée le menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contacts, menu);
 
        return true;
    }
    
    // Exécute l'action correspondant au bouton du menu cliqué
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         
         	// Afficher la fênetre des paramètres
            case R.id.parameters:
               final Intent settingsActivity = new Intent(getBaseContext(), UserPreferencesActivity.class);
               startActivity(settingsActivity);
               return true;
               
            // Quitter l'application
            case R.id.logout:
               finish();
               return true;
         }
         
         return false;
	}
    
	// Met à jour la vue des listes de contacts
	private void updateContactsView() {
		contactListAdapter.update();
	}
	
	
	/* Implémentation de l'interface du listener des contacts
	-------------------------------------------------------------------------*/
    // Met a jour la liste de contacts
	public void onContactListUpdated() {
		loading.dismiss();
		
		// Afficher la liste des contacts
		runOnUiThread(new Runnable() {
			public void run() {
				updateContactsView();
			}
		});
		
		// Telecharge les VCards
		if (downloaderThread == null) {
			downloaderThread = new DownloaderThread(contactListAdapter);
			downloaderThread.start();
		}
	}

	
	/* Classe pour télécharger les VCards des profils
	-------------------------------------------------------------------------*/
	private class DownloaderThread extends Thread {

		// Adaptateur pour la liste des contacts
		private SeparatedContactsListAdapter listAdapter;
		
		// Constructeur
		public DownloaderThread(SeparatedContactsListAdapter listAdapter) {
			super();
			this.listAdapter = listAdapter;
		}

		// Télécharge les profils
		@Override
		public void run() {
			List<Profile> profilesList = ContactsList.getInstance().getProfilesList();
			
			// Télécharger les profiles non téléchargés
			for (Profile profile : profilesList) {
				if (!profile.isDownloaded()) {
					
					// Télcharger le profil
					Profile downloadedProfile = 
							XmppContactsManager.downloadProfile(profile.getJid());
					if (downloadedProfile != null) {
						profile.update(downloadedProfile);
					}
					
					// Mettre à jour la vue des listes de contacts
					if (listAdapter != null) {
						runOnUiThread(new Runnable() {
							public void run() {
								listAdapter.notifyDataSetChanged();
							}
						});
					}
				}
			}
			
			// Signaler la fin du téléchargement
			downloaderThread = null;
		}
	}
}