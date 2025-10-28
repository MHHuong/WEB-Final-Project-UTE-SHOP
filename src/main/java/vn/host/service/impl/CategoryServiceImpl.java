package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.host.dto.CategoryNodeDTO;
import vn.host.entity.Category;
import vn.host.repository.CategoryRepository;
import vn.host.service.CategoryService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Page<Category> searchByName(String keyword, Pageable pageable) {
        return categoryRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    @Override
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
        if (!category.getProducts().isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục này vì đang chứa sản phẩm!");
        }
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategoryNames() {
        return categoryRepository.findAllCategoryNames();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryNodeDTO> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAll(); // Lấy hết từ DB
        Map<Long, CategoryNodeDTO> categoryMap = new HashMap<>();
        List<CategoryNodeDTO> rootCategories = new ArrayList<>();
        for (Category category : allCategories) {
            categoryMap.put(category.getCategoryId(), convertToNodeDTO(category));
        }
        for (Category category : allCategories) {
            CategoryNodeDTO node = categoryMap.get(category.getCategoryId());
            if (category.getParent() != null) {
                CategoryNodeDTO parentNode = categoryMap.get(category.getParent().getCategoryId());
                if (parentNode != null) {
                    parentNode.getChildren().add(node); // Thêm con vào cha
                } else {
                    rootCategories.add(node);
                }
            } else {
                rootCategories.add(node);
            }
        }
        return rootCategories;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getExpandedCategoryIds(Long categoryId) {
        Set<Long> expandedIds = new HashSet<>();
        if (categoryId == null) {
            return expandedIds;
        }
        Category current = categoryRepository.findById(categoryId).orElse(null);
        while (current != null) {
            expandedIds.add(current.getCategoryId());
            current = current.getParent();
        }
        return expandedIds;
    }

    private CategoryNodeDTO convertToNodeDTO(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryNodeDTO(
                category.getCategoryId(),
                category.getName(),
                new ArrayList<>()
        );
    }
    @Override
    @Transactional(readOnly = true)
    public Set<Long> getCategoryAndDescendantIds(Long categoryId) {
        Set<Long> ids = new HashSet<>();
        if (categoryId == null) {
            return ids;
        }

        Queue<Long> queue = new ArrayDeque<>();
        queue.offer(categoryId);

        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            ids.add(currentId);
            List<Category> children = categoryRepository.findByParent_CategoryId(currentId);
            for (Category child : children) {
                if (!ids.contains(child.getCategoryId())) {
                    queue.offer(child.getCategoryId());
                }
            }
        }
        return ids;
    }
    @Override
    @Transactional(readOnly = true)
    public String getCategoryNameById(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .map(Category::getName)
                .orElse(null);
    }
}