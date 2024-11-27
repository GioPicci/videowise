package com.form;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class AudioTranscriptionForm {

    @PartType(MediaType.TEXT_PLAIN)
    @FormParam("title")
    private String title;

    @PartType(MediaType.TEXT_PLAIN)
    @FormParam("id")
    private String id;

    public AudioTranscriptionForm(String title, String id) {
        this.title = title;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
