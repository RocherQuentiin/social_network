// Load public projects of a specific user

document.addEventListener('DOMContentLoaded', function() {
    displayUserName();
    loadPublicProjects();
});

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

    card.innerHTML = `
        <div class="project-header">
            <h3>${escapeHtml(project.name)}</h3>
            <span class="visibility-badge visibility-${project.visibilityType.toLowerCase()}">${visibilityLabel}</span>
        </div>
        <p class="project-description">${escapeHtml(description)}</p>
        <div class="project-footer">
            <span class="project-date">
                <i data-lucide="calendar"></i>
                ${createdAt}
            </span>
        </div>
    `;

    return card;
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
