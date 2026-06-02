/**
 * Downloads msedgedriver into e2e/.edgedriver (matches local Edge major version).
 * Run before npm run test:edge if EdgeDriver is missing.
 */
const path = require('path');
const edgedriver = require('edgedriver');

const cacheDir =
  process.env.EDGEDRIVER_CACHE_DIR || path.join(__dirname, '..', '.edgedriver');

edgedriver
  .download(process.env.EDGEDRIVER_VERSION, cacheDir)
  .then((driverPath) => {
    console.log(`EdgeDriver ready: ${driverPath}`);
  })
  .catch((err) => {
    console.error('EdgeDriver install failed:', err.message);
    console.error(
      'Install Microsoft Edge, then retry: npm run install:edge-driver'
    );
    process.exit(1);
  });
