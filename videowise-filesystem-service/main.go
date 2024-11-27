package main

import (
    "fmt"
    "io"
    "log"
	"os/exec"
    "net/http"
    "os"
    "path/filepath"
    "strings"
)

const (
    uploadDir = "./uploads/"
)

func main() {
    // Create the upload directory if it doesn't exist
    if err := os.MkdirAll(uploadDir, os.ModePerm); err != nil {
        log.Fatalf("Failed to create upload directory: %v", err)
    }

    http.HandleFunc("/upload_stream", uploadStreamHandler)
    http.HandleFunc("/download/", downloadHandler)
    http.HandleFunc("/extract_audio", extractAudioHandler)
	http.HandleFunc("/stream/", streamHandler)
    http.HandleFunc("/delete/", deleteHandler)

    fmt.Println("Server is listening on 0.0.0.0:8081")
    log.Fatal(http.ListenAndServe("0.0.0.0:8081", nil))
}

// uploadStreamHandler handles the /upload_stream endpoint
func uploadStreamHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("Called /upload_stream endpoint")
    // Only accept POST method
    if r.Method != http.MethodPost {
        http.Error(w, "Invalid request method", http.StatusMethodNotAllowed)
        return
    }

	fmt.Println("Extracting title from headers")
    // Get the title from the header
    title := r.Header.Get("title")
    if title == "" {
        http.Error(w, "Missing 'title' header", http.StatusBadRequest)
        return
    }

    // Create the file path
    filePath := filepath.Join(uploadDir, title)
    
	fmt.Println("Creating empy file")
    // Create the file
    out, err := os.Create(filePath)
    if err != nil {
        http.Error(w, "Unable to create file", http.StatusInternalServerError)
        return
    }
    defer out.Close()

	fmt.Println("Copying stream in the empty file")
    // Copy the uploaded data to the file
    _, err = io.Copy(out, r.Body)
    if err != nil {
        http.Error(w, "Failed to save file", http.StatusInternalServerError)
        return
    }

    w.WriteHeader(http.StatusOK)
    fmt.Fprintf(w, "File uploaded successfully: %s", title)
	fmt.Printf("File uploaded successfully: %s\n", title)

}

// downloadHandler handles the /download/{filename} endpoint
func downloadHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("Called /download endpoint")
    filename := strings.TrimPrefix(r.URL.Path, "/download/")
    filePath := filepath.Join(uploadDir, filename)

    // Check if file exists
	fmt.Println("Check if requested file exists")
    if _, err := os.Stat(filePath); os.IsNotExist(err) {
        http.Error(w, "File not found", http.StatusNotFound)
        return
    }

	fmt.Println("Prepare response")
    // Set the appropriate headers
    w.Header().Set("Content-Disposition", "attachment; filename="+filename)
	// Determine content type based on file extension
    ext := strings.ToLower(filepath.Ext(filename))
    switch ext {
    case ".mp3":
        w.Header().Set("Content-Type", "audio/mpeg")
    case ".mp4":
        w.Header().Set("Content-Type", "video/mp4")
    // Add other cases for different file types as needed
    default:
        w.Header().Set("Content-Type", "application/octet-stream")
    }
    
	fmt.Println("Serving the file")
    // Serve the file
    http.ServeFile(w, r, filePath)
}

func deleteHandler(w http.ResponseWriter, r *http.Request) {
    fmt.Println("Called /delete endpoint")

    // Extract the file name from the URL
    fileName := strings.TrimPrefix(r.URL.Path, "/delete/")

    // Validate the filename (no path traversal, only base name)
    if strings.Contains(fileName, "..") || strings.Contains(fileName, "/") {
        http.Error(w, "Invalid filename", http.StatusBadRequest)
        return
    }

    // Construct the full file path
    filePath := filepath.Join(uploadDir, fileName)

    // Check if the file exists
    if _, err := os.Stat(filePath); os.IsNotExist(err) {
        http.Error(w, "File not found", http.StatusNotFound)
        return
    }

    // Attempt to delete the file
    if err := os.Remove(filePath); err != nil {
        http.Error(w, "Error deleting file", http.StatusInternalServerError)
        return
    }

    // Return success message
    w.WriteHeader(http.StatusOK)
    fmt.Fprintf(w, "File %s deleted successfully", fileName)
}

// extractAudioHandler handles the /extract_audio endpoint
func extractAudioHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("Called /extract_audio endpoint")
    // Parse the multipart form
    err := r.ParseMultipartForm(1024 << 20) // limit your max input length to 1GB
    if err != nil {
        http.Error(w, "Error parsing multipart form", http.StatusBadRequest)
        return
    }

	fmt.Println("Extracting title from form")
    // Get the title from the form data
    title := r.FormValue("title")
    if title == "" {
        http.Error(w, "Missing 'title' form field", http.StatusBadRequest)
        return
    }

    // Define input and output file paths
    inputPath := filepath.Join(uploadDir, title)
    outputPath := filepath.Join(uploadDir, strings.TrimSuffix(title, filepath.Ext(title))+".mp3")

	fmt.Println("Launching FFMPEG process")
    // Command to convert video to audio using ffmpeg
    cmd := fmt.Sprintf("ffmpeg -y -i %s -vn -acodec libmp3lame -q:a 2 %s", inputPath, outputPath)
    if err := exec.Command("bash", "-c", cmd).Run(); err != nil {
        http.Error(w, "Error extracting audio", http.StatusInternalServerError)
        return
    }

	fmt.Printf("Audio extracted successfully: %s\n", outputPath)
    w.WriteHeader(http.StatusOK)
    fmt.Fprintf(w, "Audio extracted successfully: %s", outputPath)
}

// streamHandler handles the /stream/{video_name} endpoint
func streamHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("Called /stream endpoint")
    // Extract the video name from the URL
    videoName := strings.TrimPrefix(r.URL.Path, "/stream/")

    // Validate the filename (no path traversal, only base name)
    if strings.Contains(videoName, "..") || strings.Contains(videoName, "/") {
        http.Error(w, "Invalid filename", http.StatusBadRequest)
        return
    }

    // Validate that the filename has a valid video extension
    ext := strings.ToLower(filepath.Ext(videoName))
    if ext != ".mp4" && ext != ".mkv" {
        http.Error(w, "Invalid file type. Only .mp4 and .mkv files are allowed.", http.StatusBadRequest)
        return
    }

    // Construct the full file path
    filePath := filepath.Join(uploadDir, videoName)

    // Check if the video file exists
    if _, err := os.Stat(filePath); os.IsNotExist(err) {
        http.Error(w, "Video not found", http.StatusNotFound)
        return
    }

    // Set the appropriate headers for video streaming
    w.Header().Set("Content-Type", "video/mp4")
    w.Header().Set("Accept-Ranges", "bytes")

    // Open the video file
    file, err := os.Open(filePath)
    if err != nil {
        http.Error(w, "Error opening video file", http.StatusInternalServerError)
        return
    }
    defer file.Close()

    // Get the file info for Content-Length
    fileInfo, err := file.Stat()
    if err != nil {
        http.Error(w, "Error getting file info", http.StatusInternalServerError)
        return
    }

    // Set Content-Length header
    w.Header().Set("Content-Length", fmt.Sprintf("%d", fileInfo.Size()))

    fmt.Println("Start streaming content")
    // Stream the video file
    http.ServeContent(w, r, videoName, fileInfo.ModTime(), file)
}
