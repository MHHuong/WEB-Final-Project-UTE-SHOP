(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const tbody = document.getElementById('returnsTbody');
    const searchInput = document.getElementById('searchInput');
    const statusFilter = document.getElementById('statusFilter');
    const pagerUl = document.querySelector('.pagination.mb-0');
    const showingText = document.querySelector('.border-top span');

    let page = 0, size = 10, sort = 'createdAt,desc';
    let inflight;

    const fmtMoney = v => (v ?? 0).toLocaleString('vi-VN') + ' VND';
    const fmtDate = iso => iso ? new Date(iso).toLocaleString() : '';

    const badge = st => {
        switch (String(st).toUpperCase()) {
            case 'RETURNING':
                return 'bg-info';
            case 'RETURNED':
                return 'bg-dark';
            default:
                return 'bg-light text-dark';
        }
    };

    function actionsHtml(o) {
        const st = String(o.status).toUpperCase();
        if (st === 'RETURNING') {
            // Hiển thị nút Confirm khi confirmable
            return `
        <div class="d-flex gap-2 justify-content-end">
          <button class="btn btn-sm btn-success btn-confirm-received d-none" data-id="${o.orderId}">
            CONFIRM RECEIVED RETURN ORDER
          </button>
        </div>`;
        }
        return ''; // RETURNED: không có action
    }

    function row(o) {
        const link = `<a class="text-inherit" href="${BASE}/shop/order/order-single?orderId=${o.orderId}">
                    <h6 class="mb-0">#${o.orderId}</h6>
                  </a>`;
        const customer = `${o.customerName || ''}<div class="text-muted small">${o.customerEmail || ''}</div>`;
        const dateStr = fmtDate(o.createdAt);
        const amount = fmtMoney(o.amount);
        const statusHtml = `<span class="badge ${badge(o.status)}">${o.status}</span>`;

        return `<tr data-id="${o.orderId}">
      <td>${o.orderId}</td>
      <td>${link}</td>
      <td>${customer}</td>
      <td>${dateStr}</td>
      <td>${o.paymentMethod || ''}</td>
      <td>${amount}</td>
      <td>${statusHtml}</td>
      <td class="text-end">${actionsHtml(o)}</td>
    </tr>`;
    }

    async function checkConfirmable(orderId, btnEl) {
        try {
            const resp = await fetch(`${BASE}/api/shop/orders/${orderId}/return/confirmable`, {
                headers: {Authorization: `Bearer ${token}`}
            });
            if (!resp.ok) return;
            const ok = await resp.json();
            if (ok && btnEl) btnEl.classList.remove('d-none');
        } catch {
        }
    }

    async function load() {
        if (inflight) inflight.abort();
        inflight = new AbortController();

        const params = new URLSearchParams();
        const q = (searchInput?.value || '').trim();
        if (q) params.set('q', q);

        const st = (statusFilter?.value || 'RETURNING').trim();
        if (st) params.set('status', st);

        params.set('page', String(page));
        params.set('size', String(size));
        params.set('sort', sort);

        const url = `${BASE}/api/shop/orders?${params.toString()}`;
        const resp = await fetch(url, {
            headers: {Authorization: `Bearer ${token}`},
            signal: inflight.signal
        });
        if (!resp.ok) throw new Error('Load error');
        const data = await resp.json();

        tbody.innerHTML = (data.content || []).map(row).join('');

        // For RETURNING rows, check confirmable to reveal button
        if (st === 'RETURNING') {
            tbody.querySelectorAll('.btn-confirm-received').forEach(btn => {
                const orderId = btn.dataset.id;
                checkConfirmable(orderId, btn);
            });
        }

        // pager
        const totalPages = data.totalPages || 0;
        const totalElements = data.totalElements || 0;
        pagerUl.innerHTML = '';

        function addLi(target, label, active) {
            const li = document.createElement('li');
            li.className = 'page-item' + (active ? ' active' : '');
            const a = document.createElement('a');
            a.className = 'page-link';
            a.href = '#';
            a.textContent = label;
            a.addEventListener('click', (e) => {
                e.preventDefault();
                if (target < 0 || target >= totalPages) return;
                page = target;
                load().catch(console.error);
            });
            li.appendChild(a);
            pagerUl.appendChild(li);
        }

        if (totalPages > 0) {
            addLi(page - 1, 'Prev', false);
            for (let p = 0; p < totalPages; p++) addLi(p, String(p + 1), p === page);
            addLi(page + 1, 'Next', false);
        }

        if (showingText) {
            const start = totalElements === 0 ? 0 : (page * size + 1);
            const end = Math.min(totalElements, (page + 1) * size);
            showingText.textContent = `Showing ${start} to ${end} of ${totalElements} entries`;
        }
    }

    // events
    document.getElementById('btnSearch')?.addEventListener('click', () => {
        page = 0;
        load().catch(console.error);
    });
    statusFilter?.addEventListener('change', () => {
        page = 0;
        load().catch(console.error);
    });
    searchInput?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            page = 0;
            load().catch(console.error);
        }
    });

    // delegate actions
    tbody.addEventListener('click', async (e) => {
        const t = e.target;
        if (!(t instanceof HTMLElement)) return;
        const tr = t.closest('tr');
        if (!tr) return;
        const id = tr.getAttribute('data-id');

        async function call(endpoint) {
            const resp = await fetch(`${BASE}/api/shop/orders/${id}${endpoint}`, {
                method: 'POST',
                headers: {Authorization: `Bearer ${token}`}
            });
            if (!resp.ok) {
                if (resp.status === 409) {
                    alert('Order has not been returned by shipper yet (no RETURN_DELIVER).');
                } else {
                    alert('Action failed.');
                }
            } else {
                load().catch(console.error);
            }
        }

        if (t.classList.contains('btn-approve')) {
            await call('/return/approve');
        } else if (t.classList.contains('btn-reject')) {
            await call('/return/reject');
        } else if (t.classList.contains('btn-confirm-received')) {
            await call('/return/confirm-received');
        }
    });

    // init
    load().catch(console.error);
})();
