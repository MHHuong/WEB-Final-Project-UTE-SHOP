import {showErrorToast, showSuccessToast} from "../../../js/utils/toastUtils.js";

(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    const userId = localStorage.getItem('userId');
    const tbody = document.getElementById('tbData');
    const pager = document.getElementById('pager');
    const modalEl = document.getElementById('orderDetailModal');
    const detailDl = document.getElementById('detailDl');
    const pickupBtn = document.getElementById('pickupBtn');


// Khởi tạo modal (nếu dùng global bootstrap)
    const bsModal = modalEl && window.bootstrap ? new window.bootstrap.Modal(modalEl) : null;
    if (pickupBtn) {
        pickupBtn.addEventListener('click', async () => {
            const id = pickupBtn.dataset.id; // lấy từ data-id đặt khi mở modal
            if (!id) return;
            try {
                pickupBtn.disabled = true;
                await pickup(Number(id));      // gọi hàm pickup đang dùng cho nút ngoài bảng
                bsModal?.hide();
            } catch (e) {
                alert(e.message || 'Pick up failed');
            } finally {
                pickupBtn.disabled = false;
            }
        });
    }
    let page = 0, size = 10, sort = 'createdAt,desc';

    function fmt(v) {
        return (v ?? '') + '';
    }

    function money(v) {
        const n = Number(v ?? 0);
        return n === 0 ? '-' : n.toLocaleString('vi-VN');
    }

    function fmtDate(iso) {
        if (!iso) return '';
        const d = new Date(iso);
        return d.toLocaleDateString('vi-VN', {hour: '2-digit', minute: '2-digit'}) + ' ' +
            d.toLocaleDateString('vi-VN');
    }

    async function load() {
        const url = `${BASE}/api/shipper/orders/confirmed?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`;
        const res = await fetch(url, {headers: {'Authorization': `Bearer ${token}`}});
        if (!res.ok) {
            console.error('Load failed', res.status);
            tbody.innerHTML = `<tr><td colspan="9" class="text-center text-danger">Cannot load data</td></tr>`;
            return;
        }
        const data = await res.json();

        // Chuẩn hoá: API trả content/number/totalPages
        const list = Array.isArray(data.content) ? data.content : [];
        const curPage = (typeof data.page === 'number') ? data.page : (data.number ?? 0);
        const totalPages = data.totalPages ?? 0;

        // Render rows
        tbody.innerHTML = '';
        if (list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-center text-muted py-4">No Order</td></tr>`;
        } else {
            list.forEach(it => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
        <td>#${fmt(it.orderId)}</td>
        <td style="max-width:260px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${fmt(it.productName)}</td>
        <td><span class="badge bg-warning-subtle text-warning">${fmt(it.status)}</span></td>
        <td>${fmt(it.shippingProvider)}</td>
        <td>${fmt(it.shopName)}</td>
        <td>${fmt(it.receiverName)}</td>
        <td>${fmt(it.receiverPhone)}</td>
        <td style="max-width:320px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${fmt(it.receiverAddress)}</td>
        <td>${money(it.amountForCOD)}</td>
        <td class="text-end">
          <div class="btn-group">
            <button class="btn btn-sm btn-light btn-view" data-id="${it.orderId}">View</button>
            <button class="btn btn-sm btn-primary btn-pick" data-id="${it.orderId}">Pick up</button>
          </div>
        </td>
      `;
                // sự kiện
                tr.querySelector('.btn-view').addEventListener('click', () => {
                    if (!detailDl || !bsModal) return;
                    detailDl.innerHTML = `
          <dt class="col-sm-4">Order Id</dt><dd class="col-sm-8">#${fmt(it.orderId)}</dd>
          <dt class="col-sm-4">Product</dt><dd class="col-sm-8">${fmt(it.productName)}</dd>
          <dt class="col-sm-4">Status</dt><dd class="col-sm-8">${fmt(it.status)}</dd>
          <dt class="col-sm-4">Shipping Provider</dt><dd class="col-sm-8">${fmt(it.shippingProvider)}</dd>
          <dt class="col-sm-4">Shop</dt><dd class="col-sm-8">${fmt(it.shopName)}</dd>
          <dt class="col-sm-4">Receiver</dt><dd class="col-sm-8">${fmt(it.receiverName)} — ${fmt(it.receiverPhone)}</dd>
          <dt class="col-sm-4">Address</dt><dd class="col-sm-8">${fmt(it.receiverAddress)}</dd>
          <dt class="col-sm-4">COD</dt><dd class="col-sm-8">${money(it.amountForCOD)}</dd>
          <dt class="col-sm-4">Created at</dt><dd class="col-sm-8">${fmtDate(it.createdAt)}</dd>
        `;
                    pickupBtn?.setAttribute('data-id', it.orderId);
                    bsModal.show();
                });
                tr.querySelector('.btn-pick').addEventListener('click', () => pickup(it.orderId));
                tbody.appendChild(tr);
            });
        }

        // Render pager
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

// Ví dụ pick-up
    async function pickup(orderId) {
        try {
            const res = await fetch(`${BASE}/api/shipper/orders/${orderId}/pickup`, {
                method: 'POST',
                headers: {'Authorization': `Bearer ${token}`}
            });
            if (!res.ok) {
                throw new Error('Pick up failed');
            }
            if (bsModal) bsModal.hide();
            load();
        } catch (e) {
            alert(e.message);
        }
    }

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

