(function () {
    window.ProductForm = {
        BASE: '/UTE_SHOP',

        bindMediaPicker() {
            const btnPick = document.getElementById('btnPick');
            const input = document.getElementById('mediaInput');
            const preview = document.getElementById('preview');

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
        },

        hasAtLeastOneImage(files) {
            return [...(files || [])].some(f => f && (f.type?.startsWith('image/') ||
                /\.(jpg|jpeg|png|gif|webp|bmp|avif)$/i.test(f.name || '')));
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
            const input = document.getElementById('mediaInput');

            if (!name) throw new Error('Vui lòng nhập tên sản phẩm.');
            if (!catVal) throw new Error('Vui lòng chọn danh mục.');
            if (isNaN(price) || price < 0) throw new Error('Giá không hợp lệ.');

            if (requireImage && !this.hasAtLeastOneImage(input?.files)) {
                throw new Error('Mỗi sản phẩm phải có ít nhất 1 ảnh.');
            }

            return {
                data: {name, categoryId: Number(catVal), price, stock, status, description},
                files: input?.files || []
            };
        },

        fillForm(p) {
            // p: ProductVM từ API GET /api/shop/products/{id}
            document.getElementById('productName').value = p.name ?? '';
            document.getElementById('stock').value = p.stock ?? 0;
            document.getElementById('price').value = p.price ?? 0;
            document.getElementById('shopDescription').value = p.description ?? '';
            const statusSel = document.getElementById('statusSelect');
            if (statusSel) statusSel.value = String(p.status ?? '');

            // Category: nếu combo đã load từ script riêng, set value (đợi async 1 chút)
            const catSel = document.getElementById('categorySelect');
            if (catSel && p.categoryId) {
                const trySet = () => {
                    const exists = [...catSel.options].some(o => Number(o.value) === Number(p.categoryId));
                    if (exists) catSel.value = String(p.categoryId);
                    else setTimeout(trySet, 200);
                };
                trySet();
            }

            // Render media đã có (ảnh/video)
            const existing = document.getElementById('existingMedia');
            if (existing && Array.isArray(p.media)) {
                existing.innerHTML = '';
                p.media.forEach(m => {
                    const col = document.createElement('div');
                    col.className = 'col-6 col-md-3';
                    const isVideo = (m.type || '').toLowerCase() === 'video';
                    col.innerHTML = isVideo
                        ? `<video src="${m.url}" class="w-100 rounded" controls preload="metadata" style="max-height:160px;object-fit:cover"></video>`
                        : `<img src="${m.url}" class="img-fluid rounded" style="max-height:160px;object-fit:cover"/>`;
                    existing.appendChild(col);
                });
            }
        }
    };
})();