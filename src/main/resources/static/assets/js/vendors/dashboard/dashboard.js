(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = BASE + '/login';
        return;
    }

    // helpers
    const $ = id => document.getElementById(id);
    const fmtMoney = (n) => {
        if (n == null) return '0';
        try {
            return Number(n).toLocaleString('vi-VN', {style: 'currency', currency: 'VND'});
        } catch (_) {
            return n;
        }
    };
    const fmtDateShort = (yyyy_mm_dd) => {
        if (!yyyy_mm_dd) return '';
        const [y, m, d] = String(yyyy_mm_dd).split('-').map(Number);
        const dt = new Date(y, (m || 1) - 1, d || 1);
        return dt.toLocaleDateString('vi-VN', {day: '2-digit', month: '2-digit'});
    };

    // charts
    function renderRevenueChart(points) {
        const el = document.querySelector('#revenueChart');
        if (!el) return;
        const categories = points.map(p => fmtDateShort(p.date));
        const data = points.map(p => p.amount || 0);

        const options = {
            chart: {type: 'line', height: 320, toolbar: {show: false}},
            stroke: {width: 3, curve: 'smooth'},
            dataLabels: {enabled: false},
            xaxis: {categories},
            yaxis: {labels: {formatter: (v) => (v | 0).toLocaleString('vi-VN')}},
            series: [{name: 'Revenue', data}]
        };
        const chart = new ApexCharts(el, options);
        chart.render();
    }

    function renderStatusDonut(statusCounts) {
        const el = document.querySelector('#totalSale');
        if (!el) return;

        // map cố định thứ tự hiển thị (có thể đổi)
        const ORDER = ['NEW', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'RECEIVED', 'CANCELLED', 'RETURNED'];
        const labels = [];
        const series = [];
        ORDER.forEach(k => {
            const v = statusCounts?.[k] ?? 0;
            labels.push(k);
            series.push(v);
        });

        const options = {
            chart: {type: 'donut', height: 260},
            labels,
            series,
            legend: {position: 'bottom'},
            dataLabels: {enabled: true}
        };
        const chart = new ApexCharts(el, options);
        chart.render();
    }

    async function loadDashboard() {
        const res = await fetch(`${BASE}/api/shop/dashboard`, {
            headers: {Authorization: 'Bearer ' + token}
        });
        if (!res.ok) {
            if (res.status === 401) {
                localStorage.removeItem('authToken');
                window.location.href = BASE + '/login';
                return;
            }
            throw new Error('Failed to load dashboard');
        }
        const data = await res.json();

        // metrics
        const revEl = $('dashMonthlyRevenue');
        if (revEl) revEl.textContent = fmtMoney(data.monthRevenue);
        const ordEl = $('dashOrdersMonth');
        if (ordEl) ordEl.textContent = (data.ordersThisMonth ?? 0).toLocaleString('vi-VN');
        const cusEl = $('dashUniqueCustomers');
        if (cusEl) cusEl.textContent = (data.uniqueCustomersThisMonth ?? 0).toLocaleString('vi-VN');

        // charts
        renderRevenueChart(data.revenueDaily || []);
        renderStatusDonut(data.statusCounts || {});
    }

    loadDashboard().catch(err => {
        console.error(err);
        alert('Không tải được dữ liệu dashboard.');
    });
})();
