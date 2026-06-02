const LOGOUT_LINK = '.navbar-actions a[href="/logout"]';
const LOGIN_NAV = '[data-testid="nav-login"]';

/**
 * Custom command: end session via header logout link.
 */
exports.command = function logout() {
  return this.waitForElementVisible(LOGOUT_LINK)
    .click(LOGOUT_LINK)
    .waitForElementVisible(LOGIN_NAV, 10000);
};
