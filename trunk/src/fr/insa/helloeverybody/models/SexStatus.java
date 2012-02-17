package fr.insa.helloeverybody.models;

public enum SexStatus {
	MAN("Homme"),
	WOMAN("Femme");
   
	private final String text;
	   
    private SexStatus(String text) {
        this.text = text;
    }
   
    @Override
    public String toString() {
        return text;
    }
}
