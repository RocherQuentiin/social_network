/**
 * Authentication — login success and failure
 */
describe('User login', function () {
  const loginPage = browser.page.loginPage();
  const { invalidLogin } = browser.globals.testData;

  beforeEach(function () {
    loginPage.navigate();
  });

  it('rejects invalid credentials with an error message', function () {
    loginPage
      .waitForElementVisible('@form')
      .setValue('@email', invalidLogin.email)
      .setValue('@password', invalidLogin.password)
      .click('@submit');

    loginPage.waitForElementVisible('@error');
    loginPage.expect.element('@error').text.to.match(/incorrect|invalide/i);
    browser.assert.urlContains('/login');
  });

  it('logs in a verified student and redirects to the feed', function () {
    browser.loginAs('student');
    browser.assert.urlContains('/feed');
    browser.page.feedPage().waitForElementVisible('@publishArea');
  });

  it('logs in an admin and lands on the admin dashboard', function () {
    browser.loginAs('admin');
    browser.page.adminPage().waitForElementVisible('@title');
    browser.assert.urlContains('/admin/dashboard');
  });
});
