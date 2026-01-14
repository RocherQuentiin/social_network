/**
 * PROJECT MESSAGING - Gestion des messages de groupe pour les projets
 * Gère les conversations de groupe liées aux projets
 */

// Global state for project messaging
let currentProjectGroupId = null;
let currentProjectGroupSubscription = null;
let projectStompClient = null;
let userProjects = [];

// Initialize project messaging when page loads
document.addEventListener('DOMContentLoaded', function() {
    if (typeof stompClient !== 'undefined' && stompClient !== null) {
        projectStompClient = stompClient;
    }
    loadUserProjects();
});

/**
 * Load all projects user is part of
 */
function loadUserProjects() {
    fetch('/api/projects/my-projects')
        .then(response => response.json())
        .then(projects => {
            userProjects = projects;
            loadProjectGroups();
        })
        .catch(error => {
            console.error('Error loading projects:', error);
        });
}

/**
 * Load all project message groups
 */
function loadProjectGroups() {
    const projectGroupsList = document.getElementById('projectGroupsList');
    projectGroupsList.innerHTML = '<div class="loading-spinner">Chargement...</div>';

    // Get all message groups for user's projects
    const groupPromises = userProjects.map(project => 
        fetch(`/api/project-messages/groups/${project.id}`)
            .then(response => response.json())
            .then(groups => groups.map(group => ({...group, projectName: project.name})))
    );

    Promise.all(groupPromises)
        .then(results => {
            const allGroups = results.flat();
            
            if (allGroups.length === 0) {
                projectGroupsList.innerHTML = `
                    <div class="empty-state">
                        <p>Aucun groupe de discussion</p>
                        <small>Les groupes apparaîtront ici une fois créés dans vos projets</small>
                    </div>`;
                return;
            }

            projectGroupsList.innerHTML = '';
            allGroups.forEach(group => {
                const groupItem = createProjectGroupItem(group);
                projectGroupsList.appendChild(groupItem);
            });
        })
        .catch(error => {
            console.error('Error loading project groups:', error);
            projectGroupsList.innerHTML = '<div class="error-message">Erreur de chargement</div>';
        });
}

/**
 * Create a project group list item
 */
function createProjectGroupItem(group) {
    const div = document.createElement('div');
    div.className = 'conversation-item project-group';
    div.dataset.groupId = group.id;
    
    div.innerHTML = `
        <div class="conversation-avatar">
            <i class="fas fa-users" style="font-size: 20px; color: #10b981;"></i>
        </div>
        <div class="conversation-info">
            <div class="conversation-header">
                <span class="conversation-name">${escapeHtml(group.name)}</span>
            </div>
            <div class="conversation-preview">
                <small style="color: var(--text-secondary);">${escapeHtml(group.projectName)}</small>
            </div>
        </div>
    `;

    div.addEventListener('click', () => selectProjectGroup(group));
    return div;
}

/**
 * Select a project group and load its messages
 */
function selectProjectGroup(group) {
    // Remove active class from all items
    document.querySelectorAll('.conversation-item').forEach(item => {
        item.classList.remove('active');
    });

    // Add active class to selected item
    const selectedItem = document.querySelector(`[data-group-id="${group.id}"]`);
    if (selectedItem) {
        selectedItem.classList.add('active');
    }

    currentProjectGroupId = group.id;

    // Show chat window
    document.getElementById('noChatSelected').style.display = 'none';
    document.getElementById('chatWindow').style.display = 'flex';

    // Update chat header
    document.getElementById('chatHeaderName').textContent = group.name;
    document.getElementById('chatStatus').textContent = group.projectName;
    document.getElementById('otherUserAvatar').style.display = 'none';
    
    // Show project badge
    const chatTypeBadge = document.getElementById('chatType');
    chatTypeBadge.textContent = 'Projet';
    chatTypeBadge.className = 'chat-type-badge project';
    chatTypeBadge.style.display = 'inline-block';

    // Load messages
    loadProjectGroupMessages(group.id);

    // Subscribe to real-time updates
    subscribeToProjectGroup(group.id);
}

/**
 * Load messages for a project group
 */
function loadProjectGroupMessages(groupId) {
    const messagesContainer = document.getElementById('messagesContainer');
    messagesContainer.innerHTML = '<div class="loading-spinner">Chargement des messages...</div>';

    fetch(`/api/project-messages/${groupId}`)
        .then(response => response.json())
        .then(messages => {
            messagesContainer.innerHTML = '';
            
            if (messages.length === 0) {
                messagesContainer.innerHTML = `
                    <div class="empty-state">
                        <p>Aucun message</p>
                        <small>Soyez le premier à écrire dans ce groupe !</small>
                    </div>`;
                return;
            }

            messages.forEach(message => {
                displayProjectMessage(message);
            });

            // Scroll to bottom
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        })
        .catch(error => {
            console.error('Error loading messages:', error);
            messagesContainer.innerHTML = '<div class="error-message">Erreur de chargement</div>';
        });
}

/**
 * Subscribe to real-time project group messages
 */
function subscribeToProjectGroup(groupId) {
    if (!projectStompClient || !projectStompClient.connected) {
        console.warn('WebSocket not connected');
        return;
    }

    // Unsubscribe from previous group
    if (currentProjectGroupSubscription) {
        currentProjectGroupSubscription.unsubscribe();
    }

    // Subscribe to new group
    currentProjectGroupSubscription = projectStompClient.subscribe(
        `/topic/project-group/${groupId}`,
        (message) => {
            const msg = JSON.parse(message.body);
            displayProjectMessage(msg);
        }
    );
}

/**
 * Display a project message in the chat
 */
function displayProjectMessage(message) {
    const messagesContainer = document.getElementById('messagesContainer');
    const currentUserId = document.getElementById('currentUserId').value;
    
    // Check for duplicates
    if (document.querySelector(`[data-message-id="${message.id}"]`)) {
        return;
    }

    const isOwnMessage = message.senderId === currentUserId;
    
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${isOwnMessage ? 'sent' : 'received'}`;
    messageDiv.dataset.messageId = message.id;

    const timestamp = message.timestamp ? new Date(message.timestamp).toLocaleTimeString('fr-FR', {
        hour: '2-digit',
        minute: '2-digit'
    }) : '';

    messageDiv.innerHTML = `
        ${!isOwnMessage ? `
            <div class="message-avatar">
                <img src="${message.senderAvatar || '/img/default-avatar.png'}" alt="${escapeHtml(message.senderName)}">
            </div>
        ` : ''}
        <div class="message-content">
            ${!isOwnMessage ? `<div class="message-sender">${escapeHtml(message.senderName)}</div>` : ''}
            <div class="message-bubble">
                <p>${escapeHtml(message.content)}</p>
                ${timestamp ? `<span class="message-time">${timestamp}</span>` : ''}
            </div>
        </div>
    `;

    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

/**
 * Send a project message
 */
function sendProjectMessage(content) {
    if (!content.trim() || !currentProjectGroupId) {
        return;
    }

    const currentUserId = document.getElementById('currentUserId').value;

    // Send via REST API
    fetch(`/api/project-messages/send/${currentProjectGroupId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
            content: content
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to send message');
        }
        return response.json();
    })
    .then(message => {
        // Broadcast via WebSocket
        if (projectStompClient && projectStompClient.connected) {
            projectStompClient.send(
                `/app/send-project-message/${currentProjectGroupId}`,
                {},
                JSON.stringify(message)
            );
        }

        // Clear input
        document.getElementById('messageInput').value = '';
    })
    .catch(error => {
        console.error('Error sending message:', error);
        alert('Erreur lors de l\'envoi du message');
    });
}

/**
 * Check if we're in project mode
 */
function isProjectMode() {
    const projectsTab = document.getElementById('projectsTab');
    return projectsTab && projectsTab.classList.contains('active');
}

// Expose functions globally
window.loadProjectGroups = loadProjectGroups;
window.sendProjectMessage = sendProjectMessage;
window.isProjectMode = isProjectMode;
