import {showErrorToast, showSuccessToast} from "../../utils/toastUtils.js";
import addressService from "../../services/api/addressService.js";
import cartService from "../../services/api/cartService.js";
import orderService from "../../services/api/orderService.js";
import favoriteService from "../../services/api/favoriteService.js";
import {renderPagination, showPageInfo} from "../../utils/paginationUtils.js";
import {loadDistricts, loadProvinces, loadWards} from "../../utils/locationUtils.js";
import {showOrderStatusModal} from "../../utils/orderStatusModal.js";

import {AuthState} from "../../auth.js";


// Hàm lấy USER_ID động
const getUserId = () => AuthState.getUserId() || 0;
let USER_ID = 0; // Sẽ được set sau khi AuthState load xong
let isEditingAddress = false;
let editingAddressId = null;
let orders = [];
let currentOrderStatus = 'all'; // Track current filter status
let currentSearchKeyword = ''; // Track current search keyword
const BASE_URL = window.location.origin
// Pagination state for addresses
let addressPagination = {
    currentPage: 0,
    size: 4,
    totalPages: 0,
    totalElements: 0
};

// Section Navigation
document.querySelectorAll('.nav-link[data-section]').forEach(link => {
    link.addEventListener('click', function (e) {
        e.preventDefault();
        const section = this.getAttribute('data-section');
        console.log(section);

        // Update active state
        document.querySelectorAll('.nav-link[data-section]').forEach(l => l.classList.remove('active'));
        this.classList.add('active');

        // Show/hide sections
        document.querySelectorAll('.content-section').forEach(s => s.style.display = 'none');
        document.getElementById(section + '-section').style.display = 'block';

        // Load data if needed
        if (section === 'orders') {
            loadOrders();
        } else if (section === 'love') {
            loadLovedProducts();
        } else if (section === 'security') {
            loadSecuritySection();
        }
    });
});

function loadProfileSection(UserInfo) {
    console.log(UserInfo);
    const fullNameInput = document.getElementById('fullName');
    const emailInput = document.getElementById('email');
    const phoneInput = document.getElementById('phone');

    const fullName = document.getElementById('user-fullname');
    const email = document.getElementById('user-email');

    fullNameInput.value = UserInfo.fullName || '';
    emailInput.value = UserInfo.email || '';
    phoneInput.value = UserInfo.phone || '';

    fullName.textContent = UserInfo.fullName || 'Người dùng';
    email.textContent = UserInfo.email || 'Chưa cập nhật email';
}

document.getElementById('personal-info-form').addEventListener('submit', async function (e) {
    e.preventDefault();
    const fullName = document.getElementById('fullName').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const email = document.getElementById('email').value.trim();

    // Validation
    let isValid = true;

    // Validate fullName
    if (!fullName) {
        isValid = false;
        document.getElementById('fullName').classList.add('is-invalid');
    } else if (fullName.length < 2) {
        isValid = false;
        document.getElementById('fullName').classList.add('is-invalid');
    } else {
        document.getElementById('fullName').classList.remove('is-invalid');
        document.getElementById('fullName').classList.add('is-valid');
    }

    // Validate phone
    const phoneRegex = /^(0|\+84)[3|5|7|8|9][0-9]{8}$/;
    if (!phone) {
        isValid = false;
        document.getElementById('phone').classList.add('is-invalid');
        showErrorToast('Phone number is required!');
    } else if (!phoneRegex.test(phone)) {
        isValid = false;
        document.getElementById('phone').classList.add('is-invalid');
        showErrorToast('Phone number should be valid! 10 digits starting with 0 or +84');
    } else {
        document.getElementById('phone').classList.remove('is-invalid');
        document.getElementById('phone').classList.add('is-valid');
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email) {
        isValid = false;
        document.getElementById('email').classList.add('is-invalid');
    } else if (!emailRegex.test(email)) {
        isValid = false;
        document.getElementById('email').classList.add('is-invalid');
        showErrorToast("Email should be valid!");
    } else {
        document.getElementById('email').classList.remove('is-invalid');
        document.getElementById('email').classList.add('is-valid');
    }

    if (!isValid) {
        return;
    }

    try {
        const user = {
            fullName,
            phone,
            email
        }
        const result = await AuthState.updateUserInfo(USER_ID, user);
        if (result.status === "Success") {
            showSuccessToast('Update information successfully!');
            const userInfo = AuthState.getUserInfo();
            loadProfileSection(userInfo);
            // Remove validation classes after successful update
            document.getElementById('fullName').classList.remove('is-valid', 'is-invalid');
            document.getElementById('phone').classList.remove('is-valid', 'is-invalid');
            document.getElementById('email').classList.remove('is-valid', 'is-invalid');
        } else {
            showErrorToast(result.message || 'Cannot update information');
        }
    } catch (error) {
        console.error('Error updating profile:', error);
        showErrorToast('Cannot update information');
    }
});

// Load Addresses with pagination
async function loadAddresses(page = 0, size = 4) {
    try {
        console.log(USER_ID);
        let addresses = [];
        const result = await addressService.getAddressesPaginationByUserId(USER_ID, size, page);
        if (result.status === "Success") {
            addresses = result.data.content;
            addressPagination = {
                currentPage: result.data.pageable.pageNumber,
                size: result.data.pageable.pageSize,
                totalPages: result.data.totalPages,
                totalElements: result.data.totalElements
            };
        } else {
            addresses = []
            showErrorToast('Cannot load addresses');
        }
        const container = document.getElementById('addresses-list');
        container.innerHTML = '';

        if (addresses.length === 0) {
            container.innerHTML = `
                <div class="col-12">
                    <div class="text-center py-5">
                        <i class="bi bi-inbox" style="font-size: 3rem; color: #ccc;"></i>
                        <p class="text-muted mt-3">No addresses found. Please add a new address.</p>
                    </div>
                </div>
            `;
        } else {
            addresses.map(addr => {
                const addressCard = `
                            <div class="col-md-6">
                                <div class="card ${addr.isDefault ? 'border-primary' : ''}">
                                    <div class="card-body">
                                        ${addr.isDefault ? '<span class="badge bg-primary mb-2">Default</span>' : ''}
                                        <h6>${addr.receiverName}</h6>
                                        <p class="mb-1 text-muted small">${addr.phone}</p>
                                        <p class="mb-0 text-muted small">${addr.addressDetail}, ${addr.ward}, ${addr.district}, ${addr.province}</p>
                                        <div class="mt-2">
                                            <button class="edit-address-btn btn btn-sm btn-outline-primary"
                                            th:data-id="${addr.addressId}"
                                            th:data-receiver-name="${addr.receiverName}"
                                            th:data-phone="${addr.phone}"
                                            th:data-province="${addr.province}"
                                            th:data-district="${addr.district}"
                                            th:data-ward="${addr.ward}"
                                            th:data-address-detail="${addr.addressDetail}"
                                            th:data-is-default="${addr.isDefault}"
                                            >Edit</button>
                                            <button class="remove-address-btn btn btn-sm btn-outline-danger"  th:data-id="${addr.addressId}">Delete</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        `;
                container.innerHTML += addressCard;
            });
        }

        // Render pagination
        renderPagination(addressPagination, (newPage) => {
            loadAddresses(newPage, addressPagination.size);
        }, 'address-pagination');
        showPageInfo(addressPagination, 'address-page-info');
    } catch (error) {
        console.error('Error loading addresses:', error);
        showErrorToast('Cannot load addresses');
    }
}

document.addEventListener("click", async function (e) {
    if (e.target.classList.contains("edit-address-btn")) {
        const btn = e.target.closest(".edit-address-btn");
        if (!btn) return;
        isEditingAddress = true;

        // Use existing modal instance or get it
        const modalEl = document.getElementById('addAddressModal');
        const addModal = bootstrap.Modal.getOrCreateInstance(modalEl);
        addModal.show();

        editingAddressId = e.target.getAttribute("th:data-id");
        document.getElementById('receiver-name').value = e.target.getAttribute("th:data-receiver-name");
        document.getElementById('receiver-phone').value = e.target.getAttribute("th:data-phone");
        document.getElementById('address-detail').value = e.target.getAttribute("th:data-address-detail");

        const provinceName = e.target.getAttribute("th:data-province");
        const districtName = e.target.getAttribute("th:data-district");
        const wardName = e.target.getAttribute("th:data-ward");

        // Set province
        const provinceSelect = document.getElementById('province');
        const provinceOption = Array.from(provinceSelect.options).find(opt => opt.text === provinceName);
        if (provinceOption) {
            provinceSelect.value = provinceOption.value;
            await loadDistricts(provinceSelect.value);

            // Set district
            const districtSelect = document.getElementById('district');
            const districtOption = Array.from(districtSelect.options).find(opt => opt.text === districtName);
            if (districtOption) {
                districtSelect.value = districtOption.value;
                await loadWards(districtSelect.value);

                // Set ward
                const wardSelect = document.getElementById('ward');
                const wardOption = Array.from(wardSelect.options).find(opt => opt.text === wardName);
                if (wardOption) {
                    wardSelect.value = wardOption.value;
                }
            }
        }
        console.log(e.target.getAttribute("th:data-is-default"));
        document.getElementById('isDefault').checked = e.target.getAttribute("th:data-is-default") === '1';
    }
});

document.addEventListener("click", async function (e) {
    if (e.target.classList.contains("remove-address-btn")) {
        const btn = e.target.closest(".remove-address-btn");
        if (!btn) return;

        const id = e.target.getAttribute("th:data-id");
        try {
            const result = await addressService.removeAddress(id, USER_ID);
            if (result.status === "Success") {
                showSuccessToast('Delete address successfully!');
                await loadAddresses();
            } else {
                showErrorToast(result.message || 'Xóa địa chỉ thất bại');
            }
        } catch (error) {
            console.error('Error deleting address:', error);
            showErrorToast('Cannot delete address');
        }
    }
});


// Add Address
document.getElementById('save-address-btn').addEventListener('click', async function (e) {
    e.preventDefault();
    const receiverName = document.getElementById('receiver-name').value.trim();
    const phone = document.getElementById('receiver-phone').value.trim();
    const provinceSelect = document.getElementById('province');
    const districtSelect = document.getElementById('district');
    const wardSelect = document.getElementById('ward');
    const addressDetail = document.getElementById('address-detail').value.trim();
    const isDefault = document.getElementById('isDefault').checked;

    // Simple validation
    let isValid = true;

    if (!receiverName) {
        isValid = false;
        document.getElementById('receiver-name').classList.add('is-invalid');
    } else {
        document.getElementById('receiver-name').classList.remove('is-invalid');
    }

    if (!phone) {
        isValid = false;
        document.getElementById('receiver-phone').classList.add('is-invalid');
    } else {
        document.getElementById('receiver-phone').classList.remove('is-invalid');
    }

    if (!provinceSelect.value) {
        isValid = false;
        document.getElementById('province').classList.add('is-invalid');
    } else {
        document.getElementById('province').classList.remove('is-invalid');
    }

    if (!districtSelect.value) {
        isValid = false;
        document.getElementById('district').classList.add('is-invalid');
    } else {
        document.getElementById('district').classList.remove('is-invalid');
    }

    if (!wardSelect.value) {
        isValid = false;
        document.getElementById('ward').classList.add('is-invalid');
    } else {
        document.getElementById('ward').classList.remove('is-invalid');
    }

    if (!addressDetail) {
        isValid = false;
        document.getElementById('address-detail').classList.add('is-invalid');
    } else {
        document.getElementById('address-detail').classList.remove('is-invalid');
    }

    if (!isValid) {
        return;
    }

    const addressData = {
        receiverName,
        phone,
        province: provinceSelect.options[provinceSelect.selectedIndex].text,
        district: districtSelect.options[districtSelect.selectedIndex].text,
        ward: wardSelect.options[wardSelect.selectedIndex].text,
        addressDetail,
        isDefault: isDefault ? 1 : 0
    };

    try {
        let result;
        if (isEditingAddress && editingAddressId) {
            addressData.addressId = editingAddressId;
            result = await addressService.updateAddress(addressData, USER_ID);
        } else {
            result = await addressService.createAddress(addressData, USER_ID);
        }

        if (result.status === "Success") {
            showSuccessToast(isEditingAddress ? 'Edit address successfully!' : 'Add address successfully!');
            await loadAddresses();
            await resetAddressForm();
        } else {
            showErrorToast(result.message || (isEditingAddress ? 'Cannot edit address' : 'Cannot add address'));
        }
    } catch (error) {
        console.error('Error saving address:', error);
        showErrorToast(isEditingAddress ? 'Cannot edit address' : 'Cannot add address');
    }
});

async function resetAddressForm() {
    const existingBackdrops = document.querySelectorAll('.modal-backdrop');
    existingBackdrops.forEach(backdrop => backdrop.remove());


    const modalEl = document.getElementById('addAddressModal');
    const modalInstance = bootstrap.Modal.getInstance(modalEl);


    if (modalInstance) {
        modalInstance.hide();
    }


    const form = document.getElementById('add-address-form');
    if (form) {
        form.reset();
    }


    isEditingAddress = false;
    editingAddressId = null;

    // Remove invalid classes
    document.getElementById('province')?.classList.remove('is-invalid');
    document.getElementById('district')?.classList.remove('is-invalid');
    document.getElementById('ward')?.classList.remove('is-invalid');
    document.getElementById('receiver-name')?.classList.remove('is-invalid');
    document.getElementById('receiver-phone')?.classList.remove('is-invalid');
    document.getElementById('address-detail')?.classList.remove('is-invalid');
}

// Load Orders
async function loadOrders() {
    try {
        const result = await orderService.getOrderByUserId(USER_ID);
        if (result.status !== "Success") {
            showErrorToast('Cannot load orders');
        } else orders = result.data;
    } catch (error) {
        console.error('Error loading orders:', error);
        showErrorToast('Cannot load orders');
    }
}

function paginateOrders(filteredOrders, page, size) {
    const start = (page - 1) * size;
    const end = start + size;
    return {
        content: filteredOrders.slice(start, end),
        pageable: {
            pageNumber: page - 1,
            pageSize: size
        },
        totalPages: Math.ceil(filteredOrders.length / size),
        totalElements: filteredOrders.length
    };
}


// Search and filter orders
function searchAndFilterOrders(searchKeyword = '', status = 'all') {
    let filteredOrders = orders;
    if (status !== 'all') {
        filteredOrders = filteredOrders.filter(order => order.status === status);
    }

    if (status === 'RETURNED' || status === 'REQUEST_RETURN' || status === 'RETURNING') {
        filteredOrders = filteredOrders.filter(order => order.status === status);
    }

    // Then filter by search keyword
    if (searchKeyword.trim() !== '') {
        const keyword = searchKeyword.toLowerCase().trim();
        filteredOrders = filteredOrders.filter(order => {
            // Search in order ID
            const orderIdMatch = order.orderId.toString().toLowerCase().includes(keyword);

            // Search in produ
            const productMatch = order.orderItem.some(item =>
                item.productName.toLowerCase().includes(keyword)
            );

            // Search in receiver name
            const receiverMatch = order.receiverName?.toLowerCase().includes(keyword) || false;

            // Search in phone
            const phoneMatch = order.phone?.includes(keyword) || false;

            return orderIdMatch || productMatch || receiverMatch || phoneMatch;
        });
    }

    return filteredOrders;
}

async function displayOrders(status = 'all', page = 1, size = 5, searchKeyword = '') {
    const container = document.getElementById('orders-list');
    container.innerHTML = '';

    const filteredOrders = searchAndFilterOrders(searchKeyword, status);

    const result = paginateOrders(filteredOrders, page, size);
    const orderPagination = {
        currentPage: result.pageable.pageNumber,
        size: result.pageable.pageSize,
        totalPages: result.totalPages,
        totalElements: result.totalElements
    }
    const orderContent = result.content;
    if (orderContent.length === 0) {
        const message = searchKeyword.trim() !== ''
            ? `Cannot find order with "${searchKeyword}"`
            : 'No orders found for the selected status.';
        container.innerHTML = `
                        <div class="text-center py-5">
                            <i class="bi bi-inbox" style="font-size: 3rem; color: #ccc;"></i>
                            <p class="text-muted mt-3">${message}</p>
                        </div>
                    `;
        return;
    }
    orderContent.forEach(order => {
        // Highlight search keyword in order ID and product names
        const highlightText = (text, keyword) => {
            if (!keyword.trim()) return text;
            const regex = new RegExp(`(${keyword})`, 'gi');
            return text.replace(regex, '<mark>$1</mark>');
        };

        const orderCard = `
                        <div class="card mb-3">
                            <div class="card-header bg-light d-flex justify-content-between align-items-center">
                                <div>
                                    <strong>Order ID: ${highlightText(order.orderId.toString(), searchKeyword)}</strong>
                                    <span class="text-muted ms-3">${new Date(order.createdAt).toLocaleDateString('vi-VN')}</span>
                                </div>
                                <span class="badge bg-success">${getStatusText(order.status)}</span>
                            </div>
                            <div class="card-body">
                                ${order.orderItem.map(item => `
                                    <div class="d-flex justify-content-between mb-2">
                                        <div>
                                            <h6 class="mb-0">${highlightText(item.productName, searchKeyword)}</h6>
                                            <small class="text-muted">x${item.quantity}</small>
                                        </div>
                                        <div class="text-end">
                                            <strong>${formatCurrency(item.unitPrice * item.quantity)}</strong>
                                        </div>
                                    </div>
                                    ${order.status === 'RECEIVED' ? `
                                                                      <div class="text-end mt-1">
                                                                        <button class="btn btn-sm btn-outline-success btn-open-review"
                                                                                data-order-id="${order.orderId}"
                                                                                data-product-id="${item.productId}"
                                                                                data-product-name="${item.productName}">
                                                                            Review
                                                                        </button>
                                                                      </div>
                                                                    ` : ''}
                                `).join('')}
                                <hr>
                                <div class="d-flex justify-content-between align-items-center">
                                    <h6 class="mb-0">Total:</h6>
                                    <h5 class="mb-0 text-primary">${formatCurrency(order.totalAmount)}</h5>
                                </div>
                                <div class="mt-3">
                                    <a href="/UTE_SHOP/user/order/detail?orderId=${order.orderId}" class="btn btn-sm btn-outline-primary">Watch details</a>
                                    ${order.status === 'DELIVERED' ? `<button class="btn-delivered-order btn btn-sm btn-primary" data-order-id='${order.orderId}'>Confirm received</button>` : ''}
                                    ${order.status === 'NEW' || order.status === "CONFIRMED" ? `<button class="btn-remove-order btn btn-sm btn-danger" data-order-id='${order.orderId}'>Cancel order</button>` : ''}
                                    ${order.status === 'RECEIVED' ? `<button class="btn-received-order btn btn-sm btn-danger" data-order-id='${order.orderId}'>Return order</button>` : ''}
                                </div>
                            </div>
                        </div>
                    `;
        container.innerHTML += orderCard;
    });

    // Render pagination
    renderPagination(orderPagination, (newPage) => {
        displayOrders(status, newPage + 1, orderPagination.size, searchKeyword);
    }, 'order-pagination');
    showPageInfo(orderPagination, 'order-page-info');
}

function getStatusText(status) {
    const statusMap = {
        'NEW': 'Waiting for confirmation',
        'CONFIRMED': 'Confirmed',
        'SHIPPING': 'On Shipping',
        'DELIVERED': 'Shipping Completed',
        'RECEIVED': 'Received',
        'CANCELLED': 'Cancelled',
        'REQUEST_RETURN': 'Return Requested',
        'RETURNING': 'Approve Returning',
        'RETURNED': 'Returned'
    };
    return statusMap[status] || status;
}

function reverseStatusMap(text) {
    const statusMap = {
        ['All Orders']: 'all',
        ['Pending']: 'NEW',
        ['Confirmed']: 'CONFIRMED',
        ['On Shipping']: 'SHIPPING',
        ['Shipping Completed']: 'DELIVERED',
        ['Received']: 'RECEIVED',
        ['Cancelled']: 'CANCELLED',
        ['Return Requested']: 'REQUEST_RETURN',
        ['Approve Returning']: 'RETURNING',
        ['Returned']: 'RETURNED'
    };
    return statusMap[text] || text;
}

const tabs = document.getElementById("orderFilterTabs");
tabs.querySelectorAll(".nav-link").forEach(btn => {
    btn.addEventListener('click', function (e) {
        e.preventDefault();
        const status = this.innerText;
        currentOrderStatus = reverseStatusMap(status);

        tabs.querySelectorAll(".nav-link").forEach(b => b.classList.remove('active'));
        this.classList.add('active');

        // Use current search keyword when changing tabs
        displayOrders(currentOrderStatus, 1, 5, currentSearchKeyword);
    });
});

let searchTimeout;
const searchInput = document.getElementById('order-search-input');
const clearSearchBtn = document.getElementById('clear-search-btn');

searchInput?.addEventListener('input', function (e) {
    const keyword = e.target.value;

    // Show/hide clear button
    if (keyword.trim() !== '') {
        clearSearchBtn.style.display = 'block';
    } else {
        clearSearchBtn.style.display = 'none';
    }

    // Debounce search - wait 500ms after user stops typing
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        currentSearchKeyword = keyword;
        displayOrders(currentOrderStatus, 1, 5, currentSearchKeyword);
    }, 500);
});

// Clear search button event listener
clearSearchBtn?.addEventListener('click', function () {
    searchInput.value = '';
    currentSearchKeyword = '';
    clearSearchBtn.style.display = 'none';
    displayOrders(currentOrderStatus, 1, 5, '');
});

// Enter key to search
searchInput?.addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        e.preventDefault();
        clearTimeout(searchTimeout);
        currentSearchKeyword = searchInput.value;
        displayOrders(currentOrderStatus, 1, 5, currentSearchKeyword);
    }
});

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(amount);
}

document.addEventListener('click', async function (e) {
    if (e.target.classList.contains('btn-remove-order')) {
        const orderId = e.target.getAttribute('data-order-id');

        const order = orders.find(o => Number(o.orderId) === Number(orderId));
        if (!order) {
            showErrorToast('Cannot find order information');
            return;
        }
        showOrderStatusModal(orderId, order.status, 'CANCELLED', async () => {
            await loadOrders();
            await displayOrders(currentOrderStatus, 1, 5, currentSearchKeyword);
        });
    }
});

document.addEventListener('click', async function (e) {
    if (e.target.classList.contains('btn-delivered-order')) {
        const orderId = e.target.getAttribute('data-order-id');
        const order = orders.find(o => Number(o.orderId) === Number(orderId));
        if (!order) {
            showErrorToast('Cannot find order information');
            return;
        }
        showOrderStatusModal(orderId, order.status, 'RECEIVED', async () => {
            await loadOrders();
            await displayOrders(currentOrderStatus, 1, 5, currentSearchKeyword);
        });
    }
});

document.addEventListener('click', async function (e) {
    if (e.target.classList.contains('btn-received-order')) {
        const orderId = e.target.getAttribute('data-order-id');
        const order = orders.find(o => Number(o.orderId) === Number(orderId));
        if (!order) {
            showErrorToast('Cannot find order information');
            return;
        }
        showOrderStatusModal(orderId, order.status, 'REQUEST_RETURN', async () => {
            await loadOrders();
            await displayOrders(currentOrderStatus, 1, 5, currentSearchKeyword);
        });
    }
});

document.addEventListener('DOMContentLoaded', async function () {
    if (!AuthState.getUserId()) {
        console.log('Waiting for AuthState to load userId...');
        await AuthState.fetchUserInfo();
    }

    USER_ID = AuthState.getUserId() || 0;
    console.log('USER_ID initialized:', USER_ID);

    loadProvinces();

    document.getElementById('province').addEventListener('change', (e) => {
        loadDistricts(e.target.value);
    });

    // District change event
    document.getElementById('district').addEventListener('change', (e) => {
        loadWards(e.target.value);
    });

    // Initial load of addresses
    await loadAddresses();

    // Initial load of orders
    await loadOrders();
    await displayOrders();

    // Load profile section
    const userInfo = AuthState.getUserInfo();
    loadProfileSection(userInfo);
});

let lovedProducts = [];
let currentLoveSearchKeyword = '';

async function loadLovedProducts() {
    try {
        const result = await favoriteService.getFavoirtiesByUserId(USER_ID)
        if (result.status === "Success") {
            lovedProducts = result.data;
        } else showErrorToast(result.message);
        displayLovedProducts('');
    } catch (error) {
        console.error('Error loading loved products:', error);
        showErrorToast('Cannot load loved products');
    }
}

function paginateLovedProducts(filteredProducts, page, size) {
    const start = (page - 1) * size;
    const end = start + size;
    return {
        content: filteredProducts.slice(start, end),
        pageable: {
            pageNumber: page - 1,
            pageSize: size
        },
        totalPages: Math.ceil(filteredProducts.length / size),
        totalElements: filteredProducts.length
    };
}

function displayLovedProducts(searchKeyword = '', p = 1, size = 3) {
    const container = document.getElementById('loved-products-list');
    const countBadge = document.getElementById('love-count');

    let filteredProducts = lovedProducts;

    // Filter by search keyword
    if (searchKeyword.trim() !== '') {
        const keyword = searchKeyword.toLowerCase();
        filteredProducts = filteredProducts.filter(product =>
            product.name.toLowerCase().includes(keyword)
        );
    }

    let result = paginateLovedProducts(filteredProducts, p, size);
    const lovedProductsPagination = {
        currentPage: result.pageable.pageNumber,
        size: result.pageable.pageSize,
        totalPages: result.totalPages,
        totalElements: result.totalElements
    }

    const productContent = result.content;
    countBadge.textContent = lovedProducts.length.toString();
    container.innerHTML = '';

    if (productContent.length === 0) {
        const message = searchKeyword.trim() !== ''
            ? `No products found with keyword "${searchKeyword}"`
            : 'No loved products yet';
        container.innerHTML = `
            <div class="col-12">
                <div class="text-center py-5">
                    <i class="bi bi-heart" style="font-size: 3rem; color: #ccc;"></i>
                    <p class="text-muted mt-3">${message}</p>
                    ${searchKeyword.trim() === '' ? '<a href="/user/products" class="btn btn-primary mt-2"><i class="bi bi-shop me-2"></i>Start Shopping</a>' : ''}
                </div>
            </div>
        `;
        return;
    }

    function buildUrl(p) {
        if (!p) return '/assets/images/sample/snack.jpg';
        if (/^https?:\/\//i.test(p)) return p; // http / https giữ nguyên
        if (p.startsWith(BASE_URL + contextPath + '/')) return p;
        if (p.startsWith('/')) return BASE_URL + contextPath + p;
        return BASE_URL + contextPath + '/' + p.replace(/^\/+/, '');
    }

    productContent.forEach(product => {
        const discount = product.price ? Math.round((1 - product.price / product.price) * 100) : 0;
        const productCard = `
            <div class="col-md-6 col-lg-4">
                <div class="card h-100 card-product border-0 shadow-sm">
                    <div class="card-body">
                        <div class="text-center position-relative">
                            ${discount > 0 ? `<span class="badge bg-danger position-absolute top-0 start-0 m-2">${discount}% OFF</span>` : ''}
                            <button class="btn btn-sm btn-light position-absolute top-0 end-0 m-2 remove-love-btn" data-product-id="${product.productId}">
                                <i class="bi bi-heart-fill text-danger"></i>
                            </button>
                            <a href="/UTE_SHOP/products/${product.productId}">
                                <img src="${buildUrl(product.imageUrl)}" alt="${product.name}" class="mb-3 img-fluid" style="height: 200px; object-fit: cover;">
                            </a>
                        </div>
                        <div class="text-small mb-1">
                            <div class="text-warning">
                                ${Array(5).fill(0).map((_, i) =>
            `<i class="bi bi-star${i < Math.floor(product.averageRating) ? '-fill' : ''}"></i>`
        ).join('')}
                                <span class="text-muted ms-1">${product.averageRating}</span>
                            </div>
                        </div>
                        <h2 class="fs-6">
                            <a href="/user/product/${product.productId}" class="text-inherit text-decoration-none">${product.name}</a>
                        </h2>
                        <div class="d-flex justify-content-between align-items-center mt-3">
                            <div>
                                <span class="text-dark fw-bold">${formatCurrency(product.price)}</span>
                            </div>
                        </div>
                        <div class="mt-3">
                            <button class="btn btn-primary btn-sm w-100 add-to-cart-btn" data-product-id="${product.productId}">
                                <i class="bi bi-cart-plus me-2"></i>Add to Cart
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        container.innerHTML += productCard;
    });
    renderPagination(lovedProductsPagination, (newPage) => {
        displayLovedProducts(searchKeyword, newPage + 1, lovedProductsPagination.size);
    }, 'loved-product-pagination');
    showPageInfo(lovedProductsPagination, 'loved-product-page-info');
}

const loveSearchInput = document.getElementById('love-search-input');
const clearLoveSearchBtn = document.getElementById('clear-love-search-btn');

loveSearchInput?.addEventListener('input', function (e) {
    const keyword = e.target.value;

    if (keyword.trim() !== '') {
        clearLoveSearchBtn.style.display = 'block';
    } else {
        clearLoveSearchBtn.style.display = 'none';
    }

    currentLoveSearchKeyword = keyword;
    displayLovedProducts(currentLoveSearchKeyword);
});

clearLoveSearchBtn?.addEventListener('click', function () {
    loveSearchInput.value = '';
    currentLoveSearchKeyword = '';
    clearLoveSearchBtn.style.display = 'none';
    displayLovedProducts('');
});

// Remove from loved products
document.addEventListener('click', async function (e) {
    if (e.target.closest('.remove-love-btn')) {
        const btn = e.target.closest('.remove-love-btn');
        const productId = btn.getAttribute('data-product-id');
        const result = await favoriteService.removeFavorite(productId, USER_ID);
        if (result.status !== "Success") {
            showErrorToast(result.message || 'Cannot remove from loved products');
        } else {
            displayLovedProducts(currentLoveSearchKeyword);
            showSuccessToast('Removed from loved products!');
            await loadLovedProducts();
        }
    }
});

document.addEventListener('click', async function (e) {
    if (e.target.closest('.add-to-cart-btn')) {
        const btn = e.target.closest('.add-to-cart-btn');
        const productId = btn.getAttribute('data-product-id');
        try {
            const cart = {
                userId: USER_ID,
                productId: productId,
                quantity: 1
            }
            const result = await cartService.addSelectedCartItem(cart);
            if (result.status === "Success") {
                showSuccessToast('Added to cart successfully!');
            } else {
                showErrorToast(result.message || 'Cannot add to cart');
            }
        } catch (error) {
            console.error('Error adding to cart:', error);
            showErrorToast('Cannot add to cart');
        }
    }
})

function loadSecuritySection() {
    // Load account information
    const userInfo = AuthState.getUserInfo();
    if (userInfo) {
        document.getElementById('security-email').textContent = userInfo.email || 'N/A';

        // Format created date if available
        if (userInfo.createdAt) {
            const date = new Date(userInfo.createdAt);
            document.getElementById('security-created-date').textContent = date.toLocaleDateString('vi-VN');
        } else {
            document.getElementById('security-created-date').textContent = 'N/A';
        }

        if (userInfo.role) {
            document.getElementById('security-role').textContent = userInfo.role;
        } else document.getElementById('security-role').textContent = 'N/A';
    }

    initPasswordToggles();
}

function initPasswordToggles() {
    const toggleButtons = [
        {btnId: 'toggle-current-password', inputId: 'current-password'},
        {btnId: 'toggle-new-password', inputId: 'new-password'},
        {btnId: 'toggle-confirm-password', inputId: 'confirm-password'}
    ];

    toggleButtons.forEach(({btnId, inputId}) => {
        const btn = document.getElementById(btnId);
        const input = document.getElementById(inputId);

        if (btn && input) {
            const newBtn = btn.cloneNode(true);
            btn.parentNode.replaceChild(newBtn, btn);

            newBtn.addEventListener('click', function () {
                input.type = input.type === 'password' ? 'text' : 'password';
                const icon = this.querySelector('i');
                icon.classList.toggle('bi-eye');
                icon.classList.toggle('bi-eye-slash');
            });
        }
    });
}

// Change password form
document.addEventListener('submit', async function (e) {
    if (e.target.id === 'change-password-form') {
        e.preventDefault();

        const currentPassword = document.getElementById('current-password').value;
        const newPassword = document.getElementById('new-password').value;
        const confirmPassword = document.getElementById('confirm-password').value;

        let isValid = true;

        if (!currentPassword) {
            isValid = false;
            document.getElementById('current-password').classList.add('is-invalid');
        } else {
            document.getElementById('current-password').classList.remove('is-invalid');
        }

        if (!newPassword || newPassword.length < 6) {
            isValid = false;
            document.getElementById('new-password').classList.add('is-invalid');
        } else {
            document.getElementById('new-password').classList.remove('is-invalid');
        }

        if (newPassword !== confirmPassword) {
            isValid = false;
            document.getElementById('confirm-password').classList.add('is-invalid');
            showErrorToast('Passwords do not match!');
        } else {
            document.getElementById('confirm-password').classList.remove('is-invalid');
        }

        if (!isValid) return;

        try {
            const password = {
                currentPassword: currentPassword,
                newPassword: newPassword
            }

            const result = await AuthState.updatePassword(USER_ID, password);
            if (result.status === 'Success') {
                showSuccessToast('Password changed successfully!');
                document.getElementById('change-password-form').reset();
                // Collapse the form
                const collapseElement = document.getElementById('changePasswordForm');
                const bsCollapse = bootstrap.Collapse.getInstance(collapseElement);
                if (bsCollapse) {
                    bsCollapse.hide();
                } else {
                    new bootstrap.Collapse(collapseElement, {toggle: true});
                }
            } else {
                showErrorToast(result.message || 'Failed to change password. Please check your current password.');
            }
        } catch (error) {
            console.log(error);
            console.error('Error changing password:', error);
            showErrorToast('An error occurred while changing password: ' + (error.message || 'Please try again later.'));
        }
    }
});

document.getElementById("logout-btn").addEventListener('click', async function (e) {
    e.preventDefault()
    AuthState.logout();
})

// ====== REVIEW: open modal (prefill nếu đã có) ======
document.addEventListener('click', function (e) {
    const btn = e.target.closest('.btn-open-review');
    if (!btn) return;

    const orderId = btn.getAttribute('data-order-id');
    const productId = btn.getAttribute('data-product-id');

    // 1) SET hidden inputs TRƯỚC rồi mới gọi API prefill
    document.getElementById('rv-orderId').value = orderId;
    document.getElementById('rv-productId').value = productId;

    // 2) reset form mặc định
    document.getElementById('rv-comment').value = '';
    document.getElementById('rv-files').value = '';
    document.getElementById('rv-previews').innerHTML = '';
    document.getElementById('rv-rating').value = '5';
    document.querySelectorAll('#rv-stars .star').forEach(st => st.classList.remove('active'));
    document.querySelectorAll('#rv-stars .star').forEach(st => {
        if (Number(st.dataset.val) <= 5) st.classList.add('active');
    });
    // clear hidden reviewId nếu có
    document.getElementById('rv-reviewId')?.remove();

    // 3) Prefill: gọi API lấy review của tôi theo product
    (async () => {
        try {
            const resp = await fetch(`/UTE_SHOP/api/shop/reviews/mine?productId=${productId}`);
            if (resp.ok) {
                const data = await resp.json();
                if (data) {
                    // -> đang EDIT
                    const hidden = document.createElement('input');
                    hidden.type = 'hidden';
                    hidden.id = 'rv-reviewId';
                    hidden.value = data.reviewId;
                    document.getElementById('reviewForm').appendChild(hidden);

                    // set rating/comment
                    document.getElementById('rv-rating').value = String(data.rating || 5);
                    document.querySelectorAll('#rv-stars .star').forEach(s => {
                        s.classList.toggle('active', Number(s.dataset.val) <= (data.rating || 5));
                    });
                    document.getElementById('rv-comment').value = data.comment || '';

                    // preview media cũ
                    const prev = document.getElementById('rv-previews');
                    prev.innerHTML = '';
                    (data.media || []).forEach(m => {
                        const isVideo = (m.type || '').toLowerCase() === 'video';
                        const node = document.createElement(isVideo ? 'video' : 'img');
                        if (isVideo) node.controls = true;
                        node.src = m.url;
                        node.style.width = '100px';
                        node.style.height = '100px';
                        node.style.objectFit = 'cover';
                        node.style.borderRadius = '.5rem';
                        prev.appendChild(node);
                    });
                }
            }
        } catch (_) {
        }
    })();

    // 4) Mở modal
    const modal = new bootstrap.Modal(document.getElementById('reviewModal'));
    modal.show();
});


// ====== REVIEW: select stars ======
document.querySelectorAll('#rv-stars .star').forEach(star => {
    star.addEventListener('click', function () {
        const val = Number(this.dataset.val);
        document.getElementById('rv-rating').value = String(val);
        document.querySelectorAll('#rv-stars .star').forEach(s => {
            s.classList.toggle('active', Number(s.dataset.val) <= val);
        });
    });
});

// ====== REVIEW: preview files ======
document.getElementById('rv-files').addEventListener('change', function () {
    const container = document.getElementById('rv-previews');
    container.innerHTML = '';
    const files = Array.from(this.files || []);
    files.slice(0, 10).forEach(f => {
        const url = URL.createObjectURL(f);
        let node;
        if (f.type.startsWith('video')) {
            node = document.createElement('video');
            node.controls = true;
            node.src = url;
        } else {
            node = document.createElement('img');
            node.src = url;
            node.alt = f.name;
        }
        container.appendChild(node);
    });
});

// ====== REVIEW: submit (upsert) ======
document.getElementById('rv-submit').addEventListener('click', async function () {
    try {
        const orderId = document.getElementById('rv-orderId').value;
        const productId = document.getElementById('rv-productId').value;
        const rating = document.getElementById('rv-rating').value;
        const comment = document.getElementById('rv-comment').value;
        const filesInput = document.getElementById('rv-files');

        const reviewIdEl = document.getElementById('rv-reviewId');
        const isEdit = !!reviewIdEl;

        const fd = new FormData();
        if (!isEdit) {
            // lần đầu cần orderId + productId để validate
            fd.append('orderId', orderId);
            fd.append('productId', productId);
        }
        fd.append('rating', rating);
        if (comment) fd.append('comment', comment);

        const files = Array.from(filesInput.files || []).slice(0, 10);
        for (const f of files) fd.append('files', f, f.name);

        const endpoint = isEdit
            ? `/UTE_SHOP/api/shop/reviews/${reviewIdEl.value}`
            : `/UTE_SHOP/api/shop/reviews`;
        const method = isEdit ? 'PUT' : 'POST';

        const resp = await fetch(endpoint, {method, body: fd});
        if (!resp.ok) {
            const data = await resp.json().catch(() => ({}));
            throw new Error(data.message || 'Upload review failed');
        }

        showSuccessToast(isEdit ? 'Updated rating successfully!' : 'Rating successfully!');
        bootstrap.Modal.getInstance(document.getElementById('reviewModal'))?.hide();

        // Reload đơn (size mặc định 5 như displayOrders đang dùng)
        await loadOrders();
        await displayOrders(currentOrderStatus, 1, 5, currentSearchKeyword);
    } catch (err) {
        showErrorToast(err.message || 'Have error');
    }
});



