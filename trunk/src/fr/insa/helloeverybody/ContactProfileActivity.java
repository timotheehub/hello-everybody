package fr.insa.helloeverybody;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class ContactProfileActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.contact_profile);
	    
	    fillProfile();
	}
	
	
	// M�thode qui se d�clenchera lorsque vous appuierez sur le bouton menu du t�l�phone
	public boolean onCreateOptionsMenu(Menu menu) {
	 
		//Cr�ation d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
		MenuInflater inflater = getMenuInflater();
		//Instanciation du menu XML sp�cifier en un objet Menu
	    inflater.inflate(R.menu.profil, menu);
	 
	    return true;
	}
	 
	// M�thode qui se d�clenchera au clic sur un item
	public boolean onOptionsItemSelected(MenuItem item) {
	    // On regarde quel item a �t� cliqu� gr�ce � son id et on d�clenche une action
		switch (item.getItemId()) {
			case R.id.modify:
				// Ouvrir la fen�tre des param�tres
				Toast.makeText(ContactProfileActivity.this, "Modifier", Toast.LENGTH_SHORT).show();
				return true;
	   
	    	case R.id.logout:
	    		// D�connexion et quitter l'application
	    		finish();
	    		return true;
	     }
	     return false;
	}
	 
	// Remplit le profil de l'utilisateur
	private void fillProfile() {
	}
}