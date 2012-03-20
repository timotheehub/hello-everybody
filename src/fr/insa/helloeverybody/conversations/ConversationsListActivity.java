package fr.insa.helloeverybody.conversations;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.contacts.InviteContactActivity;
import fr.insa.helloeverybody.helpers.ConversationsListener;
import fr.insa.helloeverybody.helpers.SeparatedConversationListAdapter;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.Conversation;
import fr.insa.helloeverybody.models.ConversationMessage;
import fr.insa.helloeverybody.models.ConversationsList;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.preferences.UserPreferencesActivity;
import fr.insa.helloeverybody.smack.ChatService;
import fr.insa.helloeverybody.smack.InternalEvent;


public class ConversationsListActivity extends Activity implements ConversationsListener {
	
	public final static int CONVERSATION_ACTIVITY = 1;
	
	// Listes de conversations
	private Map<String,Conversation> pendingConversationsList;
	private Map<String,Conversation> publicConversationsList;
	
	// ListView des contacts
	private ListView conversationsListView;
	
	//Gestions groupes publiques
	private ConversationsListActions conversationsListActions;
	
	private Handler newRoomHandler = new Handler(){
		public void handleMessage(Message msg){
			InternalEvent ie = (InternalEvent)msg.obj;
			if (ie.getMessageCode() == ChatService.EVT_NEW_ROOM) {
				updateConversationViews();
			}
		}
	};
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        conversationsListActions = ConversationsListActions.getInstance(this);
        System.out.println("on Create conversation List");
		setContentView(R.layout.conversations_list);
		ConversationsList.getInstance().addConversationsListener(this);
		fillPendingConversationsList();
		fillPublicConversationsList();
		fillConversationsView();
		ConversationsList.getInstance().registerHandler(newRoomHandler);
    }
    
    
    
    @Override
	protected void onResume() {
		super.onResume();
		//updateConversationViews();
	}



	// Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone
    public boolean onCreateOptionsMenu(Menu menu) {
 
        //Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML spécifier en un objet Menu
        inflater.inflate(R.menu.conversations, menu);
 
        return true;
    }
 
    
    
    // Méthode qui se déclenchera au clic sur un item
    public boolean onOptionsItemSelected(MenuItem item) {
         //On regarde quel item a été cliqué grâce à son id et on déclenche une action
         switch (item.getItemId()) {
            case R.id.parameters:
            	// Ouvrir la fenêtre des paramètres
            	final Intent settingsActivity = new Intent(getBaseContext(), UserPreferencesActivity.class);
                startActivity(settingsActivity);
                return true;
            case R.id.add_public_group:
            	// Créer un groupe publique
                Toast.makeText(ConversationsListActivity.this, "Création d'un groupe publique", Toast.LENGTH_SHORT).show();
                Intent newGroupActivity = new Intent(getBaseContext(), InviteContactActivity.class);
                newGroupActivity.putStringArrayListExtra("members", new ArrayList<String>());
                startActivityForResult(newGroupActivity, CONVERSATION_ACTIVITY);
                return true;
            case R.id.logout:
            	// Déconnexion et quitter l'application
               finish();
               return true;
         }
         return false;
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch(requestCode) {
    		case CONVERSATION_ACTIVITY :
    			if (resultCode != 0){
    				final ArrayList<String> selectedList = data.getStringArrayListExtra("toInvite");
    			
    				final Dialog dialog = new Dialog(ConversationsListActivity.this);
    				dialog.setContentView(R.layout.request_room_name);
    				dialog.setTitle("Saisir le nom du groupe");
    				final EditText titleText = (EditText)(dialog.findViewById(R.id.title));
    	    	
    				Button acceptButton = (Button)(dialog.findViewById(R.id.accept));
    				acceptButton.setOnClickListener(new View.OnClickListener() {
    					public void onClick(View v) {
    						String title = titleText.getText().toString();
    						if (!title.equals("")) {
    							ConversationsList.getInstance().createPublicGroupConversation(selectedList,title);
    							dialog.dismiss();
    						}
    						else {
    							ConversationsList.getInstance().createPublicGroupConversation(selectedList,"Default Name");
    							dialog.dismiss();
    						}
    					}
    				});
    				dialog.show();
    			}
			break;
    	}
    }
    
    // Creer la vue des conversations
    private void fillConversationsView() {
    	final SeparatedConversationListAdapter listAdapter = new SeparatedConversationListAdapter(this);
    	conversationsListActions.askUpdateGroups();

		listAdapter.addSection(getString(R.string.pending),
					getPendingAdapter(), getConversationIds(pendingConversationsList));
		listAdapter.addSection(getString(R.string.opened_to_all),
					getPublicAdapter(), getConversationIds(publicConversationsList));
		
		conversationsListView = (ListView) findViewById(R.id.conversations_list);
		conversationsListView.setAdapter(listAdapter);
		
		final Intent intent;  // Reusable Intent for each tab
		
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, ConversationActivity.class);
		
        conversationsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            	//TODO : Ajouter le démarrage d'une conversation publique ici
        		intent.putExtra("id", listAdapter.getRoomName(position) );
        		startActivityForResult(intent,CONVERSATION_ACTIVITY);
        	}
         });
    }
    

    
	// Retourne la liste des identifiants
	private List<String> getConversationIds(Map<String,Conversation> conversationsList) {
		List<String> conversationIds = new ArrayList<String>();
		
		for (Entry<String,Conversation> conversation : conversationsList.entrySet()) {
			conversationIds.add(conversation.getKey());
		}
		
		return conversationIds;
	}
    
    // Retourne l'adaptateur des discussions en cours
	private SimpleAdapter getPendingAdapter() {
    	List<Map<String, Object>> pendingList = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> pendingAttributesMap;
		
		for (Entry<String,Conversation> conversation : pendingConversationsList.entrySet()) {
			pendingAttributesMap = new HashMap<String, Object>();
			
			if(conversation.getValue().isPublic()) {
				pendingAttributesMap.put("private", R.drawable.empty_key);
			}
			else {
				pendingAttributesMap.put("private", R.drawable.key);
			}
			
			pendingAttributesMap.put("title", conversation.getValue().getTitle());
			
			if(conversation.getValue().getNbUnreadMessages() > 0) {
				pendingAttributesMap.put("unread_msg", conversation.getValue().getNbUnreadMessages());
			}
			else {
				pendingAttributesMap.put("unread_msg", "");
			}
			pendingList.add(pendingAttributesMap);
		}
    	
    	SimpleAdapter pendingAdapter = new SimpleAdapter (this.getBaseContext(),
    			pendingList, R.layout.conversation_item,
        		new String[] {"private", "title", "unread_msg"},
        		new int[] {R.id.private_conversation, R.id.title, R.id.unread_message});
			
    	return pendingAdapter;
    }
    
    
    // Retourne l'adaptateur des discussions publiques
    private SimpleAdapter getPublicAdapter() {
    	List<Map<String, Object>> publicList = new ArrayList<Map<String, Object>>();
		Map<String, Object> publicAttributesMap;
		
		for (Entry<String,Conversation> conversation : publicConversationsList.entrySet()) {
			publicAttributesMap = new HashMap<String, Object>();
			publicAttributesMap.put("private", R.drawable.empty_key);
			publicAttributesMap.put("title", conversation.getValue().getTitle());
			publicAttributesMap.put("unread_msg", "");
			publicList.add(publicAttributesMap);
		}
    	
    	SimpleAdapter pendingAdapter = new SimpleAdapter (this.getBaseContext(),
    			publicList, R.layout.conversation_item,
        		new String[] {"private", "title", "unread_msg"}, 
        		new int[] {R.id.private_conversation, R.id.title, R.id.unread_message});
    	
    	return pendingAdapter;
    }
    
    
    // Creer les conversations en cours
    private void fillPendingConversationsList() {
    	pendingConversationsList = ConversationsList.getInstance().getPendingList();
    }
    
    
    
    // Creer les conversations publics
    private void fillPublicConversationsList() {
    	publicConversationsList = ConversationsList.getInstance().getPublicList();
    }
    
    private void updateConversationViews() {
    	fillConversationsView();
    }

    public void creationConversationFailed() {
		// Inutilisé
	}
    
	public void conversationAdded(String roomName) {
		updateConversationViews();
	}

	public void conversationRemoved(String roomName) {
		updateConversationViews();
	}

  	/** Méthode qui est appelée lorsqu'un nouveau membre arrive  */
	public void newMember(String roomName, String jid) {
		updateConversationViews();
	}

	/** Méthode qui est appelée lorsqu'un membre quitte  */
	public void memberQuit(String roomName, String jid) {
		updateConversationViews();
	}

	public void newMessage(String roomName, ConversationMessage newMessage) {
		// Inutilisé
	
	}

	public void rejectedInvitation(String roomName, String jid) {
		Profile removedMember = ContactsList.getInstance().getProfileByJid(jid);
		Toast.makeText(ConversationsListActivity.this, removedMember.getFirstName() + " refuse de vous parler !" , Toast.LENGTH_SHORT).show();
	}
    
   
    
    @Override
	public void onDestroy() {
    	System.out.println("on Destroy conversation List");
		super.onDestroy();
    	ConversationsList.getInstance().removeConversationsListener(this);
    }



	public void conversationPublicAdded(String roomName) {
		updateConversationViews();
	}
    
}
