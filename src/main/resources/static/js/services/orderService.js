import apiClient from '/js/services/apiClient.js';

const orderService = {
    getOrderByUserId: function(userId) {
        apiClient.get(`/orders/${userId}`).then((response) => {
            return response;
        })
    },

    saveOrder: async function(orderData) {
        try {
           return await apiClient.post(`/orders`, orderData)
        } catch (error) {
            console.error('Lỗi khi tạo đơn hàng:', error);
            throw error;
        }
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
    },

    setDisplayTempOrder: async function (orderDate) {
        try {
            return await apiClient.post('/orders/temp-order', orderDate)
        } catch (error) {
            console.error('Lỗi khi lưu đơn hàng tạm thời:', error);
            throw error;
        }
    },

    getDisplayTempOrder: async function () {
        try {
            return await apiClient.get('/orders/temp-order')
        } catch (error) {
            console.error('Lỗi khi lấy đơn hàng tạm thời:', error);
            throw error;
        }
    }
}

export default orderService;