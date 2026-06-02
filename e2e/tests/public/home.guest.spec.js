/**
 * Public landing page — unauthenticated visitors
 */
describe('Public home page (guest)', function () {
  const homePage = browser.page.homePage();

  before(function () {
    homePage.navigate();
  });

  it('displays the main hero headline', function () {
    homePage.waitForElementVisible('@heroTitle');
    homePage.expect.element('@heroTitle').text.to.contain('sport');
  });

  it('shows the registration call-to-action', function () {
    homePage.waitForElementVisible('@ctaRegister');
    homePage.expect.element('@ctaRegister').to.be.visible;
  });

  it('shows the login entry point in the header', function () {
    homePage.waitForElementVisible('@navLogin');
    homePage.expect.element('@navLogin').text.to.contain('Connexion');
  });

  it('navigates to registration when CTA is clicked', function () {
    homePage.click('@ctaRegister');
    browser.assert.urlContains('/register');
    browser.page.registerPage().waitForElementVisible('@form');
  });
});
