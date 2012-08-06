package fr.insa.helloeverybody.conversations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Pair;
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
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.controls.ConversationPagerAdapter;
import fr.insa.helloeverybody.controls.MessageAdapter;
import fr.insa.helloeverybody.helpers.LogWriter;
import fr.insa.helloeverybody.interfaces.ConversationListener;
import fr.insa.helloeverybody.models.Conversation;
import fr.insa.helloeverybody.models.ConversationMessage;
import fr.insa.helloeverybody.smack.XmppRoomManager;
import fr.insa.helloeverybody.viewmodels.ConversationList;
import fr.insa.helloeverybody.viewmodels.LocalUserProfile;

public class ConversationActivity extends Activity implements ConversationListener {

	// Constantes
	public final static String TAG = "ConversationActivity";
	public final static String ROOM_NAME_EXTRA = "room_name";
	private final static int EXIT_CONVERSATION = 0;
	private final static int INVITE_CONTACT_REQUEST = 1;
	
	// Salon actif (null si l'activité n'est pas démarrée)
	private static String visibleRoomName; 
	private String currentRoomName;

	// Adaptateur pour la vue
	private ViewPager mConversationViewPager;
    private ConversationPagerAdapter mConversationPagerAdapter;
    
    // Liste des conversations
    private List<Pair<String, ListView>> mListViewList;
    private Map<String, ListView> mListViewMap;
    private Map<String, MessageAdapter> mMessageAdapterMap;
    
    // Bar de navigation
    private ImageView mGoLeftImageView;
    private ImageView mGoRightImageView;
    private TextView mTitleTextView;
    
    // Retourne la nom de la conversation courante
    public static String getVisbleRoomName() {
		return visibleRoomName;
	}
    
    // Crée l'activité
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation);
        
        LogWriter.logIfDebug(TAG, "OnCreate");
        ConversationList.getInstance().addConversationListener(this);
        
        // Instanciation du conteneur de conversation
        mListViewList = Collections.synchronizedList(
        			new LinkedList<Pair<String, ListView>>());
        mListViewMap = new HashMap<String, ListView>();
        mMessageAdapterMap = new HashMap<String, MessageAdapter>();
        
        // Initialisation du conteneur et de l'adaptateur de pages
        mConversationViewPager = (ViewPager) findViewById(R.id.message_list);
        mConversationPagerAdapter = new ConversationPagerAdapter(this, mListViewList);
        mConversationViewPager.setAdapter(mConversationPagerAdapter);
        mConversationViewPager.setOnPageChangeListener(new ConversationPageChangeListener());
        
        // Gérer le click du boutton Envoyer
        Button bSend = (Button) findViewById(R.id.button_send);
        bSend.setOnClickListener(new SendButtonClickListener());
        
        // Initialiser la barre de navigation
        mGoLeftImageView = (ImageView) findViewById(R.id.go_left);
        mGoRightImageView = (ImageView) findViewById(R.id.go_right);
        mTitleTextView = (TextView) findViewById(R.id.title);
        mGoLeftImageView.setOnClickListener(new NavigationClickListener(-1));
        mGoRightImageView.setOnClickListener(new NavigationClickListener(+1));
        
        // Ajouter les conversations existantes
        ConversationList conversationList = ConversationList.getInstance();
        for (Conversation conversation : conversationList.getPendingRoomList()) {
        	addConversationPage(conversation.getRoomName());
        	for (ConversationMessage message : conversation.getMessages()) {
        		addMessage(conversation.getRoomName(), message);
        	}
        }
        
        // Récupérer le numéro de page de conversation
        Bundle extras = getIntent().getExtras();
        currentRoomName = extras.getString(ROOM_NAME_EXTRA);
    }
    
    // Redémarre une activité existante avec d'autres paramètres
    @Override
    protected void onNewIntent(Intent intent) {
    	
    	// Récupérer le salon courant
        Bundle extras = getIntent().getExtras();
        currentRoomName = extras.getString(ROOM_NAME_EXTRA);
        visibleRoomName = currentRoomName;
        
		// Mettre à jour le nom du salon
        int currentPage = ConversationList
        			.getInstance().getPendingIndex(currentRoomName);
        mConversationViewPager.setCurrentItem(currentPage);
        updateConversationBar();
    }
    
    // Débute l'activité
	@Override
	protected void onStart() {
		super.onStart();
		
		// Mettre à jour le nom du salon
        visibleRoomName = currentRoomName;
        int currentPage = ConversationList
    			.getInstance().getPendingIndex(currentRoomName);
        mConversationViewPager.setCurrentItem(currentPage);
        updateConversationBar();
	}

	// Arrête l'activité
	@Override
	protected void onStop() {
		super.onStop();
		visibleRoomName = null;
	}

	// Détruit l'activité
	@Override
	public void onDestroy() {
		super.onDestroy();
    	ConversationList.getInstance().removeConversationListener(this);
	}
    
    // Crée le menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversation, menu);
 
        return true;
	}
 
	// Exécute l'action correspondant au bouton du menu cliqué
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	 	switch (item.getItemId()) {
	 	
	 		// Inviter un contact
	    	case R.id.invite:
	            final Intent inviteContact = new Intent().
	            		setClass(this, InviteContactActivity.class);
	            inviteContact.putStringArrayListExtra("memberJidList", 
	            		ConversationList.getInstance().
	            		getConversationByName(currentRoomName).getMemberJidList());
	            inviteContact.putExtra("roomName", currentRoomName);
	            startActivityForResult(inviteContact, INVITE_CONTACT_REQUEST);               
	            return true;
	           
	        // Fermer la conversation actuelle
	    	case R.id.close:
				showDialog(EXIT_CONVERSATION);
	            return true;
	     }
	 	
	     return false;
	}

	// Crée un dialogue de conversation pour quitter la conversation
	@Override
	protected Dialog onCreateDialog(int id) {
		 Dialog dialog;
		 switch (id) {
		 
		 	// Demander confirmation de la fermeture de la conversation
			case EXIT_CONVERSATION:
				 AlertDialog.Builder builder = new AlertDialog.Builder(this);
				 builder.setMessage("Voulez-vous vraiment fermer cette conversation ?");
				 builder.setCancelable(false);
				 builder.setPositiveButton("Oui",
					 new DialogInterface.OnClickListener() {
						 public void onClick(DialogInterface dialog, int id) {
								XmppRoomManager.getInstance().leaveRoom(currentRoomName);
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
	
			// Pas de dialogue si l'identifiant n'est pas connu
			default:
				dialog = null;
		 }
		 
		 return dialog;
	}
	
	// Invite les utilisateurs choisies dans l'activité InviteContactActivity
	@Override
    protected void onActivityResult(int requestCode, 
    					int resultCode, Intent data) {
		switch (requestCode) {
    	
			// Invite les utilisateurs
			case INVITE_CONTACT_REQUEST :
	    		if (resultCode != 0) {		
		    		ArrayList<String> inviteeJidList = 
		    				data.getStringArrayListExtra("toInvite");
		    		String roomName = data.getStringExtra("roomName");
		    		XmppRoomManager.getInstance()
		    			.inviteUserListToRoom(roomName, inviteeJidList);
	    		}
	    		break;
		}
    }
    
  	// Met à jour la barre de navigation des conversations
    private void updateConversationBar() {
    	
    	// Afficher ou cacher les boutons de navigation (gauche et droite)
    	int currentPage = ConversationList.getInstance()
    				.getPendingIndex(currentRoomName);
    	boolean isFirst = (currentPage == 0);
    	boolean isLast = (currentPage == mMessageAdapterMap.size() - 1);
    	mGoLeftImageView.setVisibility(isFirst ? 
    			ImageView.INVISIBLE : ImageView.VISIBLE);
    	mGoRightImageView.setVisibility(isLast ? 
    			ImageView.INVISIBLE : ImageView.VISIBLE);
    	
    	// Afficher le nom de la conversation
    	Conversation currentConversation = 
    			ConversationList.getInstance().getConversationByName(currentRoomName);
    	if (currentConversation != null) {
			mTitleTextView.setText(currentConversation.getRoomSubject());
			currentConversation.setNbUnreadMessages(0);
    	}
    }
    
    // Met à jour l'ordre des conversations
    private void updateConversationOrder(String aRoomName) {
	
		// Créer la nouvelle liste de ListView
		List<Pair<String, ListView>> newListViewList =
				new ArrayList<Pair<String, ListView>>(mListViewMap.size());
		List<Conversation> pendingConversationList =
				ConversationList.getInstance().getPendingRoomList();
		for (Conversation conversation : pendingConversationList) {
			String roomName = conversation.getRoomName(); 
			ListView listView = mListViewMap.get(roomName);
			if (listView != null) {
				newListViewList.add(new Pair<String, ListView>(roomName, listView));
			}
		}
		
		// Mettre à jour la liste de ListView
		synchronized (mListViewList) {
			mListViewList.clear();
			mListViewList.addAll(newListViewList);
		}
		
		// Mettre à jour le numéro de page courante
		int currentPage = ConversationList.getInstance()
					.getPendingIndex(currentRoomName);
        mConversationViewPager.setCurrentItem(currentPage);
        mConversationPagerAdapter.notifyDataSetChanged();
        updateConversationBar();
    }
    
    // Ajoute un message
    private void addMessage(String roomName, ConversationMessage message) {
        		
		// Vérfier que la conversation a été créée
        MessageAdapter messageAdapter = mMessageAdapterMap.get(roomName);
        if (messageAdapter == null) {
			return;
        }
        
    	// Créer une copie du message
        ConversationMessage messageCopy = new ConversationMessage();
        messageCopy.setContact(message.getContact());
        messageCopy.setMessage(message.getMessage());
        
        // Ajouter le message à l'adaptateur
        messageAdapter.add(messageCopy);
        messageAdapter.notifyDataSetChanged();
        
        // Afficher le message
        ListView listView = mListViewMap.get(roomName);
        listView.invalidateViews();
        listView.scrollBy(0, 0);
        
        // Marquer le message comme lu
        Conversation conversation = ConversationList.getInstance().
        		getConversationByName(currentRoomName);
        if ((conversation != null) 
        		&& (conversation.getRoomName().equals(roomName))) {
        	conversation.setNbUnreadMessages(0);
        }
    }
    
    // Ajoute une page de conversation
    private void addConversationPage(String roomName) {
    	
    	// Ajouter la vue de la conversation
    	LayoutInflater xmlLayout = getLayoutInflater();
    	ListView newConversationListView = (ListView) 
    			xmlLayout.inflate(R.layout.message_list, null);
    	mListViewMap.put(roomName, newConversationListView);
    	mListViewList.add(new Pair<String, ListView>(
    			roomName, newConversationListView));
        
    	// Ajouter l'adaptateur des messages à la vue
    	MessageAdapter newMessageAdapter = 
    			new MessageAdapter(this, R.layout.message);
        newConversationListView.setAdapter(newMessageAdapter);
        mMessageAdapterMap.put(roomName, newMessageAdapter);
        
        // Signaler l'ajout de la conversation
        mConversationPagerAdapter.notifyDataSetChanged();
    }
    
	
	/* Implémentation de l'interfaces des listeners de conversations
	-------------------------------------------------------------------------*/
	public void onConversationCreationFailed(String roomName) { }
	 
 	public void onConversationCreationSucceeded(final String roomName) { }
 	
	public void onPublicConversationAdded(String roomName) { }

 	public void onConversationRemoved(final String aRoomName) {
		runOnUiThread(new Runnable() {
			public void run() {
				
				// Supprimer la conversation
		 		mMessageAdapterMap.remove(aRoomName);
		 		mListViewMap.remove(aRoomName);
		 		Iterator<Pair<String, ListView>> iterator = mListViewList.iterator();
				while (iterator.hasNext()) {
					Pair<String, ListView> pair = iterator.next();
					String roomName = pair.first;
					if ((roomName != null) && (roomName.equals(aRoomName))) {
						iterator.remove();
					}
				}
				
				// Fermer l'activité si c'était la conversation active
				if (aRoomName.equals(currentRoomName)) {
					finish();
				}
			}
		});
 	}
 	
	public void onMemberJoined(String roomName, String jid) {
		if (roomName.equals(currentRoomName)) {
			runOnUiThread(new Runnable() {
				public void run() {
					updateConversationBar();
				}
			});
		}
	}

	public void onMemberLeft(String roomName, String jid) {
		if (roomName.equals(currentRoomName)) {
			runOnUiThread(new Runnable() {
				public void run() {
					updateConversationBar();
				}
			});
		}
	}
	
 	public void onMessageReceived(final String roomName, final ConversationMessage newMessage) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (mListViewMap.containsKey(roomName) == false) {
					addConversationPage(roomName);
				}
				updateConversationOrder(roomName);
				addMessage(roomName, newMessage);
			}
		});
 	}

	public void onInvitationRejected(String roomName, String jid) { }
	
	
	/* Classe capable d'envoyer un message
	-------------------------------------------------------------------------*/
	private class SendButtonClickListener implements OnClickListener {

        // Envoye un message
        public void onClick(View v) {
        	
        	// Récupérer le message
            EditText view = (EditText) findViewById(R.id.edit_text_out);
            String messageText = view.getText().toString();
            
            // Envoyer le message
            if ((messageText != null) && (messageText != "")) {
            	ConversationMessage newMessage = new ConversationMessage();
    			newMessage.setMessage(messageText);
    			newMessage.setContact(LocalUserProfile.getInstance().getProfile());
        		XmppRoomManager.getInstance().sendMessageToRoom(currentRoomName, messageText);
        		addMessage(currentRoomName, newMessage);
                view.setText("");
            }
        }
	}
	
	
	/* Classe capable de changer de page de conversation
	-------------------------------------------------------------------------*/
	private class ConversationPageChangeListener implements OnPageChangeListener {
        	
		// Change de conversation
		public void onPageSelected(int pageNumber) {
			currentRoomName = ConversationList.getInstance()
						.getPendingRoomName(pageNumber);
			visibleRoomName = currentRoomName;
			updateConversationBar();
		}
		
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		
		public void onPageScrollStateChanged(int arg0) {
		}
	}
	
	
	/* Classe capable de gérer les clics de navigation
	-------------------------------------------------------------------------*/
	private class NavigationClickListener implements OnClickListener {
		
		private int mShiftPage;
		
		// Constructeur
		public NavigationClickListener(int shiftPage) {
			mShiftPage = shiftPage;
		}
		
		// Gère un clic sur un bouton de navigation
		public void onClick(View view) {
			int currentPage = ConversationList.getInstance()
						.getPendingIndex(currentRoomName);
			currentPage += mShiftPage;
			mConversationViewPager.setCurrentItem(currentPage);
	        currentRoomName = ConversationList.getInstance()
						.getPendingRoomName(currentPage);
	        visibleRoomName = currentRoomName;
			updateConversationBar();
		}
	}
}
