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

    btnPick.addEventListener('click', () => input.click());

    input.addEventListener('change', () => {
        preview.innerHTML = '';
        [...input.files].forEach(file => {
            const col = document.createElement('div');
            col.className = 'col-6 col-md-3';
            const url = URL.createObjectURL(file);
            col.innerHTML = file.type.startsWith('video/')
                ? `<video src="${url}" class="w-100 rounded" controls preload="metadata" style="max-height:160px;object-fit:cover"></video>`
                : `<img src="${url}" class="img-fluid rounded" style="max-height:160px;object-fit:cover"/>`;
            preview.appendChild(col);
        });
    });

    async function createProduct() {
        const name = document.querySelector('#productName')?.value?.trim();
        const categoryId = Number(document.querySelector('#categorySelect')?.value);
        const price = Number(document.querySelector('#price')?.value || 0);
        const stock = Number(document.querySelector('#stock')?.value || 0);
        const status = Number(document.querySelector('#statusSelect')?.value || 0);
        const description = document.querySelector('#shopDescription')?.value || null;

        const data = {name, categoryId, price, stock, status, description};

        const fd = new FormData();
        fd.append('data', new Blob([JSON.stringify(data)], {type: 'application/json'}));
        [...(input.files || [])].forEach(f => fd.append('files', f));

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
        alert('Created!');
        console.log('Created:', json);
    }

    const btnCreate = document.querySelector('.btn.btn-primary');
    if (btnCreate) {
        btnCreate.addEventListener('click', (e) => {
            e.preventDefault();
            createProduct().catch(err => {
                console.error(err);
                alert('Have errors when create product');
            });
            window.location.href = BASE + '/shop/product/products';
        });
    }
})();