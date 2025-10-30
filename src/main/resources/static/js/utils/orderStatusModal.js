import orderService from '../services/api/orderService.js';
import {showErrorToast, showSuccessToast} from "../../js/utils/toastUtils.js";

// Map trạng thái tiếng Việt
const statusMap = {
    'NEW': { label: 'Pending', class: 'bg-warning' },
    'CONFIRMED': { label: 'Confirmed', class: 'bg-info' },
    'SHIPPING': { label: 'On shipping', class: 'bg-primary' },
    'DELIVERED': { label: 'Shipping completed', class: 'bg-success' },
    'RECEIVED': { label: 'Received', class: 'bg-success' },
    'CANCELLED': { label: 'Cancelled', class: 'bg-danger' },
    'RETURNED': { label: 'Returned', class: 'bg-secondary' }
};

let currentOrderId = null;
let currentOldStatus = null;
let currentNewStatus = null;
let onSuccessCallback = null;

export function showOrderStatusModal(orderId, oldStatus, newStatus, onSuccess = null) {
    currentOrderId = orderId;
    currentOldStatus = oldStatus;
    currentNewStatus = newStatus;
    onSuccessCallback = onSuccess;

    // Cập nhật nội dung modal
    document.getElementById('modal-order-id').textContent = `#${orderId}`;

    // Cập nhật trạng thái cũ
    const oldStatusBadge = document.getElementById('modal-old-status');
    oldStatusBadge.textContent = statusMap[oldStatus]?.label || oldStatus;
    oldStatusBadge.className = `badge ${statusMap[oldStatus]?.class || 'bg-secondary'}`;

    // Cập nhật trạng thái mới
    const newStatusBadge = document.getElementById('modal-new-status');
    newStatusBadge.textContent = statusMap[newStatus]?.label || newStatus;
    newStatusBadge.className = `badge ${statusMap[newStatus]?.class || 'bg-secondary'}`;

    // Hiển thị/ẩn phần lý do
    const reasonSection = document.getElementById('reason-section');
    const cancelReason = document.getElementById('cancel-reason');
    const warningText = document.getElementById('warning-text');

    if (newStatus === 'CANCELLED' || newStatus === 'RETURNED') {
        reasonSection.style.display = 'block';
        cancelReason.value = '';
        cancelReason.classList.remove('is-invalid');

        if (newStatus === 'CANCELLED') {
            warningText.textContent = 'Please provide a reason for cancelling the order so we can improve our service.';
        } else {
            warningText.textContent = 'Please provide a reason for returning the order so we can improve our service.';
        }
    } else {
        reasonSection.style.display = 'none';
        cancelReason.value = '';
        cancelReason.classList.remove('is-invalid');
        warningText.textContent = 'Are you sure you want to change the order status?';
    }

    // Hiển thị modal
    const modal = new bootstrap.Modal(document.getElementById('orderStatusConfirmModal'));
    modal.show();
}


async function confirmStatusChange() {
    const reasonSection = document.getElementById('reason-section');
    const cancelReason = document.getElementById('cancel-reason');
    let reason = '';

    // Validate lý do nếu cần
    if (reasonSection.style.display !== 'none') {
        reason = cancelReason.value.trim();
        if (!reason) {
            cancelReason.classList.add('is-invalid');
            return;
        }
        cancelReason.classList.remove('is-invalid');
    }

    // Disable nút xác nhận
    const confirmBtn = document.getElementById('confirm-status-change-btn');
    const originalBtnText = confirmBtn.innerHTML;
    confirmBtn.disabled = true;
    confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Processing...';

    try {

        const result = await orderService.updateStatusOrderWithReason(
            currentOrderId,
            currentNewStatus,
            reason
        );

        if (result.status === "Success") {
            showSuccessToast('Cập nhật trạng thái đơn hàng thành công!');

            // Đóng modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('orderStatusConfirmModal'));
            modal.hide();

            // Gọi callback nếu có
            if (onSuccessCallback && typeof onSuccessCallback === 'function') {
                onSuccessCallback();
            }
        } else {
            showErrorToast(result.message || 'Error updating order status. Please try again.');
        }
    } catch (error) {
        console.error('Error updating order status:', error);
        showErrorToast('Error updating order status. Please try again.');
    } finally {
        // Khôi phục nút xác nhận
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = originalBtnText;
    }
}

// Xử lý sự kiện khi DOM loaded
document.addEventListener('DOMContentLoaded', function() {
    // Xử lý nút xác nhận
    const confirmBtn = document.getElementById('confirm-status-change-btn');
    if (confirmBtn) {
        confirmBtn.addEventListener('click', confirmStatusChange);
    }

    // Xóa validation khi người dùng nhập
    const cancelReason = document.getElementById('cancel-reason');
    if (cancelReason) {
        cancelReason.addEventListener('input', function() {
            if (this.value.trim()) {
                this.classList.remove('is-invalid');
            }
        });
    }

    // Reset modal khi đóng
    const modal = document.getElementById('orderStatusConfirmModal');
    if (modal) {
        modal.addEventListener('hidden.bs.modal', function() {
            const cancelReason = document.getElementById('cancel-reason');
            if (cancelReason) {
                cancelReason.value = '';
                cancelReason.classList.remove('is-invalid');
            }
        });
    }
});

export default {
    showOrderStatusModal
};
