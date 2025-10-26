(function () {
    const PF = window.__PromoForm;
    const BASE = PF?.BASE || '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const titleEl = document.getElementById('promoTitle');
    const descEl = document.getElementById('promoDescription');
    const disEl = document.getElementById('promoDiscount');
    const sEl = document.getElementById('promoStart');
    const eEl = document.getElementById('promoEnd');
    const catSel = document.getElementById('promoCategory');
    const alertEl = document.getElementById('promoAlert');
    const btn = document.getElementById('btnSubmit');

    function showAlert(msg, ok = false) {
        if (!alertEl) return;
        alertEl.className = `alert ${ok ? 'alert-success' : 'alert-danger'}`;
        alertEl.textContent = msg;
        alertEl.classList.remove('d-none');
    }

    function validate() {
        const title = (titleEl.value || '').trim();
        const dp = parseFloat(disEl.value);
        const sd = sEl.value;
        const ed = eEl.value;
        if (!title) return 'Title is required';
        if (isNaN(dp) || dp < 0 || dp > 100) return 'Discount must be between 0 and 100';
        if (!sd || !ed) return 'Start/End date is required';
        if (new Date(sd) > new Date(ed)) return 'Start date must be before or equal End date';
        return null;
    }

    function authFetch(url, opt = {}) {
        return fetch(url, {
            ...opt,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token,
                ...(opt.headers || {})
            }
        });
    }

    btn?.addEventListener('click', async (e) => {
        e.preventDefault();
        alertEl?.classList.add('d-none');

        const err = validate();
        if (err) {
            showAlert(err);
            return;
        }

        const payload = {
            title: (titleEl.value || '').trim(),
            description: descEl.value || '',
            discountPercent: parseFloat(disEl.value),
            startDate: sEl.value,
            endDate: eEl.value,
            applyCategoryId: (catSel.value || '') === '' ? null : Number(catSel.value)
        };

        btn.disabled = true;
        const old = btn.textContent;
        btn.textContent = 'Creating...';

        try {
            const res = await authFetch(`${BASE}/shop/promotions`, {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error(await res.text());
            showAlert('Created!', true);
            setTimeout(() => window.location.href = `${BASE}/shop/promotion/promotions`, 300);
        } catch (err2) {
            showAlert(err2?.message || 'Create failed');
        } finally {
            btn.disabled = false;
            btn.textContent = old || 'Create Promotion';
        }
    });
})();
