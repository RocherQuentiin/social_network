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

    // Add click handler for add project card
    const addProjectCard = document.getElementById('add-project-card');
    if (addProjectCard) {
        addProjectCard.addEventListener('click', function() { window.location.href = '/projects'; });
        addProjectCard.style.cursor = 'pointer';
    }

    // Initialize lucide icons if present
    if (typeof lucide !== 'undefined') lucide.createIcons();
});


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