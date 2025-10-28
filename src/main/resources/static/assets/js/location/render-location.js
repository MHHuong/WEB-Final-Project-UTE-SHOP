const existingAddress = (document.getElementById('address')?.value || '').trim();

function once(eventName) {
    return new Promise(resolve => {
        const h = (e) => {
            document.removeEventListener(eventName, h);
            resolve(e);
        };
        document.addEventListener(eventName, h);
    });
}

async function prefillFromExisting(full) {
    if (!full) return;
    const parts = full.split(',').map(s => s.trim());
    if (parts.length < 4) return;
    const [detail, wardName, districtName, provinceName] = parts;

    const $province = document.getElementById('provinceSelect');
    const $district = document.getElementById('districtSelect');
    const $ward = document.getElementById('wardSelect');
    const $detail = document.getElementById('detailAddress');

    $detail.value = detail;

    [...$province.options].forEach(opt => {
        if (opt.text === provinceName) opt.selected = true;
    });

    if (!$province || $province.options.length <= 1) {
        await once('vn:provincesLoaded');
    }
    [...$province.options].forEach(opt => {
        if (opt.text === provinceName) opt.selected = true;
    });
    $province.dispatchEvent(new Event('change'));
    const wait = ms => new Promise(r => setTimeout(r, ms));
    await once('vn:districtsLoaded');

    [...$district.options].forEach(opt => {
        if (opt.text === districtName) opt.selected = true;
    });

    $district.dispatchEvent(new Event('change'));
    await once('vn:wardsLoaded');

    [...$ward.options].forEach(opt => {
        if (opt.text === wardName) opt.selected = true;
    });
}

prefillFromExisting(existingAddress);