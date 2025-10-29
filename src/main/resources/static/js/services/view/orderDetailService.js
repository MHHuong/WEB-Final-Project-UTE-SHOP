import {showErrorToast, showSuccessToast} from "../../utils/toastUtils.js";
import orderService from "../../api/orderService.js";
import {showOrderStatusModal} from "../../utils/orderStatusModal.js";
import { AuthState } from "../../auth.js";


const getUserId = () => AuthState.getUserId() || 1;
let userId = getUserId();
// Get order ID from URL
const urlParams = new URLSearchParams(window.location.search);
const orderId = urlParams.get('orderId');


// Order status mapping
const STATUS_ORDER = ['NEW', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'RECEIVED'];
const STATUS_TEXT = {
    'NEW': 'Đơn Hàng Mới',
    'CONFIRMED': 'Đã Xác Nhận',
    'SHIPPING': 'Đang Giao Hàng',
    'DELIVERED': 'Đã Giao Hàng',
    'RECEIVED': 'Đã Nhận Hàng',
    'CANCELLED': 'Đã Hủy',
    'RETURNED': 'Đã Trả Hàng'
};


let stompClient = null;
function connect() {
    const userId = getUserId();
    console.log(userId)
    if (!userId) { alert('Enter userId first'); return; }
    const sock = new SockJS('/ws?userId=' + encodeURIComponent(userId));
    stompClient = Stomp.over(sock);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame, 'ok');
        stompClient.subscribe('/user/queue/orders', function (message) {
            try {
                const body = JSON.parse(message.body);
                    updateOrderTimeline(body.status);
                    if (Number(body.orderId) === Number(orderId) && Number(body.userId) === Number(userId)) {
                        showSuccessToast(`Đơn hàng #${body.orderId} đã được cập nhật trạng thái: ${STATUS_TEXT[body.status] || body.status}`);
                    }
            } catch (e) {
                console.log('Received (raw): ' + message.body, 'ok');
            }
        });
    }, function(error) {
        console.log('STOMP error: ' + error, 'err');
    });
}


// Load order details
async function loadOrderDetails() {
    if (!orderId) {
        return
    }
    document.getElementById('order-timeline-container').style.display = 'block';
    try {
        const result = await orderService.getOrderById(orderId);
        console.log('Order details:', result);

        if (result.status !== 'Success' || !result.data) {
            showErrorToast('Không thể tải thông tin đơn hàng');
            return;
        }

        const order = result.data;
        // Display order code
        document.getElementById('order-code').textContent = `#${order.orderId}`;

        // Update timeline - chỉ cần status
        updateOrderTimeline(order.status);

        // Display order items
        displayOrderItems(order.orderItem || []);

        let discount = 0;
        discount += order.orderItem.reduce((sum, item) => sum + (item.discountAmount || 0), 0);
        if (order.couponValue > 100) {
            discount += order.couponValue;
        }
        else if (order.couponValue > 0) discount += order.totalAmount * (1 / (1 - (order.couponValue) / 100)  - 1);
        // Display order summary
        displayOrderSummary(order, discount);

        // Display shipping info
        displayShippingInfo(order);

        // Display payment info
        displayPaymentInfo(order);

        // Display action buttons based on status
        displayActionButtons(order);

        const infoList = document.getElementById('info-list');
        infoList.innerHTML = `
                      <li>Bạn có thể theo dõi trạng thái đơn hàng trên trang này.</li>
                      <li>Vui lòng kiểm tra thông tin đơn hàng và liên hệ với chúng tôi nếu có bất kỳ thắc mắc nào.</li>
                      <li>Đơn hàng sẽ được giao trong thời gian dự kiến theo phương thức vận chuyển bạn đã chọn.</li>
                `;

    } catch (error) {
        console.error('Error loading order details:', error);
        showErrorToast('Đã xảy ra lỗi khi tải thông tin đơn hàng');
    }
}

// Update order timeline based on current status only
function updateOrderTimeline(currentStatus) {
    const timelineItems = document.querySelectorAll('.timeline-item-horizontal');
    const timelineLines = document.querySelectorAll('.timeline-line-horizontal');
    const currentStatusText = document.getElementById('current-status-text');

    // Update current status text
    currentStatusText.textContent = STATUS_TEXT[currentStatus] || currentStatus;

    // Handle CANCELLED status
    if (currentStatus === 'CANCELLED') {
        // Hide all normal timeline items and lines
        timelineItems.forEach(item => {
            if (!item.classList.contains('timeline-item-cancelled')) {
                item.style.display = 'none';
            }
        });
        timelineLines.forEach(line => line.style.display = 'none');

        // Show only cancelled item
        const cancelledItem = document.querySelector('.timeline-item-cancelled');
        if (cancelledItem) {
            cancelledItem.style.display = 'flex';
            cancelledItem.classList.add('active');
        }
        return;
    }

    // Handle RETURNED status
    if (currentStatus === 'RETURNED') {
        const normalStatuses = ['NEW', 'CONFIRMED', 'SHIPPING', 'DELIVERED'];
        normalStatuses.forEach(status => {
            const item = document.querySelector(`.timeline-item-horizontal[data-status="${status}"]`);
            if (item) {
                item.style.display = 'flex';
                item.classList.add('completed');
            }
        });

        timelineLines.forEach(line => {
            line.style.display = 'block';
            line.classList.add('completed');
        });

        const returnedItem = document.querySelector('.timeline-item-returned');
        if (returnedItem) {
            returnedItem.style.display = 'flex';
            returnedItem.classList.add('active');
        }
        return;
    }

    // Normal flow: Update timeline for standard order statuses
    const currentIndex = STATUS_ORDER.indexOf(currentStatus);

    timelineItems.forEach((item) => {
        const itemStatus = item.getAttribute('data-status');

        if (itemStatus === 'CANCELLED' || itemStatus === 'RETURNED') {
            return;
        }

        const itemIndex = STATUS_ORDER.indexOf(itemStatus);

        // Show the item
        item.style.display = 'flex';

        if (itemIndex < currentIndex) {
            // Completed status
            item.classList.add('completed');
            item.classList.remove('active');
        } else if (itemIndex === currentIndex) {
            // Current active status
            item.classList.add('active');
            item.classList.remove('completed');
        } else {
            // Future status
            item.classList.remove('completed', 'active');
        }
    });

    // Update timeline lines
    timelineLines.forEach((line, index) => {
        line.style.display = 'block';

        // Line is completed if both items around it are completed or active
        if (index < currentIndex) {
            line.classList.add('completed');
        } else {
            line.classList.remove('completed');
        }
    });
}


// Display order items
function displayOrderItems(items) {
    const container = document.getElementById('order-items-container');

    if (!items || items.length === 0) {
        container.innerHTML = '<p class="text-center text-muted">Không có sản phẩm nào</p>';
        return;
    }

    container.innerHTML = items.map(item => `
        <div class="order-item">
            <div class="d-flex gap-3">
                <img src="${item.image || '/images/products/default.jpg'}" 
                     alt="${item.productName}" 
                     class="product-img-success">
                <div class="flex-grow-1">
                    <h6 class="mb-1">${item.productName}</h6>
                    <p class="text-muted mb-1 small">Số lượng: ${item.quantity}</p>
                    <p class="mb-0">
                        <span class="text-primary fw-bold">${formatCurrency(item.unitPrice)}</span>
                    </p>
                </div>
                <div class="text-end">
                    <strong class="text-primary">${formatCurrency(item.unitPrice * item.quantity)}</strong>
                </div>
            </div>
        </div>
    `).join('');
}

// Display order summary
function displayOrderSummary(order, discountValue) {
    const subtotal = order.orderItem?.reduce((sum, item) => sum + (item.unitPrice * item.quantity) - item.discountAmount,0) || 0;
    const discount = discountValue || 0;
    const total = order.totalAmount || 0;
    const shipping = total - subtotal + (discountValue || 0);

    document.getElementById('subtotal-amount').textContent = formatCurrency(subtotal);
    document.getElementById('shipping-amount').textContent = formatCurrency(shipping);
    document.getElementById('discount-amount').textContent = `-${formatCurrency(discount)}`;
    document.getElementById('total-amount').textContent = formatCurrency(total);
}

// Display shipping info
function displayShippingInfo(order) {
    document.getElementById('receiver-name').textContent = order.address.receiverName || 'N/A';
    document.getElementById('receiver-phone').textContent = order.address.phone || 'N/A';

    const address = [
        order.address.addressDetail,
        order.address.ward,
        order.address.district,
        order.address.province
    ].filter(Boolean).join(', ');

    document.getElementById('shipping-address').textContent = address || 'N/A';
    document.getElementById('shipping-method').textContent = shippingMethod(order.estimatedDeliveryDays)
}

function shippingMethod(estimatedDeliveryTime) {
    if (estimatedDeliveryTime < 1) {
        return 'Giao hàng trong ngày';
    }
    else if (estimatedDeliveryTime < 3) {
        return 'Giao hàng nhanh';
    }
    else {
        return 'Giao hàng tiêu chuẩn';
    }
}

// Display payment info
function displayPaymentInfo(order) {
    const paymentMethod = order.paymentMethod === 'COD' ? 'Thanh toán khi nhận hàng' :
        (order.paymentMethod === 'MOMO' || order.paymentMethod === 'VNPAY') ? `Thanh toán online (${order.paymentMethod})` :
                         order.paymentMethod || 'N/A';

    document.getElementById('payment-method').textContent = paymentMethod;

    // Payment status
    const paymentStatusEl = document.getElementById('payment-status');
    if (order.paymentStatus === 'SUCCESS') {
        paymentStatusEl.textContent = 'Đã thanh toán';
        paymentStatusEl.className = 'badge bg-success';
    } else if (order.paymentStatus === 'PENDING') {
        paymentStatusEl.textContent = 'Chờ thanh toán';
        paymentStatusEl.className = 'badge bg-warning';
    } else {
        paymentStatusEl.textContent = 'Chưa thanh toán';
        paymentStatusEl.className = 'badge bg-secondary';
    }

    // Order time
    document.getElementById('order-time').textContent = formatDateTime(order.createdAt);
}


function displayActionButtons(order) {
    const container = document.getElementById('order-actions');

    let buttonsHTML = `
        <a href="/user/profile" class="btn btn-outline-primary btn-lg px-5">
            <i class="bi bi-list-ul me-2"></i>
            Xem Đơn Hàng Của Tôi
        </a>
        <a href="/user" class="btn btn-outline-secondary btn-lg px-5">
            <i class="bi bi-house me-2"></i>
            Tiếp Tục Mua Sắm
        </a>
    `;

    if (order.status === 'NEW' || order.status === 'CONFIRMED') {
        buttonsHTML = `
            <button class="btn btn-cancel-order btn-lg px-5" id="cancel-order-btn">
                <i class="bi bi-x-circle me-2"></i>
                Hủy Đơn Hàng
            </button>
        ` + buttonsHTML;
    }

    if (order.status === 'DELIVERED') {
        buttonsHTML = `
            <button class="btn btn-confirm-received btn-lg px-5" id="confirm-received-btn">
                <i class="bi bi-check-circle me-2"></i>
                Xác Nhận Đã Nhận Hàng
            </button>
        ` + buttonsHTML;
    }

    container.innerHTML = buttonsHTML;

    // Add event listeners
    const cancelBtn = document.getElementById('cancel-order-btn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', (e) => handleCancelOrder(order));
    }

    const confirmBtn = document.getElementById('confirm-received-btn');
    if (confirmBtn) {
        confirmBtn.addEventListener('click', () => handleConfirmReceived(order));
    }
}

// Handle cancel order
async function handleCancelOrder(order) {
    if (!order) {
        showErrorToast('Không tìm thấy thông tin đơn hàng');
        return;
    }
    showOrderStatusModal(order.orderId, order.status, 'CANCELLED', async () => {
        await loadOrderDetails()
    });
}

// Handle confirm received
async function handleConfirmReceived(order) {
    if (!order) {
        showErrorToast('Không tìm thấy thông tin đơn hàng');
        return;
    }
    showOrderStatusModal(order.orderId, order.status, 'RECEIVED', async () => {
        await loadOrderDetails()
    });
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount || 0);
}

// Format date time
function formatDateTime(dateString) {
    if (!dateString) return 'N/A';

    const date = new Date(dateString);
    return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    loadOrderDetails();
    if (orderId) {
        connect()
    }
});
