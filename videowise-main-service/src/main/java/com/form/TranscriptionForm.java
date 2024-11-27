package com.form;

import jakarta.ws.rs.FormParam;

// Classe per il form multipart
public class TranscriptionForm {
    @FormParam("chatId")
    private String chatId;

    @FormParam("videoId")
    private String videoId;

    @FormParam("language")
    private String language;

    public Long getChatIdAsLong() {
        return chatId != null ? Long.parseLong(chatId) : null;
    }

    public Long getVideoIdAsLong() {
        return videoId != null ? Long.parseLong(videoId) : null;
    }

    public String getLanguage() {
        return language;
    }
}
