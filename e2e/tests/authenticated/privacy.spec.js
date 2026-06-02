/**
 * Privacy settings — form access and save
 */
describe('Privacy settings', function () {
  const privacyPage = browser.page.privacyPage();

  before(function () {
    browser.loginAs('student');
    privacyPage.navigate();
  });

  it('loads the privacy form', function () {
    privacyPage.waitForElementVisible('@form');
    privacyPage.expect.element('@allowFriendRequests').to.be.present;
  });

  it('saves privacy preferences and stays on the page', function () {
    privacyPage.savePreferences();
    privacyPage.waitForElementVisible('@form', 15000);
    browser.assert.urlContains('/privacy');
  });
});
