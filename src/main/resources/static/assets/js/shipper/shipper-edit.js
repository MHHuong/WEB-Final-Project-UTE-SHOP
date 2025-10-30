(async function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const providerSelect = document.getElementById('providerSelect');
    const companyInput = document.getElementById('companyInput');
    const phoneInput = document.getElementById('phoneInput');
    const provinceSelect = document.getElementById('provinceSelect');
    const districtSelect = document.getElementById('districtSelect');
    const wardSelect = document.getElementById('wardSelect');
    const detailAddress = document.getElementById('detailAddress');
    const addrHid = document.getElementById('address');
    const form = document.getElementById('editForm');

    const getSelectedText = (sel) =>
        sel?.selectedOptions?.[0]
            ? (sel.selectedOptions[0].getAttribute('data-name') || sel.selectedOptions[0].textContent || '').trim()
            : '';

    function buildAddressString() {
        const parts = [
            (detailAddress.value || '').trim(),
            getSelectedText(wardSelect),
            getSelectedText(districtSelect),
            getSelectedText(provinceSelect)
        ].filter(Boolean);
        return parts.join(', ');
    }

    async function loadProviders(selectedId) {
        try {
            const res = await fetch(`${BASE}/api/shipping-providers`, {
                headers: {'Authorization': `Bearer ${token}`}
            });
            if (!res.ok) return;
            const arr = await res.json();
            providerSelect.innerHTML = '<option value="">-- Choose provider --</option>'
                + arr.map(p => `<option value="${p.shippingProviderId || p.id || p.providerId}">${p.name}</option>`).join('');
            if (selectedId != null) providerSelect.value = String(selectedId);
        } catch (e) {
            console.error(e);
        }
    }

    async function loadProfile() {
        try {
            const res = await fetch(`${BASE}/api/shipper/me`, {
                headers: {'Authorization': `Bearer ${token}`}
            });
            if (!res.ok) return;
            const d = await res.json();

            companyInput.value = d.companyName || '';
            phoneInput.value = d.phone || '';
            addrHid.value = d.address || '';

            await loadProviders(d.shippingProviderId);

            // Gọi lại prefill để hiển thị location
            if (window.prefillFromExisting) {
                window.prefillFromExisting(addrHid.value);
            }
        } catch (e) {
            console.error(e);
        }
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const fullAddress = buildAddressString();
        addrHid.value = fullAddress;
        const payload = {
            shippingProviderId: providerSelect.value ? Number(providerSelect.value) : null,
            companyName: companyInput.value.trim(),
            phone: phoneInput.value.trim(),
            address: buildAddressString()
        };
        try {
            const res = await fetch(`${BASE}/api/shipper/me`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });
            if (!res.ok) {
                const t = await res.text();
                alert('Update failed: ' + t);
                return;
            }
            alert('Profile updated successfully');
            window.location.href = BASE + '/shipper';
        } catch (e2) {
            alert('Update error: ' + e2);
        }
    });

    loadProfile();
})();
