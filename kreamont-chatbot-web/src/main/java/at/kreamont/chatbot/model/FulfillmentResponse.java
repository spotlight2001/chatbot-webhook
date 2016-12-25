package at.kreamont.chatbot.model;

public class FulfillmentResponse {
    private String speech;
    private String source = "telefonliste";

    public String getSpeech() {
        return speech;
    }

    public void setSpeech(String speech) {
        this.speech = speech;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}