# ISEP Social Network

ISEP Social Network is an internal social networking application built for members of ISEP (students, staff and collaborators) to share posts, messages, projects and collaborate within a closed environment.

This repository contains a Spring Boot (Java 17) application with a server-side rendered UI (Thymeleaf), lightweight client-side JS for interactions, and a relational schema (SQL files) for the database.

## Key Features

- User accounts, profiles and searchable directory
- Posts, comments and a feed (timeline)
- Private messaging
- Project pages and user public projects
- Friend requests, suggestions and recommendations
- Admin dashboard: moderation tools (block, suspend, delete), statistics and time-series charts

## Tech stack

- Java 17
- Spring Boot (MVC, Data JPA)
- Thymeleaf templates for server-rendered views
- PostgreSQL / MySQL (SQL scripts provided)
- Maven for build & dependency management
- Chart.js and light client-side JS for charts and UX

## Prerequisites

- Java 17 JDK installed and `JAVA_HOME` set
- Maven (`mvn`) installed OR Docker (to use the Maven container image)
- A running SQL database (Postgres/MySQL) and credentials

## Quick start (development)

1. Create the database and apply the schema found in `db/database_schema.sql` (or use the drawSQL dump). Adjust DB connection in `src/main/resources/application.yml` or `application-dev.yml`.

2. Build and run with Maven:

```powershell
cd socialnetwork
mvn spring-boot:run
```

3. Or run tests only:

```powershell
mvn -Dtest=SuggestionUserServiceTest test
```

4. If you don't have Maven installed, use Docker to run Maven in a container (from the repository root):

```powershell
docker run --rm -v ${PWD}:/workspace -w /workspace/socialnetwork maven:3.9.6-jdk-17 mvn -DskipTests spring-boot:run
```

5. Open the application in your browser at `http://localhost:8080`.

## Build a runnable JAR

```powershell
mvn clean package -DskipTests
java -jar target/socialnetwork-*.jar
```

## Configuration

- `src/main/resources/application.yml` (and `application-dev.yml` / `application-prod.yml`) contain profile-specific properties (database, mail, etc.).
- Database schema files are in the `db/` directory.

## Admin

- Admin role (`ADMIN`) is available in `UserRole`. Admins can access `/admin/dashboard` for moderation and statistics. Admin users must be created or flagged in DB.

## Tests

Unit tests are under `src/test/java`. Use your IDE's test runner or Maven as shown above.

## Notes and troubleshooting

- If `mvn` is missing: either install Maven or run via Docker as shown.
- If the UI loads but features fail, check logs (console) for stack traces and ensure database schema is applied.

---

If you want, I can also add an INSTALL.md with step-by-step environment setup (DB, env vars), or update this README with contributor-specific commands.

## Production

This application is running in production at: https://socialnetworkisep.hangar.garageisep.com/

Please note that the production environment is intended for the ISEP community only and contains real user data; coordinate with the admins before making changes that could affect production.

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