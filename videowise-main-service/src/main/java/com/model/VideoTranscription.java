package com.model;

import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_transcriptions")
public class VideoTranscription extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "video_transcriptions_id_seq")
    @SequenceGenerator(name = "video_transcriptions_id_seq", sequenceName = "video_transcriptions_id_seq", allocationSize = 1)
    public Long id;

    @OneToOne
    public Video video;

    public String transcription;

    public String language;

    public String status; // "PENDING", "COMPLETED", "ERROR"

    public LocalDateTime created_at;

    public LocalDateTime updated_at;

    public Long getId(){
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public String getTranscription() {
        return transcription;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }
}
