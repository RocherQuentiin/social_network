/**
 * Friend Request / Connection Management
 */

const friendRequestButtons = document.querySelectorAll(".btn-friend-request");

if (friendRequestButtons.length > 0) {
	friendRequestButtons.forEach(el => {
		el.addEventListener('click', handleFriendRequest);
	});
}

function showAlert(message, type = 'info') {
    // Create a simple alert - you can replace this with a toast notification
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} active`;
    alertDiv.textContent = message;
    
    document.body.insertBefore(alertDiv, document.body.firstChild);
    
    setTimeout(() => {
        alertDiv.remove();
    }, 3000);
}

function handleFriendRequest(event) {

	const button = event.target == undefined ? event : event.target;
	const userId = button.getAttribute('data-id');
	const action = button.getAttribute('data-action') || 'send';

	// Désactiver le bouton pendant la requête
	button.disabled = true;
	const originalText = button.textContent;
	button.textContent = 'En attente...';

	const endpoint =
		action === 'accept' ? '/friend-request/accept' :
			action === 'decline' ? '/friend-request/decline' :
				'/friend-request/send';

	const params = action === 'send' ? 'userId=' + userId : 'requesterId=' + userId;

	fetch(endpoint, {
		method: 'POST',
		headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
		body: params
	})
		.then(response => {
			button.disabled = false;
			button.textContent = originalText;

			if (response.status === 200) {
				showAlert('Opération réussie! La page va se recharger...', 'success');
				setTimeout(() => window.location.reload(), 1500);
			} else if (response.status === 409) {
				showAlert('Cette demande existe déjà ou vous êtes déjà amis', 'info');
				button.disabled = false;
			} else if (response.status === 403) {
				showAlert('Cet utilisateur n\'autorise pas les demandes pour le moment.', 'error');
				button.disabled = false;
			} else {
				showAlert('Une erreur s\'est produite. Veuillez réessayer.', 'error');
				button.disabled = false;
			}
		})
		.catch(error => {
			console.error('Erreur:', error);
			showAlert('Erreur de connexion. Veuillez réessayer.', 'error');
			button.disabled = false;
			button.textContent = originalText;
		});
}

/**
 * Load and display pending friend requests (received)
 */
function loadPendingRequests() {
	fetch('/friend-request/pending', {
		method: 'GET',
		headers: {
			'Content-Type': 'application/json'
		}
	})
		.then(response => {
			if (response.status === 200) {
				return response.json();
			} else {
				console.error('Failed to load pending requests');
				return [];
			}
		})
		.then(requests => {
			fetch('/eventattendee/pending', {
				method: 'GET',
				headers: {
					'Content-Type': 'application/json'
				}
			})
				.then(response => {
					if (response.status === 200) {
						return response.json();
					} else {
						console.error('Failed to load pending requests');
						return [];
					}
				})
				.then(requestsEvent => {
					displayReceivedRequests(requests, requestsEvent);
					loadSentRequests();
				});

		})
		.catch(error => console.error('Error loading pending requests:', error));
}

/**
 * Load and display sent friend requests (waiting for acceptance)
 */
function loadSentRequests() {
	fetch('/friend-request/sent', {
		method: 'GET',
		headers: {
			'Content-Type': 'application/json'
		}
	})
		.then(response => {
			if (response.status === 200) {
				return response.json();
			} else {
				console.error('Failed to load sent requests, status:', response.status);
				return [];
			}
		})
		.then(requests => {
			fetch('/eventattendee/sent', {
				method: 'GET',
				headers: {
					'Content-Type': 'application/json'
				}
			})
				.then(response => {
					if (response.status === 200) {
						return response.json();
					} else {
						console.error('Failed to load sent requests, status:', response.status);
						return [];
					}
				})
				.then(requestsEvent => {
					console.log('Sent requests loaded:', requests);
					markSentButtons(requests);
					displaySentRequests(requests, requestsEvent);
				})
		})
		.catch(error => console.error('Error loading sent requests:', error));
}

function showAlert(message, type = 'info') {
    // Create a simple alert - you can replace this with a toast notification
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} active`;
    alertDiv.textContent = message;
    
    document.body.insertBefore(alertDiv, document.body.firstChild);
    
    setTimeout(() => {
        alertDiv.remove();
    }, 3000);
}

function markSentButtons(requests) {
	if (!requests || requests.length === 0) return;
	const btns = document.querySelectorAll('.btn-friend-request');
	if (!btns || btns.length === 0) return;

	const sentIds = new Set(requests
		.map(r => r?.receiver?.id || r?.receiverId)
		.filter(Boolean)
	);

	btns.forEach(btn => {
		const uid = btn.getAttribute('data-id');
		const action = btn.getAttribute('data-action') || 'send';
		if (action === 'send' && sentIds.has(uid)) {
			btn.classList.add('pending');
			btn.textContent = 'En attente';
			btn.disabled = true;
		}
	});
}

function displayReceivedRequests(requests, requestsEvent) {
	const container = document.getElementById('received-requests-container');
	if (!container) return;

	if (requests.length === 0 && requestsEvent.length == 0) {
		container.innerHTML = '<p>Pas de demandes reçues.</p>';
		return;
	}

	let html = '<div class="pending-requests-list">';
	html += '<h3>Demandes reçues</h3>';
	requests.forEach(req => {
		const requesterName = req.requester.firstName + ' ' + req.requester.lastName;
		const requesterId = req.requester.id;

		html += `
            <div class="pending-request-item">
                <div class="request-info">
                    <strong>${requesterName}</strong>
                    <p>Envoyée le ${new Date(req.createdAt).toLocaleDateString()}</p>
                </div>
                <div class="request-actions">
                    <button class="btn btn-success btn-accept" data-id="${req.requester.id}">Accepter</button>
                    <button class="btn btn-danger btn-decline" data-id="${req.requester.id}">Refuser</button>
                </div>
            </div>
        `;
	});

	requestsEvent.forEach(req => {
		const eventId = req.event.id;
		const eventName = req.event.name;
		const requesterName = req.user.firstName + ' ' + req.user.lastName;
		const requesterId = req.user.id;

		html += `
            <div class="pending-request-item">
                <div class="request-info">
                    <strong>${requesterName} souhaite participer à l'événement : ${eventName}</strong>
                    <p>Envoyée le ${new Date(req.createdAt).toLocaleDateString()}</p>
                </div>
                <div class="request-actions">
                    <button class="btn btn-success btn-accept-event" data-event-id="${eventId}" data-user-id="${requesterId}">Accepter</button>
                    <button class="btn btn-danger btn-decline-event" data-event-id="${eventId}" data-user-id="${requesterId}">Refuser</button>
                </div>
            </div>
        `;
	});
	html += '</div>';

	container.innerHTML = html;

	// Attach event listeners to accept/decline buttons
	document.querySelectorAll('.btn-accept').forEach(btn => {
		btn.addEventListener('click', function() {
			const requesterId = this.getAttribute('data-id');
			acceptRequest(requesterId);
		});
	});

	document.querySelectorAll('.btn-decline').forEach(btn => {
		btn.addEventListener('click', function() {
			const requesterId = this.getAttribute('data-id');
			declineRequest(requesterId);
		});
	});

	// Attach event listeners to accept/decline buttons
	document.querySelectorAll('.btn-accept-event').forEach(btn => {
		btn.addEventListener('click', function() {
			const requesterId = this.getAttribute('data-user-id');
			const eventId = this.getAttribute('data-event-id');
			acceptEventRequest(requesterId, eventId);
		});
	});

	document.querySelectorAll('.btn-decline-event').forEach(btn => {
		btn.addEventListener('click', function() {
			const requesterId = this.getAttribute('data-user-id');
			const eventId = this.getAttribute('data-event-id');
			declineEventRequest(requesterId, eventId);
		});
	});
}

function displaySentRequests(requests, requestsEvent) {
	const container = document.getElementById('sent-requests-container');
	if (!container) return;

	if (requests.length === 0 && requestsEvent.length == 0) {
		container.innerHTML = '<p>Pas de demandes en attente.</p>';
		return;
	}

	let html = '<div class="pending-requests-list sent-list">';
	html += '<h3>Demandes envoyées (en attente)</h3>';
	requests.forEach(req => {
		const receiverName = req.receiver.firstName + ' ' + req.receiver.lastName;
		const receiverId = req.receiver.id;

		html += `
            <div class="pending-request-item sent-item">
                <div class="request-info">
                    <strong class="status-pending">${receiverName}</strong>
                    <p class="status-pending">En attente d'acceptation depuis le ${new Date(req.createdAt).toLocaleDateString()}</p>
                </div>
            </div>
        `;
	});
	requestsEvent.forEach(req => {
		const eventName = req.event.name;

		html += `
            <div class="pending-request-item sent-item">
                <div class="request-info">
                    <strong class="status-pending">Événement : ${eventName}</strong>
                    <p class="status-pending">En attente d'acceptation depuis le ${new Date(req.createdAt).toLocaleDateString()}</p>
                </div>
            </div>
        `;
	});
	html += '</div>';

	container.innerHTML = html;
}

function acceptRequest(requesterId) {
	fetch('/friend-request/accept', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		body: 'requesterId=' + requesterId
	})
		.then(response => {
			if (response.status === 200) {
				alert('Friend request accepted!');
				loadPendingRequests();
			} else {
				alert('Failed to accept request');
			}
		})
		.catch(error => console.error('Error:', error));
}

function acceptEventRequest(requesterId, eventId) {
	var payload = {
		requesterId: requesterId,
		eventId: eventId,
	};
	fetch('/eventattendee/accept', {
		method: 'PUT',
		headers: {
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		body: 'requesterId=' + requesterId + '&eventId=' + eventId
	})
		.then(response => {
			if (response.status === 200) {
				alert('Event join accepted!');
				loadPendingRequests();
			} else {
				alert('Failed to accept request');
			}
		})
		.catch(error => console.error('Error:', error));
}

function declineEventRequest(requesterId, eventId) {
	var payload = {
		requesterId: requesterId,
		eventId: eventId,
	};
	fetch('/eventattendee/decline', {
		method: 'PUT',
		headers: {
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		body: 'requesterId=' + requesterId + '&eventId=' + eventId
	})
		.then(response => {
			if (response.status === 200) {
				alert('Event join declined!');
				loadPendingRequests();
			} else {
				alert('Failed to declined request');
			}
		})
		.catch(error => console.error('Error:', error));
}

function declineRequest(requesterId) {
	fetch('/friend-request/decline', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		body: 'requesterId=' + requesterId
	})
		.then(response => {
			if (response.status === 200) {
				alert('Friend request declined!');
				loadPendingRequests();
			} else {
				alert('Failed to decline request');
			}
		})
		.catch(error => console.error('Error:', error));
}

/**
 * Show notification badge count (count only received requests)
 */
function loadNotificationBadge() {
	fetch('/friend-request/pending', {
		method: 'GET',
		headers: { 'Content-Type': 'application/json' }
	})
		.then(response => response.status === 200 ? response.json() : [])
		.then(requests => {
			const badge = document.getElementById('notification-badge');
			if (badge) {
				badge.textContent = requests.length;
				badge.style.display = requests.length > 0 ? 'inline-block' : 'none';
			}
		})
		.catch(error => console.error('Error loading badge:', error));
}

function markAcceptedFriends() {
	fetch('/friend-request/accepted-ids')
		.then(response => response.status === 200 ? response.json() : [])
		.then(friendIds => {
			const btns = document.querySelectorAll('.btn-friend-request');
			const friendSet = new Set(friendIds);
			btns.forEach(btn => {
				const uid = btn.getAttribute('data-id');
				if (friendSet.has(uid)) {
					btn.outerHTML = `<span class="badge-friends"><i data-lucide="check"></i> Amis</span>`;
				}
			});
			if (window.lucide) lucide.createIcons();
		})
		.catch(err => console.error('Erreur lors du chargement des amis:', err));
}

function displaySuggestionRequests(requests) {
	const container = document.getElementById('suggestions-container');
	if (!container) return;

	if (Object.keys(requests).length == 0) {
		container.innerHTML = '<p>Pas de suggestions.</p>';
		return;
	}

	let html = '<div class="pending-requests-list sent-list">';
	html += '<h3>Suggestions amis</h3>';
	
	
	Object.entries(requests).forEach(([key, value]) => {
		var tabKey = key.split(" ");
		var id = tabKey[0];
		var requesterName = tabKey[1] + " " + tabKey[2];

		html += `
            <div class="pending-request-item">
                <div class="request-info">
                    <strong>${requesterName}</strong>
                </div>
                <div class="request-actions">
                    <button class="btn btn-success btn-friend-request" data-id="${id}" data-action="send">Demande d'amis</button>
                    <button class="btn btn-primary-header view-profile" data-id="${id}">Voir profil</button>
                    <button class="btn btn-primary-header question" data-reason="${value}">?</button>
                </div>
            </div>
        `;
        
        
    });
	
	html += '</div>';

	container.innerHTML = html;
	
	// Attach event listeners to accept/decline buttons
	document.querySelectorAll('.btn-friend-request').forEach(btn => {
		btn.addEventListener('click', function() {
			console.log(btn)
			handleFriendRequest(btn);
		});
	});

	document.querySelectorAll('.view-profile').forEach(btn => {
		btn.addEventListener('click', function() {
		console.log(btn)
			const requesterId = this.getAttribute('data-id');
			window.location.href = "/profil/" + requesterId;
		});
	});
	
	document.querySelectorAll('.question').forEach(btn => {
		btn.addEventListener('click', function() {
			const reason = this.getAttribute('data-reason');
			DisplayReasonModal(reason);
		});
	});
}

function DisplayReasonModal(reason){
	document.getElementById("editEventModal").style.display = "flex";
	document.getElementById("whysuggestion").innerText = reason;
}

document.getElementById("closeEditModal").addEventListener('click',closeModal);
function closeModal(){  document.getElementById("editEventModal").style.display = 'none'; }

function loadSuggestion() {
	fetch('/suggestion', {
		method: 'GET',
		headers: {
			'Content-Type': 'application/json'
		}
	})
		.then(response => {
			if (response.status === 200) {
				return response.json();
			} else {
				console.error('Failed to load pending requests');
				return [];
			}
		})
		.then(requests => {
			displaySuggestionRequests(requests);
		})
		.catch(error => console.error('Error loading pending requests:', error));

}

document.addEventListener('DOMContentLoaded', function() {
	loadPendingRequests();
	loadSuggestion();
	loadNotificationBadge();
	markAcceptedFriends();
});
