import apiClient from '/js/services/apiClient.js';

const cartService = {
    getCartItemByUserId: async function (itemId) {
        try {
            return await apiClient.get(`/carts/${itemId}`);
        } catch (error) {
            console.error('Lỗi khi lấy sản phẩm:', error);
            throw error;
        }
    },

    getCartItemByUserIdPaginated: async function(userId, page = 1, size = 5) {
        try {
            return await apiClient.get(`/carts/${userId}/paginated?page=${page}&size=${size}`);
        } catch (error) {
            console.error('Lỗi khi lấy sản phẩm có phân trang:', error);
            throw error;
        }
    },

    updateCartItem: async function(cartItemId, quantity) {
        try {
            return await apiClient.put(`/carts/${cartItemId}?quantity=${quantity}`);
        } catch (error) {
            console.error('Lỗi khi cập nhật giỏ hàng:', error);
            throw error;
        }
    },

    removeCartItem: async function(cartItemId) {
        try {
            return await apiClient.delete(`/carts/${cartItemId}`);
        } catch (error) {
            console.error('Lỗi khi xóa sản phẩm khỏi giỏ hàng:', error);
            throw error;
        }
    },

    saveSelectedCartItem: async function (listSelected) {
        try {
            return await apiClient.post(`/carts/selected`, listSelected);
        } catch (error) {
            console.error('Lỗi khi chọn sản phẩm trong giỏ hàng:', error);
            throw error;
        }
    },

    getSelectedCartItem: async function (userId) {
        try {
            return await apiClient.get(`/carts/selected`);
        } catch (error) {
            console.error('Lỗi khi lấy sản phẩm đã chọn trong giỏ hàng:', error);
            throw error;
        }
    }
};

export default cartService;
