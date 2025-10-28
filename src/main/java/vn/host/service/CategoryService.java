package vn.host.service;

import vn.host.dto.CategoryNodeDTO;
import vn.host.entity.Category;

import java.util.List;
import java.util.Set;

public interface CategoryService {
    void save(Category category);
    void delete(long id);
    List<Category> findAll();
    Category findById(long id);
    List<String> getAllCategoryNames();
    List<CategoryNodeDTO> getCategoryTree();
    Set<Long> getExpandedCategoryIds(Long categoryId);
    Set<Long> getCategoryAndDescendantIds(Long categoryId);
    String getCategoryNameById(Long categoryId);
}