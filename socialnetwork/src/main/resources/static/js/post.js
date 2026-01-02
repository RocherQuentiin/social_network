document.addEventListener('DOMContentLoaded', function(){
    console.log('post.js loaded: DOMContentLoaded');
    const openBtn = document.getElementById('openPostModal');
    const modal = document.getElementById('postModal');
    const cancel = document.getElementById('cancelPost');
    console.log('post.js elements:', {openBtn: !!openBtn, modal: !!modal, cancel: !!cancel});

    if(openBtn && modal){
        openBtn.addEventListener('click', ()=>{
            console.log('openPostModal clicked');
            modal.style.display = 'block';
        });

        cancel && cancel.addEventListener('click', ()=>{
            console.log('cancelPost clicked');
            modal.style.display = 'none';
        });

        // close when clicking on backdrop
        modal.addEventListener('click', (e)=>{
            if(e.target === modal){
                console.log('backdrop clicked');
                modal.style.display = 'none';
            }
        });
    } else {
        console.warn('post.js: openBtn or modal not found; modal will not open');
    }
    
    const postVideo = document.getElementById('postVideo');
	const postVideoLink = document.getElementById('postVideoUrl');
	const postImage = document.getElementById('postImage');
	const postImageLink = document.getElementById('postImageUrl');
	
	postVideo.onclick = function () {
                    postVideoLink.click()
                }
                
	postImage.onclick = function () {
                    postImageLink.click()
                }
    
});
