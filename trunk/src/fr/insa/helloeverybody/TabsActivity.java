package fr.insa.helloeverybody;

import fr.insa.helloeverybody.contacts.ContactsListActivity;
import fr.insa.helloeverybody.conversations.ConversationActivity;
import fr.insa.helloeverybody.conversations.ConversationsListActivity;
import fr.insa.helloeverybody.helpers.ConversationsListener;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.ConversationMessage;
import fr.insa.helloeverybody.models.ConversationsList;
import fr.insa.helloeverybody.profile.ProfileActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

public class TabsActivity extends TabActivity implements ConversationsListener {
	
	public final static int CONVERSATION_ACTIVITY = 1;
	private static final int N_MESSAGE = 1;
	private static final int N_MEMBER = 2;
	
	private static View convTabView=null;
	
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
						true, ConversationsList.getInstance().getUnreadConversationscount());
		convTabView=tabview;
		spec = tabHost.newTabSpec("conversations").setIndicator(tabview).setContent(intent);
		tabHost.addTab(spec);				

		tabHost.setCurrentTab(this.getIntent().getIntExtra("tab", 1));
		
		ConversationsList.getInstance().addConversationsListener(this);
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		ContactsList.getInstance().destroyContactsList();
		ConversationsList.getInstance().removeConversationsListener(this);
	}
	
	public int getTab(){
		return getTabHost().getCurrentTab();
	}
	
	public static boolean updateUnreadChats() {
	    TextView tv = (TextView) convTabView.findViewById(R.id.conv_number);
	    if (tv == null) {
	    	return false;
	    }
	    
	    int nbUnread = ConversationsList.getInstance().getUnreadConversationscount();
	    
		if (nbUnread > 0) {
		    tv.setText(String.valueOf(nbUnread));
		    return true;
		}
		
		tv.setText("");
		return false;
	}
	
	
	private void displayConversationNotification(int type, String tickerText, String title, String text, String roomName){
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		long now = System.currentTimeMillis();
		int icon = R.drawable.ic_launcher;
		Notification notification = new Notification(icon, tickerText, now);
		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(this, ConversationActivity.class);
		notificationIntent.putExtra("id", roomName );
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, title, text, contentIntent);
		nm.notify(type, notification);
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

	public void creationConversationFailed() {
		// Inutilisé
	}
	
	public void conversationAdded(String roomName) {
		if (ConversationActivity.getActiveConversation()==null) {
			Intent mIntent = new Intent().setClass(this, ConversationActivity.class);
			mIntent.putExtra("id", roomName);
			startActivityForResult(mIntent, TabsActivity.CONVERSATION_ACTIVITY);
		}
	}

	public void conversationRemoved(String roomName) {
	}

	public void newMember(String roomName, String jid) {
		if (ConversationActivity.getActiveConversation()==null) {
			displayConversationNotification(N_MEMBER, "Nouveau membre"
				, "HelloEverybody", ContactsList.getInstance().getProfileByJid(jid).getFullName() 
				+ " a rejoint une conversation", roomName);
		}
	}

	public void memberQuit(String roomName, String jid) {
	}

	public void rejectedInvitation(String roomName, String jid) {
	}

	public void newMessage(String roomName, ConversationMessage newMessage) {
		if (ConversationActivity.getActiveConversation()==null) {
			displayConversationNotification(N_MESSAGE, "Nouveaux messages"
					, "HelloEverybody", "Vous avez de nouveaux messages", roomName);
		}
	}

	public void conversationPublicAdded(String roomName) {
	}
}
