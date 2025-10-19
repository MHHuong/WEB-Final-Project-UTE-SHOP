import apiClient from '/js/services/apiClient.js';

const cartService = {
    async getCartItemByUserId(itemId) {
        try {
            return await apiClient.get(`/carts/${itemId}`);
        } catch (error) {
            console.error('Lỗi khi lấy sản phẩm:', error);
            throw error;
        }
    }
};

export default cartService;
