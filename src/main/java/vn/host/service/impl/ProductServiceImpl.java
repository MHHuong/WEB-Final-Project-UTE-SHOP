package vn.host.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.host.dto.common.PageResult;
import vn.host.dto.product.ProductListItemVM;
import vn.host.entity.Product;
import vn.host.repository.ProductMediaRepository;
import vn.host.repository.ProductRepository;
import vn.host.repository.ShopRepository;
import vn.host.repository.UserRepository;
import vn.host.service.ProductService;
import vn.host.spec.ProductSpecs;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repo;
    private final UserRepository users;
    private final ShopRepository shops;
    private final ProductMediaRepository mediaRepo;

    @Override
    public void save(Product product) {
        repo.save(product);
    }

    @Override
    public void delete(long id) {
        repo.deleteById(id);
    }

    @Override
    public List<Product> findAll() {
        return repo.findAll();
    }

    @Override
    public Product findById(long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
    }

    @Override
    public List<Product> findByShopId(long shopId) {
        return repo.findByShop_ShopId(shopId, Pageable.unpaged()).getContent();
    }

    @Override
    public List<Product> findByCategoryId(long id) {
        return repo.findByCategory_CategoryId(id, Pageable.unpaged()).getContent();
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

        Page<Product> pg = repo.findAll(spec, pageable);

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
    @Transactional
    public void softDeleteOwnerProduct(String userEmail, long productId) {
        var user = users.findByEmail(userEmail)
                .orElseThrow(() -> new SecurityException("User not found"));
        var shop = shops.findFirstByOwner_UserId(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("Bạn chưa đăng ký shop."));

        Product p = repo.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        if (p.getShop() == null || !p.getShop().getShopId().equals(shop.getShopId())) {
            throw new SecurityException("Bạn không có quyền thao tác sản phẩm này.");
        }

        // Soft delete
        p.setStatus(3);
        repo.save(p);
    }

    @Override
    @Transactional
    public void restoreOwnerProduct(String userEmail, long productId, int toStatus) {
        if (toStatus < 0 || toStatus > 2) {
            throw new IllegalArgumentException("Trạng thái khôi phục phải là 0/1/2");
        }
        var user = users.findByEmail(userEmail)
                .orElseThrow(() -> new SecurityException("User not found"));
        var shop = shops.findFirstByOwner_UserId(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("Bạn chưa đăng ký shop."));

        Product p = repo.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        if (p.getShop() == null || !p.getShop().getShopId().equals(shop.getShopId())) {
            throw new SecurityException("Bạn không có quyền thao tác sản phẩm này.");
        }

        p.setStatus(toStatus);
        repo.save(p);
    }

    @Override
    @Transactional
    public void bulkUpdateStatus(String userEmail, List<Long> productIds, int status) {
        if (status < 0 || status > 3) throw new IllegalArgumentException("Status phải là 0/1/2/3");
        if (productIds == null || productIds.isEmpty()) return;

        var user = users.findByEmail(userEmail)
                .orElseThrow(() -> new SecurityException("User not found"));
        var shop = shops.findFirstByOwner_UserId(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("Bạn chưa đăng ký shop."));
        Long shopId = shop.getShopId();

        var list = repo.findAllById(productIds);
        if (list.isEmpty()) return;

        list.stream()
                .filter(p -> p.getShop() != null && shopId.equals(p.getShop().getShopId()))
                .forEach(p -> p.setStatus(status));

        repo.saveAll(list);
    }
}