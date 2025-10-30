window.addEventListener('DOMContentLoaded', () => {
    (async function initVNAddress() {
        try {
            const $province = document.getElementById('provinceSelect');
            const $district = document.getElementById('districtSelect');
            const $ward = document.getElementById('wardSelect');
            const $detail = document.getElementById('detailAddress');
            const $address = document.getElementById('address');

            if (!$province || !$district || !$ward) {
                console.error('[Address] Missing element(s)', {$province, $district, $ward});
                return;
            }

            const resProv = await fetch('/UTE_SHOP/api/locations/provinces');
            if (!resProv.ok) throw new Error('Load provinces failed: ' + resProv.status);
            const provinces = await resProv.json();

            $province.innerHTML = '<option value="">-- Choose Province --</option>';
            provinces.forEach(p => {
                const opt = document.createElement('option');
                opt.value = p.code;
                opt.textContent = p.name;
                $province.appendChild(opt);
            });
            document.dispatchEvent(new CustomEvent('vn:provincesLoaded'));

            $province.addEventListener('change', async () => {
                try {
                    const code = $province.value;
                    $district.innerHTML = '<option value="">-- Choose District --</option>';
                    $ward.innerHTML = '<option value="">-- Choose Ward --</option>';
                    $district.disabled = true;
                    $ward.disabled = true;
                    if (!code) return;

                    const resDist = await fetch('/UTE_SHOP/api/locations/districts?provinceCode=' + code);
                    if (!resDist.ok) throw new Error('Load districts failed: ' + resDist.status);
                    const districts = await resDist.json();

                    districts.forEach(d => {
                        const opt = document.createElement('option');
                        opt.value = d.code;
                        opt.textContent = d.name;
                        $district.appendChild(opt);
                    });
                    $district.disabled = false;
                    document.dispatchEvent(new CustomEvent('vn:districtsLoaded', {detail: {provinceCode: code}}));
                } catch (e) {
                    console.error(e);
                }
            });

            $district.addEventListener('change', async () => {
                try {
                    const code = $district.value;
                    $ward.innerHTML = '<option value="">-- Choose Ward --</option>';
                    $ward.disabled = true;
                    if (!code) return;

                    const resWard = await fetch('/UTE_SHOP/api/locations/wards?districtCode=' + code);
                    if (!resWard.ok) throw new Error('Load wards failed: ' + resWard.status);
                    const wards = await resWard.json();

                    wards.forEach(w => {
                        const opt = document.createElement('option');
                        opt.value = w.code;
                        opt.textContent = w.name;
                        $ward.appendChild(opt);
                    });
                    $ward.disabled = false;
                    document.dispatchEvent(new CustomEvent('vn:wardsLoaded', {detail: {districtCode: code}}));
                } catch (e) {
                    console.error(e);
                }
            });

            const form = document.querySelector('form');
            if (form) {
                form.addEventListener('submit', (e) => {
                    const detail = ($detail?.value || '').trim();
                    const wardName = $ward.options[$ward.selectedIndex]?.text || '';
                    const districtName = $district.options[$district.selectedIndex]?.text || '';
                    const provinceName = $province.options[$province.selectedIndex]?.text || '';
                    if (!detail || !$ward.value || !$district.value || !$province.value) {
                        e.preventDefault();
                        alert('Please choose particular province, district, ward and detail.');
                        return;
                    }
                    $address.value = `${detail}, ${wardName}, ${districtName}, ${provinceName}`;
                });
            }
        } catch (err) {
            console.error('[Address init] ', err);
        }
    })();
});