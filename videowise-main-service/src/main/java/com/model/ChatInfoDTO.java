package com.model;
import java.time.LocalDateTime;

public class ChatInfoDTO {
    public Long id;
    public String title;
    public LocalDateTime created_at;
    public LocalDateTime updated_at;

    public ChatInfoDTO(Long id, String title, LocalDateTime created_at, LocalDateTime updated_at) {
        this.id = id;
        this.title = title;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }
}

