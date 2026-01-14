document.addEventListener('DOMContentLoaded', function(){
    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.getElementById('searchBtn');
    const body = document.getElementById('usersTableBody');
    const pager = document.getElementById('usersPager');
    let page = 0, size = 20;

    async function load(pageArg=0){
        const q = encodeURIComponent(searchInput.value||'');
        const res = await fetch(`/admin/api/users?query=${q}&page=${pageArg}&size=${size}`);
        if(!res.ok){ body.innerHTML = '<tr><td colspan="6">Erreur de chargement</td></tr>'; return; }
        const data = await res.json();
        body.innerHTML = '';
        for(const u of data.items){
            const tr = document.createElement('tr');
            const suspended = u.suspendedUntil ? new Date(u.suspendedUntil) : null;
            const suspendedText = suspended && !isNaN(suspended) ? suspended.toLocaleString() : '';
            const statusHtml = u.isActive ? `<span class="badge badge-success">Actif</span>` : `<span class="badge badge-danger">Bloqué</span>`;
            const roleHtml = `<span class="badge badge-muted">${u.role || 'USER'}</span>`;
            const usernameHtml = `<div style="font-weight:600">${u.username || ''}</div>`;
            const actions = [];
            if(u.isActive){ actions.push(`<button class="btn btn-outline" data-id="${u.id}" data-action="block">Bloquer</button>`); }
            else { actions.push(`<button class="btn btn-outline" data-id="${u.id}" data-action="unblock">Débloquer</button>`); }
            actions.push(`<button class="btn btn-primary" data-id="${u.id}" data-action="suspend">Suspendre</button>`);

            tr.innerHTML = `
                <td>${usernameHtml}<div style="font-size:0.85rem;color:var(--text-dim)">${u.displayName || ''}</div></td>
                <td>${u.email || ''}</td>
                <td>${roleHtml}</td>
                <td>${statusHtml}</td>
                <td>${suspendedText}</td>
                <td class="user-actions">${actions.join('')}</td>
            `;
            body.appendChild(tr);
        }
        renderPager(data.page, data.size, data.total);
    }

    function renderPager(p, s, total){
        pager.innerHTML = '';
        const totalPages = Math.ceil(total / s);
        for(let i=0;i<totalPages;i++){
            const btn = document.createElement('button');
            btn.className = 'btn btn-outline';
            btn.innerText = i+1;
            if(i===p) btn.disabled = true;
            btn.addEventListener('click', ()=> load(i));
            pager.appendChild(btn);
        }
    }

    body.addEventListener('click', async function(e){
        const actionBtn = e.target.closest('button[data-action]');
        if(!actionBtn) return;
        const id = actionBtn.getAttribute('data-id');
        const action = actionBtn.getAttribute('data-action');
        // open confirm modal for block/unblock
        if(action === 'block' || action === 'unblock'){
            openConfirm(`Voulez-vous ${action === 'block' ? 'bloquer' : 'débloquer'} cet utilisateur ?`, async ()=>{
                await fetch(`/admin/api/user/${id}/${action}`, {method:'POST'});
                closeConfirm(); load(page);
            });
        } else if(action === 'suspend'){
            openSuspend(id);
        }
    });

    // Modal helpers
    const confirmModal = document.getElementById('confirmModal');
    const confirmMessage = document.getElementById('confirmMessage');
    const confirmOk = document.getElementById('confirmOk');
    const confirmCancel = document.getElementById('confirmCancel');
    let confirmCallback = null;
    function openConfirm(message, cb){ confirmMessage.innerText = message; confirmCallback = cb; confirmModal.style.display = 'flex'; }
    function closeConfirm(){ confirmModal.style.display = 'none'; confirmCallback = null; }
    confirmCancel.addEventListener('click', ()=> closeConfirm());
    confirmOk.addEventListener('click', ()=> { if(confirmCallback) confirmCallback(); });

    // Suspend modal
    const suspendModal = document.getElementById('suspendModal');
    const suspendOk = document.getElementById('suspendOk');
    const suspendCancel = document.getElementById('suspendCancel');
    const suspendDaysInput = document.getElementById('suspendDays');
    let suspendTargetId = null;
    function openSuspend(id){ suspendTargetId = id; suspendModal.style.display = 'flex'; }
    function closeSuspend(){ suspendModal.style.display = 'none'; suspendTargetId = null; }
    suspendCancel.addEventListener('click', ()=> closeSuspend());
    suspendOk.addEventListener('click', async ()=>{
        const days = parseInt(suspendDaysInput.value,10) || 0;
        if(!suspendTargetId || days <= 0) return;
        await fetch(`/admin/api/user/${suspendTargetId}/suspend`, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({days})});
        closeSuspend(); load(page);
    });

    searchBtn.addEventListener('click', ()=> load(0));
    searchInput.addEventListener('keydown', (e)=>{ if(e.key === 'Enter') load(0); });

    // tab switcher
    const tabKpis = document.getElementById('tabKpis');
    const tabUsers = document.getElementById('tabUsers');
    const kpisTab = document.getElementById('kpisTab');
    const usersTab = document.getElementById('usersTab');
    tabKpis.addEventListener('click', ()=>{ kpisTab.style.display='block'; usersTab.style.display='none'; });
    tabUsers.addEventListener('click', ()=>{ kpisTab.style.display='none'; usersTab.style.display='block'; load(0); });
});
