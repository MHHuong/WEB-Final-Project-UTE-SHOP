(function () {
    // --- CHỐT: nếu đã init trước đó thì thoát ---
    if (window.__PF_INITED__) return;
    window.__PF_INITED__ = true;

    const BASE = '/UTE_SHOP';
    const state = {selectedFiles: []};

    function isImageFile(f) {
        const ct = (f?.type || '').toLowerCase();
        if (ct.startsWith('image/')) return true;
        const n = (f?.name || '').toLowerCase();
        return /\.(jpg|jpeg|png|gif|webp|bmp|avif)$/.test(n);
    }

    function isVideoFile(f) {
        const ct = (f?.type || '').toLowerCase();
        if (ct.startsWith('video/')) return true;
        const n = (f?.name || '').toLowerCase();
        return /\.(mp4|webm|mov|mkv|avi)$/.test(n);
    }

    function keyOf(f) {
        return `${f?.name || ''}|${f?.size || 0}|${f?.lastModified || 0}`;
    }

    function renderPreview() {
        const preview = document.getElementById('preview');
        if (!preview) return;
        preview.innerHTML = '';
        state.selectedFiles.forEach((file, idx) => {
            const isVid = isVideoFile(file);
            const url = URL.createObjectURL(file);
            const col = document.createElement('div');
            col.className = 'col-6 col-md-3';
            col.innerHTML = `
        <div class="position-relative">
          ${isVid ? `<video src="${url}" class="w-100 rounded"
       controls playsinline preload="metadata"
       style="max-height:160px;object-fit:cover"></video>`
                : `<img src="${url}" class="img-fluid rounded" style="max-height:160px;object-fit:cover"/>`}
          <button type="button" class="btn btn-sm btn-danger position-absolute top-0 end-0 m-1"
                  data-role="remove-staged" data-index="${idx}">
            <i class="bi bi-x"></i>
          </button>
        </div>
        <div class="small text-truncate mt-1" title="${file.name}">${file.name}</div>
      `;
            preview.appendChild(col);
        });
        preview.querySelectorAll('[data-role="remove-staged"]').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const i = Number(e.currentTarget.getAttribute('data-index'));
                if (!Number.isInteger(i)) return;
                state.selectedFiles.splice(i, 1);
                renderPreview();
            }, {once: true}); // mỗi nút chỉ bind 1 lần
        });
    }

    const ProductForm = {
        BASE,
        getSelectedFiles() {
            return Array.isArray(state.selectedFiles) ? [...state.selectedFiles] : [];
        },
        addFiles(filesLike) {
            const incoming = Array.from(filesLike || []);
            if (!incoming.length) return;
            const existed = new Set(state.selectedFiles.map(keyOf));
            incoming.forEach(f => {
                const k = keyOf(f);
                if (!existed.has(k)) {
                    state.selectedFiles.push(f);
                    existed.add(k);
                }
            });
            renderPreview();
        },
        hasAtLeastOneImage(arr) {
            const a = Array.isArray(arr) ? arr : state.selectedFiles;
            return a.some(f => f && isImageFile(f));
        },
        getStatusOrThrow() {
            const sel = document.getElementById('statusSelect');
            const v = sel?.value ?? '';
            if (!['0', '1', '2'].includes(v)) throw new Error('Vui lòng chọn Status.');
            return Number(v);
        },
        collectDataOrThrow(requireImage = true) {
            const name = document.getElementById('productName')?.value?.trim();
            const catVal = document.getElementById('categorySelect')?.value;
            const price = Number(document.getElementById('price')?.value || 0);
            const stock = Number(document.getElementById('stock')?.value || 0);
            const status = this.getStatusOrThrow();
            const description = document.getElementById('shopDescription')?.value || null;
            if (!name) throw new Error('Vui lòng nhập tên sản phẩm.');
            if (!catVal) throw new Error('Vui lòng chọn danh mục.');
            if (isNaN(price) || price < 0) throw new Error('Giá không hợp lệ.');
            const files = this.getSelectedFiles();
            if (requireImage && !this.hasAtLeastOneImage(files)) throw new Error('Sản phẩm cần có ít nhất 1 hình ảnh.');
            return {data: {name, categoryId: Number(catVal), price, stock, status, description}, files};
        },
        fillForm(p) {
            document.getElementById('productName')?.setAttribute('value', p?.name || '');
            document.getElementById('price')?.setAttribute('value', p?.price ?? 0);
            document.getElementById('stock')?.setAttribute('value', p?.stock ?? 0);
            const descEl = document.getElementById('shopDescription');
            if (descEl) descEl.value = p?.description || '';
            const statusSel = document.getElementById('statusSelect');
            if (statusSel && [0, 1, 2].includes(Number(p?.status))) statusSel.value = String(p.status);
            const catSel = document.getElementById('categorySelect');
            if (catSel && p?.categoryId) {
                const trySet = () => {
                    const ok = [...catSel.options].some(o => Number(o.value) === Number(p.categoryId));
                    if (ok) catSel.value = String(p.categoryId); else setTimeout(trySet, 200);
                };
                trySet();
            }
            const existing = document.getElementById('existingMedia');
            if (existing && Array.isArray(p?.media)) {
                existing.innerHTML = '';
                p.media.forEach(m => {
                    const isVid = (m?.type || '').toLowerCase() === 'video';
                    const col = document.createElement('div');
                    col.className = 'col-6 col-md-3';
                    col.innerHTML = `
            <div class="position-relative">
              ${isVid ? `<video src="${url}" class="w-100 rounded"
       controls playsinline preload="metadata"
       style="max-height:160px;object-fit:cover"></video>`
                        : `<img src="${m.url}" class="img-fluid rounded" style="max-height:160px;object-fit:cover"/>`}
              <button type="button" class="btn btn-sm btn-outline-danger position-absolute top-0 end-0 m-1"
                      data-role="delete-existing" data-media-id="${m.id}">
                <i class="bi bi-trash"></i>
              </button>
            </div>`;
                    existing.appendChild(col);
                });
            }
        },

        bindMediaPicker() {
            const btnPick = document.getElementById('btnPick');
            const input = document.getElementById('mediaInput');

            // tránh bind trùng
            if (btnPick && btnPick.dataset.pfBound !== '1') {
                btnPick.addEventListener('click', () => {
                    if (input) input.click();
                }, {passive: true});
                btnPick.dataset.pfBound = '1';
            }
            if (input && input.dataset.pfBound !== '1') {
                input.addEventListener('change', () => {
                    const files = Array.from(input.files || []);
                    if (files.length) ProductForm.addFiles(files);
                    // reset để lần sau vẫn chọn lại được cùng file
                    input.value = '';
                });
                input.dataset.pfBound = '1';
            }
        },

        init() {
            this.bindMediaPicker();
            renderPreview();
        },

        // flag để trang khác kiểm tra nếu cần
        __ready: true
    };

    window.ProductForm = ProductForm;

    // chỉ init qua DOMContentLoaded để tránh đua, không init ngay lập tức
    document.addEventListener('DOMContentLoaded', () => ProductForm.init());
})();
