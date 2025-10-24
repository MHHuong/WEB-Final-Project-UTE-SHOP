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
        if (!statusEl) return;
        statusEl.textContent = m || '';
        statusEl.className = 'small ' + (ok ? 'text-muted' : 'text-danger');
    };

    // Nếu đã có shop -> đưa về profile
    fetch(BASE + '/api/auth/me', {headers: {'Authorization': 'Bearer ' + token}})
        .then(r => r.ok ? r.json() : Promise.reject())
        .then(me => {
            if (me && me.shop) window.location.href = BASE + '/shop/account/profile';
        })
        .catch(() => { /* bỏ qua */
        });

    // ======= Nạp tỉnh/huyện/xã (đúng path: /api/locations/...) =======
    const provinceEl = document.getElementById('provinceSelect');
    const districtEl = document.getElementById('districtSelect');
    const wardEl = document.getElementById('wardSelect');

    const form = document.getElementById('shopRegisterForm');

    function getSelectedText(sel) {
        const opt = sel?.selectedOptions?.[0];
        return opt ? (opt.getAttribute('data-name') || opt.textContent || '').trim() : '';
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        setStatus('Đang tạo shop…');

        const shopName = document.getElementById('shopName')?.value?.trim() || '';
        const description = document.getElementById('shopDescription')?.value?.trim() || '';
        const detail = document.getElementById('detailAddress')?.value?.trim() || '';

        const provinceName = getSelectedText(provinceEl);
        const districtName = getSelectedText(districtEl);
        const wardName = getSelectedText(wardEl);

        const parts = [detail, wardName, districtName, provinceName].filter(Boolean);
        const address = parts.join(', ');

        if (!shopName) {
            setStatus('Vui lòng nhập tên shop.', false);
            return;
        }

        const payload = {shopName, description: description || null, address: address || null};

        try {
            const res = await fetch(BASE + '/api/shops/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify(payload)
            });

            if (!res.ok) {
                const t = await res.text();
                setStatus((res.status === 409 ? 'Tài khoản đã có shop. ' : '') + 'Đăng ký thất bại: ' + t, false);
                return;
            }

            window.location.href = BASE + '/shop/account/profile';
        } catch (err) {
            setStatus('Có lỗi mạng. Vui lòng thử lại.', false);
        }
    });
})();