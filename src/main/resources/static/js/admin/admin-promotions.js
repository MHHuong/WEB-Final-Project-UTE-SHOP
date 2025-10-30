const contextPath = (() => {
    try {
        const part = window.location.pathname.split('/')[1];
        if (!part || part.toLowerCase() === 'api') return '';
        return '/' + part;
    } catch (e) {
        return '';
    }
})();

// ================== ADMIN PROMOTION JS ==================
document.addEventListener("DOMContentLoaded", function () {
    const tbody = document.querySelector("#tblPromotions tbody") || document.querySelector("#tblPromotions");
    const pagination = document.querySelector("#pagination");
    const searchBtn = document.querySelector("#btnSearch");
    const searchInput = document.querySelector("#searchKeyword");
    const reloadBtn = document.querySelector("#btnReload");
    const path = window.location.pathname;

    let currentPage = 0;
    const pageSize = 10;

    // ============ RENDER PROMOTION LIST ============
    function renderPromotions(promotions) {
        tbody.innerHTML = "";
        if (!promotions || promotions.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-center text-muted">Không có khuyến mãi nào</td></tr>`;
            return;
        }

        promotions.forEach(p => {
            const status = getStatus(p.startDate, p.endDate);
            const statusClass =
                status === "Ongoing" ? "status-ongoing" :
                    status === "Upcoming" ? "status-upcoming" : "status-expired";

            tbody.insertAdjacentHTML("beforeend", `
                <tr>
                    <td>${p.promotionId}</td>
                    <td>${p.title}</td>
                    <td>${p.description || ""}</td>
                    <td>${p.discountPercent ?? 0}%</td>
                    <td>${p.startDate}</td>
                    <td>${p.endDate}</td>
                    <td>${p.applyCategory ? p.applyCategory.name : "<span class='text-secondary'>Tất cả</span>"}</td>
                    <td><span class="status-badge ${statusClass}">${status}</span></td>
                    <td class="text-center">
                        <a href="${contextPath}/admin/promotions/edit?id=${p.promotionId}" 
                           class="btn btn-sm btn-outline-primary me-1" title="Sửa">
                           <i class="bi bi-pencil-square"></i>
                        </a>
                        <button class="btn btn-sm btn-outline-danger" 
                                data-id="${p.promotionId}" data-title="${p.title}" 
                                data-action="delete" title="Xóa">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `);
        });
    }

    // ============ GET STATUS ============
    function getStatus(startDate, endDate) {
        const now = new Date();
        const s = new Date(startDate);
        const e = new Date(endDate);
        if (now < s) return "Upcoming";
        if (now > e) return "Expired";
        return "Ongoing";
    }

    // ============ RENDER PAGINATION ============
    function renderPagination(totalPages, currentPage) {
        pagination.innerHTML = "";
        if (totalPages <= 1) return;

        for (let i = 0; i < totalPages; i++) {
            const btn = document.createElement("button");
            btn.textContent = i + 1;
            btn.className = "btn btn-sm " + (i === currentPage ? "btn-primary" : "btn-outline-primary");
            btn.addEventListener("click", () => loadPromotions(i));
            pagination.appendChild(btn);
        }
    }

    // ============ LOAD PROMOTIONS ============
    function loadPromotions(page = 0, url = null) {
        currentPage = page;
        const apiUrl = url || `${contextPath}/api/admin/promotions?page=${page}&size=${pageSize}`;
        fetch(apiUrl)
            .then(res => res.json())
            .then(data => {
                const list = data.content || data;
                renderPromotions(list);
                if (data.totalPages) renderPagination(data.totalPages, data.number);
            })
            .catch(err => console.error("Lỗi tải danh sách khuyến mãi:", err));
    }

    // ============ PAGE: LIST ============
    if (path.endsWith("/admin/promotions")) {
        loadPromotions();

        // --- Search ---
        searchBtn?.addEventListener("click", () => {
            const keyword = searchInput.value.trim();
            if (keyword)
                loadPromotions(0, `${contextPath}/api/admin/promotions?keyword=${encodeURIComponent(keyword)}&page=0&size=${pageSize}`);
            else loadPromotions();
        });

        // --- Reload ---
        reloadBtn?.addEventListener("click", () => {
            searchInput.value = "";
            loadPromotions();
        });

        // --- Delete ---
        tbody.addEventListener("click", async e => {
            const btn = e.target.closest("button[data-action='delete']");
            if (!btn) return;

            const id = btn.dataset.id;
            const title = btn.dataset.title;

            if (confirm(`Bạn có chắc muốn xóa khuyến mãi "${title}" (ID: ${id})?`)) {
                try {
                    const res = await fetch(`${contextPath}/api/admin/promotions/${id}`, { method: "DELETE" });
                    const msg = await res.text();

                    if (!res.ok) alert("❌ " + (msg || "Không thể xóa!"));
                    else {
                        alert("✅ " + (msg || "Xóa thành công!"));
                        loadPromotions(currentPage);
                    }
                } catch (err) {
                    console.error("Delete failed:", err);
                    alert("⚠️ Lỗi kết nối đến máy chủ!");
                }
            }
        });
    }

    // ============ PAGE: ADD ============
    const form = document.getElementById("promotionForm");
    const isEditPage = path === `${contextPath}/admin/promotions/edit`;

    if (form && !isEditPage) {
        const title = document.getElementById("title");
        const description = document.getElementById("description");
        const discountPercent = document.getElementById("discountPercent");
        const startDate = document.getElementById("startDate");
        const endDate = document.getElementById("endDate");
        const applyCategoryId = document.getElementById("applyCategoryId");

        // ===== LOAD DANH MỤC =====
        fetch(`${contextPath}/api/admin/categories?page=0&size=100`)
            .then(res => res.json())
            .then(data => {
                const select = applyCategoryId;
                const categories = data.content || data;
                select.innerHTML = "";
                const defaultOption = document.createElement("option");
                defaultOption.value = "";
                defaultOption.textContent = "— Apply to all categories —";
                select.appendChild(defaultOption);
                categories.forEach(c => {
                    const opt = document.createElement("option");
                    opt.value = c.categoryId;
                    opt.textContent = c.name;
                    select.appendChild(opt);
                });
            })
            .catch(err => console.error("Lỗi tải danh mục:", err));

        // ===== SUBMIT FORM (ADD) =====
        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const data = {
                title: title.value.trim(),
                description: description.value.trim(),
                discountPercent: discountPercent.value.trim(),
                startDate: startDate.value,
                endDate: endDate.value,
                applyCategoryId: applyCategoryId.value || null
            };

            if (!data.title || !data.discountPercent || !data.startDate || !data.endDate) {
                alert("⚠️ Vui lòng điền đầy đủ thông tin bắt buộc!");
                return;
            }
            if (new Date(data.startDate).toDateString() === new Date().toDateString()) {
                const confirmNow = confirm("⚠️ Khuyến mãi bắt đầu ngay hôm nay. Sẽ không chỉnh sửa được! Bạn có chắc chắn muốn tạo?");
                if (!confirmNow) return;
            }

            if (new Date(data.startDate) > new Date(data.endDate)) {
                alert("⚠️ Ngày bắt đầu không được sau ngày kết thúc!");
                return;
            }

            try {
                const res = await fetch(`${contextPath}/api/admin/promotions`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(data)
                });
                const msg = await res.text();
                if (!res.ok) alert("❌ " + (msg || "Thêm thất bại!"));
                else {
                    alert("✅ " + (msg || "Thêm khuyến mãi thành công!"));
                    window.location.href = `${contextPath}/admin/promotions`;
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
    if (!path.endsWith("/admin/promotions/edit")) return;

    const form = document.getElementById("promotionForm");
    if (!form) return;

    const promotionId = new URLSearchParams(window.location.search).get("id");
    if (!promotionId) {
        alert("⚠️ Không tìm thấy ID khuyến mãi!");
        window.location.href = `${contextPath}/admin/promotions`;
        return;
    }

    const title = document.getElementById("title");
    const description = document.getElementById("description");
    const discountPercent = document.getElementById("discountPercent");
    const startDate = document.getElementById("startDate");
    const endDate = document.getElementById("endDate");
    const applyCategoryId = document.getElementById("applyCategoryId");

    // ===== LOAD DANH MỤC =====
    fetch(`${contextPath}/api/admin/categories?page=0&size=100`)
        .then(res => res.json())
        .then(data => {
            const categories = data.content || data;
            applyCategoryId.innerHTML = "";
            const defaultOpt = document.createElement("option");
            defaultOpt.value = "";
            defaultOpt.textContent = "— Apply to all categories —";
            applyCategoryId.appendChild(defaultOpt);
            categories.forEach(c => {
                const opt = document.createElement("option");
                opt.value = c.categoryId;
                opt.textContent = c.name;
                applyCategoryId.appendChild(opt);
            });
            loadPromotion();
        })
        .catch(err => {
            console.error("Lỗi tải danh mục:", err);
            loadPromotion();
        });

    // ===== LOAD PROMOTION =====
    function loadPromotion() {
        fetch(`${contextPath}/api/admin/promotions/${promotionId}`)
            .then(res => {
                if (!res.ok) throw new Error("Không tìm thấy chương trình khuyến mãi");
                return res.json();
            })
            .then(p => {
                title.value = p.title || "";
                description.value = p.description || "";
                discountPercent.value = p.discountPercent || "";
                startDate.value = p.startDate;
                endDate.value = p.endDate;
                applyCategoryId.value = p.applyCategory?.categoryId || "";
            })
            .catch(err => {
                alert("⚠️ Lỗi tải dữ liệu khuyến mãi!");
                console.error(err);
                window.location.href = `${contextPath}/admin/promotions`;
            });
    }

    // ===== SUBMIT FORM (EDIT) =====
    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const data = {
            title: title.value.trim(),
            description: description.value.trim(),
            discountPercent: discountPercent.value.trim(),
            startDate: startDate.value,
            endDate: endDate.value,
            applyCategoryId: applyCategoryId.value || null
        };

        if (!data.title || !data.discountPercent || !data.startDate || !data.endDate) {
            alert("⚠️ Vui lòng điền đầy đủ thông tin bắt buộc!");
            return;
        }

        if (new Date(data.startDate) > new Date(data.endDate)) {
            alert("⚠️ Ngày bắt đầu không được sau ngày kết thúc!");
            return;
        }

        try {
            const res = await fetch(`${contextPath}/api/admin/promotions/${promotionId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data)
            });
            const msg = await res.text();
            if (!res.ok) alert("❌ " + (msg || "Cập nhật thất bại!"));
            else {
                alert("✅ " + (msg || "Cập nhật khuyến mãi thành công!"));
                window.location.href = `${contextPath}/admin/promotions`;
            }
        } catch (err) {
            console.error(err);
            alert("⚠️ Lỗi kết nối đến máy chủ!");
        }
    });
});
