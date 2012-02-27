package fr.insa.helloeverybody.classes;

import java.util.ArrayList;

public class Profile {
    private String given_name;
    private String family_name;
    private Integer age;
    private String situation;
    private ArrayList<String> hobbies;
 
    public Profile(String given_name, String family_name, Integer age, String situation, ArrayList<String> hobbies) {
        this.given_name = given_name;
        this.family_name = family_name;
        this.age = age;
        this.situation = situation;
        this.hobbies = hobbies;
        
    }

	public Profile(String given_name) {
		 this.given_name = given_name;
	 }

	public String getGiven_name() {
		return given_name;
	}

	public void setGiven_name(String given_name) {
		this.given_name = given_name;
	}

	public String getFamily_name() {
		return family_name;
	}

	public void setFamily_name(String family_name) {
		this.family_name = family_name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getSituation() {
		return situation;
	}

	public void setSituation(String situation) {
		this.situation = situation;
	}

	public ArrayList<String> getHobbies() {
		return hobbies;
	}

	public void setHobbies(ArrayList<String> hobbies) {
		this.hobbies = hobbies;
	}

	public void addHobby(String string) {
		this.hobbies.add(string);
	}
	
	public void removeHobby(String hobby) {
		this.hobbies.remove(this.hobbies.indexOf(hobby));
	}
 

 
}
