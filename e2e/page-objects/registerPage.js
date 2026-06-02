/**
 * Registration page (/register)
 */
module.exports = {
  url() {
    return `${this.api.launchUrl}/register`;
  },
  elements: {
    form: {
      selector: 'form.login-form',
    },
    username: {
      selector: '#username',
    },
    email: {
      selector: '#email',
    },
    firstName: {
      selector: '#firstName',
    },
    lastName: {
      selector: '#lastName',
    },
    password: {
      selector: '#passwordHash',
    },
    bio: {
      selector: '#bio',
    },
    submit: {
      selector: 'form.login-form button[type="submit"]',
    },
    error: {
      selector: '.alert-error',
    },
  },
};
