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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ConversationActivity extends Activity {

    // Page courrante affich�e
    private int currentPage;

	/** Widgets et conteneurs */ 
	
	// Permet de naviguer entre les conversations
	private ViewPager mConversationViewPager;
    // Adaptateur pour les messages
    private ArrayList<MessageAdapter> mConversationMessageAdapters;
    // Adaptateur pour les listes de messages des conversations
    private ConversationPagerAdapter mConversationPagerAdapter;
    // Liste des messages des conversations
    private ArrayList<ListView> mConversationsArrayList;
    // Fl�che gauche
    private ImageView mGoLeftImageView;
    // Fl�che droite
    private ImageView mGoRightImageView;
    // Titre de la conversation
    private TextView mTitleTextView;
    
    /** Mod�les */
    // TODO R�cup�rer le mod�le contenant les conversations
    
    /** Instances pour les tests */
    // Profil de l'utilisateur
    private Profile userProfil;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.conversation);
        
        // Instanciation du conteneur de conversation
        mConversationsArrayList = new ArrayList<ListView>();
        
        // Instanciation du conteneur d'adaptateur et de conteneur de messages
        mConversationMessageAdapters = new ArrayList<MessageAdapter>();
        
        currentPage = 0;
        
        // Initialisation du conteneur et de l'adaptateur de pages
        mConversationViewPager = (ViewPager) findViewById(R.id.message_list);
        mConversationPagerAdapter = new ConversationPagerAdapter(this, mConversationsArrayList);
        mConversationViewPager.setAdapter(mConversationPagerAdapter);
        mConversationViewPager.setOnPageChangeListener(new OnPageChangeListener() {
        	
			public void onPageSelected(int arg0) {
				currentPage = arg0;
				updateConversationBar();
			}
			
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        
        // Initialisation du boutton Envoyer
        Button bSend = (Button) findViewById(R.id.button_send);
        bSend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Envoyer un message � partir du contenu du EditText
                EditText view = (EditText) findViewById(R.id.edit_text_out);
                addMessage(userProfil, view.getText().toString());
                view.setText("");
            }
        });
        
        // Initialisation de la barre de navigation
        mGoLeftImageView = (ImageView) findViewById(R.id.go_left);
        mGoRightImageView = (ImageView) findViewById(R.id.go_right);
        mTitleTextView= (TextView) findViewById(R.id.title);
        mGoLeftImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				mConversationViewPager.setCurrentItem(--currentPage);
			}
		});
        mGoRightImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				mConversationViewPager.setCurrentItem(++currentPage);
			}
		});
        updateConversationBar();
        
        // Test - START  
        
        addConversation();
        addConversation();
        
        // Cr�ation du profil de l'utilisateur
        userProfil = new Profile();
        userProfil.setAvatar(R.drawable.default_profile_icon);
        userProfil.setFirstName("Moi");
        userProfil.setUser(true);
        
        final Profile bob = new Profile();
        bob.setAvatar(R.drawable.sponge_bob);
        bob.setLastName("L'Eponge)");
        bob.setFirstName("Bob");
        bob.setUser(false);
        
        addMessage(bob, "Hello World !");
        
        // Test - END
    }
    
  //M�thode qui se d�clenchera lorsque vous appuierez sur le bouton menu du t�l�phone
    public boolean onCreateOptionsMenu(Menu menu) {
 
        //Cr�ation d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML sp�cifier en un objet Menu
        inflater.inflate(R.menu.conversation, menu);
 
        return true;
     }
 
      //M�thode qui se d�clenchera au clic sur un item
      public boolean onOptionsItemSelected(MenuItem item) {
         //On regarde quel item a �t� cliqu� gr�ce � son id et on d�clenche une action
         switch (item.getItemId()) {
            case R.id.parameters:
            	// Ouvrir la fen�tre des param�tres
               Toast.makeText(ConversationActivity.this, "Param�tres Conversation", Toast.LENGTH_SHORT).show();
               return true;
            case R.id.invite:
            	// Inviter un contact
                Toast.makeText(ConversationActivity.this, "Invitation d'un contact", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.logout:
            	// D�connexion et quitter l'application
               finish();
               return true;
         }
         return false;
     }
      
    private void updateConversationBar() {
    	if (currentPage==0) {
    		mGoLeftImageView.setVisibility(ImageView.INVISIBLE);
    	} else {
    		mGoLeftImageView.setVisibility(ImageView.VISIBLE);
        }
    		
    	if (currentPage==1) { // TODO Remplacer 1 par le nombre de conversation existante
    		mGoRightImageView.setVisibility(ImageView.INVISIBLE);
    	} else {
    		mGoRightImageView.setVisibility(ImageView.VISIBLE);
    	}
    	// TODO Changer le titre
    	mTitleTextView.setText("Conversation "+String.valueOf(currentPage+1));
    }
    
    /** Fonction pour la cr�ation et l'ajout de message */
    private void addMessage(Profile profil, String content) {
    	// TODO : ajouter le message au mod�le de la conversation
        Message monMessage = new Message();
        monMessage.setContact(profil);
        monMessage.setMessage(content);
        mConversationMessageAdapters.get(currentPage).add(monMessage);
    }
    

    
    /** Fonction pour la cr�ation et l'ajout d'une conversation */
    private void addConversation() {
    	// Cr�ation d'une nouvelle page de conversation
    	LayoutInflater lf = getLayoutInflater();
    	ListView newConversationListView= (ListView) lf.inflate(R.layout.message_list, null);
    	mConversationsArrayList.add(newConversationListView);
    	mConversationViewPager.addView(newConversationListView);
        
    	// Instanciation d'un nouveau conteneur et adapteur pour les messages de
    	// la conversation
    	MessageAdapter newConversationMessageAdapter = new MessageAdapter(this, R.layout.message);
        newConversationListView.setAdapter(newConversationMessageAdapter);
        mConversationMessageAdapters.add(newConversationMessageAdapter);
    }
    
}
