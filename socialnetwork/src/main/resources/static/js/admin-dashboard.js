async function fetchStats(){
    const res = await fetch('/admin/api/stats');
    if(!res.ok){ console.error('Failed to load stats'); return; }
    const data = await res.json();
    document.getElementById('totalUsers').innerText = data.totalUsers;
    document.getElementById('totalPosts').innerText = data.totalPosts;
    document.getElementById('activeUsers').innerText = data.activeUsers;
}

async function fetchTimeSeries(){
    const res = await fetch('/admin/api/stats/timeseries?days=30');
    if(!res.ok) { console.warn('No timeseries'); return; }
    const data = await res.json();
    const users = data.users.map(x => x.count);
    const posts = data.posts.map(x => x.count);
    const labels = data.users.map(x => x.date);

    if(typeof Chart !== 'undefined'){
        const uCtx = document.getElementById('usersChart').getContext('2d');
        new Chart(uCtx, {
            type: 'line',
            data: { labels, datasets: [{ label: 'Nouveaux utilisateurs', data: users, borderColor: 'rgba(29,155,240,0.9)', backgroundColor: 'rgba(29,155,240,0.2)', tension:0.3 }] },
            options: { plugins:{legend:{display:false}}, scales:{x:{display:false}} }
        });

        const pCtx = document.getElementById('postsChart').getContext('2d');
        new Chart(pCtx, {
            type: 'bar',
            data: { labels, datasets: [{ label: 'Nouveaux posts', data: posts, backgroundColor: 'rgba(29,155,240,0.8)' }] },
            options: { plugins:{legend:{display:false}}, scales:{x:{display:false}} }
        });
    }
}

async function fetchMessageStats(){
    const res = await fetch('/admin/api/stats/messages');
    if(!res.ok) return;
    const data = await res.json();
    const container = document.getElementById('recentList');
    const html = `<div>Total messages: <strong>${data.totalMessages}</strong></div><div>Moyenne messages/utilisateur: <strong>${Number(data.avgMessagesPerUser).toFixed(2)}</strong></div>`;
    container.insertAdjacentHTML('beforeend', html);
}

async function blockUser(){
    const id = document.getElementById('entityId').value.trim();
    if(!id) return alert('Enter an ID');
    const res = await fetch('/admin/api/user/'+id+'/block',{method:'POST'});
    alert(res.ok? 'User blocked' : 'Action failed');
}
async function unblockUser(){
    const id = document.getElementById('entityId').value.trim();
    if(!id) return alert('Enter an ID');
    const res = await fetch('/admin/api/user/'+id+'/unblock',{method:'POST'});
    alert(res.ok? 'User unblocked' : 'Action failed');
}
async function suspendUser(){
    const id = document.getElementById('entityId').value.trim();
    const days = parseInt(prompt('Number of days to suspend', '7'),10) || 0;
    if(!id) return alert('Enter an ID');
    const res = await fetch('/admin/api/user/'+id+'/suspend',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({days})});
    alert(res.ok? 'User suspended' : 'Action failed');
}
async function deletePost(){
    const id = document.getElementById('entityId').value.trim();
    if(!id) return alert('Enter an ID');
    const res = await fetch('/admin/api/post/'+id,{method:'DELETE'});
    alert(res.ok? 'Post deleted' : 'Action failed');
}

fetchStats();
fetchTimeSeries();
fetchMessageStats();
