/**
 * Projects — page load and create-project modal
 */
describe('Projects management', function () {
  const projectsPage = browser.page.projectsPage();
  const { projects } = browser.globals.testData;

  before(function () {
    browser.loginAs('student');
    projectsPage.navigate();
  });

  it('displays the projects page title', function () {
    projectsPage.waitForElementVisible('@pageTitle');
    projectsPage.expect.element('@pageTitle').text.to.contain('Projet');
  });

  it('opens the create project modal and fills the form', function () {
    projectsPage.click('@createBtn');
    projectsPage.waitForElementVisible('@createModal');
    projectsPage
      .setValue('@projectName', projects.name)
      .setValue('@projectDescription', projects.description);

    projectsPage.expect.element('@projectName').value.to.equal(projects.name);
    // Close modal without submitting to avoid cross-test DB noise
    projectsPage.click('#closeCreateModal');
  });
});
