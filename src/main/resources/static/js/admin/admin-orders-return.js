const contextPath = (() => {
    try {
        const part = window.location.pathname.split('/')[1];
        if (!part || part.toLowerCase() === 'api') return '';
        return '/' + part;
    } catch (e) {
        return '';
    }
})();

document.addEventListener("DOMContentLoaded", function () {
    const tbody = document.querySelector("#tblReturnOrders tbody") || document.querySelector("#tblReturnOrders");
    const pagination = document.querySelector("#pagination");
    const reloadBtn = document.querySelector("#btnReload");
    const searchInput = document.querySelector("#searchKeyword");
    const statusSelect = document.querySelector("#filterStatus");
    const btnSearch = document.querySelector("#btnSearch");
    const btnReset = document.querySelector("#btnReset");

    let allOrders = []; // ✅ lưu toàn bộ dữ liệu để lọc local
    let currentPage = 0;
    const pageSize = 10;

    // ======= RENDER TABLE =======
    function renderOrders(orders) {
        tbody.innerHTML = "";
        if (!orders || orders.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-center text-muted">No return orders found</td></tr>`;
            return;
        }

        orders.forEach(o => {
            let statusClass = "";
            let statusLabel = o.status;
            switch (o.status) {
                case "REQUEST_RETURN":
                    statusClass = "status-request";
                    statusLabel = "REQUEST RETURN";
                    break;
                case "RETURNING":
                    statusClass = "status-returning";
                    statusLabel = "RETURNING";
                    break;
                case "RETURNED":
                    statusClass = "status-returned";
                    statusLabel = "RETURNED";
                    break;
                case "CANCELLED":
                    statusClass = "status-cancelled";
                    statusLabel = "CANCELLED";
                    break;
            }

            let actions = "";
            if (o.status === "REQUEST_RETURN") {
                actions = `
                    <button class="btn btn-sm btn-success" data-id="${o.orderId}" data-action="approve">
                        <i class="bi bi-check-circle"></i> Approve
                    </button>
                    <button class="btn btn-sm btn-danger" data-id="${o.orderId}" data-action="reject">
                        <i class="bi bi-x-circle"></i> Reject
                    </button>`;
            } else if (o.status === "RETURNING") {
                actions = `
                    <button class="btn btn-sm btn-primary" data-id="${o.orderId}" data-action="confirm">
                        <i class="bi bi-box-arrow-in-down"></i> Confirm Received
                    </button>`;
            } else {
                actions = `<span class="text-muted fst-italic">Not available</span>`;
            }

            tbody.insertAdjacentHTML("beforeend", `
                <tr>
                    <td>${o.orderId}</td>
                    <td>${o.shopName}</td>
                    <td>${o.userName}</td>
                    <td>${o.totalAmount.toLocaleString("vi-VN")} ₫</td>
                    <td>${o.paymentMethod}</td>
                    <td><span class="status-badge ${statusClass}">${statusLabel}</span></td>
                    <td>${o.createdAt ? o.createdAt.replace("T", " ").slice(0, 19) : ""}</td>
                    <td>${o.note || "<span class='text-secondary'>No note</span>"}</td>
                    <td class="text-center">${actions}</td>
                </tr>
            `);
        });
    }

    // ======= PHÂN TRANG =======
    function renderPagination(totalPages, currentPage) {
        pagination.innerHTML = "";
        if (totalPages <= 1) return;

        for (let i = 0; i < totalPages; i++) {
            const btn = document.createElement("button");
            btn.textContent = i + 1;
            btn.className = "btn btn-sm " + (i === currentPage ? "btn-primary" : "btn-outline-primary");
            btn.addEventListener("click", () => displayPage(i));
            pagination.appendChild(btn);
        }
    }

    // ======= HIỂN THỊ TRANG HIỆN TẠI =======
    function displayPage(page = 0, filtered = allOrders) {
        currentPage = page;
        const start = page * pageSize;
        const end = start + pageSize;
        renderOrders(filtered.slice(start, end));
        renderPagination(Math.ceil(filtered.length / pageSize), page);
    }

    // ======= LỌC LOCAL (SEARCH & FILTER) =======
    function filterOrders() {
        const keyword = searchInput?.value?.trim().toLowerCase() || "";
        const status = statusSelect?.value || "";

        return allOrders.filter(o => {
            const matchKeyword =
                o.userName?.toLowerCase().includes(keyword) ||
                o.shopName?.toLowerCase().includes(keyword) ||
                o.note?.toLowerCase().includes(keyword);
            const matchStatus = !status || o.status === status;
            return matchKeyword && matchStatus;
        });
    }

    // ======= LOAD DỮ LIỆU TỪ API CHÍNH =======
    function loadOrders() {
        fetch(`${contextPath}/api/admin/orders/returns?page=0&size=1000`) // ✅ tải hết 1 lần
            .then(res => res.json())
            .then(data => {
                allOrders = data.content || data;
                displayPage(0);
            })
            .catch(err => console.error("Error loading return orders:", err));
    }

    // ======= CÁC NÚT =======
    btnSearch?.addEventListener("click", () => {
        const filtered = filterOrders();
        displayPage(0, filtered);
    });

    btnReset?.addEventListener("click", () => {
        searchInput.value = "";
        statusSelect.value = "";
        displayPage(0, allOrders);
    });

    reloadBtn?.addEventListener("click", () => {
        loadOrders();
    });

    // ======= HÀNH ĐỘNG (approve / reject / confirm) =======
    tbody.addEventListener("click", async e => {
        const btn = e.target.closest("button[data-action]");
        if (!btn) return;

        const id = btn.dataset.id;
        const action = btn.dataset.action;
        let url = "", msg = "";

        switch (action) {
            case "approve":
                url = `${contextPath}/api/admin/orders/${id}/approve-return`;
                msg = "✅ Return approved!";
                break;
            case "reject":
                url = `${contextPath}/api/admin/orders/${id}/reject-return`;
                msg = "❌ Return rejected!";
                break;
            case "confirm":
                url = `${contextPath}/api/admin/orders/${id}/confirm-returned`;
                msg = "📦 Return confirmed!";
                break;
        }

        if (!url) return;
        if (!confirm(`Are you sure you want to ${action} order #${id}?`)) return;

        try {
            const res = await fetch(url, {method: "PUT"});
            if (!res.ok) throw new Error(await res.text());
            alert(msg);
            loadOrders();
        } catch (err) {
            alert("⚠️ Action failed: " + err.message);
        }
    });

    loadOrders();
});
