/**
 * Login page (/login)
 */
module.exports = {
  url() {
    return `${this.api.launchUrl}/login`;
  },
  elements: {
    form: {
      selector: '[data-testid="login-form"]',
    },
    email: {
      selector: '#email',
    },
    password: {
      selector: '#passwordHash',
    },
    submit: {
      selector: 'form.login-form button[type="submit"]',
    },
    error: {
      selector: '.alert-error',
    },
    registerLink: {
      selector: 'a[href*="register"]',
    },
  },
};
