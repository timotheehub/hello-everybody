package fr.insa.helloeverybody.models;

//EnumÃ©ration des types de situations
public enum RelationshipStatus {
    SINGLE("CÃ©libataire"),
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
