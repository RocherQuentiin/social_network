/**
 * Forgot password — guest can open the email request form
 */
describe('Forgot password page (guest)', function () {
  before(function () {
    browser.url(`${browser.globals.testData.baseUrl}/forgotpassword/email`);
  });

  it('displays the email input for password recovery', function () {
    browser.waitForElementVisible('#email');
    browser.expect.element('#email').to.be.visible;
  });
});
