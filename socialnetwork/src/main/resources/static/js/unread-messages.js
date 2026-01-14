// Update unread messages badge in header (includes private + project messages)
async function updateUnreadMessagesBadge() {
    try {
        // Get unread private messages count
        const privateResponse = await fetch('/api/messages/unread-count');
        if (!privateResponse.ok) return;
        
        let totalCount = await privateResponse.json();
        
        // Get unread project messages count
        try {
            const projectCount = await getUnreadProjectMessagesCount();
            totalCount += projectCount;
        } catch (error) {
            console.warn('Could not fetch project messages count:', error);
        }
        
        const badge = document.getElementById('unread-messages-badge');
        const badgeMobile = document.getElementById('unread-messages-badge-mobile');
        
        if (totalCount > 0) {
            const displayCount = totalCount > 99 ? '99+' : totalCount.toString();
            if (badge) {
                badge.textContent = displayCount;
                badge.style.display = 'flex';
            }
            if (badgeMobile) {
                badgeMobile.textContent = displayCount;
                badgeMobile.style.display = 'flex';
            }
        } else {
            if (badge) badge.style.display = 'none';
            if (badgeMobile) badgeMobile.style.display = 'none';
        }
    } catch (error) {
        console.error('Error updating unread messages badge:', error);
    }
}

/**
 * Get total unread count for all project message groups
 */
async function getUnreadProjectMessagesCount() {
    try {
        // Get user's projects
        const projectsResponse = await fetch('/api/project/my-projects');
        if (!projectsResponse.ok) return 0;
        
        const projects = await projectsResponse.json();
        if (!Array.isArray(projects) || projects.length === 0) return 0;
        
        // Get unread count for each project's groups
        let totalUnread = 0;
        
        for (const project of projects) {
            try {
                const groupsResponse = await fetch(`/api/project-messages/groups/${project.id}`);
                if (!groupsResponse.ok) continue;
                
                const groups = await groupsResponse.json();
                if (!Array.isArray(groups)) continue;
                
                for (const group of groups) {
                    try {
                        const unreadResponse = await fetch(`/api/project-messages/${group.id}/unread-count`);
                        if (unreadResponse.ok) {
                            const count = await unreadResponse.json();
                            totalUnread += count;
                        }
                    } catch (error) {
                        console.warn(`Could not fetch unread count for group ${group.id}:`, error);
                    }
                }
            } catch (error) {
                console.warn(`Could not fetch groups for project ${project.id}:`, error);
            }
        }
        
        return totalUnread;
    } catch (error) {
        console.error('Error calculating unread project messages:', error);
        return 0;
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    updateUnreadMessagesBadge();
    // Refresh every 30 seconds
    setInterval(updateUnreadMessagesBadge, 30000);
});

// Expose function globally for use in project-chat.js
window.updateUnreadMessagesBadge = updateUnreadMessagesBadge;
