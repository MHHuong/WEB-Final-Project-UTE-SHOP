// Toast utility functions for displaying notifications

function showToast(type, message) {
    const toastId = `${type}Toast`;
    const toastMessageId = `${type}ToastMessage`;
    const toastElement = document.getElementById(toastId);
    const toastMessageElement = document.getElementById(toastMessageId);

    if (toastMessageElement) {
        toastMessageElement.textContent = message;
    }

    const toast = new bootstrap.Toast(toastElement, {
        animation: true,
        autohide: true,
        delay: 3000
    });
    toast.show();
}

/**
 * Show success toast
 * @param {string} message - Success message
 */
export function showSuccessToast(message) {
    showToast('success', message);
}


export function showErrorToast(message) {
    showToast('error', message);
}


export function showWarningToast(message) {
    showToast('warning', message);
}


export function showInfoToast(message) {
    showToast('info', message);
}

export default {
    showSuccessToast,
    showErrorToast,
    showWarningToast,
    showInfoToast
};

