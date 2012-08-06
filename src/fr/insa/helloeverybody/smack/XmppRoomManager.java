package fr.insa.helloeverybody.smack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.DefaultParticipantStatusListener;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.DiscoverInfo;

import android.location.Location;
import android.util.Log;
import fr.insa.helloeverybody.customserver.ServerInteractionHelper;
import fr.insa.helloeverybody.device.DatabaseManager;
import fr.insa.helloeverybody.device.GpsHelper;
import fr.insa.helloeverybody.helpers.LogWriter;
import fr.insa.helloeverybody.models.Profile;
import fr.insa.helloeverybody.models.ProfileType;
import fr.insa.helloeverybody.viewmodels.ContactList;
import fr.insa.helloeverybody.viewmodels.ConversationList;
import fr.insa.helloeverybody.viewmodels.LocalUserProfile;

/* Gère les salons de discussions publics et privés
-----------------------------------------------------------------------------*/
public class XmppRoomManager {
	
	// Constantes
	public static final String TAG = "XmppRoomManager";
	
	// Variables
	private int roomCounter;
	private ConcurrentHashMap<String, MultiUserChat> mMultiUserChatMap;
	
	// Singleton
	private static XmppRoomManager instance = null;
	
	// Constructeur privée
	private XmppRoomManager() {
		roomCounter = 0;
		mMultiUserChatMap = new ConcurrentHashMap<String, MultiUserChat>();
	}
	
	// Retourne le singleton
	public static synchronized XmppRoomManager getInstance() {
		if (instance == null) {
			instance = new XmppRoomManager();
		}
		return instance;
	}
	
	// Retourne les informations d'un salon
	public CustomRoomInfo getRoomInformation(String roomName) {
		// Vérifier que la connection XMPP n'est pas nul
		XMPPConnection xmppConnection
				= XmppConnectionManager.getInstance().getXmppConnection();
		if (xmppConnection == null) {
			return null;
		}
		
		// Récupérer le gestionnaire de découverte des salons
		ServiceDiscoveryManager discoveryManager
				= ServiceDiscoveryManager.getInstanceFor(xmppConnection);
		if (discoveryManager == null) {
			new ServiceDiscoveryManager(xmppConnection);
		}
		
		// Essayer de trouver les informations du salons
		try {
			DiscoverInfo info = ServiceDiscoveryManager
					.getInstanceFor(xmppConnection).discoverInfo(roomName 
					+ "@" + XmppConnectionManager.CONFERENCE_SERVER_ADDRESS);
			
	        return new CustomRoomInfo(info);
		} catch (XMPPException e) {
			return null;
		}
	}
	
	// Retourne le sujet du salon
	public String getRoomSubject(String roomName) {
		MultiUserChat muc = mMultiUserChatMap.get(roomName);
		
		if (muc != null) {
			return muc.getSubject();
		}
		
		return null;
	}
	
	// Retourne les participants à un salon
	public ArrayList<String> getRoomParticipants(String roomName) {
		MultiUserChat muc = mMultiUserChatMap.get(roomName);
		ArrayList<String> jidList = new ArrayList<String>();
		
		if (muc != null) {
			Iterator<String> it = muc.getOccupants();
			
			while (it.hasNext()) {
				String jid = it.next().split("/")[1];
				jidList.add(jid);
				Log.d(TAG, "participant jid : " + jid);
			}
			
			return jidList;
		}
		
		return null;
	}
	
	// Retourne la liste des salons publics
	public HashMap<String, String> getPublicRooms() {
		HashMap<String, String> mapPublicRooms = new HashMap<String, String>();
		XMPPConnection xmppConnection = XmppConnectionManager.getInstance()
											.getXmppConnection();
		
		try {		
			// Récupérer le gestionnaire de découverte des salons
			ServiceDiscoveryManager mgr = ServiceDiscoveryManager
											.getInstanceFor(xmppConnection);
			if (mgr == null) {
				new ServiceDiscoveryManager(xmppConnection);
			}
			
			// Récupérer la liste des salons publics
			Collection<HostedRoom> hr = MultiUserChat
					.getHostedRooms(xmppConnection, 
							XmppConnectionManager.CONFERENCE_SERVER_ADDRESS);
			
			for (HostedRoom hostedRoom : hr) {
				mapPublicRooms.put(hostedRoom.getJid().split("@")[0], hostedRoom.getName());
			}
		} catch (XMPPException e) { }
		
		return mapPublicRooms;
	}
	
	// Créer un salon privée
	public void createPrivateRoom(final String inviteeJid) {
		NetworkThread.enqueueRunnable(new Runnable() {
			public void run() {	
				
				// Essayer de créer le salon sur le serveur 
				String roomName = createRoomInternal(null, false);
				
				// Vérifier que le salon a été créé
				if (roomName == null) {
					ConversationList.getInstance().
						notifyConversationCreationFailure(null);
					return;
				}
				
				// Inviter l'utilisateur
				inviteUserToRoomInternal(roomName, inviteeJid);
				
				// Ajouter le salon à la liste des salons
				ConversationList.getInstance()
						.addPendingConversation(false, roomName, null);
				ConversationList.getInstance()
						.notifyConversationCreationSuccess(roomName);
				
				LogWriter.logIfDebug(TAG, "Private room created: " + roomName);
			}
		});
	}
	
	// Créer un salon public
	public void createPublicRoom(final String subject, 
				final List<String> inviteeJidList) {
		NetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				// Essayer de créer le salon sur le serveur 
				String roomName = createRoomInternal(subject, true);
				
				// Vérifier que le salon a été créé
				if (roomName == null) {
					ConversationList.getInstance()
						.notifyConversationCreationFailure(subject);
					return;
				}
				
				// Enregistrer le salon public sur le serveur custom
				// TODO(fonctionnalité): Message d'erreur si l'enregistrement échoue
				Location location = GpsHelper.getInstance().getLocation();
				if (location != null)
					ServerInteractionHelper.registerPublicRoom(roomName, subject, location);
				else
					LogWriter.logIfDebug(TAG, "Can't register group : Location is null !");
				
				// Inviter les utilisateurs
				for (String inviteeJid : inviteeJidList) {
					inviteUserToRoomInternal(roomName, inviteeJid);
				}
				
				// Ajouter le salon à la liste des salons
				ConversationList.getInstance()
						.addPendingConversation(true, roomName, subject);	
				ConversationList.getInstance()
						.notifyConversationCreationSuccess(roomName);	
				
				Log.d(TAG, "Public room created: " + roomName);
			}
		});
	}
	
	// Invite une liste d'utilisateusr à un salon
	public void inviteUserListToRoom(final String roomName, 
							final List<String> inviteeJidList) {
		NetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				for (String inviteeJid : inviteeJidList) {
					LogWriter.logIfDebug("TAG", "Invite : " + inviteeJid 
							+ " " + roomName);
					inviteUserToRoomInternal(roomName, inviteeJid);
				}
			}
		});
	}
	
	// Invite un utilisateur à un salon
	public void inviteUserToRoom(final String roomName, final String jid) {
		NetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				if (roomName != null) {
					LogWriter.logIfDebug(TAG, "Invite : " + jid
							+ " " + roomName);
					inviteUserToRoomInternal(roomName, jid);
				}
			}
		});
	}
	
	// Rejoint un salon
	public void joinRoom(final boolean isPublic,
				final String roomName, final String roomSubject) {
		NetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				
				// Rejoindre la conversation
				if ((roomName == null) || (!joinRoomInternal(roomName))) {
					return;
				}
				
				// Ajouter la conversation aux conversations courantes
				LogWriter.logIfDebug(TAG, "Join : " + roomName);
				ConversationList.getInstance()
						.addHiddenConversation(isPublic, roomName, roomSubject);
								
				// Ajouter les participants existants
				List<String> memberJidList = XmppRoomManager
						.getInstance().getRoomParticipants(roomName);
				String localJid = LocalUserProfile.getInstance().getProfile().getJid();
				for (String jid : memberJidList) {
					
				    // Vérifier que le profil n'est pas l'utilisateur local
					if (jid.equals(localJid)) {
						continue;
					}

					// Télécharger le profil s'il est inconnu
					Profile profile = ContactList.getInstance().getProfileByJid(jid);
					if (profile == null) {
						profile = XmppContactsManager.downloadProfile(jid);
						
						// Vérifier que le profil télécharger n'est pas nul
						if (profile == null) {
							profile = new Profile(jid, "Inconnu", "HE");
						}
						
						// Ajouter le profil à la liste de contacts
						profile.setKnown(true);
						ContactList.getInstance().addProfile(profile);
					}
					
					// Si le profil est connu, mettre à jour la liste de contacts
					else {
						ProfileType previousProfileType = profile.getProfileType();
						profile.setKnown(true);
						ContactList.getInstance().update(profile, previousProfileType);
					}
					
					// Mettre à jour la base de données
					DatabaseManager.getInstance().insertOrUpdateContact(profile);
					
					// Ajouter le profil à la conversation
					ConversationList.getInstance().addConversationMember(roomName, jid);
				}
			}
		});
	}
	
	// Quitte un salon
	public void leaveRoom(final String roomName) {
		NetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				if (roomName != null) {
					leaveRoomInternal(roomName);
				}
				LogWriter.logIfDebug(TAG, "Leave the room : " + roomName);
			}
		});
	}
	
	// Envoie un message à un salon
	public void sendMessageToRoom(final String roomName, final String message) {

		NetworkThread.enqueueRunnable(new Runnable() {
			public void run() {
				MultiUserChat muc = mMultiUserChatMap.get(roomName);
		
				if (muc != null) {
					try {
						muc.sendMessage(message);
						ConversationList.getInstance().addSendMessage(roomName, message);
					} catch (XMPPException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
		});
	}
	
	// Crée un salon
	private String createRoomInternal(String subject, Boolean isPublic) {
		String localUserJid = LocalUserProfile.getInstance().getProfile().getJid();
		String roomName = localUserJid + (++roomCounter);
		MultiUserChat muc = new MultiUserChat(XmppConnectionManager.
				getInstance().getXmppConnection(), roomName + "@" 
						+ XmppConnectionManager.CONFERENCE_SERVER_ADDRESS);
		Boolean creationSuccess = false;

		try {
			muc.create(localUserJid);
			
			// Gestion du formulaire de configuration
			Form form = muc.getConfigurationForm();
			Form submitForm = form.createAnswerForm();

			// Mets les valeurs par défaut pour toutes les options
			for (Iterator<FormField> fields = form.getFields(); fields.hasNext();) {
				FormField field = fields.next();
				if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
					submitForm.setDefaultAnswer(field.getVariable());
				}
			}
			
			if (subject != null) {
				submitForm.setAnswer("muc#roomconfig_changesubject", true);
			}
			
			submitForm.setAnswer("muc#roomconfig_publicroom", isPublic);
			submitForm.setAnswer("muc#roomconfig_allowinvites", true);

			muc.sendConfigurationForm(submitForm);
			
			if (subject != null) {
				muc.changeSubject(subject);
			}
			
			Log.d(TAG, "1 room subject set to : " + subject);
			Log.d(TAG, "2 room subject set to : " + muc.getSubject());
			
			creationSuccess = true;
			
		} catch (XMPPException e) {
			Log.e(TAG, e.getMessage(), e);
		}

		if (creationSuccess) {
			mMultiUserChatMap.put(roomName, muc);
			addListenersToMuc(muc, roomName);
			return roomName;
		} else {
			return null;
		}
	}
	
	// Rejoint un salon
	private Boolean joinRoomInternal(String roomName) {
		MultiUserChat muc = new MultiUserChat(XmppConnectionManager.
				getInstance().getXmppConnection(), roomName + "@" 
						+ XmppConnectionManager.CONFERENCE_SERVER_ADDRESS);

		// Essayer de rejoindre un salon
		try {
			muc.join(LocalUserProfile.getInstance().getProfile().getJid());
		} catch (XMPPException e) {
			Log.e(TAG, e.getMessage(), e);
			return false;
		}

		// Ajouter les listeners au salon
		mMultiUserChatMap.put(roomName, muc);
		addListenersToMuc(muc, roomName);

		return true;
	}

	// Quitte un salon
	private Boolean leaveRoomInternal(String roomName) {
		MultiUserChat muc = mMultiUserChatMap.get(roomName);

		if (muc != null) {
			muc.leave();
			mMultiUserChatMap.remove(roomName);
			return true;
		}

		return false;
	}

	// Invite un utilisateur à un salon
	private Boolean inviteUserToRoomInternal(String roomName, String userJid) {
		MultiUserChat muc = mMultiUserChatMap.get(roomName);

		if (muc != null) {
			muc.invite(userJid + "@" 
					+ XmppConnectionManager.SERVER_ADDRESS, null);
			return true;
		}

		return false;
	}
	
	// Ajoute les listeners à un chat
	private void addListenersToMuc(final MultiUserChat muc, final String roomName) {
		
		// Gèrer les refus des contacts à entrer dans un salon
		muc.addInvitationRejectionListener(new InvitationRejectionListener() {
			public void invitationDeclined(String invitee, String reason) {
				ConversationList conversationList = ConversationList.getInstance();
				
				// Supprimer le salon
				if (conversationList.getConversationByName(roomName).isEmpty()) {
					conversationList.removeConversation(roomName);
				}
			}
		});

		// Gèrer les réceptions de message
		muc.addMessageListener(new PacketListener() {
			public void processPacket(Packet pck) {
				Message msg = (Message) pck;
				String senderJid = muc.getOccupant(msg.getFrom()).getJid().split("@")[0];
				
				// Ajouter le message aux message reçus
				ConversationList.getInstance().
						addReceivedMessage(roomName, senderJid, msg.getBody());
				Log.d(TAG, "Received message from: " + senderJid);
			}
		});

		// Gérer les ajouts/retraits de participants
		muc.addParticipantStatusListener(new DefaultParticipantStatusListener() {
			// Gère les nouveaux participants
			@Override
			public void joined(String participant) {
				super.joined(participant);
				String newMemberJid = muc.getOccupant(participant).getJid().split("@")[0];
				
				// Ajouter un participant
				ConversationList.getInstance().
						addConversationMember(roomName, newMemberJid);
			}

			// Gère les départs des participants
			@Override
			public void left(String participant) {
				super.left(participant);
				ConversationList conversationList = ConversationList.getInstance();
				String memberJid = participant.split("/")[1];
				String localJid = LocalUserProfile.getInstance().getProfile().getJid();
				boolean isLocalUser = memberJid.equals(localJid);
				
				// Supprimer le participant du salon
				if (!isLocalUser) {
					conversationList.removeConversationMember(roomName, memberJid);
				}
				
				// Supprimer le salon
				if (conversationList.getConversationByName(roomName).isEmpty() 
						|| isLocalUser) {
					conversationList.removeConversation(roomName);
				}
			}
		});
	}
}
