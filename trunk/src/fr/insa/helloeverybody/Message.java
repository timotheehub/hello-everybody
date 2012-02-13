package fr.insa.helloeverybody;

public class Message {
	
	private Profil contact;
	private String message;
	
	public Profil getContact() {
		return contact;
	}
	public void setContact(Profil contact) {
		this.contact = contact;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}
