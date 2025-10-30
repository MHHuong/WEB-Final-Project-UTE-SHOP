import cartService from "../services/api/cartService.js";

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
            const result = await cartService.getCartItemByUserId(userId);
            if (result.status === 'Success' && result.data) {
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
