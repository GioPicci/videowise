package com.model;
import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "videos")
public class Video extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "videos_id_seq")
    @SequenceGenerator(name = "videos_id_seq", sequenceName = "videos_id_seq", allocationSize = 1)
    public Long id;

    @ManyToOne
    public Chat chat;

    public String videoPath;

    public String videoTitle;

    public String videoMetadata;

    public String status;

    public Long getId() {
        return id;
    }

    public String getVideoMetadata() {
        return videoMetadata;
    }

    public void setVideoMetadata(String videoMetadata) {
        this.videoMetadata = videoMetadata;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Method to check if a Video with the given videopath exists
    public static boolean existsByVideoPath(String videoPath) {
        return count("videoPath", videoPath) > 0;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }
}
