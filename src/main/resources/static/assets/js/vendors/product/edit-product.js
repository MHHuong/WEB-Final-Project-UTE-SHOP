(function () {
    const PF = window.ProductForm;
    if (!PF) {
        console.error('ProductForm not found');
        return;
    }

    const {BASE} = PF;
    const collectDataOrThrow = PF.collectDataOrThrow.bind(PF);

    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const params = new URLSearchParams(window.location.search);
    const pid = Number(params.get('pid'));
    if (!pid) {
        alert('Product ID is missing');
        history.back();
        return;
    }

    // ========= Helpers =========
    function buildUrl(p) {
        if (!p) return '';
        if (/^https?:\/\//i.test(p)) return p;   // absolute
        if (p.startsWith(BASE + '/')) return p;  // already has BASE
        if (p.startsWith('/')) return BASE + p;  // /uploads/...
        return BASE + '/' + p.replace(/^\/+/, '');
    }

    function isVideoType(type, url) {
        const t = String(type ?? '').toLowerCase();
        return t === 'video' || t === '2' || /\/videos\//.test(url) || /\.(mp4|webm|mov|mkv|avi)$/i.test(url);
    }

    function isImageFile(file) {
        if (!file) return false;
        const ct = String(file.type || '').toLowerCase();
        if (ct.startsWith('image/')) return true;
        const name = String(file.name || '').toLowerCase();
        return /\.(jpg|jpeg|png|gif|webp|bmp|avif)$/i.test(name);
    }

    // ========= Media state (đếm ảnh riêng) =========
    const toRemove = new Set();          // id media cũ muốn xoá
    let existingImageCount = 0;          // CHỈ đếm ảnh cũ
    const mediaTypeById = new Map();     // id -> 'image' | 'video'

    function renderExistingMedia(list) {
        const wrap = document.getElementById('existingMedia');
        if (!wrap) return;
        wrap.innerHTML = '';

        mediaTypeById.clear();
        existingImageCount = 0;

        (list || []).forEach(m => {
            const src = buildUrl(m?.url || '');
            const isVid = isVideoType(m?.type, src);
            const typeStr = isVid ? 'video' : 'image';

            mediaTypeById.set(Number(m.id), typeStr);
            if (!isVid) existingImageCount++;

            const col = document.createElement('div');
            col.className = 'col-6 col-md-3';
            col.innerHTML = `
        <div class="position-relative border rounded p-2 d-flex align-items-center justify-content-center" style="min-height:110px">
          ${
                isVid
                    ? `<video src="${src}" class="w-100 rounded" controls preload="metadata" playsinline style="max-height:160px;object-fit:cover"></video>`
                    : `<img src="${src}" class="img-fluid rounded" style="max-height:160px;object-fit:cover"
                   onerror="this.src='${buildUrl('/assets/images/png/iphone-2.png')}'"/>`
            }
          <button type="button" class="btn btn-sm btn-outline-danger position-absolute top-0 end-0 m-1"
                  title="Remove" data-role="remove-existing" data-id="${m.id}">
            <i class="bi bi-trash"></i>
          </button>
        </div>
      `;
            wrap.appendChild(col);
        });

        wrap.querySelectorAll('[data-role="remove-existing"]').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = Number(btn.getAttribute('data-id'));
                if (!id || toRemove.has(id)) return;

                toRemove.add(id);
                // Chỉ giảm đếm nếu media xoá là ẢNH
                if (mediaTypeById.get(id) === 'image') {
                    existingImageCount = Math.max(0, existingImageCount - 1);
                }

                const card = btn.closest('.position-relative') || btn.parentElement;
                if (card) card.style.opacity = '0.5';
                btn.disabled = true;
            });
        });
    }

    // ========= Init =========
    document.addEventListener('DOMContentLoaded', init);

    async function init() {
        try {
            const res = await fetch(`${BASE}/api/shop/products/${pid}`, {
                headers: {'Authorization': 'Bearer ' + token}
            });
            if (!res.ok) throw new Error(await res.text());
            const product = await res.json();

            // 1) điền input/combobox
            PF.fillForm(product);

            // 2) render media cũ (kèm set existingImageCount & map id->type)
            const list = product.media || product.mediaList || [];
            renderExistingMedia(list);
        } catch (e) {
            alert(e?.message || 'Load product failed');
            history.back();
        }
    }

    // ========= Submit =========
    document.getElementById('btnSubmit')?.addEventListener('click', onSave);

    async function onSave(e) {
        e.preventDefault();
        const btn = e.currentTarget;

        try {
            // Tính số ảnh mới (KHÔNG tính video mới)
            let newImagesCount = 0;
            if (typeof PF.getSelectedFiles === 'function') {
                newImagesCount = PF.getSelectedFiles().filter(isImageFile).length;
            } else {
                // fallback input file (đổi id nếu bạn đặt id khác)
                const fi = document.getElementById('productFiles');
                const files = fi?.files ? Array.from(fi.files) : [];
                newImagesCount = files.filter(isImageFile).length;
            }

            // Ảnh cũ còn lại sau khi trừ những ảnh đã chọn xoá
            const imagesAfterRemove = existingImageCount; // đã trừ ngay khi bấm xoá ở trên

            // YÊU CẦU: phải còn ít nhất 1 ẢNH (không tính video)
            if (imagesAfterRemove + newImagesCount <= 0) {
                alert('A product must have at least one image.');
                return;
            }

            // Thu thập dữ liệu form (không ép ảnh ở trang edit)
            const {data, files} = collectDataOrThrow(false); // boolean false

            // Gửi danh sách media muốn xoá (mảng rỗng cũng ok)
            data.removeMediaIds = Array.from(toRemove);

            const fd = new FormData();
            fd.append('data', new Blob([JSON.stringify(data)], {type: 'application/json'}));
            (files || []).forEach(f => fd.append('files', f));

            btn.disabled = true;
            const old = btn.textContent;
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
            alert(err?.message || 'Save failed');
        } finally {
            if (btn) {
                btn.disabled = false;
                btn.textContent = 'Save Changes';
            }
        }
    }
})();
