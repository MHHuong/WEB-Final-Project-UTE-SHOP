(function () {
    // Giữ phong cách module giống ProductForm để quen tay
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) return; // ở page js sẽ redirect nếu thiếu

    const categorySelect = document.getElementById('promoCategory');

    function authFetch(url, opt = {}) {
        return fetch(url, {
            ...opt,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token,
                ...(opt.headers || {})
            }
        });
    }

    // Load category cho combobox (dựa pattern charger ở project)
    function loadCategories() {
        return authFetch(`${BASE}/api/categories?page=0&size=9999`)
            .then(r => r.ok ? r.json() : Promise.reject())
            .then(data => {
                const list = data?.content || data || [];
                list.forEach(c => {
                    const op = document.createElement('option');
                    op.value = c.categoryId ?? c.id ?? c.category_id;
                    op.textContent = c.name || c.categoryName || ('Category #' + op.value);
                    categorySelect?.appendChild(op);
                });
            })
            .catch(() => {
            });
    }

    // Expose nhỏ để trang edit dùng set value an toàn
    window.__PromoForm = {
        BASE,
        loadCategories,
        setCategoryValue(val) {
            if (!categorySelect) return;
            const setLater = () => {
                const ok = [...categorySelect.options].some(o => String(o.value) === String(val));
                if (ok) categorySelect.value = String(val);
                else setTimeout(setLater, 200);
            };
            if (val != null) setLater();
        }
    };

    document.addEventListener('DOMContentLoaded', loadCategories);
})();
