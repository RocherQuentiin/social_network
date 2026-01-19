// userProfile.js
function byId(id){ return document.getElementById(id); }

// Event modal handlers
var editModal = byId('editEventModal');
var closeEventEditModal = byId('closeEventEditModal');
var addEventBtn = byId('add-event-btn');
function openModal(){ if(editModal) editModal.style.display = 'flex'; }
function closeModal(){ if(editModal) editModal.style.display = 'none'; }
if(closeEventEditModal) closeEventEditModal.addEventListener('click', closeModal);
if(addEventBtn) addEventBtn.addEventListener('click', openModal);

var editEvent = byId('edit-event');
if(editEvent) editEvent.addEventListener('click', function(e){
    let eventId = e.currentTarget.getAttribute('data-id');
    window.location.href = "/event/" + eventId;
});

// Hobby modal & delete
var hobbyModal = byId('editHobbyModal');
var loisir = byId('loisir');
var closeHobbyEditModal = byId('closeHobbyEditModal');
if(closeHobbyEditModal) closeHobbyEditModal.addEventListener('click', function(){ if(hobbyModal) hobbyModal.style.display = 'none'; });
if(loisir) loisir.addEventListener('click', function(){ if(hobbyModal) hobbyModal.style.display = 'flex'; });

document.querySelectorAll(".interest-item").forEach(hobby => {
    hobby.addEventListener("click", function(ev){
        if (!confirm('Voulez-vous vraiment supprimer cette hobby ?')) return;
        const hobbyName = ev.currentTarget.getAttribute('data-value');
        fetch('/user/hobby/delete', {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'hobbyName=' + encodeURIComponent(hobbyName)
        }).then(function(r) {
            if (r.ok) {  window.location.href = "/profil"; }
            else if (r.status === 403) { alert('Accès refusé'); }
            else { r.text().then(function(t) { console.error(t); alert('Erreur lors de la sauvegarde'); }); }
        }).catch(function(err) { console.error(err); alert('Erreur réseau'); });
    });
});

// Competence modal & delete
var competenceModal = byId('editCompetenciesModal');
var competencies = byId('competencies');
var closeCompetenciesEditModal = byId('closeCompetenciesEditModal');
if(closeCompetenciesEditModal) closeCompetenciesEditModal.addEventListener('click', function(){ if(competenceModal) competenceModal.style.display = 'none'; });
if(competencies) competencies.addEventListener('click', function(){ if(competenceModal) competenceModal.style.display = 'flex'; });

document.querySelectorAll(".competence-container").forEach(compEl => {
    compEl.addEventListener("click", function(ev){
        if (!confirm('Voulez-vous vraiment supprimer cette compétence ?')) return;
        const competenceName = ev.currentTarget.getAttribute('data-value');
        fetch('/user/competence/delete', {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'competenceName=' + encodeURIComponent(competenceName)
        }).then(function(r) {
            if (r.ok) {  window.location.href = "/profil"; }
            else if (r.status === 403) { alert('Accès refusé'); }
            else { r.text().then(function(t) { console.error(t); alert('Erreur lors de la sauvegarde'); }); }
        }).catch(function(err) { console.error(err); alert('Erreur réseau'); });
    });
});

// Load user projects dynamically on page load
document.addEventListener('DOMContentLoaded', function() {
    loadUserProjects();

    // Add click handler for add project card
    const addProjectCard = document.getElementById('add-project-card');
    if (addProjectCard) {
        addProjectCard.addEventListener('click', function() { window.location.href = '/projects'; });
        addProjectCard.style.cursor = 'pointer';
    }

    // Initialize lucide icons if present
    if (typeof lucide !== 'undefined') lucide.createIcons();
});

async function loadUserProjects() {
    // prefer viewedUserId if available, otherwise use currentUserId
    const userId = window.viewedUserId || window.currentUserId;
    if (!userId) return;
    const projectsContainer = document.getElementById('user-projects-profile');
    if (!projectsContainer) return;

    try {
        const response = await fetch(`/api/project/user/${userId}`, { method: 'GET', headers: { 'Accept': 'application/json' } });
        if (!response.ok) { console.error('Failed to load projects'); return; }
        const projects = await response.json();
        displayProjectsOnProfile(projects);
    } catch (error) {
        console.error('Error loading projects:', error);
    }
}

function displayProjectsOnProfile(projects) {
    const projectsContainer = document.getElementById('user-projects-profile');
    const addProjectCard = document.getElementById('add-project-card');
    if (!projectsContainer) return;

    // Remove existing project items
    projectsContainer.querySelectorAll('.project-item').forEach(p => p.remove());

    if (!projects || projects.length === 0) return;

    const projectsToShow = projects.slice(0, 3);
    projectsToShow.forEach(project => {
        const projectCard = createProjectCardForProfile(project);
        if (addProjectCard) projectsContainer.insertBefore(projectCard, addProjectCard);
        else projectsContainer.appendChild(projectCard);
    });

    if (typeof lucide !== 'undefined') lucide.createIcons();
}

function createProjectCardForProfile(project) {
    const card = document.createElement('div');
    card.className = 'card project-item';
    const visibilityBadge = getVisibilityBadge(project.visibilityType);
    const description = project.description || 'Aucune description';
    const truncatedDesc = description.length > 80 ? description.substring(0, 80) + '...' : description;
    card.innerHTML = `
        <div class="project-info">
            <h4>${escapeHtml(project.name)} <span class="type-tag">${visibilityBadge}</span></h4>
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
    if (!text) return '';
    const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
    return String(text).replace(/[&<>"']/g, m => map[m]);
}