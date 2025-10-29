(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const tbody = document.getElementById('tbData');
    const pager = document.getElementById('pager');
    const modalEl = document.getElementById('orderDetailModal');
    const detailDl = document.getElementById('detailDl');
    const pickupBtn = document.getElementById('pickupBtn');
    const bsModal = new bootstrap.Modal(modalEl);

    let page = 0, size = 10, sort = 'createdAt,desc';
    let currentId = null;

    function fmt(v) {
        return v ?? '';
    }

    function money(n) {
        return (n == null) ? '0' : Number(n).toLocaleString();
    }

    function buildDetail(it) {
        detailDl.innerHTML = `
      <dt class="col-sm-3">Mã đơn</dt><dd class="col-sm-9">#${fmt(it.orderId)}</dd>
      <dt class="col-sm-3">Sản phẩm</dt><dd class="col-sm-9">${fmt(it.productName)}</dd>
      <dt class="col-sm-3">Trạng thái</dt><dd class="col-sm-9">${fmt(it.status)}</dd>
      <dt class="col-sm-3">Đơn vị VC</dt><dd class="col-sm-9">${fmt(it.shippingProvider)}</dd>
      <dt class="col-sm-3">Shop</dt><dd class="col-sm-9">${fmt(it.shopName)}</dd>
      <dt class="col-sm-3">Người nhận</dt><dd class="col-sm-9">${fmt(it.receiverName)}</dd>
      <dt class="col-sm-3">SĐT</dt><dd class="col-sm-9">${fmt(it.receiverPhone)}</dd>
      <dt class="col-sm-3">Địa chỉ</dt><dd class="col-sm-9" style="word-break:break-word">${fmt(it.receiverAddress)}</dd>
      <dt class="col-sm-3">Thu COD</dt><dd class="col-sm-9">${money(it.amountForCOD)}</dd>
    `;
    }

    async function load() {
        const url = `${BASE}/api/shipper/orders/confirmed?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`;
        const res = await fetch(url, {headers: {'Authorization': `Bearer ${token}`}});
        if (!res.ok) {
            alert('Tải danh sách thất bại');
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
        <td class="text-end">
          <div class="btn-group">
            <button class="btn btn-sm btn-light btn-view">View</button>
            <button class="btn btn-sm btn-primary btn-pick">Pick up</button>
          </div>
        </td>
      `;
            tr.querySelector('.btn-view').addEventListener('click', () => {
                currentId = it.orderId;
                buildDetail(it);
                bsModal.show();
            });
            tr.querySelector('.btn-pick').addEventListener('click', () => pickup(it.orderId));
            tbody.appendChild(tr);
        });

        // pager
        pager.innerHTML = '';
        const totalPages = data.totalPages || 0;
        for (let i = 0; i < totalPages; i++) {
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

    async function pickup(orderId) {
        if (!confirm('Xác nhận nhận đơn này?')) return;
        const res = await fetch(`${BASE}/api/shipper/orders/${orderId}/pickup`, {
            method: 'POST',
            headers: {'Authorization': `Bearer ${token}`}
        });
        if (!res.ok) {
            const t = await res.text();
            alert('Pick up thất bại: ' + t);
            return;
        }
        bsModal.hide();
        await load();
    }

    pickupBtn.addEventListener('click', () => {
        if (currentId) pickup(currentId);
    });
    load();
})();
