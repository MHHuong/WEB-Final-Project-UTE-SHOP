import favoriteService from "./services/api/favoriteService.js";
import { showErrorToast, showSuccessToast } from "./utils/toastUtils.js";
import { AuthState } from "./auth.js";
import cartService from "./services/api/cartService.js";
import cartBadgeUtils from "./utils/cartBadgeUtils.js";

let USER_ID = localStorage.getItem('userId') || 0;

// Flag to ensure event listeners are only added once
let productCardInitialized = false;

// Initialize product card event listeners
export function initProductCardListeners() {
    if (productCardInitialized) {
        console.log('Product card listeners already initialized');
        return;
    }

    // Event delegation - Add to Wishlist
    document.addEventListener('click', async function (event) {
        if (event.target.closest('.btn-action-wishlist')) {
            event.preventDefault();
            const button = event.target.closest('.btn-action-wishlist');
            const productId = button.getAttribute('data-id');
            const icon = button.querySelector('i');

            // Get USER_ID
            USER_ID = localStorage.getItem('userId') || 0;
            if (USER_ID === 0) {
                const result = await AuthState.fetchUserInfo();
                if (result && result.status === "Success" && result.data) {
                    USER_ID = result.data.userId;
                    localStorage.setItem('userId', USER_ID);
                } else {
                    showErrorToast('Please log in to add favorites.');
                    return;
                }
            }
            if (button.classList.contains('active')) {
                await removeFavorite(productId, button, icon);
            }
            else await addFavorite(productId, button, icon);
        }
    }, { once: false });

    // Event delegation - Add to Cart
    document.addEventListener('click', async function (event) {
        if (event.target.closest('.btn-add-cart')) {
            event.preventDefault();
            event.stopPropagation();
            const button = event.target.closest('.btn-add-cart');
            const id = button.getAttribute('data-id');
            try {
                // Get USER_ID
                USER_ID = localStorage.getItem('userId') || 0;
                if (USER_ID === 0) {
                    const result = await AuthState.fetchUserInfo();
                    if (result && result.status === "Success" && result.data) {
                        USER_ID = result.data.userId;
                        localStorage.setItem('userId', USER_ID);
                    } else {
                        showErrorToast('Please log in to add items to cart.');
                        return;
                    }
                }
                let quantity = 1;
                const detailContainer = button.closest('.ps-lg-10');
                if (detailContainer) {
                    const quantityInput = detailContainer.querySelector('.quantity-field');
                    if (quantityInput) {
                        quantity = Number(quantityInput.value);
                    }
                }
                const cart = {
                    userId: USER_ID,
                    productId: Number(id),
                    quantity: quantity
                };

                const result = await cartService.addSelectedCartItem(cart);
                if (result.status === "Success") {
                    await cartBadgeUtils.refreshCartBadge(USER_ID);
                    showSuccessToast('Added to cart!');
                } else {
                    showErrorToast('Failed to add to cart: ' + result.message);
                }
            } catch (error) {
                console.error('Error adding to cart:', error);
                showErrorToast('An error occurred: ' + error.message);
            }
        }
    }, { once: false });

    productCardInitialized = true;
    console.log('Product card listeners initialized');
}

// Add product to favorites
async function addFavorite(productId, button, icon) {
    try {
        const result = await favoriteService.createFavorite(productId, USER_ID);
        if (result.status === "Success") {
            // Toggle active class
            button.classList.add('active');

            // Change icon to filled heart
            icon.classList.remove('bi-heart');
            icon.classList.add('bi-heart-fill');
            button.setAttribute('title', 'Remove from Wishlist');

            showSuccessToast('Added to favorites!');
        } else {
            showErrorToast('Failed to add to favorites: ' + result.message);
        }
    } catch (error) {
        console.error('Error adding to favorites:', error);
        showErrorToast('An error occurred: ' + error.message);
    }
}

async function removeFavorite(productId, button, icon) {
    try {
        const result = await favoriteService.removeFavorite(productId, USER_ID);
        if (result.status === "Success") {
            button.classList.remove('active');

            icon.classList.remove('bi-heart-fill');
            icon.classList.add('bi-heart');
            button.setAttribute('title', 'Add to Wishlist');

            showSuccessToast('Removed from favorites!');
        } else {
            showErrorToast('Failed to remove from favorites: ' + result.message);
        }
    } catch (error) {
        console.error('Error removing from favorites:', error);
        showErrorToast('An error occurred: ' + error.message);
    }
}

// Auto-initialize when DOM is loaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initProductCardListeners);
} else {
    // DOM already loaded
    initProductCardListeners();
}