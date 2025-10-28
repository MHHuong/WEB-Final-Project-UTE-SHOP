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

    // ==== Load providers ====
    async function loadProviders() {
        // tuỳ theo API bạn đã có. Ví dụ: /api/shipping-providers (GET all)
        const res = await fetch(`${BASE}/api/shipping-providers`, {
            headers: {'Authorization': `Bearer ${token}`}
        });
        if (!res.ok) return;
        const list = await res.json();
        providerSelect.innerHTML = '<option value="">-- Chọn đơn vị vận chuyển --</option>';
        (list || []).forEach(p => {
            const opt = document.createElement('option');
            opt.value = p.shippingProviderId || p.id || p.providerId;
            opt.textContent = p.name;
            providerSelect.appendChild(opt);
        });
    }

    // ==== Location render (dựa script shop) ====
    // Giả sử render-location.js đã cung cấp fetchProvinces/districts/wards
    async function initLocation() {
        // Provinces
        const provinces = await window.LocationRender.fetchProvinces();
        provinces.forEach(p => {
            const opt = document.createElement('option');
            opt.value = p.name;
            opt.textContent = p.name;
            provinceSelect.appendChild(opt);
        });

        provinceSelect.addEventListener('change', async () => {
            districtSelect.disabled = true;
            wardSelect.disabled = true;
            districtSelect.innerHTML = '<option value="">-- Quận/Huyện --</option>';
            wardSelect.innerHTML = '<option value="">-- Phường/Xã --</option>';
            const name = provinceSelect.value;
            if (!name) return;
            const districts = await window.LocationRender.fetchDistricts(name);
            districts.forEach(d => {
                const opt = document.createElement('option');
                opt.value = d.name;
                opt.textContent = d.name;
                districtSelect.appendChild(opt);
            });
            districtSelect.disabled = false;
        });

        districtSelect.addEventListener('change', async () => {
            wardSelect.disabled = true;
            wardSelect.innerHTML = '<option value="">-- Phường/Xã --</option>';
            const p = provinceSelect.value, d = districtSelect.value;
            if (!p || !d) return;
            const wards = await window.LocationRender.fetchWards(p, d);
            wards.forEach(w => {
                const opt = document.createElement('option');
                opt.value = w.name;
                opt.textContent = w.name;
                wardSelect.appendChild(opt);
            });
            wardSelect.disabled = false;
        });
    }

    function buildAddressString() {
        const detail = (detailInput.value || '').trim();
        const ward = (wardSelect.value || '').trim();
        const district = (districtSelect.value || '').trim();
        const province = (provinceSelect.value || '').trim();
        const parts = [detail, ward, district, province].filter(Boolean);
        return parts.join(', ');
    }

    registerBtn.addEventListener('click', async () => {
        const shippingProviderId = providerSelect.value;
        if (!shippingProviderId) {
            alert('Chọn đơn vị vận chuyển');
            return;
        }
        const phone = (phoneInput.value || '').trim();
        const companyName = (companyInput.value || '').trim();
        const address = buildAddressString();
        if (!address) {
            alert('Nhập địa chỉ đầy đủ');
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
        alert('Đăng ký thành công!');
        window.location.href = BASE + '/shipper/orders/confirmed';
    });

    loadProviders();
    initLocation();
})();
