/**
 * Projects management (/projects)
 */
module.exports = {
  url() {
    return `${this.api.launchUrl}/projects`;
  },
  elements: {
    pageTitle: {
      selector: '.projects-container h1',
    },
    createBtn: {
      selector: '#btn-create-project',
    },
    createModal: {
      selector: '#createProjectModal',
    },
    projectName: {
      selector: '#projectName',
    },
    projectDescription: {
      selector: '#projectDescription',
    },
    createForm: {
      selector: '#createProjectForm',
    },
    userProjectsList: {
      selector: '#user-projects',
    },
  },
};
