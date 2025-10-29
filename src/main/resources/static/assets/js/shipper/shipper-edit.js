(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    // Elements
    const providerSelect = document.getElementById('providerSelect');
    const companyInput = document.getElementById('companyInput');
    const phoneInput = document.getElementById('phoneInput');

    // address parts
    const provinceSelect = document.getElementById('provinceSelect');
    const districtSelect = document.getElementById('districtSelect');
    const wardSelect = document.getElementById('wardSelect');
    const detailInput = document.getElementById('detailInput');

    const form = document.getElementById('editForm');

    // helpers (giống shop-information.js)
    const getSelectedText = (sel) =>
        sel?.selectedOptions?.[0]
            ? (sel.selectedOptions[0].getAttribute('data-name') || sel.selectedOptions[0].textContent || '').trim()
            : '';

    function buildAddressString() {
        const parts = [
            (detailInput.value || '').trim(),
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

    async function initLocationCombos() {
        // provinces
        const provinces = await window.LocationRender.fetchProvinces();
        provinces.forEach(p => {
            const opt = document.createElement('option');
            opt.value = p.code || p.name || p.id;
            opt.textContent = p.name;
            opt.setAttribute('data-name', p.name);
            provinceSelect.appendChild(opt);
        });

        provinceSelect.addEventListener('change', async () => {
            districtSelect.disabled = true;
            wardSelect.disabled = true;
            districtSelect.innerHTML = '<option value="">-- District --</option>';
            wardSelect.innerHTML = '<option value="">-- Ward --</option>';

            const pName = getSelectedText(provinceSelect);
            if (!pName) return;

            const districts = await window.LocationRender.fetchDistricts(pName);
            districts.forEach(d => {
                const opt = document.createElement('option');
                opt.value = d.code || d.name || d.id;
                opt.textContent = d.name;
                opt.setAttribute('data-name', d.name);
                districtSelect.appendChild(opt);
            });
            districtSelect.disabled = false;
        });

        districtSelect.addEventListener('change', async () => {
            wardSelect.disabled = true;
            wardSelect.innerHTML = '<option value="">-- Ward --</option>';

            const pName = getSelectedText(provinceSelect);
            const dName = getSelectedText(districtSelect);
            if (!pName || !dName) return;

            const wards = await window.LocationRender.fetchWards(pName, dName);
            wards.forEach(w => {
                const opt = document.createElement('option');
                opt.value = w.code || w.name || w.id;
                opt.textContent = w.name;
                opt.setAttribute('data-name', w.name);
                wardSelect.appendChild(opt);
            });
            wardSelect.disabled = false;
        });
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

            await loadProviders(d.shippingProviderId);
            await initLocationCombos();

            // Prefill combobox từ địa chỉ string có sẵn
            if (window.prefillFromExisting && d.address) {
                window.prefillFromExisting(d.address, {
                    provinceSelect,
                    districtSelect,
                    wardSelect,
                    detailInput
                });
            }
        } catch (e) {
            console.error(e);
        }
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            shippingProviderId: providerSelect.value ? Number(providerSelect.value) : null,
            companyName: companyInput.value.trim(),
            phone: phoneInput.value.trim(),
            address: buildAddressString() // GHÉP từ text, không phải code
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
