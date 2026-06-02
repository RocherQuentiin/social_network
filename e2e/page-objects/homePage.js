/**
 * Public landing page (/)
 */
module.exports = {
  url() {
    return `${this.api.launchUrl}/`;
  },
  elements: {
    heroTitle: {
      selector: 'h1',
    },
    ctaRegister: {
      selector: '[data-testid="home-cta-register"]',
    },
    navLogin: {
      selector: '[data-testid="nav-login"]',
    },
  },
};
