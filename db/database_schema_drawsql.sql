-- TSN (Tailored Social Network) - PostgreSQL DDL for DrawSQL import
-- Safe to paste into DrawSQL.app (PostgreSQL mode)
-- Extension (optional for UUID generation):
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ENUM types
CREATE TYPE user_gender AS ENUM ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY');
CREATE TYPE visibility_type AS ENUM ('PUBLIC', 'FRIENDS', 'PRIVATE');
CREATE TYPE media_type AS ENUM ('IMAGE', 'VIDEO', 'AUDIO', 'DOCUMENT');
CREATE TYPE reaction_type AS ENUM ('LIKE', 'LOVE', 'HAHA', 'WOW', 'SAD', 'ANGRY');
CREATE TYPE connection_status AS ENUM ('PENDING', 'ACCEPTED', 'BLOCKED');
CREATE TYPE notification_type AS ENUM ('FRIEND_REQUEST', 'POST_LIKE', 'COMMENT', 'MESSAGE', 'MENTION', 'FOLLOW');
CREATE TYPE event_attendance_status AS ENUM ('PENDING', 'ACCEPTED', 'DECLINED', 'MAYBE');
CREATE TYPE project_member_role AS ENUM ('OWNER', 'ADMIN', 'MEMBER');
CREATE TYPE isep_specialization AS ENUM ('SOFTWARE_ENGINEERING', 'DATA_SCIENCE', 'CYBERSECURITY', 'EMBEDDED_SYSTEMS');

-- Core tables
CREATE TABLE "user" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
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

CREATE TABLE profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL,
    location VARCHAR(100),
    website VARCHAR(255),
    phone_number VARCHAR(20),
    birthdate DATE,
    user_gender user_gender,
    profession VARCHAR(100),
    company VARCHAR(100),
    education VARCHAR(255),
    isep_specialization isep_specialization,
    promo_year SMALLINT,
    interests JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE TABLE post (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL,
    content TEXT NOT NULL,
    visibility_type visibility_type DEFAULT 'PUBLIC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE TABLE comment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL,
    author_id UUID NOT NULL,
    content TEXT NOT NULL,
    parent_comment_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_comment_id) REFERENCES comment(id) ON DELETE CASCADE
);

CREATE TABLE media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID,
    comment_id UUID,
    message_id UUID,
    user_id UUID,
    file_url VARCHAR(500) NOT NULL,
    media_type media_type NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_media_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_media_comment FOREIGN KEY (comment_id) REFERENCES comment(id) ON DELETE CASCADE,
    CONSTRAINT fk_media_message FOREIGN KEY (message_id) REFERENCES message(id) ON DELETE CASCADE,
    CONSTRAINT fk_media_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CHECK ((post_id IS NOT NULL AND comment_id IS NULL AND message_id IS NULL) OR
           (post_id IS NULL AND comment_id IS NOT NULL AND message_id IS NULL) OR
           (post_id IS NULL AND comment_id IS NULL AND message_id IS NOT NULL) OR
           (post_id IS NULL AND comment_id IS NULL AND message_id IS NULL))
);

CREATE TABLE "like" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    post_id UUID,
    comment_id UUID,
    reaction_type reaction_type DEFAULT 'LIKE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_like_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_like_comment FOREIGN KEY (comment_id) REFERENCES comment(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_post_like UNIQUE(user_id, post_id),
    CONSTRAINT unique_user_comment_like UNIQUE(user_id, comment_id),
    CHECK ((post_id IS NOT NULL AND comment_id IS NULL) OR (post_id IS NULL AND comment_id IS NOT NULL))
);

-- Graph / FOAF
CREATE TABLE connection (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id UUID NOT NULL,
    receiver_id UUID NOT NULL,
    status connection_status DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_connection_requester FOREIGN KEY (requester_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_connection_receiver FOREIGN KEY (receiver_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT unique_connection UNIQUE(requester_id, receiver_id),
    CONSTRAINT prevent_self_connection CHECK (requester_id != receiver_id)
);

CREATE TABLE follow (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id UUID NOT NULL,
    following_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_follow_following FOREIGN KEY (following_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT unique_follow UNIQUE(follower_id, following_id),
    CONSTRAINT prevent_self_follow CHECK (follower_id != following_id)
);

-- Messaging
CREATE TABLE conversation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    participant_1_id UUID NOT NULL,
    participant_2_id UUID NOT NULL,
    last_message_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversation_participant1 FOREIGN KEY (participant_1_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_participant2 FOREIGN KEY (participant_2_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT unique_conversation UNIQUE(participant_1_id, participant_2_id),
    CONSTRAINT prevent_self_conversation CHECK (participant_1_id < participant_2_id)
);

CREATE TABLE message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    conversation_id UUID NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_recipient FOREIGN KEY (recipient_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE
);

ALTER TABLE conversation ADD CONSTRAINT fk_conversation_last_message
    FOREIGN KEY (last_message_id) REFERENCES message(id) ON DELETE SET NULL;

-- Notifications
CREATE TABLE notification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID NOT NULL,
    actor_id UUID NOT NULL,
    notification_type notification_type NOT NULL,
    related_post_id UUID,
    related_comment_id UUID,
    content VARCHAR(255),
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_recipient FOREIGN KEY (recipient_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_actor FOREIGN KEY (actor_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_post FOREIGN KEY (related_post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_comment FOREIGN KEY (related_comment_id) REFERENCES comment(id) ON DELETE CASCADE
);

-- Privacy
CREATE TABLE privacy_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL,
    can_see_email BOOLEAN DEFAULT false,
    can_see_phone BOOLEAN DEFAULT false,
    can_see_birthdate BOOLEAN DEFAULT false,
    can_see_location BOOLEAN DEFAULT false,
    allow_direct_messages BOOLEAN DEFAULT true,
    allow_friend_requests BOOLEAN DEFAULT true,
    allow_comments_on_posts BOOLEAN DEFAULT true,
    block_list JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_privacy_settings_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

-- Collaboration
CREATE TABLE project (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    creator_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    visibility visibility_type DEFAULT 'PRIVATE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_creator FOREIGN KEY (creator_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE TABLE project_member (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role project_member_role DEFAULT 'MEMBER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_member_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_member_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT unique_project_member UNIQUE(project_id, user_id)
);

CREATE TABLE event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    creator_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    event_date TIMESTAMP NOT NULL,
    location VARCHAR(255),
    visibility_type visibility_type DEFAULT 'PUBLIC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_creator FOREIGN KEY (creator_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE TABLE event_attendee (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status event_attendance_status DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_attendee_event FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_attendee_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT unique_event_attendee UNIQUE(event_id, user_id)
);

-- Recommendations
CREATE TABLE recommendation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    recommended_user_id UUID NOT NULL,
    score FLOAT CHECK (score >= 0 AND score <= 100),
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recommendation_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_recommendation_recommended_user FOREIGN KEY (recommended_user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT prevent_self_recommendation CHECK (user_id != recommended_user_id)
);

-- Reports
CREATE TYPE report_type AS ENUM ('SPAM','HARASSMENT','INAPPROPRIATE_CONTENT','HATE_SPEECH','FAKE_ACCOUNT','VIOLENCE','MISINFORMATION','OTHER');
CREATE TYPE report_status AS ENUM ('PENDING','UNDER_REVIEW','RESOLVED','DISMISSED');

CREATE TABLE report (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL,
    reported_user_id UUID,
    reported_post_id UUID,
    reported_comment_id UUID,
    report_type report_type NOT NULL,
    description TEXT,
    status report_status DEFAULT 'PENDING',
    reviewed_by UUID,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_report_reported_user FOREIGN KEY (reported_user_id) REFERENCES "user"(id) ON DELETE SET NULL,
    CONSTRAINT fk_report_reported_post FOREIGN KEY (reported_post_id) REFERENCES post(id) ON DELETE SET NULL,
    CONSTRAINT fk_report_reported_comment FOREIGN KEY (reported_comment_id) REFERENCES comment(id) ON DELETE SET NULL,
    CONSTRAINT fk_report_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES "user"(id) ON DELETE SET NULL
);
