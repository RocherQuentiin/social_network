// Nightwatch configuration — multi-browser E2E for MobiliTous Connect
// @see https://nightwatchjs.org/guide/configuration/

const fs = require('fs');
const path = require('path');
const chromeDriver = require('chromedriver');

const baseUrl = process.env.E2E_BASE_URL || 'http://localhost:8081';
const edgeDriverCache =
  process.env.EDGEDRIVER_CACHE_DIR || path.join(__dirname, '.edgedriver');
const edgeDriverBinary =
  process.platform === 'win32' ? 'msedgedriver.exe' : 'msedgedriver';
const edgeDriverPath = path.join(edgeDriverCache, edgeDriverBinary);
const geckoDriverCache =
  process.env.GECKODRIVER_CACHE_DIR || path.join(__dirname, '.geckodriver');
const geckoDriverBinary =
  process.platform === 'win32' ? 'geckodriver.exe' : 'geckodriver';
const geckoDriverPath = path.join(geckoDriverCache, geckoDriverBinary);

function geckoWebdriverConfig(port) {
  if (!fs.existsSync(geckoDriverPath)) {
    throw new Error(
      `GeckoDriver not found at ${geckoDriverPath}. ` +
        'Install Firefox, then run: npm run install:gecko-driver'
    );
  }
  // Nightwatch already passes --port; duplicating it makes geckodriver exit 64.
  return {
    start_process: true,
    server_path: geckoDriverPath,
    port,
    host: '127.0.0.1',
  };
}

function edgeWebdriverConfig(port) {
  if (!fs.existsSync(edgeDriverPath)) {
    throw new Error(
      `EdgeDriver not found at ${edgeDriverPath}. ` +
        'Install Microsoft Edge, then run: npm run install:edge-driver'
    );
  }
  return {
    start_process: true,
    server_path: edgeDriverPath,
    port,
    cli_args: [`--port=${port}`],
  };
}

function chromeWebdriverConfig(port) {
  return {
    start_process: true,
    server_path: chromeDriver.path,
    port,
    cli_args: [`--port=${port}`],
  };
}

function genericWebdriverConfig(port) {
  return {
    start_process: true,
    port,
    cli_args: [`--port=${port}`],
  };
}

function chromeCapabilities(headless = false) {
  const caps = {
    browserName: 'chrome',
    'goog:chromeOptions': {
      args: headless
        ? ['--headless=new', '--window-size=1920,1080', '--disable-gpu', '--no-sandbox']
        : ['--window-size=1920,1080', '--disable-gpu'],
    },
  };
  return caps;
}

const firefoxBinary =
  process.env.FIREFOX_BINARY ||
  (process.platform === 'win32'
    ? 'C:\\Program Files\\Mozilla Firefox\\firefox.exe'
    : undefined);

function firefoxCapabilities(headless = false) {
  const firefoxOptions = {
    args: headless ? ['-headless'] : [],
  };
  if (firefoxBinary && fs.existsSync(firefoxBinary)) {
    firefoxOptions.binary = firefoxBinary;
  }
  return {
    browserName: 'firefox',
    'moz:firefoxOptions': firefoxOptions,
  };
}

function edgeCapabilities(headless = false) {
  return {
    browserName: 'MicrosoftEdge',
    'ms:edgeOptions': {
      args: headless ? ['--headless=new', '--window-size=1920,1080'] : ['--window-size=1920,1080'],
    },
  };
}

module.exports = {
  src_folders: ['tests'],
  page_objects_path: ['page-objects'],
  custom_commands_path: ['commands'],
  globals_path: 'globals.js',

  webdriver: {
    start_process: true,
    server_path: '',
  },

  test_workers: {
    enabled: false,
  },

  test_settings: {
    default: {
      disable_hashing: true,
      launch_url: baseUrl,
      wait_for_condition_poll_interval: 500,
      wait_for_condition_timeout: 15000,
      screenshots: {
        enabled: true,
        on_failure: true,
        path: 'screenshots',
      },
      desiredCapabilities: chromeCapabilities(false),
      webdriver: {
        timeout_options: {
          timeout: 120000,
          retry_attempts: 2,
        },
      },
    },

    chrome: {
      desiredCapabilities: chromeCapabilities(false),
      webdriver: chromeWebdriverConfig(9515),
    },

    chromeHeadless: {
      desiredCapabilities: chromeCapabilities(true),
      webdriver: chromeWebdriverConfig(9515),
    },

    firefox: {
      desiredCapabilities: firefoxCapabilities(false),
      webdriver: geckoWebdriverConfig(4444),
    },

    firefoxHeadless: {
      desiredCapabilities: firefoxCapabilities(true),
      webdriver: geckoWebdriverConfig(4444),
    },

    edge: {
      desiredCapabilities: edgeCapabilities(false),
      webdriver: edgeWebdriverConfig(9516),
    },

    edgeHeadless: {
      desiredCapabilities: edgeCapabilities(true),
      webdriver: edgeWebdriverConfig(9516),
    },

    safari: {
      desiredCapabilities: {
        browserName: 'safari',
        acceptInsecureCerts: false,
      },
      webdriver: {
        port: 4445,
        cli_args: [`--port=4445`],
      },
    },
  },
};
