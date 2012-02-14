package fr.insa.helloeverybody;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ProfileActivity extends Activity {
	
	private ListView hobbiesListView;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //TextView textview = new TextView(this);
        //textview.setText("This is the Profil tab");
        setContentView(R.layout.profile);
        
        fillProfile();
    }
    
    
 /** Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone */
    public boolean onCreateOptionsMenu(Menu menu) {
 
        //Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML spécifier en un objet Menu
        inflater.inflate(R.menu.profil, menu);
 
        return true;
     }
 
       //Méthode qui se déclenchera au clic sur un item
      public boolean onOptionsItemSelected(MenuItem item) {
         //On regarde quel item a été cliqué grâce à son id et on déclenche une action
         switch (item.getItemId()) {
            case R.id.modify:
            	// Ouvrir la fenêtre des paramètres
               Toast.makeText(ProfileActivity.this, "Modifier", Toast.LENGTH_SHORT).show();
               return true;
            case R.id.logout:
            	// Déconnexion et quitter l'application
               finish();
               return true;
         }
         return false;
     }
     
   // Remplit le profil de l'utilisateur
  	private void fillProfile() {
  			// Récupération de la liste des centre d'intérets
  			hobbiesListView = (ListView) findViewById(R.id.profile_hobby);
  	        String[] hobbies = new String[] {
		            "Foot en salle", "Informatique", "Pêche", "les échecs", "ski nautique"};     
  	        ArrayAdapter<String> hobbyAdapter = new ArrayAdapter<String>(this, R.layout.hobby_item, R.id.hobby, hobbies);
  	        hobbiesListView.setAdapter(hobbyAdapter);
			
		   
  	}
}
