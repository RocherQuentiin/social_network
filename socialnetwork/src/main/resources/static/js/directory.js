const userView = document.querySelectorAll(".user-view");

userView.forEach(el => {
    el.addEventListener('click', callCorrectPage, el);
});

function callCorrectPage(el) {
    let userId = el.srcElement.getAttribute('data-id');
    window.location.href = "/profil/" + userId;
    console.log(userId);
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