(function () {
    const PF = window.__PromoForm;
    const BASE = PF?.BASE || '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const params = new URLSearchParams(window.location.search);
    const id = params.get('id');
    if (!id) {
        alert('Missing promotion id');
        history.back();
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

    async function loadDetail() {
        const r = await authFetch(`${BASE}/shop/promotions/${id}`);
        if (!r.ok) throw new Error(await r.text());
        const vm = await r.json();

        titleEl.value = vm.title || '';
        descEl.value = vm.description || '';
        disEl.value = vm.discountPercent ?? '';
        sEl.value = (vm.startDate || '').substring(0, 10);
        eEl.value = (vm.endDate || '').substring(0, 10);
        // set category sau khi options đã load
        if (vm.applyCategoryId != null) PF.setCategoryValue(vm.applyCategoryId);
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
        btn.textContent = 'Saving...';

        try {
            const res = await authFetch(`${BASE}/shop/promotions/${id}`, {
                method: 'PUT',
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error(await res.text());
            showAlert('Saved!', true);
            setTimeout(() => window.location.href = `${BASE}/shop/promotion/promotions`, 300);
        } catch (err2) {
            showAlert(err2?.message || 'Save failed');
        } finally {
            btn.disabled = false;
            btn.textContent = old || 'Save Changes';
        }
    });

    // init: đợi categories sẵn rồi mới fill detail (đảm bảo chọn đúng option)
    PF.loadCategories()
        .then(loadDetail)
        .catch(err => {
            alert(err?.message || 'Load failed');
            history.back();
        });
})();
