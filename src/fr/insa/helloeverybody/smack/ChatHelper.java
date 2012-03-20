package fr.insa.helloeverybody.smack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.DefaultParticipantStatusListener;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.os.Handler;
import android.util.Log;
import fr.insa.helloeverybody.models.Profile;

public class ChatHelper {
	private static final String TAG = "ChatHelper";

	private Integer roomCounter = 0;
	private ConcurrentHashMap<String, MultiUserChat> mChatList;
	private ConnectionHelper mConnectionHelper;
	private Profile mUserProfile;

	private ConcurrentHashMap<String, Handler> mChatHandlerMap;

	/**
	 * Création de classe permettant la gestion des différents chats en cours
	 * 
	 * @param localUserProfile
	 *            : Profile de l'utilisateur du téléphone courant
	 * @param connectionHelper
	 *            : Objet représentant la connection XMPP
	 */
	public ChatHelper(Profile localUserProfile, ConnectionHelper connectionHelper) {
		mChatList = new ConcurrentHashMap<String, MultiUserChat>();
		mChatHandlerMap = new ConcurrentHashMap<String, Handler>();
		mConnectionHelper = connectionHelper;
		mUserProfile = localUserProfile;
		//mInvitationListener deplacé dans ChatService, besoin de broadcastGeneralMessage
	}

	private void sendMessageToHandler(Handler handler, int id, Object message) {
		handler.obtainMessage(id, message).sendToTarget();
	}

	private void sendEventToChat(String roomName, Object message) {
		Handler h = mChatHandlerMap.get(roomName);

		if (h != null)
			sendMessageToHandler(mChatHandlerMap.get(roomName), ChatService.CHAT_EVENT, message);
		else
			Log.d(TAG, "Trying to send msg to null handler for room : " + roomName);
	}



	/**
	 * Permet d'associer un Handler a un salon de discussion(roomName)
	 */
	public void registrateHandlerToRoom(Handler handler, String roomName) {
		mChatHandlerMap.put(roomName, handler);

		Log.d(TAG, "Adding handler for : " + roomName);
	}
	
	public void unregistrateHandlerToRoom(String roomName) {
		mChatHandlerMap.remove(roomName);
	}
	
	
	/**
	 * Permet d'enregistrer les listeners a un Muc
	 * @param muc : le muc cible
	 * @param roomName : le nom du salon de discussion associe au muc
	 */
	public void setListenersToMuc(final MultiUserChat muc, final String roomName) {
		muc.addInvitationRejectionListener(new InvitationRejectionListener() {
			public void invitationDeclined(String invitee, String reason) {
				InternalEvent event = new InternalEvent(roomName, ChatService.EVT_INV_REJ);
				event.setContent(invitee);
				sendEventToChat(roomName, event);
			}
		});

		muc.addMessageListener(new PacketListener() {
			public void processPacket(Packet pck) {
				Message msg = (Message) pck;
				msg.setFrom(muc.getOccupant(msg.getFrom()).getJid().split("@")[0]);
				InternalEvent event = new InternalEvent(roomName, ChatService.EVT_MSG_RCV);
				event.setContent(msg);
				sendEventToChat(roomName, event);
			}
		});

		muc.addParticipantStatusListener(new DefaultParticipantStatusListener() {
			@Override
			public void joined(String participant) {
				super.joined(participant);
				InternalEvent event = new InternalEvent(roomName, ChatService.EVT_NEW_MEMBER);
				event.setContent(muc.getOccupant(participant).getJid().split("@")[0]);
				sendEventToChat(roomName, event);
			}

			@Override
			public void left(String participant) {
				super.left(participant);
				InternalEvent event = new InternalEvent(roomName, ChatService.EVT_MEMBER_QUIT);
				event.setContent(participant.split("/")[1]);
				sendEventToChat(roomName, event);
			}
		});
	}
	
	/**
	 * Permet de créer un salon de discussion privé sans sujet
	 * 
	 * @return Identifiant du salon de discussion (roomName)
	 */
	public String createRoom() {
		return createRoom(null, false);
	}
	
	/**
	 * 
	 * @param subject : Sujet du salon de discussion (utile uniquement si conversation publique)
	 * @param isPublic : Est-ce que la discussion doit apparaitre dans l'annuaire ?
	 * @return
	 */
	public String createRoom(String subject, Boolean isPublic) {
		String roomName = mUserProfile.getJid() + (++roomCounter);
		MultiUserChat muc = mConnectionHelper.createMultiUserChat(roomName + "@" + mConnectionHelper.getConferenceServer());
		Boolean creationSuccess = false;

		try {
			muc.create(mUserProfile.getJid());
			
			
			//Gestion du formulaire de configuration
			Form form = muc.getConfigurationForm();
			Form submitForm = form.createAnswerForm();

			//Mets les valeurs par défaut pour toutes les options
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
			
			if(subject != null)
				muc.changeSubject(subject);
			
			Log.d(TAG,"1 room subject set to : " + subject);
			Log.d(TAG,"2 room subject set to : " + muc.getSubject());
			
			creationSuccess = true;
			
		} catch (XMPPException e) {
			Log.e(TAG, e.getMessage(), e);
		}

		if (creationSuccess) {
			mChatList.put(roomName, muc);
			setListenersToMuc(muc, roomName);
			return roomName;
		} else {
			return null;
		}
	}
	
	public Boolean joinRoom(String roomName) {
		MultiUserChat muc = mConnectionHelper.createMultiUserChat(roomName + "@" + mConnectionHelper.getConferenceServer());
		Boolean joinSuccess = false;

		try {
			muc.join(mUserProfile.getJid());
			joinSuccess = true;
		} catch (XMPPException e) {
			Log.e(TAG, e.getMessage(), e);
		}

		if (joinSuccess) {
			setListenersToMuc(muc, roomName);
			mChatList.put(roomName, muc);
		}

		return joinSuccess;
	}

	public Boolean leaveRoom(String roomName) {
		MultiUserChat muc = mChatList.get(roomName);

		if (muc != null) {
			muc.leave();
			mChatList.remove(roomName);
			return true;
		}

		return false;
	}

	public Boolean inviteUserToRoom(String roomName, String userJid) {
		MultiUserChat muc = mChatList.get(roomName);

		if (muc != null) {
			muc.invite(userJid + "@" + mConnectionHelper.getServerDomain(), null);
			return true;
		}

		return false;
	}

	public Boolean sendMessageToRoom(String roomName, String message) {
		MultiUserChat muc = mChatList.get(roomName);

		if (muc != null) {
			try {
				muc.sendMessage(message);
				InternalEvent event = new InternalEvent(roomName, ChatService.EVT_MSG_SENT);
				event.setContent(message);
				sendEventToChat(roomName, event);
				return true;
			} catch (XMPPException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

		return false;
	}
	
	public String getSubject(String roomName) {
		MultiUserChat muc = mChatList.get(roomName);
		
		if (muc != null) {
			return muc.getSubject();
		}
		
		return null;
	}
	
	public ArrayList<String> getParticipants(String roomName) {
		MultiUserChat muc = mChatList.get(roomName);
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
}