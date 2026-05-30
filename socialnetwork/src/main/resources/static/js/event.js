function byId(id) { return document.getElementById(id); }

var btnEditModal = document.querySelectorAll('.editEventModal');
var editModal = byId('editEventModal');
var closeEventEditModal = byId('closeEventEditModal');
var eventSave = byId('btn-event-submit');

var joinEvent = document.querySelectorAll('.btn-join-event');
var quitEvent = document.querySelectorAll('.btn-quit');

var eventRequestsModal = byId('eventRequestsModal');
var closeEventRequestsModal = byId('closeEventRequestsModal');

function openModal(elm) { 
	var currentEditId = byId('eventId');
	var eventName = byId('eventName');
	var eventDate = byId('eventDate');
	var eventCapacity = byId('eventCapacity');
	var eventLocation = byId('eventLocation');
	var eventVisibility = byId('eventVisibility');
	var eventDescription = byId('eventDescription');
	
	
	currentEditId.value = elm.currentTarget.getAttribute('data-id');
	eventName.value = elm.currentTarget.getAttribute('data-name');
	eventDate.value = elm.currentTarget.getAttribute('data-date');
	eventCapacity.value = elm.currentTarget.getAttribute('data-capacity');
	eventCapacity.min = elm.currentTarget.getAttribute('data-capacity');
	eventLocation.value = elm.currentTarget.getAttribute('data-location');
	eventDescription.value = elm.currentTarget.getAttribute('data-description');
	eventVisibility.value = elm.currentTarget.getAttribute('data-visibility');
	
	if (editModal){ 
		editModal.style.display = 'flex'; 
	}
}
function closeModal() { if (editModal) editModal.style.display = 'none'; currentEditId = null; }

function openEventRequestsModal() {
	if (eventRequestsModal) {
		eventRequestsModal.style.display = 'flex';
	}
}

function closeEventRequestsModalFn() {
	if (eventRequestsModal) {
		eventRequestsModal.style.display = 'none';
	}
}

if (closeEventEditModal) closeEventEditModal.addEventListener('click', function() { closeModal(); });

if (closeEventRequestsModal) {
	closeEventRequestsModal.addEventListener('click', closeEventRequestsModalFn);
}

btnEditModal.forEach(elm => {
  elm.addEventListener('click', openModal, elm);
});


joinEvent.forEach(elm => {
  elm.addEventListener('click', joinEventUser, elm);
});

quitEvent.forEach(elm => {
  elm.addEventListener('click', quitEventUser, elm);
});



if (eventSave) eventSave.addEventListener('click', function() { editEvent(); });

function escapeHtml(text) {
	if (text == null) return '';
	const div = document.createElement('div');
	div.textContent = text;
	return div.innerHTML;
}

function showEventAlert(message, type) {
	const alertDiv = document.createElement('div');
	alertDiv.className = 'alert alert-' + type + ' active';
	alertDiv.textContent = message;
	document.body.insertBefore(alertDiv, document.body.firstChild);
	setTimeout(function() { alertDiv.remove(); }, 3000);
}

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
			showEventAlert('Votre demande a bien été envoyée. Le créateur pourra l\'accepter ou la refuser.', 'success');
			window.location.reload(); 
		}
		else if (r.status === 403) { showEventAlert('Accès refusé', 'error'); }
		else { r.text().then(function(t) { console.error(t); showEventAlert('Erreur lors de l\'envoi de la demande', 'error'); }); }
	}).catch(function(err) { console.error(err); showEventAlert('Erreur réseau', 'error'); });
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
		else if (r.status === 403) { showEventAlert('Accès refusé', 'error'); }
		else { r.text().then(function(t) { console.error(t); showEventAlert(t, 'error'); }); }
	}).catch(function(err) { console.error(err); showEventAlert('Erreur réseau', 'error'); });
}

function quitEventUser(el) {
	let eventID = el.currentTarget.getAttribute('data-id');
	fetch('/eventattendee/' + eventID, {
		method: 'DELETE',
		headers: { 'Content-Type': 'application/json' },
	}).then(function(r) {
		if (r.ok) { closeModal(); window.location.reload(); }
		else if (r.status === 403) { showEventAlert('Accès refusé', 'error'); }
		else { r.text().then(function(t) { console.error(t); showEventAlert('Erreur lors de la sortie', 'error'); }); }
	}).catch(function(err) { console.error(err); showEventAlert('Erreur réseau', 'error'); });
}

function renderEventRequestItem(req) {
	const user = req.user || {};
	const name = escapeHtml((user.firstName || '') + ' ' + (user.lastName || ''));
	const requesterId = user.id;
	const eventId = req.event && req.event.id ? req.event.id : window.currentEventRequestsId;
	return `
		<div class="event-request-item">
			<div class="event-request-name">${name}</div>
			<div class="event-request-actions">
				<button type="button" class="btn-event-action btn-event-accept"
					data-requester-id="${requesterId}" data-event-id="${eventId}"
					onclick="acceptEventAttendeeFromBtn(this)">Accepter</button>
				<button type="button" class="btn-event-action btn-event-decline"
					data-requester-id="${requesterId}" data-event-id="${eventId}"
					onclick="declineEventAttendeeFromBtn(this)">Refuser</button>
			</div>
		</div>
	`;
}

async function showEventRequests(eventId) {
	try {
		window.currentEventRequestsId = eventId;
		const response = await fetch('/eventattendee/event/' + eventId + '/pending', {
			method: 'GET',
			headers: { 'Accept': 'application/json' }
		});

		const container = byId('eventRequestsList');
		if (!container) return;
		container.innerHTML = '';

		if (!response.ok) {
			if (response.status === 403) {
				container.innerHTML = '<p>Accès refusé. Seul le créateur peut voir les demandes.</p>';
			} else {
				container.innerHTML = '<p>Impossible de charger les demandes.</p>';
			}
			openEventRequestsModal();
			return;
		}

		const requests = await response.json();
		if (!requests || requests.length === 0) {
			container.innerHTML = '<p>Aucune demande en attente.</p>';
		} else {
			requests.forEach(function(req) {
				container.insertAdjacentHTML('beforeend', renderEventRequestItem(req));
			});
		}

		openEventRequestsModal();
	} catch (error) {
		console.error('Error loading event requests:', error);
		showEventAlert('Erreur lors du chargement des demandes', 'error');
	}
}

async function updateEventRequestsBadge(eventId) {
	const btn = byId('event-requests-btn-' + eventId);
	if (!btn) return;
	try {
		const response = await fetch('/eventattendee/event/' + eventId + '/pending', {
			method: 'GET',
			headers: { 'Accept': 'application/json' }
		});
		if (!response.ok) {
			btn.textContent = 'Demandes';
			btn.classList.remove('btn-event-requests-notice');
			return;
		}
		const requests = await response.json();
		const count = Array.isArray(requests) ? requests.length : 0;
		btn.textContent = count > 0 ? 'Demandes (' + count + ')' : 'Demandes';
		if (count > 0) {
			btn.classList.add('btn-event-requests-notice');
		} else {
			btn.classList.remove('btn-event-requests-notice');
		}
	} catch (e) {
		console.error('Error updating event requests badge:', e);
		btn.textContent = 'Demandes';
		btn.classList.remove('btn-event-requests-notice');
	}
}

async function acceptEventAttendee(requesterId, eventId) {
	const response = await fetch(
		'/eventattendee/accept?requesterId=' + encodeURIComponent(requesterId) + '&eventId=' + encodeURIComponent(eventId),
		{ method: 'PUT', headers: { 'Accept': 'application/json' } }
	);
	if (!response.ok) {
		const txt = await response.text();
		throw new Error(txt || 'Échec de l\'acceptation');
	}
}

async function declineEventAttendee(requesterId, eventId) {
	const response = await fetch(
		'/eventattendee/decline?requesterId=' + encodeURIComponent(requesterId) + '&eventId=' + encodeURIComponent(eventId),
		{ method: 'PUT', headers: { 'Accept': 'application/json' } }
	);
	if (!response.ok) {
		const txt = await response.text();
		throw new Error(txt || 'Échec du refus');
	}
}

function acceptEventAttendeeFromBtn(btn) {
	const requesterId = btn.getAttribute('data-requester-id');
	const eventId = btn.getAttribute('data-event-id');
	if (!requesterId || !eventId) return;
	acceptEventAttendee(requesterId, eventId)
		.then(function() {
			showEventAlert('Demande acceptée', 'success');
			closeEventRequestsModalFn();
			window.location.reload();
		})
		.catch(function(e) {
			console.error('Accept event request error:', e);
			showEventAlert('Erreur: ' + e.message, 'error');
		});
}

function declineEventAttendeeFromBtn(btn) {
	const requesterId = btn.getAttribute('data-requester-id');
	const eventId = btn.getAttribute('data-event-id');
	if (!requesterId || !eventId) return;
	declineEventAttendee(requesterId, eventId)
		.then(function() {
			showEventAlert('Demande refusée', 'success');
			closeEventRequestsModalFn();
			window.location.reload();
		})
		.catch(function(e) {
			console.error('Decline event request error:', e);
			showEventAlert('Erreur: ' + e.message, 'error');
		});
}

function deleteEventFromBtn(btn) {
	const eventId = btn.getAttribute('data-event-id');
	const eventName = btn.getAttribute('data-event-name') || 'cette annonce';
	if (!eventId) return;

	const message = 'Supprimer l\'annonce « ' + eventName + ' » ? Cette action est irréversible.';
	if (!confirm(message)) {
		return;
	}

	fetch('/event/' + eventId, {
		method: 'DELETE',
		headers: { 'Accept': 'application/json' }
	}).then(function(response) {
		if (response.ok || response.status === 204) {
			showEventAlert('Annonce supprimée', 'success');
			window.location.reload();
			return;
		}
		return response.text().then(function(txt) {
			throw new Error(txt || 'Échec de la suppression');
		});
	}).catch(function(err) {
		console.error('Delete event error:', err);
		showEventAlert('Erreur: ' + err.message, 'error');
	});
}

document.addEventListener('DOMContentLoaded', function() {
	document.querySelectorAll('.btn-event-requests').forEach(function(btn) {
		const eventId = btn.getAttribute('data-event-id');
		if (eventId) {
			updateEventRequestsBadge(eventId);
		}
	});
});
