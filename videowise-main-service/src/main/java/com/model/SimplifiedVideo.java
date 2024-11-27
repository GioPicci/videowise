package com.model;

public class SimplifiedVideo {
    private final Long id;
    private final String videoPath;
    private final String videoTitle;
    private final String videoMetadata;
    private final String status;

    // Constructors, getters, and setters
    public SimplifiedVideo(Long id, String videoPath, String videoTitle, String videoMetadata, String status) {
        this.id = id;
        this.videoPath = videoPath;
        this.videoTitle = videoTitle;
        this.videoMetadata = videoMetadata;
        this.status = status;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public String getVideoMetadata() {
        return videoMetadata;
    }

    public String getStatus() {
        return status;
    }
}

