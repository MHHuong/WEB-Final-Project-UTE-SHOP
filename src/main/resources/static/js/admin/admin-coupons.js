document.addEventListener("DOMContentLoaded", function () {
    const tbody = document.querySelector("#tblCoupons");
    const searchBtn = document.querySelector("#btnSearch");
    const searchInput = document.querySelector("#searchCode");
    const reloadBtn = document.querySelector("#btnReload");
    const saveBtn = document.querySelector("#btnSaveCoupon");
    const couponForm = document.querySelector("#couponForm");

    // ============ RENDER COUPON LIST ============
    function renderCoupons(coupons) {
        tbody.innerHTML = "";
        if (!coupons || coupons.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted">Không có coupon nào</td></tr>`;
            return;
        }

        coupons.forEach(c => {
            const shopName = c.shop ? c.shop.shopName : "<span class='text-secondary'>Admin App</span>";
            tbody.insertAdjacentHTML("beforeend", `
            <tr>
                <td>${c.code}</td>
                <td>${c.discountType}</td>
                <td>${Number(c.value).toLocaleString()}</td>
                <td>${Number(c.minOrderAmount).toLocaleString()}</td>
                <td>${new Date(c.expiredAt).toLocaleString()}</td>
                <td>${shopName}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-danger" 
                            data-id="${c.couponId}" 
                            data-action="delete"
                            title="Xóa coupon">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>
        `);
        });
    }

    // ============ LOAD COUPONS ============
    function loadCoupons(url = `/api/admin/coupons/app`) {
        fetch(url)
            .then(res => res.json())
            .then(data => {
                const coupons = data.content || data;
                renderCoupons(coupons);
            })
            .catch(err => console.error("Lỗi tải danh sách coupon:", err));
    }

    // ============ ADD COUPON ============
    saveBtn?.addEventListener("click", async (e) => {
        e.preventDefault();

        const coupon = {
            code: document.querySelector("#code").value.trim(),
            discountType: document.querySelector("#discountType").value,
            value: parseFloat(document.querySelector("#value").value),
            minOrderAmount: parseFloat(document.querySelector("#minOrder").value) || 0,
            expiredAt: document.querySelector("#expiredAt").value
        };

        if (!coupon.code || !coupon.value || !coupon.expiredAt) {
            alert("⚠️ Vui lòng nhập đầy đủ thông tin bắt buộc!");
            return;
        }

        try {
            const res = await fetch(`/api/admin/coupons/app`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(coupon)
            });

            const msg = await res.text();

            if (res.ok) {
                alert("✅ Thêm coupon thành công!");
                couponForm?.reset();
                window.location.href = "/admin/coupons";
            } else {
                // 👇 xử lý lỗi trả về từ backend (như IllegalArgumentException)
                alert("❌ " + (msg || "Không thể lưu coupon!"));
            }
        } catch (err) {
            console.error("Fetch error:", err);
            alert("⚠️ Lỗi kết nối đến máy chủ!");
        }
    });

    // ============ DELETE ============
    tbody?.addEventListener("click", async (e) => {
        const btn = e.target.closest("button");
        if (!btn) return;

        const id = btn.dataset.id;
        const action = btn.dataset.action;

        if (action === "delete" && confirm("Bạn có chắc chắn muốn xóa coupon này?")) {
            try {
                const res = await fetch(`/api/admin/coupons/${id}`, { method: "DELETE" });
                const msg = await res.text();

                if (res.ok) {
                    alert("✅ " + (msg || "Xóa thành công!"));
                    loadCoupons();
                } else {
                    alert("❌ " + (msg || "Không thể xóa coupon này!"));
                }
            } catch (err) {
                console.error(err);
                alert("⚠️ Lỗi kết nối đến máy chủ!");
            }
        }
    });

    // ============ SEARCH ============
    searchBtn?.addEventListener("click", () => {
        const keyword = searchInput.value.trim();
        if (keyword)
            loadCoupons(`/api/admin/coupons/search?code=${encodeURIComponent(keyword)}`);
        else loadCoupons();
    });

    // ============ RELOAD ============
    reloadBtn?.addEventListener("click", () => {
        searchInput.value = "";
        loadCoupons();
    });

    // INIT
    loadCoupons();
});
