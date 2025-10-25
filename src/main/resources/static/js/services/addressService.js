import apiClient from '/js/services/apiClient.js';

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
    }
}

export default addressService