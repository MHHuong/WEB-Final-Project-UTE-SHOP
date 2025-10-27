(function () {
    const typeEl = document.getElementById('discountType');
    const valueEl = document.getElementById('value');

    function sync() {
        if (typeEl.value === 'PERCENT') {
            valueEl.step = '1';
            valueEl.min = '1';
            valueEl.placeholder = 'VD: 10 (10%)';
        } else {
            valueEl.step = '1000';
            valueEl.min = '1';
            valueEl.placeholder = 'VD: 20000 (đ)';
        }
    }

    typeEl?.addEventListener('change', sync);
    sync();
})();