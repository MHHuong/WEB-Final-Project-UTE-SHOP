import apiClient from '/js/utils/apiClient.js';

const orderService = {
    getOrderByUserId: async function (userId) {
        try {
            if (!userId) {
                return new Error('userId không được để trống');
            }
            return await apiClient.get(`/orders/${userId}`);
        } catch (error) {
            console.error('Lỗi khi lấy đơn hàng theo userId:', error);
            throw error;
        }
    },

    getOrderById: async function (orderId) {
        try {
            if (!orderId) {
                throw new Error('orderId không được để trống');
            }
            return await apiClient.get(`/orders/detail/${orderId}`);
        } catch (error) {
            console.error('Lỗi khi lấy chi tiết đơn hàng:', error);
            throw error;
        }
    },

    saveOrder: async function(orderData) {
        try {
           return await apiClient.post(`/orders`, orderData)
        } catch (error) {
            console.error('Lỗi khi tạo đơn hàng:', error);
            throw error;
        }
    },

    updateStatusOrder: async function (orderId, status) {
        try {
            if (!orderId || !status) {
                return new Error('orderId và status không được để trống');
            }
            return await apiClient.put(`/orders/status?order=${orderId}&status=${status}`);
        } catch (error) {
            console.error('Lỗi khi cập nhật trạng thái đơn hàng:', error);
            return error;
        }
    },

    updateStatusOrderWithReason: async function (orderId, status, reason = '') {
        try {
            if (!orderId || !status) {
                return new Error('orderId và status không được để trống');
            }
            const params = new URLSearchParams({
                order: orderId,
                status: status
            });
            if (reason) {
                params.append('reason', reason);
            }
            return await apiClient.put(`/orders/status?${params.toString()}`);
        } catch (error) {
            console.error('Lỗi khi cập nhật trạng thái đơn hàng:', error);
            return error;
        }
    },

    updateListOrderStatus: async function (listOrderId, status) {
        try {
            if (!listOrderId || !status) {
                return new Error('listOrderId và status không được để trống');
            }
            return await apiClient.put(`/orders/status/batch?orderIdsStr=${listOrderId}&status=${status}`);
        } catch (error) {
            console.error('Lỗi khi cập nhật trạng thái danh sách đơn hàng:', error);
            return error;
        }
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