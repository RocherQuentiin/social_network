(function () {
    const modal = document.getElementById('paymentMethodsModal');
    const openBtn = document.getElementById('btn-manage-payment-methods');
    const closeBtn = document.getElementById('closePaymentMethodsModal');
    const listEl = document.getElementById('payment-methods-list');
    const form = document.getElementById('addPaymentMethodForm');

    function openM() {
        if (modal) {
            modal.style.display = 'flex';
            loadList();
        }
    }

    function closeM() {
        if (modal) modal.style.display = 'none';
    }

    if (openBtn) openBtn.addEventListener('click', openM);
    if (closeBtn) closeBtn.addEventListener('click', closeM);
    if (modal) {
        modal.addEventListener('click', function (e) {
            if (e.target === modal) closeM();
        });
    }

    async function loadList() {
        if (!listEl) return;
        listEl.innerHTML = '<span style="opacity:0.7">Chargement…</span>';
        try {
            const r = await fetch('/api/user/payment-methods', { headers: { Accept: 'application/json' } });
            if (r.status === 401) {
                listEl.innerHTML = '<span style="color:#f66">Non connecté.</span>';
                return;
            }
            const items = await r.json();
            if (!Array.isArray(items) || items.length === 0) {
                listEl.innerHTML = '<span style="opacity:0.7">Aucune carte enregistrée.</span>';
                return;
            }
            listEl.innerHTML = '';
            items.forEach(function (it) {
                const row = document.createElement('div');
                row.className = 'card payment-method-row';
                row.innerHTML = '<div class="payment-method-row-info"><strong>' + escapeHtml(it.displayLabel || ('****' + it.last4)) + '</strong><span class="payment-method-row-meta">' +
                    escapeHtml(it.cardholderName) + ' · exp. ' + escapeHtml(it.expiryMonth) + '/' + escapeHtml(it.expiryYear) + '</span></div>';
                const del = document.createElement('button');
                del.type = 'button';
                del.className = 'btn-delete-payment-method';
                del.innerHTML = '<i data-lucide="trash-2"></i><span>Supprimer</span>';
                del.addEventListener('click', function () {
                    if (!confirm('Supprimer ce moyen de paiement ?')) return;
                    fetch('/api/user/payment-methods/' + it.id, { method: 'DELETE' })
                        .then(function (dr) {
                            if (dr.ok) loadList();
                            else alert('Suppression impossible');
                        })
                        .catch(function () { alert('Erreur réseau'); });
                });
                row.appendChild(del);
                listEl.appendChild(row);
            });
            if (typeof lucide !== 'undefined') lucide.createIcons();
        } catch (e) {
            console.error(e);
            listEl.innerHTML = '<span style="color:#f66">Erreur de chargement.</span>';
        }
    }

    function escapeHtml(s) {
        if (!s) return '';
        const d = document.createElement('div');
        d.textContent = s;
        return d.innerHTML;
    }

    if (form) {
        form.addEventListener('submit', function (ev) {
            ev.preventDefault();
            const payload = {
                cardholderName: document.getElementById('pmCardholder').value.trim(),
                cardNumber: document.getElementById('pmCardNumber').value.trim(),
                expiryMonth: String(document.getElementById('pmMonth').value).trim(),
                expiryYear: String(document.getElementById('pmYear').value).trim(),
                cvv: document.getElementById('pmCvv').value.trim(),
                label: document.getElementById('pmLabel').value.trim() || null
            };
            fetch('/api/user/payment-methods', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
                body: JSON.stringify(payload)
            }).then(async function (r) {
                const data = await r.json().catch(function () { return {}; });
                if (!r.ok) {
                    alert(data.message || 'Enregistrement refusé.');
                    return;
                }
                form.reset();
                loadList();
            }).catch(function () { alert('Erreur réseau'); });
        });
    }
})();
