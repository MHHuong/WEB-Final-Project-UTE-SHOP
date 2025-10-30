const RAW_CTX = (typeof window !== 'undefined' && window.APP_CONTEXT) ? window.APP_CONTEXT : '/';

const CTX = (() => {
    let x = (RAW_CTX || '').trim();
    if (x === '/' || x === '') return '';
    x = x.replace(/\/+$/, '');
    if (!x.startsWith('/')) x = '/' + x;
    return x;
})();

const join = (base, path) => ((base || '') + '/' + (path || '')).replace(/\/{2,}/g, '/');

const resolveImg = (u) => {
    if (!u) return join(CTX, '/assets/images/placeholder.png');
    return /^https?:\/\//i.test(u) ? u : join(CTX, u);
};

const NavSearch = (() => {
    const $input = document.getElementById('nav-search-input');
    const $box   = document.getElementById('nav-search-dropdown');
    let items = [];
    let active = -1;

    const api = (q) => join(CTX, '/api/search/suggest') + '?q=' + encodeURIComponent(q);
    const goto = (url) => { window.location.href = join(CTX, url); };

    const highlight = (text, q) => {
        const i = text.toLowerCase().indexOf(q.toLowerCase());
        if (i < 0) return text;
        return text.substring(0, i)
            + '<strong>' + text.substring(i, i + q.length) + '</strong>'
            + text.substring(i + q.length);
    };

    const render = (q) => {
        if (!items.length) { $box.style.display = 'none'; return; }
        $box.innerHTML = items.map((it, idx) => `
            <a class="d-flex align-items-center gap-2 px-3 py-2 text-decoration-none ${idx===active?'bg-light':''}"
               href="${join(CTX, '/products/' + it.productId)}" data-idx="${idx}">
                <img src="${resolveImg(it.imageUrl)}" alt=""
                     class="rounded" style="width:32px;height:32px;object-fit:cover">
                <span class="text-dark">${highlight(it.name, q)}</span>
            </a>
        `).join('');
        $box.style.display = 'block';

        [...$box.querySelectorAll('a')].forEach(a => {
            a.addEventListener('mouseenter', () => { active = +a.dataset.idx; render(q); });
            a.addEventListener('mousedown', (e) => e.preventDefault());
            a.addEventListener('click', () => {
                const idx = +a.dataset.idx;
                goto('/products/' + items[idx].productId);
            });
        });
    };

    const debounce = (fn, ms = 200) => {
        let t;
        return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), ms); };
    };

    const search = debounce(async () => {
        const q = $input.value.trim();
        active = -1;
        if (q.length < 1) { items = []; render(q); return; }

        try {
            const res = await fetch(api(q), { headers: { 'Accept': 'application/json' } });
            if (res.ok) items = await res.json();
            else items = [];
        } catch {
            items = [];
        }
        render(q);
    }, 200);

    const submitForm = (e) => {
        if (e) e.preventDefault();
        const q = $input.value.trim();
        if (!q) return false;
        goto(`/search?q=${encodeURIComponent(q)}`);
        return true;
    };

    const keydown = (e) => {
        if ($box.style.display === 'none') return;
        const max = items.length - 1;

        if (e.key === 'ArrowDown') {
            active = Math.min(max, active + 1);
            render($input.value);
            e.preventDefault();
        }
        else if (e.key === 'ArrowUp') {
            active = Math.max(-1, active - 1);
            render($input.value);
            e.preventDefault();
        }
        else if (e.key === 'Enter') {
            if (active >= 0 && items[active]) {
                goto('/products/' + items[active].productId);
                e.preventDefault();
            } else submitForm(e);
        }
        else if (e.key === 'Escape') {
            items = [];
            render('');
        }
    };

    const blur = () => setTimeout(() => { items = []; render(''); }, 120);

    if ($input) {
        $input.addEventListener('input', search);
        $input.addEventListener('keydown', keydown);
        $input.addEventListener('blur', blur);
        document.addEventListener('click', (ev) => {
            if (!ev.target.closest('#nav-search-dropdown') && ev.target !== $input) {
                items = [];
                render('');
            }
        });
    }

    return { submitForm };
})();