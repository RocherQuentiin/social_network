/* ============================================================
   directory.js
   ============================================================ */

/* --- Navigation vers le profil --- */
document.querySelectorAll('.user-view').forEach(el => {
    el.addEventListener('click', function() {
        const userId = this.getAttribute('data-id');
        window.location.href = '/profil/' + userId;
    });
});

/* --- Message privé --- */
document.addEventListener('click', function(e) {
    const btn = e.target.closest('.btn-msg-small');
    if (btn) {
        const userId = btn.getAttribute('data-id');
        if (userId) openOrCreateConversation(userId);
    }
});

async function openOrCreateConversation(userId) {
    try {
        const response = await fetch(`/api/messages/conversation/${userId}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });
        if (!response.ok) {
            alert('Impossible de créer la conversation : ' + await response.text());
            return;
        }
        const conversation = await response.json();
        sessionStorage.setItem('selectedConversationId', conversation.id);
        window.location.href = '/messages';
    } catch (error) {
        console.error('Error:', error);
        alert('Erreur lors de la création de la conversation');
    }
}

/* --- Vue grille / liste --- */
document.addEventListener('DOMContentLoaded', function() {
    const gridBtn = document.querySelector('.view-switcher button:first-of-type');
    const listBtn = document.querySelector('.view-switcher button:last-of-type');
    const userGrid = document.querySelector('.user-grid');

    if (gridBtn && listBtn && userGrid) {
        gridBtn.addEventListener('click', function() {
            userGrid.classList.remove('list-view');
            gridBtn.classList.add('active');
            listBtn.classList.remove('active');
        });
        listBtn.addEventListener('click', function() {
            userGrid.classList.add('list-view');
            listBtn.classList.add('active');
            gridBtn.classList.remove('active');
        });
    }

    /* --- Recherche + filtre type --- */
    const searchInput = document.getElementById('directorySearch');
    const userCards   = document.querySelectorAll('.user-grid article');

    const activeFilters = { search: '', type: 'tous' };

    function isPlace(card) {
        return card.querySelector('.badge-place') !== null;
    }

    function applyFilters() {
        userCards.forEach(card => {
            const name = card.querySelector('h3')?.textContent.toLowerCase()
                .normalize('NFD').replace(/[\u0300-\u036f]/g, '') || '';

            const matchSearch = name.includes(
                activeFilters.search.normalize('NFD').replace(/[\u0300-\u036f]/g, '')
            );

            const matchType =
                activeFilters.type === 'tous' ||
                (activeFilters.type === 'lieu'    &&  isPlace(card)) ||
                (activeFilters.type === 'personne' && !isPlace(card));

            card.style.display = (matchSearch && matchType) ? '' : 'none';
        });
        handleEmptyResults();
    }

    /* Recherche */
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            activeFilters.search = this.value.toLowerCase().trim();
            applyFilters();
        });
    }

    /* Chips type */
    document.querySelectorAll('#typeFilter .chip').forEach(btn => {
        btn.addEventListener('click', function() {
            document.querySelectorAll('#typeFilter .chip').forEach(c => c.classList.remove('active'));
            this.classList.add('active');
            activeFilters.type = this.getAttribute('data-type');
            applyFilters();
        });
    });
});

function handleEmptyResults() {
    const grid = document.querySelector('.user-grid');
    const visible = [...document.querySelectorAll('.user-grid article')]
        .filter(c => c.style.display !== 'none').length;
    const existing = document.getElementById('no-result-msg');

    if (visible === 0 && !existing) {
        const msg = document.createElement('p');
        msg.id = 'no-result-msg';
        msg.style.cssText = 'color:var(--text-dim);grid-column:1/-1;text-align:center;padding:40px';
        msg.textContent = 'Aucun résultat ne correspond à ces critères.';
        grid.appendChild(msg);
    } else if (visible > 0 && existing) {
        existing.remove();
    }
}