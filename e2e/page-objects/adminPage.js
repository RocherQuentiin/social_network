/**
 * Admin dashboard (/admin/dashboard)
 */
module.exports = {
  url() {
    return `${this.api.launchUrl}/admin/dashboard`;
  },
  elements: {
    title: {
      selector: '.main-admin-section h2',
    },
    tabKpis: {
      selector: '#tabKpis',
    },
    tabUsers: {
      selector: '#tabUsers',
    },
    totalUsers: {
      selector: '#totalUsers',
    },
  },
};
