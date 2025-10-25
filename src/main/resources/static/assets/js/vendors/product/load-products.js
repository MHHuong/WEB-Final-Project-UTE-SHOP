(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    // DOM refs
    const tbody = document.getElementById('productsTbody');
    const searchInput = document.getElementById('searchInput');
    const statusFilter = document.getElementById('statusFilter');

    // Pagination UI (dùng sẵn khung có sẵn)
    const pagerUl = document.querySelector('.pagination.mb-0');
    const showingText = document.querySelector('.border-top span'); // “Showing 1 to ...”
    // State
    let page = 0, size = 10, sort = 'createdAt,desc';

    async function load() {
        const params = new URLSearchParams();
        const q = (searchInput?.value || '').trim();
        if (q) params.set('q', q);
        const st = statusFilter?.value;
        if (st !== '' && st != null) params.set('status', st);

        params.set('page', page);
        params.set('size', size);
        params.set('sort', sort);

        const res = await fetch(`${BASE}/api/shop/products?` + params.toString(), {
            headers: {'Authorization': 'Bearer ' + token}
        });
        if (!res.ok) {
            const t = await res.text();
            console.error(t);
            tbody.innerHTML = `<tr><td colspan="8" class="text-center text-danger">Load products failed</td></tr>`;
            return;
        }
        const data = await res.json(); // {content, page, size, totalElements, totalPages}

        renderRows(data.content || []);
        renderShowing(data);
        renderPager(data.page, data.totalPages);
    }

    function renderRows(items) {
        if (!items.length) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">No products found.</td></tr>`;
            return;
        }
        const frag = document.createDocumentFragment();
        for (const it of items) {
            const tr = document.createElement('tr');

            // Checkbox
            const tdCheck = document.createElement('td');
            tdCheck.innerHTML = `
        <div class="form-check">
          <input class="form-check-input" type="checkbox"/>
        </div>`;
            tr.appendChild(tdCheck);

            // Image (media đầu tiên). Nếu là video -> dùng icon/thumb mặc định
            const tdImg = document.createElement('td');
            const thumb = BASE + it.thumbnailUrl || '';
            const isVideo = thumb.includes('/videos/') || /\.mp4|\.webm|\.mkv|\.mov|\.avi$/i.test(thumb);
            const thumbSrc = isVideo ? '/UTE_SHOP/assets/images/png/iphone-2.png' : (thumb || '/UTE_SHOP/assets/images/png/iphone-2.png');
            tdImg.innerHTML = `
        <a href="#!"><img src="${thumbSrc}" alt="" class="icon-shape icon-md"/></a>`;
            tr.appendChild(tdImg);

            // Name
            const tdName = document.createElement('td');
            tdName.innerHTML = `<a href="#" class="text-reset">${escapeHtml(it.name)}</a>`;
            tr.appendChild(tdName);

            // Category
            const tdCat = document.createElement('td');
            tdCat.textContent = it.categoryName || '';
            tr.appendChild(tdCat);

            // Status badge
            const tdStatus = document.createElement('td');
            tdStatus.innerHTML = statusBadge(it.status);
            tr.appendChild(tdStatus);

            // Price
            const tdPrice = document.createElement('td');
            tdPrice.textContent = formatMoney(it.price);
            tr.appendChild(tdPrice);

            // CreatedAt
            const tdCreated = document.createElement('td');
            tdCreated.textContent = formatDate(it.createdAt);
            tr.appendChild(tdCreated);

            // Actions
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
        const windowStart = Math.max(0, p - 2);
        const windowEnd = Math.min(totalPages - 1, p + 2);
        for (let i = windowStart; i <= windowEnd; i++) {
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
        const num = Number(n || 0);
        return '$' + num.toFixed(2);
    }

    function formatDate(iso) {
        if (!iso) return '';
        const d = new Date(iso);
        // dd MMM yyyy
        const opts = {day: '2-digit', month: 'short', year: 'numeric'};
        return d.toLocaleDateString('en-US', opts);
        // Nếu muốn HH:mm dd/MM/yyyy:
        // return d.toLocaleString('vi-VN');
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

    // Events
    searchInput?.addEventListener('keydown', e => {
        if (e.key === 'Enter') {
            page = 0;
            load();
        }
    });
    statusFilter?.addEventListener('change', () => {
        page = 0;
        load();
    });

    // Kick off
    load();
})();