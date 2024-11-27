package com.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TranscriptionResponse {
    @JsonProperty("transcription_token")
    private String transcriptionToken;
    private String status;
    private String message;
    private String output_path;
    private Output output;

    // Getters and setters
    public String getTranscriptionToken() {
        return transcriptionToken;
    }

    public void setTranscriptionToken(String transcriptionToken) {
        this.transcriptionToken = transcriptionToken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOutput_path() {
        return output_path;
    }

    public void setOutput_path(String output_path) {
        this.output_path = output_path;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }
}
