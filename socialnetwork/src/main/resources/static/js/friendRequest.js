/**
 * Friend Request / Connection Management
 */

const friendRequestButtons = document.querySelectorAll(".btn-friend-request");

if (friendRequestButtons.length > 0) {
	friendRequestButtons.forEach(el => {
		el.addEventListener('click', handleFriendRequest);
	});
}

function handleFriendRequest(event) {
	const button = event.target;
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
                customAlert('Succès', 'Opération réussie! La page va se recharger...', 'success');
                setTimeout(() => window.location.reload(), 1500);
            } else if (response.status === 409) {
                customAlert('Information', 'Cette demande existe déjà ou vous êtes déjà amis', 'info');
                button.disabled = false;
            } else if (response.status === 403) {
                customAlert('Accès refusé', 'Cet utilisateur n\'autorise pas les demandes pour le moment.', 'error');
                button.disabled = false;
            } else {
                customAlert('Erreur', 'Une erreur s\'est produite. Veuillez réessayer.', 'error');
                button.disabled = false;
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            customAlert('Erreur', 'Erreur de connexion. Veuillez réessayer.', 'error');
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

document.addEventListener('DOMContentLoaded', function () {
    loadPendingRequests();
    loadNotificationBadge();
    markAcceptedFriends();
});
