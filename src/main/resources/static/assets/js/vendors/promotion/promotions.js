(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    // DOM giống products
    const tbody = document.getElementById('promotionsTbody');
    const searchInput = document.getElementById('searchInput');
    const statusFilter = document.getElementById('statusFilter');
    const pagerUl = document.querySelector('.pagination.mb-0');
    const showingText = document.querySelector('.border-top span');

    const createBtn = document.getElementById('createPromoBtn');
    if (createBtn) {
        createBtn.addEventListener('click', (e) => {
            e.preventDefault();
            window.location.href = `${BASE}/shop/promotion/add-promotion`;
        });
    }

    let page = 0, size = 10, sort = 'startDate,desc';
    let inflight;

    async function load() {
        if (inflight) inflight.abort();
        inflight = new AbortController();

        const params = new URLSearchParams();
        const q = (searchInput?.value || '').trim();
        if (q) params.set('q', q);

        const stRaw = statusFilter?.value;
        if (stRaw !== undefined && stRaw !== null && stRaw !== '') {
            params.set('status', stRaw); // active | upcoming | expired
        }

        params.set('page', page);
        params.set('size', size);
        params.set('sort', sort);

        try {
            // API server-side giống products: /api/shop/promotions
            const res = await fetch(`${BASE}/api/shop/promotions?` + params.toString(), {
                headers: {'Authorization': 'Bearer ' + token},
                signal: inflight.signal
            });
            if (!res.ok) {
                const t = await res.text();
                console.error(t);
                tbody.innerHTML = `<tr><td colspan="8" class="text-center text-danger">Load promotions failed</td></tr>`;
                return;
            }
            const data = await res.json();
            renderRows(data.content || []);
            renderShowing(data);
            renderPager(data.page, data.totalPages);
        } catch (e) {
            if (e.name !== 'AbortError') {
                console.error(e);
                tbody.innerHTML = `<tr><td colspan="8" class="text-center text-danger">Load promotions failed</td></tr>`;
            }
        }
    }

    function renderRows(items) {
        if (!items.length) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">No promotions found.</td></tr>`;
            return;
        }
        const frag = document.createDocumentFragment();
        let idx = 0;
        for (const it of items) {
            const tr = document.createElement('tr');

            // #
            const tdIndex = document.createElement('td');
            tdIndex.textContent = String(page * size + (++idx));
            tr.appendChild(tdIndex);

            // Title (link tới edit)
            const tdTitle = document.createElement('td');
            tdTitle.innerHTML = `<a class="text-reset" href="${BASE}/shop/promotion/edit-promotion?id=${it.promotionId}">${escapeHtml(it.title || '')}</a>`;
            tr.appendChild(tdTitle);

            // Category
            const tdCat = document.createElement('td');
            tdCat.textContent = it.applyCategoryName || 'All categories';
            tr.appendChild(tdCat);

            // Discount
            const tdDisc = document.createElement('td');
            tdDisc.textContent = (it.discountPercent != null ? it.discountPercent + '%' : '');
            tr.appendChild(tdDisc);

            // Dates
            const tdStart = document.createElement('td');
            tdStart.textContent = formatDate(it.startDate);
            tr.appendChild(tdStart);

            const tdEnd = document.createElement('td');
            tdEnd.textContent = formatDate(it.endDate);
            tr.appendChild(tdEnd);

            // Status badge (Active/Upcoming/Expired) — hiển thị giống filter
            const tdStatus = document.createElement('td');
            const st = statusOf(it.startDate, it.endDate);
            tdStatus.innerHTML = statusBadge(st);
            tr.appendChild(tdStatus);

            // Actions: dropdown “...“ giống products
            const tdAct = document.createElement('td');
            tdAct.innerHTML = `
        <div class="dropdown">
          <a href="#" class="text-reset" data-bs-toggle="dropdown" aria-expanded="false">
            <i class="feather-icon icon-more-vertical fs-5"></i>
          </a>
          <ul class="dropdown-menu">
            <li>
              <a class="dropdown-item" href="${BASE}/shop/promotion/edit-promotion?id=${it.promotionId}">
                <i class="bi bi-pencil-square me-3"></i>Edit
              </a>
            </li>
            <li>
              <a class="dropdown-item text-danger btn-delete" href="#" data-id="${it.promotionId}">
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

    // Helpers (đồng bộ style với products)
    function escapeHtml(s) {
        return (s ?? '').replace(/[&<>"']/g, c => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        }[c]));
    }

    function formatDate(iso) {
        if (!iso) return '';
        const d = new Date(iso);
        if (Number.isNaN(d.getTime())) return iso;
        return d.toLocaleDateString('en-US', {day: '2-digit', month: 'short', year: 'numeric'});
    }

    function statusOf(startDate, endDate) {
        const now = new Date();
        const s = new Date(`${startDate}T00:00:00`);
        const e = new Date(`${endDate}T23:59:59`);
        if (now < s) return 'upcoming';
        if (now > e) return 'expired';
        return 'active';
    }

    function statusBadge(s) {
        const st = (s || '').toLowerCase();
        if (st === 'active') return `<span class="badge bg-light-success text-dark-success">Active</span>`;
        if (st === 'upcoming') return `<span class="badge bg-light-warning text-dark-warning">Upcoming</span>`;
        if (st === 'expired') return `<span class="badge bg-light text-dark">Expired</span>`;
        return `<span class="badge bg-light text-dark">Unknown</span>`;
    }

    // ====== Search & filter giống products (debounce + Enter + IME safe) ======
    function debounce(fn, delay = 100) {
        let t;
        return (...args) => {
            clearTimeout(t);
            t = setTimeout(() => fn(...args), delay);
        };
    }

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
    searchInput?.addEventListener('keydown', e => {
        if (e.key === 'Enter') {
            e.preventDefault();
            page = 0;
            load();
        }
    });

    statusFilter?.addEventListener('change', () => {
        page = 0;
        load();
    });

    // Delete (event delegation)
    tbody?.addEventListener('click', async (e) => {
        const a = e.target.closest('.btn-delete');
        if (!a) return;
        e.preventDefault();
        const id = a.getAttribute('data-id');
        if (!id) return;
        if (!confirm('Delete this promotion?')) return;
        try {
            const res = await fetch(`${BASE}/api/shop/promotions/${id}`, {
                method: 'DELETE',
                headers: {'Authorization': 'Bearer ' + token}
            });
            if (res.status === 204) {
                load();
            } else {
                const msg = await res.text();
                alert('Delete failed: ' + (msg || ''));
            }
        } catch (err) {
            console.error(err);
            alert('Có lỗi khi xoá promotion.');
        }
    });
    // Kick off
    load();
})();
