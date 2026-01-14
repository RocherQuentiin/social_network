const userFollow = document.querySelectorAll(".follow");
const userUnfollow = document.querySelectorAll(".unfollow");

userFollow.forEach(el => {
	el.addEventListener('click', followUser, el);
});

userUnfollow.forEach(el => {
	el.addEventListener('click', unfollowUser, el);
});

function followUser(el) {
	let userId = el.srcElement.getAttribute('data-id');
	fetch('/follow', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		body: 'userID=' + userId
	})
		.then(response => {
			if(response.status == 200){
				window.location.reload();
			}
		})
}

function unfollowUser(el) {
	let userId = el.srcElement.getAttribute('data-id');
	fetch('/unfollow', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		body: 'userID=' + userId
	})
		.then(response => {
			if(response.status == 200){
				window.location.reload();
			}
		})
}

function byId(id){ return document.getElementById(id); }

var modal = byId('userRecommandationModal');
var closeRecommandationModal = byId('closeUserRecommandationModal');
var recommandationBtn = byId('recommandationBtn');

function openModal(){ if(modal) modal.style.display = 'flex'; }
function closeModal(){ if(modal) modal.style.display = 'none'; currentEditId = null; }


 if(closeRecommandationModal) closeRecommandationModal.addEventListener('click', function(){ closeModal(); });
 if(recommandationBtn) recommandationBtn.addEventListener('click', function(){ openModal(); });

// Load user projects dynamically
document.addEventListener('DOMContentLoaded', function() {
    loadUserProjectsView();
});

function setupJoinProjectModal() {
    const closeBtn = document.getElementById('closeJoinProjectModal');
    const form = document.getElementById('joinProjectForm');
    
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            const modal = document.getElementById('joinProjectModal');
            if (modal) modal.classList.remove('active');
        });
    }
    
    if (form) {
        form.addEventListener('submit', handleJoinProjectFromProfile);
    }
    
    // Close modal when clicking outside
    window.addEventListener('click', (event) => {
        const modal = document.getElementById('joinProjectModal');
        if (event.target == modal) {
            modal.classList.remove('active');
        }
    });
}

async function loadUserProjectsView() {
    const viewedUserId = window.viewedUserId;
    
    if (!viewedUserId) return;
    
    const projectsContainer = document.getElementById('user-projects-view');
    if (!projectsContainer) return;
    
    try {
        // Load public projects created by the viewed user
        const response = await fetch(`/api/project/creator/${viewedUserId}/public?t=${Date.now()}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });
        
        if (!response.ok) {
            console.error('Failed to load projects');
            projectsContainer.innerHTML = '<p style="padding: 20px; text-align: center; color: #666;">Aucun projet public</p>';
            return;
        }
        
        const projects = await response.json();
        displayProjectsOnViewProfile(projects);
    } catch (error) {
        console.error('Error loading projects:', error);
        projectsContainer.innerHTML = '<p style="padding: 20px; text-align: center; color: #666;">Erreur lors du chargement des projets</p>';
    }
}

function displayProjectsOnViewProfile(projects) {
    const projectsContainer = document.getElementById('user-projects-view');
    
    if (!projects || projects.length === 0) {
        projectsContainer.innerHTML = '<p style="padding: 20px; text-align: center; color: #666;">Aucun projet public</p>';
        return;
    }
    
    // Show only first 3 projects
    const projectsToShow = projects.slice(0, 3);
    projectsContainer.innerHTML = '';
    
    projectsToShow.forEach(project => {
        const projectCard = createProjectCardForViewProfile(project);
        projectsContainer.appendChild(projectCard);
    });
    
    // Re-initialize lucide icons
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
}

function createProjectCardForViewProfile(project) {
    const card = document.createElement('div');
    card.className = 'card project-item';
    
    const visibilityBadge = getVisibilityBadgeView(project.visibilityType);
    const description = project.description || 'Aucune description';
    const truncatedDesc = description.length > 80 ? description.substring(0, 80) + '...' : description;
    
    card.innerHTML = `
        <div style="display: flex; gap: 15px; align-items: flex-start;">
            <div class="project-info" style="flex: 1;">
                <h4 style="margin-top: 0;">
                    ${escapeHtmlView(project.name)} 
                    <span class="type-tag">${visibilityBadge}</span>
                </h4>
                <p>${escapeHtmlView(truncatedDesc)}</p>
            </div>
        </div>
    `;
    
    return card;
}

function getVisibilityBadgeView(visibility) {
    switch(visibility) {
        case 'PUBLIC': return 'Public';
        case 'FRIENDS': return 'Amis';
        case 'PRIVATE': return 'Privé';
        default: return visibility;
    }
}

function escapeHtmlView(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}

function openJoinProjectModal(projectId, projectName, projectDescription) {
    const modal = document.getElementById('joinProjectModal');
    if (!modal) return;
    
    document.getElementById('joinProjectId').value = projectId;
    document.getElementById('joinProjectName').textContent = projectName;
    document.getElementById('joinProjectDesc').textContent = projectDescription;
    document.getElementById('joinMessage').value = '';
    modal.classList.add('active');
}

function closeJoinProjectModalView() {
    const modal = document.getElementById('joinProjectModal');
    if (modal) modal.classList.remove('active');
}

async function handleJoinProjectFromProfile(e) {
    e.preventDefault();
    
    const projectId = document.getElementById('joinProjectId').value;
    const message = document.getElementById('joinMessage').value.trim();

    if (!projectId) {
        showAlertView('Projet introuvable', 'error');
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
                showAlertView('Vous êtes déjà membre de ce projet ou avez déjà demandé à rejoindre', 'info');
            } else {
                showAlertView(errorData || 'Erreur lors de l\'envoi de la demande', 'error');
            }
            return;
        }

        showAlertView('Demande d\'adhésion envoyée avec succès!', 'success');
        closeJoinProjectModalView();
    } catch (error) {
        console.error('Error joining project:', error);
        showAlertView('Erreur lors de l\'envoi de la demande', 'error');
    }
}

function showAlertView(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    alertDiv.style.cssText = 'position: fixed; top: 20px; right: 20px; padding: 15px 20px; background: #1d9bf0; color: white; border-radius: 8px; z-index: 1000;';
    
    document.body.appendChild(alertDiv);
    
    setTimeout(() => {
        alertDiv.remove();
    }, 3000);
}