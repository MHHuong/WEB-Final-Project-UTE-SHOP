import apiClient from '/js/utils/apiClient.js';

const paymentService = {
    createPayment: async function (paymentData, paymentMethod) {
        try {
            if (paymentMethod === 'MOMO') {
                return await apiClient.post('/payment/momo/create', paymentData)
            }
            else return await apiClient.post('/payment/vn_pay/create', paymentData)
        } catch (error) {
            console.error('Lỗi khi thanh toán VNPAY:', error);
            throw error;
        }
    }
}


export default paymentService;