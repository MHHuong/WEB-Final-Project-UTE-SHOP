document.addEventListener("DOMContentLoaded", function () {
    const tbody = document.querySelector("#userTable tbody");
    const pagination = document.querySelector("#pagination");
    const searchBtn = document.querySelector("#btnSearch");
    const searchInput = document.querySelector("#searchEmail");
    const roleSelect = document.querySelector("#roleFilter");
    const statusSelect = document.querySelector("#statusFilter");
    const reloadBtn = document.querySelector("#btnReload");

    let currentPage = 0;
    const pageSize = 10;

    // ================= RENDER USER LIST =================
    function renderUsers(users) {
        tbody.innerHTML = "";
        if (users.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted">Không có người dùng nào</td></tr>`;
            return;
        }

        users.forEach(u => {
            tbody.insertAdjacentHTML("beforeend", `
        <tr>
          <td>${u.userId}</td>
          <td>${u.email}</td>
          <td>${u.fullName || ""}</td>
          <td>${u.phone || ""}</td>
          <td>${u.role}</td>
          <td>${u.status === 1
                ? '<span class="badge bg-success">Hoạt động</span>'
                : '<span class="badge bg-danger">Khóa</span>'}</td>
          <td>
              <a href="/admin/customers/customers-edits?id=${u.userId}" 
                 class="btn btn-sm btn-outline-primary">Sửa</a>
              <button class="btn btn-sm btn-outline-danger" data-id="${u.userId}" data-action="delete">Xóa</button>
              <button class="btn ${u.status === 1 ? 'btn-warning' : 'btn-success'}" 
                      data-id="${u.userId}" data-action="toggle">
                ${u.status === 1 ? 'Khóa' : 'Mở'}
              </button>
            </td>
        </tr>
      `);
        });
    }

    // ================= RENDER PAGINATION =================
    function renderPagination(totalPages, currentPage) {
        pagination.innerHTML = "";
        if (totalPages <= 1) return;

        for (let i = 0; i < totalPages; i++) {
            const btn = document.createElement("button");
            btn.textContent = i + 1;
            btn.className = "btn btn-sm " + (i === currentPage ? "btn-primary" : "btn-outline-primary");
            btn.addEventListener("click", () => loadUsers(i));
            pagination.appendChild(btn);
        }
    }

    // ================= LOAD USERS =================
    function loadUsers(page = 0, url = null) {
        currentPage = page;

        let apiUrl = url || `/api/admin/users?page=${page}&size=${pageSize}`;
        fetch(apiUrl)
            .then(res => res.json())
            .then(data => {
                const users = data.content || data; // có thể là Page<User> hoặc List<User>
                renderUsers(users);
                if (data.totalPages) renderPagination(data.totalPages, data.number);
            })
            .catch(err => console.error("Lỗi tải danh sách:", err));
    }

    // ================= INIT LOAD =================
    loadUsers();

    // ================= SEARCH =================
    searchBtn.addEventListener("click", () => {
        const keyword = searchInput.value.trim();
        if (keyword)
            loadUsers(0, `/api/admin/users/search?email=${encodeURIComponent(keyword)}&page=0&size=${pageSize}`);
        else loadUsers();
    });

    // ================= FILTER ROLE =================
    roleSelect?.addEventListener("change", () => {
        const role = roleSelect.value;
        if (role)
            loadUsers(0, `/api/admin/users/role/${role}`);
        else loadUsers();
    });

    // ================= FILTER STATUS =================
    statusSelect?.addEventListener("change", () => {
        const status = statusSelect.value;
        if (!status) return loadUsers();
        fetch(`/api/admin/users?page=0&size=100`) // lấy 1 trang lớn rồi lọc tạm
            .then(res => res.json())
            .then(data => {
                const users = (data.content || data).filter(u => u.status == status);
                renderUsers(users);
                pagination.innerHTML = "";
            });
    });

    // ================= RELOAD =================
    reloadBtn?.addEventListener("click", () => {
        searchInput.value = "";
        roleSelect.value = "";
        statusSelect.value = "";
        loadUsers();
    });

    // ================= ACTION BUTTONS =================
    tbody.addEventListener("click", e => {
        const btn = e.target.closest("button");
        if (!btn) return;

        const id = btn.dataset.id;
        const action = btn.dataset.action;

        // ----- Toggle Status -----
        if (action === "toggle") {
            const newStatus = btn.textContent.trim() === "Khóa" ? 0 : 1;
            fetch(`/api/admin/users/${id}/status?status=${newStatus}`, { method: "PUT" })
                .then(async res => {
                    const msg = await res.text();
                    if (!res.ok) alert("❌ " + (msg || "Cập nhật trạng thái thất bại!"));
                    else {
                        alert("✅ " + ("Cập nhật trạng thái thành công!"));
                        loadUsers(currentPage);
                    }
                })
                .catch(err => {
                    console.error(err);
                    alert("❌ Lỗi kết nối server!");
                });
        }

        // ----- Delete User -----
        if (action === "delete") {
            if (confirm("Bạn có chắc chắn muốn xóa user này?")) {
                fetch(`/api/admin/users/${id}`, { method: "DELETE" })
                    .then(async res => {
                        const msg = await res.text();
                        if (!res.ok) alert("❌ " + (msg || "Xóa thất bại!"));
                        else {
                            alert("✅ " + ("Xóa thành công!"));
                            loadUsers(currentPage);
                        }
                    })
                    .catch(err => {
                        console.error(err);
                        alert("❌ Lỗi kết nối server!");
                    });
            }
        }
    });
});

// ==================== FORM CREATE / EDIT USER ====================
document.addEventListener("DOMContentLoaded", function () {
    const userForm = document.getElementById("userForm");
    if (!userForm) return;

    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");

    const email = document.getElementById("email");
    const fullName = document.getElementById("fullName");
    const phone = document.getElementById("phone");
    const role = document.getElementById("role");
    const status = document.getElementById("status");
    const password = document.getElementById("password");

    // Hiện/ẩn mật khẩu
    const togglePwd = document.getElementById("togglePwd");
    if (togglePwd) {
        togglePwd.addEventListener("click", () => {
            const icon = togglePwd.querySelector("i");
            if (password.type === "password") {
                password.type = "text";
                icon.classList.replace("bi-eye", "bi-eye-slash");
            } else {
                password.type = "password";
                icon.classList.replace("bi-eye-slash", "bi-eye");
            }
        });
    }

    // Nếu là trang EDIT thì load user lên form
    if (id) {
        fetch(`/api/admin/users/${id}`)
            .then(res => {
                if (!res.ok) throw new Error("Không tìm thấy user!");
                return res.json();
            })
            .then(user => {
                email.value = user.email;
                fullName.value = user.fullName || "";
                phone.value = user.phone || "";
                role.value = user.role;
                status.value = String(user.status);
            })
            .catch(err => {
                alert("Lỗi tải dữ liệu user!");
                console.error(err);
                window.location.href = "/admin/customers";
            });
    }

    // Gửi form khi lưu
    userForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const data = {
            email: email.value.trim(),
            fullName: fullName.value.trim(),
            phone: phone.value.trim(),
            role: role.value,
            passwordHash: password.value.trim(),
            status: Number(status?.value || 1)
        };

        // Kiểm tra email trùng
        const checkRes = await fetch(`/api/admin/users`);
        const users = await checkRes.json();
        const existed = (users.content || users).find(u =>
            u.email.toLowerCase() === data.email.toLowerCase() &&
            (!id || u.userId != id)
        );
        if (existed) {
            alert("⚠️ Email này đã tồn tại trong hệ thống!");
            return;
        }

        const method = id ? "PUT" : "POST";
        const url = id ? `/api/admin/users/${id}` : `/api/admin/users`;

        const res = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        });

        const msg = await res.text();
        if (res.ok) {
            alert("✅ " + ("Lưu thành công!"));
            window.location.href = "/admin/customers";
        } else {
            alert("❌ Lỗi: " + (msg || "Không thể lưu dữ liệu!"));
        }
    });
});
