import addresses from "/addresses.json" with {type: "json"};
const locationData = addresses;
let selectedDistricts = [];

export function loadProvinces() {
    const provinceSelect = document.getElementById('province');
    locationData.forEach(province => {
        const option = document.createElement('option');
        option.value = province.code;
        option.textContent = province.name;
        provinceSelect.appendChild(option);
    });
}

// Load districts based on province
export function loadDistricts(provinceId) {
    return new Promise((resolve) => {
        const districtSelect = document.getElementById('district');
        const wardSelect = document.getElementById('ward');

        districtSelect.innerHTML = '<option value="" selected disabled>Chọn Quận/Huyện</option>';
        wardSelect.innerHTML = '<option value="" selected disabled>Chọn Phường/Xã</option>';
        wardSelect.disabled = true;

        const districts = locationData.find(prov => prov.code === Number(provinceId))?.districts || [];
        selectedDistricts = districts;
        if (districts.length > 0) {
            districtSelect.disabled = false;
            districts.forEach(district => {
                const option = document.createElement('option');
                option.value = district.code;
                option.textContent = district.name;
                districtSelect.appendChild(option);
            });
        } else {
            districtSelect.disabled = true;
        }
        resolve();
    });
}

// Load wards based on district
export function loadWards(districtId) {
    return new Promise((resolve) => {
        const wardSelect = document.getElementById('ward');
        wardSelect.innerHTML = '<option value="" selected disabled>Chọn Phường/Xã</option>';
        const wards = selectedDistricts.find(prov => prov.code === Number(districtId))?.wards || []
        if (wards.length > 0) {
            wardSelect.disabled = false;
            wards.forEach(ward => {
                const option = document.createElement('option');
                option.value = ward.code;
                option.textContent = ward.name;
                wardSelect.appendChild(option);
            });
        } else {
            wardSelect.disabled = true;
        }
        resolve();
    });
}