const contextPath = (() => {
    try {
        const part = window.location.pathname.split('/')[1];
        if (!part || part.toLowerCase() === 'api') return '';
        return '/' + part;
    } catch (e) {
        return '';
    }
})();

// ================== ADMIN RETURN ORDERS JS ==================
document.addEventListener("DOMContentLoaded", function () {
    const tbody = document.querySelector("#tblReturnOrders tbody") || document.querySelector("#tblReturnOrders");
    const pagination = document.querySelector("#pagination");
    const reloadBtn = document.querySelector("#btnReload");
    const toastEl = document.getElementById("toast");
    const toastMsg = document.getElementById("toastMsg");

    let currentPage = 0;
    const pageSize = 10;

    // ======= TOAST =======
    function showToast(message, success = true) {
        toastMsg.textContent = message;
        toastEl.classList.remove("text-bg-danger", "text-bg-success");
        toastEl.classList.add(success ? "text-bg-success" : "text-bg-danger");
        const toast = new bootstrap.Toast(toastEl);
        toast.show();
    }

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

            // Action buttons depend on status
            let actions = "";
            if (o.status === "REQUEST_RETURN") {
                actions = `
                    <button class="btn btn-sm btn-success" data-id="${o.orderId}" data-action="approve">
                        <i class="bi bi-check-circle"></i> Approve
                    </button>
                    <button class="btn btn-sm btn-danger" data-id="${o.orderId}" data-action="reject">
                        <i class="bi bi-x-circle"></i> Reject
                    </button>
                `;
            } else if (o.status === "RETURNING") {
                actions = `
                    <button class="btn btn-sm btn-primary" data-id="${o.orderId}" data-action="confirm">
                        <i class="bi bi-box-arrow-in-down"></i> Confirm Received
                    </button>
                `;
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

    // ======= RENDER PAGINATION =======
    function renderPagination(totalPages, currentPage) {
        pagination.innerHTML = "";
        if (totalPages <= 1) return;

        for (let i = 0; i < totalPages; i++) {
            const btn = document.createElement("button");
            btn.textContent = i + 1;
            btn.className = "btn btn-sm " + (i === currentPage ? "btn-primary" : "btn-outline-primary");
            btn.addEventListener("click", () => loadOrders(i));
            pagination.appendChild(btn);
        }
    }

    // ======= LOAD DATA =======
    function loadOrders(page = 0) {
        currentPage = page;
        fetch(`${contextPath}/api/admin/orders/returns?page=${page}&size=${pageSize}`)
            .then(res => res.json())
            .then(data => {
                const list = data.content || data;
                renderOrders(list);
                if (data.totalPages) renderPagination(data.totalPages, data.number);
            })
            .catch(err => console.error("Error loading return orders:", err));
    }

    // ======= HANDLE ACTIONS =======
    tbody.addEventListener("click", async e => {
        const btn = e.target.closest("button[data-action]");
        if (!btn) return;

        const id = btn.dataset.id;
        const action = btn.dataset.action;

        let url = "";
        let successMsg = "";

        switch (action) {
            case "approve":
                url = `${contextPath}/api/admin/orders/${id}/approve-return`;
                successMsg = "✅ Return request approved successfully!";
                break;
            case "reject":
                url = `${contextPath}/api/admin/orders/${id}/reject-return`;
                successMsg = "❌ Return request rejected!";
                break;
            case "confirm":
                url = `${contextPath}/api/admin/orders/${id}/confirm-returned`;
                successMsg = "📦 Return confirmed successfully!";
                break;
        }

        if (!url) return;
        if (!confirm(`Are you sure you want to perform this action for order #${id}?`)) return;

        try {
            const res = await fetch(url, { method: "PUT" });
            const msg = await res.text();

            if (!res.ok) {
                alert("⚠️ " + (msg || "Action failed!"));
            } else {
                alert(successMsg);
                loadOrders(currentPage); // ✅ reload immediately
            }
        } catch (err) {
            console.error("Action failed:", err);
            alert("⚠️ Server connection error!");
        }
    });

    // ======= RELOAD BUTTON =======
    reloadBtn?.addEventListener("click", () => {
        loadOrders(currentPage);
        showToast("Refreshed successfully!");
    });

    // ======= INIT =======
    loadOrders();
});
