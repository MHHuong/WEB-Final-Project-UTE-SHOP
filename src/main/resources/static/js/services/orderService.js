import apiClient from '/js/services/apiClient.js';

const orderService = {
    getOrderByUserId: function(userId) {
        apiClient.get(`/orders/${userId}`).then((response) => {
            return response;
        })
    },

    saveOrder: function(orderData) {
        apiClient.post('/orders', orderData).then((response) => {
            return response;
        })
    },

    updateOrder: function(orderId, orderData) {
        apiClient.put(`/orders/${orderId}`, orderData).then((response) => {
            return response;
        })
    },

    updateStatusOrder: function(orderId, status) {
        apiClient.put(`/orders/status?order=${orderId}&status=${status}`).then((response) => {
            return response;
        })
    },

    updatePaymentOrder: function(orderId, payment) {
        apiClient.put(`/orders/payment?order=${orderId}`, payment).then((response) => {
            return response;
        })
    },

    calculateShippingFee: async function(shippingData) {
        try {
            return await apiClient.post('/orders/shipping-fee', shippingData)
        } catch (error) {
            console.error('Lỗi khi tính phí vận chuyển:', error);
            throw error;
        }
    }
}

export default orderService;