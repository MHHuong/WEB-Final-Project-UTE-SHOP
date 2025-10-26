(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    // helpers
    function buildUrl(p) {
        if (!p) return '';
        if (/^https?:\/\//i.test(p)) return p;
        if (p.startsWith(BASE + '/')) return p;
        if (p.startsWith('/')) return BASE + p;
        return BASE + '/' + p.replace(/^\/+/, '');
    }

    function getId() {
        const u = new URL(location.href);
        return u.searchParams.get('id');
    }

    function fmtDate(iso) {
        return iso ? new Date(iso).toLocaleString('vi-VN') : '—';
    }

    // dom
    const el = {
        main: document.getElementById('mainPreview'),
        thumbs: document.getElementById('thumbs'),
        prodName: document.getElementById('prodName'),
        prodLink: document.getElementById('prodLink'),
        categoryName: document.getElementById('categoryName'),
        avgScore: document.getElementById('avgScore'),
        createdAt: document.getElementById('createdAt'),
        reviewerName: document.getElementById('reviewerName'),
        reviewerId: document.getElementById('reviewerId'),
        comment: document.getElementById('comment'),
    };

    function renderStars(v) {
        const w = Math.max(0, Math.min(100, (v / 5) * 100));
        document.querySelector('.stars-fill').style.width = w + '%';
        el.avgScore.textContent = (v || 0).toFixed(1);
    }

    function showMain(m) {
        el.main.innerHTML = (m.type === 'video')
            ? `<video controls src="${buildUrl(m.url)}"></video>`
            : `<img src="${buildUrl(m.url)}" alt="">`;
    }

    function renderThumbs(list) {
        el.thumbs.innerHTML = '';
        list.forEach((m, i) => {
            const t = document.createElement('div');
            t.className = 'thumb border';
            t.innerHTML = (m.type === 'video')
                ? `<video muted playsinline src="${buildUrl(m.url)}" style="width:100%;height:100%;object-fit:cover"></video><span class="play-badge">▶</span>`
                : `<img src="${buildUrl(m.url)}" style="width:100%;height:100%;object-fit:cover" alt="">`;
            t.addEventListener('click', () => {
                showMain(m);
                [...el.thumbs.children].forEach(c => c.classList.remove('active'));
                t.classList.add('active');
            });
            if (i === 0) t.classList.add('active');
            el.thumbs.appendChild(t);
        });
    }

    async function load() {
        const id = getId();
        if (!id) {
            alert('Thiếu id review');
            return;
        }

        const res = await fetch(`${BASE}/api/shop/reviews/${id}/detail`, {
            headers: {Authorization: `Bearer ${token}`}
        });
        if (!res.ok) {
            console.error(await res.text());
            alert('Tải chi tiết review thất bại');
            return;
        }

        const d = await res.json();

        el.prodName.textContent = d.productName || '—';
        el.prodLink.href = `${BASE}/shop/product/product-detail?id=${d.productId}`;
        el.categoryName.textContent = d.categoryName || '—';

        renderStars(d.rating || 0);
        el.createdAt.textContent = fmtDate(d.createdAt);

        el.reviewerName.textContent = d.userName || `User #${d.userId ?? ''}`;
        el.reviewerId.textContent = d.userId ? `(ID: ${d.userId})` : '';
        el.comment.textContent = d.comment || '';

        const media = Array.isArray(d.media) ? d.media : [];
        if (media.length) {
            showMain(media[0]);
            renderThumbs(media);
        } else {
            el.main.innerHTML = `<div class="text-muted">No media</div>`;
            el.thumbs.innerHTML = '';
        }
    }

    document.addEventListener('DOMContentLoaded', load);
})();
