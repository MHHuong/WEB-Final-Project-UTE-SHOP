import { API_BASE_URL, CONTEXT_PATH } from './config.js';
import apiClient from './utils/apiClient.js';

// Global authentication state
export const AuthState = {
    userId: null,
    userInfo: null,
    token: null,

    // Get token from localStorage
    getToken() {
        if (!this.token) {
            this.token = localStorage.getItem('authToken'); // Changed from 'authToken' to 'token'
            console.log('Token loaded from localStorage:', this.token);
        }
        return this.token;
    },

    // Set token to localStorage
    setToken(token) {
        this.token = token;
        if (token) {
            localStorage.setItem('authToken', token);
        } else {
            localStorage.removeItem('authToken');
        }
    },

    // Check if user is authenticated
    isAuthenticated() {
        return !!this.getToken();
    },

    // Logout
    logout() {
        this.token = null;
        this.userId = null;
        this.userInfo = null;
        localStorage.removeItem('authToken');
        window.location.href = `${CONTEXT_PATH}/`;
    },

    setUserId(id) {
        this.userId = id;
    },

    getUserId() {
        return this.userId;
    },

    setUserInfo(info) {
        this.userInfo = info;
    },

    getUserInfo() {
        return this.userInfo;
    },

    // Fetch userId from API
    async fetchUserId() {
        try {
            const result = await apiClient.get('/user/me');
            if (result.status === 'Success' && result.data) {
                this.setUserId(result.data.userId);
                console.log('User ID loaded from API:', this.userId);
                return result.data.userId;
            }
        } catch (error) {
            console.error('Error fetching user ID:', error);
            // Nếu lỗi 401/403, logout
            if (error.status === 401 || error.status === 403) {
                this.logout();
            }
        }
        return null;
    },

    // Fetch full user info from API
    async fetchUserInfo() {
        try {
            const result = await apiClient.get('/user/me');
            if (result.status === 'Success' && result.data) {
                this.setUserInfo(result.data);
                this.setUserId(result.data.userId);
                console.log('User info loaded from API:', this.userInfo);
                return result.data;
            }
        } catch (error) {
            console.error('Error fetching user info:', error);
            // Nếu lỗi 401/403, logout
            if (error.status === 401 || error.status === 403) {
                this.logout();
            }
        }
        return null;
    },

    async updateUserInfo(userId, userData) {
        try {
            const result = await apiClient.put(`/user/${userId}`, userData);
            if (result.status === 'Success' && result.data) {
                this.setUserInfo(result.data);
                console.log('User info updated from API:', this.userInfo);
                return result;
            }
        } catch (error) {
            console.error('Error updating user info:', error);
            // Nếu lỗi 401/403, logout
            if (error.status === 401 || error.status === 403) {
                this.logout();
            }
        }
    },

    async updatePassword(userId, password) {
        try {
            const result = await apiClient.put(`/user/${userId}/password`, password);
            console.log(result);
            if (result.status === 'Success') {
                console.log('Password updated successfully');
                return result;
            }
        } catch (error) {
            console.error('Error updating password:', error);
            // Nếu lỗi 401/403, logout
            if (error.status === 401 || error.status === 403) {
                this.logout();
            }
        }
    }
};

// Auto-initialize from API when DOM is loaded
document.addEventListener('DOMContentLoaded', async () => {
    // Kiểm tra token trước
    // if (!AuthState.isAuthenticated()) {
    //     console.warn('No auth token found, user might need to login');
    //     // Không redirect ở đây vì có thể là trang public
    //     return;
    // }

    // Nếu có token, load thông tin user
    await AuthState.fetchUserInfo();
});
