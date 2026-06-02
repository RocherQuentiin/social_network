/**
 * Protected routes — guests are redirected away from authenticated areas
 */
describe('Protected routes (guest)', function () {
  const { baseUrl } = browser.globals.testData;

  beforeEach(function () {
    browser.deleteCookies();
  });

  it('redirects unauthenticated users away from the feed', function () {
    browser.url(`${baseUrl}/feed`);
    browser.waitForElementPresent('body');
    browser.expect.element('#publishArea').not.to.be.present;
  });

  it('redirects unauthenticated users away from profile', function () {
    browser.url(`${baseUrl}/profil`);
    browser.waitForElementPresent('body');
    browser.expect.element('.navbar-links a[href="/profil"]').not.to.be.present;
  });
});
