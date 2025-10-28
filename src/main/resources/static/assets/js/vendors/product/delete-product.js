(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) return;

    const tbody = document.getElementById('productsTbody');
    if (!tbody) return;

    tbody.addEventListener('click', async (e) => {
        const a = e.target.closest('.btn-delete');
        if (!a) return;
        e.preventDefault();

        const id = a.getAttribute('data-id');
        if (!id) return;

        if (!confirm('Are you sure to delete this product?')) return;

        try {
            const res = await fetch(`${BASE}/api/shop/products/${id}`, {
                method: 'DELETE',
                headers: {'Authorization': 'Bearer ' + token}
            });
            if (res.status === 204) {
                const row = a.closest('tr');
                if (row) row.remove();
            } else {
                const msg = await res.text();
                alert('Delete failed: ' + msg);
            }
        } catch (err) {
            console.error(err);
            alert('Có lỗi khi xoá sản phẩm.');
        }
    });
})();