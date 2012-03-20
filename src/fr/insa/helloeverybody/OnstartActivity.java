package fr.insa.helloeverybody;

import fr.insa.helloeverybody.helpers.DatabaseContactHelper;
import fr.insa.helloeverybody.models.Contact;
import fr.insa.helloeverybody.models.ContactsList;
import fr.insa.helloeverybody.models.ConversationsList;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.ProfileType;
import fr.insa.helloeverybody.models.UserProfile;
import fr.insa.helloeverybody.smack.ChatService;
import fr.insa.helloeverybody.smack.InternalEvent;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class OnstartActivity extends Activity {
	public final static int TABS_ACTIVITY = 1;
	
	ServiceConnection mConnection;
	ChatService mChatService;
	Handler invitationHandler;
	private ProgressDialog loading;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Fenetre de chargement
		loading = ProgressDialog.show(OnstartActivity.this,
				"Chargement...", "Connexion en cours", true);
		getStart();	
	}
	
	public void getStart() {
		mConnection = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
				ConversationsList.getInstance().disconnectChat(mChatService);
				mChatService = null;
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				mChatService = ((ChatService.LocalBinder) service).getService();
				ConversationsList.getInstance().connectChat(mChatService);
					
				// Partie test de la reception d'une invitation
				invitationHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {

						InternalEvent ie = (InternalEvent) msg.obj;
						
						switch (ie.getMessageCode()) {
							case ChatService.EVT_INV_RCV:
								String roomName = ie.getRoomName().split("@")[0];
								String inviter = ((String) ie.getContent()).split("@")[0];
								Log.w("Réception Invitation", "Demande d'invitation : "+inviter);					
								ConversationsList.getInstance().acceptConversation(roomName, inviter);
								break;
								
							case ChatService.EVT_PUB_INV_RCV:
								String pubRoomName = ie.getRoomName().split("@")[0];
								String roomTitle = (String)ie.getContent();
								ConversationsList.getInstance().acceptPublicConversation(pubRoomName, roomTitle);
								break;
								
							case ChatService.EVT_CONN_OK:
								loading.dismiss();
								mChatService.saveProfile(UserProfile.getInstance().getProfile());
								Intent tabsActivity = new Intent(OnstartActivity.this, TabsActivity.class);
					            startActivityForResult(tabsActivity, TABS_ACTIVITY);
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
				mChatService.askConnect();
			}
		};
		// Le service ne peut pas être bind() depuis le contexte de l'activité
		getApplicationContext().bindService(new Intent(this, ChatService.class), mConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mChatService.removeGeneralHandler(invitationHandler);
		getApplicationContext().unbindService(mConnection);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
    	switch(requestCode) {
    		case TABS_ACTIVITY :
    			finish();
    			break;
			default:
				break;
    	}
	}
	
	
}
