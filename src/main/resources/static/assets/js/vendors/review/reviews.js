(function () {
    const BASE = '/UTE_SHOP'; // đổi nếu context khác
    const token = localStorage.getItem('authToken');

    function mustAuth() {
        if (!token) {
            alert('Vui lòng đăng nhập');
            window.location.href = BASE + '/login';
            return false;
        }
        return true;
    }

    const tbody = document.getElementById('reviewTableBody');

    function fmtDate(iso) {
        if (!iso) return '';
        const d = new Date(iso);
        // hiển thị dạng: 23 Nov,2022
        return d.toLocaleDateString('en-GB', {
            day: '2-digit', month: 'short', year: 'numeric'
        }).replace(' ', ' ').replace(',', ',');
    }

    function renderStars(rating) {
        const n = Math.max(0, Math.min(5, +rating || 0));
        let html = '<div>';
        for (let i = 0; i < 5; i++) {
            const cls = i < n ? 'text-warning' : 'text-light';
            html += `<span><i class="bi bi-star-fill ${cls}"></i></span>`;
        }
        html += '</div>';
        return html;
    }

    function escapeHtml(s) {
        return (s ?? '').toString()
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
    }

    async function loadReviews(page = 0, size = 10) {
        if (!mustAuth()) return;

        const url = `${BASE}/api/shop/reviews?page=${page}&size=${size}&sort=createdAt,desc`;
        const res = await fetch(url, {
            headers: {
                'Accept': 'application/json',
                'Authorization': 'Bearer ' + token
            }
        });
        if (!res.ok) {
            tbody.innerHTML = `<tr><td colspan="6">Không tải được dữ liệu (HTTP ${res.status})</td></tr>`;
            return;
        }
        const data = await res.json(); // PageResult<ReviewItemRes>

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Chưa có đánh giá nào</td></tr>`;
            return;
        }

        const rows = data.content.map(item => {
            const productName = escapeHtml(item.productName ?? '(N/A)');
            const userName = escapeHtml(item.userName ?? '(Guest)');
            const comment = escapeHtml(item.comment ?? '');
            const created = fmtDate(item.createdAt);

            // link sản phẩm (nếu bạn có route chi tiết)
            const productLink = `<a class="text-reset" href="${BASE}/shop/product/product-detail?id=${item.productId}">${productName}</a>`;

            return `
                <tr>
                    <td>${productLink}</td>
                    <td>${userName}</td>
                    <td><span class="text-truncate" title="${comment}">${comment.length > 64 ? comment.slice(0, 61) + '...' : comment}</span></td>
                    <td>${renderStars(item.rating)}</td>
                    <td>${created}</td>
                    <td class="text-end">
                        <!-- chừa chỗ hành động (xem chi tiết, ẩn review, v.v.) -->
                        <div class="btn-group">
                            <a class="btn btn-sm btn-outline-secondary"
                               href="${BASE}/shop/review/review-detail?id=${item.reviewId}">
                               View
                            </a>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        tbody.innerHTML = rows;
        // TODO: nếu cần phân trang ở UI, thêm controls và gọi loadReviews(page ± 1)
    }

    // khởi động
    document.addEventListener('DOMContentLoaded', () => {
        loadReviews(0, 10);
    });
})();