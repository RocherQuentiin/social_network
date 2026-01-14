# API & Endpoints Reference — ISEP Social Network

This document lists the main HTTP endpoints provided by the application, grouped by area. It provides the method, path, short description and notes about authentication and expected response type.

Notes:
- The app uses session-based authentication. Endpoints that require authentication are marked `Auth: required`.
- Many UI routes return HTML pages (Thymeleaf). API routes typically return JSON.

---

## Authentication & User pages

- GET `/login` — Render login page (HTML). Auth: no
- POST `/login` — Authenticate user (form). Auth: no — sets session on success
- GET `/logout` — Logout (invalidate session). Auth: required
- GET `/register` — Render registration page (HTML). Auth: no
- POST `/register` — Create new user account (form). Auth: no
- GET `/user/{code}/confirm` — Confirm account by code (email). Auth: no

## User profile & directory

- GET `/profil` — Current user's profile page (HTML). Auth: required
- GET `/profil/{id}` — Public profile page for user `id` (HTML). Auth: optional
- GET `/users` — User directory page (HTML). Auth: required
- GET `/api/check-username?username=...` — Check username availability (JSON). Auth: no

## Feed & Posts

- GET `/feed` — Main feed page (HTML). Auth: required
- POST `/post` — Create new post (form / multipart). Auth: required
- GET `/post/{id}` — Get post details (HTML/JSON depending on controller). Auth: required
- PUT `/post/{id}` — Update post `id`. Auth: required

## Reactions & Comments

- POST `/reactions` — Add reaction (JSON). Auth: required
- DELETE `/reactions` — Remove reaction. Auth: required
- GET `/reactions/summary?postId=...` — Get reactions summary for a post. Auth: required

- POST `/comments` — Add comment. Auth: required
- PUT `/comments/{id}` — Update comment `id`. Auth: required
- DELETE `/comments/{id}` — Delete comment `id`. Auth: required
- GET `/comments?postId=...` — List comments for a post. Auth: required

## Friend requests & suggestions

- GET `/friend-request/pending` — View pending friend requests (HTML/API). Auth: required
- GET `/friend-request/sent` — View sent friend requests. Auth: required
- GET `/friend-request/accepted-ids` — Get accepted friend IDs (JSON). Auth: required
- POST `/friend-request/send` — Send a friend request (JSON/form). Auth: required
- POST `/friend-request/accept` — Accept a request. Auth: required
- POST `/friend-request/decline` — Decline a request. Auth: required

- GET `/suggestion` — Get friend suggestions (HTML/JSON). Auth: required

## Suggestions / Recommendations

- POST `/recommandation` — Submit a recommendation of a user. Auth: required
- (Controller) GET endpoints render recommendations on profile pages.

## Messages & Conversations

- GET `/messages` — Message UI (HTML). Auth: required
- API base: `/api/messages`
  - GET `/api/messages/conversation/{otherUserId}` — Get conversation with `otherUserId` (JSON). Auth: required
  - GET `/api/messages/{conversationId}` — Get messages in conversation (JSON). Auth: required
  - POST `/api/messages/send` — Send a message (JSON). Auth: required
  - POST `/api/messages/{messageId}/read` — Mark a message as read. Auth: required
  - GET `/api/messages/conversations` — List conversations. Auth: required
  - GET `/api/messages/unread-count` — Unread messages count. Auth: required

- Project messaging (group/project scoped): base `/api/project-messages`
  - POST `/api/project-messages/groups` — Create message group for project. Auth: required
  - GET `/api/project-messages/groups/{projectId}` — Get message groups for a project. Auth: required
  - POST `/api/project-messages/send/{messageGroupId}` — Send to group. Auth: required
  - GET `/api/project-messages/{messageGroupId}` — Get messages of group. Auth: required
  - POST `/api/project-messages/{messageId}/read` — Mark message read. Auth: required
  - GET `/api/project-messages/{messageGroupId}/unread-count` — Unread count for group. Auth: required

## Notifications

- GET `/api/notifications` — List notifications (JSON). Auth: required
- GET `/api/notifications/unread` — List unread notifications. Auth: required
- GET `/api/notifications/unread-count` — Count unread notifications. Auth: required
- POST `/api/notifications/{notificationId}/read` — Mark single notification read. Auth: required
- POST `/api/notifications/read-all` — Mark all as read. Auth: required

## Projects & project pages

Base: `/projects` (page) and `/api/project` (API)
- GET `/projects` — Projects listing (HTML). Auth: required
- GET `/projects/user/{userId}` — Projects for a user (HTML). Auth: required

API `/api/project`:
- POST `/api/project/` — Create project. Auth: required
- PUT `/api/project/{id}` — Update project. Auth: required
- GET `/api/project/{id}` — Get project details. Auth: required
- GET `/api/project/public` — Public projects. Auth: optional
- GET `/api/project/my-projects` — Projects owned by current user. Auth: required
- DELETE `/api/project/{id}` — Delete project. Auth: required
- POST `/api/project/{id}/member` — Add member to project. Auth: required
- GET `/api/project/{id}/members` — List project members. Auth: required
- DELETE `/api/project/{id}/member/{memberId}` — Remove member. Auth: required
- DELETE `/api/project/{id}/leave` — Leave project (current user). Auth: required
- GET `/api/project/{id}/user-role` — Get current user role in project. Auth: required
- POST `/api/project/{id}/member/{memberId}/delete` — Admin delete member (legacy). Auth: required
- PUT `/api/project/{id}/member/{memberId}/role` — Change member role. Auth: required
- GET `/api/project/{projectId}/skills` — Get project skills. Auth: required
- POST `/api/project/{projectId}/skills` — Add skill to project. Auth: required
- DELETE `/api/project/{projectId}/skills` — Remove skill. Auth: required
- GET `/api/project/search/skill/{skillName}` — Search projects by skill. Auth: required
- POST `/api/project/{projectId}/request` — Request to join project. Auth: required
- GET `/api/project/{projectId}/requests` — Get requests for project. Auth: required
- GET `/api/project/{projectId}/requests/pending` — Get pending requests. Auth: required
- GET `/api/project/requests/user` — Projects the user requested. Auth: required
- PUT `/api/project/request/{requestId}/accept` — Accept join request. Auth: required
- PUT `/api/project/request/{requestId}/reject` — Reject request. Auth: required
- GET `/api/project/{projectId}/has-requested` — Check if current user has requested. Auth: required
- PUT `/api/project/{projectId}/transfer-ownership` — Transfer ownership. Auth: required

## Admin endpoints

Base routes: `/admin` (page) and API under `/admin/api`
- GET `/admin/dashboard` — Admin dashboard page (HTML). Auth: Admin
- GET `/admin/api/stats` — Returns basic stats (users, posts counts) (JSON). Auth: Admin
- GET `/admin/api/stats/messages` — Messaging stats (JSON). Auth: Admin
- GET `/admin/api/stats/timeseries?days=` — Time-series stats (JSON). Auth: Admin
- GET `/admin/api/users?query=&page=&size=` — Paginated users listing (JSON). Auth: Admin
- POST `/admin/api/user/{id}/block` — Block user (set active=false). Auth: Admin
- POST `/admin/api/user/{id}/unblock` — Unblock user. Auth: Admin
- POST `/admin/api/user/{id}/suspend` — Suspend user (body: {days}). Auth: Admin
- DELETE `/admin/api/post/{id}` — Delete a post. Auth: Admin

## Events

Base: `/event` and `/eventattendee`
- POST `/event` — Create event. Auth: required
- GET `/event/{id}` — Get event details. Auth: required
- PUT `/event/{id}` — Update event. Auth: required

Event attendees `/eventattendee`:
- POST `/eventattendee` — Register as attendee. Auth: required
- GET `/eventattendee/pending` — Get pending attendee requests (HTML/API). Auth: required
- GET `/eventattendee/sent` — Get sent requests. Auth: required
- DELETE `/eventattendee/{id}` — Cancel attendance/request. Auth: required
- PUT `/eventattendee/accept` — Accept attendee. Auth: required
- PUT `/eventattendee/decline` — Decline attendee. Auth: required

## Follow / Privacy

- POST `/follow` — Follow a user. Auth: required
- POST `/unfollow` — Unfollow a user. Auth: required
- GET `/privacy` — Get privacy settings (HTML). Auth: required
- POST `/privacy` — Update privacy settings. Auth: required

## Notifications & Helpers

- GET `/api/notifications` and related endpoints documented above
- GET `/api/users` (search API) — see `UserSearchController` — Auth: required

## Misc

- Password reset flows:
  - GET `/forgotpassword/email` (render), POST `/forgotpassword/email` (submit)
  - GET `/user/{code}/forgotpassword`, POST `/forgotpassword/changepassword`
- GET/POST `/changePassword` — change password UI and handler

---

If you want, I can:
- add request/response JSON schema examples for the main API endpoints,
- generate a Postman collection or OpenAPI spec from the controllers,
- or annotate which endpoints return HTML vs JSON explicitly for each controller.

Which of these would you prefer next?