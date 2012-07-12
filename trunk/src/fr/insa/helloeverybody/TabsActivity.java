package fr.insa.helloeverybody;

import fr.insa.helloeverybody.contacts.ContactsListActivity;
import fr.insa.helloeverybody.conversations.ConversationActivity;
import fr.insa.helloeverybody.conversations.ConversationsListActivity;
import fr.insa.helloeverybody.interfaces.ConversationListener;
import fr.insa.helloeverybody.models.ConversationMessage;
import fr.insa.helloeverybody.profile.ProfileActivity;
import fr.insa.helloeverybody.viewmodels.ContactsList;
import fr.insa.helloeverybody.viewmodels.ConversationsList;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class TabsActivity extends TabActivity implements ConversationListener {
	
	public final static int CONVERSATION_ACTIVITY = 1;
	
	private static View convTabView = null;
	
	TabHost tabHost;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		displayTabs();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateUnreadChats();
	}
	
	public void displayTabs() {
		TabSpec spec; 
		Intent intent;
		View tabview; 
		
		setContentView(R.layout.main); 
		tabHost = getTabHost();
	
		// Profil
		intent = new Intent().setClass(this, ProfileActivity.class);
		tabview = createTabView(tabHost.getContext(), "Profil",
						R.drawable.profile_selector, false, 0);
		spec = tabHost.newTabSpec("profil").setIndicator(tabview).setContent(intent);
		tabHost.addTab(spec);
		
		// Contacts
		intent = new Intent().setClass(this, ContactsListActivity.class);
		tabview = createTabView(tabHost.getContext(), "Contacts",
						R.drawable.contacts_selector, false, 0);
		spec = tabHost.newTabSpec("contacts").setIndicator(tabview).setContent(intent);
		tabHost.addTab(spec);

		// Conversation
		intent = new Intent().setClass(this, ConversationsListActivity.class);
		tabview = createTabView(tabHost.getContext(), "Messages", R.drawable.messages_selector,
						true, ConversationsList.getInstance().getUnreadConversationCount());
		convTabView=tabview;
		spec = tabHost.newTabSpec("conversations").setIndicator(tabview).setContent(intent);
		tabHost.addTab(spec);				

		tabHost.setCurrentTab(this.getIntent().getIntExtra("tab", 1));
		
		ConversationsList.getInstance().addConversationListener(this);
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		ContactsList.getInstance().destroyContactsList();
		ConversationsList.getInstance().removeConversationListener(this);
	}
	
	public int getTab(){
		return getTabHost().getCurrentTab();
	}
	
	public boolean updateUnreadChats() {
	    TextView tv = (TextView) convTabView.findViewById(R.id.conv_number);
	    if (tv == null) {
	    	return false;
	    }
	    
	    int nbUnread = ConversationsList.getInstance().getUnreadConversationCount();
	    
		if (nbUnread > 0) {
		    tv.setText(String.valueOf(nbUnread));
		    return true;
		}
		
		tv.setText("");
		return false;
	}
	
	private static View createTabView(final Context context, final String text, 
					final int drawableId, boolean conv, int nbConversations) {
		    View view = LayoutInflater.from(context).inflate(R.layout.tab, null);
		    
		    // Texte et image
		    TextView tv = (TextView) view.findViewById(R.id.tabsText);
		    tv.setText(text);
		    ImageView iconView = (ImageView) view.findViewById(R.id.tabsIcon);
		    iconView.setImageResource(drawableId);
		    
		    // Nombre de conversations
	    	TextView conv_num = (TextView) view.findViewById(R.id.conv_number);
		    if (conv && nbConversations > 0){
		    	conv_num.setText(String.valueOf(nbConversations));
		    	conv_num.setVisibility(TextView.VISIBLE);
		    } else{
		    	conv_num.setVisibility(TextView.INVISIBLE);
		    }
		    
		    return view;
	}

	
	/* Implémentation de l'interface des listeners de conversations
	-------------------------------------------------------------------------*/
	public void onCreationConversationFailed() { }
	
	// TODO(architecture): Déplacer le code
	public void onPendingConversationAdded(String roomName) {
		if (ConversationActivity.getCurrentRoomName() == null) {
			Intent mIntent = new Intent().setClass(this, ConversationActivity.class);
			mIntent.putExtra(ConversationActivity.ROOM_NAME_EXTRA, roomName);
			startActivityForResult(mIntent, TabsActivity.CONVERSATION_ACTIVITY);
		}
	}

	public void onPublicConversationAdded(String roomName) { }

	public void onConversationRemoved(String roomName) { }

	public void onMemberJoined(String roomName, String jid) { }

	public void onMemberLeft(String roomName, String jid) { }

	public void onInvitationRejected(String roomName, String jid) { }

	public void onMessageReceived(String roomName, ConversationMessage newMessage)
	{
		// TODO(fonctionnalité): Afficher le nombre de messages non lus
	}
}
