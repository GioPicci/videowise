package com.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import com.model.Video;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class VideoStatusPollingService {

    @Inject
    EntityManager em;

    private static final int DEFAULT_MAX_ATTEMPTS = 60; // 60 seconds
    private static final long POLLING_INTERVAL = 1000; // 1 second

    public CompletableFuture<Boolean> pollForVideoExtraction(Long videoId) {
        return pollForVideoExtraction(videoId, DEFAULT_MAX_ATTEMPTS);
    }

    public CompletableFuture<Boolean> pollForVideoExtraction(Long videoId, int maxAttempts) {
        return CompletableFuture.supplyAsync(() -> {
            AtomicInteger attempts = new AtomicInteger(0);

            while (attempts.incrementAndGet() <= maxAttempts) {
                try {
                    boolean isExtracted = checkVideoStatus(videoId);
                    boolean isExtractionFailed = checkExtractionError(videoId);
                    if (isExtracted) {
                        handleExtractedVideo(videoId);
                        return true;
                    } else if (isExtractionFailed) {
                        return false;
                    }

                    Thread.sleep(POLLING_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                } catch (Exception e) {
                    System.err.println("Error polling video status: " + e.getMessage());
                    return false;
                }
            }

            System.out.println("Polling timeout reached for video " + videoId);
            return false;
        });
    }

    @Transactional
    protected boolean checkVideoStatus(Long videoId) {
        Video video = em.find(Video.class, videoId);
        if (video != null) {
            return "extracted".equalsIgnoreCase(video.getStatus());
        }
        return false;
    }

    @Transactional
    protected boolean checkExtractionError(Long videoId) {
        Video video = em.find(Video.class, videoId);
        if (video != null) {
            return "extraction_failed".equalsIgnoreCase(video.getStatus());
        }
        return false;
    }

    @Transactional
    protected void handleExtractedVideo(Long videoId) {
        // Here you can implement what needs to happen when the video is extracted
        System.out.println("Video " + videoId + " has been extracted, proceeding with next steps...");
    }
}