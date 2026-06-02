/**
 * Own profile (/profil) and edit profile (/editProfil)
 */
module.exports = {
  url() {
    return `${this.api.launchUrl}/profil`;
  },
  elements: {
    editProfileLink: {
      selector: 'a[href="/editProfil"]',
    },
    addProjectCard: {
      selector: '#add-project-card',
    },
    editProfileTitle: {
      selector: 'main h1',
    },
  },
  commands: [
    {
      openEditProfile() {
        return this.api
          .url(`${this.api.launchUrl}/editProfil`)
          .waitForElementVisible('main h1');
      },
    },
  ],
};
