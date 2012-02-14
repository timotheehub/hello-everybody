package fr.insa.helloeverybody;

//Enum�ration des types de situations
public enum RelationshipStatus {
    SINGLE("C�libataire"),
    COUPLE("En couple"),
    SECRET("Top secret");
   
    private RelationshipStatus(String text) {
        this.text = text;
    }

    private final String text;
   
    @Override
    public String toString() {
        return text;
    }
}
