const userView = document.querySelectorAll(".user-view");

userView.forEach(el => {
    el.addEventListener('click', callCorrectPage, el);
});

function callCorrectPage(el) {
    let userId = el.currentTarget.getAttribute('data-id');
    window.location.href = "/profil/" + userId;
    console.log(userId);
}

// Message button click handler
document.addEventListener('click', function(e) {
    if (e.target.closest('.btn-msg-small')) {
        const btn = e.target.closest('.btn-msg-small');
        const userId = btn.getAttribute('data-id');
        if (userId) {
            openOrCreateConversation(userId);
        }
    }
});

async function openOrCreateConversation(userId) {
    try {
        // Get or create conversation with the user
        const response = await fetch(`/api/messages/conversation/${userId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Failed to get/create conversation. Status:', response.status, 'Message:', errorText);
            alert('Impossible de créer la conversation: ' + errorText);
            return;
        }

        const conversation = await response.json();
        console.log('Conversation created/fetched:', conversation);
        
        // Navigate to messages page and store the conversation ID to auto-select it
        sessionStorage.setItem('selectedConversationId', conversation.id);
        window.location.href = '/messages';
    } catch (error) {
        console.error('Error opening/creating conversation:', error);
        alert('Erreur lors de la création de la conversation');
    }
}


document.addEventListener('DOMContentLoaded', function() {
    const gridBtn = document.querySelector('.view-switcher button:first-of-type');
    const listBtn = document.querySelector('.view-switcher button:last-of-type');
    const userGrid = document.querySelector('.user-grid');

    // Fonction pour activer la vue Liste
    listBtn.addEventListener('click', function() {
        userGrid.classList.add('list-view');

        // Gestion visuelle des boutons
        listBtn.classList.add('active');
        gridBtn.classList.remove('active');
    });

    // Fonction pour activer la vue Grille (Normal)
    gridBtn.addEventListener('click', function() {
        userGrid.classList.remove('list-view');

        // Gestion visuelle des boutons
        gridBtn.classList.add('active');
        listBtn.classList.remove('active');
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('directorySearch');
    const filterButtons = document.querySelectorAll('.chip');
    const userCards = document.querySelectorAll('.user-grid article');


    const activeFilters = {
        search: "",
        filiere: "Toutes",
        promo: "Toutes"
    };

    function applyFilters() {
        userCards.forEach(card => {
            const userName = card.querySelector('.user-card-body h3').textContent.toLowerCase();
            const userMajor = card.querySelector('.user-major-label').textContent;
            const userPromo = card.querySelector('.user-promo-label').textContent; // Ex: "Promo 2025"

            const matchesSearch = userName.includes(activeFilters.search);
            const matchesFiliere = activeFilters.filiere === "Toutes" || userMajor === activeFilters.filiere;

            const matchesPromo = activeFilters.promo === "Toutes" || userPromo.includes(activeFilters.promo);

            if (matchesSearch && matchesFiliere && matchesPromo) {
                card.style.display = 'block';
            } else {
                card.style.display = 'none';
            }
        });
        handleEmptyResults();
    }

    // Écouteur Recherche
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            activeFilters.search = e.target.value.toLowerCase().trim();
            applyFilters();
        });
    }

    // Écouteur Filtres (Chips)
    filterButtons.forEach(button => {
        button.addEventListener('click', () => {
            const row = button.closest('.filter-row');
            const label = row.querySelector('.filter-label').textContent.toLowerCase();
            const value = button.textContent;

            row.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
            button.classList.add('active');

            if (label.includes('filière')) {
                activeFilters.filiere = value;
            } else if (label.includes('promo')) {
                activeFilters.promo = value;
            }

            applyFilters();
        });
    });
});

function handleEmptyResults() {
    const grid = document.querySelector('.user-grid');
    const visibleCards = document.querySelectorAll('.user-grid article[style="display: block;"]').length;
    const existingMsg = document.getElementById('no-result-msg');

    if (visibleCards === 0 && !existingMsg) {
        const msg = document.createElement('p');
        msg.id = 'no-result-msg';
        msg.style.color = 'var(--text-dim)';
        msg.style.gridColumn = '1 / -1';
        msg.style.textAlign = 'center';
        msg.style.padding = '40px';
        msg.textContent = "Aucun étudiant ne correspond à ces critères.";
        grid.appendChild(msg);
    } else if (visibleCards > 0 && existingMsg) {
        existingMsg.remove();
    }
}