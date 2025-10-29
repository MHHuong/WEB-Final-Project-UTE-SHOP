import apiClient from '../../utils/apiClient.js';

const favoriteService = {
    getFavoirtiesByUserId : async function (userId ) {
        try {
            return await apiClient.get(`/favorites/${userId}`);
        } catch (error) {
            console.error('Lỗi khi lấy danh sách yêu thích:', error);
            throw error;
        }
    },

    createFavorite : async function (productId, userId) {
        try {
            return await apiClient.post(`/favorites/${userId}/${productId}`);
        } catch (error) {
            console.error('Lỗi khi thêm danh sách yêu thích:', error);
            throw error;
        }
    },

    removeFavorite : async function (productId, userId ) {
        try {
            return await apiClient.delete(`/favorites/${userId}/${productId}`);
        } catch (error) {
            console.error('Lỗi khi thêm danh sách yêu thích:', error);
            throw error;
        }
    },
}
export default favoriteService