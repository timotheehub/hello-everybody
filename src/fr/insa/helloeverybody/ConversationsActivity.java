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
    
 //M�thode qui se d�clenchera lorsque vous appuierez sur le bouton menu du t�l�phone
    public boolean onCreateOptionsMenu(Menu menu) {
 
        //Cr�ation d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML sp�cifier en un objet Menu
        inflater.inflate(R.menu.conversations, menu);
 
        return true;
     }
 
       //M�thode qui se d�clenchera au clic sur un item
      public boolean onOptionsItemSelected(MenuItem item) {
         //On regarde quel item a �t� cliqu� gr�ce � son id et on d�clenche une action
         switch (item.getItemId()) {
            case R.id.parametres:
            	// Ouvrir la fen�tre des param�tres
               Toast.makeText(ConversationsActivity.this, "Param�tres Conversations", Toast.LENGTH_SHORT).show();
               return true;
            case R.id.groupe_publique:
            	// Cr�er un groupe publique
                Toast.makeText(ConversationsActivity.this, "Cr�ation d'un groupe publique", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.deconnexion:
            	// D�connexion et quitter l'application
               finish();
               return true;
         }
         return false;}
}
