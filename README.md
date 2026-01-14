# social_network

## Admin dashboard

Quick admin UI and API endpoints:

- Dashboard page: `/admin/dashboard` (requires a logged-in user with role `ADMIN` — set `role` to `ADMIN` in the `user` table or create an admin user via your migration).
- API endpoints (session-based auth):
	- `GET /admin/api/stats` — returns JSON with `totalUsers`, `totalPosts`, `activeUsers`.
	- `POST /admin/api/user/{id}/block` — block user (set `is_active=false`).
	- `POST /admin/api/user/{id}/unblock` — unblock user.
	- `POST /admin/api/user/{id}/suspend` — suspend user; JSON body `{ "days": 7 }` sets `suspended_until`.
	- `DELETE /admin/api/post/{id}` — soft-delete post (sets `deleted_at`).

Frontend assets: `src/main/resources/templates/admin/dashboard.html` and `src/main/resources/static/js/admin-dashboard.js`.

Note: The project uses session attributes `userId` to identify the logged user. Ensure the admin account has `role = 'ADMIN'` in the database.