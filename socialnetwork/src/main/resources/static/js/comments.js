(function(){
    function toArray(nl){ return Array.prototype.slice.call(nl || []); }
    var fn = (typeof window.fetchWithCsrf === 'function') ? window.fetchWithCsrf : fetch;

    function fetchComments(postId){
        return fn('/comments?postId=' + encodeURIComponent(postId), { method: 'GET' })
            .then(function(r){ if(!r.ok) throw new Error('HTTP ' + r.status); return r.json(); });
    }

    function postComment(postId, content, parentCommentId){
        return fn('/comments', {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({ postId: postId, content: content, parentCommentId: parentCommentId || null })
        }).then(function(r){
            if(!r.ok){ return r.text().then(function(t){ throw new Error('HTTP ' + r.status + ' ' + t); }); }
            return r.json();
        });
    }

    function updateComment(commentId, content){
        return fn('/comments/' + encodeURIComponent(commentId), {
            method: 'PUT',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({ content: content })
        }).then(function(r){
            if(!r.ok){ return r.text().then(function(t){ throw new Error('HTTP ' + r.status + ' ' + t); }); }
            return r.json();
        });
    }

    function deleteComment(commentId){
        return fn('/comments/' + encodeURIComponent(commentId), { method: 'DELETE' })
            .then(function(r){ if(!r.ok) throw new Error('HTTP ' + r.status); return r; });
    }

    function formatDate(dateStr){
        if(!dateStr) return '';
        var d = new Date(dateStr);
        var now = new Date();
        var diff = Math.floor((now - d) / 1000);
        if(diff < 60) return 'A l\'instant';
        if(diff < 3600) return Math.floor(diff/60) + ' min';
        if(diff < 86400) return Math.floor(diff/3600) + ' h';
        return d.toLocaleDateString('fr-FR');
    }

    function buildCommentHtml(comment, currentUserId, level){
        level = level || 0;
        var isOwner = currentUserId && comment.authorId === currentUserId;
        var indent = level > 0 ? 'style="margin-left:' + (level * 30) + 'px;"' : '';
        var html = '<div class="comment-item" data-comment-id="' + comment.id + '" ' + indent + '>';
        html += '<img src="' + (comment.authorAvatar || '/img/default-avatar.png') + '" class="avatar-tiny">';
        html += '<div class="comment-body">';
        html += '<div class="comment-header">';
        html += '<strong>' + (comment.authorUsername || 'Utilisateur') + '</strong>';
        html += '<span class="comment-date">' + formatDate(comment.createdAt) + '</span>';
        html += '</div>';
        html += '<div class="comment-content">' + escapeHtml(comment.content) + '</div>';
        html += '<div class="comment-actions">';
        html += '<button class="btn-reply" data-comment-id="' + comment.id + '">Repondre</button>';
        if(isOwner){
            html += '<button class="btn-edit-comment" data-comment-id="' + comment.id + '">Modifier</button>';
            html += '<button class="btn-delete-comment" data-comment-id="' + comment.id + '">Supprimer</button>';
        }
        html += '</div>';
        html += '</div>';
        html += '</div>';
        if(comment.replies && comment.replies.length > 0){
            comment.replies.forEach(function(r){ html += buildCommentHtml(r, currentUserId, level + 1); });
        }
        return html;
    }

    function escapeHtml(text){
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function renderComments(container, comments, currentUserId){
        var list = container.querySelector('.comments-list');
        if(!list) return;
        var html = '';
        comments.forEach(function(c){ html += buildCommentHtml(c, currentUserId, 0); });
        list.innerHTML = html;
        if(typeof lucide !== 'undefined') lucide.createIcons();
    }

    function refreshComments(container, currentUserId){
        var postId = container.getAttribute('data-post-id');
        if(!postId) return;
        fetchComments(postId)
            .then(function(data){
                renderComments(container, data, currentUserId);
                updateCommentCount(postId, countAllComments(data));
            })
            .catch(function(err){ console.error('fetch comments error', err); });
    }

    function countAllComments(comments){
        var count = comments.length;
        comments.forEach(function(c){ if(c.replies) count += countAllComments(c.replies); });
        return count;
    }

    function updateCommentCount(postId, count){
        var btn = document.querySelector('.btn-toggle-comments[data-post-id="' + postId + '"]');
        if(btn){
            var span = btn.querySelector('.comment-count');
            if(span) span.textContent = count;
        }
    }

    function showReplyForm(parentCommentId, container){
        var existingForm = container.querySelector('.reply-form[data-parent-id="' + parentCommentId + '"]');
        if(existingForm){ existingForm.remove(); return; }
        var commentItem = container.querySelector('.comment-item[data-comment-id="' + parentCommentId + '"]');
        if(!commentItem) return;
        var form = document.createElement('div');
        form.className = 'reply-form';
        form.setAttribute('data-parent-id', parentCommentId);
        form.innerHTML = '<textarea class="comment-input" placeholder="Votre reponse..." rows="1"></textarea>' +
            '<button type="button" class="btn-send-reply"><i data-lucide="send"></i></button>' +
            '<button type="button" class="btn-cancel-reply">Annuler</button>';
        commentItem.parentNode.insertBefore(form, commentItem.nextSibling);
        if(typeof lucide !== 'undefined') lucide.createIcons();
        form.querySelector('.btn-send-reply').addEventListener('click', function(e){
            e.preventDefault();
            var input = form.querySelector('.comment-input');
            var content = input.value.trim();
            if(!content){ alert('Le commentaire ne peut pas etre vide'); return; }
            var postId = container.getAttribute('data-post-id');
            postComment(postId, content, parentCommentId)
                .then(function(){ form.remove(); refreshComments(container, getCurrentUserId()); })
                .catch(function(err){ console.error('reply error', err); alert('Erreur lors de la reponse: ' + err.message); });
        });
        form.querySelector('.btn-cancel-reply').addEventListener('click', function(e){ e.preventDefault(); form.remove(); });
    }

    function showEditForm(commentId, container){
        var commentItem = container.querySelector('.comment-item[data-comment-id="' + commentId + '"]');
        if(!commentItem) return;
        var contentDiv = commentItem.querySelector('.comment-content');
        if(!contentDiv) return;
        var oldContent = contentDiv.textContent;
        contentDiv.innerHTML = '<textarea class="comment-input">' + escapeHtml(oldContent) + '</textarea>' +
            '<button class="btn-save-edit">Enregistrer</button>' +
            '<button class="btn-cancel-edit">Annuler</button>';
        if(typeof lucide !== 'undefined') lucide.createIcons();
        contentDiv.querySelector('.btn-save-edit').addEventListener('click', function(e){
            e.preventDefault();
            var newContent = contentDiv.querySelector('.comment-input').value.trim();
            if(!newContent){ alert('Le commentaire ne peut pas etre vide'); return; }
            updateComment(commentId, newContent)
                .then(function(){ refreshComments(container, getCurrentUserId()); })
                .catch(function(err){ console.error('update error', err); alert('Erreur lors de la modification: ' + err.message); });
        });
        contentDiv.querySelector('.btn-cancel-edit').addEventListener('click', function(e){
            e.preventDefault();
            contentDiv.textContent = oldContent;
        });
    }

    function getCurrentUserId(){
        var meta = document.querySelector('meta[name="userId"]');
        return meta ? meta.getAttribute('content') : null;
    }

    document.addEventListener('DOMContentLoaded', function(){
        var currentUserId = getCurrentUserId();

        toArray(document.querySelectorAll('.btn-toggle-comments')).forEach(function(btn){
            btn.addEventListener('click', function(){
                var postId = btn.getAttribute('data-post-id');
                var section = document.querySelector('.comments-section[data-post-id="' + postId + '"]');
                if(!section) return;
                if(section.style.display === 'none'){
                    section.style.display = 'block';
                    refreshComments(section, currentUserId);
                } else {
                    section.style.display = 'none';
                }
            });
        });

        toArray(document.querySelectorAll('.comments-section')).forEach(function(section){
            var postId = section.getAttribute('data-post-id');
            var sendBtn = section.querySelector('.btn-send-comment');
            if(sendBtn){
                sendBtn.setAttribute('type','button');
                sendBtn.addEventListener('click', function(e){
                    e.preventDefault();
                    var input = section.querySelector('.comment-input');
                    var content = input.value.trim();
                    if(!content){ alert('Le commentaire ne peut pas etre vide'); return; }
                    postComment(postId, content, null)
                        .then(function(){ input.value = ''; refreshComments(section, currentUserId); })
                        .catch(function(err){ console.error('post comment error', err); alert('Erreur lors de l\'envoi: ' + err.message); });
                });
            }

            section.addEventListener('click', function(e){
                var replyBtn = e.target.closest('.btn-reply');
                if(replyBtn){
                    e.preventDefault();
                    var commentId = replyBtn.getAttribute('data-comment-id');
                    showReplyForm(commentId, section);
                    return;
                }

                var editBtn = e.target.closest('.btn-edit-comment');
                if(editBtn){
                    e.preventDefault();
                    var commentId = editBtn.getAttribute('data-comment-id');
                    showEditForm(commentId, section);
                    return;
                }

                var deleteBtn = e.target.closest('.btn-delete-comment');
                if(deleteBtn){
                    e.preventDefault();
                    if(!confirm('Voulez-vous vraiment supprimer ce commentaire ?')) return;
                    var commentId = deleteBtn.getAttribute('data-comment-id');
                    deleteComment(commentId)
                        .then(function(){ refreshComments(section, currentUserId); })
                        .catch(function(err){ console.error('delete error', err); alert('Erreur lors de la suppression'); });
                    return;
                }
            });

            // Initial fetch to populate counts and cache comments (even if hidden)
            refreshComments(section, currentUserId);
        });
    });
})();
