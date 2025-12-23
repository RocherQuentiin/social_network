CREATE TABLE IF NOT EXISTS user (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    bio TEXT,
    profile_picture_url VARCHAR(255),
    cover_picture_url VARCHAR(255),
    is_verified BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS profile (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) UNIQUE NOT NULL,
    location VARCHAR(100),
    website VARCHAR(255),
    phone_number VARCHAR(20),
    birthdate DATE,
    user_gender ENUM('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY'),
    profession VARCHAR(100),
    company VARCHAR(100),
    education VARCHAR(255),
    isep_specialization ENUM ('SOFTWARE_ENGINEERING', 'DATA_SCIENCE', 'CYBERSECURITY', 'EMBEDDED_SYSTEMS'),
    promo_year SMALLINT,
    interests JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS post (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    author_id CHAR(36) NOT NULL,
    content TEXT NOT NULL,
    visibility_type ENUM ('PUBLIC', 'FRIENDS', 'PRIVATE') DEFAULT 'PUBLIC',
    allow_comments BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comment (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    post_id CHAR(36) NOT NULL,
    author_id CHAR(36) NOT NULL,
    content TEXT NOT NULL,
    parent_comment_id CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_comment_id) REFERENCES comment(id) ON DELETE CASCADE
);

-- Messaging
-- create conversation (no circular FK to message yet)
CREATE TABLE IF NOT EXISTS conversation (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    participant_1_id CHAR(36) NOT NULL,
    participant_2_id CHAR(36) NOT NULL,
    last_message_id CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversation_participant1 FOREIGN KEY (participant_1_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_participant2 FOREIGN KEY (participant_2_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT unique_conversation UNIQUE(participant_1_id, participant_2_id),
    CONSTRAINT prevent_self_conversation CHECK (participant_1_id < participant_2_id)
);

-- create message (references conversation)
CREATE TABLE IF NOT EXISTS message (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    sender_id CHAR(36) NOT NULL,
    recipient_id CHAR(36) NOT NULL,
    conversation_id CHAR(36) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_recipient FOREIGN KEY (recipient_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE
);

-- add circular FK from conversation.last_message_id -> message.id
ALTER TABLE conversation
  ADD CONSTRAINT fk_conversation_last_message
  FOREIGN KEY (last_message_id) REFERENCES message(id) ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS media (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    post_id CHAR(36),
    comment_id CHAR(36),
    message_id CHAR(36),
    user_id CHAR(36),
    file_url VARCHAR(500) NOT NULL,
    media_type ENUM ('IMAGE', 'VIDEO', 'AUDIO', 'DOCUMENT') NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_media_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_media_comment FOREIGN KEY (comment_id) REFERENCES comment(id) ON DELETE CASCADE,
    CONSTRAINT fk_media_message FOREIGN KEY (message_id) REFERENCES message(id) ON DELETE CASCADE,
    CONSTRAINT fk_media_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `like` (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    post_id CHAR(36),
    comment_id CHAR(36),
    reaction_type ENUM ('LIKE', 'LOVE', 'HAHA', 'WOW', 'SAD', 'ANGRY') DEFAULT 'LIKE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_like_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_like_comment FOREIGN KEY (comment_id) REFERENCES comment(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_post_like UNIQUE(user_id, post_id),
    CONSTRAINT unique_user_comment_like UNIQUE(user_id, comment_id),
    CHECK ((post_id IS NOT NULL AND comment_id IS NULL) OR (post_id IS NULL AND comment_id IS NOT NULL))
);

-- Graph / FOAF
CREATE TABLE IF NOT EXISTS connection (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    requester_id CHAR(36) NOT NULL,
    receiver_id CHAR(36) NOT NULL,
    connection_status ENUM ('PENDING', 'ACCEPTED', 'BLOCKED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_connection_requester FOREIGN KEY (requester_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_connection_receiver FOREIGN KEY (receiver_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT unique_connection UNIQUE(requester_id, receiver_id),
    CONSTRAINT prevent_self_connection CHECK (requester_id != receiver_id)
);

CREATE TABLE IF NOT EXISTS follow (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    follower_id CHAR(36) NOT NULL,
    following_id CHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_follow_following FOREIGN KEY (following_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT unique_follow UNIQUE(follower_id, following_id),
    CONSTRAINT prevent_self_follow CHECK (follower_id != following_id)
);

-- Notifications
CREATE TABLE IF NOT EXISTS notification (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    recipient_id CHAR(36) NOT NULL,
    actor_id CHAR(36) NOT NULL,
    notification_type ENUM ('FRIEND_REQUEST', 'POST_LIKE', 'COMMENT', 'MESSAGE', 'MENTION', 'FOLLOW') NOT NULL,
    related_post_id CHAR(36),
    related_comment_id CHAR(36),
    content VARCHAR(255),
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_recipient FOREIGN KEY (recipient_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_actor FOREIGN KEY (actor_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_post FOREIGN KEY (related_post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_comment FOREIGN KEY (related_comment_id) REFERENCES comment(id) ON DELETE CASCADE
);

-- Privacy
CREATE TABLE IF NOT EXISTS privacy_settings (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) UNIQUE NOT NULL,
    can_see_email BOOLEAN DEFAULT false,
    can_see_phone BOOLEAN DEFAULT false,
    can_see_birthdate BOOLEAN DEFAULT false,
    can_see_location BOOLEAN DEFAULT false,
    allow_direct_messages BOOLEAN DEFAULT true,
    allow_friend_requests BOOLEAN DEFAULT true,
    block_list JSON DEFAULT ('{}'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_privacy_settings_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- Collaboration
CREATE TABLE IF NOT EXISTS project (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    creator_id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    visibility_type ENUM ('PUBLIC', 'FRIENDS', 'PRIVATE') DEFAULT 'PRIVATE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_creator FOREIGN KEY (creator_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS project_member (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    project_member_role ENUM ('OWNER', 'ADMIN', 'MEMBER') DEFAULT 'MEMBER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_member_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_member_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT unique_project_member UNIQUE(project_id, user_id)
);

CREATE TABLE IF NOT EXISTS event (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    creator_id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    event_date TIMESTAMP NOT NULL,
    capacity INT DEFAULT 0,
    location VARCHAR(255),
    visibility_type ENUM('PUBLIC','FRIENDS','PRIVATE') DEFAULT 'PUBLIC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_creator FOREIGN KEY (creator_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS event_attendee (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    event_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    event_attendance_status ENUM ('PENDING', 'ACCEPTED', 'DECLINED', 'MAYBE') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_attendee_event FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_attendee_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT unique_event_attendee UNIQUE(event_id, user_id)
);

-- Recommendations
CREATE TABLE IF NOT EXISTS recommendation (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    recommended_user_id CHAR(36) NOT NULL,
    score FLOAT CHECK (score >= 0 AND score <= 100),
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recommendation_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_recommendation_recommended_user FOREIGN KEY (recommended_user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT prevent_self_recommendation CHECK (user_id != recommended_user_id)
);
 
-- Reports
CREATE TABLE IF NOT EXISTS report (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    reporter_id CHAR(36) NOT NULL,
    reported_user_id CHAR(36),
    reported_post_id CHAR(36),
    reported_comment_id CHAR(36),
    report_type ENUM('SPAM','HARASSMENT','INAPPROPRIATE_CONTENT','HATE_SPEECH','FAKE_ACCOUNT','VIOLENCE','MISINFORMATION','OTHER') NOT NULL,
    description TEXT,
    status ENUM('PENDING','UNDER_REVIEW','RESOLVED','DISMISSED') DEFAULT 'PENDING',
    reviewed_by CHAR(36),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_report_reported_user FOREIGN KEY (reported_user_id) REFERENCES user(id) ON DELETE SET NULL,
    CONSTRAINT fk_report_reported_post FOREIGN KEY (reported_post_id) REFERENCES post(id) ON DELETE SET NULL,
    CONSTRAINT fk_report_reported_comment FOREIGN KEY (reported_comment_id) REFERENCES comment(id) ON DELETE SET NULL,
    CONSTRAINT fk_report_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES user(id) ON DELETE SET NULL
);