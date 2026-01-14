-- =====================================================
-- Messaging System Tables
-- =====================================================

-- Drop tables if they exist (for development only)
-- DROP TABLE IF EXISTS project_message;
-- DROP TABLE IF EXISTS project_message_group;
-- DROP TABLE IF EXISTS message;
-- DROP TABLE IF EXISTS conversation;
-- DROP TABLE IF EXISTS notification;

-- =====================================================
-- Conversation Table (Direct Messages)
-- =====================================================
CREATE TABLE IF NOT EXISTS conversation (
    id BINARY(16) PRIMARY KEY,
    participant_1_id BINARY(16) NOT NULL,
    participant_2_id BINARY(16) NOT NULL,
    last_message_id BINARY(16),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_conv_participant1 FOREIGN KEY (participant_1_id) 
        REFERENCES `user`(id) ON DELETE CASCADE,
    CONSTRAINT fk_conv_participant2 FOREIGN KEY (participant_2_id) 
        REFERENCES `user`(id) ON DELETE CASCADE,
    
    UNIQUE KEY unique_conversation (participant_1_id, participant_2_id),
    KEY idx_participant1 (participant_1_id),
    KEY idx_participant2 (participant_2_id),
    KEY idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Message Table (Direct Messages)
-- =====================================================
CREATE TABLE IF NOT EXISTS message (
    id BINARY(16) PRIMARY KEY,
    sender_id BINARY(16) NOT NULL,
    recipient_id BINARY(16) NOT NULL,
    conversation_id BINARY(16) NOT NULL,
    content LONGTEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) 
        REFERENCES `user`(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_recipient FOREIGN KEY (recipient_id) 
        REFERENCES `user`(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_conversation FOREIGN KEY (conversation_id) 
        REFERENCES conversation(id) ON DELETE CASCADE,
    
    KEY idx_sender (sender_id),
    KEY idx_recipient (recipient_id),
    KEY idx_conversation (conversation_id),
    KEY idx_created_at (created_at),
    KEY idx_is_read (is_read),
    FULLTEXT idx_content (content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Add foreign key for last_message_id in conversation
-- =====================================================
ALTER TABLE conversation 
ADD CONSTRAINT fk_conv_last_message FOREIGN KEY (last_message_id) 
    REFERENCES message(id) ON DELETE SET NULL;

-- =====================================================
-- Project Message Group Table
-- =====================================================
CREATE TABLE IF NOT EXISTS project_message_group (
    id BINARY(16) PRIMARY KEY,
    project_id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_pmg_project FOREIGN KEY (project_id) 
        REFERENCES project(id) ON DELETE CASCADE,
    
    KEY idx_project (project_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Project Message Table
-- =====================================================
CREATE TABLE IF NOT EXISTS project_message (
    id BINARY(16) PRIMARY KEY,
    message_group_id BINARY(16) NOT NULL,
    sender_id BINARY(16) NOT NULL,
    content LONGTEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_pm_group FOREIGN KEY (message_group_id) 
        REFERENCES project_message_group(id) ON DELETE CASCADE,
    CONSTRAINT fk_pm_sender FOREIGN KEY (sender_id) 
        REFERENCES `user`(id) ON DELETE CASCADE,
    
    KEY idx_group (message_group_id),
    KEY idx_sender (sender_id),
    KEY idx_created_at (created_at),
    KEY idx_is_read (is_read),
    FULLTEXT idx_content (content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Notification Table (if not already exists)
-- =====================================================
CREATE TABLE IF NOT EXISTS notification (
    id BINARY(16) PRIMARY KEY,
    recipient_id BINARY(16) NOT NULL,
    actor_id BINARY(16),
    notification_type VARCHAR(50) NOT NULL,
    related_post_id BINARY(16),
    related_comment_id BINARY(16),
    content LONGTEXT,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_notif_recipient FOREIGN KEY (recipient_id) 
        REFERENCES `user`(id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_actor FOREIGN KEY (actor_id) 
        REFERENCES `user`(id) ON DELETE SET NULL,
    CONSTRAINT fk_notif_post FOREIGN KEY (related_post_id) 
        REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_comment FOREIGN KEY (related_comment_id) 
        REFERENCES comment(id) ON DELETE CASCADE,
    
    KEY idx_recipient (recipient_id),
    KEY idx_actor (actor_id),
    KEY idx_type (notification_type),
    KEY idx_created_at (created_at),
    KEY idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Indexes for performance
-- =====================================================

-- Performance indexes for common queries
CREATE INDEX idx_conversation_users ON conversation(participant_1_id, participant_2_id);
CREATE INDEX idx_message_unread ON message(recipient_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_pm_group_sender ON project_message(message_group_id, sender_id);
CREATE INDEX idx_pmg_project_created ON project_message_group(project_id, created_at);
CREATE INDEX idx_notification_unread ON notification(recipient_id, is_read) WHERE is_read = FALSE;

-- =====================================================
-- Sample Data (optional, for testing)
-- =====================================================
-- INSERT INTO conversation (id, participant_1_id, participant_2_id, created_at, updated_at)
-- VALUES (UUID_TO_BIN(UUID()), user_id_1, user_id_2, NOW(), NOW());
