package fr.insa.helloeverybody.smack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.BytestreamsProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.IBBProviders;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.pubsub.provider.EventProvider;
import org.jivesoftware.smackx.pubsub.provider.ItemProvider;
import org.jivesoftware.smackx.pubsub.provider.ItemsProvider;
import org.jivesoftware.smackx.pubsub.provider.PubSubProvider;
import org.jivesoftware.smackx.search.UserSearch;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.UserProfile;

public class ChatService extends Service {
	public static final String TAG = "ChatService";
	public static final Boolean DEBUG = true;
	
	/**
	 * L'ID CHAT_EVENT est reservé pour les événements de connection
	 */
	public static final int CHAT_EVENT = 100;
	
	/**
	 * Evenements généraux
	 */
	public static final InternalEvent EVT_CONNECTION_OK = new InternalEvent(null, "EVT-CONN-OK");
	public static final InternalEvent EVT_CONNECTION_DOWN = new InternalEvent(null, "EVT-CONN-NOK");
	public static final InternalEvent EVT_MESSAGE_RECEIVED = new InternalEvent(null, "EVT-MSG-RCV");
	
	/**
	 * Evenements liés à un salon de discussion
	 */
	public static final String EVT_NEW_ROOM = "EVT-NEW-ROOM";
	public static final String EVT_MSG_RCV = "EVT-MSG-RCV";
	public static final String EVT_MSG_SENT = "EVT-MSG-SENT";
	public static final String EVT_NEW_MEMBER = "EVT-NEW-MEMBER";
	public static final String EVT_MEMBER_QUIT = "EVT-MEMBER-QUIT";
	public static final String EVT_INV_REJ = "EVT-INV-REJ";
	
	/**
	 * Gestion de la connexion au serveur XMPP
	 */
	private ConnectionHelper mConnectionHelper;
	/**
	 * Gestion des contacts
	 */
	private RosterHelper mRosterHelper;
	/**
	 * Gestion des actions sur les chats
	 */
	private ChatHelper mChatHelper;
	
	/**
	 * Gère les tâches en background
	 */
	private PipelineThread mNetworkThread;
	
	/**
	 * Gestion des notifications pour les événements non propres à un chat
	 */
	private Set<Handler> mGeneralHandlerSet;
	
	private Profile userProfile;
	private final IBinder mBinder = new LocalBinder();
	
	/**
	 * PipelineThread permet de gérer une queue de tâche à exécuter sur ce thread
	 * 
	 */
	private class PipelineThread extends Thread {
		private Handler mHandler;
		
		@Override
		public void run() {
			Looper.prepare();
			
			//Handler qui va permettre l'exécution de Runnable dans ce thread
			mHandler = new Handler();
			
			Looper.loop();
		}
		
		public void enqueueRunnable(Runnable r) {
			mHandler.post(r);
		}
		
		public void stopThread() {
			mHandler.getLooper().quit();
		}
	}
	
	/*
	 * Outils de debug
	 */
	private void logIfDebug(String message) {
		if (DEBUG) {
			Log.d(TAG, message);
		}
	}
	
	/**
	 * A sort of patch from this thread:
	 * http://www.igniterealtime.org/community/thread/31118. Avoid
	 * ClassCastException by bypassing the classloading shit of Smack.
	 * 
	 * @param pm The ProviderManager.
	 */
	private void configure(ProviderManager pm) {
		Log.d(TAG, "configure");
		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());
		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Privacy
		// pm.addIQProvider("query", "jabber:iq:privacy", new
		// PrivacyProvider());
		// Delayed Delivery only the new version
		pm.addExtensionProvider("delay", "urn:xmpp:delay",
				new DelayInfoProvider());

		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());
		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Chat State
		ChatStateExtension.Provider chatState = new ChatStateExtension.Provider();
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates", chatState);
		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates", chatState);
		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates", chatState);
		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates", chatState);
		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates", chatState);
		// capabilities
		/*pm.addExtensionProvider("c", "http://jabber.org/protocol/caps",
				new CapsProvider());*/
		// Pubsub
		pm.addIQProvider("pubsub", "http://jabber.org/protocol/pubsub",
				new PubSubProvider());
		pm.addExtensionProvider("items", "http://jabber.org/protocol/pubsub",
				new ItemsProvider());
		pm.addExtensionProvider("items", "http://jabber.org/protocol/pubsub",
				new ItemsProvider());
		pm.addExtensionProvider("item", "http://jabber.org/protocol/pubsub",
				new ItemProvider());

		pm.addExtensionProvider("items",
				"http://jabber.org/protocol/pubsub#event", new ItemsProvider());
		pm.addExtensionProvider("item",
				"http://jabber.org/protocol/pubsub#event", new ItemProvider());
		pm.addExtensionProvider("event",
				"http://jabber.org/protocol/pubsub#event", new EventProvider());
		// TODO rajouter les manquants pour du full pubsub

		// PEP avatar
		/*pm.addExtensionProvider("metadata", "urn:xmpp:avatar:metadata",
				new AvatarMetadataProvider());
		pm.addExtensionProvider("data", "urn:xmpp:avatar:data",
				new AvatarProvider());*/

		// PEPProvider pep = new PEPProvider();
		// AvatarMetadataProvider avaMeta = new AvatarMetadataProvider();
		// pep.registerPEPParserExtension("urn:xmpp:avatar:metadata", avaMeta);
		// pm.addExtensionProvider("event",
		// "http://jabber.org/protocol/pubsub#event", pep);

		// ping
		/*pm.addIQProvider(PingExtension.ELEMENT, PingExtension.NAMESPACE,
				PingExtension.class);*/
		
		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());
		// Time
		try {
		    pm.addIQProvider("query", "jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
		    Log.w("TestClient", "Can't load class for org.jivesoftware.smackx.packet.Time");
		}
		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster", new RosterExchangeProvider());
		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event", new MessageEventProvider());
		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());
		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference", new GroupChatInvitation.Provider());
		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());
		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());
		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());
		// Version
		try {
		    pm.addIQProvider("query", "jabber:iq:version", Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
		    // Not sure what's happening here.
		    Log.w("TestClient", "Can't load class for org.jivesoftware.smackx.packet.Version");
		}
		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());
		// Offline Message Indicator
		pm.addExtensionProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());
		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup", "http://www.jivesoftware.org/protocol/sharedgroup",
		    new SharedGroupsInfo.Provider());
		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses", "http://jabber.org/protocol/address", new MultipleAddressesProvider());
		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());
		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
		pm.addIQProvider("open", "http://jabber.org/protocol/ibb", new IBBProviders.Open());
		pm.addIQProvider("close", "http://jabber.org/protocol/ibb", new IBBProviders.Close());
		pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb", new IBBProviders.Data());
	}
	
	/*
	 * Partie Notifications
	 */
	private void sendMessageToHandlers(Set<Handler> handlerSet, int id, Object message) {
		for (Iterator<Handler> iterator = handlerSet.iterator(); iterator.hasNext();) {
			Handler handler = (Handler) iterator.next();
			handler.obtainMessage(id, message).sendToTarget();
		}
	}
	
	private void broadcastGeneralMessage(Object message) {
		sendMessageToHandlers(mGeneralHandlerSet, CHAT_EVENT, message);
	}
	
	public void addGeneralHandler(Handler handler) {
		mGeneralHandlerSet.add(handler);
	}
	
	public void removeGeneralHandler(Handler handler) {
		mGeneralHandlerSet.remove(handler);
	}
	
	public void addChatHandler(String roomName, Handler handler) {
		mChatHelper.registrateHandlerToRoom(handler, roomName);
	}
	
	public void removeChatHandler(String roomName) {
		mChatHelper.unregistrateHandlerToRoom(roomName);
	}
	
	/*
	 * Partie Service
	 */
	public class LocalBinder extends Binder {
		public ChatService getService() {
            return ChatService.this;
        }
    }
	
	@Override
	public void onCreate () {
		super.onCreate();
		
		userProfile = UserProfile.getInstance().getProfile();
		
		mConnectionHelper = new ConnectionHelper();
		mNetworkThread = new PipelineThread();
		mGeneralHandlerSet = Collections.synchronizedSet(new HashSet<Handler>());
		
		this.configure(ProviderManager.getInstance());
		mNetworkThread.start();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
	    return START_STICKY;
	}
	
	@Override
	public void onDestroy () {
		super.onDestroy();
		mNetworkThread.stopThread();
	}

	@Override
	public IBinder onBind(Intent arg0) {		
		return mBinder;
	}
	
	/*
	 * Opérations en background
	 */
	public void askConnect() {
		mNetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				Boolean succes = false;
				
				if (mConnectionHelper.connect()) {
					if(mConnectionHelper.login(userProfile)) {
						mChatHelper = new ChatHelper(userProfile, mConnectionHelper);
						mRosterHelper = new RosterHelper(mConnectionHelper.getRoster());
						
						succes = true;
					} else if (mConnectionHelper.register(userProfile)) {
						succes = mConnectionHelper.login(userProfile);
						//mRosterHelper.rebuildRosterGroups();
					}
				}
				
				if (succes)
					broadcastGeneralMessage(EVT_CONNECTION_OK);
				else
					broadcastGeneralMessage(EVT_CONNECTION_DOWN);
				
				logIfDebug("AskConnect : " + succes);
			}
		});
	}
	
	public void askDisconnect() {
		mNetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				mConnectionHelper.disconnect();
			}
		});
	}
	
	public void sendMessage(final String roomName, final String toSendMessage) {
		mNetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				mChatHelper.sendMessageToRoom(roomName, toSendMessage);
			}
		});
	}
	
	public void createNewConversation() {
		mNetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				String roomName = mChatHelper.createRoom();
				
				if (roomName != null) {
					broadcastGeneralMessage(new InternalEvent(roomName, EVT_NEW_ROOM));
				}
				
				logIfDebug("New room created : " + roomName);
			}
		});
	}
	
	public void inviteToConversation(final String roomName, final String jid) {
		mNetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				if (roomName != null) {
					mChatHelper.inviteUserToRoom(roomName, jid);
				}
				
				logIfDebug("Invite : " + jid);
			}
		});
	}
	
	public void leaveConversation(final String roomName){
		mNetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				if (roomName != null) {
					mChatHelper.leaveRoom(roomName);
				}
				
				logIfDebug("Leave the room : " + roomName);
			}
		});
	}
	
	public void rejectInvitation(final String roomName, final String inviter){
		mNetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				if (roomName != null) {
					MultiUserChat.decline(mConnectionHelper.getXMPPConnection(), roomName + "@" + mConnectionHelper.getConferenceServer(), inviter, null);
				}
				
				logIfDebug("Reject the invitation to : " + roomName + "from : " + inviter);
			}
		});
		
	}
	/*
	 * Autres opérations
	 */
	
}
