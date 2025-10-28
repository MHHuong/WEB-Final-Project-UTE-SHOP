document.addEventListener('DOMContentLoaded', function () {
    const here = window.location.pathname.replace(/\/+$/, '');

    const normalize = p => (p || '').replace(/\/+$/, '');

    function expandCollapse(pane) {
        if (!pane) return;
        pane.classList.add('show');
        const toggle = document.querySelector('[data-bs-toggle="collapse"][data-bs-target="#' + pane.id + '"]');
        if (toggle) {
            toggle.classList.remove('collapsed');
            toggle.setAttribute('aria-expanded', 'true');
        }
        const parentPane = pane.closest('.collapse');
        if (parentPane && parentPane !== pane) expandCollapse(parentPane);
    }

    function activate(containerSelector) {
        const links = Array.from(document.querySelectorAll(containerSelector + ' a.nav-link[href]'))
            .filter(a => {
                const href = a.getAttribute('href') || '';
                if (!href || href.startsWith('#')) return false;
                const path = new URL(href, window.location.origin).pathname;
                return path !== '/';
            })
            .map(a => {
                const path = normalize(new URL(a.getAttribute('href'), window.location.origin).pathname);
                return {a, path};
            });

        let best = null;
        for (const item of links) {
            const exact = (here === item.path);
            const prefix = (!exact && here.startsWith(item.path + '/'));

            if (!exact && !prefix) continue;

            const score = (exact ? 2 : 1);
            const len = item.path.length;
            const rank = score * 100000 + len;

            if (!best || rank > best.rank) {
                best = {...item, exact, rank};
            }
        }

        if (best) {
            best.a.classList.add('active');
            const pane = best.a.closest('.collapse');
            if (pane) expandCollapse(pane);
        }
    }

    activate('#sideNavbar');
    activate('.navbar-offcanvac');
});
