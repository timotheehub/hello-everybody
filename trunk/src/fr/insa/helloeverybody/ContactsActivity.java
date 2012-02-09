package fr.insa.helloeverybody;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
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

public class ContactsActivity extends Activity {
	
	private ListView favoritesListView;
	
    // Appelé a la creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TextView textview = new TextView(this);
        //textview.setText("This is the Contacts tab");
        setContentView(R.layout.contacts);
        
        fillFavorites();
    }
    
    // Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone
    public boolean onCreateOptionsMenu(Menu menu) {
 
        // Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        
        // Instanciation du menu XML spécifier en un objet Menu
        inflater.inflate(R.menu.contacts, menu);
 
        return true;
    }
 
	// Méthode qui se déclenchera au clic sur un item
	public boolean onOptionsItemSelected(MenuItem item) {
         // On regarde quel item a été cliqué grâce à son id et on déclenche une action
         switch (item.getItemId()) {
            case R.id.parametres:
            	// Ouvrir la fenêtre des paramètres
               Toast.makeText(ContactsActivity.this, "Paramètres", Toast.LENGTH_SHORT).show();
               return true;
            case R.id.recherche:
            	// Ouvrir la fenêtre de recherche
                Toast.makeText(ContactsActivity.this, "Recherche", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.deconnexion:
            	// Déconnexion et quitter l'application
               finish();
               return true;
         }
         return false;
	}
	
	// Remplit la liste de favoris
	private void fillFavorites() {
		// Récupération de la listview créée dans le fichier main.xml
		favoritesListView = (ListView) findViewById(R.id.liste_favoris);
 
        // Création de la ArrayList qui nous permettra de remplir la listView
        ArrayList<HashMap<String, String>> favoritesList = 
        			new ArrayList<HashMap<String, String>>();
 
        // On déclare la HashMap qui contiendra les informations pour un item
        HashMap<String, String> favoriteAttributesMap;
        
        // Ajout des favoris
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("contactPrenom", "Arthur");
        favoriteAttributesMap.put("contactNom", "M.");
        favoriteAttributesMap.put("contactImage", 
        			String.valueOf(R.drawable.default_profile_icon));
        favoritesList.add(favoriteAttributesMap);
        
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("contactPrenom", "Bob");
        favoriteAttributesMap.put("contactNom", "L'éponge");
        favoriteAttributesMap.put("contactImage",
        			String.valueOf(R.drawable.sponge_bob));
        favoritesList.add(favoriteAttributesMap);
 
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("contactPrenom", "Patrick");
        favoriteAttributesMap.put("contactNom", "L'étoile de mer");
        favoriteAttributesMap.put("contactImage",
        			String.valueOf(R.drawable.default_profile_icon));
        favoritesList.add(favoriteAttributesMap);
 
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("contactPrenom", "Timothée");
        favoriteAttributesMap.put("contactNom", "L.");
        favoriteAttributesMap.put("contactImage", 
        			String.valueOf(R.drawable.default_profile_icon));
        favoritesList.add(favoriteAttributesMap);
 
        // Création d'un SimpleAdapter qui se chargera de mettre
        // les favoris de la liste dans la vue contact_item
        SimpleAdapter favoritesAdapter = new SimpleAdapter (this.getBaseContext(),
        		favoritesList, R.layout.contact_item,
        		new String[] {"contactImage", "contactPrenom", "contactNom"}, 
        		new int[] {R.id.contactImage, R.id.contactPrenom, R.id.contactNom});
        favoritesListView.setAdapter(favoritesAdapter);
 
        // Ecoute des clicks sur les items
        favoritesListView.setOnItemClickListener(new OnItemClickListener() {
        	@SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				// On récupère la HashMap
        		HashMap<String, String> map = (HashMap<String, String>) favoritesListView.getItemAtPosition(position);
        		
        		// On affiche le bouton cliqué
        		Toast.makeText(ContactsActivity.this, 
        				map.get("contactPrenom") + " " + map.get("contactNom"), 
        				Toast.LENGTH_SHORT).show();
        	}
         });
	}
}