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

async function loadUserProjectsView() {
    const viewedUserId = window.viewedUserId;
    
    if (!viewedUserId) return;
    
    const projectsContainer = document.getElementById('user-projects-view');
    if (!projectsContainer) return;
    
    try {
        // Load public projects created by the viewed user
        const response = await fetch(`/api/project/creator/${viewedUserId}/public`, {
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
        <div class="project-info">
            <h4>
                ${escapeHtmlView(project.name)} 
                <span class="type-tag">${visibilityBadge}</span>
            </h4>
            <p>${escapeHtmlView(truncatedDesc)}</p>
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