package vn.host.controller.api;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.host.model.request.CartRequest;
import vn.host.model.response.CartResponse;
import vn.host.model.response.PageResponse;
import vn.host.model.response.ApiResponse;
import vn.host.service.CartItemService;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    CartItemService cartItemService;

    @GetMapping("{id}")
    public ResponseEntity<?> findCartItemByUser(@PathVariable Long id) {
        try {
            List<CartResponse> userCart = cartItemService.findUserCartItems(id);
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Get user cart successfully",
                            userCart
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    ), HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("{id}/paginated")
    public ResponseEntity<?> findCartItemByUserPaginated(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            PageResponse<CartResponse> userCart = cartItemService.findUserCartItemsPaginated(id, page, size);
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Get user cart successfully",
                            userCart
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    ), HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping()
    public ResponseEntity<?> saveCartItem(@RequestBody CartRequest cartRequest) {
        try {
            cartItemService.saveCart(cartRequest);
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Add product to cart successfully",
                            null
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateCartItemQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            cartItemService.updateCartItemQuantity(id, quantity);
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Update product quantity in cart successfully",
                            null
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @DeleteMapping("{cartItemId}")
    public ResponseEntity<?> deleteCartItem(@PathVariable Long cartItemId) {
        try {
            cartItemService.deleteById(cartItemId);
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Delete product from cart successfully",
                            null
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping("/selected")
    public ResponseEntity<?> selectedCartItems(@RequestBody List<Object> productItems, HttpSession session) {
        try {
            session.setAttribute("selectedProducts", productItems);
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Selected products from cart successfully",
                            null
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/selected")
    public ResponseEntity<?> getSelectedCartItems(HttpSession session) {
        try {
            Object selectedProducts = session.getAttribute("selectedProducts");
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Get selected products from cart successfully",
                            selectedProducts
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}