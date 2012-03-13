package fr.insa.helloeverybody.models;

public class Contact {
	private String jid;
	private Boolean favorite;
	private Boolean known;
	private Boolean recommend;
	

	public Contact(String jid, Boolean favorite, Boolean known,
			Boolean recommend) {
		super();
		this.jid = jid;
		this.favorite = favorite;
		this.known = known;
		this.recommend = recommend;
	}
	public Contact() {
		// TODO Auto-generated constructor stub
	}
	public Contact(Profile profile) {
		this.jid = profile.getJid();
		this.favorite = profile.isFavorite();
		this.known = profile.isKnown();
		this.recommend = profile.isRecommended();
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
	public Boolean getRecommend() {
		return recommend;
	}
	public void setRecommend(Boolean recommend) {
		this.recommend = recommend;
	}
	
	
}
