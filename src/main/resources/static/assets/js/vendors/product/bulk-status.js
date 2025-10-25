(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) return;

    const tbody = document.getElementById('productsTbody');
    const chkAll = document.getElementById('checkAll');
    const bulkBar = document.getElementById('bulkBar');
    const bulkSel = document.getElementById('bulkStatus');
    const btnApply = document.getElementById('btnBulkApply');

    if (!tbody || !chkAll || !bulkBar || !bulkSel || !btnApply) return;

    // ---- helpers ----
    function getAllRowChecks() {
        return tbody.querySelectorAll('.row-check'); // nhớ render mỗi hàng có: <input class="row-check" data-id="...">
    }

    function getCheckedIds() {
        return [...tbody.querySelectorAll('.row-check:checked')]
            .map(cb => Number(cb.getAttribute('data-id')))
            .filter(Boolean);
    }

    function updateBulkBarVisibility() {
        const all = getAllRowChecks();
        const checked = tbody.querySelectorAll('.row-check:checked');
        const any = checked.length > 0;

        // Ẩn/hiện thanh bulk
        bulkBar.classList.toggle('d-none', !any);

        // CheckAll state
        chkAll.checked = any && checked.length === all.length;
        chkAll.indeterminate = checked.length > 0 && checked.length < all.length;

        // Nút Apply chỉ bật khi có chọn + có status hợp lệ
        btnApply.disabled = !(any && ['0', '1', '2', '3'].includes(bulkSel.value));
    }

    // ---- event bindings ----
    // Chọn tất cả
    chkAll.addEventListener('change', () => {
        getAllRowChecks().forEach(cb => cb.checked = chkAll.checked);
        updateBulkBarVisibility();
    });

    // Thay đổi lựa chọn hàng
    tbody.addEventListener('change', (e) => {
        if (e.target.matches('.row-check')) updateBulkBarVisibility();
    });

    // Chọn status trong bulk
    bulkSel.addEventListener('change', () => {
        updateBulkBarVisibility();
    });

    // Áp dụng bulk
    btnApply.addEventListener('click', async () => {
        const ids = getCheckedIds();
        const val = bulkSel.value;
        if (!['0', '1', '2', '3'].includes(val) || ids.length === 0) return;

        // Cảnh báo riêng cho Deleted (soft delete)
        if (val === '3' && !confirm(`Đặt ${ids.length} sản phẩm sang "Deleted"?`)) return;

        try {
            const res = await fetch(`${BASE}/api/shop/products/status`, {
                method: 'PATCH',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ids, status: Number(val)})
            });
            if (res.status === 204) {
                // Reload lại danh sách (ưu tiên dùng hàm load() nếu bạn đã expose)
                if (typeof window.reloadProducts === 'function') {
                    await window.reloadProducts();
                } else {
                    location.reload();
                }
            } else {
                const msg = await res.text();
                alert('Bulk update failed: ' + msg);
            }
        } catch (err) {
            console.error(err);
            alert('Có lỗi khi cập nhật trạng thái.');
        }
    });

    // ---- theo dõi render lại bảng để cập nhật thanh bulk ----
    const mo = new MutationObserver(() => updateBulkBarVisibility());
    mo.observe(tbody, {childList: true});

    // Lần đầu
    updateBulkBarVisibility();
})();