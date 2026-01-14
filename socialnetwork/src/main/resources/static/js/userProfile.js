function byId(id){ return document.getElementById(id); }

var editModal = byId('editEventModal');
var closeEventEditModal = byId('closeEventEditModal');
var addEventBtn = byId('add-event-btn');

function openModal(){ if(editModal) editModal.style.display = 'flex'; }
function closeModal(){ if(editModal) editModal.style.display = 'none'; currentEditId = null; }


 if(closeEventEditModal) closeEventEditModal.addEventListener('click', function(){ closeModal(); });
 if(addEventBtn) addEventBtn.addEventListener('click', function(){ openModal(); });

 var editEvent = byId('edit-event');
 
 if(editEvent) editEvent.addEventListener('click', function(e){ callEventPage(e); })
  
 function callEventPage(el){
	let eventId = el.currentTarget.getAttribute('data-id');
	window.location.href = "/event/" + eventId;
}

// Load user projects dynamically
document.addEventListener('DOMContentLoaded', function() {
    loadUserProjects();
    
    // Add click handler for add project card
    const addProjectCard = document.getElementById('add-project-card');
    if (addProjectCard) {
        addProjectCard.addEventListener('click', function() {
            window.location.href = '/projects';
        });
        addProjectCard.style.cursor = 'pointer';
    }
});

async function loadUserProjects() {
    const userId = window.currentUserId;
    if (!userId) return;
    
    const projectsContainer = document.getElementById('user-projects-profile');
    if (!projectsContainer) return;
    
    try {
        const response = await fetch(`/api/project/user/${userId}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });
        
        if (!response.ok) {
            console.error('Failed to load projects');
            return;
        }
        
        const projects = await response.json();
        displayProjectsOnProfile(projects);
    } catch (error) {
        console.error('Error loading projects:', error);
    }
}

function displayProjectsOnProfile(projects) {
    const projectsContainer = document.getElementById('user-projects-profile');
    const addProjectCard = document.getElementById('add-project-card');
    
    // Clear existing projects but keep the add card
    const existingProjects = projectsContainer.querySelectorAll('.project-item');
    existingProjects.forEach(project => project.remove());
    
    if (!projects || projects.length === 0) {
        // Keep only the add project card
        return;
    }
    
    // Show only first 3 projects
    const projectsToShow = projects.slice(0, 3);
    
    projectsToShow.forEach(project => {
        const projectCard = createProjectCardForProfile(project);
        projectsContainer.insertBefore(projectCard, addProjectCard);
    });
    
    // Re-initialize lucide icons
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
}

function createProjectCardForProfile(project) {
    const card = document.createElement('div');
    card.className = 'card project-item';
    
    const visibilityBadge = getVisibilityBadge(project.visibilityType);
    const description = project.description || 'Aucune description';
    const truncatedDesc = description.length > 80 ? description.substring(0, 80) + '...' : description;
    
    card.innerHTML = `
        <div class="project-info">
            <h4>
                ${escapeHtml(project.name)} 
                <span class="type-tag">${visibilityBadge}</span>
            </h4>
            <p>${escapeHtml(truncatedDesc)}</p>
            <a href="/projects" class="details-link">Gérer <i data-lucide="arrow-right"></i></a>
        </div>
    `;
    
    return card;
}

function getVisibilityBadge(visibility) {
    switch(visibility) {
        case 'PUBLIC': return 'Public';
        case 'FRIENDS': return 'Amis';
        case 'PRIVATE': return 'Privé';
        default: return visibility;
    }
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