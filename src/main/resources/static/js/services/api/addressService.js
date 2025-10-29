import apiClient from '../../utils/apiClient.js';

const addressService = {
    getAddressesByUserId : async function (userId ) {
        try {
            return await apiClient.get(`/addresses/${userId}`);
        } catch (error) {
            console.error('Lỗi khi lấy địa chỉ:', error);
            throw error;
        }
    },

    getAddressDefaultByUserId : async function (userId ) {
        try {
            return await apiClient.get(`/addresses/${userId}/default`);
        } catch (error) {
            console.error('Lỗi khi lấy địa chỉ mặc định:', error);
            throw error;
        }
    },

    getAddressesPaginationByUserId: async function (userId, size, page ) {
        try {
            return await apiClient.get(`/addresses/${userId}/pagination?size=${size}&page=${page}`);
        } catch (error) {
            console.error('Lỗi khi lấy địa chỉ phân trang:', error);
            throw error;
        }
    },

    createAddress : async function (addressData, userId) {
        try {
            return await apiClient.post(`/addresses/${userId}`, addressData);
        } catch (error) {
            console.error('Lỗi khi thêm địa chỉ:', error);
            throw error;
        }
    },

    removeAddress : async function (addressId, userId ) {
        try {
            return await apiClient.delete(`/addresses/${userId}/${addressId}`);
        } catch (error) {
            console.error('Lỗi khi xóa địa chỉ:', error);
            throw error;
        }
    },

    updateAddress : async function (addressData, userId) {
        try {
            return await apiClient.put(`/addresses/${userId}`, addressData);
        } catch (error) {
            console.error('Lỗi khi cập nhật địa chỉ:', error);
            throw error;
        }
    }
}
export default addressService