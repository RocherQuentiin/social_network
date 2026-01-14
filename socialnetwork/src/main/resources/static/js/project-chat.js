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
    
    // Setup create group button
    const createGroupBtn = document.getElementById('createGroupBtn');
    if (createGroupBtn) {
        createGroupBtn.addEventListener('click', openCreateGroupModal);
    }
    
    const closeGroupModalBtn = document.getElementById('closeGroupModalBtn');
    if (closeGroupModalBtn) {
        closeGroupModalBtn.addEventListener('click', closeCreateGroupModal);
    }
    
    const confirmCreateGroupBtn = document.getElementById('confirmCreateGroupBtn');
    if (confirmCreateGroupBtn) {
        confirmCreateGroupBtn.addEventListener('click', createProjectGroup);
    }
    
    // Setup project selector filter
    const projectSelector = document.getElementById('projectSelector');
    if (projectSelector) {
        projectSelector.addEventListener('change', filterGroupsByProject);
    }
});

/**
 * Open create group modal
 */
function openCreateGroupModal() {
    const modal = document.getElementById('createGroupModal');
    const projectSelect = document.getElementById('groupProjectSelect');
    
    // Populate project dropdown
    projectSelect.innerHTML = '<option value="">Sélectionner un projet</option>';
    userProjects.forEach(project => {
        const option = document.createElement('option');
        option.value = project.id;
        option.textContent = project.name;
        projectSelect.appendChild(option);
    });
    
    modal.style.display = 'flex';
    modal.style.visibility = 'visible';
    modal.style.opacity = '1';
    document.body.style.overflow = 'hidden';
}

/**
 * Close create group modal
 */
function closeCreateGroupModal() {
    const modal = document.getElementById('createGroupModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
    
    // Clear form
    document.getElementById('groupProjectSelect').value = '';
    document.getElementById('groupName').value = '';
    document.getElementById('groupDescription').value = '';
}

/**
 * Create a new project message group
 */
function createProjectGroup() {
    const projectId = document.getElementById('groupProjectSelect').value;
    const name = document.getElementById('groupName').value.trim();
    const description = document.getElementById('groupDescription').value.trim();
    
    if (!projectId || !name) {
        alert('Veuillez sélectionner un projet et entrer un nom pour le groupe');
        return;
    }
    
    const formData = new URLSearchParams();
    formData.append('projectId', projectId);
    formData.append('name', name);
    if (description) {
        formData.append('description', description);
    }
    
    fetch('/api/project-messages/groups', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to create group');
        }
        return response.json();
    })
    .then(group => {
        closeCreateGroupModal();
        loadProjectGroups(); // Reload the list
        alert('Groupe créé avec succès!');
    })
    .catch(error => {
        console.error('Error creating group:', error);
        alert('Erreur lors de la création du groupe');
    });
}

/**
 * Filter groups by selected project
 */
function filterGroupsByProject() {
    const selectedProjectId = document.getElementById('projectSelector').value;
    const groupItems = document.querySelectorAll('.conversation-item.project-group');
    
    groupItems.forEach(item => {
        if (!selectedProjectId) {
            item.style.display = 'flex';
        } else {
            const groupProjectId = item.dataset.projectId;
            item.style.display = groupProjectId === selectedProjectId ? 'flex' : 'none';
        }
    });
}


/**
 * Load all projects user is part of
 */
function loadUserProjects() {
    fetch('/api/project/my-projects')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load projects');
            }
            return response.json();
        })
        .then(projects => {
            // Ensure projects is an array
            userProjects = Array.isArray(projects) ? projects : [];
            loadProjectGroups();
            populateProjectSelector();
        })
        .catch(error => {
            console.error('Error loading projects:', error);
            userProjects = [];
            loadProjectGroups();
        });
}

/**
 * Populate project selector dropdown
 */
function populateProjectSelector() {
    const projectSelector = document.getElementById('projectSelector');
    if (!projectSelector) return;
    
    projectSelector.innerHTML = '<option value="">Tous les projets</option>';
    userProjects.forEach(project => {
        const option = document.createElement('option');
        option.value = project.id;
        option.textContent = project.name;
        projectSelector.appendChild(option);
    });
}

/**
 * Load all project message groups
 */
function loadProjectGroups() {
    const projectGroupsList = document.getElementById('projectGroupsList');
    projectGroupsList.innerHTML = '<div class="loading-spinner">Chargement...</div>';

    // Check if userProjects is valid
    if (!Array.isArray(userProjects) || userProjects.length === 0) {
        projectGroupsList.innerHTML = `
            <div class="empty-state">
                <p>Aucun projet</p>
                <small>Vous n'êtes membre d'aucun projet</small>
            </div>`;
        return;
    }

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
                        <small>Créez un groupe pour commencer à discuter dans vos projets</small>
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
    div.dataset.projectId = group.projectId;
    
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
                
                // Mark unread messages as read
                if (!message.isRead) {
                    markProjectMessageAsRead(message.id);
                }
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

    const avatarHtml = !isOwnMessage ? `<div class="message-avatar"><img src="${message.senderAvatar || '/img/default-avatar.png'}" alt="${escapeHtml(message.senderName)}"></div>` : '';
    const senderHtml = !isOwnMessage ? `<div class="message-sender">${escapeHtml(message.senderName)}</div>` : '';
    const timeHtml = timestamp ? `<span class="message-time">${timestamp}</span>` : '';

    messageDiv.innerHTML = `${avatarHtml}<div class="message-content">${senderHtml}<div class="message-bubble"><p>${escapeHtml(message.content)}</p>${timeHtml}</div></div>`;

    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
    
    // Update unread badge if message is from another user
    if (!isOwnMessage && typeof updateUnreadMessagesBadge === 'function') {
        updateUnreadMessagesBadge();
    }
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
 * Mark a project message as read
 */
function markProjectMessageAsRead(messageId) {
    fetch(`/api/project-messages/${messageId}/read`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        }
    })
    .then(response => {
        if (response.ok) {
            // Update unread badge
            if (typeof updateUnreadMessagesBadge === 'function') {
                updateUnreadMessagesBadge();
            }
        }
    })
    .catch(error => {
        console.warn('Error marking message as read:', error);
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
window.markProjectMessageAsRead = markProjectMessageAsRead;
