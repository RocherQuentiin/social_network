/**
 * User profile — view and edit profile pages
 */
describe('User profile', function () {
  const profilePage = browser.page.profilePage();

  before(function () {
    browser.loginAs('student');
    profilePage.navigate();
  });

  it('loads the profile page for the logged-in user', function () {
    browser.assert.urlContains('/profil');
    profilePage.waitForElementPresent('body');
  });

  it('opens the edit profile screen', function () {
    profilePage.openEditProfile();
    browser.expect.element('main h1').text.to.contain('Modifier');
  });
});
