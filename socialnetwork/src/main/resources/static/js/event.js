function byId(id) { return document.getElementById(id); }

var btnEditModal = byId('btn-edit');
var editModal = byId('editEventModal');
var closeEventEditModal = byId('closeEventEditModal');
var eventSave = byId('btn-event-submit');
var joinEvent = byId('btn-join-event');
var quitEvent = byId('btn-quit-event');

function openModal() { if (editModal) editModal.style.display = 'flex'; }
function closeModal() { if (editModal) editModal.style.display = 'none'; currentEditId = null; }

if (btnEditModal) btnEditModal.addEventListener('click', function() { openModal(); });
if (closeEventEditModal) closeEventEditModal.addEventListener('click', function() { closeModal(); });

if (eventSave) eventSave.addEventListener('click', function() { editEvent(); });

if (joinEvent) joinEvent.addEventListener('click', function(e) { joinEventUser(e); });

if (quitEvent) quitEvent.addEventListener('click', function(e) { quitEventUser(e); });

function joinEventUser(el) {
	let eventID = el.currentTarget.getAttribute('data-id');

	var payload = {
		eventID: eventID,
	};
	fetch('/eventattendee', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(payload)
	}).then(function(r) {
		if (r.ok) {  
			alert('Votre demande pour rejoindre événement a bien été envoyé, vous recevrez une notification si elle sera accepté');
			window.location.reload(); 
		}
		else if (r.status === 403) { alert('Accès refusé'); }
		else { r.text().then(function(t) { console.error(t); alert('Erreur lors de la sauvegarde'); }); }
	}).catch(function(err) { console.error(err); alert('Erreur réseau'); });
}

function editEvent() {
	var currentEditId = byId('eventId');
	var eventName = byId('eventName');
	var eventDate = byId('eventDate');
	var eventCapacity = byId('eventCapacity');
	var eventLocation = byId('eventLocation');
	var eventVisibility = byId('eventVisibility');
	var eventDescription = byId('eventDescription');
	console.log(eventDate.value.toString())
	var payload = {
		eventName: eventName ? eventName.value : '',
		eventVisibility: eventVisibility ? eventVisibility.value : 'PUBLIC',
		eventDate: eventDate ? eventDate.value.toString() : '',
		eventCapacity: eventCapacity ? eventCapacity.value : '',
		eventLocation: eventLocation ? eventLocation.value : 'PUBLIC',
		eventDescription: eventDescription ? eventDescription.value : ''
	};
	fetch('/event/' + currentEditId.value, {
		method: 'PUT',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(payload)
	}).then(function(r) {
		if (r.ok) { closeModal(); window.location.reload(); }
		else if (r.status === 403) { alert('Accès refusé'); }
		else { r.text().then(function(t) { console.error(t); alert('Erreur lors de la sauvegarde'); }); }
	}).catch(function(err) { console.error(err); alert('Erreur réseau'); });
}

function quitEventUser(el) {
	let eventID = el.currentTarget.getAttribute('data-id');
	fetch('/eventattendee/' + eventID, {
		method: 'DELETE',
		headers: { 'Content-Type': 'application/json' },
	}).then(function(r) {
		if (r.ok) { closeModal(); window.location.reload(); }
		else if (r.status === 403) { alert('Accès refusé'); }
		else { r.text().then(function(t) { console.error(t); alert('Erreur lors de la sauvegarde'); }); }
	}).catch(function(err) { console.error(err); alert('Erreur réseau'); });
}
