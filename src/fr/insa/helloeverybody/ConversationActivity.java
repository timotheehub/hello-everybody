package fr.insa.helloeverybody;


import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ConversationActivity extends Activity {

    // Page courrante affichée
    private int currentPage;
	
	// Permet de naviguer entre les conversations
	private ViewPager mConversationViewPager;

    // Array adapter for the conversation thread
    private ArrayList<MessageAdapter> mConversationMessageAdapters;
    
    // Adaptateur pour les listes de messages des conversations
    private ConversationPagerAdapter mConversationPagerAdapter;
    
    // Liste des messages des conversations
    private ArrayList<View> mConversationsArrayList;
    
    // Profil de l'utilisateur
    private Profil userProfil;
    
    /** Appelée lors du démarrage d'une nouvelle conversation */
    private void addConversation() {
    	LayoutInflater lf = getLayoutInflater();
    	ListView newConversationListView= (ListView) lf.inflate(R.layout.message_list, null);
    	mConversationsArrayList.add(newConversationListView);
    	mConversationViewPager.addView(newConversationListView);
        
     // Initialize the array adapter for the conversation thread
    	MessageAdapter newConversationMessageAdapter = new MessageAdapter(this, R.layout.message);
        newConversationListView.setAdapter(newConversationMessageAdapter);
        mConversationMessageAdapters.add(newConversationMessageAdapter);
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.conversation);
        
        mConversationsArrayList = new ArrayList<View>();
        mConversationMessageAdapters = new ArrayList<MessageAdapter>();
        currentPage = 0;
        
        mConversationViewPager = (ViewPager) findViewById(R.id.message_list);
        mConversationPagerAdapter = new ConversationPagerAdapter(this, mConversationsArrayList);
        mConversationViewPager.setAdapter(mConversationPagerAdapter);
        mConversationViewPager.setOnPageChangeListener(new OnPageChangeListener() {
        	
			public void onPageSelected(int arg0) {
				currentPage = arg0;
			}
			
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        
        addConversation();
        addConversation();
        
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
        
        addMessage(bob, "Hello World !");
        
     // Test - END
        
        Button bSend = (Button) findViewById(R.id.button_send);
        bSend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                EditText view = (EditText) findViewById(R.id.edit_text_out);
                addMessage(userProfil, view.getText().toString());
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
    public void addMessage(Profil profil, String content) {
        Message monMessage = new Message();
        monMessage.setContact(profil);
        monMessage.setMessage(content);
        mConversationMessageAdapters.get(currentPage).add(monMessage);
    }
    
}
