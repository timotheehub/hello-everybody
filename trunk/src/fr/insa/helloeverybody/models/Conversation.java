package fr.insa.helloeverybody.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class Conversation {
	
	// Attributs
	private boolean isPublic = false;
	private Long id;
	private String title;
	private List<Profile> members = new ArrayList<Profile>();
	private int nbUnreadMessages = 0;
	
	
	
	// Constructeurs
	public Conversation() {
		setDefault();
	}
	
	public Conversation(boolean isPublic, String title) {
		setDefault();
		this.isPublic = isPublic;
		this.title = title;
	}
	
	private void setDefault() {
		id = new Random().nextLong();
	}





	// Liste des participants 
	public void addMember(Profile profile) {
		members.add(profile);
	}
	
	public boolean removeMember(Profile profile) {
		return members.remove(profile);
	}
	

	
	
	// Getters et setters
	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getNbUnreadMessages() {
		return nbUnreadMessages;
	}

	public void setNbUnreadMessages(int nbUnreadMessages) {
		this.nbUnreadMessages = nbUnreadMessages;
	}
	
	
}
