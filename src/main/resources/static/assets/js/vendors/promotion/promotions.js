(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        alert('Login to continue');
        window.location.href = BASE + '/login';
        return;
    }

    const tbody = document.getElementById('promotionsTbody');
    const pager = document.getElementById('promoPagination');
    const statusFilterEl = document.getElementById('statusFilter');
    const createBtn = document.getElementById('createPromoBtn');

    if (createBtn) {
        createBtn.addEventListener('click', () => {
            window.location.href = BASE + '/shop/promotion/add-promotion';
        });
    }

    // State
    let page = 0;
    let size = 10;
    let totalPages = 0;
    let statusFilter = '';

    // Helpers
    function fmtDate(isoOrLocal) {
        if (!isoOrLocal) return '';
        const d = new Date(isoOrLocal);
        if (isNaN(d)) {
            // LocalDate (yyyy-MM-dd) => add 'T00:00:00'
            const parts = (isoOrLocal + 'T00:00:00');
            const d2 = new Date(parts);
            if (isNaN(d2)) return isoOrLocal;
            return d2.toLocaleDateString();
        }
        return d.toLocaleDateString();
    }

    function statusOf(startDate, endDate) {
        const today = new Date();
        const s = new Date(startDate + 'T00:00:00');
        const e = new Date(endDate + 'T23:59:59');
        if (today < s) return 'Upcoming';
        if (today > e) return 'Expired';
        return 'Active';
    }

    function authFetch(url, opt = {}) {
        return fetch(url, {
            ...opt,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token,
                ...(opt.headers || {})
            }
        }).then(r => {
            if (r.status === 401 || r.status === 403) {
                alert('Expired session');
                window.location.href = BASE + '/login';
                return Promise.reject(new Error('unauth'));
            }
            return r;
        });
    }

    function loadPage(p = 0) {
        authFetch(`${BASE}/shop/promotions?page=${p}&size=${size}`)
            .then(r => r.json())
            .then(showData)
            .catch(console.error);
    }

    function showData(pg) {
        // pg: PageResult<PromotionVM>
        tbody.innerHTML = '';
        const content = Array.isArray(pg.content) ? pg.content : [];
        totalPages = pg.totalPages ?? 0;
        page = pg.page ?? 0;

        // Filter client-side theo status nếu có
        const filtered = content.filter(it => {
            if (!statusFilter) return true;
            const st = statusOf(it.startDate, it.endDate).toLowerCase();
            return st === statusFilter;
        });

        if (filtered.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">No promotions found</td></tr>`;
        } else {
            filtered.forEach((it, idx) => {
                const st = statusOf(it.startDate, it.endDate);
                const tr = document.createElement('tr');

                tr.innerHTML = `
          <td>${page * size + idx + 1}</td>
          <td class="fw-semibold">${escapeHtml(it.title || '')}</td>
          <td>${escapeHtml(it.applyCategoryName || 'All categories')}</td>
          <td>${it.discountPercent ?? ''}</td>
          <td>${fmtDate(it.startDate)}</td>
          <td>${fmtDate(it.endDate)}</td>
          <td>
            <span class="badge ${badgeClass(st)}">${st}</span>
          </td>
          <td>
            <div class="btn-group btn-group-sm" role="group">
              <button class="btn btn-outline-secondary" data-act="edit" data-id="${it.promotionId}">Edit</button>
              <button class="btn btn-outline-danger" data-act="del" data-id="${it.promotionId}">Delete</button>
            </div>
          </td>
        `;
                tbody.appendChild(tr);
            });
        }

        renderPager();
    }

    function badgeClass(st) {
        const s = (st || '').toLowerCase();
        if (s === 'active') return 'bg-success';
        if (s === 'upcoming') return 'bg-info';
        return 'bg-secondary';
    }

    function renderPager() {
        pager.innerHTML = '';
        if (totalPages <= 1) return;

        function pageItem(label, p, active = false, disabled = false) {
            const li = document.createElement('li');
            li.className = `page-item ${active ? 'active' : ''} ${disabled ? 'disabled' : ''}`;
            const a = document.createElement('a');
            a.className = 'page-link';
            a.href = 'javascript:void(0)';
            a.textContent = label;
            if (!disabled) {
                a.onclick = () => loadPage(p);
            }
            li.appendChild(a);
            return li;
        }

        pager.appendChild(pageItem('«', Math.max(0, page - 1), false, page === 0));
        for (let i = 0; i < totalPages; i++) {
            pager.appendChild(pageItem(String(i + 1), i, page === i, false));
        }
        pager.appendChild(pageItem('»', Math.min(totalPages - 1, page + 1), false, page === totalPages - 1));
    }

    tbody.addEventListener('click', (e) => {
        const btn = e.target.closest('button[data-act]');
        if (!btn) return;
        const id = btn.getAttribute('data-id');
        const act = btn.getAttribute('data-act');

        if (act === 'edit') {
            window.location.href = `${BASE}/shop/promotion/edit-promotion?id=${id}`;
        } else if (act === 'del') {
            if (confirm('Xác nhận xóa promotion này?')) {
                authFetch(`${BASE}/shop/promotions/${id}`, {method: 'DELETE'})
                    .then(r => {
                        if (r.ok) {
                            loadPage(page);
                        } else {
                            return r.text().then(t => {
                                throw new Error(t || 'Delete failed');
                            });
                        }
                    })
                    .catch(err => alert(err.message));
            }
        }
    });

    if (statusFilterEl) {
        statusFilterEl.addEventListener('change', () => {
            statusFilter = (statusFilterEl.value || '').toLowerCase();
            // reload current cached page (không call API lại)
            loadPage(page);
        });
    }

    function escapeHtml(s) {
        return (s || '').replace(/[&<>"']/g, m => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        })[m]);
    }

    // First load
    loadPage(0);
})();
