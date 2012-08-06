package fr.insa.helloeverybody.conversations;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.controls.SeparatedConversationListAdapter;
import fr.insa.helloeverybody.interfaces.ConversationListener;
import fr.insa.helloeverybody.models.Conversation;
import fr.insa.helloeverybody.models.ConversationMessage;
import fr.insa.helloeverybody.preferences.UserPreferencesActivity;
import fr.insa.helloeverybody.smack.XmppRoomManager;
import fr.insa.helloeverybody.viewmodels.ConversationList;

/* Activité qui affiche la liste les conversations publiques et en cours
-----------------------------------------------------------------------------*/
public class ConversationListActivity extends Activity implements ConversationListener {
	
	private final static int CREATE_PUBLIC_ROOM_REQUEST = 1;
	
	// Variables
	private ListView conversationsListView;
	private PublicRoomsDownwloader publicRoomsDownloader;
	private SeparatedConversationListAdapter conversationListAdapter;
	
    // Crée l'activité
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		ConversationList.getInstance().addConversationListener(this);
        publicRoomsDownloader = new PublicRoomsDownwloader();
        
        // Créer la vue
		setContentView(R.layout.conversations_list);
		updateConversationViews();
		
		// Rendre les conversations cliquables 
		final Intent intent = new Intent().setClass(this, ConversationActivity.class);
        conversationsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            	if (conversationListAdapter == null) {
            		return;
            	}
            	String roomName = conversationListAdapter.getStringId(position);
            	ConversationList conversationList = ConversationList.getInstance();
            	Conversation conversation = conversationList.getConversationByName(roomName);
            	
            	// Rejoindre la conversation si elle est publique
            	if (conversation.isPublic()) {
            		String roomSubject = conversation.getRoomSubject();
            		ConversationList.getInstance().removeConversation(roomName);
            		XmppRoomManager.getInstance().joinRoom(true, roomName, roomSubject);
            	} 
        		intent.putExtra(ConversationActivity.ROOM_NAME_EXTRA, roomName);
        		startActivity(intent);
        	}
         });
    }
    
    // Démarre l'activité
    @Override
    protected void onStart() {
    	super.onStart();
		publicRoomsDownloader.startDownloadPublicRooms();
    }

    // Arrête l'activité
    @Override
    protected void onStop() {
    	super.onStop();
		publicRoomsDownloader.stopDownloadPublicRooms();
    }
    
    // Reprend l'activité
    @Override
	protected void onResume() {
		super.onResume();
		updateConversationViews();
	}
    
    // Détruit l'activité
    @Override
	protected void onDestroy() {
		super.onDestroy();
    	ConversationList.getInstance().removeConversationListener(this);
    }

	// Créer le menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversations, menu);
 
        return true;
    }
 
    // Exécute l'action correspondant au bouton du menu cliqué
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {

     		// Ouvrir la fenêtre des paramètres
            case R.id.parameters:
            	final Intent settingsActivity = new Intent(getBaseContext(), UserPreferencesActivity.class);
                startActivity(settingsActivity);
                return true;
                
            // Créer un groupe publique
            case R.id.add_public_group:
                Toast.makeText(ConversationListActivity.this, "Création d'un groupe publique", Toast.LENGTH_SHORT).show();
                Intent inviteContactActivity = new Intent(getBaseContext(), InviteContactActivity.class);
                inviteContactActivity.putStringArrayListExtra("memberJidList", new ArrayList<String>());
                startActivityForResult(inviteContactActivity, CREATE_PUBLIC_ROOM_REQUEST);
                return true;

            // Quitter l'application
            case R.id.logout:
               finish();
               return true;
         }
         return false;
	}
    
    // Crée un salon public lorsque l'activité pour sélectionner les invités est finie
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	
    		// Créer un salon public
    		case CREATE_PUBLIC_ROOM_REQUEST :
    			if (resultCode != 0){
    				
    				// Récuperer la liste des invités
    				final ArrayList<String> selectedList = data.getStringArrayListExtra("toInvite");
    			
    				// Demander le nom du salon public
    				final Dialog dialog = new Dialog(ConversationListActivity.this);
    				dialog.setContentView(R.layout.request_room_name);
    				dialog.setTitle("Saisir le nom du groupe");
    				final EditText titleText = (EditText)(dialog.findViewById(R.id.title));
    	    	
    				// Créer le bouton qui créera le salon public
    				Button acceptButton = (Button)(dialog.findViewById(R.id.accept));
    				acceptButton.setOnClickListener(new View.OnClickListener() {
    					public void onClick(View v) {
    						String roomSubject = titleText.getText().toString();
    						if (roomSubject.equals("")) {
    							roomSubject = "No name";
    						}
    						XmppRoomManager.getInstance().createPublicRoom(roomSubject, selectedList);
    						dialog.dismiss();
    					}
    				});
    				dialog.show();
    			}
			break;
    	}
    }
    
    // Met à jour la vue les listes de conversation
    private void updateConversationViews() {
    	conversationListAdapter = new SeparatedConversationListAdapter(this);
		conversationsListView = (ListView) findViewById(R.id.conversations_list);
		conversationsListView.setAdapter(conversationListAdapter);
    }
	

    /* Implémentation de l'interface du listener des conversations
    -------------------------------------------------------------------------*/
    public void onConversationCreationFailed(String roomName) { }
    
	public void onConversationCreationSucceeded(String roomName) { }

	public void onConversationRemoved(String roomName) {
		runOnUiThread(new Runnable() {
			public void run() {
				updateConversationViews();
			}
		});
	}

	public void onMemberJoined(String roomName, String jid) {
		runOnUiThread(new Runnable() {
			public void run() {
				updateConversationViews();
			}
		});
	}

	public void onMemberLeft(String roomName, String jid) {
		runOnUiThread(new Runnable() {
			public void run() {
				updateConversationViews();
			}
		});
	}

	public void onMessageReceived(final String roomName, ConversationMessage newMessage) {
		runOnUiThread(new Runnable() {
			public void run() {
				updateConversationViews();
			}
		});
	}

	public void onInvitationRejected(String roomName, String jid) { }

	public void onPublicConversationAdded(String roomName) {
		runOnUiThread(new Runnable() {
			public void run() {
				updateConversationViews();
			}
		});
	}
}
