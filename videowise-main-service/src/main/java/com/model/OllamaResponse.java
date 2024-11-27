package com.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OllamaResponse {
    private String model;

    @JsonProperty("created_at")
    private String createdAt; // Use appropriate type for date if needed

    private OllamaMessage message;

    private boolean done;

    // Optionally add these for the final response
    @JsonProperty("done_reason")
    private String doneReason;

    @JsonProperty("total_duration")
    private String totalDuration;

    @JsonProperty("load_duration")
    private String loadDuration;

    @JsonProperty("prompt_eval_count")
    private String promptEvalCount;

    @JsonProperty("prompt_eval_duration")
    private String promptEvalDuration;

    @JsonProperty("eval_count")
    private String evalCount;

    @JsonProperty("eval_duration")
    private String evalDuration;

    // Getters and setters

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public OllamaMessage getMessage() {
        return message;
    }

    public void setMessage(OllamaMessage message) {
        this.message = message;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getDoneReason() {
        return doneReason;
    }

    public void setDoneReason(String doneReason) {
        this.doneReason = doneReason;
    }

    public String getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(String totalDuration) {
        this.totalDuration = totalDuration;
    }
}


