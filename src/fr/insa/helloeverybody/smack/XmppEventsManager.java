package fr.insa.helloeverybody.smack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import fr.insa.helloeverybody.helpers.LogWriter;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.viewmodels.ContactsList;

import android.os.Handler;
import android.util.Log;

/* Gère les événements XMPP généraux et spécifiques à un salon
-----------------------------------------------------------------------------*/
public class XmppEventsManager {
	
	// Log tag
	public static final String TAG = "XmppEvents";

	// L'ID BCST_EVENT est reservé pour les événements de broadcast
	public static final int BCST_EVENT = 1;
	
	// Evénements liés au informations générales
	public static final int EVT_CONN_OK = 100;
	public static final int EVT_CONN_NOK = 101;
	
	// Evénements généraux
	public static final InternalEvent EVT_CONNECTION_OK =
			new InternalEvent(null, EVT_CONN_OK);
	public static final InternalEvent EVT_CONNECTION_DOWN =
			new InternalEvent(null, EVT_CONN_NOK);
	
	// Singleton
	private static XmppEventsManager instance = null; 
	
	// Handlers
	private Set<Handler> mGeneralHandlerSet;
	private Lock handlersLock;
	private InvitationListener mInvitationListener;
	private RosterListener mRosterListener;
	
	// Constructeur privée
	private XmppEventsManager() {
		mGeneralHandlerSet = new HashSet<Handler>();
		handlersLock = new ReentrantLock(true);
	}
	
	// Retourne le singleton
	public static synchronized XmppEventsManager getInstance() {
		if (instance == null) {
			instance = new XmppEventsManager();
		}
		return instance;
	}
	
	// Ajoute un listener à une connection XMPP pour traiter les invitations
	public void addInvitationListener(XMPPConnection xmppConnection)
	{
		mInvitationListener = new RoomInvitationListener();
		MultiUserChat.addInvitationListener(xmppConnection, mInvitationListener);
	}
	
	// Retire le listener des invitations d'une connection XMPP
	public void removeInvitationListener(XMPPConnection xmppConnection) {
		if (mInvitationListener != null) { 
			MultiUserChat.removeInvitationListener(xmppConnection, mInvitationListener);
		}
	}
	
	// Ajoute le listener des départs et arrivés des contacts
	public void addRosterListener() {
		Roster roster = XmppConnectionManager.getInstance().getRoster();
		if ((mRosterListener == null) && (roster != null)) {
			mRosterListener = new ContactRosterListener();
			roster.addRosterListener(mRosterListener);
		}
	}
	
	// Retire le listener des départs et arrivés des contacts
	public void removeRosterListener() {
		Roster roster = XmppConnectionManager.getInstance().getRoster();
		if ((mRosterListener != null) && (roster != null)) {
			roster.removeRosterListener(mRosterListener);
		}
	}
	
	// Envoie un message à tous les handlers généraux
	public void broadcastGeneralMessage(Object message) {
		sendMessageToHandlers(mGeneralHandlerSet, BCST_EVENT, message);
	}
		
	// Envoie un message à un ensemble d'handlers
	private void sendMessageToHandlers(Set<Handler> handlerSet, int id, Object message) {
		handlersLock.lock();
		for (Iterator<Handler> iterator = handlerSet.iterator(); iterator.hasNext();) {
			Handler handler = (Handler) iterator.next();
			handler.obtainMessage(id, message).sendToTarget();
		}
		handlersLock.unlock();
	}
	
	// Ajoute un handler général
	public void addGeneralHandler(Handler handler) {
		handlersLock.lock();
		mGeneralHandlerSet.add(handler);
		handlersLock.unlock();
	}
	
	// Supprime un handler général
	public void removeGeneralHandler(Handler handler) {
		handlersLock.lock();
		mGeneralHandlerSet.remove(handler);
		handlersLock.unlock();
	}
	
	
	/* Classe pour gérer les arrivées et départs des contacts
	-------------------------------------------------------------------------*/
	private class ContactRosterListener implements RosterListener {
		
		// Gère les arrivées et départs des contacts
		public void presenceChanged(Presence arg0) {
			if (arg0.isAvailable()) {
				onContactOnline(arg0.getFrom().split("@")[0]);
			} else {
				onContactOffline(arg0.getFrom().split("@")[0]);
			}
		}
		
		// Ajoute un contact à la liste
		public void onContactOnline(String jid) {
			ContactsList contactsList = ContactsList.getInstance();
			Profile onlineProfile = contactsList.getProfileByJid(jid);
			
			if (onlineProfile == null) {
				Profile downloadedProfile = XmppContactsManager.downloadProfile(jid);
				if (downloadedProfile != null) {
					contactsList.addProfile(downloadedProfile);
				}
				
				LogWriter.logIfDebug(TAG, "New JID : " + jid + " went online");
			}
			else {
				LogWriter.logIfDebug(TAG, "Old JID : " + jid + " went online");
			}
		}

		// Retire un contact de la liste
		public void onContactOffline(String jid) {
			ContactsList.getInstance().removeProfileByJid(jid);
			LogWriter.logIfDebug(TAG, "JID : " + jid + " went offline");
		}
		
		public void entriesDeleted(Collection<String> arg0) { }
		
		public void entriesAdded(Collection<String> arg0) { }

		public void entriesUpdated(Collection<String> arg0) { }
	}
	
	
	/* Classe pour gérer la réception des invitations à un salon
	-------------------------------------------------------------------------*/ 
	private class RoomInvitationListener implements InvitationListener {

		// Constructeur par défaut
		public RoomInvitationListener() { }
		
		// Gère la réception d'une invitation
		public void invitationReceived(Connection conn, String room, 
				String inviter, String reason, String password, Message message) {
			XmppRoomManager roomManager = XmppRoomManager.getInstance();
			String roomName = room.split("@")[0];
			String inviterJid = inviter.split("@")[0];
			String roomSubject = roomManager.getRoomSubject(roomName);
			CustomRoomInfo roomInfo = roomManager.getRoomInformation(roomName);

			// Accepter l'invitation et rejoindre le salon
			// TODO(bug): Bug si une autre conversation est ouverte
			XmppRoomManager.getInstance().joinRoom(
					roomInfo.isPublic(), roomName, roomSubject);

			Log.d(TAG, "Join room : " + roomName + "\tInviter: " + inviterJid);
		}
	}
}
