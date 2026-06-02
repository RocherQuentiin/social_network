const testData = require('./fixtures/testData');

const WAIT_APP_MS = parseInt(process.env.E2E_WAIT_APP_MS || '90000', 10);

/** Marker text from templates/login.html — confirms Spring Boot app, not another service on the port */
const LOGIN_PAGE_MARKER = 'Bon retour';

/**
 * Quick port scan to include in error messages when the app is not ready.
 */
async function diagnoseLoginEndpoints() {
  const ports = [8081, 8080];
  const lines = [];

  for (const port of ports) {
    try {
      const res = await fetch(`http://localhost:${port}/login`, {
        method: 'GET',
        redirect: 'follow',
        signal: AbortSignal.timeout(3000),
      });
      const body = await res.text();
      const isMobilitous = body.includes(LOGIN_PAGE_MARKER) && body.includes('id="email"');
      const server = res.headers.get('server') || '(no Server header)';
      lines.push(
        `  http://localhost:${port}/login → HTTP ${res.status}, Server: ${server}, MobiliTous page: ${isMobilitous ? 'YES' : 'no'}`
      );
    } catch (err) {
      lines.push(`  http://localhost:${port}/login → not reachable (${err.cause?.code || err.message})`);
    }
  }

  return lines.join('\n');
}

/**
 * Poll until the MobiliTous Spring app serves the real login page (profile e2e).
 */
async function waitForApplication(baseUrl) {
  const deadline = Date.now() + WAIT_APP_MS;

  while (Date.now() < deadline) {
    try {
      const res = await fetch(`${baseUrl}/login`, {
        method: 'GET',
        redirect: 'follow',
        signal: AbortSignal.timeout(5000),
      });
      const body = await res.text();
      if (res.ok && body.includes(LOGIN_PAGE_MARKER) && body.includes('id="email"')) {
        return;
      }
    } catch (err) {
      // Server not ready yet
    }
    await new Promise((resolve) => setTimeout(resolve, 1000));
  }

  const diagnosis = await diagnoseLoginEndpoints();

  throw new Error(
    `MobiliTous login page not found at ${baseUrl}/login within ${WAIT_APP_MS}ms.\n\n` +
      `Port check:\n${diagnosis}\n\n` +
      'Fix:\n' +
      '  1. Terminal 1 — start Spring (profile e2e, port 8081):\n' +
      '       cd socialnetwork\n' +
      '       $env:SPRING_PROFILES_ACTIVE = "e2e"\n' +
      '       mvn spring-boot:run\n' +
      '  2. Browser — open http://localhost:8081/login (must show « Bon retour ! »)\n' +
      '  3. Terminal 2 — npm run test:headless\n' +
      '  Note: port 8080 may be used by another app (gunicorn); E2E targets 8081.'
  );
}

module.exports = {
  // Must be >= wait time in global before() (Nightwatch default is 20000ms)
  asyncHookTimeout: WAIT_APP_MS + 10000,

  launch_url: testData.baseUrl,
  testData,

  async beforeEach(browser) {
    await browser.deleteCookies();
    await browser.execute(function () {
      try {
        localStorage.clear();
        sessionStorage.clear();
      } catch (e) {
        /* ignore */
      }
    });
  },

  async before() {
    await waitForApplication(testData.baseUrl);
  },
};
