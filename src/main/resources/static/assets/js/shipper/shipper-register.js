(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    // Elements
    const providerSelect = document.getElementById('providerSelect');
    const phoneInput = document.getElementById('phoneInput');
    const companyInput = document.getElementById('companyInput');
    const provinceSelect = document.getElementById('provinceSelect');
    const districtSelect = document.getElementById('districtSelect');
    const wardSelect = document.getElementById('wardSelect');
    const detailInput = document.getElementById('detailInput');
    const registerBtn = document.getElementById('registerBtn');

    // === helpers giống shop-information ===
    const getSelectedText = (sel) =>
        sel?.selectedOptions?.[0]
            ? (sel.selectedOptions[0].getAttribute('data-name') || sel.selectedOptions[0].textContent || '').trim()
            : '';

    function buildAddressString() {
        const detail = (detailInput.value || '').trim();
        const ward = getSelectedText(wardSelect);
        const district = getSelectedText(districtSelect);
        const province = getSelectedText(provinceSelect);
        const parts = [detail, ward, district, province].filter(Boolean);
        return parts.join(', ');
    }

    // ==== Load providers ====
    async function loadProviders() {
        const res = await fetch(`${BASE}/api/shipping-providers`, {
            headers: {'Authorization': `Bearer ${token}`}
        });
        if (!res.ok) return;
        const list = await res.json();
        providerSelect.innerHTML = '<option value="">-- Choose Shipping Provider --</option>';
        (list || []).forEach(p => {
            // value: id/code; hiển thị: name
            const opt = document.createElement('option');
            opt.value = p.shippingProviderId || p.id || p.providerId;
            opt.textContent = p.name;
            providerSelect.appendChild(opt);
        });
    }

    // ==== Khởi tạo combobox địa chỉ từ choose-location.js ====
    async function initLocation() {
        // provinces
        const provinces = await window.LocationRender.fetchProvinces();
        provinces.forEach(p => {
            const opt = document.createElement('option');
            // GIỮ value = code để đồng bộ với render-location chuẩn
            opt.value = p.code || p.name || p.id;
            opt.textContent = p.name;
            opt.setAttribute('data-name', p.name); // dùng text khi build địa chỉ
            provinceSelect.appendChild(opt);
        });

        provinceSelect.addEventListener('change', async () => {
            districtSelect.disabled = true;
            wardSelect.disabled = true;
            districtSelect.innerHTML = '<option value="">-- Quận/Huyện --</option>';
            wardSelect.innerHTML = '<option value="">-- Phường/Xã --</option>';

            const provinceName = getSelectedText(provinceSelect);
            if (!provinceName) return;

            const districts = await window.LocationRender.fetchDistricts(provinceName);
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
            wardSelect.innerHTML = '<option value="">-- Phường/Xã --</option>';

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

        // Cho phép prefill nếu cần (địa chỉ có sẵn)
        window.prefillFromExisting && window.prefillFromExisting('');
    }

    registerBtn.addEventListener('click', async () => {
        const shippingProviderId = providerSelect.value;
        if (!shippingProviderId) {
            alert('Choose shipping provider');
            return;
        }
        const phone = (phoneInput.value || '').trim();
        const companyName = (companyInput.value || '').trim();
        const address = buildAddressString();
        if (!address) {
            alert('Enter full address');
            return;
        }

        const payload = {shippingProviderId: Number(shippingProviderId), phone, companyName, address};
        const res = await fetch(`${BASE}/api/shipper/register`, {
            method: 'POST',
            headers: {'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        });
        if (!res.ok) {
            const t = await res.text();
            alert('Đăng ký thất bại: ' + t);
            return;
        }
        alert('Register successfully!');
        window.location.href = BASE + '/shipper/orders/confirmed';
    });

    loadProviders();
    initLocation();
})();
