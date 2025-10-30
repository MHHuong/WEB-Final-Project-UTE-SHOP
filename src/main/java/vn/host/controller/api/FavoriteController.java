package vn.host.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.common.ProductDTO;
import vn.host.entity.Favorite;
import vn.host.model.response.ApiResponse;
import vn.host.model.response.ProductResponse;
import vn.host.service.FavoriteService;

import java.util.List;

@RestController()
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("{userId}/{productId}")
    public ResponseEntity<?> addFavorite(@PathVariable Long userId, @PathVariable Long productId) {
        try {
            Favorite favorite = favoriteService.addFavorite(userId, productId);
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Product added to favorites successfully",
                            favorite
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return  new ResponseEntity<> (
                    new ApiResponse(
                            "Error",
                            "Error adding product to favorites: " + e.getMessage(),
                            null
                    ), HttpStatus.BAD_REQUEST
            );
        }
    }

    @DeleteMapping("{userId}/{productId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Long userId, @PathVariable Long productId) {
        try {
            favoriteService.removeFavorite(userId, productId);
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Product removed from favorites successfully",
                            null
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Error removing product from favorites: " + e.getMessage(),
                            null
                    ), HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("{userId}")
    public ResponseEntity<?> getUserFavorites(@PathVariable Long userId) {
        try {
            List<ProductDTO> favorites = favoriteService.findByUserId(userId);
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Get user favorites successfully",
                            favorites
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Error getting user favorites: " + e.getMessage(),
                            null
                    ), HttpStatus.BAD_REQUEST
            );
        }
    }
}
