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

    function fmtMoney(v) {
        if (v == null) return '—';
        try {
            return new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(v);
        } catch {
            return String(v);
        }
    }

    function fmtDate(iso) {
        if (!iso) return '—';
        const d = new Date(iso);
        return d.toLocaleString('vi-VN');
    }

    function getQueryId() {
        const url = new URL(window.location.href);
        return url.searchParams.get('id');
    }

    // DOM
    const el = {
        name: document.getElementById('pdName'),
        title: document.getElementById('pdTitle'),
        cat: document.getElementById('pdCategory'),
        price: document.getElementById('pdPrice'),
        stock: document.getElementById('pdStock'),
        status: document.getElementById('pdStatus'),
        created: document.getElementById('pdCreated'),
        desc: document.getElementById('pdDesc'),
        main: document.getElementById('mainPreview'),
        thumbs: document.getElementById('thumbs'),
        avgScore: document.getElementById('avgScore'),
        totalReviews: document.getElementById('totalReviews'),
    };

    function renderStars(avg) {
        const width = Math.max(0, Math.min(100, (avg / 5) * 100));
        document.querySelector('.stars-fill').style.width = width + '%';
        el.avgScore.textContent = (avg || 0).toFixed(1);
    }

    function showMain(m) {
        el.main.innerHTML = (m.type === 'video')
            ? `<video controls src="${buildUrl(m.url)}"></video>`
            : `<img src="${buildUrl(m.url)}" alt="">`;
    }

    function renderThumbs(list) {
        el.thumbs.innerHTML = '';
        list.forEach((m, idx) => {
            const t = document.createElement('div');
            t.className = 'thumb border';
            t.innerHTML = (m.type === 'video')
                ? `<video muted playsinline src="${buildUrl(m.url)}" style="width:100%;height:100%;object-fit:cover"></video>
           <span class="play-badge">▶</span>`
                : `<img src="${buildUrl(m.url)}" style="width:100%;height:100%;object-fit:cover" alt="">`;
            t.addEventListener('click', () => {
                showMain(m);
                [...el.thumbs.children].forEach(c => c.classList.remove('active'));
                t.classList.add('active');
            });
            if (idx === 0) t.classList.add('active');
            el.thumbs.appendChild(t);
        });
    }

    async function load() {
        const id = getQueryId();
        if (!id) {
            alert('Thiếu id sản phẩm');
            return;
        }
        const res = await fetch(`${BASE}/api/shop/products/${id}/detail`, {
            headers: {Authorization: `Bearer ${token}`}
        });
        if (!res.ok) {
            console.error(await res.text());
            alert('Tải chi tiết sản phẩm thất bại');
            return;
        }
        const d = await res.json();

        // info
        el.name.textContent = d.name || 'Product detail';
        el.title.textContent = d.name || '';
        el.cat.textContent = d.categoryName || '—';
        el.price.textContent = fmtMoney(d.price);
        el.stock.textContent = d.stock ?? '—';
        el.status.textContent = (d.status === 0 ? 'In stock' : d.status === 1 ? 'Out of stock' : d.status === 2 ? 'Hide' : d.status);
        el.created.textContent = fmtDate(d.createdAt);
        el.desc.textContent = d.description || '';

        // rating
        renderStars(d.avgRating || 0);
        el.totalReviews.textContent = `(${d.totalReviews || 0} rates)`;

        // media (mặc định hiển thị item đầu tiên)
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
