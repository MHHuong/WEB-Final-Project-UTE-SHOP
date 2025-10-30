import {showErrorToast, showSuccessToast} from "../../../js/utils/toastUtils.js";

(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const tbody = document.getElementById('tbData');
    const pager = document.getElementById('pager');
    const modalEl = document.getElementById('shipModal');
    const detailDl = document.getElementById('detailDl');
    const deliveredBtn = document.getElementById('deliveredBtn');

    // Khởi tạo modal an toàn
    const bsModal = (modalEl && window.bootstrap) ? new window.bootstrap.Modal(modalEl) : null;

    let page = 0, size = 10, sort = 'createdAt,desc', currentId = null;

    const fmt = v => v ?? '';
    const money = n => (n == null || Number(n) === 0) ? '-' : Number(n).toLocaleString('vi-VN');

    function buildDetail(it) {
        detailDl.innerHTML = `
      <dt class="col-sm-3">Order Id</dt><dd class="col-sm-9">#${fmt(it.orderId)}</dd>
      <dt class="col-sm-3">Product</dt><dd class="col-sm-9">${fmt(it.productName)}</dd>
      <dt class="col-sm-3">Status</dt><dd class="col-sm-9">${fmt(it.status)}</dd>
      <dt class="col-sm-3">Shipping Provider</dt><dd class="col-sm-9">${fmt(it.shippingProvider)}</dd>
      <dt class="col-sm-3">Shop</dt><dd class="col-sm-9">${fmt(it.shopName)}</dd>
      <dt class="col-sm-3">Receiver</dt><dd class="col-sm-9">${fmt(it.receiverName)}</dd>
      <dt class="col-sm-3">Phone</dt><dd class="col-sm-9">${fmt(it.receiverPhone)}</dd>
      <dt class="col-sm-3">Address</dt><dd class="col-sm-9" style="word-break:break-word">${fmt(it.receiverAddress)}</dd>
      <dt class="col-sm-3">COD</dt><dd class="col-sm-9">${money(it.amountForCOD)}</dd>
    `;
    }

    async function load() {
        const url = `${BASE}/api/shipper/orders/shipping?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`;
        const res = await fetch(url, {headers: {'Authorization': `Bearer ${token}`}});
        if (!res.ok) {
            alert('Load failed');
            return;
        }
        const data = await res.json();

        // Chuẩn hoá Page shape
        const list = Array.isArray(data.content) ? data.content : (data.items ?? []);
        const curPage = (typeof data.page === 'number') ? data.page : (data.number ?? 0);
        const totalPages = data.totalPages ?? 0;

        tbody.innerHTML = '';
        if (list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="10" class="text-center text-muted py-4">No Order</td></tr>`;
        } else {
            list.forEach(it => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
          <td>#${fmt(it.orderId)}</td>
          <td style="max-width:260px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${fmt(it.productName)}</td>
          <td><span class="badge bg-info-subtle text-info">${fmt(it.status)}</span></td>
          <td>${fmt(it.shippingProvider)}</td>
          <td>${fmt(it.shopName)}</td>
          <td>${fmt(it.receiverName)}</td>
          <td>${fmt(it.receiverPhone)}</td>
          <td style="max-width:320px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${fmt(it.receiverAddress)}</td>
          <td>${money(it.amountForCOD)}</td>
          <td class="text-end">
            <div class="btn-group">
              <button class="btn btn-sm btn-light btn-view">View</button>
              <button class="btn btn-sm btn-success btn-done">Delivered</button>
            </div>
          </td>
        `;
                tr.querySelector('.btn-view').addEventListener('click', () => {
                    currentId = it.orderId;
                    buildDetail(it);
                    if (deliveredBtn) deliveredBtn.dataset.id = String(it.orderId); // <-- gắn id cho nút modal
                    bsModal?.show();
                });
                tr.querySelector('.btn-done').addEventListener('click', () => deliver(it.orderId));
                tbody.appendChild(tr);
            });
        }

        // Phân trang
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

    async function deliver(orderId) {
        if (!confirm('Confirm delivered?')) return;
        const res = await fetch(`${BASE}/api/shipper/orders/${orderId}/deliver`, {
            method: 'POST', headers: {'Authorization': `Bearer ${token}`}
        });
        if (!res.ok) {
            const t = await res.text();
            alert('Deliver thất bại: ' + t);
            return;
        }
        bsModal?.hide();
        await load();
    }

    // GẮN 1 LẦN: click cho nút trong modal
    if (deliveredBtn) {
        deliveredBtn.addEventListener('click', () => {
            const id = deliveredBtn.dataset.id;
            if (id) deliver(Number(id));
        });
    }

    const userId = localStorage.getItem('userId');
    let stompClient = null;

    function connect() {
        let token = localStorage.getItem("authToken");
        if (!token) {
            showErrorToast("Please log in to continue");
            window.location.href = '/UTE_SHOP/login';
            return;
        }
        const socket = new SockJS("http://localhost:8082/UTE_SHOP/ws?token=" + token);
        stompClient = Stomp.over(socket);
        stompClient.connect(
            {},
            function (frame) {
                stompClient.subscribe('/user/queue/orders', function (message) {
                    try {
                        const body = JSON.parse(message.body);
                        if (Number(body.userId) === Number(userId)) {
                            load();
                            showSuccessToast(`Order #${body.orderId} status updated to ${body.status}`);
                        }
                    } catch (e) {
                        console.log('Parse error:', e);
                        alert('📦 Message received (raw):\n' + message.body);
                    }
                });
                if (Notification.permission === "default") {
                    Notification.requestPermission();
                }
            },
            function (error) {
                log('❌ STOMP error: ' + JSON.stringify(error), 'err');
                updateStatus(false);
                document.getElementById('connectBtn').disabled = false;
                document.getElementById('disconnectBtn').disabled = true;
            });
    }

    document.addEventListener('DOMContentLoaded', () => {
        load();
        connect();
    });
})();
