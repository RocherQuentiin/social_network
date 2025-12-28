document.addEventListener('DOMContentLoaded', function(){
    function getMeta(name){
        var m = document.querySelector('meta[name="' + name + '"]');
        return m ? m.getAttribute('content') : null;
    }

    var csrfToken = getMeta('_csrf');
    var csrfHeader = getMeta('_csrf_header') || 'X-CSRF-TOKEN';

    window.csrfHeaders = csrfToken ? (function(){ var h = {}; h[csrfHeader] = csrfToken; return h; })() : {};

    window.fetchWithCsrf = function(url, opts){
        opts = opts || {};
        opts.headers = Object.assign({}, opts.headers || {}, window.csrfHeaders);
        return fetch(url, opts);
    };

    // small debug helper
    if(csrfToken){
        console.debug('feed-posts.js: CSRF token found and set');
    } else {
        console.debug('feed-posts.js: no CSRF token found');
    }

    // Edit modal logic
    function byId(id){ return document.getElementById(id); }

    var editModal = byId('editModal');
    var closeBtn = byId('closeEditModal');
    var saveBtn = byId('saveEditBtn');
    var editContent = byId('editContent');
    var editVisibility = byId('editVisibility');
    var editAllowComments = byId('editAllowComments');
    var currentEditId = null;

    function openModal(){ if(editModal) editModal.style.display = 'flex'; }
    function closeModal(){ if(editModal) editModal.style.display = 'none'; currentEditId = null; }

    document.addEventListener('click', function(e){
        var btn = e.target.closest && (e.target.closest('button[data-post-id]') || e.target.closest('button.btn-icon'));
        if(!btn) return;
        var postId = btn.getAttribute('data-post-id');
        if(!postId) return;
        e.preventDefault();
        // fetch the post
        fetch('/post/' + postId)
            .then(function(r){ if(!r.ok) throw new Error('Not found'); return r.json(); })
            .then(function(dto){
                currentEditId = postId;
                // re-query elements in case DOM changed
                editContent = editContent || byId('editContent');
                editVisibility = editVisibility || byId('editVisibility');
                editAllowComments = editAllowComments || byId('editAllowComments');
                if(editContent) editContent.value = dto.content || '';
                if(editVisibility) editVisibility.value = (dto.visibilityType || 'PUBLIC');
                if(editAllowComments) editAllowComments.checked = !!dto.allowComments;
                openModal();
            }).catch(function(err){ console.error('fetch post error', err); alert('Impossible de charger la publication'); });
    });

    if(closeBtn) closeBtn.addEventListener('click', function(){ closeModal(); });

    if(saveBtn) saveBtn.addEventListener('click', function(){
        if(!currentEditId) return alert('Aucune publication en cours');
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
            headers: Object.assign({'Content-Type':'application/json'}, window.csrfHeaders || {}),
            body: JSON.stringify(payload)
        }).then(function(r){
            if(r.ok){ closeModal(); window.location.reload(); }
            else if(r.status === 403){ alert('Accès refusé'); }
            else { r.text().then(function(t){ console.error(t); alert('Erreur lors de la sauvegarde'); }); }
        }).catch(function(err){ console.error(err); alert('Erreur réseau'); });
    });

});
