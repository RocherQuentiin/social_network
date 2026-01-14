// Load public projects of a specific user

document.addEventListener('DOMContentLoaded', function() {
    displayUserName();
    loadPublicProjects();
    setupEventListeners();
});

function setupEventListeners() {
    // Close join modal
    const closeJoinModal = document.getElementById('closeJoinModal');
    if (closeJoinModal) {
        closeJoinModal.addEventListener('click', () => closeModal('joinProjectModal'));
    }

    // Join project form submission
    const joinForm = document.getElementById('joinProjectForm');
    if (joinForm) {
        joinForm.addEventListener('submit', handleJoinProject);
    }

    // Close modal when clicking outside
    window.addEventListener('click', (event) => {
        const modal = document.getElementById('joinProjectModal');
        if (event.target == modal) {
            modal.classList.remove('active');
        }
    });
}

function displayUserName() {
    const userName = window.userName;
    const userNameElement = document.getElementById('user-name');
    if (userNameElement && userName) {
        userNameElement.textContent = userName;
    }
}

async function loadPublicProjects() {
    const userId = window.viewedUserId;
    if (!userId) {
        showAlert('Utilisateur non trouvé', 'error');
        return;
    }

    const projectsList = document.getElementById('user-public-projects');
    if (!projectsList) return;

    try {
        const response = await fetch(`/api/project/creator/${userId}/public`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
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
    const projectsList = document.getElementById('user-public-projects');
    projectsList.innerHTML = '';

    if (!projects || projects.length === 0) {
        displayNoProjects();
        return;
    }

    projects.forEach(project => {
        const projectCard = createProjectCard(project);
        projectsList.appendChild(projectCard);
    });

    // Re-initialize lucide icons
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
}

function displayNoProjects() {
    const projectsList = document.getElementById('user-public-projects');
    projectsList.innerHTML = `
        <div class="empty-state">
            <p>Cet utilisateur n'a pas encore de projets publics.</p>
        </div>
    `;
}

function createProjectCard(project) {
    const card = document.createElement('div');
    card.className = 'project-card';

    const visibilityLabel = getVisibilityLabel(project.visibilityType);
    const description = project.description || 'Aucune description';
    const createdAt = formatDate(project.createdAt);

    let skillsHTML = '';
    if (project.skills && project.skills.length > 0) {
        skillsHTML = `
            <div class="project-skills">
                <div class="skills-section">
                    <strong>Compétences recherchées</strong>
                    <div class="skills-list">
                        ${project.skills.map(skill => `<span class="skill-badge">${escapeHtml(skill.skillName)}</span>`).join('')}
                    </div>
                </div>
            </div>
        `;
    }

    card.innerHTML = `
        <h3>${escapeHtml(project.name)}</h3>
        <p class="description">${escapeHtml(description)}</p>
        <span class="visibility">${visibilityLabel}</span>
        ${skillsHTML}
        <div class="metadata">
            <span>
                <i data-lucide="calendar"></i>
                ${createdAt}
            </span>
        </div>
        <div class="actions">
            <button class="btn-small btn-members" onclick="openJoinModal('${project.id}', '${escapeHtml(project.name)}', '${escapeHtml(description)}')">Rejoindre</button>
        </div>
    `;

    return card;
}

function openJoinModal(projectId, projectName, projectDescription) {
    document.getElementById('joinProjectId').value = projectId;
    document.getElementById('joinProjectName').textContent = projectName;
    document.getElementById('joinProjectDesc').textContent = projectDescription;
    document.getElementById('joinMessage').value = '';
    document.getElementById('joinProjectModal').classList.add('active');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
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
            body: JSON.stringify({
                message: message,
                skillName: null
            })
        });

        if (!response.ok) {
            const errorData = await response.text();
            if (response.status === 409) {
                showAlert('Vous êtes déjà membre de ce projet ou avez déjà demandé à rejoindre', 'info');
            } else {
                showAlert(errorData || 'Erreur lors de l\'envoi de la demande', 'error');
            }
            return;
        }

        showAlert('Demande d\'adhésion envoyée avec succès!', 'success');
        closeModal('joinProjectModal');
        
        // Reload projects
        setTimeout(() => {
            loadPublicProjects();
        }, 1500);
    } catch (error) {
        console.error('Error joining project:', error);
        showAlert('Erreur lors de l\'envoi de la demande', 'error');
    }
}

function getVisibilityLabel(visibility) {
    switch(visibility) {
        case 'PUBLIC': return 'Public';
        case 'FRIENDS': return 'Amis';
        case 'PRIVATE': return 'Privé';
        default: return visibility;
    }
}

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    });
}

function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}

function showAlert(message, type) {
    // Simple alert implementation
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    alertDiv.style.cssText = 'position: fixed; top: 20px; right: 20px; padding: 15px 20px; background: #1d9bf0; color: white; border-radius: 8px; z-index: 1000;';
    
    document.body.appendChild(alertDiv);
    
    setTimeout(() => {
        alertDiv.remove();
    }, 3000);
}
