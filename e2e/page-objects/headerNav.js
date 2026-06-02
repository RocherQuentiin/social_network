/**
 * Shared header navigation (fragment on every page) — href-based selectors
 */
module.exports = {
  elements: {
    navFeed: {
      selector: '.navbar-links a[href="/feed"]',
    },
    navDirectory: {
      selector: '.navbar-links a[href="/users"]',
    },
    navMessages: {
      selector: '.navbar-links a[href="/messages"]',
    },
    navProjects: {
      selector: '.navbar-links a[href="/projects"]',
    },
    navProfile: {
      selector: '.navbar-links a[href="/profil"]',
    },
    navRequests: {
      selector: '.navbar-links a[href="/profil/pending-requests"]',
    },
    navAdmin: {
      selector: '.navbar-links a[href="/admin/dashboard"]',
    },
    navLogin: {
      selector: '.navbar-actions a[href="/login"]',
    },
    navLogout: {
      selector: '.navbar-actions a[href="/logout"]',
    },
    navHome: {
      selector: 'a.navbar-logo',
    },
  },
};
