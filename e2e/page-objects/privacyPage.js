/**
 * Privacy settings (/privacy)
 */
module.exports = {
  url() {
    return `${this.api.launchUrl}/privacy`;
  },
  elements: {
    form: {
      selector: 'form.privacy-form',
    },
    saveBtn: {
      selector: '[data-testid="privacy-save-btn"]',
    },
    allowFriendRequests: {
      selector: '#allowFriendRequests',
    },
  },
  commands: [
    {
      /**
       * Scroll the save button into view, then submit (avoids footer/header overlap).
       */
      savePreferences() {
        return this.waitForElementVisible('@saveBtn').execute(function () {
          const btn = document.querySelector('[data-testid="privacy-save-btn"]');
          if (!btn) {
            throw new Error('privacy-save-btn not found');
          }
          btn.scrollIntoView({ block: 'center', inline: 'nearest' });
          btn.click();
        });
      },
    },
  ],
};
