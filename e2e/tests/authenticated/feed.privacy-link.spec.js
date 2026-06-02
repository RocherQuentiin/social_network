/**
 * Feed sidebar — navigate to privacy settings
 */
describe('Feed privacy shortcut', function () {
  const feedPage = browser.page.feedPage();

  before(function () {
    browser.loginAs('student');
    feedPage.navigate();
  });

  it('opens privacy settings from the feed sidebar', function () {
    feedPage.click('@privacyLink');
    browser.assert.urlContains('/privacy');
    browser.page.privacyPage().waitForElementVisible('@form');
  });
});
