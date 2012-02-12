package fr.insa.helloeverybody;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class ConversationsActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This is the Conversations tab");
        setContentView(textview);
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
