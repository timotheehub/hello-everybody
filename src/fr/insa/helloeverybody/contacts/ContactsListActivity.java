package fr.insa.helloeverybody.contacts;

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
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.helpers.SeparatedListAdapter;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.Profile;

public class ContactsListActivity extends Activity implements ContactsCallbackInterface {
	private ContactsActions contactsActions;
	private Profile profile;
	private ProgressDialog loading;
	
	// Listes de contacts
	private ListView contactsListView;
	
    // Appel a la creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
        
        //Création du gestionnaire des actions
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
    
    // Mettre a jour la liste de contacts
	public void contactsListUpdated(ArrayList<Profile> profilesList) {
		loading.dismiss();
        ContactsList contactsList = ContactsList.getInstance();
		for (Profile profile : profilesList) {
			contactsList.addProfile(profile);
		}
		setContentView(R.layout.contacts_list);
		
		// Ajoute des faux contacts
		fillFakeList();
		
		// Affiche la liste des contacts
		updateContactsView();
		
		// Ajoute un click aux items
		final Intent intent = new Intent().setClass(this, ContactProfileActivity.class);
		contactsListView = (ListView) findViewById(R.id.contacts_list);
		
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

		final SeparatedListAdapter listAdapter = new SeparatedListAdapter(this);
        ContactsList contactsList = ContactsList.getInstance();
		
        // Ajoute les contacts aux adaptateurs
		listAdapter.addSection(getString(R.string.favorites),
				getFavoritesAdapter(), getProfileIds(contactsList.getFavoritesList()));
		listAdapter.addSection(getString(R.string.known),
				getKnownAdapter(), getProfileIds(contactsList.getKnownList()));
		listAdapter.addSection(getString(R.string.recommended),
				getRecommendedAdapter(), getProfileIds(contactsList.getRecommendedList()));
		listAdapter.addSection(getString(R.string.near_me),
				getNearMeAdapter(), getProfileIds(contactsList.getNearMeList()));
		
		// Mettre a jour la ListView
		contactsListView = (ListView) findViewById(R.id.contacts_list);
		contactsListView.setAdapter(listAdapter);
	}
	
	// Retourne la liste des identifiants
	private List<Long> getProfileIds(List<Profile> profilesList) {
		List<Long> profileIds = new ArrayList<Long>();
		
		for (Profile profile : profilesList) {
			profileIds.add(profile.getId());
		}
		
		return profileIds;
	}
	
	// Retourne l'adaptateur des favoris
	private SimpleAdapter getFavoritesAdapter() {
		List<Map<String, String>> favoritesAttributesList = new ArrayList<Map<String, String>>();
        List<Profile> favoritesList = ContactsList.getInstance().getFavoritesList();
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
		List<Profile> knownList = ContactsList.getInstance().getKnownList();
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
		List<Profile> recommendedList = ContactsList.getInstance().getRecommendedList();
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
		List<Profile> nearMeList = ContactsList.getInstance().getNearMeList();
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