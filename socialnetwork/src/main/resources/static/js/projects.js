// Project Management JavaScript

document.addEventListener('DOMContentLoaded', () => {
    initProjectPage();
});

function initProjectPage() {
    setupEventListeners();
    loadUserProjects();
    loadPublicProjects();
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

    // Join Modal
    const closeJoin = document.getElementById('closeJoinModal');
    if (closeJoin) {
        closeJoin.addEventListener('click', () => closeModal('joinProjectModal'));
    }
    const joinForm = document.getElementById('joinProjectForm');
    if (joinForm) {
        joinForm.addEventListener('submit', handleJoinProject);
    }

    // Form Submissions
    document.getElementById('createProjectForm').addEventListener('submit', handleCreateProject);
    document.getElementById('editProjectForm').addEventListener('submit', handleEditProject);

    // Close modals when clicking outside
    window.addEventListener('click', (event) => {
        const modals = document.querySelectorAll('.modal');
        modals.forEach(modal => {
            if (event.target == modal) {
                modal.classList.remove('active');
            }
        });
    });

    // Requests Modal
    const closeReq = document.getElementById('closeRequestsModal');
    if (closeReq) {
        closeReq.addEventListener('click', () => closeModal('projectRequestsModal'));
    }
}

async function fetchUserRequestsMap() {
    try {
        const response = await fetch('/api/project/requests/user?t=' + Date.now(), {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) {
            return {};
        }
        const requests = await response.json();
        const map = {};
        if (Array.isArray(requests)) {
            requests.forEach(r => {
                if (r.project && r.project.id && r.status) {
                    map[r.project.id] = r.status;
                }
            });
        }
        return map;
    } catch (e) {
        console.error('Error fetching user requests:', e);
        return {};
    }
}

function openModal(modalId) {
    document.getElementById(modalId).classList.add('active');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

async function loadUserProjects() {
    try {
        const response = await fetch('/api/project/my-projects?t=' + Date.now(), {
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
        actionsHTML += `<button class="btn-small" id="requests-btn-${project.id}" onclick="showProjectRequests('${project.id}')">Demandes</button>`;
    }

    // Leave button (Not owner)
    if (userRole !== 'OWNER') {
        actionsHTML += `<button class="btn-small btn-leave" onclick="leaveProject('${project.id}')">Quitter</button>`;
    }

    // Delete button (Owner only)
    if (userRole === 'OWNER') {
        actionsHTML += `<button class="btn-small btn-delete" onclick="showTransferOwnershipModal('${project.id}')">Supprimer / Transférer</button>`;
    }

    card.innerHTML = `
        <h3>${escapeHtml(project.name)}</h3>
        <p class="description">${escapeHtml(project.description || 'Pas de description')}</p>
        <span class="visibility">${getVisibilityLabel(project.visibilityType)}</span>
        <div id="skills-${project.id}" class="project-skills"></div>
        <div class="metadata">
            <p>Créé par: ${project.creator ? escapeHtml(project.creator.firstName + ' ' + project.creator.lastName) : 'Inconnu'}</p>
            <p>Créé le: ${formatDate(project.createdAt)}</p>
        </div>
        <div class="actions">
            ${actionsHTML}
        </div>
    `;

    // Load skills asynchronously
    loadProjectSkills(project.id);
    // Load pending requests count asynchronously
    if (userRole === 'OWNER' || userRole === 'ADMIN') {
        updateProjectRequestsBadge(project.id);
    }

    return card;
}

// Load and render public projects (discover section)
async function loadPublicProjects() {
    const container = document.getElementById('public-projects');
    if (!container) return;

    container.innerHTML = '';
    try {
        const response = await fetch('/api/project/public?t=' + Date.now(), {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) {
            if (response.status === 204) {
                container.innerHTML = `<div class="empty-state"><p>Aucun projet public pour l'instant.</p></div>`;
                return;
            }
            throw new Error('Failed to load public projects');
        }

        const projects = await response.json();
        const currentUserId = getCurrentUserId();
        const userRequests = await fetchUserRequestsMap();

        // Filter out projects created by self
        const filtered = projects.filter(p => p?.creator?.id !== currentUserId);

        // Render each with role check to hide join if already member
        for (const project of filtered) {
            const role = await getUserRoleInProject(project.id);
            const reqStatus = role === 'NONE' ? userRequests[project.id] : null;
            const card = createPublicProjectCard(project, role, reqStatus);
            container.appendChild(card);
        }
    } catch (e) {
        console.error('Error loading public projects:', e);
        container.innerHTML = `<div class="empty-state"><p>Erreur de chargement des projets publics.</p></div>`;
    }
}

function createPublicProjectCard(project, userRole = 'NONE', requestStatus = null) {
    const card = document.createElement('div');
    card.className = 'project-card';

    let actionsHTML = '';
    if (userRole === 'NONE') {
        let disabledAttr = '';
        let label = 'Rejoindre';
        let extraAttrs = '';
        if (requestStatus === 'PENDING') {
            disabledAttr = 'disabled';
            label = 'Demande en attente';
        } else if (requestStatus === 'REJECTED') {
            disabledAttr = 'disabled';
            label = 'Demande refusée';
        }
        if (!disabledAttr) {
            extraAttrs = 'aria-disabled="false"';
        }
        const safeName = escapeHtml(project.name || '');
        const safeDesc = escapeHtml(project.description || '');
        actionsHTML += `
            <button type="button"
                class="btn-small btn-members"
                id="join-btn-${project.id}"
                ${disabledAttr} ${extraAttrs}
                data-project-id="${project.id}"
                data-project-name="${safeName}"
                data-project-desc="${safeDesc}"
                onclick="openJoinModalFromButton(this)">
                ${label}
            </button>
        `;
    } else if (userRole === 'OWNER' || userRole === 'ADMIN' || userRole === 'MEMBER') {
        actionsHTML += `<span class="visibility" style="border-color: transparent; background-color: rgba(29,155,240,0.08);">Déjà membre</span>`;
    }

    card.innerHTML = `
        <h3>${escapeHtml(project.name)}</h3>
        <p class="description">${escapeHtml(project.description || 'Pas de description')}</p>
        <span class="visibility">${getVisibilityLabel(project.visibilityType)}</span>
        <div class="metadata">
            <p>Créé par: ${escapeHtml(project.creator.firstName + ' ' + project.creator.lastName)}</p>
            <p>Créé le: ${formatDate(project.createdAt)}</p>
        </div>
        <div class="actions">${actionsHTML}</div>
    `;
    return card;
}

function openJoinModal(projectId, projectName, projectDescription) {
    document.getElementById('joinProjectId').value = projectId;
    document.getElementById('joinProjectName').textContent = projectName;
    document.getElementById('joinProjectDesc').textContent = projectDescription || '';
    openModal('joinProjectModal');
}

function openJoinModalFromButton(btn) {
    const projectId = btn.getAttribute('data-project-id');
    const projectName = btn.getAttribute('data-project-name') || '';
    const projectDesc = btn.getAttribute('data-project-desc') || '';
    openJoinModal(projectId, projectName, projectDesc);
}

async function handleJoinProject(e) {
    e.preventDefault();
    const projectId = document.getElementById('joinProjectId').value;
    const message = document.getElementById('joinMessage').value.trim();

    if (!projectId) {
        showAlert('Projet introuvable', 'error');
        return;
    }

    try {
        const response = await fetch(`/api/project/${projectId}/request`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ message: message || '', skillName: '' })
        });

        if (!response.ok) {
            const errorText = await response.text();
            if (response.status === 409) {
                showAlert('Vous êtes déjà membre ou avez déjà demandé à rejoindre ce projet', 'info');
                const joinBtn = document.getElementById(`join-btn-${projectId}`);
                if (joinBtn) {
                    joinBtn.textContent = 'Demande en attente';
                    joinBtn.disabled = true;
                }
            } else {
                showAlert(errorText || 'Erreur lors de l\'envoi de la demande', 'error');
            }
            return;
        }

        showAlert('Demande envoyée avec succès', 'success');
        closeModal('joinProjectModal');
        // Mark join button as requested
        const joinBtn = document.getElementById(`join-btn-${projectId}`);
        if (joinBtn) {
            joinBtn.textContent = 'Demande en attente';
            joinBtn.disabled = true;
        }
        loadUserProjects();
        loadPublicProjects();
    } catch (err) {
        console.error('Error join request:', err);
        showAlert('Erreur lors de l\'envoi de la demande', 'error');
    }
}

async function handleCreateProject(e) {
    e.preventDefault();

    const skillsInput = document.getElementById('projectSkills').value.trim();
    const skills = skillsInput ? skillsInput.split(',').map(s => s.trim()).filter(s => s.length > 0) : [];

    const formData = {
        name: document.getElementById('projectName').value.trim(),
        description: document.getElementById('projectDescription').value.trim(),
        visibility: document.getElementById('projectVisibility').value,
        skills: skills
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

        // Load members into edit modal
        await loadMembersForEdit(project.id);
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
    try {
        // First check if user is the owner
        const roleResponse = await fetch(`/api/project/${projectId}/user-role`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!roleResponse.ok) {
            throw new Error('Failed to get user role');
        }

        const userRole = await roleResponse.json();
        console.log('User role:', userRole);

        // If owner, show transfer modal
        if (userRole === 'OWNER') {
            await showTransferOwnershipModal(projectId);
            return;
        }

        // Otherwise, just leave
        if (!confirm('Êtes-vous sûr de vouloir quitter ce projet?')) {
            return;
        }

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
    const projectIdInput = document.getElementById('addMemberProjectId');
    if (projectIdInput) {
        projectIdInput.value = projectId;
    }

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

async function displayMembers(members, projectId) {
    const membersList = document.getElementById('membersList');
    membersList.innerHTML = '';

    if (!members || members.length === 0) {
        membersList.innerHTML = '<p>Aucun membre</p>';
        return;
    }

    const userRole = await getUserRoleInProject(projectId);

    members.forEach(member => {
        const memberItem = document.createElement('div');
        memberItem.className = 'member-item';

        let removeButton = '';
        let roleSelector = '';
        if (userRole === 'OWNER') {
            // Owner can change any role
            roleSelector = `
                <select class="role-selector" onchange="updateMemberRole('${projectId}', '${member.user.id}', this.value)" style="margin-left: 10px;">
                    <option value="MEMBER" ${member.role === 'MEMBER' ? 'selected' : ''}>Membre</option>
                    <option value="ADMIN" ${member.role === 'ADMIN' ? 'selected' : ''}>Admin</option>
                    <option value="OWNER" ${member.role === 'OWNER' ? 'selected' : ''}>Propriétaire</option>
                </select>
            `;
            removeButton = `<button class="btn-remove-member" onclick="removeMember('${projectId}', '${member.user.id}')">Supprimer</button>`;
        } else if (userRole === 'ADMIN' && member.role === 'MEMBER') {
            removeButton = `<button class="btn-remove-member" onclick="removeMember('${projectId}', '${member.user.id}')">Supprimer</button>`;
        }

        memberItem.innerHTML = `
            <div class="member-info">
                <div class="member-name">${escapeHtml(member.user.firstName + ' ' + member.user.lastName)}</div>
                <div class="member-role">${getMemberRoleLabel(member.role)}</div>
            </div>
            <div class="member-actions">
                ${roleSelector}
                ${removeButton}
            </div>
        `;

        membersList.appendChild(memberItem);
    });
}

async function loadMembersForEdit(projectId) {
    const container = document.getElementById('editMembersList');
    if (!container) return;

    container.innerHTML = '';
    try {
        const response = await fetch(`/api/project/${projectId}/members`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (!response.ok) {
            if (response.status === 204) {
                container.innerHTML = '<p>Aucun membre</p>';
                return;
            }
            throw new Error('Failed to load members');
        }

        const members = await response.json();
        const role = await getUserRoleInProject(projectId);

        members.forEach(member => {
            const item = document.createElement('div');
            item.className = 'member-item';

            let removeButton = '';
            let roleSelector = '';
            if (role === 'OWNER') {
                roleSelector = `
                    <select class="role-selector" onchange="updateMemberRole('${projectId}', '${member.user.id}', this.value)" style="margin-left: 10px;">
                        <option value="MEMBER" ${member.role === 'MEMBER' ? 'selected' : ''}>Membre</option>
                        <option value="ADMIN" ${member.role === 'ADMIN' ? 'selected' : ''}>Admin</option>
                        <option value="OWNER" ${member.role === 'OWNER' ? 'selected' : ''}>Propriétaire</option>
                    </select>
                `;
                removeButton = `<button class="btn-remove-member" onclick="removeMemberFromEdit('${projectId}', '${member.user.id}')">Supprimer</button>`;
            } else if (role === 'ADMIN' && member.role === 'MEMBER') {
                removeButton = `<button class="btn-remove-member" onclick="removeMemberFromEdit('${projectId}', '${member.user.id}')">Supprimer</button>`;
            }

            item.innerHTML = `
                <div class="member-info">
                    <div class="member-name">${escapeHtml(member.user.firstName + ' ' + member.user.lastName)}</div>
                    <div class="member-role">${getMemberRoleLabel(member.role)}</div>
                </div>
                <div class="member-actions">${roleSelector}${removeButton}</div>
            `;

            container.appendChild(item);
        });
    } catch (e) {
        console.error('Error loading members for edit:', e);
        container.innerHTML = '<p>Erreur lors du chargement des membres</p>';
    }
}

async function removeMemberFromEdit(projectId, memberId) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer ce membre du projet?')) {
        return;
    }

    try {
        const response = await fetch(`/api/project/${projectId}/member/${memberId}`, {
            method: 'DELETE',
            headers: { 'Accept': 'application/json' }
        });

        if (!response.ok) {
            const errorData = await response.text();
            throw new Error(errorData);
        }

        showAlert('Membre supprimé avec succès', 'success');
        await loadMembersForEdit(projectId);
    } catch (error) {
        console.error('Error removing member (edit):', error);
        showAlert('Erreur lors de la suppression du membre: ' + error.message, 'error');
    }
}

// Show pending join requests for a project
async function showProjectRequests(projectId) {
    try {
        window.currentRequestsProjectId = projectId;
        const response = await fetch(`/api/project/${projectId}/requests/pending`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        const container = document.getElementById('requestsList');
        if (!container) return;
        container.innerHTML = '';

        if (!response.ok) {
            if (response.status === 403) {
                container.innerHTML = '<p>Accès refusé. Seuls les propriétaires ou administrateurs peuvent voir les demandes.</p>';
                openModal('projectRequestsModal');
                return;
            }
            if (response.status === 204) {
                container.innerHTML = '<p>Aucune demande en attente.</p>';
                openModal('projectRequestsModal');
                return;
            }
            throw new Error('Failed to load requests');
        }

        const requests = await response.json();
        if (!requests || requests.length === 0) {
            container.innerHTML = '<p>Aucune demande en attente.</p>';
        } else {
            requests.forEach(req => {
                const item = document.createElement('div');
                item.className = 'member-item';
                item.innerHTML = `
                    <div class="member-info">
                        <div class="member-name">${escapeHtml(req.user.firstName + ' ' + req.user.lastName)}</div>
                        <div class="member-role">Demandeur</div>
                        <div class="member-message">${escapeHtml(req.message || '')}</div>
                    </div>
                    <div class="member-actions">
                        <button class="btn-small btn-members" onclick="acceptProjectRequest('${req.id}')">Accepter</button>
                        <button class="btn-small btn-delete" onclick="rejectProjectRequest('${req.id}')">Refuser</button>
                    </div>
                `;
                container.appendChild(item);
            });
        }

        openModal('projectRequestsModal');
    } catch (error) {
        console.error('Error loading requests:', error);
        showAlert('Erreur lors du chargement des demandes', 'error');
    }
}

async function updateProjectRequestsBadge(projectId) {
    const btn = document.getElementById(`requests-btn-${projectId}`);
    if (!btn) return;
    try {
        const response = await fetch(`/api/project/${projectId}/requests/pending`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) {
            btn.textContent = 'Demandes';
            return;
        }
        const requests = await response.json();
        const count = Array.isArray(requests) ? requests.length : 0;
        btn.textContent = count > 0 ? `Demandes (${count})` : 'Demandes';
        if (count > 0) {
            btn.classList.add('btn-notice');
        } else {
            btn.classList.remove('btn-notice');
        }
    } catch (e) {
        console.error('Error updating requests badge:', e);
        btn.textContent = 'Demandes';
    }
}

async function acceptProjectRequest(requestId) {
    try {
        const response = await fetch(`/api/project/request/${requestId}/accept`, {
            method: 'PUT',
            headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) {
            const txt = await response.text();
            throw new Error(txt || 'Échec de l\'acceptation');
        }
        showAlert('Demande acceptée', 'success');
        closeModal('projectRequestsModal');
        if (window.currentRequestsProjectId) {
            updateProjectRequestsBadge(window.currentRequestsProjectId);
        }
        loadUserProjects();
    } catch (e) {
        console.error('Accept request error:', e);
        showAlert('Erreur: ' + e.message, 'error');
    }
}

async function rejectProjectRequest(requestId) {
    try {
        const response = await fetch(`/api/project/request/${requestId}/reject`, {
            method: 'PUT',
            headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) {
            const txt = await response.text();
            throw new Error(txt || 'Échec du refus');
        }
        showAlert('Demande refusée', 'success');
        closeModal('projectRequestsModal');
        if (window.currentRequestsProjectId) {
            updateProjectRequestsBadge(window.currentRequestsProjectId);
        }
        loadUserProjects();
    } catch (e) {
        console.error('Reject request error:', e);
        showAlert('Erreur: ' + e.message, 'error');
    }
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

async function updateMemberRole(projectId, memberId, newRole) {
    if (!confirm(`Êtes-vous sûr de vouloir changer le rôle de ce membre à ${getMemberRoleLabel(newRole)}?`)) {
        // Reload to reset the select
        showProjectMembers(projectId);
        return;
    }

    try {
        const response = await fetch(`/api/project/${projectId}/member/${memberId}/role?role=${newRole}`, {
            method: 'PUT',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            const errorData = await response.text();
            throw new Error(errorData);
        }

        showAlert('Rôle mis à jour avec succès', 'success');
        showProjectMembers(projectId);
    } catch (error) {
        console.error('Error updating member role:', error);
        showAlert('Erreur lors de la mise à jour du rôle: ' + error.message, 'error');
        showProjectMembers(projectId);
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
    // Try to get from model attribute in HTML (most reliable)
    const currentUserIdElement = document.querySelector('[data-current-user-id]');
    if (currentUserIdElement) {
        const userId = currentUserIdElement.getAttribute('data-current-user-id');
        if (userId && userId.trim() !== '') {
            return userId;
        }
    }

    // Try to get from data attribute
    const userIdElement = document.querySelector('[data-user-id]');
    if (userIdElement) {
        const userId = userIdElement.getAttribute('data-user-id');
        if (userId && userId.trim() !== '') {
            return userId;
        }
    }

    // Try to get from meta tag
    const metaUserId = document.querySelector('meta[data-user-id]');
    if (metaUserId) {
        const userId = metaUserId.getAttribute('content');
        if (userId && userId.trim() !== '') {
            return userId;
        }
    }

    // No valid ID found
    console.error('Unable to retrieve current user ID from page');
    return '';
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
            // If role not found (not a member), return NONE
            return 'NONE';
        }

        // Backend returns a JSON string enum (e.g., "OWNER")
        const role = await response.json();
        return typeof role === 'string' ? role : 'NONE';
    } catch (error) {
        console.error('Error getting user role:', error);
        return 'NONE';
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

async function loadProjectSkills(projectId) {
    try {
        const response = await fetch(`/api/project/${projectId}/skills`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            return;
        }

        const skills = await response.json();
        displayProjectSkills(projectId, skills);
    } catch (error) {
        console.error('Error loading skills:', error);
    }
}

function displayProjectSkills(projectId, skills) {
    const skillsContainer = document.getElementById(`skills-${projectId}`);
    if (!skillsContainer) return;

    if (!skills || skills.length === 0) {
        skillsContainer.innerHTML = '';
        return;
    }

    const skillsHTML = skills.map(skill =>
        `<span class="skill-badge">${escapeHtml(skill.skillName)}</span>`
    ).join('');

    skillsContainer.innerHTML = `
        <div class="skills-section">
            <strong>Compétences recherchées:</strong>
            <div class="skills-list">${skillsHTML}</div>
        </div>
    `;
}

async function loadProjectSkills(projectId) {
    try {
        const response = await fetch(`/api/project/${projectId}/skills`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            return;
        }

        const skills = await response.json();
        displayProjectSkills(projectId, skills);
    } catch (error) {
        console.error('Error loading skills:', error);
    }
}

function displayProjectSkills(projectId, skills) {
    const skillsContainer = document.getElementById(`skills-${projectId}`);
    if (!skillsContainer) {
        return;
    }

    if (!skills || skills.length === 0) {
        skillsContainer.innerHTML = '';
        return;
    }

    const skillsHTML = `
        <div class="skills-section">
            <strong>Compétences recherchées:</strong>
            <div class="skills-list">
                ${skills.map(skill => `
                    <span class="skill-badge">${escapeHtml(skill.skillName)}</span>
                `).join('')}
            </div>
        </div>
    `;

    skillsContainer.innerHTML = skillsHTML;
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

// Transfer Ownership Functions
async function showTransferOwnershipModal(projectId) {
    const modal = document.getElementById('transferOwnershipModal');

    try {
        // Store projectId for delete button
        window.currentProjectIdForLeaving = projectId;

        // Get project members
        const response = await fetch(`/api/project/${projectId}/members`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to load project members');
        }

        const members = await response.json();
        const membersList = document.getElementById('transferMembersList');
        membersList.innerHTML = '';

        // Display members as clickable options
        members.forEach(member => {
            const memberDiv = document.createElement('div');
            memberDiv.className = 'transfer-member-card';

            const displayName = escapeHtml(member.user.firstName + ' ' + member.user.lastName);
            memberDiv.innerHTML = `
                <div class="transfer-member-name">${displayName}</div>
                <div class="transfer-member-email">${escapeHtml(member.user.email)}</div>
                <div class="transfer-member-role">Rôle actuel: ${getMemberRoleLabel(member.role)}</div>
            `;

            memberDiv.onclick = () => transferOwnership(projectId, member.user.id, modal);
            membersList.appendChild(memberDiv);
        });

        // Setup modal close button
        document.getElementById('closeTransferModal').onclick = () => closeModal('transferOwnershipModal');

        // Setup delete button
        document.getElementById('deleteProjectBtn').onclick = () => deleteProjectFromModal(projectId, modal);

        modal.classList.add('active');
    } catch (error) {
        console.error('Error showing transfer modal:', error);
        showAlert('Erreur: ' + error.message, 'error');
    }
}

async function transferOwnership(projectId, newOwnerId, modal) {
    if (!confirm('Êtes-vous sûr de vouloir transférer la propriété à cet utilisateur?\nVous pourrez quitter le projet ensuite.')) {
        return;
    }

    try {
        const response = await fetch(`/api/project/${projectId}/transfer-ownership?newOwnerId=${newOwnerId}`, {
            method: 'PUT',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            const errorData = await response.text();
            throw new Error(errorData);
        }

        showAlert('Propriété transférée avec succès', 'success');
        closeModal('transferOwnershipModal');

        // Now leave the project
        await leaveProject(projectId);
    } catch (error) {
        console.error('Error transferring ownership:', error);
        showAlert('Erreur lors du transfert: ' + error.message, 'error');
    }
}

async function deleteProjectFromModal(projectId, modal) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer définitivement ce projet?\nCette action ne peut pas être annulée.')) {
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
        closeModal('transferOwnershipModal');
        loadUserProjects();
    } catch (error) {
        console.error('Error deleting project:', error);
        showAlert('Erreur lors de la suppression: ' + error.message, 'error');
    }
}
