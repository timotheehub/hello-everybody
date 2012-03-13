package fr.insa.helloeverybody;

import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.ConversationsList;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.UserProfile;
import fr.insa.helloeverybody.smack.ChatService;
import fr.insa.helloeverybody.smack.InternalEvent;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class OnstartActivity extends Activity {
	
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
					
				// Partie test de la reception d'une invitation
				Handler invitationHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {

						InternalEvent ie = (InternalEvent) msg.obj;
						
						switch (ie.getMessageCode()) {
							case ChatService.EVT_INV_RCV:
								String roomName = ie.getRoomName().split("@")[0];
								String inviter = ((String) ie.getContent()).split("@")[0];
								ContactsList contactsList = ContactsList.getInstance();
								// Si le profil est le notre, celui qui a envoye l'invitation
								// n'existe pas dans la liste de contacts
								if(contactsList.getProfileByJid(inviter).isUser()) {
									Profile profile = mChatService.fetchProfile(inviter);
									profile.setJid(inviter);
									contactsList.addProfile(profile);
								}
								ConversationsList.getInstance().acceptConversation(roomName, inviter);
								break;
								
							case ChatService.EVT_CONN_OK:
								mChatService.saveProfile(UserProfile.getInstance().getProfile());
								Intent tabsActivity = new Intent(OnstartActivity.this, TabsActivity.class);
					            startActivity(tabsActivity);
								finish();
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
	}
}
