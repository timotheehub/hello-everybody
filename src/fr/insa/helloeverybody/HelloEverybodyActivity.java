package fr.insa.helloeverybody;

import java.util.List;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.Toast;
import fr.insa.helloeverybody.communication.ChatService;
import fr.insa.helloeverybody.contacts.ContactsListActivity;
import fr.insa.helloeverybody.conversations.ConversationActivity;
import fr.insa.helloeverybody.conversations.ConversationsListActivity;
import fr.insa.helloeverybody.models.Conversation;
import fr.insa.helloeverybody.models.ConversationMessage;
import fr.insa.helloeverybody.models.ConversationsList;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.profile.ProfileActivity;

public class HelloEverybodyActivity extends TabActivity {
	public final static int CONVERSATION_LAUCHED = 1;
	public final static int DECONNECTION = 2;

	// test
	/** Modèles */
	private List<Conversation> pendingConversations;

	/** Instances pour les tests */
	public static Profile userProfil;
	private Profile bob;
	private Conversation conversation1;
	private Conversation conversation2;
	private Conversation conversation3;

	/**
	 * private final Handler mHandler=new Handler(){
	 * 
	 * @Override public void handleMessage(Message msg){ switch(msg.what){ case
	 *           1: ConversationsList.getInstance().addConversationMessage(
	 *           conversation1.getId(), bob.getId(), msg.obj.toString()); break;
	 *           case 2: ConversationsList.getInstance().addConversationMessage(
	 *           conversation1.getId(), userProfil.getId(), msg.obj.toString());
	 *           break; default: break; }
	 * 
	 *           } };
	 */

	// private ChatService mChatService=null;
	//

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// Test - START

		// Création du profil de l'utilisateur
		pendingConversations = ConversationsList.getInstance().getPendingList();
		userProfil = new Profile();
		userProfil.setAvatar(R.drawable.default_profile_icon);
		userProfil.setFirstName("Moi");
		userProfil.setUser(true);
		userProfil.setJid("vincenttest");

		bob = new Profile();
		bob.setAvatar(R.drawable.sponge_bob);
		bob.setLastName("L'Eponge)");
		bob.setFirstName("Bob");
		bob.setUser(false);

		ConversationMessage message1 = new ConversationMessage();
		message1.setContact(bob);
		message1.setMessage("Hello World !");

		conversation1 = new Conversation();
		conversation1.addMember(userProfil);
		conversation1.addMember(bob);
		conversation1.addMessage(message1);
		conversation1.setTitle("Bob et Moi");
		conversation2 = new Conversation();
		conversation2.setTitle("Roger et Moi");
		conversation3 = new Conversation();
		conversation3.setTitle("Jean-Louis et Moi");
		pendingConversations.add(conversation1);
		pendingConversations.add(conversation2);
		pendingConversations.add(conversation3);
		
		//ChatService.GetChatService().doLogin("hello.everybody.app@gmail.com", "insalyonSIMP");
		//Toast.makeText(HelloEverybodyActivity.this, "Connexion établie", Toast.LENGTH_SHORT).show();

		// mChatService=new
		// ChatService(mHandler,"talk.google.com",5222,"gmail.com");
		// mChatService.doLogin("hello.everybody.app@gmail.com","insalyonSIMP");
		// mChatService=new
		// ChatService(mHandler,"im.darkserver.eu.org",5222,null);
		// mChatService.doLogin("test", "test");

		// Test - END

		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, ProfileActivity.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("profil").setIndicator("Profil")
				.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, ContactsListActivity.class);
		spec = tabHost.newTabSpec("contacts").setIndicator("Contacts")
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, ConversationsListActivity.class);
		spec = tabHost.newTabSpec("conversations")
				.setIndicator("Conversations").setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(1);

	}

}