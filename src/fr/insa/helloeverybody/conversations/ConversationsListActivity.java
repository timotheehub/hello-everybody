package fr.insa.helloeverybody.conversations;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.insa.helloeverybody.HelloEverybodyActivity;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.helpers.ConversationsListener;
import fr.insa.helloeverybody.helpers.SeparatedConversationListAdapter;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.Conversation;
import fr.insa.helloeverybody.models.ConversationMessage;
import fr.insa.helloeverybody.models.ConversationsList;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.preferences.UserPreferencesActivity;


public class ConversationsListActivity extends Activity implements ConversationsListener {
	
	public final static int CONVERSATION_ACTIVITY = 1;
	
	// Listes de conversations
	private Map<String,Conversation> pendingConversationsList;
	private Map<String,Conversation> publicConversationsList;
	
	// ListView des contacts
	private ListView conversationsListView;
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setContentView(R.layout.conversations_list);
		
		fillPendingConversationsList();
		fillPublicConversationsList();
		fillConversationsView();
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
    			switch (resultCode) {
    				case HelloEverybodyActivity.DECONNECTION :
    					finish();
    					break;
    			}
			break;
    	}
    }
    
    // Creer la vue des conversations
    private void fillConversationsView() {
    	final SeparatedConversationListAdapter listAdapter = new SeparatedConversationListAdapter(this);

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
        	@SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
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
    	List<Map<String, String>> pendingList = new ArrayList<Map<String, String>>();
		
		Map<String, String> pendingAttributesMap;
		
		for (Entry<String,Conversation> conversation : pendingConversationsList.entrySet()) {
			pendingAttributesMap = new HashMap<String, String>();
			pendingAttributesMap.put("title", conversation.getValue().getTitle());
			pendingList.add(pendingAttributesMap);
		}
    	
    	SimpleAdapter pendingAdapter = new SimpleAdapter (this.getBaseContext(),
    			pendingList, R.layout.conversation_item,
        		new String[] {"title"}, 
        		new int[] {R.id.title});
    	
    	return pendingAdapter;
    }
    
    
    // Retourne l'adaptateur des discussions publiques
    private SimpleAdapter getPublicAdapter() {
    	List<Map<String, String>> publicList = new ArrayList<Map<String, String>>();
		Map<String, String> publicAttributesMap;
		
		for (Entry<String,Conversation> conversation : publicConversationsList.entrySet()) {
			publicAttributesMap = new HashMap<String, String>();
			publicAttributesMap.put("title", conversation.getValue().getTitle());
			publicList.add(publicAttributesMap);
		}
    	
    	SimpleAdapter pendingAdapter = new SimpleAdapter (this.getBaseContext(),
    			publicList, R.layout.conversation_item,
        		new String[] {"title"}, 
        		new int[] {R.id.title});
    	
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
    	final SeparatedConversationListAdapter listAdapter = new SeparatedConversationListAdapter(this);

		listAdapter.addSection(getString(R.string.pending),
					getPendingAdapter(), getConversationIds(pendingConversationsList));
		listAdapter.addSection(getString(R.string.opened_to_all),
					getPublicAdapter(), getConversationIds(publicConversationsList));
		
		conversationsListView.setAdapter(listAdapter);
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
    
   /* public void notification(){
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
    	int icon = R.drawable.star_big_on;
    	CharSequence tickerText = "New Message";
    	long when = System.currentTimeMillis();

    	Notification notification = new Notification(icon, tickerText, when);
    	Context context = getApplicationContext();
    	CharSequence contentTitle = "New message!";
    	CharSequence contentText = "Click to open conversations";
    	HelloEverybodyActivity hea=(HelloEverybodyActivity) this.getParent();
    	hea.setUnreadChats(ConversationsList.getInstance().getUnreadConversationscount());
    	Intent notificationIntent = this.getParent().getIntent().putExtra("tab", hea.getTab());
    	
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    	notification.flags=Notification.FLAG_AUTO_CANCEL;
    	mNotificationManager.notify(1, notification);
    }*/
    
}