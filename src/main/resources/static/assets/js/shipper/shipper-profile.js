(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    async function loadProfile() {
        try {
            const res = await fetch(`${BASE}/api/shipper/me`, {
                headers: {'Authorization': `Bearer ${token}`}
            });
            if (!res.ok) {
                const t = await res.text();
                alert('Load profile failed: ' + t);
                return;
            }
            const d = await res.json();
            document.getElementById('fullName').textContent = d.fullName ?? '—';
            document.getElementById('email').textContent = d.email ?? '—';
            document.getElementById('companyName').textContent = d.companyName ?? '—';
            document.getElementById('phone').textContent = d.phone ?? '—';
            document.getElementById('address').textContent = d.address ?? '—';
            document.getElementById('providerId').textContent = d.shippingProviderId ?? '—';
            document.getElementById('providerName').textContent = d.shippingProviderName ?? '—';
        } catch (e) {
            alert('Load profile error: ' + e);
        }
    }

    loadProfile();
})();
