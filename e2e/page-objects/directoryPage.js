/**
 * Member directory (/users)
 */
module.exports = {
  url() {
    return `${this.api.launchUrl}/users`;
  },
  elements: {
    searchInput: {
      selector: '[data-testid="directory-search-input"]',
    },
    searchSubmit: {
      selector: '[data-testid="directory-search-submit"]',
    },
    userCards: {
      selector: '.user-card',
    },
  },
};
