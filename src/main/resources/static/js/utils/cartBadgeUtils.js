
const CartBadgeUtils = {
    updateCartBadge(quantity) {
        const cartBadge = document.getElementById('cart-quantity');
        if (cartBadge) {
            cartBadge.textContent = quantity;

            // Add animation effect
            cartBadge.classList.add('badge-pulse');
            setTimeout(() => {
                cartBadge.classList.remove('badge-pulse');
            }, 500);
        }
    },

    async refreshCartBadge(userId) {
        try {
            const response = await fetch(`/api/carts/${userId}`);
            const result = await response.json();

            if (result.status === 'true' && result.data) {
                const totalQuantity = result.data.length;
                this.updateCartBadge(totalQuantity);
            } else {
                this.updateCartBadge(0);
            }
        } catch (error) {
            console.error('Error refreshing cart badge:', error);
        }
    },

    initCartBadge(userId) {
        this.refreshCartBadge(userId);
    }
}

export default CartBadgeUtils;


