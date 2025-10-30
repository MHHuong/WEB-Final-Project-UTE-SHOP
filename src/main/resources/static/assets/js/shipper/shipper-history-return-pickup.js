(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const tbody = document.getElementById('tbData');
    const pager = document.getElementById('pager');
    let page = 0, size = 10;

    const fmt = v => v ?? '';
    const money = n => (n == null || Number(n) === 0) ? '-' : Number(n).toLocaleString('vi-VN');

    async function load() {
        const res = await fetch(`${BASE}/api/shipper/history/return/pickup?page=${page}&size=${size}`, {
            headers: {'Authorization': `Bearer ${token}`}
        });
        if (!res.ok) {
            alert('Load failed');
            return;
        }
        const data = await res.json();

        const list = Array.isArray(data.content) ? data.content : (data.items ?? []);
        const curPage = (typeof data.page === 'number') ? data.page : (data.number ?? 0);
        const totalPages = data.totalPages ?? 0;

        tbody.innerHTML = '';
        if (list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-center text-muted py-4">No Order</td></tr>`;
        } else {
            list.forEach(it => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
          <td>#${fmt(it.orderId)}</td>
          <td style="max-width:260px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${fmt(it.productName)}</td>
          <td>${fmt(it.status)}</td>
          <td>${fmt(it.shippingProvider)}</td>
          <td>${fmt(it.shopName)}</td>
          <td>${fmt(it.receiverName)}</td>
          <td>${fmt(it.receiverPhone)}</td>
          <td style="max-width:320px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${fmt(it.receiverAddress)}</td>
          <td>${money(it.amountForCOD)}</td>
        `;
                tbody.appendChild(tr);
            });
        }

        pager.innerHTML = '';
        for (let i = 0; i < totalPages; i++) {
            const li = document.createElement('li');
            li.className = 'page-item' + (i === curPage ? ' active' : '');
            const a = document.createElement('a');
            a.className = 'page-link';
            a.href = 'javascript:void(0)';
            a.textContent = (i + 1).toString();
            a.addEventListener('click', () => {
                page = i;
                load();
            });
            li.appendChild(a);
            pager.appendChild(li);
        }
    }

    document.addEventListener('DOMContentLoaded', load);
})();
