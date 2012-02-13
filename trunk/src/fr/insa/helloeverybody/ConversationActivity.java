package fr.insa.helloeverybody;


import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ConversationActivity extends Activity {
	
	// Permet de naviguer entre les conversations
	private ViewFlipper mConversationViewFlipper;

    // Layout Views
    private ListView mConversationView;

    // Array adapter for the conversation thread
    private MessageAdapter mConversationMessageAdapter;
	
    // Liste des messages d'une conversation
    private ArrayList<Message> mConversationArrayList;
    
    // Profil de l'utilisateur
    private Profil userProfil;
    
    /** Appelée lors du démarrage d'une nouvelle conversation */
    private void addConversation() {
    	
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mConversationViewFlipper = (ViewFlipper) findViewById(R.id.conversation);
        setContentView(mConversationViewFlipper);
        
        mConversationArrayList = new ArrayList<Message>();
        
    // Création du profil de l'utilisateur
        userProfil = new Profil();
        userProfil.setAvatar(R.drawable.default_profile_icon);
        userProfil.setPrenom("Moi");
        userProfil.setUser(true);
        
    // Test - START  
        
        final Profil bob = new Profil();
        bob.setAvatar(R.drawable.sponge_bob);
        bob.setName("L'Eponge)");
        bob.setPrenom("Bob");
        bob.setUser(false);
        
        Message essai = new Message();
        essai.setContact(bob);
        essai.setMessage("Hello World !");
        
        mConversationArrayList.add(essai);
        
     // Test - END
        
     // Initialize the array adapter for the conversation thread
        mConversationMessageAdapter = new MessageAdapter(this, R.layout.message, mConversationArrayList);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationMessageAdapter);
        
        Button bSend = (Button) findViewById(R.id.button_send);
        bSend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                EditText view = (EditText) findViewById(R.id.edit_text_out);
                sendMessage(view.getText().toString());
                view.setText("");
            }
        });
    }
    
  //Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone
    public boolean onCreateOptionsMenu(Menu menu) {
 
        //Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML spécifier en un objet Menu
        inflater.inflate(R.menu.conversation, menu);
 
        return true;
     }
 
       //Méthode qui se déclenchera au clic sur un item
      public boolean onOptionsItemSelected(MenuItem item) {
         //On regarde quel item a été cliqué grâce à son id et on déclenche une action
         switch (item.getItemId()) {
            case R.id.parameters:
            	// Ouvrir la fenêtre des paramètres
               Toast.makeText(ConversationActivity.this, "Paramètres Conversation", Toast.LENGTH_SHORT).show();
               return true;
            case R.id.invite:
            	// Inviter un contact
                Toast.makeText(ConversationActivity.this, "Invitation d'un contact", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.logout:
            	// Déconnexion et quitter l'application
               finish();
               return true;
         }
         return false;
     }
    
    /** Fonction pour tester l'ajout de message */
    public void sendMessage(String content) {
        Message monMessage = new Message();
        monMessage.setContact(userProfil);
        monMessage.setMessage(content);
        mConversationMessageAdapter.add(monMessage);
    }
    
}
