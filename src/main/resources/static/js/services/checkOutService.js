import cartService from "/js/services/cartService.js";
import { showSuccessToast, showErrorToast, showWarningToast, showInfoToast } from "/js/utils/toastUtils.js";
import addresses from "/addresses.json" with { type: "json" };
import addressService from "/js/services/addressService.js";
import orderService from "/js/services/orderService.js";
import couponService from "/js/services/couponService.js";

const USER_ID = 1;
let selectedProducts = [];
let savedAddresses = [];
let selectedAddressId = null;
let shippingFee = 0
let shippingFeePre = 0
let shippingMethod = 'STANDARD';
let shippingProviderId = 1;

// Mock data for provinces and districts (Replace with actual API)
const locationData = addresses;
let selectedDistricts = [];

// Format currency
function formatCurrency(amount) {
    if (isNaN(amount)) return "0₫";
    return amount.toLocaleString('vi-VN') + '₫';
}

// Load saved addresses
async function loadSavedAddresses() {
    try {
        const result = await addressService.getAddressesByUserId(USER_ID);

        if (result.status === 'true' && result.data && result.data.length > 0) {
            savedAddresses = result.data;
            renderSavedAddresses();

            // Auto select default address
            const defaultAddress = savedAddresses.find(addr => addr.isDefault === 1);
            if (defaultAddress) {
                await selectAddress(defaultAddress.addressId);
            }
        } else {
            document.getElementById('addresses-loading').style.display = 'none';
            document.getElementById('no-addresses-message').style.display = 'block';
            document.getElementById('address-form-section').style.display = 'block';
        }
    } catch (error) {
        console.error('Error loading addresses:', error);
        document.getElementById('addresses-loading').style.display = 'none';
        document.getElementById('no-addresses-message').style.display = 'block';
        document.getElementById('address-form-section').style.display = 'block';
    }
}

// Render saved addresses
function renderSavedAddresses() {
    const container = document.getElementById('saved-addresses-list');
    document.getElementById('addresses-loading').style.display = 'none';
    container.style.display = 'block';

    let html = '';
    savedAddresses.forEach(address => {
        const isDefault = address.isDefault === 1;
        const fullAddress = `${address.addressDetail}, ${address.ward}, ${address.district}, ${address.province}`;

        html += `
                    <div class="address-card ${selectedAddressId === address.addressId ? 'selected' : ''}"
                         data-address-id="${address.addressId}"
                         onclick="selectAddressById(${address.addressId})">
                        <div class="check-icon"></div>
                        <div class="address-content">
                                ${isDefault ? '<span class="badge bg-primary default-badge ">Mặc định</span>' : ''}
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h6 class="mb-0">${address.receiverName}</h6>
                                <span class="text-muted small mt-5">${address.phone}</span>
                            </div>
                            <p class="text-muted small mb-0">${fullAddress}</p>
                        </div>
                    </div>
                `;
    });

    container.innerHTML = html;

    document.getElementById('address-form-section').style.display = 'none';
}

window.selectAddressById = function(addressId) {
    selectAddress(addressId);
};

// Select address
async function selectAddress(addressId) {
    selectedAddressId = addressId;
    // Update UI
    document.querySelectorAll('.address-card').forEach(card => {
        card.classList.remove('selected');
    });

    const selectedCard = document.querySelector(`[data-address-id="${addressId}"]`);
    if (selectedCard) {
        selectedCard.classList.add('selected');
    }

    // Fill form with selected address
    const address = savedAddresses.find(addr => addr.addressId === addressId);
    if (address) {
        console.log(address)
        await fillFormWithAddress(address);
    }
    // Enable place order button
    document.getElementById('place-order-btn').disabled = false
    await displayShippingFeeFist();
}

// Fill form with address data
async function fillFormWithAddress(address) {
    document.getElementById('receiver-name').value = address.receiverName || '';
    document.getElementById('receiver-phone').value = address.phone || '';
    document.getElementById('address-detail').value = address.addressDetail || '';

    // Set province, district, ward
    const provinceSelect = document.getElementById('province');
    const province = Array.from(provinceSelect.options).find(opt => opt.text === address.province);
    if (province) {
        provinceSelect.value = province.value;
        loadDistricts(province.value).then(() => {
            const districtSelect = document.getElementById('district');
            const district = Array.from(districtSelect.options).find(opt => opt.text === address.district);
            if (district) {
                districtSelect.value = district.value;
                console.log(districtSelect.value);
                loadWards(district.value).then(() => {
                    const wardSelect = document.getElementById('ward');
                    const ward = Array.from(wardSelect.options).find(opt => opt.text === address.ward);
                    if (ward) {
                        wardSelect.value = ward.value;
                    }
                });
            }
        });
    }
}

// Show address form for adding new address
function showAddressForm() {
    document.getElementById('address-form-section').style.display = 'block';
    document.getElementById('cancel-new-address-btn').style.display = 'inline-block';
    document.getElementById('form-title').textContent = 'Thêm địa chỉ mới';

    const province = document.getElementById("province");
    const district = document.getElementById("district");
    const ward = document.getElementById("ward");

    province.value = '';

    district.innerHTML = '<option value="" selected disabled>Chọn Quận/Huyện</option>';
    district.disabled = true;
    ward.innerHTML = '<option value="" selected disabled>Chọn Phường/Xã</option>';
    ward.disabled = true;
    // Clear form
    document.getElementById('shipping-form').reset();
    selectedAddressId = null;

    // Deselect all address cards
    document.querySelectorAll('.address-card').forEach(card => {
        card.classList.remove('selected');
    });

    // Scroll to form
    document.getElementById('address-form-section').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// Cancel adding new address
function cancelAddressForm() {
    if (savedAddresses.length > 0) {
        document.getElementById('address-form-section').style.display = 'none';
        document.getElementById('cancel-new-address-btn').style.display = 'none';
        document.getElementById('form-title').textContent = 'Địa chỉ giao hàng';

        // Re-select default address if no address was selected
        if (!selectedAddressId) {
            const defaultAddress = savedAddresses.find(addr => addr.isDefault === 1);
            if (defaultAddress) {
                selectAddress(defaultAddress.addressId);
            } else if (savedAddresses.length > 0) {
                selectAddress(savedAddresses[0].addressId);
            }
        }
    }
}

// Load provinces
function loadProvinces() {
    const provinceSelect = document.getElementById('province');
    locationData.forEach(province => {
        const option = document.createElement('option');
        option.value = province.code;
        option.textContent = province.name;
        provinceSelect.appendChild(option);
    });
}

// Load districts based on province
function loadDistricts(provinceId) {
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
function loadWards(districtId) {
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

// Load selected products from sessionStorage
async function loadSelectedProducts() {
    const result = await cartService.getSelectedCartItem()
    if (result.status === 'true') {
        selectedProducts = result.data;
        if (!selectedProducts) {
            showWarningToast('Vui lòng chọn sản phẩm từ giỏ hàng!');
            setTimeout(() => {
                window.location.href = '/shop-cart';
            }, 2000);
            return;
        }

        try {
            renderProducts();
            updateOrderSummary();
        } catch (error) {
            console.error('Error loading selected products:', error);
            showErrorToast('Có lỗi xảy ra!');
        }
    }
    else {
        showErrorToast('Có lỗi xảy ra!');
    }
}

// Render products
function renderProducts() {
    const container = document.getElementById('order-products');

    if (selectedProducts.length === 0) {
        container.innerHTML = `
                    <div class="text-center py-4">
                        <p class="text-muted">Không có sản phẩm nào được chọn</p>
                    </div>
                `;
        return;
    }

    let html = '';
    selectedProducts.forEach(item => {
        html += `
                    <div class="product-item-checkout">
                        <div class="d-flex align-items-start">
                            <img src="${item.productModel.image}" class="product-img-checkout me-3"
                                 alt="${item.productModel.productName}">
                            <div class="flex-grow-1">
                                <h6 class="mb-1">${item.productModel.productName}</h6>
                                <small class="text-muted">SL: ${item.quantity}</small>
                                <div class="d-flex justify-content-between mt-2">
                                    <span class="text-muted">${formatCurrency(item.productModel.price)}</span>
                                    <span class="fw-bold text-primary">${formatCurrency(item.productModel.price * item.quantity)}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                `;
    });

    container.innerHTML = html;
}

// Update order summary
function updateOrderSummary() {
    const itemCount = selectedProducts.reduce((sum, item) => sum + item.quantity, 0);
    const subtotal = selectedProducts.reduce((sum, item) =>
        sum + (item.productModel.price * item.quantity), 0);
    const discount = selectedProducts[0]?.discount || 0;

    document.getElementById('item-count').textContent = itemCount;
    document.getElementById('subtotal').textContent = formatCurrency(subtotal);
    document.getElementById('total').textContent = formatCurrency(subtotal);
    document.getElementById('discount').textContent = `-${formatCurrency(discount)}`;
}

// Validate form
function validateForm() {
    const form = document.getElementById('shipping-form');
    if (!form.checkValidity()) {
        form.classList.add('was-validated');
        return false;
    }
    return true;
}

// Place order
async function placeOrder() {
    if (!validateForm()) {
        showErrorToast('Vui lòng điền đầy đủ thông tin!');
        return;
    }

    // Check if this is a new address (not from saved addresses)
    const isNewAddress = !selectedAddressId;

    if (isNewAddress) {
        // Show confirmation modal for new address
        showSaveAddressModal();
    } else {
        // Process order directly if using saved address
        processOrder(false);
    }
}

// Show save address confirmation modal
function showSaveAddressModal() {
    // Populate preview data
    const receiverName = document.getElementById('receiver-name').value;
    const receiverPhone = document.getElementById('receiver-phone').value;
    const province = document.getElementById('province').selectedOptions[0].text;
    const district = document.getElementById('district').selectedOptions[0].text;
    const ward = document.getElementById('ward').selectedOptions[0].text;
    const addressDetail = document.getElementById('address-detail').value;

    document.getElementById('preview-receiver-name').textContent = receiverName;
    document.getElementById('preview-phone').textContent = receiverPhone;
    document.getElementById('preview-address').textContent = `${addressDetail}, ${ward}, ${district}, ${province}`;

    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('saveAddressModal'));
    modal.show();
}

// Process order
async function processOrder(shouldSaveAddress = false) {
    const paymentMethod = document.querySelector('input[name="payment-method"]:checked').value;
    const ewalletType = paymentMethod === 'E_WALLET' ? document.querySelector('input[name="ewallet-type"]:checked')?.value : null;

    const orderData = {
        userId: USER_ID,
        paymentMethod: ewalletType || paymentMethod,
        shippingProviderId: 1,
        total: parseInt(document.getElementById('total').innerText.replace(/₫/g, '').replace(/\./g, '') || 0),
        payments: null,
        coupon: document.getElementById('voucher-code-input').value || null,
        address: {
            receiverName: document.getElementById('receiver-name').value,
            phone: document.getElementById('receiver-phone').value,
            province: document.getElementById('province').selectedOptions[0].text,
            district: document.getElementById('district').selectedOptions[0].text,
            ward: document.getElementById('ward').selectedOptions[0].text,
            addressDetail: document.getElementById('address-detail').value,
        },
        note: document.getElementById('order-note').value,
        orders: selectedProducts.map(item => ({
            productId: item.productModel.productId,
            shopId: item.productModel.shopId,
            quantity: item.quantity,
            price: item.productModel.price,
            discountAmount: item.discountValue || 0
        })),
    };

    // Save address if requested
    if (shouldSaveAddress) {
        const isDefault = document.getElementById('set-as-default-address').checked;
        const addressData = {
            userId: USER_ID,
            receiverName: orderData.address.receiverName,
            phone: orderData.address.phone,
            province: orderData.address.province,
            district: orderData.address.district,
            ward: orderData.address.ward,
            addressDetail: orderData.address.addressDetail,
            isDefault: isDefault ? 1 : 0
        };

        try {
            const result = await addressService.createAddress(addressData);
            if (result.status === 'true') {
                showSuccessToast('Đã lưu địa chỉ thành công!');
            }
        } catch (error) {
            console.error('Error saving address:', error);
        }
    }

    console.log('Order data:', orderData);
    await handleNavigation();
    const result = await orderService.saveOrder(orderData);
    if (result.status === 'Success') {
        showSuccessToast('Đặt hàng thành công!');
        await handleNavigation()
    } else {
        showErrorToast('Đặt hàng thất bại: ' + result.message);
    }
}

async function handleNavigation() {
    const subtotalText = document.getElementById('subtotal').innerText.replace(/₫/g, '').replace(/\./g, '');
    const subtotal = parseInt(subtotalText) || 0;

    const discountText = document.getElementById('discount').innerText.replace(/₫/g, '').replace(/\./g, '').replace(/-/g, '');
    const discount = parseInt(discountText) || 0;

    const paymentMethod = document.querySelector('input[name="payment-method"]:checked').value;
    const ewalletType = paymentMethod === 'E_WALLET' ? document.querySelector('input[name="ewallet-type"]:checked')?.value : null;

    const displayOrderData = {
        userId: USER_ID,
        shippingProviderId: 1,
        coupon: document.getElementById('voucher-code-input').value || null,
        shippingMethod: document.querySelector('input[name="shipping-method"]:checked').value,
        payment: {
            total: parseInt(document.getElementById('total').innerText.replace(/₫/g, '').replace(/\./g, '') || 0),
            subtotal: subtotal,
            shippingFee: shippingFee,
            discount: discount,
            paymentMethod: ewalletType || paymentMethod,
        },
        address: {
            receiverName: document.getElementById('receiver-name').value,
            phone: document.getElementById('receiver-phone').value,
            province: document.getElementById('province').selectedOptions[0].text,
            district: document.getElementById('district').selectedOptions[0].text,
            ward: document.getElementById('ward').selectedOptions[0].text,
            addressDetail: document.getElementById('address-detail').value,
        },
        orders: selectedProducts.map(item => ({
            productId: item.productModel.productId,
            quantity: item.quantity,
            image: item.productModel.image,
            productName: item.productModel.productName,
            price: item.productModel.price,
            shopId: item.productModel.shopId,
            discountAmount: item.discountValue || 0
        })),
        orderTime: new Date().toLocaleString('vi-VN'),
        orderCode: 'ORD' + Date.now()
    };
    await orderService.setDisplayTempOrder(displayOrderData);
    setTimeout(() => {
        if (paymentMethod === 'E_WALLET' && ewalletType) {
            window.location.href = `/status/${displayOrderData.orderCode}?status=pending`;
        }
        else window.location.href = `/status/${displayOrderData.orderCode}?status=success`;
    }, 2000);
}

async function calcShippingFee() {
    let shippingMethod = document.querySelector('input[name="shipping-method"]:checked').value;
    let shippingData = {
        province: document.getElementById('province').selectedOptions[0].text,
        district: document.getElementById('district').selectedOptions[0].text,
        ward: document.getElementById('ward').selectedOptions[0].text,
        addressDetail: document.getElementById('address-detail').value,
        shopIds: selectedProducts.map(item => item.productModel.shopId),
        shippingService: shippingMethod
    }
    const result = await orderService.calculateShippingFee(shippingData);
    console.log(result)
    if (result.status === 'Success') {
        shippingFee = Math.floor(result.data.fee) || 0;
        shippingProviderId = result.data.shippingProviderId || 0
    }
    else showErrorToast(result.message || 0);
}

async function displayShippingFeeFist() {
    // await calcShippingFee()
    document.getElementById("shipping-fee").innerText = formatCurrency(shippingFee);
    const subtotalText = document.getElementById("subtotal").innerText.replace(/₫/g, '').replace(/\./g, '');
    const subtotal = parseInt(subtotalText) || 0;
    const total = subtotal + shippingFee;
    document.getElementById("total").innerText = formatCurrency(total);
}

async function addShippingServiceFee() {
    let shippingMethod = document.querySelector('input[name="shipping-method"]:checked').value;
    // await calcShippingFee()
    switch (shippingMethod) {
        case 'STANDARD':
            shippingFee += 0;
            break;
        case 'FAST':
            shippingFee += 30000;
            break;
        case 'EXPRESS':
            shippingFee += 50000;
            break;
    }
    document.getElementById("shipping-fee").innerText = formatCurrency(shippingFee);
    const subtotalText = document.getElementById("subtotal").innerText.replace(/₫/g, '').replace(/\./g, '');
    const subtotal = parseInt(subtotalText) || 0;

    const discountText = document.getElementById('discount').innerText.replace(/₫/g, '').replace(/\./g, '').replace(/-/g, '');
    const discount = parseInt(discountText) || 0;

    const total = subtotal + shippingFee - discountText;
    document.getElementById("total").innerText = formatCurrency(total);
}

// Render vouchers in modal
async function renderVouchers() {
    let vouchers = [];
    try {
        const result = await couponService.getAllGlobalCoupons();
        if (result.status === 'Success' && result.data) {
            vouchers = result.data;
        }
        else showErrorToast(result.message);
    } catch (error) {
        console.error('Error fetching vouchers:', error);
        showErrorToast('Lỗi khi tải mã giảm giá');
        return;
    }
    const container = document.getElementById('voucher-list');
    document.getElementById('voucher-loading').style.display = 'none';

    if (!vouchers || vouchers.length === 0) {
        document.getElementById('no-vouchers-message').style.display = 'block';
        return;
    }

    container.innerHTML = '';

    const total = parseInt(document.getElementById("total").innerText.replace(/₫/g, '').replace(/\./g, '') || 0);
    vouchers.forEach(voucher => {
        const isDisabled = Number(total) < Number(voucher.minOrderAmount);
        const discountValue = voucher.discountType === 'PERCENT'
            ? `${voucher.value}%`
            : `${formatCurrency(voucher.value)}`;
        const discountLabel = 'GIẢM';
        const description = `Giảm ${discountValue} cho các hóa đơn từ ${formatCurrency(voucher.minOrderAmount)} trở lên`

        const voucherCard = document.createElement('div');
        voucherCard.className = `voucher-card border border-secondary-subtle rounded p-3 mb-3 bg-white position-relative cursor-pointer d-flex gap-3 ${isDisabled ? 'disabled' : ''}`;
        voucherCard.innerHTML = `
            <div class="voucher-left">
                <div class="discount-value">${discountValue}</div>
                <div class="discount-label">${discountLabel}</div>
            </div>
            <div class="voucher-content d-flex flex-column justify-content-center flex-fill">
                <div class="voucher-code">${voucher.code}</div>
                <div class="voucher-description">${description}</div>
                <div class="voucher-expiry">HSD: ${new Date(voucher.expiredAt).toLocaleDateString('vi-VN')}</div>
            </div>
            <div class="voucher-action d-flex align-items-center justify-content-center flex-shrink-0">
                <button class="btn-apply btn btn-outline-success btn-sm fw-medium px-3 py-1 rounded-1" ${isDisabled ? 'disabled' : ''}>
                    ${isDisabled ? 'Không thể dùng' : 'Áp dụng'}
                </button>
            </div>
        `;

        if (!isDisabled) {
            const discount = selectedProducts[0]?.discount || 0
            voucherCard.onclick = () => {
                const discountAmount = voucher.discountType === 'PERCENT'
                    ? discount + calculatePercentDiscount(voucher.value, 0)
                    : discount + voucher.value;
                applyVoucher(voucher.code, discountAmount, description);
                bootstrap.Modal.getInstance(document.getElementById('voucherModal')).hide();
            };
        }

        container.appendChild(voucherCard);
    });

    container.style.display = 'block';
}

// Calculate percent discount
function calculatePercentDiscount(percent, maxDiscount) {
    const subtotalText = document.getElementById('subtotal').innerText.replace(/₫/g, '').replace(/\./g, '');
    const subtotal = parseInt(subtotalText) || 0;
    let discount = Math.floor(subtotal * percent / 100);

    if (maxDiscount && discount > maxDiscount) {
        discount = maxDiscount;
    }

    return discount;
}

// Apply voucher
function applyVoucher(code, discountAmount, description) {
    // Update display
    document.getElementById('voucher-code-input').value = code;
    document.getElementById('voucher-code-display').textContent = code;
    document.getElementById('voucher-desc-display').textContent = description;

    // Show selected voucher, hide input section
    document.getElementById('selected-voucher-display').style.display = 'flex';
    document.getElementById('voucher-selection-section').style.display = 'none';

    // Update discount in order summary
    document.getElementById('discount').textContent = `-${formatCurrency(discountAmount)}`;

    // Recalculate total
    updateTotal();

    showSuccessToast(`Đã áp dụng mã ${code}`);
}

// Remove voucher
function removeVoucher() {
    // Clear display
    document.getElementById('voucher-code-input').value = '';
    document.getElementById('voucher-code-display').textContent = '';
    document.getElementById('voucher-desc-display').textContent = '';

    // Hide selected voucher, show input section
    document.getElementById('selected-voucher-display').classList.remove('d-flex');
    document.getElementById('selected-voucher-display').style.display = 'none';
    document.getElementById('voucher-selection-section').style.display = 'block';

    // Reset discount
    const discount = selectedProducts[0]?.discount
    document.getElementById('discount').textContent = `-${formatCurrency(discount)}`;

    // Recalculate total
    updateTotal();

    showSuccessToast('Đã xóa mã giảm giá');
}

// Update total amount
function updateTotal() {
    const subtotalText = document.getElementById('subtotal').innerText.replace(/₫/g, '').replace(/\./g, '');
    const subtotal = parseInt(subtotalText) || 0;

    const shippingFeeText = document.getElementById('shipping-fee').innerText.replace(/₫/g, '').replace(/\./g, '');
    const shippingFee = parseInt(shippingFeeText) || 0;

    const discountText = document.getElementById('discount').innerText.replace(/₫/g, '').replace(/\./g, '').replace(/-/g, '');
    const discount = parseInt(discountText) || 0;

    const total = subtotal + shippingFee - discount;
    document.getElementById('total').textContent = formatCurrency(total);
}

document.querySelectorAll('input[name="shipping-method"]').forEach(radio => {
    radio.addEventListener("change", () => {
        addShippingServiceFee();
    });
});

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    loadProvinces();
    loadSelectedProducts();
    loadSavedAddresses();

    // Add new address button
    document.getElementById('add-new-address-btn').addEventListener('click', showAddressForm);

    // Cancel new address button
    document.getElementById('cancel-new-address-btn').addEventListener('click', cancelAddressForm);

    // Province change event
    document.getElementById('province').addEventListener('change', (e) => {
        loadDistricts(e.target.value);
    });

    // District change event
    document.getElementById('district').addEventListener('change', (e) => {
        loadWards(e.target.value);
    });

    document.getElementById("ward").addEventListener('change', (e) => {
        displayShippingFeeFist();
    })

    // Enable place order button when all required fields are filled
    const form = document.getElementById('shipping-form');
    const placeOrderBtn = document.getElementById('place-order-btn');

    form.addEventListener('input', () => {
        if (form.checkValidity()) {
            placeOrderBtn.disabled = false;
        }
    });

    // Place order button click
    placeOrderBtn.addEventListener('click', placeOrder);

    // Save address modal - Confirm save button
    document.getElementById('confirm-save-address-btn').addEventListener('click', () => {
        const modal = bootstrap.Modal.getInstance(document.getElementById('saveAddressModal'));
        modal.hide();
        processOrder(true); // Save address and process order
    });

    // Save address modal - Skip button
    document.getElementById('skip-save-address-btn').addEventListener('click', () => {
        const modal = bootstrap.Modal.getInstance(document.getElementById('saveAddressModal'));
        modal.hide();
        processOrder(false); // Process order without saving address
    });

    // Payment method change event
    document.querySelectorAll('input[name="payment-method"]').forEach(radio => {
        radio.addEventListener('change', (e) => {
            const value = e.target.value;
            const ewalletOptions = document.getElementById('ewallet-options');

            if (value === 'E_WALLET') {
                ewalletOptions.style.display = 'block';
            } else {
                ewalletOptions.style.display = 'none';
            }
        });
    });

    // View vouchers button click
    document.getElementById('view-vouchers-btn').addEventListener('click', async () => {
        // Get modal element and check if it exists
        const modalElement = document.getElementById('voucherModal');
        if (!modalElement) {
            console.error('Voucher modal not found');
            return;
        }

        // Show modal
        const voucherModal = new bootstrap.Modal(modalElement);
        voucherModal.show();

        // Show loading
        document.getElementById('voucher-loading').style.display = 'block';
        document.getElementById('voucher-list').style.display = 'none';
        document.getElementById('no-vouchers-message').style.display = 'none';

        await renderVouchers();
    });

    // Apply voucher button click
    document.getElementById('apply-voucher-btn').addEventListener('click', async () => {
        const voucherCode = document.getElementById('voucher-code-input').value.trim();

        if (!voucherCode) {
            showWarningToast('Vui lòng nhập mã giảm giá');
            return;
        }
        showSuccessToast('Áp dụng mã giảm giá thành công!');
        applyVoucher(voucherCode, 50000, 'Giảm 50K phí vận chuyển');
    });

    // Remove voucher button click
    document.getElementById('remove-voucher-btn').addEventListener('click', () => {
        removeVoucher();
    });
});

