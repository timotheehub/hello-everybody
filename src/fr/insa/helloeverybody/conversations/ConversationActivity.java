package fr.insa.helloeverybody.conversations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.insa.helloeverybody.HelloEverybodyActivity;
import fr.insa.helloeverybody.contacts.InviteContactActivity;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.helpers.ConversationPagerAdapter;
import fr.insa.helloeverybody.helpers.ConversationsListener;
import fr.insa.helloeverybody.helpers.MessageAdapter;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.Conversation;
import fr.insa.helloeverybody.models.ConversationMessage;
import fr.insa.helloeverybody.models.ConversationsList;
import fr.insa.helloeverybody.models.Profile;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

public class ConversationActivity extends Activity implements ConversationsListener {
    // TODO : Ajouter un moyen de fermer une conversation

    // Page courrante affichée
    private int currentPage;

	/** Widget et conteneur pour la liste des conversations */ 
	// Permet de naviguer entre les conversations
	private ViewPager mConversationViewPager;
    // Adaptateur pour les listes de messages des conversations
    private ConversationPagerAdapter mConversationPagerAdapter;
    
    /** Widget et conteneur pour les messages d'une conversation */
    // Liste des messages des conversations
    private LinkedHashMap<String,ListView> mConversationsArrayList;
    // Adaptateur pour les messages
    private LinkedHashMap<String,MessageAdapter> mConversationMessageAdapters;
    
    /** Widgets pour la barre de navigation */
    // Flèche gauche
    private ImageView mGoLeftImageView;
    // Flèche droite
    private ImageView mGoRightImageView;
    // Titre de la conversation
    private TextView mTitleTextView;
    
    /** Modèles */
    private Map<String,Conversation> pendingConversations;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.conversation);
        
        //Récupération de la liste des conversations en cours
        pendingConversations = ConversationsList.getInstance().getPendingList();
        
        ConversationsList.getInstance().addConversationsListener(this);
        
        // Instanciation du conteneur de conversation
        mConversationsArrayList = new LinkedHashMap<String,ListView>();
        
        // Instanciation du conteneur d'adaptateur et de conteneur de messages
        mConversationMessageAdapters = new LinkedHashMap<String,MessageAdapter>();
        
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
                ConversationsList.getInstance().addSendMessage(mConversationPagerAdapter.findRoomName(currentPage), view.getText().toString());
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
        
        // Initialisation des conversations lancées existantes
        for (Entry<String,Conversation> conversation : pendingConversations.entrySet()) {
        	addConversationPage(conversation.getKey());
        	for (ConversationMessage message : conversation.getValue().getMessages()) {
        		addMessage(conversation.getKey(),message);
        	}
        }
        
        Bundle extras = getIntent().getExtras();
        currentPage = mConversationPagerAdapter.findPage(extras.getString("id"));
        mConversationViewPager.setCurrentItem(currentPage);
        
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
                	// Ferme la conversation en cours
            		conversationRemoved(mConversationPagerAdapter.findRoomName(currentPage));
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
		 super.onDestroy();
    	 ConversationsList.getInstance().removeConversationsListener(this);
     }

    /** Méthode qui est appelée lorsqu'une conversation démarre  */
  	public void conversationAdded(String roomName) {
  		addConversationPage(roomName);
  	}

  	/** Méthode qui est appelée lorsqu'une conversation se ferme  */
  	public void conversationRemoved(String roomName) {
  		// TODO Auto-generated method stub
  		
  	}

  	/** Méthode qui est appelée lorsqu'une conversation est modifiée  */
  	public void conversationChanged(String roomName) {
  		// TODO Auto-generated method stub
  		
  	}

  	/** Méthode qui est appelée lorsqu'un message est ajouté  */
  	public void newMessage(String roomName, ConversationMessage newMessage) {
  		addMessage(roomName, newMessage);
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
		mTitleTextView.setText(pendingConversations.get(mConversationPagerAdapter.findRoomName(currentPage)).getTitle());
		//pendingConversations.get(currentPage).setNbUnreadMessages(0);
    }
    
    /** Méthode pour la création et l'ajout de message */
    private void addMessage(String roomName, ConversationMessage message) {
        ConversationMessage monMessage = new ConversationMessage();
        monMessage.setContact(message.getContact());
        monMessage.setMessage(message.getMessage());
        mConversationMessageAdapters.get(roomName).add(monMessage);
        mConversationMessageAdapters.get(roomName).notifyDataSetChanged();
        mConversationsArrayList.get(roomName).invalidateViews();
        mConversationsArrayList.get(roomName).scrollBy(0, 0);
        //if(!roomName.equals(mConversationPagerAdapter.findRoomName(currentPage))){
        	//pendingConversations.get(idPage).addUnreadMessage();
        //	notification(monMessage);
       // }
    }
    
    /** Méthode pour la création et l'ajout d'une conversation */
    private void addConversationPage(String roomName) {
    	// Création d'une nouvelle page de conversation
    	LayoutInflater lf = getLayoutInflater();
    	ListView newConversationListView= (ListView) lf.inflate(R.layout.message_list, null);
    	mConversationsArrayList.put(roomName,newConversationListView);
        
    	// Instanciation d'un nouveau conteneur et adapteur pour les messages de
    	// la conversation
    	MessageAdapter newConversationMessageAdapter = new MessageAdapter(this, R.layout.message);
        newConversationListView.setAdapter(newConversationMessageAdapter);
        mConversationMessageAdapters.put(roomName,newConversationMessageAdapter);
        mConversationPagerAdapter.notifyDataSetChanged();
    }
    
    private void inviterContact() {
        final Intent inviteContact = new Intent().setClass(this, InviteContactActivity.class);
        inviteContact.putStringArrayListExtra("members", pendingConversations.get(mConversationPagerAdapter.findRoomName(currentPage)).getMembersIDs());
        startActivityForResult(inviteContact,2);
    }
    
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
    	if(requestCode==2 &&resultCode==8){		//invitation
    		ArrayList<String> toAdd=data.getStringArrayListExtra("toInvite");
    		ConversationMessage invmsg= new ConversationMessage();
    		//invmsg.setContact(HelloEverybodyActivity.userProfil);
    		String msgtxt="Invited ";
    		for(String userID:toAdd){
    			//search profile with the same ID
    			Profile p=ContactsList.getInstance().getProfileById(Long.parseLong(userID));
    			pendingConversations.get(mConversationPagerAdapter.findRoomName(currentPage)).addMember(p); 
    			msgtxt+=p.getFirstName()+" "+p.getLastName()+", ";
    			//MultiUserChat muc=new MultiUserChat(ChatService.GetChatService().getConnection(),pendingConversations.get(currentPage).getTitle());
    			//muc.invite(p.getJid(), "invite");
    		}
    		invmsg.setMessage(msgtxt.substring(0, msgtxt.length()-2)+" to the conversation.");
    		//System.out.println(msgtxt+"to the conversation.");
    		addMessage(mConversationPagerAdapter.findRoomName(currentPage),invmsg);
    	}
    	
    }


    public void notification(ConversationMessage Message){
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
    	int icon = R.drawable.star_big_on;
    	CharSequence tickerText = "New Message";
    	long when = System.currentTimeMillis();

    	Notification notification = new Notification(icon, tickerText, when);
    	Context context = getApplicationContext();
    	CharSequence contentTitle = "New message!";
    	CharSequence contentText = Message.getContact().getFirstName()==null?"":(Message.getContact().getFirstName()+" ")+Message.getContact().getLastName()==null?"":(Message.getContact().getLastName()+" ")+ "says: "+Message.getMessage();
    	//HelloEverybodyActivity hea=(HelloEverybodyActivity) this.getParent();
    	//System.out.println("hea  "+hea);
    	//hea.setUnreadChats(ConversationsList.getInstance().getUnreadConversationscount());
    	Intent notificationIntent = this.getIntent().putExtra("id", mConversationPagerAdapter.findRoomName(currentPage));
    	
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    	notification.flags=Notification.FLAG_AUTO_CANCEL;
    	mNotificationManager.notify(1, notification);
    }
}
