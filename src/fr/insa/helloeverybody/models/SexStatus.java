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
    
	public static SexStatus fromString(String text) {
		if (text != null) {
			for (SexStatus b : SexStatus.values()) {
				if (text.equalsIgnoreCase(b.text)) {
					return b;
				}
			}
		}
		return MAN;
	}
}
