const existingAddress = (document.getElementById('address')?.value || '').trim();
// Format: "địa chỉ cụ thể, Xã/Phường, Quận/Huyện, Tỉnh/Thành phố"
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

    // Chọn province theo tên
    [...$province.options].forEach(opt => {
        if (opt.text === provinceName) opt.selected = true;
    });

    // Kích hoạt load district theo province
    $province.dispatchEvent(new Event('change'));
    // Chờ districts load xong rồi chọn
    const wait = ms => new Promise(r => setTimeout(r, ms));
    await wait(150); // tuỳ UI bạn; hoặc refactor sang promise/await fetch đúng nghĩa

    [...$district.options].forEach(opt => {
        if (opt.text === districtName) opt.selected = true;
    });

    $district.dispatchEvent(new Event('change'));
    await wait(150);

    [...$ward.options].forEach(opt => {
        if (opt.text === wardName) opt.selected = true;
    });
}
prefillFromExisting(existingAddress);