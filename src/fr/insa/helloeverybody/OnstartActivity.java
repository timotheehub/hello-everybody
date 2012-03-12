package fr.insa.helloeverybody;

import fr.insa.helloeverybody.contacts.ContactsListActivity;
import fr.insa.helloeverybody.conversations.ConversationActivity;
import fr.insa.helloeverybody.conversations.ConversationsListActivity;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.ConversationsList;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.profile.ProfileActivity;
import fr.insa.helloeverybody.smack.ChatService;
import fr.insa.helloeverybody.smack.InternalEvent;
import android.app.Dialog;
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

public class OnstartActivity extends TabActivity{
	
	public final static int CONVERSATION_ACTIVITY = 1;
	
	private static View convTabView=null;
	
	ChatService mChatService;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getStart();	
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
							displayInviteDialog(roomName, inviter);
						}
					}
				};

				mChatService.addGeneralHandler(invitationHandler);
			}
		};

		// Le service ne peut pas être bind() depuis le contexte de l'activité
		getApplicationContext().bindService(new Intent(this, ChatService.class), mConnection, BIND_AUTO_CREATE);

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
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		ContactsList.getInstance().destroyContactsList();
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
}
