import {showErrorToast, showSuccessToast} from "/js/utils/toastUtils.js";
import addressService from "/js/services/addressService.js";
import orderService from "/js/services/orderService.js";
import {renderPagination, showPageInfo} from "/js/utils/paginationUtils.js";
import {loadProvinces, loadDistricts, loadWards} from "/js/utils/locationUtils.js";
import {showOrderStatusModal} from "/js/utils/orderStatusModal.js";

const USER_ID = 1;

let isEditingAddress = false;
let editingAddressId = null;
let orders = [];
let currentOrderStatus = 'all'; // Track current filter status
let currentSearchKeyword = ''; // Track current search keyword

// Pagination state for addresses
let addressPagination = {
    currentPage: 0,
    size: 4,
    totalPages: 0,
    totalElements: 0
};

// Section Navigation
document.querySelectorAll('.nav-link[data-section]').forEach(link => {
    link.addEventListener('click', function(e) {
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
        }
    });
});


// Load Addresses with pagination
async function loadAddresses(page = 0, size = 4) {
    try {
        let addresses = [];
        const result = await addressService.getAddressesPaginationByUserId(USER_ID, size, page);
        if (result.status === "true") {
            addresses = result.data.content;
            addressPagination = {
                currentPage: result.data.pageable.pageNumber,
                size: result.data.pageable.pageSize,
                totalPages: result.data.totalPages,
                totalElements: result.data.totalElements
            };
        } else {
            addresses = []
            showErrorToast('Không thể tải danh sách địa chỉ');
        }
        const container = document.getElementById('addresses-list');
        container.innerHTML = '';

        if (addresses.length === 0) {
            container.innerHTML = `
                <div class="col-12">
                    <div class="text-center py-5">
                        <i class="bi bi-inbox" style="font-size: 3rem; color: #ccc;"></i>
                        <p class="text-muted mt-3">Chưa có địa chỉ nào</p>
                    </div>
                </div>
            `;
        } else {
            addresses.map(addr => {
                const addressCard = `
                            <div class="col-md-6">
                                <div class="card ${addr.isDefault ? 'border-primary' : ''}">
                                    <div class="card-body">
                                        ${addr.isDefault ? '<span class="badge bg-primary mb-2">Mặc định</span>' : ''}
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
                                            >Sửa</button>
                                            <button class="remove-address-btn btn btn-sm btn-outline-danger"  th:data-id="${addr.addressId}">Xóa</button>
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
        showErrorToast('Không thể tải danh sách địa chỉ');
    }
}

document.addEventListener("click", async function(e) {
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

document.addEventListener("click", async function(e) {
    if (e.target.classList.contains("remove-address-btn")) {
        const btn = e.target.closest(".remove-address-btn");
        if (!btn) return;

        const id = e.target.getAttribute("th:data-id");
        try {
            const result = await addressService.removeAddress(id, USER_ID);
            if (result.status === "true") {
                showSuccessToast('Xóa địa chỉ thành công!');
                await loadAddresses();
            } else {
                showErrorToast(result.message || 'Xóa địa chỉ thất bại');
            }
        } catch (error) {
            console.error('Error deleting address:', error);
            showErrorToast('Không thể xóa địa chỉ');
        }
    }
});



// Add Address
document.getElementById('save-address-btn').addEventListener('click', async function(e) {
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

        if (result.status === "true") {
            showSuccessToast(isEditingAddress ? 'Cập nhật địa chỉ thành công!' : 'Thêm địa chỉ thành công!');
            await loadAddresses();
            await resetAddressForm();
        } else {
            showErrorToast(result.message || (isEditingAddress ? 'Cập nhật địa chỉ thất bại' : 'Thêm địa chỉ thất bại'));
        }
    } catch (error) {
        console.error('Error saving address:', error);
        showErrorToast('Không thể lưu địa chỉ');
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
            showErrorToast('Không thể tải danh sách đơn hàng');
        } else orders = result.data;
    } catch (error) {
        console.error('Error loading orders:', error);
        showErrorToast('Không thể tải danh sách đơn hàng');
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
    // Filter by status first
    if (status !== 'all') {
        filteredOrders = filteredOrders.filter(order => order.status === status);
    }
    console.log(filteredOrders);

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
                ? `Không tìm thấy đơn hàng nào với từ khóa "${searchKeyword}"`
                : 'Chưa có đơn hàng nào';
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
                                    <strong>Mã đơn hàng: ${highlightText(order.orderId.toString(), searchKeyword)}</strong>
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
                                `).join('')}
                                <hr>
                                <div class="d-flex justify-content-between align-items-center">
                                    <h6 class="mb-0">Tổng cộng:</h6>
                                    <h5 class="mb-0 text-primary">${formatCurrency(order.totalAmount)}</h5>
                                </div>
                                <div class="mt-3">
                                    <a href="/user/order/detail?orderId=${order.orderId}" class="btn btn-sm btn-outline-primary">Xem chi tiết</a>
                                    ${order.status === 'DELIVERED' ? `<button class="btn-delivered-order btn btn-sm btn-primary" data-order-id='${order.orderId}'>Xác nhận đơn hàng</button>` : ''}
                                    ${order.status === 'NEW' ? `<button class="btn-remove-order btn btn-sm btn-danger" data-order-id='${order.orderId}'>Hủy đơn</button>` : ''}
                                     ${order.status === 'RECEIVED' ? `<button class="btn-received-order btn btn-sm btn-danger" data-order-id='${order.orderId}'>Trả đơn hàng</button>` : ''}
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
        'NEW': 'Chờ xác nhận',
        'CONFIRMED': 'Đã xác nhận',
        'SHIPPING': 'Đang giao',
        'DELIVERED': 'Đã giao hàng',
        'RECEIVED': 'Đã nhận hàng',
        'CANCELLED': 'Đã hủy',
        'RETURNED': 'Đã trả hàng'
    };
    return statusMap[status] || status;
}

function reverseStatusMap(text) {
    const statusMap = {
        ['Tất cả']: 'all',
        ['Chờ xác nhận']: 'NEW',
        ['Đã xác nhận']: 'CONFIRMED',
        ['Đang giao']: 'SHIPPING',
        ['Đã giao hàng']: 'DELIVERED',
        ['Đã nhận hàng']: 'RECEIVED',
        ['Đã hủy']: 'CANCELLED',
        ['Đã trả hàng']: 'RETURNED'
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

searchInput?.addEventListener('input', function(e) {
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
clearSearchBtn?.addEventListener('click', function() {
    searchInput.value = '';
    currentSearchKeyword = '';
    clearSearchBtn.style.display = 'none';
    displayOrders(currentOrderStatus, 1, 5, '');
});

// Enter key to search
searchInput?.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        e.preventDefault();
        clearTimeout(searchTimeout);
        currentSearchKeyword = searchInput.value;
        displayOrders(currentOrderStatus, 1, 5, currentSearchKeyword);
    }
});

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

document.addEventListener('click', async function(e) {
    if (e.target.classList.contains('btn-remove-order')) {
        const orderId = e.target.getAttribute('data-order-id');

        const order = orders.find(o => Number(o.orderId) === Number(orderId));
        if (!order) {
            showErrorToast('Không tìm thấy thông tin đơn hàng');
            return;
        }
        showOrderStatusModal(orderId, order.status, 'CANCELLED', async () => {
            await loadOrders();
            await displayOrders(currentOrderStatus, 1, 5, currentSearchKeyword);
        });
    }
});

document.addEventListener('click', async function(e) {
    if (e.target.classList.contains('btn-delivered-order')) {
        const orderId = e.target.getAttribute('data-order-id');
        const order = orders.find(o => Number(o.orderId) === Number(orderId));
        if (!order) {
            showErrorToast('Không tìm thấy thông tin đơn hàng');
            return;
        }
        showOrderStatusModal(orderId, order.status, 'RECEIVED', async () => {
            await loadOrders();
            await displayOrders(currentOrderStatus, 1, 5, currentSearchKeyword);
        });
    }
});

document.addEventListener('click', async function(e) {
    if (e.target.classList.contains('btn-received-order')) {
        const orderId = e.target.getAttribute('data-order-id');
        const order = orders.find(o => Number(o.orderId) === Number(orderId));
        if (!order) {
            showErrorToast('Không tìm thấy thông tin đơn hàng');
            return;
        }
        showOrderStatusModal(orderId, order.status, 'RETURNED', async () => {
            await loadOrders();
            await displayOrders(currentOrderStatus, 1, 5, currentSearchKeyword);
        });
    }
});

document.addEventListener('DOMContentLoaded', async function() {
    // Load provinces for address form
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
});
