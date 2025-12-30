
const profilePictureImg = document.getElementById('profilePictureImg');
const profilePictureLink = document.getElementById('profilePictureUrl');
const coverPictureImg = document.getElementById('coverPictureImg');
const coverPictureLink = document.getElementById('coverPictureUrl');

profilePictureImg.onclick = function () {
                    profilePictureLink.click()
                }
                
coverPictureImg.onclick = function () {
                    coverPictureLink.click()
                }

profilePictureLink.onchange = evt => {
  const [file] = profilePictureLink.files
  if (file) {
    profilePictureImg.src = URL.createObjectURL(file)
  }
}

coverPictureLink.onchange = evt => {
  const [file] = coverPictureLink.files
  if (file) {
    coverPictureImg.src = URL.createObjectURL(file)
  }
}