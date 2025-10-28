(function() {
    const BASE = '/UTE_SHOP';

    async function loadCategories() {
        try {
            const res = await fetch(BASE + '/api/categories', {
                method: 'GET',
                headers: { 'Accept': 'application/json' }
            });
            if (!res.ok) throw new Error('Load categories failed');
            const data = await res.json();

            const sel = document.getElementById('categorySelect');
            sel.innerHTML = '<option value="">Product Category</option>';

            data.sort((a,b) => (a.name||'').localeCompare(b.name||''));

            for (const item of data) {
                const opt = document.createElement('option');
                opt.value = item.id;
                opt.textContent = item.name;
                sel.appendChild(opt);
            }
        } catch (e) {
            console.error(e);
        }
    }

    document.addEventListener('DOMContentLoaded', loadCategories);
})();