package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.host.dto.common.ProductDTO;
import vn.host.dto.common.PageResult;
import vn.host.dto.product.ProductListItemVM;
import vn.host.entity.Product;
import vn.host.model.response.ProductResponse;
import vn.host.repository.ProductMediaRepository;
import vn.host.repository.ProductRepository;
import vn.host.repository.ShopRepository;
import vn.host.repository.UserRepository;
import vn.host.service.CategoryService;
import vn.host.service.ProductService;
import vn.host.spec.ProductSpecs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final UserRepository users;
    private final ShopRepository shops;
    private final ProductMediaRepository mediaRepo;

//    @Autowired
//    public ProductServiceImpl(ProductRepository productRepository, CategoryService categoryService) {
//        this.productRepository = productRepository;
//        this.categoryService = categoryService;
//    }

    @Override
    public void deleteById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Sản phẩm không tồn tại");
        }
        productRepository.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = findById(id);
        product.setStatus(status);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAllWithDetails();
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findProductsByCategory(String categoryName, Pageable pageable) {
        Page<Product> productPage = productRepository.findByCategoryName(categoryName, pageable);
        return productPage.map(this::convertToDTO);
    }

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
    @Transactional(readOnly = true)
    public List<ProductDTO> findAllProductsAsDTO() {
        List<Product> products = this.findAll();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findProductsByCategoryId(Long categoryId, Pageable pageable) {
        Set<Long> allCategoryIds = categoryService.getCategoryAndDescendantIds(categoryId);
        if (allCategoryIds.isEmpty()) {
            return Page.empty(pageable);
        }
        Page<Product> productPage = productRepository.findByCategoryIdsIn(allCategoryIds, pageable);
        return productPage.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllProductsFiltered(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> productPage = productRepository.findAllWithPriceFilter(minPrice, maxPrice, pageable);
        return productPage.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findProductsByCategoryIdFiltered(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Set<Long> allCategoryIds = categoryService.getCategoryAndDescendantIds(categoryId);
        if (allCategoryIds.isEmpty()) {
            return Page.empty(pageable);
        }
        Page<Product> productPage = productRepository.findByCategoryIdsInAndPriceFilter(allCategoryIds, minPrice, maxPrice, pageable);
        return productPage.map(this::convertToDTO);
    }

    @Override
    public Page<Product> findAllForAdmin(String keyword, Long categoryId, Long shopId, Integer status, Pageable pageable) {
        Specification<Product> spec = Specification.allOf();

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("categoryId"), categoryId));
        }

        if (shopId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("shop").get("shopId"), shopId));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        return productRepository.findAll(spec, pageable);
    }

    @Override
    public Page<Product> searchByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public Page<Product> searchByShopName(String shopName, Pageable pageable) {
        return productRepository.findByShopNameContainingIgnoreCase(shopName, pageable);
    }

    @Override
    public void save(Product product) {
        productRepository.save(product);
    }

    @Override
    public void delete(long id) {
        productRepository.deleteById(id);
    }

    @Override
    public Product findById(long id) {
        return productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
    }

    @Override
    public List<Product> findByShopId(long shopId) {
        return productRepository.findByShop_ShopId(shopId, Pageable.unpaged()).getContent();
    }

    @Override
    public List<Product> findByCategoryId(long id) {
        return productRepository.findByCategory_CategoryId(id, Pageable.unpaged()).getContent();
    }

    @Override
    public PageResult<ProductListItemVM> searchOwnerProducts(String userEmail, String q, Long categoryId, Integer status, BigDecimal minPrice, BigDecimal maxPrice, int page, int size, Sort sort) {
        var user = users.findByEmail(userEmail)
                .orElseThrow(() -> new SecurityException("User not found"));
        var shop = shops.findFirstByOwner_UserId(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("Bạn chưa đăng ký shop."));
        Long shopId = shop.getShopId();

        Pageable pageable = PageRequest.of(page, size, sortOrDefault(sort));

        List<Specification<Product>> list = new ArrayList<>();
        list.add(ProductSpecs.belongsToShop(shopId));

        if (status == null) {
            list.add(ProductSpecs.notDeleted());
        } else {
            list.add(ProductSpecs.statusIs(status));
        }

        if (StringUtils.hasText(q)) list.add(ProductSpecs.nameContains(q));
        if (categoryId != null) list.add(ProductSpecs.categoryIs(categoryId));
        if (minPrice != null) list.add(ProductSpecs.priceGte(minPrice));
        if (maxPrice != null) list.add(ProductSpecs.priceLte(maxPrice));

        Specification<Product> spec = Specification.allOf(list);

        Page<Product> pg = productRepository.findAll(spec, pageable);

        var content = pg.getContent().stream()
                .map(p -> ProductListItemVM.of(p, resolveThumb(p.getProductId())))
                .toList();

        return PageResult.<ProductListItemVM>builder()
                .content(content)
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalElements(pg.getTotalElements())
                .totalPages(pg.getTotalPages())
                .build();
    }

    private Sort sortOrDefault(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return sort;
    }

    private String resolveThumb(Long productId) {
        var list = mediaRepo.findByProduct_ProductId(productId);
        if (list == null || list.isEmpty()) return null;

        return list.stream()
                .filter(m -> m.getType() == vn.host.util.sharedenum.MediaType.image)
                .sorted(java.util.Comparator.comparing(vn.host.entity.ProductMedia::getMediaId))
                .map(vn.host.entity.ProductMedia::getUrl)
                .findFirst()
                .orElse(null);
    }

    @Override
    @jakarta.transaction.Transactional
    public void softDeleteOwnerProduct(String userEmail, long productId) {
        var user = users.findByEmail(userEmail)
                .orElseThrow(() -> new SecurityException("User not found"));
        var shop = shops.findFirstByOwner_UserId(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("Bạn chưa đăng ký shop."));

        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        if (p.getShop() == null || !p.getShop().getShopId().equals(shop.getShopId())) {
            throw new SecurityException("Bạn không có quyền thao tác sản phẩm này.");
        }

        // Soft delete
        p.setStatus(3);
        productRepository.save(p);
    }

    @Override
    @jakarta.transaction.Transactional
    public void restoreOwnerProduct(String userEmail, long productId, int toStatus) {
        if (toStatus < 0 || toStatus > 2) {
            throw new IllegalArgumentException("Trạng thái khôi phục phải là 0/1/2");
        }
        var user = users.findByEmail(userEmail)
                .orElseThrow(() -> new SecurityException("User not found"));
        var shop = shops.findFirstByOwner_UserId(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("Bạn chưa đăng ký shop."));

        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        if (p.getShop() == null || !p.getShop().getShopId().equals(shop.getShopId())) {
            throw new SecurityException("Bạn không có quyền thao tác sản phẩm này.");
        }

        p.setStatus(toStatus);
        productRepository.save(p);
    }

    @Override
    @jakarta.transaction.Transactional
    public void bulkUpdateStatus(String userEmail, List<Long> productIds, int status) {
        if (status < 0 || status > 3) throw new IllegalArgumentException("Status phải là 0/1/2/3");
        if (productIds == null || productIds.isEmpty()) return;

        var user = users.findByEmail(userEmail)
                .orElseThrow(() -> new SecurityException("User not found"));
        var shop = shops.findFirstByOwner_UserId(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("Bạn chưa đăng ký shop."));
        Long shopId = shop.getShopId();

        var list = productRepository.findAllById(productIds);
        if (list.isEmpty()) return;

        list.stream()
                .filter(p -> p.getShop() != null && shopId.equals(p.getShop().getShopId()))
                .forEach(p -> p.setStatus(status));

        productRepository.saveAll(list);
    }

    @Override
    public List<ProductResponse> findAllProductOrder() {
        return productRepository.findAllProductsOrder();
    }
}