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
    }
}