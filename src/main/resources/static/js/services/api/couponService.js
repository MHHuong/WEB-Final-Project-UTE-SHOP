import apiClient from '/js/utils/apiClient.js';

const couponService = {
    getCouponByCode: async function (code) {
        try {
            return await apiClient.get(`/coupons/${code}`);
        } catch (error) {
            console.error('Lỗi khi lấy mã giảm giá:', error);
            throw error;
        }
    },

    getAllGlobalCoupons: async function () {
        try {
            return await apiClient.get(`/coupons/global`);
        } catch (error) {
            console.error('Lỗi khi lấy tất cả mã giảm giá:', error);
            throw error;
        }
    },

    getAllShopCoupons: async function (shopId) {
        try {
            return await apiClient.get(`/coupons/shop/${shopId}`);
        } catch (error) {
            console.log(error)
            console.error('Lỗi khi lấy tất cả mã giảm giá của cửa hàng:', error);
            throw error;
        }
    }
};

export default couponService;