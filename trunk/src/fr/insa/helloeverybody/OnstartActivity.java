package fr.insa.helloeverybody;

import fr.insa.helloeverybody.contacts.ContactsListActivity;
import fr.insa.helloeverybody.conversations.ConversationActivity;
import fr.insa.helloeverybody.conversations.ConversationsListActivity;
import fr.insa.helloeverybody.helpers.ConversationsListener;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.ConversationMessage;
import fr.insa.helloeverybody.models.ConversationsList;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.UserProfile;
import fr.insa.helloeverybody.profile.ProfileActivity;
import fr.insa.helloeverybody.smack.ChatService;
import fr.insa.helloeverybody.smack.InternalEvent;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

public class OnstartActivity extends TabActivity implements ConversationsListener {
	
	public final static int CONVERSATION_ACTIVITY = 1;
	private static final int N_MESSAGE = 1;
	private static final int N_MEMBER = 2;
	
	private static View convTabView=null;
	
	ChatService mChatService;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getStart();	
	}
	
	public void launchMainActivity() {
		setContentView(R.layout.main);
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab
		View tabview; 
	
	
		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, ProfileActivity.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		tabview = createTabView(tabHost.getContext(), "Profil",false,0);
		spec = tabHost.newTabSpec("profil").setIndicator(tabview).setContent(intent);
    

		tabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, ContactsListActivity.class);
		tabview = createTabView(tabHost.getContext(), "Contacts",false,0);
		spec = tabHost.newTabSpec("contacts").setIndicator(tabview).setContent(intent);
    

		tabHost.addTab(spec);

		intent = new Intent().setClass(this, ConversationsListActivity.class);
	 
		tabview = createTabView(tabHost.getContext(), "Chats",true,ConversationsList.getInstance().getUnreadConversationscount());
		convTabView=tabview;
		spec = tabHost.newTabSpec("conversations").setIndicator(tabview).setContent(intent);
		tabHost.addTab(spec);				

		tabHost.setCurrentTab(this.getIntent().getIntExtra("tab", 1));
	}
	
	public void getStart() {
		ServiceConnection mConnection = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
				ConversationsList.getInstance().disconnectChat(mChatService);
				mChatService = null;
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				mChatService = ((ChatService.LocalBinder) service).getService();
				mChatService.askConnect();
				ConversationsList.getInstance().connectChat(mChatService);
					
				// Partie test de la reception d'une invitation
				Handler invitationHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {

						InternalEvent ie = (InternalEvent)msg.obj;
						final String roomName = ie.getRoomName();
						final String inviter = (String)ie.getContent();
						
						switch (ie.getMessageCode()) {
							case ChatService.EVT_INV_RCV:
								displayInviteDialog(roomName, inviter);
								break;
								
							case ChatService.EVT_CONN_OK:
								launchMainActivity();
								mChatService.saveProfile(UserProfile.getInstance().getProfile());
								break;
								
							case ChatService.EVT_CONN_NOK:
								//TODO: Changer le finish
								finish();
								break;
	
							default:
								break;
						}
					}
				};

				mChatService.addGeneralHandler(invitationHandler);
			}
		};

		// Le service ne peut pas être bind() depuis le contexte de l'activité
		getApplicationContext().bindService(new Intent(this, ChatService.class), mConnection, BIND_AUTO_CREATE);
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
	
	public static boolean setUnreadChats(int i){
		if(i>0){
		//	View tempView = getTabHost().getTabWidget().getChildTabViewAt(2);
			 //View view = LayoutInflater.from(getTabHost().getContext()).inflate(R.layout.tab, null);
			     TextView tv = (TextView) convTabView.findViewById(R.id.conv_number);
			     tv.setText(""+i);

		//	((TextView) getTabHost().getTabWidget().getChildAt(2).findViewById(android.R.id.title)).setText("test change text "+i);
			return true;
		}
		return false;
	}
	
	private void displayInviteDialog(final String room, final String jid){
		final Dialog dialog = new Dialog(OnstartActivity.this);
		dialog.setContentView(R.layout.invitation_dialog);
		dialog.setTitle("Nouvelle invitation");
		dialog.setCancelable(true);
		
		TextView text = (TextView) dialog.findViewById(R.id.textView1);
		final String name = jid.split("@")[0];
		final String roomName = room.split("@")[0];
		text.setText(name + " vous invite dans sa conversation : " + roomName);

		Button acceptButton = (Button) dialog.findViewById(R.id.button1);
		final Intent intent = new Intent().setClass(this, ConversationActivity.class);
		acceptButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				ContactsList contactsList = ContactsList.getInstance();
				// Si le profil est le notre, celui qui a envoye l'invitation
				// n'existe pas dans la liste de contacts
				if(contactsList.getProfileByJid(name).isUser()) {
					Profile profile = mChatService.fetchProfile(name);
					profile.setJid(name);
					contactsList.addProfile(profile);
				}
				ConversationsList.getInstance().acceptConversation(roomName,name);
        		intent.putExtra("id", roomName);
        		startActivityForResult(intent,CONVERSATION_ACTIVITY);
				dialog.dismiss();
			}
		});

		Button refuseButton = (Button) dialog.findViewById(R.id.button2);
		refuseButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				ConversationsList.getInstance().rejectConversation(roomName,name);
				dialog.dismiss();
			}
		});
		dialog.show();
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
	
	private static View createTabView(final Context context, final String text, boolean conv, int noConv) {
		    View view = LayoutInflater.from(context).inflate(R.layout.tab, null);
		    TextView tv = (TextView) view.findViewById(R.id.tabsText);
		    tv.setText(text);
		    if (conv&&noConv>0){
		    	TextView conv_num = (TextView) view.findViewById(R.id.conv_number);
		    	conv_num.setText(noConv+"");
		    	conv_num.setVisibility(TextView.VISIBLE);
		    } else{
		    	TextView conv_num = (TextView) view.findViewById(R.id.conv_number);

		    	conv_num.setVisibility(TextView.INVISIBLE);
		    }
		    return view;
	}

	public void conversationAdded(String roomName) {
		Intent mIntent = new Intent().setClass(this, ConversationActivity.class);
		mIntent.putExtra("id", roomName);
		startActivityForResult(mIntent, OnstartActivity.CONVERSATION_ACTIVITY);
	}

	public void conversationRemoved(String roomName) {
	}

	public void newMember(String roomName, String jid) {
		displayConversationNotification(N_MEMBER, "Nouveau membre"
			, "HelloEverybody", ContactsList.getInstance().getProfileByJid(jid).getFullName() 
			+ " a rejoint une conversation", roomName);
		
	}

	public void memberQuit(String roomName, String jid) {
	}

	public void rejectedInvitation(String roomName, String jid) {
	}

	public void newMessage(String roomName, ConversationMessage newMessage) {
		displayConversationNotification(N_MESSAGE, "Nouveaux messages"
				, "HelloEverybody", "Vous avez de nouveaux messages", roomName);
	}
}
