# Nightwatch E2E Tests — MobiliTous Connect

End-to-end browser tests for the Spring Boot + Thymeleaf application, using [Nightwatch.js](https://nightwatchjs.org/) with stable `data-testid` selectors and static seeded accounts (no external SMTP or MariaDB).

## Prerequisites

- **Java 17** and **Maven**
- **Node.js 18+** and npm
- Browsers: **Chrome**, **Firefox**, **Microsoft Edge** (Safari only on macOS)
- Matching WebDriver binaries are pulled via npm (`chromedriver`, `geckodriver`, `edgedriver`)

## Quick start

### 1. Start the application (E2E profile)

Uses in-memory H2 and seeds three accounts (see `fixtures/testData.js`):

```powershell
cd d:\Project\social_network\socialnetwork
$env:SPRING_PROFILES_ACTIVE = "e2e"
# Quote -D on PowerShell (otherwise ".run.profiles=e2e" is parsed as a separate argument)
mvn spring-boot:run "-Dspring-boot.run.profiles=e2e"
```

Or:

```powershell
.\e2e\scripts\start-e2e-server.ps1
```

### 2. Install dependencies and run tests

```powershell
cd d:\Project\social_network\e2e
npm ci
npm run test:headless
```

## Seeded test accounts

| Role    | Email                      | Password      |
|---------|----------------------------|---------------|
| Student | `e2e.student@eleve.isep.fr` | `E2eTest!123` |
| Peer    | `e2e.peer@eleve.isep.fr`    | `E2eTest!123` |
| Admin   | `e2e.admin@isep.fr`         | `E2eTest!123` |

After changing `E2eDataSeeder`, **restart** Spring Boot once. On each start, E2E users get their password **reset** to `E2eTest!123` (fixes old double-encoded accounts without wiping H2 manually).

## Browser coverage

| npm script              | Nightwatch env      |
|-------------------------|---------------------|
| `npm test`              | `default` (Chrome)  |
| `npm run test:chrome`   | `chrome`            |
| `npm run test:firefox`  | `firefox`           |
| `npm run test:edge`     | `edge`              |
| `npm run test:safari`   | `safari` (macOS)    |
| `npm run test:headless` | `chromeHeadless`    |
| `npm run test:ci`       | Chrome/Firefox/Edge headless |

Run all desktop browsers sequentially:

```powershell
npm run test:all-browsers
```

## Test layout

| Folder | Scope |
|--------|--------|
| `tests/public/` | Landing, login page (guest) |
| `tests/auth/` | Login, logout, registration validation |
| `tests/authenticated/` | Feed, directory, projects, privacy, profile, messages, navigation |
| `tests/admin/` | Admin dashboard |

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `E2E_BASE_URL` | `http://localhost:8081` | Application base URL (8081 avoids conflicts with other apps on 8080) |
| `E2E_WAIT_APP_MS` | `90000` | Startup wait in `globals.js` (also drives `asyncHookTimeout`) |
| `E2E_SERVER_PORT` | `8081` | Spring server port (`application-e2e.yml`) |

## Troubleshooting

### `ChromeDriver only supports Chrome version 134` (or similar)

Your installed **Chrome** major version must match the **chromedriver** npm package. After upgrading Chrome, reinstall drivers:

```powershell
cd e2e
npm install chromedriver@148
npm run test:headless
```

Use the same major number as Chrome (`chrome://version` → e.g. 148 → `chromedriver@148`).

### Edge: `Unable to obtain browser driver` / `msedgedriver.azureedge.net`

Nightwatch does **not** use Selenium Manager for Edge anymore. Install **Microsoft Edge**, then download a matching driver into `e2e/.edgedriver`:

```powershell
cd e2e
npm run install:edge-driver
npm run test:edge
```

Requires network once (driver is cached locally). Chrome-only runs (`test:headless`) do not need this step.

### Firefox: `Failed to connect to GeckoDriver` / `status 64`

Install **Mozilla Firefox**, download the driver, then run tests:

```powershell
cd e2e
npm run install:gecko-driver
npm run test:firefox
```

If you see **Server terminated early with status 64**, Nightwatch was passing `--port` twice to GeckoDriver (fixed in `nightwatch.conf.js` — do not add `--port` in `cli_args` for Firefox).

### `this.execute is not a function` in `globals.js`

In global hooks, `this` is **not** the browser. Use the `browser` argument:

```js
async beforeEach(browser) {
  await browser.deleteCookies();
}
```

### `done is not a function` in `globals.js`

Use `async before()` without a `done` callback (Nightwatch 3).

### Where is the summary?

Nightwatch prints `TEST FAILURE` / passed counts at the end of the terminal output, and writes an HTML report to `e2e/tests_output/nightwatch-html-report/index.html`.

### Wrong app on port 8080 (`login-form` not found)

If another service (e.g. **gunicorn**) uses port **8080**, Nightwatch will load the wrong HTML. E2E uses **8081** by default — start Spring with profile `e2e` and open `http://localhost:8081/login` (must show « Bon retour ! »).

### `global before` timeout (20000ms)

Nightwatch kills the startup hook if Spring is not up in time. Start the app **before** tests; default wait is 90s on port **8081**. If you see this error, open `http://localhost:8081/login` in a browser first.

### `Application not reachable`

Start Spring Boot first (terminal 1), wait until `http://localhost:8081/login` shows the MobiliTous login page, then run Nightwatch (terminal 2).

## Practices used

- **Clear test names** — describe behaviour in plain language
- **Stable selectors** — `data-testid` on critical UI; IDs for form fields
- **Static fixtures** — `fixtures/testData.js` + `E2eDataSeeder` (no third-party APIs in tests)
- **Isolated sessions** — cookies cleared in `beforeEach`
- **Page objects** — `page-objects/` + custom commands `loginAs`, `logout`

## CI

See `.github/workflows/e2e-nightwatch.yml` at the repository root.
