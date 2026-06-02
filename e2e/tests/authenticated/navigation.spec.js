/**
 * Authenticated header navigation across main sections
 */
describe('Authenticated navigation', function () {
  const nav = browser.page.headerNav();

  before(function () {
    browser.loginAs('student');
  });

  it('opens the member directory', function () {
    nav.click('@navDirectory');
    browser.assert.urlContains('/users');
    browser.page.directoryPage().waitForElementVisible('@searchInput');
  });

  it('opens messaging', function () {
    nav.click('@navMessages');
    browser.assert.urlContains('/messages');
    browser.page.messagesPage().waitForElementVisible('@title');
  });

  it('opens projects', function () {
    nav.click('@navProjects');
    browser.assert.urlContains('/projects');
    browser.page.projectsPage().waitForElementVisible('@createBtn');
  });

  it('opens own profile', function () {
    nav.click('@navProfile');
    browser.assert.urlContains('/profil');
    browser.page.profilePage().waitForElementPresent('body');
  });

  it('opens pending friend requests', function () {
    nav.click('@navRequests');
    browser.assert.urlContains('/profil/pending-requests');
  });

  it('returns to the feed', function () {
    nav.click('@navFeed');
    browser.assert.urlContains('/feed');
    browser.page.feedPage().waitForElementVisible('@publishArea');
  });
});
