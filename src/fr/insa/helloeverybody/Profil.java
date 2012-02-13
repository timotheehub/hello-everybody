package fr.insa.helloeverybody;

public class Profil {
	
	private boolean user;
	private int avatar;
	private String firstName;
	private String name;
	private String ip;
	
	public Profil() {
		
	}
	
	public Profil(String firstName, String lastName, String ip) {
		this.firstName = firstName;
		this.name = lastName;
		this.ip = ip;
	}
	
	public String getPrenom() {
		return firstName;
	}
	public void setPrenom(String firstName) {
		this.firstName = firstName;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public int getAvatar() {
		return avatar;
	}
	public void setAvatar(int avatar) {
		this.avatar = avatar;
	}
	
	public boolean isUser() {
		return user;
	}
	public void setUser(boolean user) {
		this.user = user;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
}
