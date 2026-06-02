/**
 * Messaging UI (/messages)
 */
module.exports = {
  url() {
    return `${this.api.launchUrl}/messages`;
  },
  elements: {
    title: {
      selector: '.sidebar-header h2',
    },
    newConversationBtn: {
      selector: '#newMessageBtn',
    },
    conversationsList: {
      selector: '#conversationsList',
    },
    privateTab: {
      selector: '#tab-private',
    },
  },
};
