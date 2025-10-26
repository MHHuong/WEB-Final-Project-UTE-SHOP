import cartService from "/js/services/cartService.js";
import {showErrorToast, showInfoToast, showSuccessToast} from "/js/utils/toastUtils.js";
import cartBadgeUtils from "/js/utils/cartBadgeUtils.js";
import couponService from "/js/services/couponService.js";

const USER_ID = 1;
let cartItems = [];
let selectedItems = new Set();
let shopVouchers = {}; // Store selected vouchers per shop

// Format currency
function formatCurrency(amount) {
    if (isNaN(amount)) return "0₫";
    return amount.toLocaleString('vi-VN') + '₫';
}

// Group cart items by shop
function groupByShop(items) {
    const grouped = {};
    items.forEach(item => {
        const shopId = item.productModel.shopId || 0;
        const shopName = item.productModel.shopName || 'Cửa hàng chưa xác định';

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
        if (result.status === 'true') {
            cartItems = result.data;
            renderCartItems();
            updateTotalItems();
        } else {
            cartItems = [];
            renderEmptyCart();
        }
    } catch (error) {
        console.error('Error loading cart:', error);
        showErrorToast('Không thể tải giỏ hàng!');
    }
}

// Render empty cart
function renderEmptyCart() {
    const container = document.getElementById('cart-items-container');
    container.innerHTML = `
                <div class="text-center py-8">
                    <img src="/images/svg-graphics/empty-cart.svg" alt="Empty Cart" style="max-width: 200px;" class="mb-4">
                    <h5>Giỏ hàng trống</h5>
                    <p class="text-muted">Hãy thêm sản phẩm vào giỏ hàng để tiếp tục mua sắm</p>
                    <a href="/" class="btn btn-primary mt-3">Về trang chủ</a>
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
                                    <div class="d-flex align-items-center justify-content-between">
                                        <div class="d-flex align-items-center">
                                            <i class="bi bi-shop text-primary me-2"></i>
                                            <span class="fw-bold">${shop.shopName}</span>
                                        </div>
                                        <button id = "view-vouchers-btn" class="btn btn-sm btn-outline-primary d-none d-md-inline-block"
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
                                    <span id="voucher-code-display-${shop.shopId}" class="voucher-badge me-2"></span>
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
        const shopItems = cartItems.filter(i => i.productModel.shopId === shopId);
        const selectedShopItems = shopItems.filter(i => selectedItems.has(i.cartId));

        cb.checked = selectedShopItems.length === shopItems.length;
        cb.indeterminate = selectedShopItems.length > 0 && selectedShopItems.length < shopItems.length;
    });

    groupedShops.forEach(shop => {
        const total = cartItems.reduce((sum, item) => {
            if (selectedItems.has(item.cartId) && item.productModel.shopId === shop.shopId) {
                return sum + item.productModel.price * item.quantity;
            }
            return sum;
        }, 0);
        const voucher = shopVouchers[shop.shopId];
        if (voucher) {
            const minPrice = voucher.min;
            if (Number(minPrice) >= Number(total)) {
                showInfoToast(`Mã giảm giá ${voucher.code} vượt quá tổng đơn hàng của cửa hàng ${shop.shopName}. Vui lòng chọn mã khác.`);
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
    const total = item.productModel.price * item.quantity;
    const isSelected = selectedItems.has(item.cartId);

    return `
                <div class="product-item p-3 border-bottom ${isSelected ? 'selected' : ''}" data-cart-id="${item.cartId}">
                    <!-- Desktop Layout -->
                    <div class="row align-items-center d-none d-md-flex">
                        <div class="col-1">
                            <div class="form-check">
                                <input class="form-check-input product-checkbox" type="checkbox"
                                       id="item-${item.cartId}" data-cart-id="${item.cartId}"
                                       data-shop-id="${item.productModel.shopId}" ${isSelected ? 'checked' : ''}>
                                <label class="form-check-label" for="item-${item.cartId}"></label>
                            </div>
                        </div>
                        <div class="col-5">
                            <div class="d-flex align-items-center">
                                <img src="${item.productModel.image}" class="product-img me-3" alt="${item.productModel.productName}">
                                <div>
                                    <h6 class="mb-1">${item.productModel.productName}</h6>
                                    <small class="text-muted">Mã SP: ${item.productModel.productId}</small>
                                </div>
                            </div>
                        </div>
                        <div class="col-2 text-center">
                            <span class="fw-bold">${formatCurrency(item.productModel.price)}</span>
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
                                       data-shop-id="${item.productModel.shopId}" ${isSelected ? 'checked' : ''}>
                                <label class="form-check-label" for="item-mobile-${item.cartId}"></label>
                            </div>
                            <img src="${item.productModel.image}" class="product-img me-3" alt="${item.productModel.productName}">
                            <div class="flex-grow-1">
                                <h6 class="mb-1">${item.productModel.productName}</h6>
                                <small class="text-muted d-block mb-2">Mã SP: ${item.productModel.productId}</small>
                                <div class="text-primary fw-bold">${formatCurrency(item.productModel.price)}</div>
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
    const shopItems = cartItems.filter(item => item.productModel.shopId === shopId);

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
    const shopItems = cartItems.filter(item => item.productModel.shopId === shopId);
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
                if (item.productModel.shopId.toString() === shopId) {
                    total += item.productModel.price * item.quantity;
                }
            });
            discount = Math.floor(total * voucher.percentage / 100);
        }
        discountedSubtotal += discount;
    });
    const subtotal = selectedCartItems.reduce((sum, item) =>
        sum + (item.productModel.price * item.quantity), 0);

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
        if (result.status === 'true') {
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
        if (result.status === 'true') {
            showSuccessToast('Đã xóa sản phẩm khỏi giỏ hàng!');
            selectedItems.delete(cartId);
            await loadCartItems();
            await cartBadgeUtils.refreshCartBadge(USER_ID);
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
            discountValue: shopVouchers[item.productModel.shopId]?.type === 'PERCENT'
                ? calculateItemPercentDiscount(shopVouchers[item.productModel.shopId]?.percentage, item)
                : shopVouchers[item.productModel.shopId]?.value || 0
        }))
    const result = await cartService.saveSelectedCartItem(selectedCartItems);
    if (result.status === 'true') {
        window.location.href = `/checkout`;
    }
}

function calculateItemPercentDiscount(percent, item) {
    return Math.floor(item.productModel.price * item.quantity * percent / 100);
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
    const selectedCartItems = cartItems.filter(item => selectedItems.has(item.cartId) && item.productModel.shopId === shopId);
    selectedCartItems.forEach(item => {
        total += item.productModel.price * item.quantity;
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
        const selectedCartItems = cartItems.filter(item => selectedItems.has(item.cartId) && item.productModel.shopId === shopId);
        selectedCartItems.forEach(item => {
            total += item.productModel.price * item.quantity;
        });
        return Math.floor(total * percent / 100);
    }

// Initialize
document.addEventListener('DOMContentLoaded', async () => {
    await loadCartItems();
});