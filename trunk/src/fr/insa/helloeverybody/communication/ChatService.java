package fr.insa.helloeverybody.communication;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.Attributes;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import android.os.Handler;
import android.util.Log;
import fr.insa.helloeverybody.models.Profile;

public class ChatService {
	/* ---------------------------------------------
	 * Static vars et singleton
	 * ---------------------------------------------
	 */
	private static ChatService mChatServiceSingleton;
	
	public static final String DEFAULT_SERVER_ADDR = "im.darkserver.eu.org";
	public static final Integer DEFAULT_PORT = 5222;
	public static final String NEARME_GROUP_NAME = "nearme";
	public static final int MESSAGE_TYPE_IN = 1;
	public static final int MESSAGE_TYPE_OUT = 2;
	public static final int MESSAGE_TYPE_SYS = 3;
	
	public static ChatService GetChatService() {
		if (mChatServiceSingleton == null) {
			mChatServiceSingleton = new ChatService(DEFAULT_SERVER_ADDR, DEFAULT_PORT, null);
		}
		
		return mChatServiceSingleton;
	}
	
	public static Boolean AskLogin(String userName, String passwd) {
		if (mChatServiceSingleton != null) {
			if (mChatServiceSingleton.mLoggedIn)
				return true;
			else if (!mChatServiceSingleton.mLoginOperationPending) {
				mChatServiceSingleton.doLogin(userName, passwd);
				return true;
			}
		}
		
		return false;
	}
	
	public static void RegisterHandler(Handler handler) {
		if (mChatServiceSingleton != null) {
			mChatServiceSingleton.mHandlersList.add(handler);
		}
	}
	
	public static void RemoveHandler(Handler handler) {
		if (mChatServiceSingleton != null) {
			mChatServiceSingleton.mHandlersList.remove(handler);
		}
	}
	
	/* ---------------------------------------------
	 * Classe ChatService : Gestion des communications
	 * réseaux avec XMPP
	 * ---------------------------------------------
	 */
	private Set<Handler> mHandlersList;
	private XMPPConnection mConnection;
	private Roster mRoster;
	
	private String mTargetAddr;
	private int mTargetPort;
	private String mTargetService;
	private Boolean mLoggedIn;
	private Boolean mLoginOperationPending;
	
	/*
	 * Méthodes privées
	 */
	private ChatService(String serverAddr, int serverPort, String serverService) {
		mHandlersList = Collections.synchronizedSet(new HashSet<Handler>());
		mTargetAddr = serverAddr;
		mTargetPort = serverPort;
		mTargetService = serverService;
		mLoggedIn = false;
		mLoginOperationPending = false;
	}
	
	private void sendMessageToHandlers(int id, Object message) {
		for (Iterator<Handler> iterator = mHandlersList.iterator(); iterator.hasNext();) {
			Handler handler = (Handler) iterator.next();
			handler.obtainMessage(id, message).sendToTarget();
		}
	}
	
	private RosterGroup addRosterGroup(String groupName) {
		RosterGroup group = null;
		
		if (mLoggedIn) {
			group = mRoster.getGroup(groupName);
			
			if (group == null) {
				group = mRoster.createGroup(groupName);
			}
		}
		
		return group;
	}
	
	/*
	 * Méthodes publiques
	 */
	public void doConnect() {
		ConnectionConfiguration connConfig = new ConnectionConfiguration(mTargetAddr, mTargetPort, mTargetService);
		XMPPConnection connection = new XMPPConnection(connConfig);
		
		try {
			connection.connect();
			setConnection(connection);
		} catch (XMPPException e) {
			setConnection(null);
		}
	}

	// Methode pour creer un compte sur le serveur
	public void doRegistrate(String login, String pwd, Profile profile) {
		RegistrateThread registrateThread = new RegistrateThread(login, pwd, profile);
		registrateThread.start();
	}

	// Methode pour se logger sur le serveur, l'utilisateur peut ensuite ecrire
	// et recevoir
	// les messages
	public void doLogin(String login, String pwd) {
		LoginThread loginThread = new LoginThread(login, pwd);
		mLoginOperationPending = true;
		loginThread.start();
	}

	public Roster getContactsRoster() {
		return mRoster;
	}
	
	public Boolean flushNearMeContacts() {
		Boolean errorOccured = false;
		
		RosterGroup nearMeGroup = addRosterGroup(NEARME_GROUP_NAME);
		Collection<RosterEntry> contacts = nearMeGroup.getEntries();
		
		for (RosterEntry rosterEntry : contacts) {
			try {
				nearMeGroup.removeEntry(rosterEntry);
			} catch (XMPPException e) {
				errorOccured = true;
			}
		}
		
		return !errorOccured;
	}
	
	public void replaceNearMeContacts(Collection<Profile> newContacts) {
		String[] groupList = {NEARME_GROUP_NAME};
		flushNearMeContacts();
		
		for (Profile userProfile : newContacts) {
			try {
				mRoster.createEntry(userProfile.getJid() + "@" + mTargetAddr, userProfile.getFirstName() + " " + userProfile.getLastName(), groupList);
			} catch (XMPPException e) {
				Log.e("ChatService", e.getMessage());
			}
		}
	}

	public void setConnection(XMPPConnection connection) {
		this.mConnection = connection;
		if (mConnection != null) {
			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
			mConnection.addPacketListener(new PacketListener() {
				public void processPacket(Packet packet) {
					Message message = (Message) packet;
					if (message.getBody() != null) {
						sendMessageToHandlers(MESSAGE_TYPE_IN, message.getBody());
					}
				}
			}, filter);
		}
	}

	public void write(String to, String text) {
		Message msg = new Message(to, Message.Type.chat);
		msg.setBody(text);
		mConnection.sendPacket(msg);
		sendMessageToHandlers(MESSAGE_TYPE_OUT, text);
	}
	
	//Thread pour s'enregistrer
	private class LoginThread extends Thread {
		String login;
		String pwd;

		public LoginThread(String login, String pwd) {
			this.login = login;
			this.pwd = pwd;
		}

		public void run() {
			try {
				sendMessageToHandlers(MESSAGE_TYPE_OUT, "connecting");
				
				// si la connexion n'a pas ete etablie, on connecte
				if (mConnection == null) {
					doConnect();
				}
				
				mConnection.login(login, pwd);
				sendMessageToHandlers(MESSAGE_TYPE_OUT, "connection established");

				// Set the status to available
				Presence presence = new Presence(Presence.Type.available);
				mConnection.sendPacket(presence);
				
				mRoster = mConnection.getRoster();
				mRoster.setSubscriptionMode(SubscriptionMode.accept_all);
				
				mLoggedIn = true;
			} catch (XMPPException ex) {
				setConnection(null);
			}
			
			mLoginOperationPending = false;
		}
	}

	// Thread pour creer un nouveau compte
	private class RegistrateThread extends Thread {
		String login;
		String pwd;

		public RegistrateThread(String login, String pwd, Profile userProfile) {
			this.login = login;
			this.pwd = pwd;
		}

		public void run() {
			Attributes attributes = new Attributes();
			attributes.putValue("login", login);
			attributes.putValue("pwd", pwd);
			
			doConnect();
			AccountManager accountManager = new AccountManager(mConnection);
			
			try {
				if (accountManager.supportsAccountCreation()) {
					accountManager.createAccount(login, pwd);
					sendMessageToHandlers(MESSAGE_TYPE_OUT, "Account Created");
				}
			} catch (XMPPException e) {
				setConnection(null);
			}
		}
	}

	public XMPPConnection getConnection(){
		return this.mConnection;
	}
}