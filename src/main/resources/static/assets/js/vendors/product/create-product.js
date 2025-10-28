(function () {
    const PF = window.ProductForm;
    const {BASE, bindMediaPicker} = PF;
    const collectDataOrThrow = PF.collectDataOrThrow.bind(PF);
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    bindMediaPicker();

    document.getElementById('btnSubmit')?.addEventListener('click', async (e) => {
        e.preventDefault();
        const btn = e.currentTarget;
        try {
            const {data, files} = collectDataOrThrow(true);
            const fd = new FormData();
            fd.append('data', new Blob([JSON.stringify(data)], {type: 'application/json'}));
            [...files].forEach(f => fd.append('files', f));
            btn.disabled = true;
            btn.textContent = 'Creating...';

            const res = await fetch(`${BASE}/api/shop/products`, {
                method: 'POST',
                headers: {'Authorization': 'Bearer ' + token},
                body: fd
            });
            if (!res.ok) throw new Error(await res.text());
            alert('Created!');
            window.location.href = BASE + '/shop/product/products';
        } catch (err) {
            alert(err.message || 'Create failed');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Create Product';
        }
    });
})();