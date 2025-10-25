(function () {
    const PF = window.ProductForm;
    const {BASE, bindMediaPicker} = PF;
    const collectDataOrThrow = PF.collectDataOrThrow.bind(PF);
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    // lấy pid từ query string
    const params = new URLSearchParams(window.location.search);
    const pid = Number(params.get('pid'));
    if (!pid) {
        alert('Product ID is missing');
        history.back();
        return;
    }

    bindMediaPicker();

    // Load product hiện tại
    (async function load() {
        try {
            const res = await fetch(`${BASE}/api/shop/products/${pid}`, {
                headers: {'Authorization': 'Bearer ' + token}
            });
            if (!res.ok) throw new Error(await res.text());
            const p = await res.json();
            fillForm(p);
        } catch (err) {
            alert(err.message || 'Load product failed');
            history.back();
        }
    })();

    // Lưu thay đổi
    document.getElementById('btnSubmit')?.addEventListener('click', async (e) => {
        e.preventDefault();
        const btn = e.currentTarget;
        try {
            // Khi edit: KHÔNG bắt buộc thêm ảnh mới (requireImage=false)
            const {data, files} = collectDataOrThrow(false);
            const fd = new FormData();
            fd.append('data', new Blob([JSON.stringify(data)], {type: 'application/json'}));
            [...files].forEach(f => fd.append('files', f));

            btn.disabled = true;
            btn.textContent = 'Saving...';
            const res = await fetch(`${BASE}/api/shop/products/${pid}`, {
                method: 'PUT',
                headers: {'Authorization': 'Bearer ' + token},
                body: fd
            });
            if (!res.ok) throw new Error(await res.text());
            alert('Saved!');
            window.location.href = BASE + '/shop/product/products';
        } catch (err) {
            alert(err.message || 'Save failed');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Save Changes';
        }
    });
})();
