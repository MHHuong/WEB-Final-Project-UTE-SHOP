// ================== ADMIN SHIPPER JS ==================
document.addEventListener("DOMContentLoaded", function () {
    const tbody = document.querySelector("#tblShippers tbody") || document.querySelector("#tblShippers");
    const pagination = document.querySelector("#pagination");
    const searchBtn = document.querySelector("#btnSearch");
    const searchInput = document.querySelector("#searchKeyword");
    const reloadBtn = document.querySelector("#btnReload");
    const path = window.location.pathname;

    let currentPage = 0;
    const pageSize = 10;

    // ============ RENDER SHIPPER LIST ============
    function renderShippers(list) {
        tbody.innerHTML = "";
        if (!list || list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No shippers found</td></tr>`;
            return;
        }

        list.forEach(s => {
            tbody.insertAdjacentHTML("beforeend", `
                <tr>
                    <td>${s.shipperId}</td>
                    <td>${s.user?.fullName || '—'}</td>
                    <td>${s.companyName || '—'}</td>
                    <td>${s.phone || s.user?.phone || '—'}</td>
                    <td><span class="badge-provider">${s.shippingProvider?.name || '—'}</span></td>
                    <td class="text-center">
                        <a href="/admin/shippers/edit?id=${s.shipperId}"
                           class="btn btn-sm btn-outline-primary me-1" title="Edit">
                           <i class="bi bi-pencil-square"></i>
                        </a>
                        <button class="btn btn-sm btn-outline-danger"
                                data-id="${s.shipperId}" data-name="${s.user?.fullName || s.companyName}"
                                data-action="delete" title="Delete">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `);
        });
    }

    // ============ RENDER PAGINATION ============
    function renderPagination(totalPages, currentPage) {
        pagination.innerHTML = "";
        if (totalPages <= 1) return;

        for (let i = 0; i < totalPages; i++) {
            const btn = document.createElement("button");
            btn.textContent = i + 1;
            btn.className = "btn btn-sm " + (i === currentPage ? "btn-primary" : "btn-outline-primary");
            btn.addEventListener("click", () => loadShippers(i));
            pagination.appendChild(btn);
        }
    }

    // ============ LOAD SHIPPERS ============
    function loadShippers(page = 0, url = null) {
        currentPage = page;
        const apiUrl = url || `/api/admin/shippers?page=${page}&size=${pageSize}`;
        fetch(apiUrl)
            .then(res => res.json())
            .then(data => {
                const list = data.content || data;
                renderShippers(list);
                if (data.totalPages) renderPagination(data.totalPages, data.number);
            })
            .catch(err => console.error("Error loading shippers:", err));
    }

    // ============ PAGE: LIST ============
    if (path === "/admin/shippers") {
        loadShippers();

        searchBtn?.addEventListener("click", () => {
            const keyword = searchInput.value.trim();
            if (keyword)
                loadShippers(0, `/api/admin/shippers?keyword=${encodeURIComponent(keyword)}&page=0&size=${pageSize}`);
            else loadShippers();
        });

        reloadBtn?.addEventListener("click", () => {
            searchInput.value = "";
            loadShippers();
        });

        tbody.addEventListener("click", async e => {
            const btn = e.target.closest("button[data-action='delete']");
            if (!btn) return;

            const id = btn.dataset.id;
            const name = btn.dataset.name;

            if (confirm(`Are you sure you want to delete shipper "${name}" (ID: ${id})?`)) {
                try {
                    const res = await fetch(`/api/admin/shippers/${id}`, { method: "DELETE" });
                    const msg = await res.text();

                    if (!res.ok) alert("❌ " + (msg || "Unable to delete!"));
                    else {
                        alert("✅ Deleted successfully!");
                        loadShippers(currentPage);
                    }
                } catch (err) {
                    console.error("Delete failed:", err);
                    alert("⚠️ Connection error!");
                }
            }
        });
    }

    // ============ PAGE: ADD ============
    const form = document.getElementById("shipperForm");
    const isEditPage = path === "/admin/shippers/edit";

    if (form && !isEditPage) {
        const userId = document.getElementById("userId");
        const companyName = document.getElementById("companyName");
        const phone = document.getElementById("phone");
        const providerId = document.getElementById("shippingProviderId");

        // ===== LOAD USERS & PROVIDERS =====
        async function loadUsers() {
            try {
                const res = await fetch("/api/admin/users?size=100");
                const data = await res.json();
                const users = data.content || data;
                userId.innerHTML = `<option value="">-- Select Shipper User --</option>`;
                users.filter(u => u.role === "SHIPPER").forEach(u => {
                    const opt = document.createElement("option");
                    opt.value = u.userId;
                    opt.textContent = `${u.fullName || "(No name)"} (${u.email})`;
                    userId.appendChild(opt);
                });
                if (window.$ && $.fn.select2) $('#userId').select2({ width: '100%' });
            } catch (err) {
                console.error("Error loading users:", err);
            }
        }

        async function loadProviders() {
            try {
                const res = await fetch("/api/admin/shipping-providers?size=100");
                const data = await res.json();
                const providers = data.content || data;
                providerId.innerHTML = `<option value="">-- Select Provider --</option>`;
                providers.forEach(p => {
                    const opt = document.createElement("option");
                    opt.value = p.shippingProviderId;
                    opt.textContent = `${p.name} (${Number(p.fee).toLocaleString()} ₫, ${p.estimatedDays} days)`;
                    providerId.appendChild(opt);
                });
                if (window.$ && $.fn.select2) $('#shippingProviderId').select2({ width: '100%' });
            } catch (err) {
                console.error("Error loading providers:", err);
            }
        }

        loadUsers();
        loadProviders();

        // ===== SUBMIT FORM (ADD) =====
        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const data = {
                user: { userId: parseInt(userId.value || 0) },
                companyName: companyName.value.trim(),
                phone: phone.value.trim(),
                shippingProvider: { shippingProviderId: parseInt(providerId.value || 0) }
            };

            if (!data.user.userId || !data.shippingProvider.shippingProviderId) {
                alert("⚠️ Please select valid user and provider!");
                return;
            }

            try {
                const res = await fetch("/api/admin/shippers", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(data)
                });
                const msg = await res.text();
                if (!res.ok) alert("❌ " + (msg || "Failed to add shipper!"));
                else {
                    alert("✅ Shipper added successfully!");
                    window.location.href = "/admin/shippers";
                }
            } catch (err) {
                console.error(err);
                alert("⚠️ Server connection error!");
            }
        });
    }

    // ============ PAGE: EDIT ============
    if (isEditPage) {
        const shipperId = new URLSearchParams(window.location.search).get("id");
        if (!shipperId) {
            alert("⚠️ Shipper ID not found!");
            window.location.href = "/admin/shippers";
            return;
        }

        const companyName = document.getElementById("companyName");
        const phone = document.getElementById("phone");
        const providerId = document.getElementById("shippingProviderId");

        async function loadProviders() {
            const res = await fetch("/api/admin/shipping-providers?size=100");
            const data = await res.json();
            const providers = data.content || data;
            providerId.innerHTML = `<option value="">-- Select Provider --</option>`;
            providers.forEach(p => {
                const opt = document.createElement("option");
                opt.value = p.shippingProviderId;
                opt.textContent = `${p.name} (${Number(p.fee).toLocaleString()} ₫, ${p.estimatedDays} days)`;
                providerId.appendChild(opt);
            });
        }

        async function loadShipper() {
            const res = await fetch(`/api/admin/shippers/${shipperId}`);
            if (!res.ok) throw new Error("Shipper not found");
            const s = await res.json();
            companyName.value = s.companyName || "";
            phone.value = s.phone || "";
            await loadProviders();
            providerId.value = s.shippingProvider?.shippingProviderId || "";
        }

        loadShipper();

        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const data = {
                companyName: companyName.value.trim(),
                phone: phone.value.trim(),
                shippingProvider: { shippingProviderId: parseInt(providerId.value || 0) }
            };

            const res = await fetch(`/api/admin/shippers/${shipperId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data)
            });

            const msg = await res.text();
            if (!res.ok) alert("❌ " + (msg || "Update failed!"));
            else {
                alert("✅ Shipper updated successfully!");
                window.location.href = "/admin/shippers";
            }
        });
    }
});
