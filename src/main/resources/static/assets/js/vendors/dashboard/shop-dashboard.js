(function () {
    const BASE = '/UTE_SHOP';
    const token = localStorage.getItem('authToken');

    if (!token) {
        // Chưa đăng nhập -> về login
        window.location.href = BASE + '/login';
        return;
    }

    // ---------- Helpers ----------
    const $ = (id) => document.getElementById(id);
    const setText = (id, v) => {
        const el = $(id);
        if (el) el.textContent = v;
    };
    const fmtVN = (n) => (Number(n || 0)).toLocaleString('vi-VN');
    const clamp = (v, lo, hi) => Math.max(lo, Math.min(hi, v));

    // Build combobox 3 năm gần nhất
    const yearSel = $('yearFilter');
    (function buildYears() {
        if (!yearSel) return;
        const nowY = new Date().getFullYear();
        const years = [nowY, nowY - 1, nowY - 2];
        yearSel.innerHTML = '';
        for (const y of years) {
            const opt = document.createElement('option');
            opt.value = String(y);
            opt.textContent = String(y);
            yearSel.appendChild(opt);
        }
        yearSel.value = String(nowY);
    })();

    // Tính mốc thời gian theo năm (ms)
    function yearRangeMs(year) {
        const from = new Date(year, 0, 1, 0, 0, 0, 0).getTime();
        const to = new Date(year + 1, 0, 1, 0, 0, 0, 0).getTime();
        return {from, to};
    }

    // ---------- Charts ----------
    let lineChart = null;   // #revenueChart (2 series: Gross vs Fees)
    let donutChart = null;  // #totalSale (Gross/Sales/Returns/Net)

    function renderLineChart(categories, grossSeries, feeSeries) {
        const el = document.querySelector('#revenueChart');
        if (!el) return;

        const options = {
            chart: {type: 'line', height: 320, toolbar: {show: false}},
            stroke: {width: 3, curve: 'smooth'},
            dataLabels: {enabled: false},
            xaxis: {categories},
            yaxis: {labels: {formatter: (v) => v.toLocaleString('vi-VN')}},
            tooltip: {y: {formatter: (v) => v.toLocaleString('vi-VN')}},
            legend: {position: 'top'},
            series: [
                {name: 'Gross', data: grossSeries},
                {name: 'Sales + Returned', data: feeSeries}
            ]
        };

        if (!lineChart) {
            lineChart = new ApexCharts(el, options);
            lineChart.render();
        } else {
            lineChart.updateOptions(options);
        }
    }

    function renderDonutChart(gross, salesFee, returns, net) {
        const el = document.querySelector('#totalSale');
        if (!el) return;

        const labels = ['Gross', 'Sales fee', 'Returned', 'Net'];
        const series = [gross, salesFee, returns, net].map((v) => Number(v || 0));

        const options = {
            chart: {type: 'donut', height: 300},
            labels,
            series,
            tooltip: {y: {formatter: (v) => v.toLocaleString('vi-VN')}},
            legend: {position: 'bottom'},
            dataLabels: {enabled: false}
        };

        if (!donutChart) {
            donutChart = new ApexCharts(el, options);
            donutChart.render();
        } else {
            donutChart.updateOptions(options);
        }
    }

    // ---------- API ----------
    let inflight; // AbortController
    async function fetchDashboard(fromMs, toMs) {
        // Hủy request cũ nếu còn chạy
        if (inflight) inflight.abort();
        inflight = new AbortController();

        const url = new URL(BASE + '/api/shop/dashboard', window.location.origin);
        if (fromMs) url.searchParams.set('from', String(fromMs));
        if (toMs) url.searchParams.set('to', String(toMs));

        const res = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token
            },
            signal: inflight.signal
        });

        if (!res.ok) {
            const text = await res.text().catch(() => '');
            throw new Error(`Dashboard API ${res.status}: ${text}`);
        }
        return res.json();
    }

    // ---------- Render pipeline ----------
    async function load() {
        try {
            const year = parseInt(yearSel?.value || String(new Date().getFullYear()), 10);
            const {from, to} = yearRangeMs(year);

            const data = await fetchDashboard(from, to);

            // Cards
            setText('dashMonthlyRevenue', fmtVN(data.monthRevenue));
            setText('dashOrdersMonth', fmtVN(data.ordersThisMonth));
            setText('dashUniqueCustomers', fmtVN(data.uniqueCustomersThisMonth));

            // Summary (Year)
            const gross = Number(data.totalGross || 0);
            const salesFee = Number(data.totalSalesFee || 0);
            const returns = Number(data.totalReturns || 0);
            const net = Number(data.totalNet || 0);

            // ===== Sales Overview breakdown =====
            setText('soGross', fmtVN(gross));
            setText('soSalesFee', fmtVN(salesFee));
            setText('soReturns', fmtVN(returns));
            setText('soIncome', fmtVN(net));

            const base = gross > 0 ? gross : 1; // tránh chia 0
            const pctGross = gross > 0 ? 100 : 0;
            const pctSales = clamp(Math.round((salesFee / base) * 100), 0, 100);
            const pctReturns = clamp(Math.round((returns / base) * 100), 0, 100);
            const pctNet = clamp(Math.round((net / base) * 100), 0, 100);

            setText('soGrossPct', `${pctGross}%`);
            setText('soSalesFeePct', `${pctSales}%`);
            setText('soReturnsPct', `${pctReturns}%`);
            setText('soIncomePct', `${pctNet}%`);

            const setBar = (id, v) => {
                const b = $(id);
                if (b) {
                    b.style.width = `${v}%`;
                    b.setAttribute('aria-valuenow', String(v));
                }
            };
            setBar('soGrossBar', pctGross);
            setBar('soSalesFeeBar', pctSales);
            setBar('soReturnsBar', pctReturns);
            setBar('soIncomeBar', pctNet);


            setText('totalGross', fmtVN(gross));
            setText('totalSalesFee', fmtVN(salesFee));
            setText('totalReturns', fmtVN(returns));
            setText('totalNet', fmtVN(net));

            // Sales Overview: Total Income (Net) + %Net/Gross
            setText('soIncome', fmtVN(net));
            const pct = gross > 0 ? clamp(Math.round((net / gross) * 100), 0, 100) : 0;
            setText('soIncomePct', `${pct}%`);
            const bar = $('soIncomeBar');
            if (bar) {
                bar.style.width = `${pct}%`;
                bar.setAttribute('aria-valuenow', String(pct));
            }

            // Line chart (daily)
            const daily = Array.isArray(data.revenueDaily) ? data.revenueDaily : [];
            const categories = daily.map(d => d.date); // "YYYY-MM-DD"
            const seriesGross = daily.map(d => Number(d.gross || 0));
            const seriesFees = daily.map(d => Number(d.salesFee || 0) + Number(d.returns || 0));
            renderLineChart(categories, seriesGross, seriesFees);

            // Donut chart (year summary)
            renderDonutChart(gross, salesFee, returns, net);

        } catch (err) {
            console.error('[Dashboard] Load error:', err);
            // Optional: bạn có thể hiển thị toast/alert ở đây
        }
    }

    // Events
    yearSel && yearSel.addEventListener('change', load);

    // First load
    load();
})();
