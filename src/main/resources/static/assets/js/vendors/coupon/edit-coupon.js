(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const url = new URL(window.location.href);
    const id = url.searchParams.get('id');

    const form = document.getElementById('couponForm');
    const codeEl = document.getElementById('code');
    const typeEl = document.getElementById('discountType');
    const valueEl = document.getElementById('value');
    const minEl = document.getElementById('minOrderAmount');
    const expEl = document.getElementById('expiredAt');

    async function load() {
        const res = await fetch(`${BASE}/api/shop/coupons/${id}`, {
            headers: {'Authorization': 'Bearer ' + token}
        });
        if (!res.ok) {
            alert('Load coupon failed');
            return;
        }
        const it = await res.json();
        codeEl.value = it.code;
        typeEl.value = it.discountType;
        valueEl.value = it.value;
        minEl.value = it.minOrderAmount ?? 0;
        // convert ISO -> yyyy-MM-ddTHH:mm for input
        const d = new Date(it.expiredAt);
        expEl.value = d.toISOString().slice(0, 16);
    }

    form?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const body = {
            code: (codeEl.value || '').trim(),
            discountType: typeEl.value,
            value: Number(valueEl.value),
            minOrderAmount: Number(minEl.value || 0),
            expiredAt: new Date(expEl.value).toISOString()
        };
        const res = await fetch(`${BASE}/api/shop/coupons/${id}`, {
            method: 'PUT',
            headers: {'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json'},
            body: JSON.stringify(body)
        });
        if (!res.ok) return alert(await res.text());
        window.location.href = `${BASE}/shop/coupon/coupons`;
    });

    load();
})();
