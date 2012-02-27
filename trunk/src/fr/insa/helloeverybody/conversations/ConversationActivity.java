package fr.insa.helloeverybody.conversations;


import java.util.ArrayList;
import java.util.List;

import fr.insa.helloeverybody.HelloEverybodyActivity;
import fr.insa.helloeverybody.contacts.InviteContactActivity;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.communication.ChatService;
import fr.insa.helloeverybody.helpers.ConversationPagerAdapter;
import fr.insa.helloeverybody.helpers.ConversationsListener;
import fr.insa.helloeverybody.helpers.MessageAdapter;
import fr.insa.helloeverybody.models.Conversation;
import fr.insa.helloeverybody.models.ConversationMessage;
import fr.insa.helloeverybody.models.ConversationsList;
import fr.insa.helloeverybody.models.Profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class ConversationActivity extends Activity implements ConversationsListener {
    // TODO : Ajouter un moyen de fermer une conversation

    // Page courrante affichée
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
    // Flèche gauche
    private ImageView mGoLeftImageView;
    // Flèche droite
    private ImageView mGoRightImageView;
    // Titre de la conversation
    private TextView mTitleTextView;
    
    /** Modèles */
    private List<Conversation> pendingConversations;
    
    /** Instances pour les tests */
    // Profil de l'utilisateur
    private Profile userProfil;
    private Profile bob;

    // test
    private final Handler mHandler=new Handler(){
    	@Override
    	public void handleMessage(Message msg){
    		ConversationMessage message = new ConversationMessage();
    		switch(msg.what){
    		case 1:
    	        message.setContact(bob);
    	        message.setMessage(msg.obj.toString());
    	        addMessage(currentPage,message);
    			break;
    		case 2:
    	        message.setContact(userProfil);
    	        message.setMessage(msg.obj.toString());
    	        addMessage(currentPage,message);
    			break;
    		default:
    			break;
    		}
    		
    	}
    };
    
    private ChatService mChatService=null;
    //
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.conversation);
        
        //Récupération de la liste des conversations en cours
        pendingConversations = ConversationsList.getInstance().getOpenList();
        
        ConversationsList.getInstance().addConversationsListener(this);
        
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
			}
			
			public void onPageScrollStateChanged(int arg0) {
			}
		});
        
        // Initialisation du boutton Envoyer
        Button bSend = (Button) findViewById(R.id.button_send);
        bSend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Envoyer un message à partir du contenu du EditText
                EditText view = (EditText) findViewById(R.id.edit_text_out);
                String destID=pendingConversations.get(currentPage).getDestID();
                mChatService.write(destID,view.getText().toString());
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
        
        // Test - START  
        
        // Création du profil de l'utilisateur
        userProfil = new Profile();
        userProfil.setAvatar(R.drawable.default_profile_icon);
        userProfil.setFirstName("Moi");
        userProfil.setUser(true);
        
        bob = new Profile();
        bob.setAvatar(R.drawable.sponge_bob);
        bob.setLastName("L'Eponge)");
        bob.setFirstName("Bob");
        bob.setUser(false);
        
        ConversationMessage message1 = new ConversationMessage();
        message1.setContact(bob);
        message1.setMessage("Hello World !");

        Conversation conversation1 = new Conversation();
        conversation1.addMember(userProfil);
        conversation1.addMember(bob);
        conversation1.addMessage(message1);
        conversation1.setTitle("Bob et Moi");
        Conversation conversation2 = new Conversation();
        conversation2.setTitle("Roger et Moi");
        Conversation conversation3 = new Conversation();
        conversation3.setTitle("Jean-Louis et Moi");
        pendingConversations.add(conversation1);
        pendingConversations.add(conversation2);
        pendingConversations.add(conversation3);
        
        //mChatService=new ChatService(mHandler,"talk.google.com",5222,"gmail.com");
        //mChatService.doLogin("hello.everybody.app@gmail.com","insalyonSIMP");
        mChatService=new ChatService(mHandler,"im.darkserver.eu.org",5222,null);
        mChatService.doLogin("test", "test");
        // Test - END
        
        // Initialisation des conversations
        for (int i=0; i < pendingConversations.size() ; i++) {
        	addConversationPage();
        	for (ConversationMessage message : pendingConversations.get(i).getMessages()) {
        		addMessage(i,message);
        	}
        }
        updateConversationBar();
    }
    
  /** Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone */
    public boolean onCreateOptionsMenu(Menu menu) {
 
        //Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML spécifier en un objet Menu
        inflater.inflate(R.menu.conversation, menu);
 
        return true;
     }
 
      /** Méthode qui se déclenchera au clic sur un item */
      public boolean onOptionsItemSelected(MenuItem item) {
         //On regarde quel item a été cliqué grâce à son id et on déclenche une action
         switch (item.getItemId()) {
            case R.id.parameters:
            	// Ouvrir la fenêtre des paramètres
               Toast.makeText(ConversationActivity.this, "Paramètres Conversation", Toast.LENGTH_SHORT).show();
               return true;
            case R.id.invite:
            	// Inviter un contact
               // Toast.makeText(ConversationActivity.this, "Invitation d'un contact", Toast.LENGTH_SHORT).show();
            	inviterContact();                
                return true;
            case R.id.close:
                	// Ouvrir la fenêtre des paramètres
                    Toast.makeText(ConversationActivity.this, "Fermeture de la conversation", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.logout:
            	// Déconnexion et quitter l'application
               setResult(HelloEverybodyActivity.DECONNECTION);
               finish();
               return true;
         }
         return false;
     }
      
	 /** Appelée lorsque l'activité est finie */
	 @Override
     public void onDestroy() {
    	 ConversationsList.getInstance().removeConversationsListener(this);
     }

    /** Méthode qui est appelée lorsqu'une conversation démarre  */
  	public void conversationAdded(long idConversation) {
  		addConversationPage();
  	}

  	/** Méthode qui est appelée lorsqu'une conversation se ferme  */
  	public void conversationRemoved(long idConversation) {
  		// TODO Auto-generated method stub
  		
  	}

  	/** Méthode qui est appelée lorsqu'une conversation est modifiée  */
  	public void conversationChanged(long idConversation) {
  		// TODO Auto-generated method stub
  		
  	}

  	/** Méthode qui est appelée lorsqu'un message est ajouté  */
  	public void newMessage(long idConversation, ConversationMessage newMessage) {
  		addMessage(findPage(idConversation), newMessage);
  	}
    
  	/** Méthode qui met à jour l'affichage de la barre du haut */
    private void updateConversationBar() {
    	if (currentPage==0) {
    		mGoLeftImageView.setVisibility(ImageView.INVISIBLE);
    	} else {
    		mGoLeftImageView.setVisibility(ImageView.VISIBLE);
        }
    		
    	if (currentPage==pendingConversations.size()-1) {
    		mGoRightImageView.setVisibility(ImageView.INVISIBLE);
    	} else {
    		mGoRightImageView.setVisibility(ImageView.VISIBLE);
    	}
		mTitleTextView.setText(pendingConversations.get(currentPage).getTitle());
    }
    
    /** Méthode pour la création et l'ajout de message */
    private void addMessage(int idPage, ConversationMessage message) {
        ConversationMessage monMessage = new ConversationMessage();
        monMessage.setContact(message.getContact());
        monMessage.setMessage(message.getMessage());
        mConversationMessageAdapters.get(idPage).add(monMessage);
    }
    
    /** Méthode pour la création et l'ajout d'une conversation */
    private void addConversationPage() {
    	// Création d'une nouvelle page de conversation
    	LayoutInflater lf = getLayoutInflater();
    	ListView newConversationListView= (ListView) lf.inflate(R.layout.message_list, null);
    	mConversationsArrayList.add(newConversationListView);
        
    	// Instanciation d'un nouveau conteneur et adapteur pour les messages de
    	// la conversation
    	MessageAdapter newConversationMessageAdapter = new MessageAdapter(this, R.layout.message);
        newConversationListView.setAdapter(newConversationMessageAdapter);
        mConversationMessageAdapters.add(newConversationMessageAdapter);
    }
    
    /** Méthode qui retrouve le numéro de page de la conversation */
    private int findPage(long idConversation) {
    	for(int i = 0 ; i < pendingConversations.size() ; i++) {
    		if (pendingConversations.get(i).getId() == idConversation) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    private void inviterContact() {
        final Intent inviteContact = new Intent().setClass(this, InviteContactActivity.class);
        startActivity(inviteContact);
    }
}
