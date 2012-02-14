package fr.insa.helloeverybody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insa.helloeverybody.communication.ServerInteraction;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ContactsActivity extends Activity {
	
	private Context context;
	
	// Profil de l'utilisateur
	private Profil profil;
	
	// Liste des contacts à proximité
	private ArrayList<Profil> contactsList;
	
	// Listes de contacts (ListView)
	/*private ListView favoritesListView;
	private ListView knownListView;
	private ListView recommendedListView;
	private ListView nearMeListView;*/
	private ListView contactsListView;
	private ServerInteraction serverInteraction;
	
	// ProgressDialog pour l'attente de la récupération des contacts
	private ProgressDialog loading;
	
	// Handler qui sera exécuté à la fin de la récupération des contacts
	final Handler uiThreadCallback = new Handler();
	
	//TODO: pour les tests, à virer
	Boolean res;
	
	
    // Appelé a la creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context = this;
        
        profil = new Profil();
        profil.setName("MonNom");
        profil.setPrenom("Prenom");
        
        // Fenêtre de chargement        
        loading = ProgressDialog.show(ContactsActivity.this,
        		"Chargement...", "Récupération des contacts", true);
        
        // Exécuté une fois les contacts récupérés
        final Runnable runInUIThread = new Runnable() {
        	public void run() {
        		setContentView(R.layout.contacts);
        		Toast.makeText(ContactsActivity.this, res.toString(), Toast.LENGTH_SHORT).show();
                fillContacts();
        	}
    	};
    	  
    	// Thread de récupération des contacts
    	new Thread() {
    		@Override
    		public void run() {
    			// Récupération de la liste des contacts à proximité
    			// Get informations from the server
    	        String serverAdr = "http://10.0.2.2/otsims/ws.php";
    	        serverInteraction = new ServerInteraction(context, serverAdr);
    	        res = serverInteraction.register(profil);
    	        contactsList = serverInteraction.getPeopleAround();
    			loading.dismiss();
    			uiThreadCallback.post(runInUIThread);
		    }
		}.start();
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
               Toast.makeText(ContactsActivity.this, "Param?tres Contact", Toast.LENGTH_SHORT).show();
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
	private void fillContacts() {

		SeparatedListAdapter listAdapter = new SeparatedListAdapter(this);
		
		listAdapter.addSection(getString(R.string.favorites), getFavoritesAdapter());
		listAdapter.addSection(getString(R.string.known), getKnownAdapter());
		listAdapter.addSection(getString(R.string.recommended), getRecommendedAdapter());
		listAdapter.addSection(getString(R.string.near_me), getNearMeAdapter());
		
		contactsListView = (ListView) findViewById(R.id.contactsList);
		contactsListView.setAdapter(listAdapter);
		
		contactsListView.setOnItemClickListener(new OnItemClickListener() {
        	@SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				// On récupère la HashMap
        		Map<String, String> map = (Map<String, String>) adapter.getItemAtPosition(position);

        		// On affiche le bouton cliqué
        		if (map != null)
        		{
	        		Toast.makeText(ContactsActivity.this, 
	        				map.get("firstName") + " " + map.get("lastName"), 
	        				Toast.LENGTH_SHORT).show();
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
        favoriteAttributesMap.put("lastName", "L'éponge");
        favoriteAttributesMap.put("picture",
        			String.valueOf(R.drawable.sponge_bob));
        favoritesList.add(favoriteAttributesMap);
 
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("firstName", "Patrick");
        favoriteAttributesMap.put("lastName", "L'étoile de mer");
        favoriteAttributesMap.put("picture",
        			String.valueOf(R.drawable.default_profile_icon));
        favoritesList.add(favoriteAttributesMap);
 
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("firstName", "Timothée");
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
        recommendedAttributesMap.put("firstName", "Loïc");
        recommendedAttributesMap.put("lastName", "T.");
        recommendedAttributesMap.put("picture", 
        			String.valueOf(R.drawable.default_profile_icon));
        recommendedList.add(recommendedAttributesMap);

        recommendedAttributesMap = new HashMap<String, String>();
        recommendedAttributesMap.put("firstName", "Raphaël");
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
	
	
	
	
	
	/*
	// Remplit la liste de favoris
	private void fillFavorites() {
		// Recuperation de la listview creee dans le fichier main.xml
		favoritesListView = (ListView) findViewById(R.id.favoritesList);
 
        // Creation de la ArrayList qui nous permettra de remplir la listView
        ArrayList<HashMap<String, String>> favoritesList = 
        			new ArrayList<HashMap<String, String>>();
 
        // On declare la HashMap qui contiendra les informations pour un item
        HashMap<String, String> favoriteAttributesMap;
        
        // Ajout des favoris
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("firstName", "Arthur");
        favoriteAttributesMap.put("lastName", "M.");
        favoriteAttributesMap.put("picture", 
        			String.valueOf(R.drawable.default_profile_icon));
        favoritesList.add(favoriteAttributesMap);
        
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("firstName", "Bob");
        favoriteAttributesMap.put("lastName", "L'éponge");
        favoriteAttributesMap.put("picture",
        			String.valueOf(R.drawable.sponge_bob));
        favoritesList.add(favoriteAttributesMap);
 
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("firstName", "Patrick");
        favoriteAttributesMap.put("lastName", "L'étoile de mer");
        favoriteAttributesMap.put("picture",
        			String.valueOf(R.drawable.default_profile_icon));
        favoritesList.add(favoriteAttributesMap);
 
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("firstName", "Timothée");
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
        favoritesListView.setAdapter(favoritesAdapter);
 
        // Ecoute des clicks sur les items
        favoritesListView.setOnItemClickListener(new OnItemClickListener() {
        	@SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				// On recupere la HashMap
        		HashMap<String, String> map = (HashMap<String, String>) favoritesListView.getItemAtPosition(position);
        		
        		// On affiche le bouton clique
        		Toast.makeText(ContactsActivity.this, 
        				map.get("firstName") + " " + map.get("lastName"), 
        				Toast.LENGTH_SHORT).show();
        	}
         });
	}
	
	
	
	// Remplit la liste des personnes à qui on a déjà parlé
	private void fillKnown() {
		// Récupération de la listview créée dans le fichier main.xml
		knownListView = (ListView) findViewById(R.id.knownList);
 
        // Création de la ArrayList qui nous permettra de remplir la listView
        ArrayList<HashMap<String, String>> knownList = 
        			new ArrayList<HashMap<String, String>>();
 
        // On déclare la HashMap qui contiendra les informations pour un item
        HashMap<String, String> knownAttributesMap;
        
        // Ajout des connus
        knownAttributesMap = new HashMap<String, String>();
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
        // les connus de la liste dans la vue contact_item
        SimpleAdapter knownAdapter = new SimpleAdapter (this.getBaseContext(),
        		knownList, R.layout.contact_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
        knownListView.setAdapter(knownAdapter);
 
        // Ecoute des clicks sur les items
        knownListView.setOnItemClickListener(new OnItemClickListener() {
        	@SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				// On récupère la HashMap
        		HashMap<String, String> map = (HashMap<String, String>) knownListView.getItemAtPosition(position);
        		
        		// On affiche le bouton cliqué
        		Toast.makeText(ContactsActivity.this, 
        				map.get("firstName") + " " + map.get("lastName"), 
        				Toast.LENGTH_SHORT).show();
        	}
         });
	}
	
	
	
	// Remplit la liste de recommandés
	private void fillRecommended() {
		// Récupération de la listview créée dans le fichier main.xml
		recommendedListView = (ListView) findViewById(R.id.recommendedList);
 
        // Création de la ArrayList qui nous permettra de remplir la listView
        ArrayList<HashMap<String, String>> recommendedList = 
        			new ArrayList<HashMap<String, String>>();
 
        // On déclare la HashMap qui contiendra les informations pour un item
        HashMap<String, String> recommendedAttributesMap;
        
        // Ajout des recommandés
        recommendedAttributesMap = new HashMap<String, String>();
        recommendedAttributesMap.put("firstName", "Li Chen");
        recommendedAttributesMap.put("lastName", "T.");
        recommendedAttributesMap.put("picture", 
        			String.valueOf(R.drawable.default_profile_icon));
        recommendedList.add(recommendedAttributesMap);
 
        recommendedAttributesMap = new HashMap<String, String>();
        recommendedAttributesMap.put("firstName", "Loïc");
        recommendedAttributesMap.put("lastName", "T.");
        recommendedAttributesMap.put("picture", 
        			String.valueOf(R.drawable.default_profile_icon));
        recommendedList.add(recommendedAttributesMap);

        recommendedAttributesMap = new HashMap<String, String>();
        recommendedAttributesMap.put("firstName", "Raphaël");
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
        recommendedListView.setAdapter(recommendedAdapter);
 
        // Ecoute des clicks sur les items
        recommendedListView.setOnItemClickListener(new OnItemClickListener() {
        	@SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				// On récupère la HashMap
        		HashMap<String, String> map = (HashMap<String, String>) recommendedListView.getItemAtPosition(position);
        		
        		// On affiche le bouton cliqué
        		Toast.makeText(ContactsActivity.this, 
        				map.get("firstName") + " " + map.get("lastName"), 
        				Toast.LENGTH_SHORT).show();
        	}
         });
	}
	
	
	
	// Remplit la liste des personnes proches
	private void fillNearMe() {
		// Récupération de la listview créée dans le fichier main.xml
		nearMeListView = (ListView) findViewById(R.id.nearMeList);
 
        // Création de la ArrayList qui nous permettra de remplir la listView
        ArrayList<HashMap<String, String>> nearMeList = 
        			new ArrayList<HashMap<String, String>>();
 
        // On déclare la HashMap qui contiendra les informations pour un item
        HashMap<String, String> nearMeAttributesMap;
        
        // Ajout des personnes proches
        nearMeAttributesMap = new HashMap<String, String>();
        nearMeAttributesMap.put("firstName", "Flora");
        nearMeAttributesMap.put("lastName", "B.");
        nearMeAttributesMap.put("picture",
        			String.valueOf(R.drawable.default_profile_icon));
        nearMeList.add(nearMeAttributesMap);
        
        nearMeAttributesMap = new HashMap<String, String>();
        nearMeAttributesMap.put("firstName", "Mathilde");
        nearMeAttributesMap.put("lastName", "A.");
        nearMeAttributesMap.put("picture",
        			String.valueOf(R.drawable.default_profile_icon));
        nearMeList.add(nearMeAttributesMap);

        nearMeAttributesMap = new HashMap<String, String>();
        nearMeAttributesMap.put("firstName", "Pikachu");
        nearMeAttributesMap.put("lastName", "Le pokémon");
        nearMeAttributesMap.put("picture", 
        			String.valueOf(R.drawable.default_profile_icon));
        nearMeList.add(nearMeAttributesMap);
 
        // Création d'un SimpleAdapter qui se chargera de mettre
        // les personnes proches de la liste dans la vue contact_item
        SimpleAdapter nearMeAdapter = new SimpleAdapter (this.getBaseContext(),
        		nearMeList, R.layout.contact_item,
        		new String[] {"picture", "firstName", "lastName"}, 
        		new int[] {R.id.picture, R.id.firstName, R.id.lastName});
        nearMeListView.setAdapter(nearMeAdapter);
 
        // Ecoute des clicks sur les items
        nearMeListView.setOnItemClickListener(new OnItemClickListener() {
        	@SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				// On récupère la HashMap
        		HashMap<String, String> map = (HashMap<String, String>) nearMeListView.getItemAtPosition(position);
        		
        		// On affiche le bouton cliqué
        		Toast.makeText(ContactsActivity.this, 
        				map.get("firstName") + " " + map.get("lastName"), 
        				Toast.LENGTH_SHORT).show();
        	}
         });
	}
		
	
	
	// Créer les têtes de listes
	private List<Map<String, String>> createGroupList() {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		
		// Ajouts des headers
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("listName", "Favoris");
		result.add(headerMap);
		
		headerMap = new HashMap<String, String>();
		headerMap.put("listName", "Récents");
		result.add(headerMap);
		
		headerMap = new HashMap<String, String>();
		headerMap.put("listName", "Recommandés");
		result.add(headerMap);
		
		headerMap = new HashMap<String, String>();
		headerMap.put("listName", "A proximité");
		result.add(headerMap);
		
		return result;
	}
	
	// Créer les listes
	private List<List<Map<String, String>>> createChildList() {
		List<List<Map<String, String>>> result = new ArrayList<List<Map<String, String>>>();
		
		result.add(getFavoritesList());
		result.add(getKnownList());
		result.add(getRecommendedList());
		result.add(getNearMeList());
		
		return result;
	}*/
}