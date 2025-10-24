import apiClient from '/js/services/apiClient.js';

const cartService = {
    async getCartItemByUserId(itemId) {
        try {
            return await apiClient.get(`/carts/${itemId}`);
        } catch (error) {
            console.error('Lỗi khi lấy sản phẩm:', error);
            throw error;
        }
    },

    async getCartItemByUserIdPaginated(userId, page = 1, size = 5) {
        try {
            return await apiClient.get(`/carts/${userId}/paginated?page=${page}&size=${size}`);
        } catch (error) {
            console.error('Lỗi khi lấy sản phẩm có phân trang:', error);
            throw error;
        }
    },

    async updateCartItem(cartItemId, quantity) {
        try {
            return await apiClient.put(`/carts/${cartItemId}?quantity=${quantity}`);
        } catch (error) {
            console.error('Lỗi khi cập nhật giỏ hàng:', error);
            throw error;
        }
    },

    async removeCartItem(cartItemId) {
        try {
            return await apiClient.delete(`/carts/${cartItemId}`);
        } catch (error) {
            console.error('Lỗi khi xóa sản phẩm khỏi giỏ hàng:', error);
            throw error;
        }
    }
};

export default cartService;
