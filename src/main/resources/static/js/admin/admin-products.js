document.addEventListener("DOMContentLoaded", function () {
    const tbody = document.querySelector("#tblProducts");
    const searchBtn = document.querySelector("#btnSearch");
    const searchInput = document.querySelector("#searchProduct");
    const searchShopBtn = document.querySelector("#btnSearchShop");
    const searchShopInput = document.querySelector("#searchShop");
    const categorySelect = document.querySelector("#categoryFilter");
    const reloadBtn = document.querySelector("#btnReload");
    const dropdown = document.querySelector("#categoryFilter");

    let currentPage = 0;
    const pageSize = 10;

    // ================= RENDER PRODUCT LIST =================
    // ================= RENDER PRODUCT LIST =================
    function renderProducts(products) {
        tbody.innerHTML = "";
        if (products.length === 0) {
            tbody.innerHTML = `<tr><td colspan="10" class="text-center text-muted">Không có sản phẩm nào</td></tr>`;
            return;
        }

        products.forEach(p => {
            tbody.insertAdjacentHTML("beforeend", `
        <tr>
          <td><input type="checkbox"></td>
          <td><img src="${p.media?.[0]?.url || '/assets/images/sample/snack.jpg'}" class="product-img"></td>
          <td>${p.name}</td>
          <td>${p.category ? p.category.name : '-'}</td>
          <td>${p.shop ? p.shop.shopName : '-'}</td>
          <td>
            ${p.status === 1
                ? '<span class="badge bg-success">Active</span>'
                : '<span class="badge bg-danger">Inactive</span>'}
          </td>
          <td class="text-end">$${p.price.toFixed(2)}</td>
          <td class="text-center">${p.stock || 0}</td>
          <td>${formatDate(p.createdAt)}</td>
          <td class="text-center">
              <button class="btn btn-sm btn-outline-warning me-1" 
                      data-id="${p.productId}" data-action="toggle" 
                      title="${p.status === 1 ? 'Hide product' : 'Show product'}">
                  <i class="bi ${p.status === 1 ? 'bi-eye-slash-fill' : 'bi-eye-fill'}"></i>
              </button>
              <button class="btn btn-sm btn-outline-danger" 
                      data-id="${p.productId}" data-action="delete" 
                      title="Delete product">
                  <i class="bi bi-trash"></i>
              </button>
          </td>
        </tr>
      `);
        });
    }

    // ================= LOAD PRODUCTS =================
    function loadProducts(page = 0, url = null) {
        currentPage = page;
        const apiUrl = url || `/api/admin/products?page=${page}&size=${pageSize}`;
        fetch(apiUrl)
            .then(res => res.json())
            .then(data => {
                const products = data.content || data;
                renderProducts(products);
            })
            .catch(err => console.error("Lỗi tải danh sách sản phẩm:", err));
    }

    loadProducts();

    // ================= SEARCH BY PRODUCT NAME =================
    searchBtn?.addEventListener("click", () => {
        const keyword = searchInput.value.trim();
        if (keyword)
            loadProducts(0, `/api/admin/products/search/name?q=${encodeURIComponent(keyword)}&page=0&size=${pageSize}`);
        else loadProducts();
    });

    // ================= SEARCH BY SHOP NAME =================
    searchShopBtn?.addEventListener("click", () => {
        const shopName = searchShopInput.value.trim();
        if (shopName)
            loadProducts(0, `/api/admin/products/search/shop?q=${encodeURIComponent(shopName)}&page=0&size=${pageSize}`);
        else loadProducts();
    });

    // ================= FILTER CATEGORY =================
    categorySelect?.addEventListener("change", () => {
        const cateId = categorySelect.value;
        if (cateId)
            loadProducts(0, `/api/admin/products?categoryId=${cateId}`);
        else loadProducts();
    });

    // ================= RELOAD =================
    reloadBtn?.addEventListener("click", () => {
        searchInput.value = "";
        searchShopInput.value = "";
        categorySelect.value = "";
        loadProducts();
    });

    // ================= ACTION BUTTONS =================
    tbody.addEventListener("click", e => {
        const btn = e.target.closest("button");
        if (!btn) return;

        const id = btn.dataset.id;
        const action = btn.dataset.action;

        if (action === "toggle") {
            const icon = btn.querySelector("i");
            const newStatus = icon.classList.contains("bi-eye-slash-fill") ? 0 : 1;

            fetch(`/api/admin/products/${id}/status?status=${newStatus}`, { method: "PUT" })
                .then(res => res.text())
                .then(() => {
                    alert("✅ Cập nhật trạng thái thành công!");
                    loadProducts(currentPage);
                })
                .catch(err => console.error(err));
        }

        if (action === "delete") {
            if (confirm("Bạn có chắc chắn muốn xóa sản phẩm này?")) {
                fetch(`/api/admin/products/${id}`, { method: "DELETE" })
                    .then(res => res.text())
                    .then(() => {
                        alert("✅ Xóa thành công!");
                        loadProducts(currentPage);
                    })
                    .catch(err => console.error(err));
            }
        }
    });

    // ================= FORMAT DATE =================
    function formatDate(isoString) {
        if (!isoString) return "-";
        const d = new Date(isoString);
        return d.toLocaleDateString("en-GB");
    }

    // ================= LOAD CATEGORY OPTIONS =================
    function loadCategories() {
        fetch("/api/admin/categories?page=0&size=100")
            .then(res => res.json())
            .then(data => {
                const select = document.getElementById("categoryFilter");
                select.innerHTML = `<option value="">— Filter by category —</option>`;
                const categories = data.content || data;
                categories.forEach(c => {
                    select.insertAdjacentHTML("beforeend",
                        `<option value="${c.categoryId}">${c.name}</option>`);
                });
            })
            .catch(err => console.error("Lỗi tải danh mục:", err));
    }

    // ✅ GỌI HÀM SAU KHI DOM READY
    loadCategories();
});
