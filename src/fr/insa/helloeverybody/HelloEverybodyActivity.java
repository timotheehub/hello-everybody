package fr.insa.helloeverybody;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import fr.insa.helloeverybody.contacts.ContactsListActivity;
import fr.insa.helloeverybody.conversations.ConversationsListActivity;
import fr.insa.helloeverybody.device.DeviceHelper;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.ConversationsList;
import fr.insa.helloeverybody.models.Database;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.UserProfile;
import fr.insa.helloeverybody.profile.ImageSaver;
import fr.insa.helloeverybody.profile.ProfileActivity;

public class HelloEverybodyActivity extends TabActivity {
	public final static int CONVERSATION_LAUCHED = 1;
	public final static int DECONNECTION = 2;
	private static View convTabView=null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * Initialisation DB
		 */
		Database.getInstance().initDatabase(this.getApplicationContext());
		ContactsList.getInstance().initContactsList(this.getApplicationContext());
		
		UserProfile userProfile = UserProfile.getInstance();		
		userProfile.retrieve(0L);
		if (userProfile.getProfile() == null) {
			Profile profile = new Profile();
			profile.setAvatar(ImageSaver.getAvatar());
			profile.setFirstName("Julian");
			profile.setJid(new DeviceHelper(getApplicationContext()).getPhoneImei());
			profile.setPassword("test");
			userProfile.setProfile(profile);
			userProfile.saveProfile();
		}

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
	    
		//spec = tabHost.newTabSpec("profil").setIndicator("Profil")
		//		.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, ContactsListActivity.class);
		tabview = createTabView(tabHost.getContext(), "Contacts",false,0);
	    spec = tabHost.newTabSpec("contacts").setIndicator(tabview).setContent(intent);
	    
		//spec = tabHost.newTabSpec("contacts").setIndicator("Contacts")
		//		.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, ConversationsListActivity.class);
		 
		tabview = createTabView(tabHost.getContext(), "Chats",true,ConversationsList.getInstance().getUnreadConversationscount());
		convTabView=tabview;
	    spec = tabHost.newTabSpec("conversations").setIndicator(tabview).setContent(intent);
	    tabHost.addTab(spec);
				
	//	spec = tabHost.newTabSpec("conversations")
	//			.setIndicator("Conversations").setContent(intent);
	//	tabHost.addTab(spec);

	    		
		//this.getIntent().getIntExtra("tab", 0);
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