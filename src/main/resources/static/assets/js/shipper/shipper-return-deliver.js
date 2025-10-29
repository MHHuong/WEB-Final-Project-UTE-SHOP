(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const tbody = document.getElementById('tbData');
    const pager = document.getElementById('pager');
    const modalEl = document.getElementById('returnDeliverModal');
    const detailDl = document.getElementById('detailDl');
    const deliverBtn = document.getElementById('returnDeliverBtn');

    const bsModal = (modalEl && window.bootstrap) ? new window.bootstrap.Modal(modalEl) : null;

    let page = 0, size = 10;
    const fmt = (v) => v ?? '';
    const money = n => (n == null || Number(n) === 0) ? '-' : Number(n).toLocaleString('vi-VN');

    async function load() {
        const url = `${BASE}/api/shipper/orders/return/deliver?page=${page}&size=${size}`;
        const res = await fetch(url, {headers: {'Authorization': `Bearer ${token}`}});
        if (!res.ok) {
            alert('Load failed');
            return;
        }
        const data = await res.json();
        render(data);
    }

    function render(pr) {
        const {content = [], totalPages = 1, page: curPage = 0} = pr || {};
        tbody.innerHTML = '';

        content.forEach(it => {
            const tr = document.createElement('tr');

            tr.innerHTML = `
        <td>#${fmt(it.orderId)}</td>
        <td>${fmt(it.productName)}</td>
        <td>${fmt(it.status)}</td>
        <td>${fmt(it.shippingProvider)}</td>
        <td>${fmt(it.shopName)}</td>
        <td>${fmt(it.receiverName)}</td>
        <td>${fmt(it.receiverPhone)}</td>
        <td>${fmt(it.receiverAddress)}</td>
        <td>${money(it.amountForCOD)}</td>
        <td>
          <button class="btn btn-sm btn-success btn-done">Deliver</button>
          <button class="btn btn-sm btn-outline-secondary btn-view">View</button>
        </td>
      `;

            tr.querySelector('.btn-view').addEventListener('click', () => openDetail(it));
            tr.querySelector('.btn-done').addEventListener('click', () => doReturnDeliver(it.orderId));

            tbody.appendChild(tr);
        });

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

    function openDetail(it) {
        detailDl.innerHTML = `
      <dt class="col-sm-3">Mã đơn</dt><dd class="col-sm-9">#${fmt(it.orderId)}</dd>
      <dt class="col-sm-3">Sản phẩm</dt><dd class="col-sm-9">${fmt(it.productName)}</dd>
      <dt class="col-sm-3">Trạng thái</dt><dd class="col-sm-9">${fmt(it.status)}</dd>
      <dt class="col-sm-3">Shop</dt><dd class="col-sm-9">${fmt(it.shopName)}</dd>
      <dt class="col-sm-3">Người nhận</dt><dd class="col-sm-9">${fmt(it.receiverName)}</dd>
      <dt class="col-sm-3">SĐT</dt><dd class="col-sm-9">${fmt(it.receiverPhone)}</dd>
      <dt class="col-sm-3">Địa chỉ nhận</dt><dd class="col-sm-9">${fmt(it.receiverAddress)}</dd>
      <dt class="col-sm-3">COD</dt><dd class="col-sm-9">${money(it.amountForCOD)}</dd>
    `;
        if (deliverBtn) deliverBtn.dataset.id = it.orderId;
        bsModal?.show();
    }

    async function doReturnDeliver(orderId) {
        if (!confirm('Confirm?')) return;
        const res = await fetch(`${BASE}/api/shipper/orders/${orderId}/return/deliver`, {
            method: 'POST',
            headers: {'Authorization': `Bearer ${token}`}
        });
        if (!res.ok) {
            alert('Deliver failed');
            return;
        }
        bsModal?.hide();
        load();
    }

    if (deliverBtn) {
        deliverBtn.addEventListener('click', () => {
            const id = deliverBtn.dataset.id;
            if (id) doReturnDeliver(Number(id));
        });
    }

    document.addEventListener('DOMContentLoaded', load);
})();
