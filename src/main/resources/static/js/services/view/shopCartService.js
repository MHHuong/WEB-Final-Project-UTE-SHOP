import {showErrorToast, showInfoToast, showSuccessToast} from "../../utils/toastUtils.js";
import cartBadgeUtils from "../../utils/cartBadgeUtils.js";
import couponService from "../../services/api/couponService.js";
import cartService from "../../services/api/cartService.js";
import { AuthState } from "../../auth.js";


let cartItems = [];
let selectedItems = new Set();
let shopVouchers = {}; // Store selected vouchers per shop
let USER_ID = localStorage.getItem("userId");




// Format currency
function formatCurrency(amount) {
    if (isNaN(amount)) return "0₫";
    return amount.toLocaleString('vi-VN') + '₫';
}

// Group cart items by shop
function groupByShop(items) {
    const grouped = {};
    items.forEach(item => {
        const shopId = item.productResponse.shopId || 0;
        const shopName = item.productResponse.shopName || 'Cửa hàng chưa xác định';

        if (!grouped[shopId]) {
            grouped[shopId] = {
                shopId: shopId,
                shopName: shopName,
                items: []
            };
        }
        grouped[shopId].items.push(item);
    });
    return Object.values(grouped);
}

// Load cart items
async function loadCartItems() {
    try {
        const result = await cartService.getCartItemByUserId(USER_ID);
        if (result.status === 'Success') {
            cartItems = result.data;
            renderCartItems();
            updateTotalItems();
        } else {
            console.log("No cart items found");
            cartItems = [];
            renderEmptyCart();
        }
    } catch (error) {
        console.error('Error loading cart:', error);
        showErrorToast('Can\'t load cart items!');
    }
}

// Render empty cart
function renderEmptyCart() {
    const container = document.getElementById('cart-items-container');
    container.innerHTML = `
                <div class="text-center py-8">
                    <svg class="empty-cart-icon mb-3 d-inline-block align-middle"
                            width="90"
                            height="90"
                            fill="ccccc"
                            style="transform: translateY(-5px) translateX(-10px)"
                            viewBox="0 0 231.523 231.523" id="Capa_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" xml:space="preserve" stroke="#ccc">
                    <g id="SVGRepo_bgCarrier" stroke-width="0"></g>
                        <g id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"></g>
                            <g id="SVGRepo_iconCarrier"> <g> 
                            <path d="M107.415,145.798c0.399,3.858,3.656,6.73,7.451,6.73c0.258,0,0.518-0.013,0.78-0.04c4.12-0.426,7.115-4.111,6.689-8.231 l-3.459-33.468c-0.426-4.12-4.113-7.111-8.231-6.689c-4.12,0.426-7.115,4.111-6.689,8.231L107.415,145.798z"></path> 
                            <path d="M154.351,152.488c0.262,0.027,0.522,0.04,0.78,0.04c3.796,0,7.052-2.872,7.451-6.73l3.458-33.468 c0.426-4.121-2.569-7.806-6.689-8.231c-4.123-0.421-7.806,2.57-8.232,6.689l-3.458,33.468 C147.235,148.377,150.23,152.062,154.351,152.488z"></path> 
                            <path d="M96.278,185.088c-12.801,0-23.215,10.414-23.215,23.215c0,12.804,10.414,23.221,23.215,23.221 c12.801,0,23.216-10.417,23.216-23.221C119.494,195.502,109.079,185.088,96.278,185.088z M96.278,216.523 c-4.53,0-8.215-3.688-8.215-8.221c0-4.53,3.685-8.215,8.215-8.215c4.53,0,8.216,3.685,8.216,8.215 C104.494,212.835,100.808,216.523,96.278,216.523z"></path> 
                            <path d="M173.719,185.088c-12.801,0-23.216,10.414-23.216,23.215c0,12.804,10.414,23.221,23.216,23.221 c12.802,0,23.218-10.417,23.218-23.221C196.937,195.502,186.521,185.088,173.719,185.088z M173.719,216.523 c-4.53,0-8.216-3.688-8.216-8.221c0-4.53,3.686-8.215,8.216-8.215c4.531,0,8.218,3.685,8.218,8.215 C181.937,212.835,178.251,216.523,173.719,216.523z"></path> 
                            <path d="M218.58,79.08c-1.42-1.837-3.611-2.913-5.933-2.913H63.152l-6.278-24.141c-0.86-3.305-3.844-5.612-7.259-5.612H18.876 c-4.142,0-7.5,3.358-7.5,7.5s3.358,7.5,7.5,7.5h24.94l6.227,23.946c0.031,0.134,0.066,0.267,0.104,0.398l23.157,89.046 c0.86,3.305,3.844,5.612,7.259,5.612h108.874c3.415,0,6.399-2.307,7.259-5.612l23.21-89.25C220.49,83.309,220,80.918,218.58,79.08z M183.638,165.418H86.362l-19.309-74.25h135.895L183.638,165.418z"></path> 
                            <path d="M105.556,52.851c1.464,1.463,3.383,2.195,5.302,2.195c1.92,0,3.84-0.733,5.305-2.198c2.928-2.93,2.927-7.679-0.003-10.607 L92.573,18.665c-2.93-2.928-7.678-2.927-10.607,0.002c-2.928,2.93-2.927,7.679,0.002,10.607L105.556,52.851z"></path> 
                            <path d="M159.174,55.045c1.92,0,3.841-0.733,5.306-2.199l23.552-23.573c2.928-2.93,2.925-7.679-0.005-10.606 c-2.93-2.928-7.679-2.925-10.606,0.005l-23.552,23.573c-2.928,2.93-2.925,7.679,0.005,10.607 C155.338,54.314,157.256,55.045,159.174,55.045z"></path> 
                            <path d="M135.006,48.311c0.001,0,0.001,0,0.002,0c4.141,0,7.499-3.357,7.5-7.498l0.008-33.311c0.001-4.142-3.356-7.501-7.498-7.502 c-0.001,0-0.001,0-0.001,0c-4.142,0-7.5,3.357-7.501,7.498l-0.008,33.311C127.507,44.951,130.864,48.31,135.006,48.311z"></path> 
                        </g> 
                    </g>
                    </svg>
                    <h5>Giỏ hàng trống</h5>
                    <p class="text-muted">Add products to cart to continue shopping</p>
                    <a href="/UTE_SHOP" class="btn btn-primary mt-3">Back to home page</a>
                </div>
            `;
}

// Render cart items grouped by shop
function renderCartItems() {
    const container = document.getElementById('cart-items-container');
    if (cartItems.length === 0) {
        renderEmptyCart();
        return;
    }

    const groupedShops = groupByShop(cartItems);
    let html = '';

    groupedShops.forEach(shop => {
        html += `
                    <div class="shop-section mb-3">
                        <!-- Shop Header -->
                        <div class="shop-header p-3">
                            <div class="row align-items-center">
                                <div class="col-1">
                                    <div class="form-check">
                                        <input class="form-check-input shop-checkbox" type="checkbox"
                                               id="shop-${shop.shopId}" data-shop-id="${shop.shopId}">
                                        <label class="form-check-label" for="shop-${shop.shopId}"></label>
                                    </div>
                                </div>
                                <div class="col-11">
                                    <div class="d-flex align-items-center justify-content-between flex-wrap">
                                        <div class="d-flex align-items-center mb-2 mb-md-0">
                                            <i class="bi bi-shop text-primary me-2"></i>
                                            <span class="fw-bold">${shop.shopName}</span>
                                        </div>
                                        <button id="view-vouchers-btn-${shop.shopId}" class="btn btn-sm btn-outline-primary" 
                                                style="width: auto; min-width: 150px;"
                                                onclick="selectVoucher(${shop.shopId})">
                                            <i class="bi bi-ticket-perforated me-1"></i>Chọn mã giảm giá
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Shop Voucher Section (if voucher selected) -->
                        <div id="selected-voucher-display-${shop.shopId}" class="shop-voucher-section" style="display: none ">
                            <div class="d-flex align-items-center justify-content-between w-100">
                                <div class="d-flex align-items-center">
                                    <i class="bi bi-ticket-perforated text-warning me-2"></i>
                                    <span id="voucher-code-display-${shop.shopId}" class="voucher-badge me-2 bg-warning-subtle text-warning fw-medium px-2 py-1 rounded"></span>
                                    <small id="voucher-desc-display-${shop.shopId}" class="text-muted"></small>
                                </div>
                              <button type="button" class="btn-close" aria-label="Close"
                                onclick="removeVoucher(${shop.shopId})"></button>
                            </div>
                        </div>
                       
                        <!-- Shop Products -->
                        <div class="shop-products">
                            ${shop.items.map(item => renderProductItem(item)).join('')}
                        </div>
                    </div>
                `;
    });

    container.innerHTML = html;
    attachEventListeners();
    document.querySelectorAll('.product-checkbox').forEach(cb => {
        const cartId = parseInt(cb.dataset.cartId);
        cb.checked = selectedItems.has(cartId);
    });

    document.querySelectorAll('.shop-checkbox').forEach(cb => {
        const shopId = parseInt(cb.dataset.shopId);
        const shopItems = cartItems.filter(i => i.productResponse.shopId === shopId);
        const selectedShopItems = shopItems.filter(i => selectedItems.has(i.cartId));

        cb.checked = selectedShopItems.length === shopItems.length;
        cb.indeterminate = selectedShopItems.length > 0 && selectedShopItems.length < shopItems.length;
    });

    groupedShops.forEach(shop => {
        const total = cartItems.reduce((sum, item) => {
            if (selectedItems.has(item.cartId) && item.productResponse.shopId === shop.shopId) {
                return sum + item.productResponse.price * item.quantity;
            }
            return sum;
        }, 0);
        const voucher = shopVouchers[shop.shopId];
        if (voucher) {
            const minPrice = voucher.min;
            if (Number(minPrice) >= Number(total)) {
                showInfoToast(`Mã giảm giá ${voucher.code} vượt quá tổng đơn h��ng của cửa hàng ${shop.shopName}. Vui lòng chọn mã khác.`);
                removeVoucher(shop.shopId);
            }
        }
    });
    Object.entries(shopVouchers).forEach(([shopId, voucher]) => {
        applyVoucher(voucher.code, voucher.value, voucher.description, shopId);
    });
}

// Render single product item
function renderProductItem(item) {
    const total = item.productResponse.price * item.quantity;
    const isSelected = selectedItems.has(item.cartId);
    return `
                <div class="product-item p-3 border-bottom ${isSelected ? 'selected' : ''}" data-cart-id="${item.cartId}">
                    <!-- Desktop Layout -->
                    <div class="row align-items-center d-none d-md-flex">
                        <div class="col-1">
                            <div class="form-check">
                                <input class="form-check-input product-checkbox" type="checkbox"
                                       id="item-${item.cartId}" data-cart-id="${item.cartId}"
                                       data-shop-id="${item.productResponse.shopId}" ${isSelected ? 'checked' : ''}>
                                <label class="form-check-label" for="item-${item.cartId}"></label>
                            </div>
                        </div>
                        <div class="col-5">
                            <div class="d-flex align-items-center">
                                <img src="${item.productResponse.image}" class="product-img me-3" alt="${item.productResponse.productName}">
                                <div>
                                    <h6 class="mb-1">${item.productResponse.productName}</h6>
                                    <small class="text-muted">Mã SP: ${item.productResponse.productId}</small>
                                </div>
                            </div>
                        </div>
                        <div class="col-2 text-center">
                            <span class="fw-bold">${formatCurrency(item.productResponse.price)}</span>
                        </div>
                        <div class="col-2 text-center">
                            <div class="input-group input-group-sm justify-content-center">
                                <button class="btn btn-outline-secondary btn-quantity" type="button"
                                        onclick="decreaseQuantity(${item.cartId})">-</button>
                                <input type="number" class="form-control quantity-input"
                                       value="${item.quantity}" min="1" max="10" readonly>
                                <button class="btn btn-outline-secondary btn-quantity" type="button"
                                        onclick="increaseQuantity(${item.cartId})">+</button>
                            </div>
                        </div>
                        <div class="col-2 text-end">
                            <div class="d-flex justify-content-end align-items-center">
                                <span class="fw-bold text-primary me-3">${formatCurrency(total)}</span>
                                <button class="btn btn-link text-muted delete-btn p-0"
                                        onclick="removeItem(${item.cartId})">
                                    <i class="bi bi-trash fs-5"></i>
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- Mobile Layout -->
                    <div class="d-md-none">
                        <div class="d-flex align-items-start mb-3">
                            <div class="form-check me-3 mt-2">
                                <input class="form-check-input product-checkbox" type="checkbox"
                                       id="item-mobile-${item.cartId}" data-cart-id="${item.cartId}"
                                       data-shop-id="${item.productResponse.shopId}" ${isSelected ? 'checked' : ''}>
                                <label class="form-check-label" for="item-mobile-${item.cartId}"></label>
                            </div>
                            <img src="${item.productResponse.image}" class="product-img me-3" alt="${item.productResponse.productName}">
                            <div class="flex-grow-1">
                                <h6 class="mb-1">${item.productResponse.productName}</h6>
                                <small class="text-muted d-block mb-2">Mã SP: ${item.productResponse.productId}</small>
                                <div class="text-primary fw-bold">${formatCurrency(item.productResponse.price)}</div>
                            </div>
                            <button class="btn btn-link text-muted delete-btn p-0 ms-2"
                                    onclick="removeItem(${item.cartId})">
                                <i class="bi bi-trash fs-5"></i>
                            </button>
                        </div>

                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <small class="text-muted d-block mb-1">Số lượng</small>
                                <div class="input-group input-group-sm" style="width: auto;">
                                    <button class="btn btn-outline-secondary btn-quantity" type="button"
                                            onclick="decreaseQuantity(${item.cartId})">-</button>
                                    <input type="number" class="form-control quantity-input"
                                           value="${item.quantity}" min="1" max="10" readonly>
                                    <button class="btn btn-outline-secondary btn-quantity" type="button"
                                            onclick="increaseQuantity(${item.cartId})">+</button>
                                </div>
                            </div>
                            <div class="text-end">
                                <small class="text-muted d-block mb-1">Thành tiền</small>
                                <div class="fw-bold text-primary fs-6">${formatCurrency(total)}</div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
}

// Attach event listeners
function attachEventListeners() {
    // Select all checkbox
    const selectAllCheckbox = document.getElementById('select-all');
    selectAllCheckbox?.addEventListener('change', handleSelectAll);

    // Shop checkboxes
    document.querySelectorAll('.shop-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', handleShopCheckbox);
    });

    // Product checkboxes
    document.querySelectorAll('.product-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', handleProductCheckbox);
    });
}

// Handle select all
function handleSelectAll(e) {
    const isChecked = e.target.checked;
    selectedItems.clear();

    if (isChecked) {
        cartItems.forEach(item => selectedItems.add(item.cartId));
    }

    renderCartItems();
    updateOrderSummary();
}

// Handle shop checkbox
function handleShopCheckbox(e) {
    const shopId = parseInt(e.target.dataset.shopId);
    const isChecked = e.target.checked;
    const shopItems = cartItems.filter(item => item.productResponse.shopId === shopId);

    console.log(shopId)
    shopItems.forEach(item => {
        if (isChecked) {
            selectedItems.add(item.cartId);
        } else {
            selectedItems.delete(item.cartId);
        }
    });

    renderCartItems();
    updateOrderSummary();
}

// Handle product checkbox
function handleProductCheckbox(e) {
    const cartId = parseInt(e.target.dataset.cartId);

    if (e.target.checked) {
        selectedItems.add(cartId);
    } else {
        selectedItems.delete(cartId);
    }

    // Update shop checkbox
    const shopId = parseInt(e.target.dataset.shopId);
    updateShopCheckbox(shopId);

    renderCartItems();
    updateOrderSummary();
}

// Update shop checkbox state
function updateShopCheckbox(shopId) {
    const shopItems = cartItems.filter(item => item.productResponse.shopId === shopId);
    const selectedShopItems = shopItems.filter(item => selectedItems.has(item.cartId));
    const shopCheckbox = document.querySelector(`#shop-${shopId}`);

    if (shopCheckbox) {
        shopCheckbox.checked = selectedShopItems.length === shopItems.length;
        shopCheckbox.indeterminate = selectedShopItems.length > 0 && selectedShopItems.length < shopItems.length;
    }
}

// Update total items
function updateTotalItems() {
    const totalItems = cartItems.reduce((sum, item) => sum + item.quantity, 0);
    document.getElementById('total-items').textContent = totalItems;
}

// Update order summary
function updateOrderSummary() {
    const selectedCartItems = cartItems.filter(item => selectedItems.has(item.cartId));
    const selectedCount = selectedCartItems.length;

    let discountedSubtotal = 0
    Object.entries(shopVouchers).forEach(([shopId, voucher]) => {
        let discount = voucher.value;
        if (voucher.type === 'PERCENT') {
            let total = 0;
            selectedCartItems.forEach(item => {
                if (item.productResponse.shopId.toString() === shopId) {
                    total += item.productResponse.price * item.quantity;
                }
            });
            discount = Math.floor(total * voucher.percentage / 100);
        }
        discountedSubtotal += discount;
    });
    const subtotal = selectedCartItems.reduce((sum, item) =>
        sum + (item.productResponse.price * item.quantity), 0);

    document.getElementById('selected-count').textContent = selectedCount;
    document.getElementById('checkout-count').textContent = selectedCount;
    document.getElementById('discount').textContent = `-${formatCurrency(discountedSubtotal)}`;
    document.getElementById('subtotal').textContent = formatCurrency(subtotal);
    document.getElementById('total').textContent = formatCurrency(subtotal - discountedSubtotal);

    // Enable/disable checkout button
    const checkoutBtn = document.getElementById('checkout-btn');
    checkoutBtn.disabled = selectedCount === 0;
}

// Increase quantity
window.increaseQuantity = async function(cartId) {
    const item = cartItems.find(i => i.cartId === cartId);
    if (item && item.quantity < 10) {
        await updateQuantity(cartId, item.quantity + 1);
    }
}

// Decrease quantity
window.decreaseQuantity = async function(cartId) {
    const item = cartItems.find(i => i.cartId === cartId);
    if (item && item.quantity > 1) {
        await updateQuantity(cartId, item.quantity - 1);
    }
}

// Update quantity
async function updateQuantity(cartId, newQuantity) {
    try {
        const result = await cartService.updateCartItem(cartId, newQuantity);
        if (result.status === 'Success') {
            showSuccessToast('Cập nhật số lượng thành công!');
            await loadCartItems();
            await cartBadgeUtils.refreshCartBadge(USER_ID);
            updateOrderSummary();
        } else {
            showErrorToast('Không thể cập nhật số lượng!');
        }
    } catch (error) {
        console.error('Error updating quantity:', error);
        showErrorToast('Có lỗi xảy ra!');
    }
}

// Remove item
window.removeItem = async function(cartId) {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;

    try {
        const result = await cartService.removeCartItem(cartId);
        if (result.status === 'Success') {
            showSuccessToast('Đã xóa sản phẩm khỏi giỏ hàng!');
            selectedItems.delete(cartId);
            await loadCartItems();
            await cartBadgeUtils.refreshCartBadge(getUserId());
            updateOrderSummary();
        } else {
            showErrorToast('Không thể xóa sản phẩm!');
        }
    } catch (error) {
        console.error('Error removing item:', error);
        showErrorToast('Có lỗi xảy ra!');
    }
}

// Select voucher for shop
window.selectVoucher = async function(shopId) {
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

    await renderVouchers(shopId);
    // renderCartItems();
    // updateOrderSummary();
}

window.handleNavigation = async () => {
    const discountText = document.getElementById('discount').innerText.replace(/₫/g, '').replace(/\./g, '');
    const discountAmount = - parseInt(discountText) || 0;
    const selectedCartItems = cartItems.filter(item => selectedItems.has(item.cartId))
        .map(item => ({
            ...item,
            discount: discountAmount,
            discountValue: shopVouchers[item.productResponse.shopId]?.type === 'PERCENT'
                ? calculateItemPercentDiscount(shopVouchers[item.productResponse.shopId]?.percentage, item)
                : shopVouchers[item.productResponse.shopId]?.value || 0
        }))
    const result = await cartService.saveSelectedCartItem(selectedCartItems);
    if (result.status === 'Success') {
        window.location.href = `/UTE_SHOP/user/checkout`;
    }
}

function calculateItemPercentDiscount(percent, item) {
    return Math.floor(item.productResponse.price * item.quantity * percent / 100);
}

async function renderVouchers(shopId) {
    let vouchers = [];
    try {
        const result = await couponService.getAllShopCoupons(shopId);
        if (result.status === 'Success' && result.data) {
            vouchers = result.data;
        } else showErrorToast(result.message);
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
    let total = 0;
    const selectedCartItems = cartItems.filter(item => selectedItems.has(item.cartId) && item.productResponse.shopId === shopId);
    selectedCartItems.forEach(item => {
        total += item.productResponse.price * item.quantity;
    });
    vouchers.forEach(voucher => {
        const isDisabled = Number(total) < Number(voucher.minOrderAmount);
        const discountValue = voucher.discountType === 'PERCENT'
            ? `${voucher.value}%`
            : `${formatCurrency(voucher.value)}`;
        const discountLabel = 'GIẢM';
        const description = `Giảm ${discountValue} cho các hóa đơn từ ${formatCurrency(voucher.minOrderAmount)} trở lên`

        const voucherCard = document.createElement('div');
        voucherCard.className = `voucher-card  border border-secondary-subtle rounded p-3 mb-3 bg-white position-relative cursor-pointer d-flex gap-3 ${isDisabled ? 'disabled' : ''}`;
        voucherCard.innerHTML = `
            <div class="voucher-left d-flex flex-column align-items-center justify-content-center rounded p-2 flex-shrink-0 text-white">
                <div class="discount-value text-white fw-bold lh-sm">${discountValue}</div>
                <div class="discount-label text-white text-uppercase">${discountLabel}</div>
            </div>
            <div class="voucher-content d-flex flex-column justify-content-center flex-fill">
                <div class="voucher-code fw-semibold text-dark mb-1">${voucher.code}</div>
                <div class="voucher-description text-secondary mb-1 lh-sm">${description}</div>
                <div class="voucher-expiry">HSD: ${new Date(voucher.expiredAt).toLocaleDateString('vi-VN')}</div>
            </div>
            <div class="voucher-action d-flex align-items-center justify-content-center flex-shrink-0">
                <button class="btn-apply btn btn-outline-success btn-sm fw-medium px-3 py-1 rounded-1" ${isDisabled ? 'disabled' : ''}>
                    ${isDisabled ? 'Không thể dùng' : 'Áp dụng'}
                </button>
            </div>
        `;

        if (!isDisabled) {
            voucherCard.onclick = () => {
                const discountAmount = voucher.discountType === 'PERCENT'
                    ? calculatePercentDiscount(voucher.value, shopId)
                    : voucher.value;
                shopVouchers[shopId] = {
                    code: voucher.code,
                    value: discountAmount,
                    min: voucher.minOrderAmount,
                    description: description,
                    type: voucher.discountType,
                    percentage: voucher.value
                }
                applyVoucher(voucher.code, discountAmount, description, shopId);
                bootstrap.Modal.getInstance(document.getElementById('voucherModal')).hide();
                showSuccessToast(`Đã áp dụng mã ${voucher.code}`);
            };
        }
        container.appendChild(voucherCard);
    });
    container.style.display = 'block';
}

function applyVoucher(code, discountAmount, description, shopId) {
    // Update display
    document.getElementById(`voucher-code-display-${shopId}`).textContent = code;
    document.getElementById(`voucher-desc-display-${shopId}`).textContent = description;

    // Show selected voucher, hide input section
    document.getElementById(`selected-voucher-display-${shopId}`).style.display = 'flex';
    // document.getElementById('voucher-selection-section').style.display = 'none';

    // Update discount in order summary
    document.getElementById('discount').textContent = `-${formatCurrency(discountAmount)}`;
    updateOrderSummary()
}

// Remove voucher from shop
window.removeVoucher = function(shopId) {
    showSuccessToast('Đã bỏ chọn mã giảm giá!');

    document.getElementById(`voucher-code-display-${shopId}`).textContent = '';
    document.getElementById(`voucher-desc-display-${shopId}`).textContent = '';

    // Show selected voucher, hide input section
    document.getElementById(`selected-voucher-display-${shopId}`).style.display = 'none';
    delete shopVouchers[shopId];

    renderCartItems();
    updateOrderSummary();
}


function calculatePercentDiscount(percent, shopId) {
    let total = 0;
    const selectedCartItems = cartItems.filter(item => selectedItems.has(item.cartId) && item.productResponse.shopId === shopId);
    selectedCartItems.forEach(item => {
        total += item.productResponse.price * item.quantity;
    });
    return Math.floor(total * percent / 100);
}

// Initialize
document.addEventListener('DOMContentLoaded', async () => {
    await AuthState.fetchUserInfo()
    if (USER_ID === 0) {
        USER_ID = AuthState.getUserId() || 0;
    }
    USER_ID = AuthState.getUserId() || 0;
    if (USER_ID === 0) {
        setTimeout(async () => {
            showErrorToast('User not authenticated. Redirecting to login page.');
            window.location.href = '/login';
        },2000)
    }
    await loadCartItems();


});