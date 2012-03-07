package fr.insa.helloeverybody.smack;

import java.util.HashMap;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.VCard;

import fr.insa.helloeverybody.models.Profile;

import android.util.Log;

public class ConnectionHelper {
	public static final String DEFAULT_SERVER_ADDR = "im.darkserver.eu.org";
	public static final String DEFAULT_CONFERENCE_SERVER_ADDR = "conference.im.darkserver.eu.org";
	public static final Integer DEFAULT_PORT = 5222;
	public static final String DEFAULT_RESSOURCE = "HE-Mobile";
	
	private final String TAG = "ConnectionHelper";
	
	private XMPPConnection mXMPPConnection;
	private ConnectionConfiguration mConnectionConfig;
	private String mConferenceServerAdr;
	
	/**
	 * Connexion au serveur XMPP avec les paramètres DEFAULT_SERVER_ADR et DEFAULT_PORT
	 */
	public ConnectionHelper() {
		this(DEFAULT_SERVER_ADDR, DEFAULT_CONFERENCE_SERVER_ADDR, DEFAULT_PORT);
	}
	
	/**
	 * Connexion au serveur XMPP avec les paramètres suivants
	 * @param serverAdr : Adresse du serveur XMPP
	 * @param serverPort : Port du serveur XMPP
	 */
	public ConnectionHelper(String serverAdr, String conferenceServerAdr, Integer serverPort) {
		mConnectionConfig = new ConnectionConfiguration(serverAdr, serverPort, DEFAULT_RESSOURCE);
		mConferenceServerAdr = conferenceServerAdr;
		mXMPPConnection = new XMPPConnection(mConnectionConfig);
	}
	
	/**
	 * Réalise la connexion au serveur XMPP
	 * @return Réussite de la connexion
	 */
	public Boolean connect() {
		Boolean connectionSuccesful = false;

		try {
			if (!mXMPPConnection.isConnected()) {
				mXMPPConnection.connect();
				connectionSuccesful = true;
			}
		} catch (XMPPException e) {
			Log.e(TAG, e.getMessage(), e);
		}

		return connectionSuccesful;
	}
	
	/**
	 * S'enregistre auprès du serveur XMPP
	 * @param localUserProfile : Profil de l'utilisateur local
	 * @return Réussite de l'enregistrement
	 */
	public Boolean login(Profile localUserProfile) {
		Boolean loginSuccesful = false;
		
		if (mXMPPConnection.isConnected()) {
			try {
				if (!mXMPPConnection.isAuthenticated()) {
					mXMPPConnection.login(localUserProfile.getJid() + "@" + mConnectionConfig.getHost(), localUserProfile.getPassword());
					mXMPPConnection.sendPacket(new Presence(Presence.Type.available));
					loginSuccesful = true;
				}
			} catch (XMPPException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		
		return loginSuccesful;
	}
	
	/**
	 * Crée un compte sur le serveur XMPP, si celui-ci accepte
	 * @param localUserProfile : Profil de l'utilisateur local
	 * @return Réussite de la création du compte
	 */
	public Boolean register(Profile localUserProfile) {
		Boolean registerSuccesful = false;
		
		if (mXMPPConnection.isConnected()) {
			AccountManager accountManager = new AccountManager(mXMPPConnection);
			
			if (accountManager.supportsAccountCreation()) {
				try {
					accountManager.createAccount(localUserProfile.getJid(), localUserProfile.getPassword());
					
					//La création du compte n'est pas prise en compte immédiatement, il faut se déconnecter puis se reconnecter
					this.disconnect();
					this.connect();
					
					registerSuccesful = true;
				} catch (XMPPException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
		
		return registerSuccesful;
	}
	
	/**
	 * Se déconnecte du serveur
	 */
	public void disconnect() {
		mXMPPConnection.disconnect();
	}
	
	public XMPPConnection getXMPPConnection() {
		return mXMPPConnection;
	}
	
	/**
	 * Permet de récupérer le ChatManager associé à la connexion
	 * @return ChatManager
	 */
	public ChatManager getChatManager() {
		return mXMPPConnection.getChatManager();
	}
	
	/**
	 * Permet de récupérer le Roster associé à la connexion
	 * @return Roster
	 */
	public Roster getRoster() {
		return mXMPPConnection.getRoster();
	}
	
	public String getServerDomain() {
		return mConnectionConfig.getHost();
	}
	
	public String getConferenceServer() {
		return mConferenceServerAdr;
	}
	
	public VCard getVCard(String jid) {
		VCard vcard = new VCard();
		
		try {
			vcard.load(mXMPPConnection, jid);
		} catch (XMPPException e) {
			vcard = null;
			Log.e(TAG, e.getMessage(), e);
		}
		
		return vcard;
	}
	
	public Boolean saveVCard(VCard vc) {
		try {
			vc.save(mXMPPConnection);
			return true;
		} catch (XMPPException e) {
			return false;
		}
	}
	
	/**
	 * Crée une MUC pour les communications à plusieurs utilisant le serveur donné à la construction
	 * @param roomName : Nom de la conversation
	 * @return Object MUC associé à la connexion
	 */
	public MultiUserChat createMultiUserChat(String roomName) {
		return new MultiUserChat(mXMPPConnection, roomName);
	}
	
	/**
	 * 
	 * @param invitationListener
	 */
	public void addInvitationListener(InvitationListener invitationListener) {
		MultiUserChat.addInvitationListener(mXMPPConnection, invitationListener);
	}
	
	/**
	 * 
	 * @param invitationListener
	 */
	public void removeInvitationListener(InvitationListener invitationListener) {
		MultiUserChat.removeInvitationListener(mXMPPConnection, invitationListener);
	}
}
