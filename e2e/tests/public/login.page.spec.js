/**
 * Login page UI — guest access
 */
describe('Login page (guest)', function () {
  const loginPage = browser.page.loginPage();

  before(function () {
    loginPage.navigate();
  });

  it('renders the login form with email and password fields', function () {
    loginPage.waitForElementVisible('@form');
    loginPage.expect.element('@email').to.be.visible;
    loginPage.expect.element('@password').to.be.visible;
    loginPage.expect.element('@submit').to.be.visible;
  });

  it('links to the registration page', function () {
    loginPage.click('@registerLink');
    browser.assert.urlContains('/register');
  });
});
