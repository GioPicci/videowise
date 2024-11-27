-- users table creation
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

-- user_preferences table creation
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    preference_key VARCHAR(255) NOT NULL,
    preference_value VARCHAR(255) NOT NULL,
    preference_description TEXT,
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- chats table creation
CREATE TABLE IF NOT EXISTS chats (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    chat_msg TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- videos table creation
CREATE TABLE IF NOT EXISTS videos (
    id BIGSERIAL PRIMARY KEY,
    videoPath VARCHAR(255) NOT NULL,
    videoTitle VARCHAR(255) NOT NULL,
    videoMetadata TEXT,
    status VARCHAR(255),
    chat_id BIGINT NOT NULL,
    CONSTRAINT fk_chat
        FOREIGN KEY (chat_id)
        REFERENCES chats(id)
        ON DELETE CASCADE
);

-- video_transcriptions table creation
CREATE TABLE IF NOT EXISTS video_transcriptions (
    id BIGSERIAL PRIMARY KEY,
    video_id BIGINT NOT NULL,
    transcription TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    language VARCHAR(10) DEFAULT 'auto',
    CONSTRAINT fk_video
        FOREIGN KEY (video_id)
        REFERENCES videos(id)
        ON DELETE CASCADE
);

-- Indexes for better performance (optional)
--CREATE INDEX idx_users_username ON users(username);
--CREATE INDEX idx_chats_user_id ON chats(user_id);
--CREATE INDEX idx_transcriptions_chat_id ON video_transcriptions(chat_id);
--CREATE INDEX idx_videos ON videos(video_id);