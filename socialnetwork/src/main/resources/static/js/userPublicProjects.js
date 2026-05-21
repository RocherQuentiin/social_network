// Projets publics d'un utilisateur (page /projects/user/{id})

document.addEventListener('DOMContentLoaded', () => {
    displayUserName();
    handlePaymentFeedback();
    loadPublicProjects();
    setupEventListeners();
});

function setupEventListeners() {
    const closeJoinModal = document.getElementById('closeJoinModal');
    if (closeJoinModal) {
        closeJoinModal.addEventListener('click', () => closeModal('joinProjectModal'));
    }

    const joinForm = document.getElementById('joinProjectForm');
    if (joinForm) {
        joinForm.addEventListener('submit', handleJoinProject);
    }

    window.addEventListener('click', (event) => {
        const modal = document.getElementById('joinProjectModal');
        if (modal && event.target === modal) {
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

function isOwnPublicProfile() {
    const cur = (window.currentUserId || '').toString().trim();
    const viewed = (window.viewedUserId || '').toString().trim();
    return cur.length > 0 && cur === viewed;
}

async function loadPublicProjects() {
    const userId = window.viewedUserId;
    if (!userId) {
        showAlert('Utilisateur non trouvé', 'error');
        return;
    }

    const projectsList = document.getElementById('user-public-projects');
    if (!projectsList) {
        return;
    }

    try {
        const response = await fetch(`/api/project/creator/${userId}/public?t=${Date.now()}`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
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

    projects.forEach((project) => {
        projectsList.appendChild(createProjectCard(project));
    });

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

function isProjectPaid(project) {
    if (!project) {
        return false;
    }
    return project.isPaid === true || project.isPaid === 'true' || project.paid === true;
}

function formatPrice(price) {
    const value = Number(price || 0);
    return `${value.toFixed(2)} €`;
}

function renderPaidBadge(project) {
    if (!isProjectPaid(project)) {
        return '';
    }
    return `<span class="visibility paid-project-badge" title="Projet payant">Payant · ${formatPrice(project.price)}</span>`;
}

function redirectToPaymentPage(projectId, returnTo) {
    window.location.href = `/projects/${projectId}/payment?returnTo=${encodeURIComponent(returnTo || '/projects')}`;
}

function createProjectCard(project) {
    const card = document.createElement('div');
    card.className = 'project-card';

    const visibilityLabel = getVisibilityLabel(project.visibilityType);
    const description = project.description || 'Aucune description';
    const createdAt = formatDate(project.createdAt);
    const returnTo = `/projects/user/${window.viewedUserId}`;

    let skillsHTML = '';
    if (project.skills && project.skills.length > 0) {
        skillsHTML = `
            <div class="project-skills">
                <div class="skills-section">
                    <strong>Compétences recherchées</strong>
                    <div class="skills-list">
                        ${project.skills.map((skill) => `<span class="skill-badge">${escapeHtml(skill.skillName)}</span>`).join('')}
                    </div>
                </div>
            </div>
        `;
    }

    const actionsWrap = document.createElement('div');
    actionsWrap.className = 'actions';

    if (isOwnPublicProfile()) {
        const span = document.createElement('span');
        span.className = 'visibility';
        span.style.cssText = 'border-color: transparent; background-color: rgba(29,155,240,0.08);';
        span.textContent = 'Votre projet';
        actionsWrap.appendChild(span);
    } else if (isProjectPaid(project)) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'btn-small btn-members';
        btn.textContent = 'Rejoindre';
        btn.title = `Paiement requis : ${formatPrice(project.price)}`;
        btn.addEventListener('click', () => redirectToPaymentPage(project.id, returnTo));
        actionsWrap.appendChild(btn);
    } else {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'btn-small btn-members';
        btn.textContent = 'Rejoindre';
        btn.addEventListener('click', () => openJoinModal(project.id, project.name, description));
        actionsWrap.appendChild(btn);
    }

    card.innerHTML = `
        <h3>${escapeHtml(project.name)}</h3>
        <p class="description">${escapeHtml(description)}</p>
        <span class="visibility">${visibilityLabel}</span>
        ${renderPaidBadge(project)}
        ${skillsHTML}
        <div class="metadata">
            <span>
                <i data-lucide="calendar"></i>
                ${createdAt}
            </span>
        </div>
    `;

    card.appendChild(actionsWrap);
    return card;
}

function openJoinModal(projectId, projectName, projectDescription) {
    document.getElementById('joinProjectId').value = projectId;
    document.getElementById('joinProjectName').textContent = projectName;
    document.getElementById('joinProjectDesc').textContent = projectDescription || '';
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

        setTimeout(() => {
            loadPublicProjects();
        }, 800);
    } catch (error) {
        console.error('Error joining project:', error);
        showAlert('Erreur lors de l\'envoi de la demande', 'error');
    }
}

function getVisibilityLabel(visibility) {
    switch (visibility) {
        case 'PUBLIC':
            return 'Public';
        case 'FRIENDS':
            return 'Amis';
        case 'PRIVATE':
            return 'Privé';
        default:
            return visibility;
    }
}

function formatDate(dateString) {
    if (!dateString) {
        return '';
    }
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
    return String(text).replace(/[&<>"']/g, (m) => map[m]);
}

function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    alertDiv.style.cssText =
        'position: fixed; top: 20px; right: 20px; padding: 15px 20px; background: #1d9bf0; color: white; border-radius: 8px; z-index: 1000;';

    document.body.appendChild(alertDiv);

    setTimeout(() => {
        alertDiv.remove();
    }, 3500);
}

function handlePaymentFeedback() {
    const url = new URL(window.location.href);
    const paymentStatus = url.searchParams.get('payment');
    if (!paymentStatus) {
        return;
    }

    if (paymentStatus === 'success') {
        showAlert('Paiement réussi, vous avez rejoint le projet.', 'success');
    } else if (paymentStatus === 'failed') {
        showAlert('Le paiement a échoué. Vérifiez les informations de carte de test.', 'error');
    } else if (paymentStatus === 'unavailable') {
        showAlert('Ce projet n\'est pas disponible au paiement.', 'error');
    } else if (paymentStatus === 'own-project') {
        showAlert('Vous ne pouvez pas payer votre propre projet.', 'info');
    }

    url.searchParams.delete('payment');
    url.searchParams.delete('projectId');
    const nextUrl = url.pathname + (url.search ? url.search : '');
    window.history.replaceState({}, '', nextUrl);
}
