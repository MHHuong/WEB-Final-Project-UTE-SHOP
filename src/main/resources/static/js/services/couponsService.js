import apiClient from "./apiClient";
const couponsService = {
    getCouponByCode: async function (code) {
        try {
            return await apiClient.get(`/coupons/${code}`);
        } catch (error) {
            console.error('Lỗi khi lấy mã giảm giá:', error);
            throw error;
        }
    },

    getAllCoupons: async function () {
        try {
            return await apiClient.get(`/coupons`);
        } catch (error) {
            console.error('Lỗi khi lấy tất cả mã giảm giá:', error);
            throw error;
        }
    }
};

export default couponsService;