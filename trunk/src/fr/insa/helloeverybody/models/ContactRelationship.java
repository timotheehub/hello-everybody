package fr.insa.helloeverybody.models;

public class ContactRelationship {
	private String jid;
	private Boolean favorite;
	private Boolean known;
	private Boolean recommended;

	public ContactRelationship() { }
	
	public ContactRelationship(String jid, Boolean favorite, 
			Boolean known, Boolean recommended) {
		super();
		this.jid = jid;
		this.favorite = favorite;
		this.known = known;
		this.recommended = recommended;
	}
	
	public ContactRelationship(Profile profile) {
		this.jid = profile.getJid();
		this.favorite = profile.isFavorite();
		this.known = profile.isKnown();
		this.recommended = profile.isRecommended();
	}
	
	// Getters et Setters
	public String getJid() {
		return jid;
	}
	public void setJid(String jid) {
		this.jid = jid;
	}
	public Boolean getFavorite() {
		return favorite;
	}
	public void setFavorite(Boolean favorite) {
		this.favorite = favorite;
	}
	public Boolean getKnown() {
		return known;
	}
	public void setKnown(Boolean known) {
		this.known = known;
	}
	public Boolean getRecommended() {
		return recommended;
	}
	public void setRecommended(Boolean recommended) {
		this.recommended = recommended;
	}
}
