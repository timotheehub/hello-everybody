package fr.insa.helloeverybody.contacts;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.device.DeviceHelper;
import fr.insa.helloeverybody.helpers.FilterTextWatcher;
import fr.insa.helloeverybody.helpers.SeparatedContactsListAdapter;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.ConversationsList;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.UserProfile;
import fr.insa.helloeverybody.preferences.UserPreferencesActivity;
import fr.insa.helloeverybody.smack.ChatService;
import fr.insa.helloeverybody.smack.InternalEvent;

public class ContactsListActivity extends Activity implements ContactsCallbackInterface {
	private ContactsActions contactsActions;
	private Profile profile;
	private ProgressDialog loading;
	
	private ListView contactsListView;
	private EditText filterText;
	private FilterTextWatcher filterTextWatcher;
	
	ChatService mChatService;
	
    // Appel a la creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.contacts_list);
        
        // Vide les listes de profiles
        ContactsList contactsList = ContactsList.getInstance();
        contactsList.clearAllLists();
        
        // Recuperation du profil utilisateur
        profile = UserProfile.getInstance().getProfile();
        
        // Créer un listener sur le filtre
        filterTextWatcher = new FilterTextWatcher();
        filterText = (EditText) findViewById(R.id.search_box);
        filterText.addTextChangedListener(filterTextWatcher);
        
        
        // Création du gestionnaire des actions
        contactsActions = ContactsActions.getInstance(this, profile);
        contactsActions.register(this);
        contactsActions.askUpdateContacts();
        
		ServiceConnection mConnection = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
				ConversationsList.getInstance().disconnectChat(mChatService);
				mChatService = null;
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				mChatService = ((ChatService.LocalBinder) service).getService();
				mChatService.askConnect();
				ConversationsList.getInstance().connectChat(mChatService);
				
				/*if (new DeviceHelper(getApplicationContext()).getPhoneImei().equals("353509030078441")) {
					mChatService.createNewConversation();
					//Téléphone Vincent
					mChatService.inviteToConversation("3535090300784411", "test");
					
					Handler h = new Handler() {
						@Override
						public void handleMessage(Message androidMessage) {
							InternalEvent ie = (InternalEvent)androidMessage.obj;
							org.jivesoftware.smack.packet.Message smackMsg = null;
							
							if (ie.getContent().getClass().equals(org.jivesoftware.smack.packet.Message.class))
								smackMsg  = (org.jivesoftware.smack.packet.Message)ie.getContent();
							
							if (ie.getMessageCode() == ChatService.EVT_MSG_RCV && smackMsg.getFrom().split("/")[1].equalsIgnoreCase("test")) {
								mChatService.sendMessage("3535090300784411", smackMsg.getBody());
							}
							
							Log.d("TEST", ie.getRoomName() + " " + ie.getMessageCode());
						}
					};
					
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					mChatService.addChatHandler("3535090300784411", h);
				}*/
					
				// Partie test de la reception d'une invitation
				Handler invitationHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {

						InternalEvent ie = (InternalEvent)msg.obj;
						final String roomName = ie.getRoomName();
						final String inviter = (String)ie.getContent();

						if (ie.getMessageCode() == ChatService.EVT_INV_RCV) {
							final Dialog dialog = new Dialog(ContactsListActivity.this);
							dialog.setContentView(R.layout.invitation_dialog);
							dialog.setTitle(inviter + " vous invite dans sa conversation : " + roomName);
							dialog.setCancelable(true);

							Button acceptButton = (Button) dialog.findViewById(R.id.button1);
							acceptButton.setOnClickListener(new OnClickListener(){
								public void onClick(View v){
									mChatService.joinIntoConversation(roomName);
									dialog.dismiss();
								}
							});

							Button refuseButton = (Button) dialog.findViewById(R.id.button2);
							refuseButton.setOnClickListener(new OnClickListener(){
								public void onClick(View v){
									mChatService.rejectInvitation(roomName, inviter);
									dialog.dismiss();
								}
							});

							dialog.show();

						}
					}
				};

				mChatService.addGeneralHandler(invitationHandler);
			}
		};

		// Le service ne peut pas être bind() depuis le contexte de l'activité
		getApplicationContext().bindService(new Intent(this, ChatService.class), mConnection, BIND_AUTO_CREATE);

		// Fenetre de chargement
		loading = ProgressDialog.show(ContactsListActivity.this,
				"Chargement...", "Récupération des contacts", true);
	}
    
    // Appel a l'affichage
    @Override
    public void onResume() {
    	super.onResume();
    	if (loading.isShowing() == false) {
    		updateContactsView();
    	}
    }
    
    // Appel lors de la destruction de l'activite par le systeme
    @Override
	protected void onDestroy() {
    	contactsActions.stopScheduledUpdate();
    	filterText.removeTextChangedListener(filterTextWatcher);
		super.onDestroy();
	}
    
    // Mettre a jour la liste de contacts
	public void contactsListUpdated(ArrayList<Profile> profilesList) {
		loading.dismiss();
		//Lancement les timers GPS
		contactsActions.contactsReceived();
        contactsActions.launchScheduledUpdate();
        
        ContactsList contactsList = ContactsList.getInstance();
        contactsList.clearAllLists();
		for (Profile profile : profilesList) {
			contactsList.addProfile(profile);
		}
		
		// Ajoute des faux contacts
		fillFakeList();
		
		// Affiche la liste des contacts
		updateContactsView();
		
		// Ajoute un click aux items
		final Intent intent = new Intent().setClass(this, ContactProfileActivity.class);
		contactsListView = (ListView) findViewById(R.id.contacts_list);
		contactsListView.setTextFilterEnabled(true);
		
		contactsListView.setOnItemClickListener(new OnItemClickListener() {
        	@SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        		intent.putExtra("id", adapter.getItemIdAtPosition(position));
        		
        		startActivity(intent);
        	}
         });
	}

	// Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone
    public boolean onCreateOptionsMenu(Menu menu) {
 
        // Creation d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        
        // Instanciation du menu XML specifier en un objet Menu
        inflater.inflate(R.menu.contacts, menu);
 
        return true;
    }
    
	// Méthode qui se déclenchera au clic sur un item
	public boolean onOptionsItemSelected(MenuItem item) {
         // On regarde quel item a ete clique grace a son id et on declenche une action
         switch (item.getItemId()) {
            case R.id.parameters:
            	// Ouvrir la fenetre des parametres
               final Intent settingsActivity = new Intent(getBaseContext(), UserPreferencesActivity.class);
               startActivity(settingsActivity);
               return true;
            case R.id.logout:
            	// Déconnexion et quitter l'application
               finish();
               return true;
         }
         return false;
	}
	
	// Remplit les différentes listes de contacts
	private void updateContactsView() {

		final SeparatedContactsListAdapter listAdapter = new SeparatedContactsListAdapter(this);
		filterText.setText("");
		
		// Mettre a jour la ListView
		contactsListView = (ListView) findViewById(R.id.contacts_list);
		contactsListView.setAdapter(listAdapter);
        filterTextWatcher.setAdapter(listAdapter);
	}
	
	// Remplit la liste de favoris
	private void fillFakeList() {
        ContactsList contactsList = ContactsList.getInstance();
        
        // Favoris
        contactsList.addProfile(new Profile(R.drawable.default_profile_icon, 
								"Arthur", "M.", true, false, false));
		Profile bobProfile = new Profile(R.drawable.sponge_bob,
								"Bob", "L'éponge", true, true, true);
		bobProfile.setAge(25);
		bobProfile.getInterestsList().add("Pêche à la méduse");
		bobProfile.getInterestsList().add("Karaté");
		bobProfile.getInterestsList().add("Bulles de savon");
		bobProfile.setJid("test");
		contactsList.addProfile(bobProfile);
	    contactsList.addProfile(new Profile(R.drawable.default_profile_icon,
								"Patrick", "L'étoile de mer", true, false, true));
	    contactsList.addProfile(new Profile(R.drawable.default_profile_icon,
								"Timothée", "L.", true, true, false));
	    
	    // Recents
		contactsList.addProfile(new Profile(R.drawable.default_profile_icon,
								"Julian", "Dos Santos", false, true, false));
		contactsList.addProfile(new Profile(R.drawable.default_profile_icon,
								"Vincent", "B.", false, true, true));
		
		// Recommandes
		contactsList.addProfile(new Profile(R.drawable.default_profile_icon,
								"Li Chen", "T.", false, false, true));
		contactsList.addProfile(new Profile(R.drawable.default_profile_icon,
								"Loïc", "T.", false, false, true));
		contactsList.addProfile(new Profile(R.drawable.default_profile_icon,
								"Raphaël", "Corral", false, false, true));
	}
}