lucide.createIcons();
(function(){
    const emailInput = document.getElementById('email');
    const usernameInput = document.getElementById('username');
    const emailMsg = document.getElementById('emailMsg');
    const usernameMsg = document.getElementById('usernameMsg');
    const passwordHashInput = document.getElementById('passwordHash')
    const passwordHashMsg = document.getElementById('passwordHashMsg');

    const emailRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@(eleve\.isep\.fr|isep\.fr|ext\.isep\.fr)$/i;
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

    function setState(el, msgEl, ok, message){
        el.classList.remove('success','error');
        if(ok){
            el.classList.add('success');
            msgEl.style.display = 'none';
        } else {
            el.classList.add('error');
            msgEl.textContent = message || '';
            msgEl.style.display = 'block';
        }
    }

    // email validation
    emailInput.addEventListener('input', function(){
        const val = this.value.trim();
        if(val.length === 0){
            this.classList.remove('success','error');
            emailMsg.style.display='none';
            return;
        }
        if(emailRegex.test(val)){
            setState(this, emailMsg, true);
        } else {
            setState(this, emailMsg, false, "L'email doit être une adresse ISEP (eleve.isep.fr, isep.fr, ext.isep.fr)");
        }
    });

    // username uniqueness check with debounce
    let timer = null;
    usernameInput.addEventListener('input', function(){
        const val = this.value.trim();
        clearTimeout(timer);
        if(val.length === 0){
            this.classList.remove('success','error');
            usernameMsg.style.display='none';
            return;
        }
        timer = setTimeout(async ()=>{
            try{
                const resp = await fetch('/api/check-username?username='+encodeURIComponent(val));
                if(resp.ok){
                    const data = await resp.json();
                    if(data.exists){
                        setState(usernameInput, usernameMsg, false, 'Username already taken');
                    } else {
                        setState(usernameInput, usernameMsg, true);
                    }
                } else {
                    // on error, mark neutral
                    usernameInput.classList.remove('success','error');
                    usernameMsg.style.display='none';
                }
            }catch(e){
                usernameInput.classList.remove('success','error');
                usernameMsg.style.display='none';
            }
        }, 450);
    });


    passwordHashInput.addEventListener('input', function(){
        const val = this.value.trim();
        if(val.length === 0){
            this.classList.remove('success','error');
            usernameMsg.style.display='none';
            return;
        }

        if(passwordRegex.test(val)){
            setState(this, passwordHashMsg, true);
        } else {
            setState(this, passwordHashMsg, false, "Le mot de passe doit contenir au moins 8 caractères, avec au moins une majuscule, une minuscule, un chiffre et un caractère spécial");
        }
    });

})();