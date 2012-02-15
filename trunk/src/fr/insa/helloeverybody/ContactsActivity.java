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
	private ArrayList<Profile> contactsList;
	
	// Listes de contacts (ListView)
	private ListView contactsListView;
	private ProgressDialog loading;
	
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
    
	public void contactsListUpdated(ArrayList<Profile> contactsList) {
		loading.dismiss();
		this.contactsList = contactsList;
		setContentView(R.layout.contacts_list);
		fillContacts();
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
            	// Dï¿½connexion et quitter l'application
               finish();
               return true;
         }
         return false;
	}
	
	// Remplit les différentes listes de contacts
	private void fillContacts() {

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
		List<Map<String, String>> favoritesList = new ArrayList<Map<String, String>>();
				
		Map<String, String> favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("firstName", "Arthur");
        favoriteAttributesMap.put("lastName", "M.");
        favoriteAttributesMap.put("picture", 
        			String.valueOf(R.drawable.default_profile_icon));
        favoritesList.add(favoriteAttributesMap);
        
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("firstName", "Bob");
        favoriteAttributesMap.put("lastName", "L'ï¿½ponge");
        favoriteAttributesMap.put("picture",
        			String.valueOf(R.drawable.sponge_bob));
        favoritesList.add(favoriteAttributesMap);
 
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("firstName", "Patrick");
        favoriteAttributesMap.put("lastName", "L'ï¿½toile de mer");
        favoriteAttributesMap.put("picture",
        			String.valueOf(R.drawable.default_profile_icon));
        favoritesList.add(favoriteAttributesMap);
 
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("firstName", "Timothï¿½e");
        favoriteAttributesMap.put("lastName", "L.");
        favoriteAttributesMap.put("picture", 
        			String.valueOf(R.drawable.default_profile_icon));
        favoritesList.add(favoriteAttributesMap);
        
        // Creation d'un SimpleAdapter qui se chargera de mettre
        // les favoris de la liste dans la vue contact_item
        SimpleAdapter favoritesAdapter = new SimpleAdapter (this.getBaseContext(),
        		favoritesList, R.layout.contact_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
        
        return favoritesAdapter;
	}
	
	// Retourne l'adaptateur des récents
	private SimpleAdapter getKnownAdapter() {
		List<Map<String, String>> knownList = new ArrayList<Map<String, String>>();
		
		Map<String, String> knownAttributesMap = new HashMap<String, String>();
        knownAttributesMap.put("firstName", "Julian");
        knownAttributesMap.put("lastName", "Dos Santos");
        knownAttributesMap.put("picture", 
        			String.valueOf(R.drawable.default_profile_icon));
        knownList.add(knownAttributesMap);
        
        knownAttributesMap = new HashMap<String, String>();
        knownAttributesMap.put("firstName", "Vincent");
        knownAttributesMap.put("lastName", "B.");
        knownAttributesMap.put("picture", 
        			String.valueOf(R.drawable.default_profile_icon));
        knownList.add(knownAttributesMap);
        
        // Création d'un SimpleAdapter qui se chargera de mettre
        // les récents de la liste dans la vue contact_item
        SimpleAdapter knownAdapter = new SimpleAdapter (this.getBaseContext(),
        		knownList, R.layout.contact_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
        
        return knownAdapter;
	}
	
	// Retourne l'adaptateur des recommandés
	private SimpleAdapter getRecommendedAdapter() {
		List<Map<String, String>> recommendedList = new ArrayList<Map<String, String>>();
		
		Map<String, String> recommendedAttributesMap = new HashMap<String, String>();
        recommendedAttributesMap.put("firstName", "Li Chen");
        recommendedAttributesMap.put("lastName", "T.");
        recommendedAttributesMap.put("picture", 
        			String.valueOf(R.drawable.default_profile_icon));
        recommendedList.add(recommendedAttributesMap);
 
        recommendedAttributesMap = new HashMap<String, String>();
        recommendedAttributesMap.put("firstName", "Loï¿½c");
        recommendedAttributesMap.put("lastName", "T.");
        recommendedAttributesMap.put("picture", 
        			String.valueOf(R.drawable.default_profile_icon));
        recommendedList.add(recommendedAttributesMap);

        recommendedAttributesMap = new HashMap<String, String>();
        recommendedAttributesMap.put("firstName", "Raphaï¿½l");
        recommendedAttributesMap.put("lastName", "Corral");
        recommendedAttributesMap.put("picture", 
        			String.valueOf(R.drawable.default_profile_icon));
        recommendedList.add(recommendedAttributesMap);
        
        // Création d'un SimpleAdapter qui se chargera de mettre
        // les recommandés de la liste dans la vue contact_item
        SimpleAdapter recommendedAdapter = new SimpleAdapter (this.getBaseContext(),
        		recommendedList, R.layout.contact_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
        
        return recommendedAdapter;
	}
	
	// Retourne l'adaptateur des gens à proxmité
	private SimpleAdapter getNearMeAdapter() {
		List<Map<String, String>> nearMeList = new ArrayList<Map<String, String>>();
		
		Map<String, String> nearMeAttributesMap = new HashMap<String, String>();
        nearMeAttributesMap.put("firstName", "Flora");
        nearMeAttributesMap.put("lastName", "Z.");
        nearMeAttributesMap.put("picture",
        			String.valueOf(R.drawable.default_profile_icon));
        nearMeList.add(nearMeAttributesMap);
        
        nearMeAttributesMap = new HashMap<String, String>();
        nearMeAttributesMap.put("firstName", "Jilinna");
        nearMeAttributesMap.put("lastName", "P.");
        nearMeAttributesMap.put("picture", 
        			String.valueOf(R.drawable.default_profile_icon));
        nearMeList.add(nearMeAttributesMap);
        
        nearMeAttributesMap = new HashMap<String, String>();
        nearMeAttributesMap.put("firstName", "Mathilde");
        nearMeAttributesMap.put("lastName", "S.");
        nearMeAttributesMap.put("picture",
        			String.valueOf(R.drawable.default_profile_icon));
        nearMeList.add(nearMeAttributesMap);
        
        // Création d'un SimpleAdapter qui se chargera de mettre
        // les personnes proches de la liste dans la vue contact_item
        SimpleAdapter nearMeAdapter = new SimpleAdapter (this.getBaseContext(),
        		nearMeList, R.layout.contact_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
        
		return nearMeAdapter;
	}
}