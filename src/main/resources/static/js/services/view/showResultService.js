import {showErrorToast, showSuccessToast, showWarningToast, showInfoToast} from "/js/utils/toastUtils.js";
import orderService from "/js/services/api/orderService.js";
import {urlParams} from "/js/utils/apiClient.js";
import paymentService from "/js/services/api/paymentService.js";


let paymentData = {};
let paymentMethod = '';
// Format currency
function formatCurrency(amount) {
    if (isNaN(amount)) return "0₫";
    return amount.toLocaleString('vi-VN') + '₫';
}

// Get shipping method text
function getShippingMethodText(method) {
    const methods = {
        'STANDARD': 'Giao hàng tiêu chuẩn (3-5 ngày)',
        'FAST': 'Giao hàng nhanh (1-2 ngày)',
        'EXPRESS': 'Giao hàng hỏa tốc (trong 24h)'
    };
    return methods[method] || method;
}

// Get payment method text
function getPaymentMethodText(method) {
    const methods = {
        'COD': 'Thanh toán khi nhận hàng (COD)',
        'E_WALLET': 'Ví điện tử',
        'MOMO': 'Ví MoMo',
        'VNPAY': 'VNPay'
    };
    return methods[method] || method;
}

async function loadOrderData() {
    const status = urlParams.get('status'); // success, pending, failed
    if (!status) {
        return;
    }
    const result = await orderService.getDisplayTempOrder();
    if (result.status === "Success") {
        try {
            document.getElementById('order-code').textContent = result.orderCode || 'N/A';
            displayOrderData(result.data);
            paymentData = {
                orderId: result.data.orderCode,
                amount: result.data.payment.total || 0,
            }
            paymentMethod = result.data.payment.paymentMethod
            if (status) {
                setPaymentState(status);
            } else if (result.paymentMethod === 'COD') {
                setPaymentState(PAYMENT_STATE.SUCCESS);
            } else {
                // E-wallet payment - check if already paid
                setPaymentState(PAYMENT_STATE.PENDING);
            }
        } catch (error) {
            console.error('Error parsing order data:', error);
            showErrorToast('Không thể tải thông tin đơn hàng');
        }
    } else {
        document.getElementById('order-code').textContent = `#${result.orderCode}` || 'N/A';
        populateDefaultData();
        if (status) {
            setPaymentState(status);
        } else {
            setPaymentState(PAYMENT_STATE.SUCCESS);
        }
    }
}

// Display order data
function displayOrderData(orderData) {
    // Order code
    if (!orderData) {
        return;
    }
    if (orderData.orderCode) {
        document.getElementById('order-code').textContent = `#${orderData.orderCode}`;
    }

    if (orderData.orders && orderData.orders.length > 0) {
        renderOrderItems(orderData.orders);
    }

    // Amounts
    const subtotal = orderData.payment.subtotal || 0;
    const shippingFee = orderData.payment.shippingFee || 0;
    const discount = orderData.payment.discount || 0;
    const total = orderData.payment.total || 0;

    document.getElementById('subtotal-amount').textContent = formatCurrency(subtotal);
    document.getElementById('shipping-amount').textContent = formatCurrency(shippingFee);
    document.getElementById('discount-amount').textContent = `-${formatCurrency(discount)}`;
    document.getElementById('total-amount').textContent = formatCurrency(total);

    // Shipping info
    document.getElementById('receiver-name').textContent = orderData.address.receiverName || 'N/A';
    document.getElementById('receiver-phone').textContent = orderData.address.phone || 'N/A';

    document.getElementById('shipping-address').textContent = orderData.address.addressDetail
        ? `${orderData.address.addressDetail}, ${orderData.address.ward || ''}, ${orderData.address.district || ''}, ${orderData.address.province || ''}`
        : 'N/A';
    document.getElementById('shipping-method').textContent = getShippingMethodText(orderData.shippingMethod || 'STANDARD');

    document.getElementById('payment-method').textContent = getPaymentMethodText(orderData.payment.paymentMethod || 'COD');
    const paymentStatus = orderData.payment.paymentMethod === 'COD' ? 'Chưa thanh toán' : 'Đã thanh toán';
    const statusClass = orderData.payment.paymentMethod === 'COD' ? 'bg-warning' : 'bg-success';
    document.getElementById('payment-status').textContent = paymentStatus;
    document.getElementById('payment-status').className = `badge ${statusClass}`;

    // Order time
    document.getElementById('order-time').textContent = orderData.orderTime || new Date().toLocaleString('vi-VN');
}

// Render order items
function renderOrderItems(items) {
    const container = document.getElementById('order-items-container');
    let html = '';

    items.forEach(item => {
        html += `
                <div class="order-item">
                    <div class="d-flex align-items-start gap-3">
                        <img src="${item.image || '/images/products/default.jpg'}"
                             class="product-img-success"
                             alt="${item.productName || 'Product'}">
                        <div class="flex-grow-1">
                            <h6 class="mb-1">${item.productName || 'N/A'}</h6>
                            <div class="d-flex justify-content-between align-items-center">
                                <small class="text-muted">Số lượng: ${item.quantity || 1}</small>
                                <div class="text-end">
                                    <div class="text-muted small">${formatCurrency(item.price || 0)}</div>
                                    <div class="fw-bold text-primary">${formatCurrency((item.price || 0) * (item.quantity || 1))}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
    });

    container.innerHTML = html;
}

// Populate default data if no order data available
function populateDefaultData() {
    document.getElementById('order-items-container').innerHTML = `
            <div class="text-center py-3 text-muted">
                <i class="bi bi-inbox fs-1"></i>
                <p class="mt-2">Không có thông tin sản phẩm</p>
            </div>
        `;

    document.getElementById('receiver-name').textContent = 'N/A';
    document.getElementById('receiver-phone').textContent = 'N/A';
    document.getElementById('shipping-address').textContent = 'N/A';
    document.getElementById('shipping-method').textContent = 'N/A';
    document.getElementById('payment-method').textContent = 'N/A';
    document.getElementById('order-time').textContent = new Date().toLocaleString('vi-VN');
}

const PAYMENT_STATE = {
    PENDING: 'pending',
    SUCCESS: 'success',
    FAILED: 'failed'
};

let currentState = PAYMENT_STATE.SUCCESS;
let countdownTimer = null;
let remainingSeconds = 60;

function setPaymentState(state) {
    currentState = state;

    // Hide all states
    document.getElementById('success-state').style.display = 'none';
    document.getElementById('pending-state').style.display = 'none';
    document.getElementById('failed-state').style.display = 'none';
    document.getElementById('order-timeline-container').style.display = 'none';

    // Show appropriate state
    switch(state) {
        case PAYMENT_STATE.SUCCESS:
            document.getElementById('success-state').style.display = 'block';
            document.getElementById('action-buttons-card').style.display = 'block';
            updatePaymentStatus('Đã thanh toán', 'bg-success');
            updateInfoAlert('success');
            if (countdownTimer) {
                clearInterval(countdownTimer);
            }
            break;

        case PAYMENT_STATE.PENDING:
            document.getElementById('pending-state').style.display = 'block';
            document.getElementById('action-buttons-card').style.display = 'none';
            updatePaymentStatus('Chờ thanh toán', 'bg-warning');
            updateInfoAlert('pending');
            startCountdown();
            break;

        case PAYMENT_STATE.FAILED:
            document.getElementById('failed-state').style.display = 'block';
            document.getElementById('action-buttons-card').style.display = 'block';
            updatePaymentStatus('Thanh toán thất bại', 'bg-danger');
            updateInfoAlert('failed');
            if (countdownTimer) {
                clearInterval(countdownTimer);
            }
            break;
    }
}

function updatePaymentStatus(text, badgeClass) {
    const badge = document.getElementById('payment-status');
    badge.textContent = text;
    badge.className = `badge ${badgeClass}`;
}

function updateInfoAlert(state) {
    const infoList = document.getElementById('info-list');

    switch(state) {
        case 'success':
            infoList.innerHTML = `
                    <li>Bạn sẽ nhận được email xác nhận đơn hàng trong vài phút.</li>
                    <li>Vui lòng kiểm tra thông tin đơn hàng và liên hệ với chúng tôi nếu có bất kỳ thắc mắc nào.</li>
                    <li>Đơn hàng sẽ được giao trong thời gian dự kiến theo phương thức vận chuyển bạn đã chọn.</li>
                `;
            break;
        case 'pending':
            infoList.innerHTML = `
                    <li>Vui lòng hoàn tất thanh toán trong thời gian quy định.</li>
                    <li>Đơn hàng sẽ tự động hủy nếu không thanh toán trong thời gian cho phép.</li>
                    <li>Sau khi thanh toán thành công, bạn sẽ nhận được email xác nhận.</li>
                `;
            break;
        case 'failed':
            infoList.innerHTML = `
                    <li>Đơn hàng của bạn chưa được thanh toán thành công.</li>
                    <li>Bạn có thể thử lại thanh toán hoặc đặt hàng mới.</li>
                    <li>Liên hệ với chúng tôi nếu bạn cần hỗ trợ.</li>
                `;
            break;
    }
}
function startCountdown() {
    const timerDisplay = document.getElementById('timer-display');

    // Nếu chưa có thời gian hết hạn, tạo mới
    if (!localStorage.getItem('paymentDeadline')) {
        const deadline = Date.now() + remainingSeconds * 1000;
        localStorage.setItem('paymentDeadline', deadline);
    }

    const deadline = parseInt(localStorage.getItem('paymentDeadline'), 10);

    countdownTimer = setInterval(async () => {
        const now = Date.now();
        remainingSeconds = Math.floor((deadline - now) / 1000);

        if (remainingSeconds < 0) remainingSeconds = 0;

        // Cập nhật giao diện
        const minutes = Math.floor(remainingSeconds / 60);
        const seconds = remainingSeconds % 60;
        timerDisplay.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;

        // Khi hết thời gian
        if (remainingSeconds <= 0) {
            clearInterval(countdownTimer);
            localStorage.removeItem('paymentDeadline'); // Xóa để lần sau tạo mới

            setPaymentState(PAYMENT_STATE.FAILED);
            document.getElementById('failed-message').textContent =
                'Đơn hàng của bạn đã hết thời gian thanh toán. Vui lòng thử lại.';

            try {
                const result = await orderService.updateListOrderStatus(paymentData.orderId, 'CANCELLED');
                if (result.status === "Success") {
                    showErrorToast('Hết thời gian thanh toán!');
                }
            } catch (error) {
                console.error('Lỗi khi hủy đơn hàng:', error);
            }
        }
    }, 1000);
}


async function handlePaymentClick() {
    let url = null;
    const result = await paymentService.createPayment(paymentData, paymentMethod);
    console.log(result.data);
    if (paymentMethod === "MOMO")
        url = result.data.payUrl;
    else url = result.data;
    showInfoToast('Đang chuyển đến trang thanh toán...');
    setTimeout(() => {
        window.location.href = url;
    }, 2000);
}

// Handle retry payment
function handleRetryPayment() {
    remainingSeconds = 900;
    setPaymentState(PAYMENT_STATE.PENDING);
    showWarningToast('Vui lòng hoàn tất thanh toán trong 15 phút');
    const newUrl = window.location.pathname + '/' + paymentData.orderId + '?status=pending';
    window.history.pushState({}, '', newUrl);
}


// Initialize on page load
document.addEventListener('DOMContentLoaded', async () => {
    await loadOrderData();

    // Add event listeners
    const payNowBtn = document.getElementById('pay-now-btn');
    if (payNowBtn) {
        payNowBtn.addEventListener('click', handlePaymentClick);
    }

    const retryBtn = document.getElementById('retry-payment-btn');
    if (retryBtn) {
        retryBtn.addEventListener('click', handleRetryPayment);
    }
});

// Cleanup on page unload
window.addEventListener('beforeunload', () => {
    if (countdownTimer) {
        clearInterval(countdownTimer);
    }
});