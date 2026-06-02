/**
 * Member directory — search and listing
 */
describe('Member directory', function () {
  const directoryPage = browser.page.directoryPage();
  const { users } = browser.globals.testData;

  before(function () {
    browser.loginAs('student');
    directoryPage.navigate();
  });

  it('lists at least one member card', function () {
    directoryPage.waitForElementVisible('@userCards');
    directoryPage.expect.element('@userCards').to.be.present;
  });

  it('shows the search field for member lookup', function () {
    directoryPage
      .clearValue('@searchInput')
      .setValue('@searchInput', users.peer.firstName);

    directoryPage.expect.element('@searchInput').value.to.equal(users.peer.firstName);
    directoryPage.expect.element('@searchSubmit').to.be.visible;
  });

  it('lists the seeded peer on the directory page', function () {
    browser.expect.element('body').text.to.contain(users.peer.firstName);
  });
});
