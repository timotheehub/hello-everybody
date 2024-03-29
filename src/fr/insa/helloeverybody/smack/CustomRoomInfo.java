package fr.insa.helloeverybody.smack;

import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.packet.DiscoverInfo;

/**
 * Contient les informations d'un salon de discussion 
 * (nom, est public, nombre de participants).
 * Basée sur le code de RoomInfo, qui aurait dû être sous classée, mais le
 * constructeur est privé et impossible d'accéder au Form de la room en dehors.
 * 
 * @author vincent
 */
public class CustomRoomInfo {
	/**
	 * JID of the room. The node of the JID is commonly used as the ID of the
	 * room or name.
	 */
	private String room;
	/**
	 * Description of the room.
	 */
	private String description = "";
	/**
	 * Last known subject of the room.
	 */
	private String subject = "";
	/**
	 * Current number of occupants in the room.
	 */
	private int occupantsCount = -1;
	/**
	 * A room is considered members-only if an invitation is required in order
	 * to enter the room. Any user that is not a member of the room won't be
	 * able to join the room unless the user decides to register with the room
	 * (thus becoming a member).
	 */
	private boolean membersOnly;
	/**
	 * Moderated rooms enable only participants to speak. Users that join the
	 * room and aren't participants can't speak (they are just visitors).
	 */
	private boolean moderated;
	/**
	 * Every presence packet can include the JID of every occupant unless the
	 * owner deactives this configuration.
	 */
	private boolean nonanonymous;
	/**
	 * Indicates if users must supply a password to join the room.
	 */
	private boolean passwordProtected;
	/**
	 * Persistent rooms are saved to the database to make sure that rooms
	 * configurations can be restored in case the server goes down.
	 */
	private boolean persistent;
	/**
	 * Indicates if the room is public or private
	 */
	private boolean publicRoom;

	/**
	 * Constructor
	 * 
	 * @param info
	 */
	public CustomRoomInfo(DiscoverInfo info) {
		this.room = info.getFrom();
		// Get the information based on the discovered features
		this.membersOnly = info.containsFeature("muc_membersonly");
		this.moderated = info.containsFeature("muc_moderated");
		this.nonanonymous = info.containsFeature("muc_nonanonymous");
		this.passwordProtected = info.containsFeature("muc_passwordprotected");
		this.persistent = info.containsFeature("muc_persistent");
		this.publicRoom = info.containsFeature("muc_public");
		// Get the information based on the discovered extended information
		Form form = Form.getFormFrom(info);
		if (form != null) {
			FormField descField = form.getField("muc#roominfo_description");
			this.description = (descField == null || !(descField.getValues().hasNext())) ? "" : descField.getValues().next();

			FormField subjField = form.getField("muc#roominfo_subject");
			this.subject = (subjField == null || !(subjField.getValues().hasNext())) ? "" : subjField.getValues().next();

			FormField occCountField = form.getField("muc#roominfo_occupants");
			this.occupantsCount = occCountField == null ? -1 : Integer.parseInt(occCountField.getValues().next());
		}
	}

	/**
	 * Returns the JID of the room whose information was discovered.
	 * 
	 * @return the JID of the room whose information was discovered.
	 */
	public String getRoom() {
		return room;
	}

	/**
	 * Returns the discovered description of the room.
	 * 
	 * @return the discovered description of the room.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the discovered subject of the room. The subject may be empty if
	 * the room does not have a subject.
	 * 
	 * @return the discovered subject of the room.
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Returns the discovered number of occupants that are currently in the
	 * room. If this information was not discovered (i.e. the server didn't send
	 * it) then a value of -1 will be returned.
	 * 
	 * @return the number of occupants that are currently in the room or -1 if
	 *         that information was not provided by the server.
	 */
	public int getOccupantsCount() {
		return occupantsCount;
	}

	/**
	 * Returns true if the room has restricted the access so that only members
	 * may enter the room.
	 * 
	 * @return true if the room has restricted the access so that only members
	 *         may enter the room.
	 */
	public boolean isMembersOnly() {
		return membersOnly;
	}

	/**
	 * Returns true if the room enabled only participants to speak. Occupants
	 * with a role of visitor won't be able to speak in the room.
	 * 
	 * @return true if the room enabled only participants to speak.
	 */
	public boolean isModerated() {
		return moderated;
	}

	/**
	 * Returns true if presence packets will include the JID of every occupant.
	 * 
	 * @return true if presence packets will include the JID of every occupant.
	 */
	public boolean isNonanonymous() {
		return nonanonymous;
	}

	/**
	 * Returns true if users musy provide a valid password in order to join the
	 * room.
	 * 
	 * @return true if users musy provide a valid password in order to join the
	 *         room.
	 */
	public boolean isPasswordProtected() {
		return passwordProtected;
	}

	/**
	 * Returns true if the room will persist after the last occupant have left
	 * the room.
	 * 
	 * @return true if the room will persist after the last occupant have left
	 *         the room.
	 */
	public boolean isPersistent() {
		return persistent;
	}
	
	/**
	 * Returns true if the room is public
	 * 
	 * @return true if the room is public
	 */
	public boolean isPublic() {
		return publicRoom;
	}
}
