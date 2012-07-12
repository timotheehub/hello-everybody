package fr.insa.helloeverybody;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import fr.insa.helloeverybody.smack.InternalEvent;
import fr.insa.helloeverybody.smack.XmppConnectionManager;
import fr.insa.helloeverybody.smack.XmppContactsManager;
import fr.insa.helloeverybody.smack.XmppEventsManager;
import fr.insa.helloeverybody.smack.XmppService;

public class OnstartActivity extends Activity {
	public final static int TABS_ACTIVITY = 1;
	
	private ServiceConnection mConnection;
	private Handler invitationHandler;
	private ProgressDialog loading;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getStart();	
	}
	
	@Override
	public void onStart() {
		super.onStart();
		loading = ProgressDialog.show(OnstartActivity.this,
				"Chargement...", "Connexion en cours", true);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		loading.dismiss();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		XmppEventsManager.getInstance().removeGeneralHandler(invitationHandler);
 		XmppConnectionManager.getInstance().askDisconnect();
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
	
	public void getStart() {
		 mConnection = new ServiceConnection() {
			 public void onServiceDisconnected(ComponentName name) { }
			 
             public void onServiceConnected(ComponentName name, IBinder service) {      
                     invitationHandler = new Handler() {
                             @Override
                             public void handleMessage(Message msg) {

                                     InternalEvent ie = (InternalEvent) msg.obj;
                                     
                                     switch (ie.getMessageCode()) {
	                                 	case XmppEventsManager.EVT_CONN_OK:
	                 						XmppContactsManager.saveLocalProfile();
	                 						Intent tabsActivity = new Intent(OnstartActivity.this, TabsActivity.class);
	                 			            startActivityForResult(tabsActivity, TABS_ACTIVITY);
	                 						break;
	                 						
	                                 	case XmppEventsManager.EVT_CONN_NOK:
	                 						// TODO(fonctionnalité): Changer le finish
	                 						finish();
                                     }
                             }
                     };

             		XmppEventsManager.getInstance().addGeneralHandler(invitationHandler);
             		XmppConnectionManager.getInstance().askConnect();
             }
		 };
     
		 // Binder le service
		 getApplicationContext().bindService(
    		 new Intent(this, XmppService.class), mConnection, BIND_AUTO_CREATE);
	}
	
	
}
