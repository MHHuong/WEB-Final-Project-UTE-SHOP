(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    // DOM refs
    const tbody = document.getElementById('productsTbody');
    // searchInput PHẢI tồn tại, bạn nói đã có id này rồi
    const searchInput = document.getElementById('searchInput');
    const statusFilter = document.getElementById('statusFilter'); // có cũng được, không chọn thì bỏ qua

    // Pagination UI
    const pagerUl = document.querySelector('.pagination.mb-0');
    const showingText = document.querySelector('.border-top span');

    // State
    let page = 0, size = 10, sort = 'createdAt,desc';
    let inflight; // AbortController để hủy request cũ (tránh race condition)

    async function load() {
        // hủy request cũ (nếu còn)
        if (inflight) inflight.abort();
        inflight = new AbortController();

        const params = new URLSearchParams();
        const q = (searchInput?.value || '').trim();
        if (q) params.set('q', q);

        // KHÔNG bắt buộc chọn status; chỉ đẩy khi có giá trị số 0/1/2
        const stRaw = statusFilter?.value;
        if (stRaw !== undefined && stRaw !== null && stRaw !== '') {
            params.set('status', stRaw);
        }

        params.set('page', page);
        params.set('size', size);
        params.set('sort', sort);

        try {
            const res = await fetch(`${BASE}/api/shop/products?` + params.toString(), {
                headers: {'Authorization': 'Bearer ' + token},
                signal: inflight.signal
            });
            if (!res.ok) {
                const t = await res.text();
                console.error(t);
                tbody.innerHTML = `<tr><td colspan="8" class="text-center text-danger">Load products failed</td></tr>`;
                return;
            }
            const data = await res.json();
            renderRows(data.content || []);
            renderShowing(data);
            renderPager(data.page, data.totalPages);
        } catch (e) {
            if (e.name !== 'AbortError') {
                console.error(e);
                tbody.innerHTML = `<tr><td colspan="8" class="text-center text-danger">Load products failed</td></tr>`;
            }
        }
    }

    function renderRows(items) {
        if (!items.length) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">No products found.</td></tr>`;
            return;
        }
        const frag = document.createDocumentFragment();
        for (const it of items) {
            const tr = document.createElement('tr');

            const tdCheck = document.createElement('td');
            tdCheck.innerHTML = `<div class="form-check"><input class="form-check-input" type="checkbox"/></div>`;
            tr.appendChild(tdCheck);

            // thumbnail an toàn
            const tdImg = document.createElement('td');
            const thumbUrl = it.thumbnailUrl ? (BASE + it.thumbnailUrl) : '';
            const isVideo = thumbUrl.includes('/videos/') || /\.(mp4|webm|mkv|mov|avi)$/i.test(thumbUrl);
            const thumbSrc = isVideo ? `${BASE}/assets/images/png/iphone-2.png`
                : (thumbUrl || `${BASE}/assets/images/png/iphone-2.png`);
            tdImg.innerHTML = `<a href="#!"><img src="${thumbSrc}" alt="" class="icon-shape icon-md"/></a>`;
            tr.appendChild(tdImg);

            const tdName = document.createElement('td');
            tdName.innerHTML = `<a href="#" class="text-reset">${escapeHtml(it.name)}</a>`;
            tr.appendChild(tdName);

            const tdCat = document.createElement('td');
            tdCat.textContent = it.categoryName || '';
            tr.appendChild(tdCat);

            const tdStatus = document.createElement('td');
            tdStatus.innerHTML = statusBadge(it.status);
            tr.appendChild(tdStatus);

            const tdPrice = document.createElement('td');
            tdPrice.textContent = formatMoney(it.price);
            tr.appendChild(tdPrice);

            const tdCreated = document.createElement('td');
            tdCreated.textContent = formatDate(it.createdAt);
            tr.appendChild(tdCreated);

            const tdAct = document.createElement('td');
            tdAct.innerHTML = `
        <div class="dropdown">
          <a href="#" class="text-reset" data-bs-toggle="dropdown" aria-expanded="false">
            <i class="feather-icon icon-more-vertical fs-5"></i>
          </a>
          <ul class="dropdown-menu">
            <li>
              <a class="dropdown-item" href="${BASE}/shop/product/edit?pid=${it.productId}">
                <i class="bi bi-pencil-square me-3"></i>Edit
              </a>
            </li>
            <li>
              <a class="dropdown-item text-danger" href="#"
                 onclick="event.preventDefault(); alert('TODO: Delete ${it.productId}');">
                <i class="bi bi-trash me-3"></i>Delete
              </a>
            </li>
          </ul>
        </div>`;
            tr.appendChild(tdAct);

            frag.appendChild(tr);
        }
        tbody.innerHTML = '';
        tbody.appendChild(frag);
    }

    function renderShowing(data) {
        if (!showingText) return;
        const start = data.totalElements === 0 ? 0 : (data.page * data.size + 1);
        const end = Math.min((data.page + 1) * data.size, data.totalElements);
        showingText.textContent = `Showing ${start} to ${end} of ${data.totalElements} entries`;
    }

    function renderPager(p, totalPages) {
        if (!pagerUl) return;
        pagerUl.innerHTML = '';

        const mkItem = (label, target, disabled = false, active = false) => {
            const li = document.createElement('li');
            li.className = `page-item ${disabled ? 'disabled' : ''} ${active ? 'active' : ''}`;
            const a = document.createElement('a');
            a.className = `page-link ${active ? 'active' : ''}`;
            a.href = '#!';
            a.textContent = label;
            a.onclick = (e) => {
                e.preventDefault();
                if (disabled || active) return;
                page = target;
                load();
            };
            li.appendChild(a);
            return li;
        };

        pagerUl.appendChild(mkItem('Previous', Math.max(0, p - 1), p === 0));
        const start = Math.max(0, p - 2);
        const end = Math.min(totalPages - 1, p + 2);
        for (let i = start; i <= end; i++) {
            pagerUl.appendChild(mkItem(String(i + 1), i, false, i === p));
        }
        pagerUl.appendChild(mkItem('Next', Math.min(totalPages - 1, p + 1), p >= totalPages - 1));
    }

    // Helpers
    function statusBadge(s) {
        if (s === 0) return `<span class="badge bg-light-success text-dark-success">In stock</span>`;
        if (s === 1) return `<span class="badge bg-light-danger text-dark-danger">Out of stock</span>`;
        if (s === 2) return `<span class="badge bg-light-warning text-dark-warning">Hide</span>`;
        return `<span class="badge bg-light text-dark">Unknown</span>`;
    }

    function formatMoney(n) {
        return '$' + Number(n || 0).toFixed(2);
    }

    function formatDate(iso) {
        if (!iso) return '';
        const d = new Date(iso);
        return d.toLocaleDateString('en-US', {day: '2-digit', month: 'short', year: 'numeric'});
    }

    function escapeHtml(s) {
        return (s ?? '').replace(/[&<>"']/g, c => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        }[c]));
    }

    // ====== TÌM NGAY KHI GÕ (debounce) + HỖ TRỢ ENTER ======
    function debounce(fn, delay = 100) {
        let t;
        return (...args) => {
            clearTimeout(t);
            t = setTimeout(() => fn(...args), delay);
        };
    }

    // IME safe: chỉ chạy khi người dùng kết thúc nhập (không đang compose)
    let composing = false;
    searchInput?.addEventListener('compositionstart', () => composing = true);
    searchInput?.addEventListener('compositionend', () => {
        composing = false;
        page = 0;
        load();
    });

    searchInput?.addEventListener('input', debounce(() => {
        if (composing) return;
        page = 0;
        load();
    }, 300));

    // Enter để tìm (fallback nếu bạn thích thao tác này)
    searchInput?.addEventListener('keydown', e => {
        if (e.key === 'Enter') {
            e.preventDefault();
            page = 0;
            load();
        }
    });

    // Đổi trạng thái vẫn hoạt động bình thường (không bắt buộc chọn)
    statusFilter?.addEventListener('change', () => {
        page = 0;
        load();
    });

    // Kick off
    load();
})();