package fr.insa.helloeverybody.smack;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.bytestreams.ibb.provider.CloseIQProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.OpenIQProvider;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.AttentionExtension;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.Nick;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.HeadersProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.pubsub.provider.AffiliationProvider;
import org.jivesoftware.smackx.pubsub.provider.AffiliationsProvider;
import org.jivesoftware.smackx.pubsub.provider.ConfigEventProvider;
import org.jivesoftware.smackx.pubsub.provider.EventProvider;
import org.jivesoftware.smackx.pubsub.provider.FormNodeProvider;
import org.jivesoftware.smackx.pubsub.provider.ItemProvider;
import org.jivesoftware.smackx.pubsub.provider.ItemsProvider;
import org.jivesoftware.smackx.pubsub.provider.PubSubProvider;
import org.jivesoftware.smackx.pubsub.provider.RetractEventProvider;
import org.jivesoftware.smackx.pubsub.provider.SimpleNodeProvider;
import org.jivesoftware.smackx.pubsub.provider.SubscriptionProvider;
import org.jivesoftware.smackx.pubsub.provider.SubscriptionsProvider;
import org.jivesoftware.smackx.search.UserSearch;

import fr.insa.helloeverybody.helpers.LogWriter;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.viewmodels.LocalUserProfile;

import android.util.Log;

/* Gère les connections au serveur XMPP
-----------------------------------------------------------------------------*/
public class XmppConnectionManager {
	
	// Tag pour les exceptions
	public static final String TAG = "XmppConnectionManager";
	
	// Adresse du serveur qui gère les contacts
	public static final String SERVER_ADDRESS = "im.darkserver.eu.org";
	
	// Adresse du serveur qui gère les salons de conversation
	public static final String CONFERENCE_SERVER_ADDRESS = 
								"conference.im.darkserver.eu.org";
	
	// Port de connection
	public static final Integer PORT = 5222;
	
	// Nom de la ressource sur le serveur XMPP
	public static final String RESOURCE = "HE-Mobile";
	
	// Nombre d'essais de connections au serveur
	private static final Integer MAX_CONNECTION_TRIES = 3;
	
	// Singleton
	private static XmppConnectionManager instance = null; 
	
	// Variables
	private XMPPConnection mXMPPConnection;
	
	// Constructeur privé
	private XmppConnectionManager() {
		configureProvideManager();
		configureServiceDiscoveryManager();
		ConnectionConfiguration connectionConfig = 
				new ConnectionConfiguration(SERVER_ADDRESS, PORT, RESOURCE);
		mXMPPConnection = new XMPPConnection(connectionConfig);
		mXMPPConnection.getRoster().setSubscriptionMode(SubscriptionMode.accept_all);
	}
	
	// Retourne le singleton
	public static synchronized XmppConnectionManager getInstance() {
		if (instance == null) {
			instance = new XmppConnectionManager();
		}
		return instance;
	}
	
	// Retourne vrai si on est connecté au serveur XMPP
	public boolean isConnected() {
		return mXMPPConnection.isConnected();
	}
	
	// Retourne vrai si on est authentifié sur le serveur XMPP
	public boolean isAuthenticated() {
		return mXMPPConnection.isAuthenticated();
	}
	
	// Retourne la connection XMPP
	public XMPPConnection getXmppConnection() {
		return mXMPPConnection;
	}
	
	// Retourne la liste de contacts
	public Roster getRoster() {
		return mXMPPConnection.getRoster();
	}
	
	// Demande de se connecter au serveur XMPP
	public void askConnect() {
		NetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				XmppEventsManager xmppEvents = XmppEventsManager.getInstance();
				Boolean success = false;
				int nbTries = 0;
				
				while (!success && (nbTries < MAX_CONNECTION_TRIES)) {
					if (tryXmppConnect()
						&& (tryXmppLogin()
							|| (tryXmppRegister() && tryXmppLogin()))) {
						XmppContactsManager.createNearMeGroup();
						xmppEvents.addInvitationListener(mXMPPConnection);
						xmppEvents.broadcastGeneralMessage(XmppEventsManager.EVT_CONNECTION_OK);
						success = true;
					}
					
					nbTries++;
					LogWriter.logIfDebug(TAG, "askConnect : " + success + ", try no" + nbTries);
				}
				
				if (!success) {
					xmppEvents.broadcastGeneralMessage(XmppEventsManager.EVT_CONNECTION_DOWN);
				}
			}
		});
	}
	
	// Demande de se déconnecter du serveur XMPP.
	public void askDisconnect() {
		NetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				XmppEventsManager xmppEvents = XmppEventsManager.getInstance();
				xmppEvents.removeInvitationListener(mXMPPConnection);
				xmppEvents.removeRosterListener();
				xmppDisconnect();
				LogWriter.logIfDebug(TAG, "Disconnected");
			}
		});
	}
	
	// Essaie de se connecter au serveur XMPP
	private boolean tryXmppConnect() {
		// Retourne vrai si on est déjà connecté
		if (mXMPPConnection.isConnected()) {
			return true;
		}
		
		// Essaye de se connecter
		try {
			mXMPPConnection.connect();
			return true;
		} catch (XMPPException e) {
			Log.d(TAG, e.getMessage(), e);
			return false;
		}
	}
	
	// Se déconnecte du serveur XMPP
	private void xmppDisconnect() {
		mXMPPConnection.disconnect();
	}
		
	// Essaye de s'authentifier
	private boolean tryXmppLogin() {
		// Vérifie qu'on est connecté
		if (!mXMPPConnection.isConnected()) {
			return false;
		}
		
		// Retourne vrai si on est déjà authentifié
		if (mXMPPConnection.isAuthenticated()) {
			return true;
		}
		
		// Récupère le profile utilisation
		Profile localUserProfile = LocalUserProfile.getInstance().getProfile();
		if (localUserProfile == null) {
			Log.d(TAG, "Local user profile is null");
			return false;
		}
		
		// Essaye de s'authenfitier
		try {
			mXMPPConnection.login(localUserProfile.getJid() + "@" + SERVER_ADDRESS, localUserProfile.getPassword());
			mXMPPConnection.sendPacket(new Presence(Presence.Type.available));
			return true;
		} catch (XMPPException e) {
			Log.d(TAG, "Login failed");
			return false;
		}
	}
	
	// Essaye d'inscrire l'utilisateur
	private boolean tryXmppRegister() {
		// Vérifie qu'on est connecté
		if (!mXMPPConnection.isConnected()) {
			return false;
		}
		
		// Vérifier que l'on peut créer un compte
		AccountManager accountManager = new AccountManager(mXMPPConnection);
		if (!accountManager.supportsAccountCreation()) {
			return false;
		}
		
		// Récupère le profile utilisation
		Profile localUserProfile = LocalUserProfile.getInstance().getProfile();
		if (localUserProfile == null) {
			Log.d(TAG, "Local user profile is null");
			return false;
		}
		
		// Essaye de créer un compte utilisateur
		try {
			accountManager.createAccount(localUserProfile.getJid(), localUserProfile.getPassword());
			
			// La création du compte n'est pas prise en compte immédiatement
			// Il faut se déconnecter puis se reconnecter
			xmppDisconnect();
			return tryXmppConnect();
		} catch (XMPPException e) {
			Log.d(TAG, e.getMessage(), e);
			return false;
		}
	}
	
	// Configure le gestionnaire de découvertes des services XMPP.
	// http://code.google.com/p/asmack/issues/detail?id=13
	private void configureServiceDiscoveryManager() {
		
		// Appeler le constructeur statique
		ServiceDiscoveryManager.getIdentityName();
	}
	
	// Configure le gestionnaire des documents XML des packets XMPP.
	// http://www.igniterealtime.org/community/thread/31118. 
	private void configureProvideManager() {
		// The order is the same as in the smack.providers file
		ProviderManager pm = ProviderManager.getInstance();

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());
		
		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			Log.w(TAG, "Can't load class for org.jivesoftware.smackx.packet.Time");
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster", new RosterExchangeProvider());
		
		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event", new MessageEventProvider());
		
		// Chat State
		pm.addExtensionProvider("active", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference", new GroupChatInvitation.Provider());
		
		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
		
		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());
		
		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
		
		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());
		
		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());
		
		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());
		
		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay", new DelayInformationProvider());
		pm.addExtensionProvider("delay", "urn:xmpp:delay", new DelayInformationProvider());
		
		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version", Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			Log.w(TAG, "Can't load class for org.jivesoftware.smackx.packet.Version");
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
		pm.addIQProvider("sharedgroup", "http://www.jivesoftware.org/protocol/sharedgroup", new SharedGroupsInfo.Provider());
		
		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses", "http://jabber.org/protocol/address", new MultipleAddressesProvider());

		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());
		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
		pm.addIQProvider("open", "http://jabber.org/protocol/ibb", new OpenIQProvider());
		pm.addIQProvider("data", "http://jabber.org/protocol/ibb", new DataPacketProvider());
		pm.addIQProvider("close", "http://jabber.org/protocol/ibb", new CloseIQProvider());
		pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb", new DataPacketProvider());

		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());

		// SHIM
		pm.addExtensionProvider("headers", "http://jabber.org/protocol/shim", new HeadersProvider());
		pm.addExtensionProvider("header", "http://jabber.org/protocol/shim", new HeadersProvider());

		// PubSub
		pm.addIQProvider("pubsub", "http://jabber.org/protocol/pubsub", new PubSubProvider());
		pm.addExtensionProvider("create", "http://jabber.org/protocol/pubsub", new SimpleNodeProvider());
		pm.addExtensionProvider("items", "http://jabber.org/protocol/pubsub", new ItemsProvider());
		pm.addExtensionProvider("item", "http://jabber.org/protocol/pubsub", new ItemProvider());
		pm.addExtensionProvider("subscriptions", "http://jabber.org/protocol/pubsub", new SubscriptionsProvider());
		pm.addExtensionProvider("subscription", "http://jabber.org/protocol/pubsub", new SubscriptionProvider());
		pm.addExtensionProvider("affiliations", "http://jabber.org/protocol/pubsub", new AffiliationsProvider());
		pm.addExtensionProvider("affiliation", "http://jabber.org/protocol/pubsub", new AffiliationProvider());
		pm.addExtensionProvider("options", "http://jabber.org/protocol/pubsub", new FormNodeProvider());
		
		// PubSub owner
		pm.addIQProvider("pubsub", "http://jabber.org/protocol/pubsub#owner", new PubSubProvider());
		pm.addExtensionProvider("configure", "http://jabber.org/protocol/pubsub#owner", new FormNodeProvider());
		pm.addExtensionProvider("default", "http://jabber.org/protocol/pubsub#owner", new FormNodeProvider());
		
		// PubSub event
		pm.addExtensionProvider("event", "http://jabber.org/protocol/pubsub#event", new EventProvider());
		pm.addExtensionProvider("configuration", "http://jabber.org/protocol/pubsub#event", new ConfigEventProvider());
		pm.addExtensionProvider("delete", "http://jabber.org/protocol/pubsub#event", new SimpleNodeProvider());
		pm.addExtensionProvider("options", "http://jabber.org/protocol/pubsub#event", new FormNodeProvider());
		pm.addExtensionProvider("items", "http://jabber.org/protocol/pubsub#event", new ItemsProvider());
		pm.addExtensionProvider("item", "http://jabber.org/protocol/pubsub#event", new ItemProvider());
		pm.addExtensionProvider("retract", "http://jabber.org/protocol/pubsub#event", new RetractEventProvider());
		pm.addExtensionProvider("purge", "http://jabber.org/protocol/pubsub#event", new SimpleNodeProvider());

		// Nick Exchange
		pm.addExtensionProvider("nick", "http://jabber.org/protocol/nick", new Nick.Provider());

		// Attention
		pm.addExtensionProvider("attention", "urn:xmpp:attention:0", new AttentionExtension.Provider());
	}
}
