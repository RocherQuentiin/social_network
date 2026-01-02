const userFollow = document.querySelectorAll(".follow");
const userUnfollow = document.querySelectorAll(".unfollow");

userFollow.forEach(el => {
	el.addEventListener('click', followUser, el);
});

userUnfollow.forEach(el => {
	el.addEventListener('click', unfollowUser, el);
});

function followUser(el) {
	let userId = el.srcElement.getAttribute('data-id');
	fetch('/follow', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		body: 'userID=' + userId
	})
		.then(response => {
			if(response.status == 200){
				window.location.reload();
			}
		})
}

function unfollowUser(el) {
	let userId = el.srcElement.getAttribute('data-id');
	fetch('/unfollow', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		body: 'userID=' + userId
	})
		.then(response => {
			if(response.status == 200){
				window.location.reload();
			}
		})
}