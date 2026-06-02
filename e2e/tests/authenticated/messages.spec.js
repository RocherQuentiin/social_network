/**
 * Messaging UI — layout and controls (no WebSocket third-party mocks)
 */
describe('Messaging page', function () {
  const messagesPage = browser.page.messagesPage();

  before(function () {
    browser.loginAs('student');
    messagesPage.navigate();
  });

  it('displays the messages heading', function () {
    messagesPage.waitForElementVisible('@title');
    messagesPage.expect.element('@title').text.to.contain('Messages');
  });

  it('shows conversation sidebar controls', function () {
    messagesPage.waitForElementVisible('@newConversationBtn');
    messagesPage.waitForElementVisible('@privateTab');
    messagesPage.expect.element('@conversationsList').to.be.present;
  });
});
