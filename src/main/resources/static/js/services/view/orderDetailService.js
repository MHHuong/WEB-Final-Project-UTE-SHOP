import {showErrorToast, showSuccessToast} from "../../utils/toastUtils.js";
import orderService from "../../services/api/orderService.js";
import {showOrderStatusModal} from "../../utils/orderStatusModal.js";
import { AuthState } from "../../auth.js";

let userId = localStorage.getItem("userId") || 0;

// Get order ID from URL
const urlParams = new URLSearchParams(window.location.search);
const orderId = urlParams.get('orderId');


// Order status mapping
const STATUS_ORDER = ['NEW', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'RECEIVED','CANCELLED', 'REQUEST_RETURN', 'RETURNING', 'RETURNED'];
const STATUS_TEXT = {
    'NEW': 'New Order',
    'CONFIRMED': 'Confirmed',
    'SHIPPING': 'On Shipping',
    'DELIVERED': 'Delivered',
    'RECEIVED': 'Received',
    'CANCELLED': 'Cancelled',
    'REQUEST_RETURN' : 'Return Requested',
    'RETURNING' : 'Returning',
    'RETURNED' : 'Returned'
};



let stompClient = null;
function connect() {
    let token = localStorage.getItem("authToken");
    if (!token) {
        showErrorToast("Please log in to continue");
        window.location.href = '/UTE_SHOP/login';
        return;
    }
    const socket = new SockJS("http://localhost:8082/UTE_SHOP/ws?token=" + token);
    stompClient = Stomp.over(socket);
    stompClient.connect(
        {},
        function(frame) {
            stompClient.subscribe('/user/queue/orders', function(message) {
                try {
                    const body = JSON.parse(message.body);
                    if (Number(body.orderId) === Number(orderId) && Number(body.userId) === Number(userId)) {
                        updateOrderTimeline(body.status);
                        showSuccessToast(`Order #${body.orderId} status updated to ${body.status}`);
                    }
                } catch (e) {
                    console.log('Parse error:', e);
                    alert('📦 Message received (raw):\n' + message.body);
                }
            });
            if (Notification.permission === "default") {
                Notification.requestPermission();
            }
        },
        function(error) {
            document.getElementById('connectBtn').disabled = false;
            document.getElementById('disconnectBtn').disabled = true;
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
            showErrorToast('Cannot load order details');
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
                    <li>You can check your order status on this page.</li>
                    <li>Please review your order details and reach out to us if you have any questions.</li>
                    <li>Your order will be delivered within the estimated timeframe based on your chosen shipping method.</li>
                `;

    } catch (error) {
        console.error('Error loading order details:', error);
        showErrorToast('Error loading order details');
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

function buildUrl(p) {
    if (!p) return '/assets/images/sample/snack.jpg';
    if (/^https?:\/\//i.test(p)) return p; // http / https giữ nguyên
    if (p.startsWith(BASE_URL + contextPath + '/')) return p;
    if (p.startsWith('/')) return BASE_URL + contextPath + p;
    return BASE_URL + contextPath + '/' + p.replace(/^\/+/, '');
}

// Display order items
function displayOrderItems(items) {
    const container = document.getElementById('order-items-container');

    if (!items || items.length === 0) {
        container.innerHTML = '<p class="text-center text-muted">No products found</p>';
        return;
    }
    container.innerHTML = items.map(item => `
        <div class="order-item">
            <div class="d-flex gap-3">
                <img src="${buildUrl(item.image)}" 
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
        return 'Delivery within the day';
    }
    else if (estimatedDeliveryTime < 3) {
        return 'Fast delivery';
    }
    else {
        return 'Standard delivery';
    }
}

// Display payment info
function displayPaymentInfo(order) {
    const paymentMethod = order.paymentMethod === 'COD' ? 'Pay on Delivery' :
        (order.paymentMethod === 'MOMO' || order.paymentMethod === 'VNPAY') ? `Online Payment (${order.paymentMethod})` :
                         order.paymentMethod || 'N/A';
    document.getElementById('payment-method').textContent = paymentMethod;

    // Payment status
    const paymentStatusEl = document.getElementById('payment-status');
    if (order.paymentStatus === 'SUCCESS') {
        paymentStatusEl.textContent = 'Paid';
        paymentStatusEl.className = 'badge bg-success';
    } else if (order.paymentStatus === 'PENDING') {
        paymentStatusEl.textContent = 'Waiting for Payment';
        paymentStatusEl.className = 'badge bg-warning';
    } else {
        paymentStatusEl.textContent = 'Haven\'t Paid Yet';
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
            Watch Order History
        </a>
        <a href="/user" class="btn btn-outline-secondary btn-lg px-5">
            <i class="bi bi-house me-2"></i>
             Continue Shopping
        </a>
    `;

    if (order.status === 'NEW' || order.status === 'CONFIRMED') {
        buttonsHTML = `
            <button class="btn btn-cancel-order btn-lg px-5" id="cancel-order-btn">
                <i class="bi bi-x-circle me-2"></i>
                Cancel Order
            </button>
        ` + buttonsHTML;
    }

    if (order.status === 'DELIVERED') {
        buttonsHTML = `
            <button class="btn btn-confirm-received btn-lg px-5" id="confirm-received-btn">
                <i class="bi bi-check-circle me-2"></i>
                Confirm Received
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
        showErrorToast('Cannot find order information');
        return;
    }
    showOrderStatusModal(order.orderId, order.status, 'CANCELLED', async () => {
        await loadOrderDetails()
    });
}

// Handle confirm received
async function handleConfirmReceived(order) {
    if (!order) {
        showErrorToast('Cannot find order information');
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
