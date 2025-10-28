(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const tbody = document.getElementById('couponsTbody');
    const searchInput = document.getElementById('searchInput');
    const statusFilter = document.getElementById('statusFilter');
    const pagerUl = document.querySelector('.pagination.mb-0');
    const showingText = document.querySelector('.border-top span');

    const createBtn = document.getElementById('createCouponBtn');
    if (createBtn) {
        createBtn.addEventListener('click', (e) => {
            e.preventDefault();
            window.location.href = `${BASE}/shop/coupon/add-coupon`;
        });
    }

    let page = 0, size = 10, sort = 'expiredAt,desc';
    let inflight;

    async function load() {
        if (inflight) inflight.abort();
        inflight = new AbortController();

        const params = new URLSearchParams();
        const q = (searchInput?.value || '').trim();
        if (q) params.set('q', q);
        const stRaw = statusFilter?.value;
        if (stRaw !== undefined && stRaw !== null && stRaw !== '') params.set('status', stRaw);
        params.set('page', page);
        params.set('size', size);
        params.set('sort', sort);

        try {
            const res = await fetch(`${BASE}/api/shop/coupons?` + params.toString(), {
                headers: {'Authorization': 'Bearer ' + token},
                signal: inflight.signal
            });
            if (!res.ok) {
                const t = await res.text();
                console.error(t);
                tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger">Load coupons failed</td></tr>`;
                return;
            }
            const data = await res.json();
            renderRows(data.content || []);
            renderShowing(data);
            renderPager(data.page, data.totalPages);
        } catch (e) {
            if (e.name !== 'AbortError') {
                console.error(e);
                tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger">Load coupons failed</td></tr>`;
            }
        }
    }

    function renderRows(items) {
        if (!items.length) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted">No coupons found.</td></tr>`;
            return;
        }
        tbody.innerHTML = '';
        for (const it of items) {
            const tr = document.createElement('tr');

            const tdCode = document.createElement('td');
            tdCode.textContent = it.code;
            tr.appendChild(tdCode);

            const tdType = document.createElement('td');
            tdType.textContent = it.discountType;
            tr.appendChild(tdType);

            const tdValue = document.createElement('td');
            tdValue.textContent = it.value;
            tr.appendChild(tdValue);

            const tdMin = document.createElement('td');
            tdMin.textContent = it.minOrderAmount ?? 0;
            tr.appendChild(tdMin);

            const tdExpired = document.createElement('td');
            tdExpired.textContent = new Date(it.expiredAt).toLocaleString();
            tr.appendChild(tdExpired);

            const tdStatus = document.createElement('td');
            const ts = Date.parse(it.expiredAt);
            const isExpired = Number.isFinite(ts) ? ts <= Date.now() : false;
            tdStatus.innerHTML = `<span class="badge ${isExpired ? 'bg-danger' : 'bg-success'}">
                                  ${isExpired ? 'Expired' : 'Active'}
                                </span>`;
            tr.appendChild(tdStatus);

            const tdAction = document.createElement('td');
            tdAction.className = 'text-muted';
            tdAction.innerHTML = `
        <div class="dropdown">
          <a href="#" class="text-reset" data-bs-toggle="dropdown" aria-expanded="false">
            <i class="feather-icon icon-more-vertical fs-5"></i>
          </a>
          <ul class="dropdown-menu">
            <li><a class="dropdown-item" href="${BASE}/shop/coupon/edit-coupon?id=${it.couponId}">Edit</a></li>
            <li><a class="dropdown-item text-danger" data-action="delete" data-id="${it.couponId}" href="#">Delete</a></li>
          </ul>
        </div>`;
            tr.appendChild(tdAction);

            tbody.appendChild(tr);
        }
        tbody.querySelectorAll('[data-action="delete"]').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.preventDefault();
                const id = btn.getAttribute('data-id');
                if (!confirm('Delete this coupon?')) return;
                const res = await fetch(`${BASE}/api/shop/coupons/${id}`, {
                    method: 'DELETE',
                    headers: {'Authorization': 'Bearer ' + token}
                });
                if (res.status === 204) load();
                else alert(await res.text());
            });
        });
    }

    function renderShowing(pg) {
        const start = pg.totalElements === 0 ? 0 : (pg.page * pg.size + 1);
        const end = Math.min(pg.totalElements, (pg.page + 1) * pg.size);
        if (showingText) showingText.textContent = `Showing ${start} to ${end} of ${pg.totalElements} entries`;
    }

    function renderPager(cur, total) {
        if (!pagerUl) return;
        pagerUl.innerHTML = '';
        const mk = (label, p, disabled = false, active = false) => {
            const li = document.createElement('li');
            li.className = `page-item ${disabled ? 'disabled' : ''} ${active ? 'active' : ''}`;
            li.innerHTML = `<a class="page-link" href="#">${label}</a>`;
            if (!disabled && !active) li.addEventListener('click', (e) => {
                e.preventDefault();
                page = p;
                load();
            });
            return li;
        };
        pagerUl.appendChild(mk('Prev', Math.max(cur - 1, 0), cur === 0));
        for (let i = 0; i < total; i++) pagerUl.appendChild(mk(String(i + 1), i, false, i === cur));
        pagerUl.appendChild(mk('Next', Math.min(cur + 1, total - 1), cur >= total - 1));
    }

    searchInput?.addEventListener('input', () => {
        page = 0;
        load();
    });
    statusFilter?.addEventListener('change', () => {
        page = 0;
        load();
    });

    load();
})();
