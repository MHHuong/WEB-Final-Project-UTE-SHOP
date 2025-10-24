(function () {
    const BASE = '/UTE_SHOP'; // chỉnh nếu app chạy context khác
    const token = localStorage.getItem('authToken');

    if (!token) {
        alert('Vui lòng đăng nhập để tiếp tục');
        window.location.href = BASE + '/login';
        return;
    }

    const statusEl = document.getElementById('registerStatus');
    const setStatus = (m, ok = true) => {
        statusEl.textContent = m || '';
        statusEl.className = 'small ' + (ok ? 'text-muted' : 'text-danger');
    };

    // Nếu đã có shop -> đưa về profile
    fetch(BASE + '/api/auth/me', { headers: { 'Authorization': 'Bearer ' + token } })
        .then(r => r.ok ? r.json() : Promise.reject())
        .then(me => { if (me && me.shop) window.location.href = BASE + '/shop/account/profile'; })
        .catch(() => {});

    // ===== Nạp tỉnh/huyện/xã (đổi URL API nếu dự án bạn dùng path khác) =====
    const provinceEl = document.getElementById('provinceSelect');
    const districtEl = document.getElementById('districtSelect');
    const wardEl = document.getElementById('wardSelect');

    async function loadProvinces() {
        try {
            const res = await fetch(BASE + '/api/location/provinces');
            if (!res.ok) return;
            const data = await res.json();
            provinceEl.innerHTML = '<option value="">-- Chọn tỉnh --</option>' +
                data.map(p => `<option value="${p.code}">${p.name}</option>`).join('');
        } catch(_) {}
    }
    async function loadDistricts(provinceCode) {
        districtEl.disabled = true; wardEl.disabled = true;
        districtEl.innerHTML = '<option value="">-- Chọn huyện --</option>';
        wardEl.innerHTML = '<option value="">-- Chọn xã --</option>';
        if (!provinceCode) return;
        try {
            const res = await fetch(BASE + '/api/location/districts?provinceCode=' + encodeURIComponent(provinceCode));
            if (!res.ok) return;
            const data = await res.json();
            districtEl.innerHTML = '<option value="">-- Chọn huyện --</option>' +
                data.map(d => `<option value="${d.code}">${d.name}</option>`).join('');
            districtEl.disabled = false;
        } catch(_) {}
    }
    async function loadWards(districtCode) {
        wardEl.disabled = true;
        wardEl.innerHTML = '<option value="">-- Chọn xã --</option>';
        if (!districtCode) return;
        try {
            const res = await fetch(BASE + '/api/location/wards?districtCode=' + encodeURIComponent(districtCode));
            if (!res.ok) return;
            const data = await res.json();
            wardEl.innerHTML = '<option value="">-- Chọn xã --</option>' +
                data.map(w => `<option value="${w.code}">${w.name}</option>`).join('');
            wardEl.disabled = false;
        } catch(_) {}
    }
    provinceEl.addEventListener('change', e => loadDistricts(e.target.value));
    districtEl.addEventListener('change', e => loadWards(e.target.value));
    loadProvinces();

    // ===== Submit: tạo shop (không upload ảnh) rồi chuyển sang profile =====
    const form = document.getElementById('shopRegisterForm');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        setStatus('Đang tạo shop…');

        const body = {
            name: document.getElementById('shopName').value?.trim() || null,
            description: document.getElementById('shopDescription').value?.trim() || null,
            // BE đã merge theo tên field (setIfExists). Nếu entity lưu code thì gửi code; nếu lưu tên thì gửi name:
            province: provinceEl.selectedOptions[0]?.text || null,
            district: districtEl.selectedOptions[0]?.text || null,
            ward: wardEl.selectedOptions[0]?.text || null,
            addressDetail: document.getElementById('detailAddress').value?.trim() || null
        };

        try {
            const res = await fetch(BASE + '/api/shop/profile/upsert', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify(body)
            });
            if (!res.ok) {
                const t = await res.text();
                setStatus('Đăng ký thất bại: ' + t, false);
                return;
            }
            window.location.href = BASE + '/shop/account/profile';
        } catch (err) {
            setStatus('Có lỗi mạng. Vui lòng thử lại.', false);
        }
    });
})();