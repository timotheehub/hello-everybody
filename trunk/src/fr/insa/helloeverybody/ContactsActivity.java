package fr.insa.helloeverybody;

import java.util.ArrayList;
import java.util.HashMap;

import fr.insa.helloeverybody.communication.ServerInteraction;

import android.app.Activity;
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
	private ServerInteraction serverInteraction;
	
    // Appel� a la creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TextView textview = new TextView(this);
        //textview.setText("This is the Contacts tab");
        setContentView(R.layout.contacts);
        
        //Get informations from the server
        String serverAdr = "http://10.0.2.2/otsims/ws.php";
        Profil profil = new Profil();
        profil.setName("MonNom");
        profil.setPrenom("Prenom");
        
        serverInteraction = new ServerInteraction(this, serverAdr);
        Boolean res = serverInteraction.register(profil);
        
        Toast.makeText(ContactsActivity.this, res.toString(), Toast.LENGTH_SHORT).show();
        
        fillFavorites();
    }
    
    // M�thode qui se d�clenchera lorsque vous appuierez sur le bouton menu du t�l�phone
    public boolean onCreateOptionsMenu(Menu menu) {
 
        // Cr�ation d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        
        // Instanciation du menu XML sp�cifier en un objet Menu
        inflater.inflate(R.menu.contacts, menu);
 
        return true;
    }
 
	// M�thode qui se d�clenchera au clic sur un item
	public boolean onOptionsItemSelected(MenuItem item) {
         // On regarde quel item a �t� cliqu� gr�ce � son id et on d�clenche une action
         switch (item.getItemId()) {
            case R.id.parameters:
            	// Ouvrir la fen�tre des param�tres
               Toast.makeText(ContactsActivity.this, "Param�tres Contact", Toast.LENGTH_SHORT).show();
               return true;
            case R.id.search:
            	// Ouvrir la fen�tre de recherche
                Toast.makeText(ContactsActivity.this, "Recherche", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.logout:
            	// D�connexion et quitter l'application
               finish();
               return true;
         }
         return false;
	}
	
	// Remplit la liste de favoris
	private void fillFavorites() {
		// R�cup�ration de la listview cr��e dans le fichier main.xml
		favoritesListView = (ListView) findViewById(R.id.favorite_list);
 
        // Cr�ation de la ArrayList qui nous permettra de remplir la listView
        ArrayList<HashMap<String, String>> favoritesList = 
        			new ArrayList<HashMap<String, String>>();
 
        // On d�clare la HashMap qui contiendra les informations pour un item
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
        favoriteAttributesMap.put("contactNom", "L'�ponge");
        favoriteAttributesMap.put("contactImage",
        			String.valueOf(R.drawable.sponge_bob));
        favoritesList.add(favoriteAttributesMap);
 
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("contactPrenom", "Patrick");
        favoriteAttributesMap.put("contactNom", "L'�toile de mer");
        favoriteAttributesMap.put("contactImage",
        			String.valueOf(R.drawable.default_profile_icon));
        favoritesList.add(favoriteAttributesMap);
 
        favoriteAttributesMap = new HashMap<String, String>();
        favoriteAttributesMap.put("contactPrenom", "Timoth�e");
        favoriteAttributesMap.put("contactNom", "L.");
        favoriteAttributesMap.put("contactImage", 
        			String.valueOf(R.drawable.default_profile_icon));
        favoritesList.add(favoriteAttributesMap);
 
        // Cr�ation d'un SimpleAdapter qui se chargera de mettre
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
				// On r�cup�re la HashMap
        		HashMap<String, String> map = (HashMap<String, String>) favoritesListView.getItemAtPosition(position);
        		
        		// On affiche le bouton cliqu�
        		Toast.makeText(ContactsActivity.this, 
        				map.get("contactPrenom") + " " + map.get("contactNom"), 
        				Toast.LENGTH_SHORT).show();
        	}
         });
	}
}