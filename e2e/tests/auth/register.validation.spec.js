/**
 * Registration validation — server-side rules without external email dependency
 */
describe('Registration validation', function () {
  const registerPage = browser.page.registerPage();
  const { register } = browser.globals.testData;

  beforeEach(function () {
    registerPage.navigate();
  });

  it('rejects a non-ISEP email domain', function () {
    registerPage
      .waitForElementVisible('@form')
      .setValue('@username', register.invalidEmail.username)
      .setValue('@email', register.invalidEmail.email)
      .setValue('@firstName', register.invalidEmail.firstName)
      .setValue('@lastName', register.invalidEmail.lastName)
      .setValue('@password', register.invalidEmail.password)
      .click('@submit');

    registerPage.waitForElementVisible('@error');
    registerPage.expect.element('@error').text.to.match(/ISEP|isep/i);
    browser.assert.urlContains('/register');
  });

  it('rejects a password that does not meet complexity rules', function () {
    registerPage
      .setValue('@username', register.weakPassword.username)
      .setValue('@email', register.weakPassword.email)
      .setValue('@firstName', register.weakPassword.firstName)
      .setValue('@lastName', register.weakPassword.lastName)
      .setValue('@password', register.weakPassword.password)
      .click('@submit');

    registerPage.waitForElementVisible('@error');
    registerPage.expect.element('@error').text.to.match(/mot de passe|password/i);
  });
});
