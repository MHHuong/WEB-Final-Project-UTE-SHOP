import {showErrorToast, showSuccessToast} from "../../../../js/utils/toastUtils.js";

(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    const tbody = document.getElementById('ordersTbody');
    const searchInput = document.getElementById('orderSearch');
    const statusFilter = document.getElementById('orderStatusFilter');
    const pagerUl = document.querySelector('.pagination.mb-0');
    const showingText = document.querySelector('.border-top span');

    let page = 0, size = 10, sort = 'createdAt,desc';
    let inflight;

    const fmtMoney = v => (v ?? 0).toLocaleString('vi-VN') + ' VND';
    const fmtDate = iso => iso ? new Date(iso).toLocaleString() : '';

    const badge = st => {
        switch (String(st).toUpperCase()) {
            case 'NEW':
                return 'bg-warning';
            case 'CONFIRMED':
                return 'bg-success';
            case 'CANCELLED':
                return 'bg-secondary';
            case 'SHIPPING':
                return 'bg-info';
            case 'DELIVERED':
                return 'bg-info';
            case 'RECEIVED':
                return 'bg-primary';
            case 'RETURNED':
                return 'bg-dark';
            default:
                return 'bg-light text-dark';
        }
    };

    function row(o) {
        const BASE = '/UTE_SHOP';
        const thumb = o.thumbnailUrl
            ? `<img src="${o.thumbnailUrl}" class="icon-shape icon-xxl" style="width:48px;height:48px;object-fit:cover;border-radius:8px;" alt="">`
            : `<div class="icon-shape icon-xxl bg-light rounded-3 d-inline-flex align-items-center justify-content-center" style="width:48px;height:48px;">
           <i class="bi bi-box-seam"></i>
         </div>`;

        const link = `<a class="text-inherit" href="${BASE}/shop/order/order-single?orderId=${o.orderId}">
                  <h6 class="mb-0">#${o.orderId}</h6>
                </a>`;

        const customer = `${o.customerName || ''}<div class="text-muted small">${o.customerEmail || ''}</div>`;
        const dateStr = o.createdAt ? new Date(o.createdAt).toLocaleString() : '';
        const payment = o.paymentMethod || '';
        const amount = (o.amount ?? 0).toLocaleString('vi-VN') + ' VND';
        const statusHtml = `<span class="badge ${badge(o.status)}">${o.status}</span>`;
        const actions = `<a class="btn btn-sm btn-outline-primary" href="${BASE}/shop/order/order-single?orderId=${o.orderId}">View</a>`;

        return `<tr>
    <td>
      <div class="form-check">
        <input class="form-check-input" type="checkbox" value="${o.orderId}" />
        <label class="form-check-label"></label>
      </div>
    </td>
    <td>${thumb}</td>
    <td>${link}</td>
    <td>${customer}</td>
    <td>${dateStr}</td>
    <td>${payment}</td>
    <td>${statusHtml}</td>
    <td class="fw-semibold">${amount}</td>
    <td class="text-end">${actions}</td>
  </tr>`;
    }


    async function load() {
        if (inflight) inflight.abort();
        inflight = new AbortController();

        const params = new URLSearchParams();
        const q = (searchInput?.value || '').trim();
        if (q) params.set('q', q);
        const st = (statusFilter?.value || '').trim();
        if (st && /^(NEW|CONFIRMED|CANCELLED|SHIPPING|DELIVERED|RECEIVED|RETURNED)$/i.test(st)) {
            params.set('status', st.toUpperCase());
        }
        if (st) params.set('status', st);
        params.set('page', page);
        params.set('size', size);
        params.set('sort', sort);

        const res = await fetch(`${BASE}/api/shop/orders?` + params.toString(), {
            headers: {'Authorization': 'Bearer ' + token}
        });
        if (!res.ok) {
            alert('Failed to load orders');
            return;
        }
        const data = await res.json();

        tbody.innerHTML = (data.content || []).map(row).join('');

        // pagination
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
                load();
            });
            li.appendChild(a);
            pagerUl.appendChild(li);
        }

        addLi(page - 1, '«', false);
        for (let i = 0; i < totalPages; i++) addLi(i, String(i + 1), i === page);
        addLi(page + 1, '»', false);

        if (showingText) {
            const start = totalElements === 0 ? 0 : (page * size + 1);
            const end = Math.min((page + 1) * size, totalElements);
            showingText.textContent = `Showing ${start} to ${end} of ${totalElements} entries`;
        }
    }

    searchInput?.addEventListener('input', () => {
        page = 0;
        load();
    });
    statusFilter?.addEventListener('change', () => {
        page = 0;
        load();
    });

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
