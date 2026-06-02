/**
 * Admin dashboard — KPIs tab (admin role only)
 */
describe('Admin dashboard', function () {
  const adminPage = browser.page.adminPage();

  before(function () {
    browser.loginAs('admin');
    adminPage.navigate();
    // Admin JS may leave modals in the DOM; hide overlays so tab buttons are clickable
    browser.execute(function () {
      document.querySelectorAll('.modal').forEach(function (modal) {
        modal.style.display = 'none';
      });
    });
  });

  it('shows the administration heading', function () {
    adminPage.waitForElementVisible('@title');
    adminPage.expect.element('@title').text.to.contain('Administration');
  });

  it('displays KPI widgets after load', function () {
    adminPage.waitForElementVisible('@totalUsers', 20000);
    adminPage.expect.element('@tabKpis').to.be.visible;
    adminPage.expect.element('@tabUsers').to.be.visible;
  });

  it('switches to the users tab', function () {
    browser.execute(function () {
      document.querySelectorAll('.modal').forEach(function (modal) {
        modal.style.display = 'none';
      });
    });
    adminPage.click('@tabUsers');
    browser.waitForElementVisible('#usersTab', 10000);
    browser.expect.element('#usersTab').to.be.visible;
  });
});
