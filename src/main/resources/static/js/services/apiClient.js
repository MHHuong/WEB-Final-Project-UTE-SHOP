const apiClient = {
        baseUrl: '/api',

        get: function(endpoint, params = {}) {
            return $.ajax({
                url: this.baseUrl + endpoint,
                method: 'GET',
                data: params,
                contentType: 'application/json',
            })
        },

        post: function(endpoint, data = {}) {
            return $.ajax({
                url: this.baseUrl + endpoint,
                method: 'POST',
                data: JSON.stringify(data),
                contentType: 'application/json',
            });
        },

        put: function(endpoint, data = {}) {
            return $.ajax({
                url: this.baseUrl + endpoint,
                method: 'PUT',
                data: JSON.stringify(data),
                contentType: 'application/json',
            });
        },

        delete: function(endpoint) {
            return $.ajax({
                url: this.baseUrl + endpoint,
                method: 'DELETE',
                contentType: 'application/json',
            });
        }
}

export default apiClient;
export const urlParams = new URLSearchParams(window.location.search);