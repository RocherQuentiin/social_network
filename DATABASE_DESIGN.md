# TSN (Tailored Social Network) - Database Design

## Entity-Relationship Diagram (ERD)

### Core Entities

#### 1. **User**
- `id` (PK, UUID)
- `username` (UNIQUE, VARCHAR)
- `email` (UNIQUE, VARCHAR)
- `password_hash` (VARCHAR)
- `first_name` (VARCHAR)
- `last_name` (VARCHAR)
- `bio` (TEXT)
- `profile_picture_url` (VARCHAR)
- `cover_picture_url` (VARCHAR)
- `is_verified` (BOOLEAN)
- `is_active` (BOOLEAN)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

#### 2. **Profile**
- `id` (PK, UUID)
- `user_id` (FK -> User)
- `location` (VARCHAR)
- `website` (VARCHAR)
- `phone_number` (VARCHAR)
- `birthdate` (DATE)
- `gender` (ENUM: MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY)
- `profession` (VARCHAR)
- `company` (VARCHAR)
- `education` (VARCHAR)
- `interests` (TEXT - JSON format)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

#### 3. **Post**
- `id` (PK, UUID)
- `author_id` (FK -> User)
- `content` (TEXT)
- `visibility` (ENUM: PUBLIC, FRIENDS, PRIVATE)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)
- `deleted_at` (TIMESTAMP, nullable)

#### 4. **Media**
- `id` (PK, UUID)
- `post_id` (FK -> Post, nullable)
- `comment_id` (FK -> Comment, nullable)
- `message_id` (FK -> Message, nullable)
- `user_id` (FK -> User) [For profile pictures]
- `file_url` (VARCHAR)
- `media_type` (ENUM: IMAGE, VIDEO, AUDIO, DOCUMENT)
- `file_size` (BIGINT)
- `mime_type` (VARCHAR)
- `created_at` (TIMESTAMP)

#### 5. **Comment**
- `id` (PK, UUID)
- `post_id` (FK -> Post)
- `author_id` (FK -> User)
- `content` (TEXT)
- `parent_comment_id` (FK -> Comment, nullable) [For nested comments]
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)
- `deleted_at` (TIMESTAMP, nullable)

#### 6. **Like** (Reaction)
- `id` (PK, UUID)
- `user_id` (FK -> User)
- `post_id` (FK -> Post, nullable)
- `comment_id` (FK -> Comment, nullable)
- `reaction_type` (ENUM: LIKE, LOVE, HAHA, WOW, SAD, ANGRY)
- `created_at` (TIMESTAMP)
- UNIQUE(user_id, post_id, comment_id)

#### 7. **Connection** (Graph-based relationships)
- `id` (PK, UUID)
- `requester_id` (FK -> User)
- `receiver_id` (FK -> User)
- `status` (ENUM: PENDING, ACCEPTED, BLOCKED)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)
- UNIQUE(requester_id, receiver_id)

#### 8. **Message**
- `id` (PK, UUID)
- `sender_id` (FK -> User)
- `recipient_id` (FK -> User)
- `conversation_id` (FK -> Conversation)
- `content` (TEXT)
- `is_read` (BOOLEAN)
- `read_at` (TIMESTAMP, nullable)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

#### 9. **Conversation**
- `id` (PK, UUID)
- `participant_1_id` (FK -> User)
- `participant_2_id` (FK -> User)
- `last_message_id` (FK -> Message, nullable)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)
- UNIQUE(participant_1_id, participant_2_id)

#### 10. **Notification**
- `id` (PK, UUID)
- `recipient_id` (FK -> User)
- `actor_id` (FK -> User) [Who triggered the notification]
- `notification_type` (ENUM: FRIEND_REQUEST, POST_LIKE, COMMENT, MESSAGE, MENTION, FOLLOW)
- `related_post_id` (FK -> Post, nullable)
- `related_comment_id` (FK -> Comment, nullable)
- `content` (VARCHAR)
- `is_read` (BOOLEAN)
- `read_at` (TIMESTAMP, nullable)
- `created_at` (TIMESTAMP)

#### 11. **Follow**
- `id` (PK, UUID)
- `follower_id` (FK -> User)
- `following_id` (FK -> User)
- `created_at` (TIMESTAMP)
- UNIQUE(follower_id, following_id)

#### 12. **PrivacySettings**
- `id` (PK, UUID)
- `user_id` (FK -> User)
- `can_see_email` (BOOLEAN, default: false)
- `can_see_phone` (BOOLEAN, default: false)
- `can_see_birthdate` (BOOLEAN, default: false)
- `can_see_location` (BOOLEAN, default: false)
- `allow_direct_messages` (BOOLEAN, default: true)
- `allow_friend_requests` (BOOLEAN, default: true)
- `allow_comments_on_posts` (BOOLEAN, default: true)
- `block_list` (JSON - array of user IDs)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

#### 13. **Project** (Collaboration)
- `id` (PK, UUID)
- `creator_id` (FK -> User)
- `name` (VARCHAR)
- `description` (TEXT)
- `visibility` (ENUM: PUBLIC, PRIVATE, MEMBERS_ONLY)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

#### 14. **ProjectMember**
- `id` (PK, UUID)
- `project_id` (FK -> Project)
- `user_id` (FK -> User)
- `role` (ENUM: OWNER, ADMIN, MEMBER)
- `joined_at` (TIMESTAMP)
- UNIQUE(project_id, user_id)

#### 15. **Event** (Collaboration)
- `id` (PK, UUID)
- `creator_id` (FK -> User)
- `name` (VARCHAR)
- `description` (TEXT)
- `event_date` (TIMESTAMP)
- `location` (VARCHAR)
- `visibility` (ENUM: PUBLIC, PRIVATE, MEMBERS_ONLY)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

#### 16. **EventAttendee**
- `id` (PK, UUID)
- `event_id` (FK -> Event)
- `user_id` (FK -> User)
- `status` (ENUM: PENDING, ACCEPTED, DECLINED, MAYBE)
- `created_at` (TIMESTAMP)
- UNIQUE(event_id, user_id)

#### 17. **Recommendation**
- `id` (PK, UUID)
- `user_id` (FK -> User) [For whom the recommendation is]
- `recommended_user_id` (FK -> User) [Recommended user]
- `score` (FLOAT) [Recommendation strength: 0-100]
- `reason` (VARCHAR) [Why recommended]
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

---

## Key Relationships

- **User 1:1 Profile** — Each user has one detailed profile
- **User 1:N Post** — Users create multiple posts
- **User 1:N Comment** — Users write multiple comments
- **User 1:N Like** — Users create multiple reactions
- **Post 1:N Comment** — Posts receive multiple comments
- **Post 1:N Like** — Posts receive multiple likes
- **Comment 1:N Like** — Comments receive likes
- **Comment 1:N Comment** — Comments can have nested replies
- **User N:N Connection** — Graph-based friend network
- **User N:N Follow** — Follow relationships
- **User 1:N Message** — Users send/receive messages
- **User 1:N Notification** — Users receive notifications
- **User 1:1 PrivacySettings** — Each user has privacy settings
- **User 1:N Project** — Users create projects
- **Project N:N User** (via ProjectMember)
- **User 1:N Event** — Users create events
- **Event N:N User** (via EventAttendee)
- **User N:N Recommendation** — ML recommendations

---

## Database Indexes

```sql
CREATE INDEX idx_user_username ON user(username);
CREATE INDEX idx_user_email ON user(email);
CREATE INDEX idx_post_author_id ON post(author_id);
CREATE INDEX idx_post_visibility ON post(visibility);
CREATE INDEX idx_comment_post_id ON comment(post_id);
CREATE INDEX idx_comment_author_id ON comment(author_id);
CREATE INDEX idx_like_user_id ON like(user_id);
CREATE INDEX idx_like_post_id ON like(post_id);
CREATE INDEX idx_connection_requester_id ON connection(requester_id);
CREATE INDEX idx_connection_receiver_id ON connection(receiver_id);
CREATE INDEX idx_message_sender_id ON message(sender_id);
CREATE INDEX idx_message_recipient_id ON message(recipient_id);
CREATE INDEX idx_notification_recipient_id ON notification(recipient_id);
CREATE INDEX idx_notification_is_read ON notification(is_read);
CREATE INDEX idx_follow_follower_id ON follow(follower_id);
CREATE INDEX idx_follow_following_id ON follow(following_id);
```

---

## Notes

1. **Graph Structure**: The `Connection` table implements the FOAF ontology for graph-based networking
2. **Privacy**: `PrivacySettings` allows fine-grained control over data visibility
3. **Soft Deletes**: Posts and Comments use `deleted_at` for soft deletion
4. **Timestamps**: All entities track creation and modification times
5. **Enums**: Use database enums for fixed values (visibility, status, etc.)
6. **Scalability**: Indexes on frequently queried columns for optimal performance
7. **Media Flexibility**: Media can be associated with Posts, Comments, or Messages
8. **Real-time**: Notification and Message tables support real-time features
