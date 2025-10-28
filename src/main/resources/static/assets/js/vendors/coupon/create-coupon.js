(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const form = document.getElementById('couponForm');
    const codeEl = document.getElementById('code');
    const typeEl = document.getElementById('discountType');
    const valueEl = document.getElementById('value');
    const minEl = document.getElementById('minOrderAmount');
    const expEl = document.getElementById('expiredAt'); // type=datetime-local

    form?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const body = {
            code: (codeEl.value || '').trim(),
            discountType: typeEl.value,
            value: Number(valueEl.value),
            minOrderAmount: Number(minEl.value || 0),
            expiredAt: new Date(expEl.value).toISOString()
        };
        const res = await fetch(`${BASE}/api/shop/coupons`, {
            method: 'POST',
            headers: {'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json'},
            body: JSON.stringify(body)
        });
        if (!res.ok) return alert(await res.text());
        window.location.href = `${BASE}/shop/coupon/coupons`;
    });
})();
