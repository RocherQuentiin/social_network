// Project Management JavaScript

document.addEventListener('DOMContentLoaded', () => {
    initProjectPage();
});

function initProjectPage() {
    setupEventListeners();
    loadUserProjects();
}

function setupEventListeners() {
    // Create Project Modal
    document.getElementById('btn-create-project').addEventListener('click', () => {
        openModal('createProjectModal');
    });
    document.getElementById('closeCreateModal').addEventListener('click', () => {
        closeModal('createProjectModal');
    });

    // Edit Project Modal
    document.getElementById('closeEditModal').addEventListener('click', () => {
        closeModal('editProjectModal');
    });

    // Members Modal
    document.getElementById('closeMembersModal').addEventListener('click', () => {
        closeModal('projectMembersModal');
    });

    // Form Submissions
    document.getElementById('createProjectForm').addEventListener('submit', handleCreateProject);
    document.getElementById('editProjectForm').addEventListener('submit', handleEditProject);
    document.getElementById('addMemberForm').addEventListener('submit', handleAddMember);

    // Close modals when clicking outside
    window.addEventListener('click', (event) => {
        const modals = document.querySelectorAll('.modal');
        modals.forEach(modal => {
            if (event.target == modal) {
                modal.classList.remove('active');
            }
        });
    });
}

function openModal(modalId) {
    document.getElementById(modalId).classList.add('active');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

async function loadUserProjects() {
    try {
        const response = await fetch('/api/project/user/' + getCurrentUserId(), {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 204) {
                // No projects
                displayNoProjects();
                return;
            }
            throw new Error('Failed to load projects');
        }

        const projects = await response.json();
        displayProjects(projects);
    } catch (error) {
        console.error('Error loading projects:', error);
        showAlert('Erreur lors du chargement des projets', 'error');
    }
}

function displayProjects(projects) {
    const projectsList = document.getElementById('user-projects');
    projectsList.innerHTML = '';

    if (!projects || projects.length === 0) {
        displayNoProjects();
        return;
    }

    // Load projects with their roles
    projects.forEach(async (project) => {
        const userRole = await getUserRoleInProject(project.id);
        const projectCard = createProjectCard(project, userRole);
        projectsList.appendChild(projectCard);
    });
}

function displayNoProjects() {
    const projectsList = document.getElementById('user-projects');
    projectsList.innerHTML = `
        <div class="empty-state">
            <p>Vous n'avez pas encore de projets.</p>
            <p>Cliquez sur "Créer un projet" pour commencer.</p>
        </div>
    `;
}

function createProjectCard(project, userRole = 'MEMBER') {
    const card = document.createElement('div');
    card.className = 'project-card';

    let actionsHTML = '';
    
    // Edit button (Owner only)
    if (userRole === 'OWNER') {
        actionsHTML += `<button class="btn-small btn-edit" onclick="editProject('${project.id}')">Éditer</button>`;
    }

    // Members button (Owner and Admin)
    if (userRole === 'OWNER' || userRole === 'ADMIN') {
        actionsHTML += `<button class="btn-small btn-members" onclick="showProjectMembers('${project.id}')">Membres</button>`;
    }

    // Leave button (Not owner)
    if (userRole !== 'OWNER') {
        actionsHTML += `<button class="btn-small btn-leave" onclick="leaveProject('${project.id}')">Quitter</button>`;
    }

    // Delete button (Owner only)
    if (userRole === 'OWNER') {
        actionsHTML += `<button class="btn-small btn-delete" onclick="deleteProject('${project.id}')">Supprimer</button>`;
    }

    card.innerHTML = `
        <h3>${escapeHtml(project.name)}</h3>
        <p class="description">${escapeHtml(project.description || 'Pas de description')}</p>
        <span class="visibility">${getVisibilityLabel(project.visibilityType)}</span>
        <div class="metadata">
            <p>Créé par: ${escapeHtml(project.creator.firstName + ' ' + project.creator.lastName)}</p>
            <p>Créé le: ${formatDate(project.createdAt)}</p>
        </div>
        <div class="actions">
            ${actionsHTML}
        </div>
    `;

    return card;
}

async function handleCreateProject(e) {
    e.preventDefault();

    const formData = {
        name: document.getElementById('projectName').value.trim(),
        description: document.getElementById('projectDescription').value.trim(),
        visibility: document.getElementById('projectVisibility').value
    };

    if (!formData.name) {
        showAlert('Le nom du projet est requis', 'error');
        return;
    }

    try {
        const response = await fetch('/api/project/', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        if (!response.ok) {
            const errorData = await response.text();
            throw new Error(errorData);
        }

        showAlert('Projet créé avec succès', 'success');
        closeModal('createProjectModal');
        document.getElementById('createProjectForm').reset();
        loadUserProjects();
    } catch (error) {
        console.error('Error creating project:', error);
        showAlert('Erreur lors de la création du projet: ' + error.message, 'error');
    }
}

async function editProject(projectId) {
    try {
        const response = await fetch(`/api/project/${projectId}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to load project');
        }

        const project = await response.json();

        document.getElementById('editProjectId').value = project.id;
        document.getElementById('editProjectName').value = project.name;
        document.getElementById('editProjectDescription').value = project.description || '';
        document.getElementById('editProjectVisibility').value = project.visibilityType;

        openModal('editProjectModal');
    } catch (error) {
        console.error('Error loading project for edit:', error);
        showAlert('Erreur lors du chargement du projet', 'error');
    }
}

async function handleEditProject(e) {
    e.preventDefault();

    const projectId = document.getElementById('editProjectId').value;
    const formData = {
        name: document.getElementById('editProjectName').value.trim(),
        description: document.getElementById('editProjectDescription').value.trim(),
        visibility: document.getElementById('editProjectVisibility').value
    };

    if (!formData.name) {
        showAlert('Le nom du projet est requis', 'error');
        return;
    }

    try {
        const response = await fetch(`/api/project/${projectId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        if (!response.ok) {
            const errorData = await response.text();
            throw new Error(errorData);
        }

        showAlert('Projet modifié avec succès', 'success');
        closeModal('editProjectModal');
        loadUserProjects();
    } catch (error) {
        console.error('Error updating project:', error);
        showAlert('Erreur lors de la modification du projet: ' + error.message, 'error');
    }
}

async function deleteProject(projectId) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer ce projet? Cette action est irréversible.')) {
        return;
    }

    try {
        const response = await fetch(`/api/project/${projectId}`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            const errorData = await response.text();
            throw new Error(errorData);
        }

        showAlert('Projet supprimé avec succès', 'success');
        loadUserProjects();
    } catch (error) {
        console.error('Error deleting project:', error);
        showAlert('Erreur lors de la suppression du projet: ' + error.message, 'error');
    }
}

async function leaveProject(projectId) {
    if (!confirm('Êtes-vous sûr de vouloir quitter ce projet?')) {
        return;
    }

    try {
        const response = await fetch(`/api/project/${projectId}/leave`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            const errorData = await response.text();
            throw new Error(errorData);
        }

        showAlert('Vous avez quitté le projet', 'success');
        loadUserProjects();
    } catch (error) {
        console.error('Error leaving project:', error);
        showAlert('Erreur lors du départ du projet: ' + error.message, 'error');
    }
}

async function showProjectMembers(projectId) {
    document.getElementById('addMemberProjectId').value = projectId;

    try {
        const response = await fetch(`/api/project/${projectId}/members`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 204) {
                document.getElementById('membersList').innerHTML = '<p>Aucun membre</p>';
            } else {
                throw new Error('Failed to load members');
            }
        } else {
            const members = await response.json();
            displayMembers(members, projectId);
        }

        openModal('projectMembersModal');
    } catch (error) {
        console.error('Error loading project members:', error);
        showAlert('Erreur lors du chargement des membres', 'error');
    }
}

function displayMembers(members, projectId) {
    const membersList = document.getElementById('membersList');
    membersList.innerHTML = '';

    if (!members || members.length === 0) {
        membersList.innerHTML = '<p>Aucun membre</p>';
        return;
    }

    const userRole = getUserRoleInProject(projectId);

    members.forEach(member => {
        const memberItem = document.createElement('div');
        memberItem.className = 'member-item';

        let removeButton = '';
        if (userRole === 'OWNER' || (userRole === 'ADMIN' && member.role !== 'ADMIN' && member.role !== 'OWNER')) {
            removeButton = `<button class="btn-remove-member" onclick="removeMember('${projectId}', '${member.user.id}')">Supprimer</button>`;
        }

        memberItem.innerHTML = `
            <div class="member-info">
                <div class="member-name">${escapeHtml(member.user.firstName + ' ' + member.user.lastName)}</div>
                <div class="member-role">${getMemberRoleLabel(member.role)}</div>
            </div>
            <div class="member-actions">
                ${removeButton}
            </div>
        `;

        membersList.appendChild(memberItem);
    });
}

async function handleAddMember(e) {
    e.preventDefault();

    const projectId = document.getElementById('addMemberProjectId').value;
    const userId = document.getElementById('memberUserId').value;
    const role = document.getElementById('memberRole').value;

    if (!userId) {
        showAlert('Veuillez sélectionner un utilisateur', 'error');
        return;
    }

    const formData = {
        userId: userId,
        role: role
    };

    try {
        const response = await fetch(`/api/project/${projectId}/member`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        if (!response.ok) {
            const errorData = await response.text();
            if (response.status === 409) {
                throw new Error('Cet utilisateur est déjà membre du projet');
            }
            throw new Error(errorData);
        }

        showAlert('Membre ajouté avec succès', 'success');
        document.getElementById('addMemberForm').reset();
        showProjectMembers(projectId);
    } catch (error) {
        console.error('Error adding member:', error);
        showAlert('Erreur lors de l\'ajout du membre: ' + error.message, 'error');
    }
}

async function removeMember(projectId, memberId) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer ce membre du projet?')) {
        return;
    }

    try {
        const response = await fetch(`/api/project/${projectId}/member/${memberId}`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            const errorData = await response.text();
            throw new Error(errorData);
        }

        showAlert('Membre supprimé avec succès', 'success');
        showProjectMembers(projectId);
    } catch (error) {
        console.error('Error removing member:', error);
        showAlert('Erreur lors de la suppression du membre: ' + error.message, 'error');
    }
}

function showAlert(message, type = 'info') {
    // Create a simple alert - you can replace this with a toast notification
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} active`;
    alertDiv.textContent = message;
    
    document.body.insertBefore(alertDiv, document.body.firstChild);
    
    setTimeout(() => {
        alertDiv.remove();
    }, 3000);
}

function getCurrentUserId() {
    // Try to get from data attribute
    const userIdElement = document.querySelector('[data-user-id]');
    if (userIdElement) {
        return userIdElement.getAttribute('data-user-id');
    }

    // Try to get from meta tag
    const metaUserId = document.querySelector('meta[data-user-id]');
    if (metaUserId) {
        return metaUserId.getAttribute('content');
    }

    // Try to get from model attribute in HTML
    const currentUserIdElement = document.querySelector('[data-current-user-id]');
    if (currentUserIdElement) {
        return currentUserIdElement.getAttribute('data-current-user-id');
    }

    // Fallback - this might need to be adjusted
    return localStorage.getItem('userId') || '';
}

async function getUserRoleInProject(projectId) {
    try {
        const response = await fetch(`/api/project/${projectId}/user-role`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            return 'MEMBER';
        }

        const role = await response.text();
        return role;
    } catch (error) {
        console.error('Error getting user role:', error);
        return 'MEMBER';
    }
}

function getVisibilityLabel(visibility) {
    const labels = {
        'PUBLIC': 'Public',
        'FRIENDS': 'Amis',
        'PRIVATE': 'Privé'
    };
    return labels[visibility] || visibility;
}

function getMemberRoleLabel(role) {
    const labels = {
        'OWNER': 'Propriétaire',
        'ADMIN': 'Administrateur',
        'MEMBER': 'Membre'
    };
    return labels[role] || role;
}

function formatDate(dateString) {
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return new Date(dateString).toLocaleDateString('fr-FR', options);
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
