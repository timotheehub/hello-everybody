package fr.insa.helloeverybody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ContactsActivity extends Activity implements ContactsCallbackInterface {
	private ContactsActions contactsActions;
	private Profile profile;
	private ProgressDialog loading;
	
	// Listes de contacts
	private ListView contactsListView;
	private List<Profile> favoritesList = new ArrayList<Profile>();
	private List<Profile> knownList = new ArrayList<Profile>();
	private List<Profile> recommendedList = new ArrayList<Profile>();
	private List<Profile> nearMeList = new ArrayList<Profile>();
	
    // Appele a la creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Creation du profil utilisateur
        //TODO: Récupération du vraie profil
        profile = new Profile();
        profile.setFirstName("MonNom");
        profile.setLastName("Prenom");
        
        //Création du gestionnaire des actions
        contactsActions = new ContactsActions(this, profile, this);
        
        //Demande de MAJ des Contacts
        contactsActions.askUpdateContacts();
        contactsActions.launchScheduledUpdate();
        
        // Fenetre de chargement
        loading = ProgressDialog.show(ContactsActivity.this, "Chargement...", "Récupération des contacts", true);
    }
    
    // Mettre a jour la liste de contacts
	public void contactsListUpdated(ArrayList<Profile> contactsList) {
		loading.dismiss();
		nearMeList.addAll(contactsList);
		setContentView(R.layout.contacts_list);
		
		// Remplit les listes et les affiche
		fillFavoritesList();
		fillKnownList();
		fillRecommendedList();
		
		fillContactsView();
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
               Toast.makeText(ContactsActivity.this, "Parametres Contact", Toast.LENGTH_SHORT).show();
               return true;
            case R.id.search:
            	// Ouvrir la fenetre de recherche
                Toast.makeText(ContactsActivity.this, "Recherche", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.logout:
            	// Déconnexion et quitter l'application
               finish();
               return true;
         }
         return false;
	}
	
	// Remplit les différentes listes de contacts
	private void fillContactsView() {

		SeparatedListAdapter listAdapter = new SeparatedListAdapter(this);
		
		listAdapter.addSection(getString(R.string.favorites), getFavoritesAdapter());
		listAdapter.addSection(getString(R.string.known), getKnownAdapter());
		listAdapter.addSection(getString(R.string.recommended), getRecommendedAdapter());
		listAdapter.addSection(getString(R.string.near_me), getNearMeAdapter());
		
		contactsListView = (ListView) findViewById(R.id.contacts_list);
		contactsListView.setAdapter(listAdapter);
		

		final Intent intent;  // Reusable Intent for each tab
		
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, ContactProfileActivity.class);
		
		contactsListView.setOnItemClickListener(new OnItemClickListener() {
        	@SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				// On récupére la HashMap
        		Map<String, String> map = (Map<String, String>) adapter.getItemAtPosition(position);

        		// On affiche le bouton cliquï¿½
        		if (map != null)
        		{
        			startActivity(intent);
        		}
        	}
         });
	}
	
	// Retourne l'adaptateur des favoris
	private SimpleAdapter getFavoritesAdapter() {
		List<Map<String, String>> favoritesAttributesList = new ArrayList<Map<String, String>>();
				
		Map<String, String> favoriteAttributesMap;
		for (Profile profile : favoritesList) {
			favoriteAttributesMap = new HashMap<String, String>();
			favoriteAttributesMap.put("firstName", profile.getFirstName());
			favoriteAttributesMap.put("lastName", profile.getLastName());
			favoriteAttributesMap.put("picture", String.valueOf(profile.getAvatar()));
			favoritesAttributesList.add(favoriteAttributesMap);
		}
        
        // Creation d'un SimpleAdapter qui se chargera de mettre
        // les favoris de la liste dans la vue contact_item
        SimpleAdapter favoritesAdapter = new SimpleAdapter (this.getBaseContext(),
        		favoritesAttributesList, R.layout.contact_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
        
        return favoritesAdapter;
	}
	
	// Retourne l'adaptateur des récents
	private SimpleAdapter getKnownAdapter() {
		List<Map<String, String>> knownAttributesList = new ArrayList<Map<String, String>>();
		
		Map<String, String> knownAttributesMap;
		for (Profile profile : knownList) {
			knownAttributesMap = new HashMap<String, String>();
			knownAttributesMap.put("firstName", profile.getFirstName());
			knownAttributesMap.put("lastName", profile.getLastName());
			knownAttributesMap.put("picture", String.valueOf(profile.getAvatar()));
			knownAttributesList.add(knownAttributesMap);
		}
        
        // Création d'un SimpleAdapter qui se chargera de mettre
        // les récents de la liste dans la vue contact_item
        SimpleAdapter knownAdapter = new SimpleAdapter (this.getBaseContext(),
        		knownAttributesList, R.layout.contact_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
        
        return knownAdapter;
	}
	
	// Retourne l'adaptateur des recommandés
	private SimpleAdapter getRecommendedAdapter() {
		List<Map<String, String>> recommendedAttributesList = new ArrayList<Map<String, String>>();
		
		Map<String, String> recommendedAttributesMap;
		for (Profile profile : recommendedList) {
			recommendedAttributesMap = new HashMap<String, String>();
			recommendedAttributesMap.put("firstName", profile.getFirstName());
			recommendedAttributesMap.put("lastName", profile.getLastName());
			recommendedAttributesMap.put("picture", String.valueOf(profile.getAvatar()));
			recommendedAttributesList.add(recommendedAttributesMap);
		}
        
        // Création d'un SimpleAdapter qui se chargera de mettre
        // les recommandés de la liste dans la vue contact_item
        SimpleAdapter recommendedAdapter = new SimpleAdapter (this.getBaseContext(),
        		recommendedAttributesList, R.layout.contact_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
        
        return recommendedAdapter;
	}
	
	// Retourne l'adaptateur des gens à proxmité
	private SimpleAdapter getNearMeAdapter() {
		List<Map<String, String>> nearMeAttributesList = new ArrayList<Map<String, String>>();
		
		Map<String, String> nearMeAttributesMap;
		for (Profile profile : nearMeList) {
			nearMeAttributesMap = new HashMap<String, String>();
			nearMeAttributesMap.put("firstName", profile.getFirstName());
			nearMeAttributesMap.put("lastName", profile.getLastName());
			nearMeAttributesMap.put("picture", String.valueOf(profile.getAvatar()));
			nearMeAttributesList.add(nearMeAttributesMap);
		}
        
        // Création d'un SimpleAdapter qui se chargera de mettre
        // les personnes proches de la liste dans la vue contact_item
        SimpleAdapter nearMeAdapter = new SimpleAdapter (this.getBaseContext(),
        		nearMeAttributesList, R.layout.contact_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
        
		return nearMeAdapter;
	}
	
	// Remplit la liste de favoris
	private void fillFavoritesList() {
		favoritesList.add(new Profile(R.drawable.default_profile_icon, 
								"Arthur", "M."));
		favoritesList.add(new Profile(R.drawable.sponge_bob,
								"Bob", "L'éponge"));
		favoritesList.add(new Profile(R.drawable.default_profile_icon,
								"Patrick", "L'étoile de mer"));
		favoritesList.add(new Profile(R.drawable.default_profile_icon,
								"Timothée", "L."));
	}
	
	// Remplit la liste de recents
	private void fillKnownList() {
		knownList.add(new Profile(R.drawable.default_profile_icon,
								"Julian", "Dos Santos"));
		knownList.add(new Profile(R.drawable.default_profile_icon,
								"Vincent", "B."));
	}
	
	// Remplit la liste des recommandes
	private void fillRecommendedList() {
		recommendedList.add(new Profile(R.drawable.default_profile_icon,
								"Li Chen", "T."));
		recommendedList.add(new Profile(R.drawable.default_profile_icon,
								"Loïc", "T."));
		recommendedList.add(new Profile(R.drawable.default_profile_icon,
								"Raphaël", "Corral"));
	}
}