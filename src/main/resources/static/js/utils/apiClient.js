import { API_BASE_URL } from '../config.js';

const apiClient = {
        baseUrl: API_BASE_URL,

        // Helper: Get auth token from localStorage
        getAuthHeaders() {
            const token = localStorage.getItem('authToken');
            const headers = {
                'Content-Type': 'application/json'
            };
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }
            return headers;
        },

        get: function(endpoint, params = {}) {
            return $.ajax({
                url: this.baseUrl + endpoint,
                method: 'GET',
                data: params,
                headers: this.getAuthHeaders(),
            })
        },

        post: function(endpoint, data = {}) {
            return $.ajax({
                url: this.baseUrl + endpoint,
                method: 'POST',
                data: JSON.stringify(data),
                headers: this.getAuthHeaders(),
            });
        },

        put: function(endpoint, data = {}) {
            return $.ajax({
                url: this.baseUrl + endpoint,
                method: 'PUT',
                data: JSON.stringify(data),
                headers: this.getAuthHeaders(),
            });
        },

        delete: function(endpoint) {
            return $.ajax({
                url: this.baseUrl + endpoint,
                method: 'DELETE',
                headers: this.getAuthHeaders(),
            });
        }
}

export default apiClient;
export const urlParams = new URLSearchParams(window.location.search);