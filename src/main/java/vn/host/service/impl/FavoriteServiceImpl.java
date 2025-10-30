package vn.host.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.host.dto.common.ProductDTO;
import vn.host.entity.Favorite;
import vn.host.entity.Product;
import vn.host.entity.User;
import vn.host.model.response.ProductResponse;
import vn.host.repository.FavoriteRepository;
import vn.host.repository.ProductRepository;
import vn.host.repository.UserRepository;
import vn.host.service.FavoriteService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {
    @Autowired
    FavoriteRepository favoriteRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;


    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getMedia() != null && !product.getMedia().isEmpty()) {
            dto.setImageUrl(product.getMedia().iterator().next().getUrl());
        } else {
            dto.setImageUrl("/assets/images/products/product-img-default.jpg"); // Ảnh mặc định
        }
        double avgRating = 0.0;
        int reviewCount = 0;
        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            avgRating = product.getReviews().stream()
                    .mapToDouble(review -> review.getRating())
                    .average()
                    .orElse(0.0);
            reviewCount = product.getReviews().size();
        }

        dto.setAverageRating(avgRating);
        dto.setReviewCount(reviewCount);

        return dto;
    }

    @Override
    public List<ProductDTO> findByUserId(long userId) {
       List<Favorite> favorites = favoriteRepository.findByUser_UserId(userId);
       List<Product> products = new ArrayList<>();
       for (Favorite favorite : favorites) {
           products.add(favorite.getProduct());
       }
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Favorite addFavorite(Long userId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Favorite favorite = Favorite.builder()
                .id(new vn.host.entity.FavoriteId(userId, productId))
                .user(user)
                .product(product)
                .build();
        return favoriteRepository.save(favorite);
    }

    @Override
    public void removeFavorite(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        favoriteRepository.deleteByUserAndProduct(user, product);
    }
}
