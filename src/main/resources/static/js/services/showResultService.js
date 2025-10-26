import {showErrorToast} from "/js/utils/toastUtils.js";
import orderService from "/js/services/orderService.js";

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
   const result = await orderService.getDisplayTempOrder();
    if (result.status === "Success") {
        try {
            console.log(result.data);
            document.getElementById('order-code').textContent = result.orderCode || 'N/A';
            displayOrderData(result.data);
        } catch (error) {
            console.error('Error parsing order data:', error);
            showErrorToast('Không thể tải thông tin đơn hàng');
        }
    } else {
        document.getElementById('order-code').textContent = result.orderCode || 'N/A';
        populateDefaultData();
    }
}

// Display order data
function displayOrderData(orderData) {
    // Order code
    if (orderData.orderCode) {
        document.getElementById('order-code').textContent = orderData.orderCode;
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

document.addEventListener('DOMContentLoaded', async () => {
    await loadOrderData();
});