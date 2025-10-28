package vn.host.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.host.dto.CategoryNodeDTO;
import vn.host.entity.Category;
import vn.host.repository.CategoryRepository;
import vn.host.service.CategoryService;

import java.util.*;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void save(Category category) {
    }

    @Override
    public void delete(long id) {
    }

    @Override
    public List<Category> findAll() {
        return List.of();
    }

    @Override
    public Category findById(long id) {
        return null;
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