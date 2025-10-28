(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }
    const tbody = document.getElementById('tbData'), pager = document.getElementById('pager');
    let page = 0, size = 10;

    function fmt(v) {
        return v ?? '';
    }

    function money(n) {
        return (n == null) ? '0' : Number(n).toLocaleString();
    }

    async function load() {
        const res = await fetch(`${BASE}/api/shipper/history/delivered?page=${page}&size=${size}`, {
            headers: {'Authorization': `Bearer ${token}`}
        });
        if (!res.ok) {
            alert('Tải lịch sử thất bại');
            return;
        }
        const data = await res.json();
        tbody.innerHTML = '';
        (data.items || []).forEach(it => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
        <td>#${fmt(it.orderId)}</td>
        <td>${fmt(it.productName)}</td>
        <td>${fmt(it.status)}</td>
        <td>${fmt(it.shippingProvider)}</td>
        <td>${fmt(it.shopName)}</td>
        <td>${fmt(it.receiverName)}</td>
        <td>${fmt(it.receiverPhone)}</td>
        <td style="max-width:280px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${fmt(it.receiverAddress)}</td>
        <td>${money(it.amountForCOD)}</td>
      `;
            tbody.appendChild(tr);
        });

        pager.innerHTML = '';
        const tp = data.totalPages || 0;
        for (let i = 0; i < tp; i++) {
            const li = document.createElement('li');
            li.className = 'page-item' + (i === data.page ? ' active' : '');
            const a = document.createElement('a');
            a.className = 'page-link';
            a.href = 'javascript:void(0)';
            a.textContent = (i + 1);
            a.addEventListener('click', () => {
                page = i;
                load();
            });
            li.appendChild(a);
            pager.appendChild(li);
        }
    }

    load();
})();
