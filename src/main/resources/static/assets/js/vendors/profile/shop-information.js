(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const $ = id => document.getElementById(id);
    const nameEl = $('shopName');
    const emailEl = $('userEmail');
    const phoneEl = $('userPhone');
    const descEl = $('shopDescription');
    const addrHid = $('address');
    const detailEl = $('detailAddress');
    const provinceEl = $('provinceSelect');
    const districtEl = $('districtSelect');
    const wardEl = $('wardSelect');
    const imgPrev = $('shopLogoPreview');
    const statusEl = $('profileStatus');
    const saveBtn = $('saveBtn');
    const resetBtn = $('resetBtn');
    const avatarInp = $('avatarFile');

    const cardShopName = $('cardShopName');
    const cardShopShort = $('cardShopShortName');
    const cardShopAddress = $('cardShopAddress');
    const cardUserEmail = $('cardUserEmail');
    const cardUserPhone = $('cardUserPhone');

    const setStatus = (m, ok = true) => {
        if (!statusEl) return;
        statusEl.textContent = m || '';
        statusEl.className = 'small ' + (ok ? 'text-muted' : 'text-danger');
    };
    const getSelectedText = (sel) =>
        sel?.selectedOptions?.[0]
            ? (sel.selectedOptions[0].getAttribute('data-name') || sel.selectedOptions[0].textContent || '').trim()
            : '';

    function buildFullAddress() {
        const parts = [
            (detailEl?.value || '').trim(),
            getSelectedText(wardEl),
            getSelectedText(districtEl),
            getSelectedText(provinceEl)
        ].filter(Boolean);
        return parts.join(', ');
    }

    function applyShopToCard(shop, user) {
        cardShopName && (cardShopName.textContent = shop?.shopName || 'Shop Owner');
        cardShopShort && (cardShopShort.textContent = shop?.shopName || '—');
        cardShopAddress && (cardShopAddress.textContent = shop?.address || '—');
        cardUserEmail && (cardUserEmail.textContent = user?.email || '—');
        cardUserPhone && (cardUserPhone.textContent = (user?.phone || '').trim() || '—');
    }

    let original = null;

    async function loadMe() {
        setStatus('Đang tải dữ liệu…');
        try {
            const res = await fetch(BASE + '/api/auth/me', {headers: {'Authorization': 'Bearer ' + token}});
            if (res.status === 401) {
                window.location.href = BASE + '/login';
                return;
            }
            if (!res.ok) throw new Error('Fetch /api/auth/me failed');
            const me = await res.json();

            const user = me || {};
            const shop = me?.shop || null;
            if (!shop) {
                window.location.href = BASE + '/shop/account/shop-register';
                return;
            }

            nameEl && (nameEl.value = shop.shopName || '');
            descEl && (descEl.value = shop.description || '');
            emailEl && (emailEl.value = user.email || '');
            phoneEl && (phoneEl.value = (user.phone || '').trim());
            addrHid && (addrHid.value = shop.address || '');

            applyShopToCard(shop, user);

            if (imgPrev) {
                imgPrev.src = shop.hasLogo ? (BASE + '/api/shops/me/logo?ts=' + Date.now()) : (imgPrev.dataset.fallback || '');
            }

            original = {
                shopName: shop.shopName || '',
                description: shop.description || '',
                address: shop.address || '',
                hasLogo: !!shop.hasLogo,
                userEmail: user.email || '',
                userPhone: (user.phone || '').trim()
            };

            if (window.prefillFromExisting) {
                window.prefillFromExisting(addrHid.value);
            }

            setStatus('');
        } catch (e) {
            setStatus('Không tải được dữ liệu.', false);
        }
    }

    async function doSave() {
        if (!nameEl.value.trim()) {
            setStatus('Vui lòng nhập tên shop.', false);
            return;
        }

        const fullAddress = buildFullAddress();
        addrHid && (addrHid.value = fullAddress);

        const payload = {
            shopName: nameEl.value.trim(),
            description: descEl.value.trim() || null,
            address: fullAddress || null,
            phone: phoneEl.value.trim() || null
        };

        setStatus('Đang lưu…');
        saveBtn && (saveBtn.disabled = true);

        try {
            const res = await fetch(BASE + '/api/shops/me', {
                method: 'PUT',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });
            if (!res.ok) {
                const t = await res.text();
                throw new Error(t || 'Update failed');
            }
            const s = await res.json();

            // update card + state
            applyShopToCard(s, {email: original.userEmail, phone: payload.phone || original.userPhone});
            original = {
                shopName: s.shopName || '',
                description: s.description || '',
                address: s.address || '',
                hasLogo: !!s.hasLogo,
                userEmail: original.userEmail,
                userPhone: payload.phone || original.userPhone
            };

            setStatus('Đã lưu thay đổi.');
        } catch (err) {
            setStatus('Lưu thất bại: ' + (err.message || 'Lỗi không xác định'), false);
        } finally {
            saveBtn && (saveBtn.disabled = false);
        }
    }

    // ===== Reset =====
    function doReset() {
        if (!original) return;

        nameEl && (nameEl.value = original.shopName);
        descEl && (descEl.value = original.description);
        phoneEl && (phoneEl.value = original.userPhone || '');

        addrHid && (addrHid.value = original.address || '');
        detailEl && (detailEl.value = '');

        if (window.prefillFromExisting) {
            window.prefillFromExisting(original.address || '');
        }

        applyShopToCard(
            {shopName: original.shopName, address: original.address},
            {email: original.userEmail, phone: original.userPhone}
        );

        setStatus('Đã khôi phục dữ liệu ban đầu.');
    }

    async function uploadLogo(file) {
        if (!file) return;
        setStatus('Đang tải logo…');
        try {
            const fd = new FormData();
            fd.append('file', file);
            const res = await fetch(BASE + '/api/shops/me/logo', {
                method: 'POST',
                headers: {'Authorization': 'Bearer ' + token},
                body: fd
            });
            if (!res.ok) throw new Error(await res.text() || 'Upload failed');
            if (imgPrev) imgPrev.src = BASE + '/api/shops/me/logo?ts=' + Date.now();
            original && (original.hasLogo = true);
            setStatus('Đã cập nhật logo.');
            avatarInp && (avatarInp.value = '');
        } catch (e) {
            setStatus('Tải logo thất bại: ' + (e.message || ''), false);
        }
    }

    async function deleteLogo() {
        setStatus('Đang xóa logo…');
        try {
            const res = await fetch(BASE + '/api/shops/me/logo', {
                method: 'DELETE',
                headers: {'Authorization': 'Bearer ' + token}
            });
            if (!res.ok) throw new Error(await res.text() || 'Delete failed');
            if (imgPrev) imgPrev.src = imgPrev.dataset.fallback || '';
            original && (original.hasLogo = false);
            setStatus('Đã xóa logo.');
        } catch (e) {
            setStatus('Xóa logo thất bại: ' + (e.message || ''), false);
        }
    }

    saveBtn && saveBtn.addEventListener('click', e => {
        e.preventDefault();
        doSave();
    });
    resetBtn && resetBtn.addEventListener('click', e => {
        e.preventDefault();
        doReset();
    });
    avatarInp && avatarInp.addEventListener('change', e => uploadLogo(e.target.files?.[0]));
    window.deleteAvatar = (e) => {
        e?.preventDefault();
        deleteLogo();
        return false;
    };

    loadMe();
})();