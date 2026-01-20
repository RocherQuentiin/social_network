let stompClient = null;
let notificationClient = null;
let projectMessageClient = null;
let currentConversationId = null;
let currentOtherId = null;
let currentUserId = null;
let currentConversationSubscription = null; // Track the current subscription

// Initialize WebSocket connection
function initializeWebSocket() {
    const socket = new SockJS('/ws-messaging');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Disable debug logging
    
    stompClient.connect({}, function(frame) {
        console.log('Messaging WebSocket Connected: ' + frame.version);
    }, function(error) {
        console.error('Messaging WebSocket error:', error);
    });
}

// Initialize notification WebSocket
function initializeNotificationWebSocket() {
    const socket = new SockJS('/ws-notifications');
    notificationClient = Stomp.over(socket);
    notificationClient.debug = null; // Disable debug logging
    
    notificationClient.connect({}, function(frame) {
        console.log('Notification WebSocket Connected');
        
        // Subscribe to user's notifications
        notificationClient.subscribe('/topic/user/' + currentUserId, function(message) {
            const notification = JSON.parse(message.body);
            displayNotification(notification);
            updateNotificationCount();
        });
    }, function(error) {
        console.error('Notification WebSocket error:', error);
    });
}

// Load conversations
async function loadConversations() {
    try {
        const response = await fetch('/api/messages/conversations');
        const conversations = await response.json();
        
        const conversationsList = document.getElementById('conversationsList');
        conversationsList.innerHTML = '';
        
        conversations.forEach(conv => {
            const item = createConversationItem(conv);
            conversationsList.appendChild(item);
        });
    } catch (error) {
        console.error('Error loading conversations:', error);
    }
}

function createConversationItem(conversation) {
    const item = document.createElement('div');
    item.className = 'conversation-item';
    item.dataset.conversationId = conversation.conversationId;
    item.onclick = () => selectConversation(conversation);
    
    const preview = conversation.lastMessage ? conversation.lastMessage.substring(0, 50) + '...' : 'Aucun message';
    
    item.innerHTML = `
        <div class="conversation-header">
            <img src="${conversation.otherUserAvatar || '/img/default-avatar.png'}" alt="" class="conversation-avatar">
            <div class="conversation-info">
                <div class="conversation-name">${conversation.otherUserName}</div>
                <div class="conversation-preview">${preview}</div>
            </div>
        </div>
        <div class="conversation-meta">${formatTime(conversation.updatedAt)}</div>
    `;
    
    return item;
}

function selectConversation(conversation) {
    try {
        currentConversationId = conversation.conversationId;
        currentOtherId = conversation.otherUserId;
        
        // Unsubscribe from previous conversation
        if (currentConversationSubscription) {
            currentConversationSubscription.unsubscribe();
            currentConversationSubscription = null;
        }
        
        // Update active state
        document.querySelectorAll('.conversation-item').forEach(item => {
            item.classList.remove('active');
        });
        
        // Find and mark the clicked item as active
        const conversationItems = document.querySelectorAll('.conversation-item');
        conversationItems.forEach(item => {
            if (item.dataset.conversationId === conversation.conversationId) {
                item.classList.add('active');
            }
        });
        
        // Show chat window
        document.getElementById('noChatSelected').style.display = 'none';
        document.getElementById('chatWindow').style.display = 'flex';
        
        // Mobile: Add mobile-active class to show chat area
        const chatArea = document.querySelector('.chat-area');
        if (chatArea && window.innerWidth <= 480) {
            chatArea.classList.add('mobile-active');
        }
        
        // Update header
        document.getElementById('chatHeaderName').textContent = conversation.otherUserName;
        document.getElementById('otherUserAvatar').src = conversation.otherUserAvatar || '/img/default-avatar.png';
        
        // Load messages
        loadMessages(conversation.conversationId);
        
        // Subscribe to WebSocket for new messages
        if (stompClient && stompClient.connected) {
            currentConversationSubscription = stompClient.subscribe('/topic/conversation/' + currentConversationId, function(message) {
                const msg = JSON.parse(message.body);
                displayMessage(msg, true);
                // Update badge when receiving new message
                if (typeof updateUnreadMessagesBadge === 'function') {
                    updateUnreadMessagesBadge();
                }
            });
        }
    } catch (error) {
        console.error('Error selecting conversation:', error);
    }
}

async function loadMessages(conversationId) {
    try {
        const response = await fetch(`/api/messages/${conversationId}`);
        const messages = await response.json();
        
        const container = document.getElementById('messagesContainer');
        container.innerHTML = '';
        
        messages.reverse().forEach(msg => {
            displayMessage(msg, false);
        });
        
        // Scroll to bottom
        container.scrollTop = container.scrollHeight;
    } catch (error) {
        console.error('Error loading messages:', error);
    }
}

function displayMessage(message, isNew = false) {
    const container = document.getElementById('messagesContainer');
    
    // Check if message already exists (prevent duplicates)
    if (message.id) {
        const existingMessage = container.querySelector(`[data-message-id="${message.id}"]`);
        if (existingMessage) {
            console.log('Message already displayed:', message.id);
            return;
        }
    }
    
    const messageDiv = document.createElement('div');
    messageDiv.setAttribute('data-message-id', message.id); // Add ID for duplicate check
    
    const isSent = message.senderId === currentUserId;
    messageDiv.className = `message ${isSent ? 'sent' : 'received'}`;
    
    // Handle timestamp - use current time if not provided or invalid
    let timestamp = message.timestamp || Date.now();
    if (typeof timestamp === 'string') {
        timestamp = new Date(timestamp).getTime();
    }
    
    const time = new Date(timestamp).toLocaleTimeString('fr-FR', { 
        hour: '2-digit', 
        minute: '2-digit' 
    });
    
    messageDiv.innerHTML = `
        ${!isSent ? `<img src="${message.senderAvatar || '/img/default-avatar.png'}" alt="" class="message-avatar">` : ''}
        <div>
            <div class="message-bubble">${escapeHtml(message.content || '')}</div>
            <div class="message-time">${time}</div>
        </div>
    `;
    
    container.appendChild(messageDiv);
    
    // Mark as read if received
    if (!isSent && message.id) {
        markMessageAsRead(message.id);
    }
    
    // Auto scroll
    if (isNew) {
        container.scrollTop = container.scrollHeight;
    }
}

async function sendMessage() {
    const input = document.getElementById('messageInput');
    const content = input.value.trim();
    
    if (!content || !currentConversationId || !currentOtherId) return;
    
    try {
        const response = await fetch('/api/messages/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `conversationId=${currentConversationId}&recipientId=${currentOtherId}&content=${encodeURIComponent(content)}`
        });
        
        // Check if response is OK
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Server error:', response.status, errorText);
            return;
        }
        
        // Check if response has content
        const responseText = await response.text();
        if (!responseText) {
            console.error('Empty response from server');
            return;
        }
        
        const message = JSON.parse(responseText);
        console.log('Message sent:', message);
        
        // Display message immediately (optimistic UI)
        displayMessage(message, true);
        
        // Also send via WebSocket to notify other users
        if (stompClient && stompClient.connected) {
            stompClient.send("/app/send-message/" + currentConversationId, {}, JSON.stringify({
                id: message.id,
                conversationId: message.conversationId,
                senderId: message.senderId,
                senderName: message.senderName,
                senderAvatar: message.senderAvatar,
                content: message.content,
                isRead: false,
                timestamp: message.timestamp || new Date().toISOString()
            }));
        }
        
        input.value = '';
        input.style.height = 'auto';
    } catch (error) {
        console.error('Error sending message:', error);
    }
}

async function markMessageAsRead(messageId) {
    try {
        await fetch(`/api/messages/${messageId}/read`, { method: 'POST' });
        // Update badge after marking as read
        if (typeof updateUnreadMessagesBadge === 'function') {
            updateUnreadMessagesBadge();
        }
    } catch (error) {
        console.error('Error marking message as read:', error);
    }
}

// Load notifications
async function loadNotifications() {
    try {
        const response = await fetch('/api/notifications');
        const notifications = await response.json();
        
        const notificationsList = document.getElementById('notificationsList');
        notificationsList.innerHTML = '';
        
        notifications.forEach(notif => {
            const item = createNotificationItem(notif);
            notificationsList.appendChild(item);
        });
        
        updateNotificationCount();
    } catch (error) {
        console.error('Error loading notifications:', error);
    }
}

function createNotificationItem(notification) {
    const item = document.createElement('div');
    item.className = `notification-item ${!notification.isRead ? 'unread' : ''}`;
    
    const time = new Date(notification.createdAt).toLocaleString('fr-FR');
    
    item.innerHTML = `
        <div class="notification-header">
            <img src="${notification.actorAvatar || '/img/default-avatar.png'}" alt="" class="notification-avatar">
            <span class="notification-type">${notification.notificationType}</span>
        </div>
        <div class="notification-content">${notification.content}</div>
        <div class="notification-time">${formatTime(notification.createdAt)}</div>
    `;
    
    item.onclick = () => {
        if (!notification.isRead) {
            markNotificationAsRead(notification.id);
        }
    };
    
    return item;
}

function displayNotification(notification) {
    const notificationsList = document.getElementById('notificationsList');
    const item = createNotificationItem(notification);
    notificationsList.insertBefore(item, notificationsList.firstChild);
}

async function markNotificationAsRead(notificationId) {
    try {
        await fetch(`/api/notifications/${notificationId}/read`, { method: 'POST' });
        updateNotificationCount();
        loadNotifications();
    } catch (error) {
        console.error('Error marking notification as read:', error);
    }
}

async function updateNotificationCount() {
    try {
        const response = await fetch('/api/notifications/unread-count');
        const count = await response.json();
        document.getElementById('unreadCount').textContent = count;
    } catch (error) {
        console.error('Error updating notification count:', error);
    }
}

// Modal functions
function openNewConversationModal() {
    const modal = document.getElementById('newConversationModal');
    modal.style.display = 'flex';
    modal.style.visibility = 'visible';
    modal.style.opacity = '1';
    document.getElementById('userSearch').value = '';
    document.getElementById('userSearchResults').innerHTML = '';
    document.body.style.overflow = 'hidden'; // Prevent scrolling
    setTimeout(() => document.getElementById('userSearch').focus(), 100);
}

function closeNewConversationModal() {
    const modal = document.getElementById('newConversationModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto'; // Re-enable scrolling
}

// Search users function
document.addEventListener('DOMContentLoaded', function() {
    const userSearchInput = document.getElementById('userSearch');
    if (userSearchInput) {
        userSearchInput.addEventListener('input', debounce(searchUsers, 300));
    }
});

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

async function searchUsers(e) {
    const query = e.target.value.trim();
    const resultsContainer = document.getElementById('userSearchResults');
    
    if (query.length < 2) {
        resultsContainer.innerHTML = '';
        return;
    }
    
    try {
        const response = await fetch(`/api/users?search=${encodeURIComponent(query)}`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const users = await response.json();
        
        resultsContainer.innerHTML = '';
        
        // Check if users is actually an array
        if (!Array.isArray(users)) {
            console.error('Users response is not an array:', users);
            resultsContainer.innerHTML = '<div style="text-align: center; padding: 20px; color: var(--text-tertiary);">Erreur lors de la recherche</div>';
            return;
        }
        
        const filteredUsers = users.filter(user => user.id !== currentUserId);
        
        if (filteredUsers.length === 0) {
            resultsContainer.innerHTML = '<div style="text-align: center; padding: 20px; color: var(--text-tertiary);">Aucun utilisateur trouvé</div>';
            return;
        }
        
        filteredUsers.forEach(user => {
            const item = createUserSearchItem(user);
            resultsContainer.appendChild(item);
        });
    } catch (error) {
        console.error('Error searching users:', error);
        resultsContainer.innerHTML = '<div style="text-align: center; padding: 20px; color: var(--text-tertiary);">Erreur lors de la recherche</div>';
    }
}

function createUserSearchItem(user) {
    const item = document.createElement('div');
    item.className = 'search-result-item';
    
    const initials = (user.firstName && user.lastName) 
        ? (user.firstName[0] + user.lastName[0]).toUpperCase()
        : user.username.substring(0, 2).toUpperCase();
    
    item.innerHTML = `
        <div class="search-result-avatar">${initials}</div>
        <div class="search-result-info">
            <div class="search-result-name">@${user.username}</div>
            <div class="search-result-username">${user.firstName} ${user.lastName}</div>
        </div>
    `;
    
    item.style.cursor = 'pointer';
    item.onclick = () => startConversation(user.id);
    
    return item;
}

async function startConversation(userId) {
    try {
        const response = await fetch(`/api/messages/conversation/${userId}`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const conversation = await response.json();
        
        closeNewConversationModal();
        
        // Create a proper conversation object to select
        const conversationData = {
            conversationId: conversation.id,
            otherUserId: userId,
            otherUserName: conversation.otherUserName || 'Utilisateur',
            otherUserAvatar: conversation.otherUserAvatar || '/img/default-avatar.png'
        };
        
        selectConversation(conversationData);
        loadConversations();
    } catch (error) {
        console.error('Error starting conversation:', error);
        alert('Erreur lors de la création de la conversation');
    }
}

// Utility functions
function formatTime(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    
    // Reset time to midnight for date comparison
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const messageDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    
    const diffTime = today - messageDate;
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) {
        // Today - show time
        return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    } else if (diffDays === 1) {
        // Yesterday
        return 'Hier';
    } else if (diffDays < 7) {
        // Within a week - show days ago
        return diffDays + 'j';
    } else {
        // Older - show date
        return date.toLocaleDateString('fr-FR', { month: 'short', day: 'numeric' });
    }
}

function escapeHtml(unsafe) {
    if (!unsafe || typeof unsafe !== 'string') {
        return '';
    }
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// Event listeners
document.addEventListener('DOMContentLoaded', function() {
    // Get current user ID from document or session
    const userIdElement = document.getElementById('currentUserId');
    if (userIdElement) {
        currentUserId = userIdElement.value;
    } else {
        // Fetch from API if not in DOM
        fetch('/api/user/current')
            .then(r => r.json())
            .then(data => {
                currentUserId = data.id;
                initializeApp();
            })
            .catch(() => {
                console.error('Unable to get current user ID');
            });
        return;
    }
    
    initializeApp();
});

function initializeApp() {
    // Initialize
    loadConversations();
    loadNotifications();
    initializeWebSocket();
    initializeNotificationWebSocket();
    
    // Check if a conversation ID was stored (from message button in directory)
    const selectedConversationId = sessionStorage.getItem('selectedConversationId');
    if (selectedConversationId) {
        sessionStorage.removeItem('selectedConversationId');
        // Auto-select the conversation after a short delay to allow loadConversations to complete
        setTimeout(() => {
            const conversationItems = document.querySelectorAll('.conversation-item');
            let foundItem = false;
            conversationItems.forEach(item => {
                if (item.dataset.conversationId === selectedConversationId) {
                    item.click();
                    foundItem = true;
                }
            });
            if (!foundItem) {
                // Reload conversations and try again
                loadConversations().then(() => {
                    const items = document.querySelectorAll('.conversation-item');
                    items.forEach(item => {
                        if (item.dataset.conversationId === selectedConversationId) {
                            item.click();
                        }
                    });
                });
            }
        }, 500);
    }
    
    // Tab switching functionality
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const tab = this.dataset.tab;
            switchTab(tab);
        });
    });
    
    // Button listeners
    document.getElementById('newMessageBtn').addEventListener('click', openNewConversationModal);
    document.getElementById('closeModalBtn').addEventListener('click', closeNewConversationModal);
    document.getElementById('sendMessageBtn').addEventListener('click', handleSendMessage);
    document.getElementById('markAllReadBtn').addEventListener('click', async () => {
        await fetch('/api/notifications/read-all', { method: 'POST' });
        loadNotifications();
    });
    
    // Message input auto-resize
    const messageInput = document.getElementById('messageInput');
    messageInput.addEventListener('input', function() {
        this.style.height = 'auto';
        this.style.height = Math.min(this.scrollHeight, 100) + 'px';
    });
    
    // Send message on Enter
    messageInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSendMessage();
        }
    });
    
    // Search conversations
    document.getElementById('conversationSearch').addEventListener('input', function() {
        const filter = this.value.toLowerCase();
        document.querySelectorAll('.conversation-item').forEach(item => {
            const name = item.querySelector('.conversation-name').textContent.toLowerCase();
            item.style.display = name.includes(filter) ? 'block' : 'none';
        });
    });
    
    // Refresh conversations every 30 seconds
    setInterval(loadConversations, 30000);
}

/**
 * Switch between Private and Projects tabs
 */
function switchTab(tabName) {
    // Update tab buttons
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');
    
    // Update tab content
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
        content.style.display = 'none';
    });
    
    if (tabName === 'private') {
        document.getElementById('privateTab').classList.add('active');
        document.getElementById('privateTab').style.display = 'block';
        // Clear current chat if switching tabs
        clearCurrentChat();
    } else if (tabName === 'projects') {
        document.getElementById('projectsTab').classList.add('active');
        document.getElementById('projectsTab').style.display = 'block';
        // Load project groups if not already loaded
        if (typeof loadProjectGroups === 'function') {
            loadProjectGroups();
        }
        // Clear current chat if switching tabs
        clearCurrentChat();
    }
}

/**
 * Clear current chat window
 */
function clearCurrentChat() {
    document.getElementById('noChatSelected').style.display = 'flex';
    document.getElementById('chatWindow').style.display = 'none';
    currentConversationId = null;
    currentOtherId = null;
    
    // Unsubscribe from any active subscriptions
    if (currentConversationSubscription) {
        currentConversationSubscription.unsubscribe();
        currentConversationSubscription = null;
    }
}

/**
 * Handle send message - determines if private or project message
 */
function handleSendMessage() {
    const content = document.getElementById('messageInput').value.trim();
    
    if (!content) {
        return;
    }
    
    // Check if we're in project mode
    if (typeof isProjectMode === 'function' && isProjectMode()) {
        if (typeof sendProjectMessage === 'function') {
            sendProjectMessage(content);
        }
    } else {
        // Private message
        sendMessage();
    }
}

// Close modal when clicking outside
document.addEventListener('click', function(e) {
    const modal = document.getElementById('newConversationModal');
    if (modal && e.target === modal) {
        closeNewConversationModal();
    }
});

// Mobile: Handle back button to return to conversations list
document.addEventListener('DOMContentLoaded', function() {
    const btnBackToConversations = document.getElementById('btnBackToConversations');
    if (btnBackToConversations) {
        btnBackToConversations.addEventListener('click', function() {
            const chatArea = document.querySelector('.chat-area');
            if (chatArea) {
                chatArea.classList.remove('mobile-active');
            }
        });
    }
});

//affichage sur petit ecrant
document.addEventListener('DOMContentLoaded', function() {
    const container = document.querySelector('.messaging-container');
    const backBtn = document.getElementById('btnBackToConversations');

    document.getElementById('conversationsList').addEventListener('click', function(e) {
        const item = e.target.closest('.conversation-item');
        if (item) {

            container.classList.add('chat-open');
        }
    });

    document.getElementById('projectGroupsList').addEventListener('click', function(e) {
        if (e.target.closest('.conversation-item')) {
            container.classList.add('chat-open');
        }
    });

    if (backBtn) {
        backBtn.addEventListener('click', function() {
            container.classList.remove('chat-open');
        });
    }
});