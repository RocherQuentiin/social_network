const testData = require('../fixtures/testData');

const LOGGED_IN_NAV = '.navbar-actions a[href="/logout"]';
const LOGIN_FORM = '[data-testid="login-form"]';

/**
 * Custom command: log in via the login form using seeded E2E accounts.
 * @param {'student'|'peer'|'admin'} role
 */
exports.command = function loginAs(role = 'student') {
  const user = testData.users[role];
  if (!user) {
    throw new Error(`Unknown E2E role: ${role}`);
  }

  const loginPage = this.page.loginPage();

  // End any prior session so /login renders the form (not accueil redirect)
  this.url(`${testData.baseUrl}/logout`);

  loginPage
    .navigate()
    .waitForElementVisible(LOGIN_FORM, 15000)
    .clearValue('@email')
    .clearValue('@password')
    .setValue('@email', user.email)
    .setValue('@password', testData.password)
    .click('@submit');

  const browser = this;

  if (role === 'admin') {
    return browser
      .waitForElementVisible(LOGGED_IN_NAV, 25000)
      .assert.urlContains('/admin/dashboard');
  }

  return browser
    .waitForElementVisible(LOGGED_IN_NAV, 25000)
    .assert.urlContains('/feed');
};
