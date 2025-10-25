(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const $ = id => document.getElementById(id);
    const btnPick = $('btnPick');
    const input = $('mediaInput');
    const preview = $('preview');

    // --- Preview media ---
    btnPick?.addEventListener('click', () => input?.click());

    input?.addEventListener('change', () => {
        if (!preview) return;
        preview.innerHTML = '';
        [...(input.files || [])].forEach(file => {
            const col = document.createElement('div');
            col.className = 'col-6 col-md-3';
            const url = URL.createObjectURL(file);
            col.innerHTML = file.type.startsWith('video/')
                ? `<video src="${url}" class="w-100 rounded" controls preload="metadata" style="max-height:160px;object-fit:cover"></video>`
                : `<img src="${url}" class="img-fluid rounded" style="max-height:160px;object-fit:cover"/>`;
            preview.appendChild(col);
        });
    });

    // --- Helpers ---
    function hasAtLeastOneImage(files) {
        return [...(files || [])].some(f => f && (f.type?.startsWith('image/') ||
            /\.(jpg|jpeg|png|gif|webp|bmp|avif)$/i.test(f.name || '')));
    }

    function getStatus() {
        const sel = document.querySelector('#statusSelect');
        const v = sel?.value ?? '';
        return ['0', '1', '2'].includes(v) ? Number(v) : 0;
    }

    // --- Submit ---
    async function createProduct() {
        const name = document.querySelector('#productName')?.value?.trim();
        const catVal = document.querySelector('#categorySelect')?.value;
        const categoryId = catVal ? Number(catVal) : null;
        const price = Number(document.querySelector('#price')?.value || 0);
        const stock = Number(document.querySelector('#stock')?.value || 0);
        const status = getStatus();
        const description = document.querySelector('#shopDescription')?.value || null;

        if (!name) {
            alert('Please enter product name');
            return;
        }
        if (!categoryId) {
            alert('Please choose a category');
            return;
        }
        if (isNaN(price) || price < 0) {
            alert('Invalid price');
            return;
        }
        if (!hasAtLeastOneImage(input?.files)) {
            alert('A product must have at least one image.');
            return;
        }

        const data = {name, categoryId, price, stock, status, description};

        const fd = new FormData();
        fd.append('data', new Blob([JSON.stringify(data)], {type: 'application/json'}));
        [...(input?.files || [])].forEach(f => fd.append('files', f));

        const btnCreate = getCreateBtn();
        const oldText = btnCreate?.textContent;
        if (btnCreate) {
            btnCreate.classList.add('disabled');
            btnCreate.textContent = 'Creating...';
        }

        try {
            const res = await fetch(BASE + '/api/shop/products', {
                method: 'POST',
                headers: {'Authorization': 'Bearer ' + token},
                body: fd
            });

            if (!res.ok) {
                const t = await res.text();
                alert('Create failed: ' + t);
                return;
            }

            const json = await res.json();
            console.log('Created:', json);
            alert('Created!');
            // ✅ Chỉ redirect sau khi tạo thành công
            window.location.href = BASE + '/shop/product/products';

        } catch (err) {
            console.error(err);
            alert('Have errors when create product');
        } finally {
            if (btnCreate) {
                btnCreate.classList.remove('disabled');
                btnCreate.textContent = oldText || 'Create Product';
            }
        }
    }

    function getCreateBtn() {
        const candidates = document.querySelectorAll('a.btn.btn-primary, button.btn.btn-primary');
        return candidates[candidates.length - 1] || null;
    }

    const btnCreate = getCreateBtn();
    if (btnCreate) {
        btnCreate.addEventListener('click', (e) => {
            e.preventDefault();
            createProduct().catch(err => {
                console.error(err);
                alert('Have errors when create product');
            });
        });
    }
})();
