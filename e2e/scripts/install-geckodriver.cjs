/**
 * Downloads geckodriver into e2e/.geckodriver.
 * Run before npm run test:firefox if GeckoDriver is missing.
 */
const path = require('path');
const geckodriver = require('geckodriver');

const cacheDir =
  process.env.GECKODRIVER_CACHE_DIR || path.join(__dirname, '..', '.geckodriver');

geckodriver
  .download(process.env.GECKODRIVER_VERSION, cacheDir)
  .then((driverPath) => {
    console.log(`GeckoDriver ready: ${driverPath}`);
  })
  .catch((err) => {
    console.error('GeckoDriver install failed:', err.message);
    console.error(
      'Install Mozilla Firefox, then retry: npm run install:gecko-driver'
    );
    process.exit(1);
  });
