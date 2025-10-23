package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.host.entity.Category;
import vn.host.repository.CategoryRepository;
import vn.host.service.CategoryService;

import java.util.Optional;

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
}
