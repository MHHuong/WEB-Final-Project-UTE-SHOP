package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.host.dto.CategoryNodeDTO;
import vn.host.entity.Category;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CategoryService {
    Page<Category> findAll(Pageable pageable);

    Optional<Category> findById(Long id);

    Page<Category> searchByName(String keyword, Pageable pageable);

    Category save(Category category);

    void delete(Long id);

    List<String> getAllCategoryNames();

    List<CategoryNodeDTO> getCategoryTree();

    Set<Long> getExpandedCategoryIds(Long categoryId);

    Set<Long> getCategoryAndDescendantIds(Long categoryId);

    String getCategoryNameById(Long categoryId);

    List<Category> findAll();

    List<Category> findByParent_CategoryId(Long parentId);
}
