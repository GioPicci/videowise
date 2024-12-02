![Centered Logo](./videowise-ui-client/VideoWiseLogoNewFont.png)
# <a name="videowise"></a>VideoWise
In an age where video content dominates our digital interactions, finding key information within hours of footage can feel like searching for a needle in a haystack. VideoWise transforms the way you manage and interact with video content by making it **searchable**, **interactive**, and **insightful**. Whether you’re navigating a training session, analyzing a lecture, or creating engaging content, VideoWise makes working with videos more efficient and effective.\
<br>
At its core, VideoWise provides a web application to upload videos, which are then transcribed using [WhisperX](https://github.com/m-bain/whisperX), a highly efficient and accurate tool based on the [Whisper](https://github.com/openai/whisper) OpenAI model. Each sentence is tied to a precise timestamp, enabling effortless navigation through hours of content without the frustration of scrubbing timelines.\
Going beyond transcription, **VideoWise** integrates with [Ollama](https://github.com/ollama/ollama), enabling users to interact with an AI assistant to ask questions about the video, generate summaries, or even create quizzes and documentation. Export options let users save the AI-powered chats in various formats or download the transcribed video with subtitles applied.
## Table of Contents
1. [Introduction](#videowise)
2. [Key Features](#key-features)
3. [Setup](#setup)
   - [Requirements](#requirements)
   - [Installation](#installation)
     - [Simple Setup](#1-simple-setup)
     - [Modular Setup](#2-modular-setup)
4. [How to Use](#how-to-use)
5. [Technical Details](#technical-details)
6. [Limitations](#limitations)
7. [TODOs](#todos)
## <a name="key-features"></a>Key Features
- 📤 **Seamless Video Uploads**: Quickly upload your videos to get started.
- 🎙️ **Accurate Transcription and Translation**: WhisperX ensures high-quality transcriptions in multiple languages `(en, fr, de, es, it, ja, zh, nl, uk, pt)`.
- ⏱️ **Timestamped Navigation**: Automatically associate transcribed sentences with their relative timestamps, enabling effortless navigation through video content.
- 🤖 **AI-Powered Interactions**: Communicate with an AI using Ollama to ask questions about the transcribed video, generate summaries, or create quizzes.
- 📦 **Flexible Export Options**: Export AI-powered chats in various formats or download the transcribed video with subtitles applied.

![Centered Logo](./videowise-ui-client/videowise_ui.png)
## <a name="setup"></a>Setup
### <a name="requirements"></a>Requirements
- **Ollama** (tested with *v0.3.12* and *v0.4.5*)
- **Docker Desktop** (tested with *v24.0.2*) or **Docker Engine** (tested with *v24.0.7*)
- **[OPTIONAL] NVIDIA GPU** with installed drivers  for optimal transcription performance.
### <a name="installation"></a>Installation
**VideoWise** offers two different installation methods:
#### 1. <a name="1-simple-setup"></a>Simple Setup (Recommended for Quick Start)
This method is ideal for users who want a fast and simple installation. It runs the entire application on a single machine.
#### **Steps**
1. Install the required dependencies (Ollama and Docker)
2. Configure the Ollama API URL in the `.env` file. To do so, be sure to substitute `<your_machine_ip>` in `OLLAMA_API_URL` with the actual IP of the machine running Ollama, for example:
   ```markup
   OLLAMA_API_URL=http://127.0.0.1:11434/api/chat
   ```
3. **[OPTIONAL]** Modify the WhisperX and Ollama configuration in the `.env` file.
   ```markup
   # WhisperX Configuration
   WHISPER_MODEL="large-v2" # Whisper transcription model, available models are {tiny, base, small, medium, large-v2, large-v3}

   # Ollama Configuration
   OLLAMA_MODEL="llama3.1:latest" # Ollama chat model
   OLLAMA_CTX_LEN=16000 # Ollama model context length
   OLLAMA_MAX_PRED_LEN=4000 # Ollama model max response length
   ```
5. Run **Docker Compose** to build and start the application:
   ```bash
   docker-compose up --build
   ```
At the end of the process, you'll be able to access the application on port 80 (e.g. `http://localhost:80`). 
#### <a name="2-modular-setup"></a>2. Modular Setup (For Advanced Users) 
The modular setup allows more flexibility and is ideal for separating services onto different machines (e.g., running the WhisperX transcription service on a GPU-equipped system). This setup requires manual configuration of each service.
<br>
- **Application Modules**
  -  🌐 **Main Server**: Acts as a central hub for all communication between modules.
  -  🖥️ **Web UI Client**: The front-end interface for the application.
  -  📁 **FileSystem Server**: Manages uploaded/generated files and handles video streaming.
  -  🐍 **Python Server**: Interfaces with WhisperX for transcription and performs file conversions (HTML to PDF/DOCX).
  -  🗄️ **DataBase Service**: PostgreSQL instance storing non-file data (e.g., chats, users).
- **Steps** 
  1. Install **Docker** on every machine where a service will run and **Ollama** on the one that will provide the AI chat functionality. 
  2. Build and run the **Database Service** with:
     ```bash
     cd videowise-db
     ./start_db.sh
     ```
  3. Build and run the **FileSystem Service** with:
     ```bash
     cd videowise-filesystem-service
     docker build -t videowise-filesystem-service .
     docker run -d --name videowise-filesystem-service -p 8081:8081 videowise-filesystem-service
     ```
  4. Set up the Environment Variables for the **Python Server**.\
     Edit `/videowise-python-service/Dockerfile`, lines _23-24_
     ```markdown
     # ENV FILESYSTEM_API_URL="http://<your_fs_service_ip>:8081" 
     # ENV WHISPER_MODEL="large-v2"
     ```
     uncommenting them and replacing placeholders with the appropriate IP addresses.
  5. Build and run the **Python Service** with:
     ```bash
     cd videowise-python-service
     docker build -t videowise-python-service .
     docker run -d --name videowise-python-service -p 8000:8000 videowise-python-service
     ```
  6. Set up the Environment Variables for the **Main Server**.\
     Edit `/videowise-main-service/Dockerfile`, lines _28-39_
     ```markdown
     # --- External Services ---
     # ENV OLLAMA_API_URL="http://<your_ollama_ip>:11434/api/chat" 
     # ENV QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://<your_db_ip>:5432/video_transcriptions_db" 
     # ENV FILESYSTEM_API_URL="http://<your_fs_service_ip>:8081" 
     # ENV FILESYSTEM_STREAMING_API_URL="http://<your_fs_service_ip>:8081" 
     # ENV WHISPER_API_URL="http://<your_python_service_ip>:8000"
     # --- WhisperX config ---
     # ENV WHISPER_MODEL="large-v2" 
     # --- Ollama config ---
     # ENV OLLAMA_MODEL="llama3.1:latest" 
     # ENV OLLAMA_CTX_LEN=16000 
     # ENV OLLAMA_MAX_PRED_LEN=4000 
     ```
     uncommenting them and replacing placeholders with the appropriate IP addresses.
  7. Build and run the **Main Server** with:
     ```bash
     cd videowise-main-service
     docker build -t videowise-main-service .
     docker run -d --name videowise-main-service -p 8080:8080 videowise-main-service
     ```
  8. Set up the *Main Service URL* Environment variable for the **Web UI Client**, in `/videowise-ui-client/Dockerfile`
     ```markdown
     # ENV MAIN_SERVICE_URL="http://<your_main_service_ip>:8080"
     ```
     Uncomment this line and substitute the placeholder with the main service host machine's IP address.
  9. Build and run the **Web UI Client** with:
     ```bash
     cd videowise-ui-client
     docker build -t videowise-ui-client .
     docker run -d --name videowise-ui-client -p 80:80 videowise-ui-client
     ```
## <a name="how-to-use"></a>How to Use
- 🆕 **Create a new Chat:** Begin by clicking on the _New Chat_ button.
- 🎥 **Upload a Video:** Drag and drop your video file onto the right side of the interface.  
- ⏳ **Wait for Transcription:** Allow the system to process and transcribe the video content.  
- 🤖 **Interact with AI:** Check the "Inject Video Context" option to provide the AI with video context, then ask questions, generate summaries, or even create quizzes. 
- 💾 **Export Options:** Export the transcribed video with embedded subtitles or save the AI chat content in PDF, Word, or TXT format. 
## <a name="technical-details"></a>Technical Details
- By default, the Python Server employs the `large-v2` model for video transcription. You can change this setting in the `.env` file ([Simple Setup](#1-simple-setup)), or `videowise-main-service/Dockerfile` ([Modular Setup](#2-modular-setup)):
  ```python
  ENV WHISPER_MODEL="large-v2"
  ```
- To optimize memory usage, the server automatically clears models from memory after 5 minutes of inactivity. You can adjust the timeout or disable it entirely:
  ```python
  # `videowise-python-service/main.py`
  
  whisperx_manager = WhisperXModelManager(
     model_name=model_name,
     device="cuda" if torch.cuda.is_available() else "cpu",
     timeout=300 # Release timeout, in seconds
     auto_release=True # True: If unused, resources are released after the timeout; False: Disable resource auto-release.
  )
  ```
- The default Large Language Model (LLM) for AI interactions is set to `"llama3.1:latest"`. You can change this setting in the `.env` file ([Simple Setup](#1-simple-setup)), or `videowise-main-service/Dockerfile` ([Modular Setup](#2-modular-setup)):
  ```markup
  ENV OLLAMA_MODEL="llama3.1:latest" 
  ```
  The maximum response length is capped at `4.000 tokens` and context length is set to `16.000 tokens`. These can also be adjusted:
  ```markup
  ENV OLLAMA_CTX_LEN=16000 
  ENV OLLAMA_MAX_PRED_LEN=4000 
  ```
## <a name="limitations"></a>Limitations 
- Speaker diarization (identifying speakers in audio) is currently not supported.
- Chats are limited to a single video as the context.
- The system integrates exclusively with Ollama and does not yet support other AI models such as ChatGPT, Gemini, or Claude.
- Audio file uploads are not supported.
- Minor bugs may occur as VideoWise is actively under development.
## <a name="todos"></a>TODOs
- [ ] Enable multi-video uploads within a single chat.
- [ ] Add support for popular AI models (e.g., ChatGPT, Gemini, Claude).
- [ ] Implement speaker diarization.
- [ ] Add functionality to upload videos directly from YouTube URLs.
- [ ] Introduce voice chat for real-time interaction with AI.
- [ ] Support direct uploads of audio files.
- [ ] Expand question presets for improved AI interactions.

     
