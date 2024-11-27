package com.model;

import java.util.ArrayList;

public class OllamaRequest {
    public String model;

    public ArrayList<OllamaMessage> messages;

    public OllamaOptions options;

    public OllamaRequest(String model, ArrayList<OllamaMessage> messages, OllamaOptions options) {
        this.model = model;
        this.messages = messages;
        this.options = options;
    }

    public ArrayList<OllamaMessage> getMessages() {
        return this.messages;
    }

    public void setMessages(ArrayList<OllamaMessage> messages) {
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public OllamaOptions getOllamaOptions() {
        return options;
    }

    public void setOllamaOptions(OllamaOptions ollamaOptions) {
        this.options = ollamaOptions;
    }
}
