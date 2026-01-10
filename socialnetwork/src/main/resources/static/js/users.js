const userView = document.querySelectorAll(".user-view");

userView.forEach(el => {
  el.addEventListener('click', callCorrectPage, el);
});

function callCorrectPage(el) {
	let userId = el.srcElement.getAttribute('data-id');
	window.location.href = "/profil/" + userId;
}