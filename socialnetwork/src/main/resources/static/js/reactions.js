(function(){
    function toArray(nodeList){ return Array.prototype.slice.call(nodeList || []); }

    function fetchJson(url){
        var fn = (typeof window.fetchWithCsrf === 'function') ? window.fetchWithCsrf : fetch;
        return fn(url, { method: 'GET' }).then(function(r){
            if(!r.ok){ throw new Error('HTTP ' + r.status); }
            return r.json();
        });
    }

    function requestReaction(postId, type, method){
        var fn = (typeof window.fetchWithCsrf === 'function') ? window.fetchWithCsrf : fetch;
        var url = '/reactions?postId=' + encodeURIComponent(postId) + '&type=' + encodeURIComponent(type);
        return fn(url, { method: method });
    }

    function renderSummary(container, summary){
        var counts = summary && summary.counts ? summary.counts : {};
        var userTypes = summary && summary.userReactedTypes ? summary.userReactedTypes : [];
        var usersByType = summary && summary.usersByType ? summary.usersByType : {};
        var total = summary && typeof summary.total === 'number' ? summary.total : 0;

        toArray(container.querySelectorAll('.reaction-btn')).forEach(function(btn){
            var type = btn.getAttribute('data-type');
            var count = counts[type] || 0;
            var names = usersByType[type] || [];
            var reacted = Array.isArray(userTypes) ? userTypes.indexOf(type) !== -1 : false;
            var countEl = btn.querySelector('.count');
            if(countEl) countEl.textContent = count;
            btn.classList.toggle('active', reacted);
            btn.setAttribute('title', names.length ? names.join(', ') : '');
        });

        var totalEl = container.querySelector('.reaction-total');
        if(totalEl){ totalEl.textContent = total + (total > 1 ? ' réactions' : ' réaction'); }
    }

    function refresh(container){
        var postId = container.getAttribute('data-post-id');
        if(!postId) return;
        fetchJson('/reactions/summary?postId=' + encodeURIComponent(postId))
            .then(function(data){ renderSummary(container, data); })
            .catch(function(err){ console.error('reactions summary error', err); });
    }

    function bind(container){
        refresh(container);
        container.addEventListener('click', function(e){
            var btn = e.target.closest && e.target.closest('.reaction-btn');
            if(!btn || !container.contains(btn)) return;
            e.preventDefault();
            var type = btn.getAttribute('data-type');
            var postId = container.getAttribute('data-post-id');
            if(!type || !postId) return;
            var isActive = btn.classList.contains('active');
            btn.disabled = true;
            requestReaction(postId, type, isActive ? 'DELETE' : 'POST')
                .then(function(resp){
                    if(resp.ok || resp.status === 409){
                        refresh(container);
                    } else if(resp.status === 403){
                        alert('Session expirée, veuillez vous reconnecter.');
                    } else {
                        console.error('reaction error', resp.status);
                        alert('Impossible de mettre à jour la reaction');
                    }
                })
                .catch(function(err){ console.error('reaction network error', err); alert('Erreur reseau'); })
                .finally(function(){ btn.disabled = false; });
        });
    }

    document.addEventListener('DOMContentLoaded', function(){
        var bars = toArray(document.querySelectorAll('.reactions-bar'));
        bars.forEach(bind);
    });
})();
