/**
 * Session termination via logout
 */
describe('User logout', function () {
  before(function () {
    browser.loginAs('student');
  });

  it('ends the session and shows the login button again', function () {
    browser.logout();
    browser.page.homePage().waitForElementVisible('@navLogin');
    browser.assert.not.urlContains('/feed');
  });
});
