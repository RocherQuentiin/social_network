/**
 * Feed — create a post (form submit, no external APIs)
 */
describe('Feed post creation', function () {
  const feedPage = browser.page.feedPage();
  const { posts } = browser.globals.testData;

  before(function () {
    browser.loginAs('student');
    feedPage.navigate();
    feedPage.waitForElementVisible('@publishArea');
  });

  it('publishes a text post and reloads the feed', function () {
    feedPage.publishTextPost(posts.sampleContent);

    // After redirect/reload, the publish area should still be available
    feedPage.waitForElementVisible('@publishArea', 15000);
    browser.assert.urlContains('/feed');
  });

  it('shows the posts container on the feed', function () {
    feedPage.waitForElementVisible('@postsList');
    feedPage.expect.element('@postsList').to.be.present;
  });
});
