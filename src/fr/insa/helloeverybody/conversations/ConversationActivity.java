package fr.insa.helloeverybody.conversations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.insa.helloeverybody.TabsActivity;
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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
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

	private final int EXIT_CONVERSATION = 0;
	
	// Roomname de la conversation active (null si l'activité n'est pas démarrée)
	private static String activeConversation; 
	
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
				activeConversation = mConversationPagerAdapter.findRoomName(currentPage);
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
                ConversationsList.getInstance().sendMessage(activeConversation, view.getText().toString());
                view.setText("");
            }
        });
        
        // Initialisation de la barre de navigation
        mGoLeftImageView = (ImageView) findViewById(R.id.go_left);
        mGoRightImageView = (ImageView) findViewById(R.id.go_right);
        mTitleTextView= (TextView) findViewById(R.id.title);
        mGoLeftImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				System.out.println("closing: "+currentPage);
				mConversationViewPager.setCurrentItem(--currentPage);
			}
		});
        mGoRightImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				System.out.println("closing: "+currentPage);
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

        activeConversation = mConversationPagerAdapter.findRoomName(currentPage);
        updateConversationBar();
        
        System.out.println("on Create conversation "+ currentPage);
        pendingConversations.get(activeConversation).setNbUnreadMessages(0);
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
            case R.id.invite:
	        	// Inviter un contact
	            // Toast.makeText(ConversationActivity.this, "Invitation d'un contact", Toast.LENGTH_SHORT).show();
	        	inviterContact();                
	            return true;
            case R.id.close:
				// Ferme la conversation en cours
				showDialog(EXIT_CONVERSATION);
                return true;
         }
         return false;
     }
      
	 @Override
	protected void onStart() {
		super.onStart();
		activeConversation = mConversationPagerAdapter.findRoomName(currentPage);
	}

	@Override
	protected void onStop() {
		super.onStop();
		activeConversation = null;
	}

	@Override
	/** Méthode qui se déclenchera lors de l'appel d'une boite de dialogue */
	protected Dialog onCreateDialog(int id) {
		 Dialog dialog;
		 switch (id) {
		 case EXIT_CONVERSATION:
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 builder.setMessage("Voulez-vous vraiment fermer cette conversation ?");
			 builder.setCancelable(false);
			 builder.setPositiveButton("Oui",
				 new DialogInterface.OnClickListener() {
					 public void onClick(DialogInterface dialog, int id) {;
							ConversationsList.getInstance().sendLeave(activeConversation);
					 }
				 });
			 builder.setNegativeButton("Non",
				 new DialogInterface.OnClickListener() {
				 public void onClick(DialogInterface dialog, int id) {
					 dialog.cancel();
				 }
				 });
			 dialog = builder.create();
			 break;
		 default:
			 dialog = null;
		 }
		 return dialog;
	}

	/** Appelée lorsque l'activité est finie */
	 @Override
     public void onDestroy() {
		 super.onDestroy();
		 System.out.println("on Destroy conversation");
		 System.out.println("closing: "+currentPage);
		 currentPage=-1;
    	 ConversationsList.getInstance().removeConversationsListener(this);
     }

	 /** Méthode qui est appelée lorsque la creation d'une conversation a echouée */
	 public void creationConversationFailed() {
		 // Inutilisé
	}
	 
    /** Méthode qui est appelée lorsqu'une conversation démarre  */
  	public void conversationAdded(String roomName) {
  		addConversationPage(roomName);
  		mConversationPagerAdapter.notifyDataSetChanged();
  		mConversationViewPager.setCurrentItem(0);
  	}

  	/** Méthode qui est appelée lorsqu'une conversation se ferme  */
  	public void conversationRemoved(String roomName) {
  		mConversationMessageAdapters.remove(roomName);
  		mConversationsArrayList.remove(roomName);
  		if (pendingConversations.isEmpty()) {
			finish();
		} else {
	  		mConversationPagerAdapter.notifyDataSetChanged();
			mConversationViewPager.setCurrentItem(0);
		}
  	}

  	/** Méthode qui est appelée lorsqu'un nouveau membre arrive  */
	public void newMember(String roomName, String jid) {
		Profile newMember = ContactsList.getInstance().getProfileByJid(jid);
		Toast.makeText(ConversationActivity.this, newMember.getFirstName() + " arrive dans la conversation !" , Toast.LENGTH_SHORT).show();
		updateConversationBar();
	}

	/** Méthode qui est appelée lorsqu'un membre quitte  */
	public void memberQuit(String roomName, String jid) {
		Profile removedMember = ContactsList.getInstance().getProfileByJid(jid);
		Toast.makeText(ConversationActivity.this, removedMember.getFirstName() + " a quitté la conversation !" , Toast.LENGTH_SHORT).show();
		updateConversationBar();
		
	}

  	/** Méthode qui est appelée lorsqu'un message est ajouté  */
  	public void newMessage(String roomName, ConversationMessage newMessage) {
  		addMessage(roomName, newMessage);
  	}

  	/** Méthode qui est appelée lorsqu'un membre refuse une invitation  */
	public void rejectedInvitation(String roomName, String jid) {
		Profile removedMember = ContactsList.getInstance().getProfileByJid(jid);
		Toast.makeText(ConversationActivity.this, removedMember.getFirstName() + " refuse de vous parler !" , Toast.LENGTH_SHORT).show();
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
	    	
			mTitleTextView.setText(pendingConversations.get(activeConversation).getTitle());
			pendingConversations.get(activeConversation).setNbUnreadMessages(0);
			System.out.println("opening: "+currentPage);
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
        
        // Si la conversation est active, les messages sont lus
        Conversation conv = pendingConversations.get(activeConversation);
        if(conv != null && conv.getRoomName().equals(roomName)) {
        	conv.setNbUnreadMessages(0);
        }
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
    
    public static String getActiveConversation() {
		return activeConversation;
	}

	public static void setActiveConversation(String activeConversation) {
		ConversationActivity.activeConversation = activeConversation;
	}

	private void inviterContact() {
        final Intent inviteContact = new Intent().setClass(this, InviteContactActivity.class);
        inviteContact.putStringArrayListExtra("members", pendingConversations.get(activeConversation).getMembersIDs());
        inviteContact.putExtra("roomName", activeConversation);
        startActivityForResult(inviteContact,2);
    }
    
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
    	if(resultCode==8){		//invitation
    		ArrayList<String> toAdd=data.getStringArrayListExtra("toInvite");
    		String roomName = data.getStringExtra("roomName");
    		ConversationsList.getInstance().inviteMembers(roomName, toAdd);
    	}
    	
    }


    public void notification(ConversationMessage Message){
    	if(Message.getContact()!=null){
	    	String ns = Context.NOTIFICATION_SERVICE;
	    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
	    	int icon = R.drawable.star_big_on;
	    	CharSequence tickerText = "New Message";
	    	long when = System.currentTimeMillis();
	
	    	Notification notification = new Notification(icon, tickerText, when);
	    	Context context = getApplicationContext();
	    	CharSequence contentTitle = "New message!";
	    	String contentText = Message.getContact().getFirstName()==null?"":(Message.getContact().getFirstName()+" ");
	    		contentText+=(Message.getContact().getLastName()==null)?"":(Message.getContact().getLastName()+" ");
	    		contentText+=  "says: "+Message.getMessage();
	    	//Intent i= new Intent(Intent.ACTION_MAIN);
	    	//System.out.println("father: " + this.getParent().getLocalClassName());
	    	//System.out.println("grandpa: " + this.getParent().getParent().getLocalClassName());
	    	//HelloEverybodyActivity hea=(HelloEverybodyActivity) this.getParent();
	    	//System.out.println("hea  "+hea);
	    	TabsActivity.setUnreadChats(ConversationsList.getInstance().getUnreadConversationscount());
	    	Intent notificationIntent = this.getIntent().putExtra("id", activeConversation);
	    	
	    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
	
	    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	    	notification.flags=Notification.FLAG_AUTO_CANCEL;
	    	mNotificationManager.notify(1, notification);
	    }
    }

	public void conversationPublicAdded(String roomName) {
	}
    
}
