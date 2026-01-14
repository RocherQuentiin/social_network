// Update unread messages badge in header
async function updateUnreadMessagesBadge() {
    try {
        const response = await fetch('/api/messages/unread-count');
        if (!response.ok) return;
        
        const count = await response.json();
        
        const badge = document.getElementById('unread-messages-badge');
        const badgeMobile = document.getElementById('unread-messages-badge-mobile');
        
        if (count > 0) {
            const displayCount = count > 99 ? '99+' : count.toString();
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

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    updateUnreadMessagesBadge();
    // Refresh every 30 seconds
    setInterval(updateUnreadMessagesBadge, 30000);
});
