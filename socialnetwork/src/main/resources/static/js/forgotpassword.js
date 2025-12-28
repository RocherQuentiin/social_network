lucide.createIcons();


   const passwordHashInput = document.getElementById('passwordHash');
   const confirmPasswordHashInput = document.getElementById('confirmpasswordHash');
   const passwordHashMsg = document.getElementById('passwordHashMsg');
   const confirmPasswordHashMsg = document.getElementById('confirmpasswordHashMsg');
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
   
   passwordHashInput.addEventListener('input', function(){
        const val = this.value.trim();
        console.log("ok")
        console.log(val);
        if(val.length === 0){
            this.classList.remove('success','error');
            passwordHashMsg.style.display='none';
            return;
        }

        if(passwordRegex.test(val)){
            setState(this, passwordHashMsg, true);
        } else {
            setState(this, passwordHashMsg, false, "Le mot de passe doit contenir au moins 8 caractères, avec au moins une majuscule, une minuscule, un chiffre et un caractère spécial");
        }
    });
    
    confirmPasswordHashInput.addEventListener('input', function(){
        const val = this.value.trim();
        
        if(val.length === 0){
            this.classList.remove('success','error');
            confirmPasswordHashMsg.style.display='none';
            return;
        }
        
        if(val == passwordHashInput.value){
            setState(this, confirmPasswordHashMsg, true);
        }
        else{
            setState(this, confirmPasswordHashMsg, false, "Les deux mots de passes doivent être identiques");
        }
    });
