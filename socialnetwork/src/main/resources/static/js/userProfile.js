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

 var hobbyModal = byId('editHobbyModal');
 var loisir = byId('loisir');
 var closeHobbyEditModal = byId('closeHobbyEditModal');
 
 if(closeHobbyEditModal) closeHobbyEditModal.addEventListener('click', function(){ closeHobbyModal(); });
 if(loisir) loisir.addEventListener('click', function(){ openHobbyModal(); })
 
 function openHobbyModal(){ if(hobbyModal) hobbyModal.style.display = 'flex'; }
 function closeHobbyModal(){ if(hobbyModal) hobbyModal.style.display = 'none'; }
 
 
const deleteHobby = document.querySelectorAll(".interest-item");

deleteHobby.forEach(hobby => {
	hobby.addEventListener("click", DeleteHobbyAsync, hobby)
})


function DeleteHobbyAsync(hobby) {
if (!confirm('Voulez-vous vraiment supprimer cette hobby ?')) return;

	let hobbyName = hobby.currentTarget.getAttribute('data-value');
	console.log(hobbyName)
	fetch('/user/hobby/delete', {
		method: 'DELETE',
		headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
		body: 'hobbyName=' + hobbyName
	}).then(function(r) {
		if (r.ok) {  
			window.location.href = "/profil"; 
		}
		else if (r.status === 403) { alert('Accès refusé'); }
		else { r.text().then(function(t) { console.error(t); alert('Erreur lors de la sauvegarde'); }); }
	}).catch(function(err) { console.error(err); alert('Erreur réseau'); });
}

 var competenceModal = byId('editCompetenciesModal');
 var competencies = byId('competencies');
 var closeCompetenciesEditModal = byId('closeCompetenciesEditModal');
 
 if(closeCompetenciesEditModal) closeCompetenciesEditModal.addEventListener('click', function(){ closeCompetenceModal(); });
 if(competencies) competencies.addEventListener('click', function(){ openCompetenceModal(); })
 
 function openCompetenceModal(){ if(competenceModal) competenceModal.style.display = 'flex'; }
 function closeCompetenceModal(){ if(competenceModal) competenceModal.style.display = 'none'; }
 
 
 const deleteCompetence = document.querySelectorAll(".competence-container");

deleteCompetence.forEach(competencies => {
	competencies.addEventListener("click", DeleteCompetenceAsync, competencies)
})


function DeleteCompetenceAsync(comp) {
if (!confirm('Voulez-vous vraiment supprimer cette compétence ?')) return;

	let competenceName = comp.currentTarget.getAttribute('data-value');
	fetch('/user/competence/delete', {
		method: 'DELETE',
		headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
		body: 'competenceName=' + competenceName
	}).then(function(r) {
		if (r.ok) {  
			window.location.href = "/profil"; 
		}
		else if (r.status === 403) { alert('Accès refusé'); }
		else { r.text().then(function(t) { console.error(t); alert('Erreur lors de la sauvegarde'); }); }
	}).catch(function(err) { console.error(err); alert('Erreur réseau'); });
}