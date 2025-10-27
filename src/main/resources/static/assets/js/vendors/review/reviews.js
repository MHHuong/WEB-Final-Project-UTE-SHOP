(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');

    function mustAuth() {
        if (!token) {
            alert('Login to continue');
            window.location.href = BASE + '/login';
            return false;
        }
        return true;
    }

    const tbody = document.getElementById('reviewTableBody');
    // Lấy đúng ô search & combobox rating từ HTML hiện tại
    const searchInput = document.getElementById('reviewSearch');
    const ratingSelect = document.getElementById('ratingFilter');
    const pagerUl = document.querySelector('.pagination.mb-0');
    const showingText = document.querySelector('.border-top span');

    let page = 0, size = 10, sort = 'createdAt,desc';
    let inflight;

    function fmtDate(iso) {
        if (!iso) return '';
        const d = new Date(iso);
        return d.toLocaleDateString('en-GB', {day: '2-digit', month: 'short', year: 'numeric'});
    }

    function renderStars(rating) {
        const n = Math.max(0, Math.min(5, +rating || 0));
        let html = '<div>';
        for (let i = 0; i < 5; i++) {
            html += `<i class="bi ${i < n ? 'bi-star-fill text-warning' : 'bi-star'} me-1"></i>`;
        }
        html += '</div>';
        return html;
    }

    function escapeHtml(s) {
        return (s ?? '').toString()
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    async function load() {
        if (!mustAuth()) return;

        if (inflight) inflight.abort?.();
        inflight = new AbortController();

        const params = new URLSearchParams();
        const q = (searchInput?.value || '').trim();
        if (q) params.set('q', q);

        // lọc theo sao (nếu người dùng chọn 1..5)
        const ratingVal = (ratingSelect?.value || '').trim();
        if (/^[1-5]$/.test(ratingVal)) params.set('rating', ratingVal);

        params.set('page', page);
        params.set('size', size);
        params.set('sort', sort);

        const url = `${BASE}/api/shop/reviews?` + params.toString();
        const res = await fetch(url, {
            headers: {
                'Accept': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            signal: inflight.signal
        });
        if (!res.ok) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-danger">Cannot load data</td></tr>`;
            return;
        }
        const data = await res.json(); // PageResult<ReviewItemRes>

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No reviews found</td></tr>`;
            renderShowing({page: data.page, size: data.size, totalElements: data.totalElements || 0});
            renderPager(data.page, data.totalPages || 0);
            return;
        }

        const rows = data.content.map((item, i) => {
            const productName = escapeHtml(item.productName ?? '(N/A)');
            const userName = escapeHtml(item.userName ?? '(Guest)');
            const comment = escapeHtml(item.comment ?? '');
            const created = fmtDate(item.createdAt);
            const productLink = `<a class="text-reset" href="${BASE}/shop/product/product-detail?id=${item.productId}">${productName}</a>`;

            return `
        <tr>
          <td>${productLink}</td>
          <td>${userName}</td>
          <td><span class="text-truncate" title="${comment}">${comment.length > 64 ? comment.slice(0, 61) + '...' : comment}</span></td>
          <td>${renderStars(item.rating)}</td>
          <td>${created}</td>
          <td class="text-end">
            <div class="btn-group">
              <a class="btn btn-sm btn-outline-secondary" href="${BASE}/shop/review/review-detail?id=${item.reviewId}">View</a>
            </div>
          </td>
        </tr>
      `;
        }).join('');

        tbody.innerHTML = rows;
        renderShowing(data);
        renderPager(data.page, data.totalPages);
    }

    function renderShowing(data) {
        if (!showingText) return;
        const total = data.totalElements || 0;
        const start = total === 0 ? 0 : (data.page * data.size + 1);
        const end = Math.min((data.page + 1) * data.size, total);
        showingText.textContent = `Showing ${start} to ${end} of ${total} entries`;
    }

    function renderPager(p, totalPages) {
        if (!pagerUl) return;
        pagerUl.innerHTML = '';

        const mk = (label, target, disabled = false, active = false) => {
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

        pagerUl.appendChild(mk('Previous', Math.max(0, p - 1), p === 0));
        const start = Math.max(0, p - 2);
        const end = Math.min(totalPages - 1, p + 2);
        for (let i = start; i <= end; i++) pagerUl.appendChild(mk(String(i + 1), i, false, i === p));
        pagerUl.appendChild(mk('Next', Math.min(totalPages - 1, p + 1), p >= totalPages - 1));
    }

    // Search + lọc sao: debounce + Enter (giống products/promotions)
    function debounce(fn, delay = 250) {
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
    }, 250));
    searchInput?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            page = 0;
            load();
        }
    });

    ratingSelect?.addEventListener('change', () => {
        page = 0;
        load();
    });

    document.addEventListener('DOMContentLoaded', () => load());
})();
