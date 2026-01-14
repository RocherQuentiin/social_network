document.addEventListener('DOMContentLoaded', function() {
	function getMeta(name)  {
		var m = document.querySelector('meta[name="' + name + '"]');
		return m ? m.getAttribute('content') : null;
	}

	var csrfToken = getMeta('_csrf');
	var csrfHeader = getMeta('_csrf_header') || 'X-CSRF-TOKEN';

	window.csrfHeaders = csrfToken ? (function() { var h = {}; h[csrfHeader] = csrfToken; return h; })() : {};

	window.fetchWithCsrf = function(url, opts) {
		opts = opts || {};
		opts.headers = Object.assign({}, opts.headers || {}, window.csrfHeaders);
		return fetch(url, opts);
	};

	// small debug helper
	if  (csrfToken) {
	console.debug('feed-posts.js: CSRF token found and set');
} else {
	console.debug('feed-posts.js: no CSRF token found');
}

// Edit modal logic
function byId(id)  { return document.getElementById(id); }

var editModal = byId('editModal');
var closeBtn = byId('closeEditModal');
var saveBtn = byId('saveEditBtn');
var editContent = byId('editContent');
var editVisibility = byId('editVisibility');
var editAllowComments = byId('editAllowComments');
var currentEditId = null;

function openModal() { if (editModal) editModal.style.display = 'flex'; }
function closeModal()  { if (editModal) editModal.style.display = 'none'; currentEditId = null; }

document.addEventListener('click', function(e)  {
	var btn = e.target.closest && (e.target.closest('button[data-post-id]') || e.target.closest('button.btn-icon'));
	if  (!btn) return;
	// Skip if this is a comment toggle button or reaction button
	if  (btn.classList.contains('btn-toggle-comments') || btn.classList.contains('reaction-btn')) return;
var postId = btn.getAttribute('data-post-id');
if (!postId) return;
// if this is a delete button
if (btn.classList.contains('btn-delete'))  {
	if (!confirm('Voulez-vous vraiment supprimer cette publication ?')) return;
	window.fetchWithCsrf('/post/' + postId, { method: 'DELETE' })
		.then(function(r) {
			if  (r.ok) {
				// remove element from DOM
				var card = document.querySelector('[data-post-card-id="' + postId + '"]');
				if  (card) card.remov();
			} else if (r.status === 403) { alert('Accès refusé'); }
			else { r.text().then(function(t)  { console.erro(t); alert('Erreur lors de la suppression'); }); }
		}).catch(function(err) { console.error(err); alert('Erreur réseau'); });
	return;
}
e.preventDefault();
// fetch the post
fetch('/post/' + postId)
	.then(function(r) { if (!r.ok) throw new Error('Not found'); return r.json(); })
	.then(function(dto) {
		currentEditId = postId;
		// re-query elements in case DOM changed
		editContent = editContent || byId('editContent');
		editVisibility = editVisibility || byId('editVisibility');
		editAllowComments = editAllowComments || byId('editAllowComments');
		if  (editConten) editContent.value = dto.content || '';
		if  (editVisibility) editVisibility.value = (dto.visibilityType || 'PUBLIC');
if  (editAllowComments) editAllowComments.checked = !!dto.allowComments;
openModal();
			}).catch (function(err)  { consol.error('fetch post error', err); alert('Impossible de charger la publication'); });
	});

if (closeBtn) closeBtn.addEventListener('click', function() { closeModal(); });

if (saveBtn) saveBtn.addEventListener('click', function()  {
	if (!currentEditId) return alert('Aucune publication en cours');
	// re-query elements
	editContent = editContent || byId('editContent');
	editVisibility = editVisibility || byId('editVisibility');
	editAllowComments = editAllowComments || byId('editAllowComments');
	var payload = {
		content: editContent ? editContent.value : '',
		visibilityType: editVisibility ? editVisibility.value : 'PUBLIC',
		allowComments: editAllowComments ? !!editAllowComments.checked : true
	};
	window.fetchWithCsrf('/post/' + currentEditId, {
		method: 'PUT',
		headers: Object.assign({ 'Content-Type': 'application/json' }, window.csrfHeaders || {}),
		body: JSON.stringify(payload)
	}).then(function(r) {
		if  (r.ok ) { closeModal(); window.location.reload(); }
		else if (r.status === 403) { alert('Accès refusé'); }
		else { r.text().then(function(t)  { consol.error(t); alert('Erreur lors de la sauvegarde'); }); }
	}).catch(function(err) { console.error(err); alert('Erreur réseau'); });
});

const postVideo = document.getElementById('postVideo');
const postVideoLink = document.getElementById('postVideoUrl');
const postImage = document.getElementById('postImage');
const postImageLink = document.getElementById('postImageUrl');

postVideo.onclick = function() {
	postVideoLink.click()
}

postImage.onclick = function() {
	postImageLink.click()
}

const publicCheckbox = document.getElementById('publicCheckbox');
const privateCheckbox = document.getElementById('privateCheckbox');
const friendsCheckbox = document.getElementById('friendsCheckbox');

const publicElements = document.querySelectorAll("[data-post-visibility='PUBLIC']")
const privateElements = document.querySelectorAll("[data-post-visibility='PRIVATE']")
const friendsElements = document.querySelectorAll("[data-post-visibility='FRIENDS']")

publicCheckbox.addEventListener("change", changeVisibility);
privateCheckbox.addEventListener("change", changeVisibility);
friendsCheckbox.addEventListener("change", changeVisibility);

function changeVisibility() {
	let publicChecked = publicCheckbox.checked;
	let privateChecked = privateCheckbox.checked;
	let friendsChecked = friendsCheckbox.checked;

	publicVisibility(publicChecked);
	privateVisibility(privateChecked);
	friendsVisibility(friendsChecked);
}

function publicVisibility(publicChecked) {
	if (publicChecked) {
		publicElements.forEach(el => {
			el.style.display = "";
		})
	}
	else {
		publicElements.forEach(el => {
			el.style.display = "none";
		})
	}
}

function privateVisibility(privateChecked) {
	if (privateChecked) {
		privateElements.forEach(el => {
			el.style.display = "";
		})
	}
	else {
		privateElements.forEach(el => {
			el.style.display = "none";
		})
	}
}

function friendsVisibility(friendsChecked) {
	if (friendsChecked) {
		friendsElements.forEach(el => {
			el.style.display = "";
		})
	}
	else {
		friendsElements.forEach(el => {
			el.style.display = "none";
		})
	}
}

const annoucements = document.querySelectorAll(".announcement-part");
const eventAction = document.querySelectorAll(".eventAction");

annoucements.forEach(el => {
	el.addEventListener('click', GoToEvent, el)
})

eventAction.forEach(el => {
	el.addEventListener('click', GoToEvent, el)
})

function GoToEvent(el) {
	let eventId = el.currentTarget.getAttribute('event-id');
	window.location.href = "/event/" + eventId;
}

const eventInputSearch = document.getElementById("event-search");
const annoucement = document.querySelectorAll(".announcement-part.card");

eventInputSearch.addEventListener('input', function() {
	let inputValue = eventInputSearch.value.toLowerCase();
	annoucement.forEach(elm => {
		let firstName = elm.getAttribute('data-creator-firstname').toLowerCase();
		let lastName = elm.getAttribute('data-creator-lastname').toLowerCase();
		let eventName = elm.getAttribute('data-event-name').toLowerCase();
		let eventLocation = elm.getAttribute('data-event-location').toLowerCase();
		let eventVisibility = elm.getAttribute('data-event-visibility').toLowerCase();

		if (firstName.includes(inputValue) || lastName.includes(inputValue) || eventName.includes(inputValue)
			|| eventLocation.includes(inputValue) || eventVisibility.includes(inputValue)) {
			elm.style.display = "";
		}
		else {
			elm.style.display = "none";
		}
	})

})

const postInputSearch = document.getElementById("post-search");
const post = document.querySelectorAll(".post-card.card");

postInputSearch.addEventListener('input', function() {
	let inputValue = postInputSearch.value.toLowerCase();
	console.log(post)
	post.forEach(elm => {
		let firstName = elm.getAttribute('data-author-firstname').toLowerCase();
		let lastName = elm.getAttribute('data-auhtor-lastname').toLowerCase();
		let postVisibility = elm.getAttribute('data-post-visibility').toLowerCase();
		let postContent = elm.getAttribute('data-post-content').toLowerCase();

		if (firstName.includes(inputValue) || lastName.includes(inputValue)
			|| postContent.includes(inputValue) || postVisibility.includes(inputValue))  {
			elm.style.display = "";
		}
		else {
			elm.style.display = "none";
		}
	})

})


console.log(preferences)
preferences.addEventListener("change", function() {
	let val = preferences.value;
	console.log(val)
	
	if(val == "Popularité"){
		sortPostsByReactions();
	}
	else if(val == "Commentaires"){
		sortPostsByComments();
	}
	else{
		sortPostsByDates();
	}
});

function sortPostsByReactions() {
	const container = document.querySelector('.posts-list');
	const posts = Array.from(container.querySelectorAll('.post-card'));
	
	posts.sort((a, b) => {
		const aTotal = parseInt(
			a.querySelector('.reaction-total')?.innerText.charAt(0) || 0
		);
		const bTotal = parseInt(
			b.querySelector('.reaction-total')?.innerText.charAt(0) || 0
		);

		return bTotal - aTotal;
	});
	console.log(posts)
	posts.forEach(post => container.appendChild(post));
}

function sortPostsByComments() {
	const container = document.querySelector('.posts-list');
	const posts = Array.from(container.querySelectorAll('.post-card'));

	posts.sort((a, b) => {
		console.log(a.querySelector('.comment-count')?.innerText)
		const aTotal = parseInt(
			a.querySelector('.comment-count')?.innerText || 0
		);
		const bTotal = parseInt(
			b.querySelector('.comment-count')?.innerText || 0
		);

		return bTotal - aTotal;
	});

	posts.forEach(post => container.appendChild(post));
}

function sortPostsByDates() {
	const container = document.querySelector('.posts-list');
    const posts = Array.from(container.querySelectorAll(".post-card"));

    posts.sort((a, b) => {
        const dateA = new Date(a.dataset.postCreatedat);
        const dateB = new Date(b.dataset.postCreatedat);
        return dateB - dateA; // DESC (récent → ancien)
    });

    posts.forEach(post => container.appendChild(post));
}

});
