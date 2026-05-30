document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('eventPaymentForm');
    if (!form) {
        return;
    }

    const walletBalance = parseFloat(document.getElementById('metaWalletBalance')?.value || '0') || 0;
    const eventPrice = parseFloat(document.getElementById('metaEventPrice')?.value || '0') || 0;

    const modeRadios = form.querySelectorAll('input[name="paymentMode"]');
    const walletSection = document.getElementById('walletPaySection');
    const savedSection = document.getElementById('savedCardSection');
    const newSection = document.getElementById('newCardSection');
    const savedSelect = document.getElementById('savedMethodSelect');
    const walletHint = document.getElementById('walletPayHint');

    if (walletHint) {
        walletHint.textContent = 'Solde disponible : ' + walletBalance.toFixed(2) + ' EUR. Montant de l\'événement : ' + eventPrice.toFixed(2) + ' EUR.';
    }

    function updateModeUi() {
        const savedRadio = document.getElementById('modeSavedRadio');
        const hasSavedCards = savedSelect && savedSelect.options && savedSelect.options.length > 0;

        if (savedRadio) {
            savedRadio.disabled = !hasSavedCards;
            if (!hasSavedCards && savedRadio.checked) {
                savedRadio.checked = false;
                const newR = form.querySelector('input[name="paymentMode"][value="new"]');
                if (newR) {
                    newR.checked = true;
                }
            }
        }
        const savedTile = savedRadio ? savedRadio.closest('.payment-mode-tile') : null;
        if (savedTile) {
            savedTile.style.opacity = hasSavedCards ? '1' : '0.45';
            savedTile.style.pointerEvents = hasSavedCards ? '' : 'none';
        }

        const mode = form.querySelector('input[name="paymentMode"]:checked')?.value || 'new';
        if (walletSection) walletSection.style.display = mode === 'wallet' ? 'block' : 'none';
        if (savedSection) savedSection.style.display = mode === 'saved' ? 'block' : 'none';
        if (newSection) newSection.style.display = mode === 'new' ? 'block' : 'none';
        const wRadio = document.getElementById('modeWalletRadio');
        const walletTile = wRadio ? wRadio.closest('.payment-mode-tile') : null;
        if (walletTile) {
            walletTile.style.opacity = eventPrice > 0 && walletBalance >= eventPrice ? '1' : '0.45';
            walletTile.style.pointerEvents = eventPrice <= 0 || walletBalance < eventPrice ? 'none' : '';
        }
        if (wRadio) {
            wRadio.disabled = eventPrice <= 0 || walletBalance < eventPrice;
        }
    }

    modeRadios.forEach((r) => r.addEventListener('change', updateModeUi));

    async function loadSavedMethods() {
        if (!savedSelect) return;
        savedSelect.innerHTML = '';
        try {
            const r = await fetch('/api/user/payment-methods', { headers: { Accept: 'application/json' } });
            if (!r.ok) {
                updateModeUi();
                return;
            }
            const list = await r.json();
            const savedRadio = document.getElementById('modeSavedRadio');
            if (!Array.isArray(list) || list.length === 0) {
                if (savedRadio) {
                    savedRadio.disabled = true;
                    savedRadio.checked = false;
                }
                const newR = form.querySelector('input[name="paymentMode"][value="new"]');
                if (newR) newR.checked = true;
                updateModeUi();
                return;
            }
            if (savedRadio) savedRadio.disabled = false;
            list.forEach((m) => {
                const opt = document.createElement('option');
                opt.value = m.id;
                opt.textContent = m.displayLabel || ('****' + m.last4);
                savedSelect.appendChild(opt);
            });
        } catch (e) {
            console.error(e);
        }
        updateModeUi();
    }

    loadSavedMethods();
    updateModeUi();

    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

    form.addEventListener('submit', handleEventPayment);
});

async function handleEventPayment(event) {
    event.preventDefault();

    const form = document.getElementById('eventPaymentForm');
    const eventId = document.getElementById('paymentEventId').value;
    const returnTo = document.getElementById('paymentReturnTo').value || '/feed';
    const mode = form.querySelector('input[name="paymentMode"]:checked')?.value || 'new';

    const payload = { returnTo };

    if (mode === 'wallet') {
        payload.useWalletBalance = true;
    } else if (mode === 'saved') {
        const sel = document.getElementById('savedMethodSelect');
        const cvv = document.getElementById('savedCvv')?.value?.trim() || '';
        if (!sel || !sel.value) {
            showPaymentAlert('Aucune carte enregistrée. Ajoutez-en une depuis votre profil.', 'error');
            return;
        }
        if (!cvv) {
            showPaymentAlert('Saisissez le CVV.', 'error');
            return;
        }
        payload.savedPaymentMethodId = sel.value;
        payload.cvv = cvv;
    } else {
        payload.cardholderName = document.getElementById('cardholderName').value.trim();
        payload.cardNumber = document.getElementById('cardNumber').value.trim();
        payload.expiryMonth = String(document.getElementById('expiryMonth').value).trim();
        payload.expiryYear = String(document.getElementById('expiryYear').value).trim();
        payload.cvv = document.getElementById('cvv').value.trim();
        if (!payload.cardholderName || !payload.cardNumber || !payload.expiryMonth || !payload.expiryYear || !payload.cvv) {
            showPaymentAlert('Remplissez tous les champs de la carte.', 'error');
            return;
        }
        if (document.getElementById('savePaymentMethod')?.checked) {
            payload.savePaymentMethod = true;
        }
    }

    try {
        const response = await fetch(`/api/event/${eventId}/payment`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            },
            body: JSON.stringify(payload)
        });

        const text = await response.text();
        let data = {};
        try {
            data = text ? JSON.parse(text) : {};
        } catch (parseErr) {
            console.error(parseErr);
            showPaymentAlert('Réponse du serveur illisible.', 'error');
            return;
        }

        if (!response.ok || !data.success) {
            showPaymentAlert(data.message || 'Le paiement a échoué.', 'error');
            return;
        }

        const target = data.redirectUrl || '/feed?payment=success';
        window.location.href = target;
    } catch (error) {
        console.error('Payment error:', error);
        showPaymentAlert('Une erreur est survenue pendant le paiement.', 'error');
    }
}

function showPaymentAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} active`;
    alertDiv.textContent = message;
    document.body.insertBefore(alertDiv, document.body.firstChild);

    setTimeout(() => {
        alertDiv.remove();
    }, 3500);
}
