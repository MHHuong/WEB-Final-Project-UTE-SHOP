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
    const tbody = document.querySelector("#tblCategories");
    const pagination = document.querySelector("#pagination"); // ✅ thêm dòng này
    const searchBtn = document.querySelector("#btnSearch");
    const searchInput = document.querySelector("#searchCategory");
    const reloadBtn = document.querySelector("#btnReload");
    const path = window.location.pathname;

    let currentPage = 0;
    const pageSize = 10;

    // ============ RENDER CATEGORY LIST ============
    function renderCategories(categories) {
        tbody.innerHTML = "";
        if (categories.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">Không có danh mục nào</td></tr>`;
            return;
        }

        categories.forEach(c => {
            const productCount = c.products ? c.products.length : 0;

            tbody.insertAdjacentHTML("beforeend", `
        <tr>
          <td>${c.name}</td>
          <td class="text-center">${productCount}</td>
          <td class="text-center">
            <a href="${contextPath}/admin/categories/edit/${c.categoryId}" 
               class="btn btn-sm btn-outline-primary me-1" 
               title="Sửa">
               <i class="bi bi-pencil-square"></i>
            </a>
            <button class="btn btn-sm btn-outline-danger" 
                    data-id="${c.categoryId}" 
                    data-action="delete" 
                    title="Xóa">
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
            btn.addEventListener("click", () => loadCategories(i));
            pagination.appendChild(btn);
        }
    }

    // ============ LOAD CATEGORIES ============
    function loadCategories(page = 0, url = null) {
        currentPage = page;
        const apiUrl = url || `${contextPath}/api/admin/categories?page=${page}&size=${pageSize}`;
        fetch(apiUrl)
            .then(res => res.json())
            .then(data => {
                const categories = data.content || data;
                renderCategories(categories);
                if (data.totalPages !== undefined) renderPagination(data.totalPages, data.number);
            })
            .catch(err => console.error("Lỗi tải danh sách danh mục:", err));
    }

    // ============ PAGE: LIST ============
    if (path.endsWith("/admin/categories")) {
        loadCategories();

        // --- Search ---
        searchBtn?.addEventListener("click", () => {
            const keyword = searchInput.value.trim();
            if (keyword)
                loadCategories(0, `${contextPath}/api/admin/categories/search?q=${encodeURIComponent(keyword)}&page=0&size=${pageSize}`);
            else loadCategories();
        });

        // --- Reload ---
        reloadBtn?.addEventListener("click", () => {
            searchInput.value = "";
            loadCategories();
        });

        // --- Delete category ---
        tbody.addEventListener("click", e => {
            const btn = e.target.closest("button");
            if (!btn) return;
            const id = btn.dataset.id;
            const action = btn.dataset.action;

            if (action === "delete" && confirm("Bạn có chắc chắn muốn xóa danh mục này?")) {
                fetch(`${contextPath}/api/admin/categories/${id}`, { method: "DELETE" })
                    .then(async res => {
                        const msg = await res.text();
                        if (!res.ok) {
                            alert("❌ " + (msg || "Không thể xóa danh mục này vì có sản phẩm liên kết!"));
                        } else {
                            alert("✅ " + (msg || "Xóa thành công!"));
                            loadCategories(currentPage);
                        }
                    })
                    .catch(err => {
                        console.error(err);
                        alert("⚠️ Lỗi kết nối đến máy chủ!");
                    });
            }
        });
    }
});

// ==================== FORM ADD / EDIT CATEGORY ====================
document.addEventListener("DOMContentLoaded", function () {
    const categoryForm = document.getElementById("categoryForm");
    if (!categoryForm) return; // nếu không ở trang add/edit thì bỏ qua

    const params = new URLSearchParams(window.location.search);
    const id = typeof categoryId !== "undefined" && categoryId ? categoryId : params.get("id");

    const nameInput = document.getElementById("categoryName");

    // --- Nếu là trang EDIT ---
    if (id) {
        fetch(`${contextPath}/api/admin/categories/${id}`)
            .then(res => {
                if (!res.ok) throw new Error("Không tìm thấy danh mục!");
                return res.json();
            })
            .then(category => {
                nameInput.value = category.name;
            })
            .catch(err => {
                alert("⚠️ Lỗi tải dữ liệu danh mục!");
                console.error(err);
                window.location.href = `${contextPath}/admin/categories`;
            });
    }

    // --- Submit form ---
    categoryForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const data = { name: nameInput.value.trim() };
        if (!data.name) {
            alert("Vui lòng nhập tên danh mục!");
            return;
        }

        const method = id ? "PUT" : "POST";
        const url = id ? `${contextPath}/api/admin/categories/${id}` : `${contextPath}/api/admin/categories`;

        const res = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        });

        if (res.ok) {
            alert("✅ Lưu danh mục thành công!");
            window.location.href = `${contextPath}/admin/categories`;
        } else {
            const msg = await res.text();
            alert("❌ Lỗi: " + (msg || "Không thể lưu danh mục!"));
        }
    });
});
