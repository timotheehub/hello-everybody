package fr.insa.helloeverybody;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ConversationsActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


		final Intent intent;  // Reusable Intent for each tab
		
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, ConversationActivity.class);
        
        Button button = new Button(this);
        button.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		startActivity(intent);
        	}
        });
        button.setText("Test");
        setContentView(button);
    }
    
 //Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone
    public boolean onCreateOptionsMenu(Menu menu) {
 
        //Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML spécifier en un objet Menu
        inflater.inflate(R.menu.conversations, menu);
 
        return true;
     }
 
       //Méthode qui se déclenchera au clic sur un item
      public boolean onOptionsItemSelected(MenuItem item) {
         //On regarde quel item a été cliqué grâce à son id et on déclenche une action
         switch (item.getItemId()) {
            case R.id.parametres:
            	// Ouvrir la fenêtre des paramètres
               Toast.makeText(ConversationsActivity.this, "Paramètres Conversations", Toast.LENGTH_SHORT).show();
               return true;
            case R.id.groupe_publique:
            	// Créer un groupe publique
                Toast.makeText(ConversationsActivity.this, "Création d'un groupe publique", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.deconnexion:
            	// Déconnexion et quitter l'application
               finish();
               return true;
         }
         return false;}
}
