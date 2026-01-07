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

    const params = action === 'send' ?
        'userId=' + userId :
        'requesterId=' + userId;

    fetch(endpoint, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: params
    })
        .then(response => {
            button.disabled = false;
            button.textContent = originalText;

            if (response.status === 200) {
                alert('Opération réussie! Page en cours de rechargement...');
                setTimeout(() => window.location.reload(), 500);
            } else if (response.status === 409) {
                alert('Cette demande existe déjà ou vous êtes déjà amis');
                button.disabled = false;
            } else if (response.status === 403) {
                alert('Cet utilisateur n\'a pas autorisé les demandes de connexion. Réessayez après qu\'il ait changé ses paramètres.');
                button.disabled = false;
            } else {
                alert('Une erreur s\'est produite. Veuillez réessayer.');
                button.disabled = false;
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            alert('Erreur de connexion. Veuillez réessayer.');
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
            displayReceivedRequests(requests);
            loadSentRequests();
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
            console.log('Sent requests loaded:', requests);
            displaySentRequests(requests);
        })
        .catch(error => console.error('Error loading sent requests:', error));
}

function displayReceivedRequests(requests) {
    const container = document.getElementById('received-requests-container');
    if (!container) return;

    if (requests.length === 0) {
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
                    <button class="btn btn-success btn-accept" data-id="${requesterId}">Accepter</button>
                    <button class="btn btn-danger btn-decline" data-id="${requesterId}">Refuser</button>
                </div>
            </div>
        `;
    });
    html += '</div>';

    container.innerHTML = html;

    // Attach event listeners to accept/decline buttons
    document.querySelectorAll('.btn-accept').forEach(btn => {
        btn.addEventListener('click', function () {
            const requesterId = this.getAttribute('data-id');
            acceptRequest(requesterId);
        });
    });

    document.querySelectorAll('.btn-decline').forEach(btn => {
        btn.addEventListener('click', function () {
            const requesterId = this.getAttribute('data-id');
            declineRequest(requesterId);
        });
    });
}

function displaySentRequests(requests) {
    const container = document.getElementById('sent-requests-container');
    if (!container) return;

    if (requests.length === 0) {
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
                    <strong>${receiverName}</strong>
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
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (response.status === 200) {
                return response.json();
            }
            return [];
        })
        .then(requests => {
            const badge = document.getElementById('notification-badge');
            if (badge) {
                const count = requests.length;
                if (count > 0) {
                    badge.textContent = count;
                    badge.style.display = 'inline-block';
                } else {
                    badge.style.display = 'none';
                }
            }
        })
        .catch(error => console.error('Error loading notification badge:', error));
}

// Load pending requests on page load
document.addEventListener('DOMContentLoaded', function () {
    loadPendingRequests();
    loadNotificationBadge();
});
