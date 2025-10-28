// ================== ADMIN SHIPPING PROVIDER JS ==================
document.addEventListener("DOMContentLoaded", function () {
    const tbody = document.querySelector("#tblProviders tbody") || document.querySelector("#tblProviders");
    const pagination = document.querySelector("#pagination");
    const searchBtn = document.querySelector("#btnSearch");
    const searchInput = document.querySelector("#searchKeyword");
    const reloadBtn = document.querySelector("#btnReload");
    const path = window.location.pathname;

    let currentPage = 0;
    const pageSize = 10;

    // ============ RENDER PROVIDER LIST ============
    function renderProviders(providers) {
        tbody.innerHTML = "";
        if (!providers || providers.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">Không có nhà vận chuyển nào</td></tr>`;
            return;
        }

        providers.forEach(p => {
            tbody.insertAdjacentHTML("beforeend", `
                <tr>
                    <td>${p.shippingProviderId}</td>
                    <td>${p.name}</td>
                    <td><span class="badge-fee">${Number(p.fee).toLocaleString()} ₫</span></td>
                    <td><span class="badge-days">${p.estimatedDays} ngày</span></td>
                    <td class="text-center">
                        <a href="/admin/shipping-providers/edit?id=${p.shippingProviderId}" 
                           class="btn btn-sm btn-outline-primary me-1" title="Sửa">
                           <i class="bi bi-pencil-square"></i>
                        </a>
                        <button class="btn btn-sm btn-outline-danger" 
                                data-id="${p.shippingProviderId}" data-name="${p.name}" 
                                data-action="delete" title="Xóa">
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
            btn.addEventListener("click", () => loadProviders(i));
            pagination.appendChild(btn);
        }
    }

    // ============ LOAD PROVIDERS ============
    function loadProviders(page = 0, url = null) {
        currentPage = page;
        const apiUrl = url || `/api/admin/shipping-providers?page=${page}&size=${pageSize}`;
        fetch(apiUrl)
            .then(res => res.json())
            .then(data => {
                const list = data.content || data;
                renderProviders(list);
                if (data.totalPages) renderPagination(data.totalPages, data.number);
            })
            .catch(err => console.error("Lỗi tải danh sách nhà vận chuyển:", err));
    }

    // ============ PAGE: LIST ============
    if (path === "/admin/shipping-providers") {
        loadProviders();

        // --- Search ---
        searchBtn?.addEventListener("click", () => {
            const keyword = searchInput.value.trim();
            if (keyword)
                loadProviders(0, `/api/admin/shipping-providers?keyword=${encodeURIComponent(keyword)}&page=0&size=${pageSize}`);
            else loadProviders();
        });

        // --- Reload ---
        reloadBtn?.addEventListener("click", () => {
            searchInput.value = "";
            loadProviders();
        });

        // --- Delete ---
        tbody.addEventListener("click", async e => {
            const btn = e.target.closest("button[data-action='delete']");
            if (!btn) return;

            const id = btn.dataset.id;
            const name = btn.dataset.name;

            if (confirm(`Are you sure you want to delete the shipping provider "${name}" (ID: ${id})?`)) {
                try {
                    const res = await fetch(`/api/admin/shipping-providers/${id}`, { method: "DELETE" });
                    const msg = await res.text();

                    if (!res.ok) alert("❌ " + (msg || "Unable to delete!"));
                    else {
                        alert("✅ " + ("Deleted successfully!"));
                        loadProviders(currentPage);
                    }
                } catch (err) {
                    console.error("Delete failed:", err);
                    alert("⚠️ Lỗi kết nối đến máy chủ!");
                }
            }
        });
    }

    // ============ PAGE: ADD ============
    const form = document.getElementById("providerForm");
    const isEditPage = path === "/admin/shipping-providers/edit";

    if (form && !isEditPage) {
        const name = document.getElementById("name");
        const fee = document.getElementById("fee");
        const estimatedDays = document.getElementById("estimatedDays");

        // ===== SUBMIT FORM (ADD) =====
        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const data = {
                name: name.value.trim(),
                fee: parseFloat(fee.value || 0),
                estimatedDays: parseInt(estimatedDays.value || 0)
            };

            if (!data.name || data.fee <= 0 || data.estimatedDays <= 0) {
                alert("⚠️ Please enter all required and valid information!");
                return;
            }

            if (!data.name || data.name.trim().length < 3) {
                alert("⚠️ Provider name must be at least 3 characters!");
                return;
            }

            if (data.fee < 0 || data.fee > 1000000) {
                alert("⚠️ Shipping fee must be between 0 and 1,000,000 ₫!");
                return;
            }

            if (data.estimatedDays < 1 || data.estimatedDays > 14) {
                alert("⚠️ Estimated delivery time must be between 1 and 14 days!");
                return;
            }

            try {
                const res = await fetch("/api/admin/shipping-providers", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(data)
                });
                const msg = await res.text();
                if (!res.ok) alert("❌ " + (msg || "Failed to add shipping provider!"));
                else {
                    alert("✅ " + ("Shipping provider added successfully!"));
                    window.location.href = "/admin/shipping-providers";
                }
            } catch (err) {
                console.error(err);
                alert("⚠️ Lỗi kết nối đến máy chủ!");
            }
        });
    }
});

// ============ PAGE: EDIT ============
document.addEventListener("DOMContentLoaded", function () {
    const path = window.location.pathname;
    if (path !== "/admin/shipping-providers/edit") return;

    const form = document.getElementById("providerForm");
    if (!form) return;

    const providerId = new URLSearchParams(window.location.search).get("id");
    if (!providerId) {
        alert("⚠️ Shipping provider ID not found!");
        window.location.href = "/admin/shipping-providers";
        return;
    }

    const name = document.getElementById("name");
    const fee = document.getElementById("fee");
    const estimatedDays = document.getElementById("estimatedDays");

    // ===== LOAD PROVIDER =====
    function loadProvider() {
        fetch(`/api/admin/shipping-providers/${providerId}`)
            .then(res => {
                if (!res.ok) throw new Error("Shipping provider not found");
                return res.json();
            })
            .then(p => {
                name.value = p.name || "";
                fee.value = p.fee || 0;
                estimatedDays.value = p.estimatedDays || 0;
            })
            .catch(err => {
                alert("⚠️ Failed to load shipping provider data!");
                console.error(err);
                window.location.href = "/admin/shipping-providers";
            });
    }
    loadProvider();

    // ===== SUBMIT FORM (EDIT) =====
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const data = {
            name: name.value.trim(),
            fee: parseFloat(fee.value || 0),
            estimatedDays: parseInt(estimatedDays.value || 0)
        };

        if (!data.name || data.fee <= 0 || data.estimatedDays <= 0) {
            alert("⚠️ Please enter all required and valid information!");
            return;
        }

        if (data.fee < 0 || data.fee > 1000000) {
            alert("⚠️ Shipping fee must be between 0 and 1,000,000 ₫!");
            return;
        }

        if (!data.name || data.name.trim().length < 3) {
            alert("⚠️ Provider name must be at least 3 characters!");
            return;
        }

        if (data.estimatedDays < 1 || data.estimatedDays > 14) {
            alert("⚠️ Estimated delivery time must be between 1 and 14 days!");
            return;
        }
        try {
            const res = await fetch(`/api/admin/shipping-providers/${providerId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data)
            });

            const msg = await res.text();
            if (!res.ok) alert("❌ " + (msg || "Update failed!"));
            else {
                alert("✅ " + ("Shipping provider updated successfully!"));
                window.location.href = "/admin/shipping-providers";
            }
        } catch (err) {
            console.error(err);
            alert("⚠️ Connection error to the server!");
        }
    });
});
