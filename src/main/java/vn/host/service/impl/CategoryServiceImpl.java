package vn.host.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.host.entity.Category;
import vn.host.repository.CategoryRepository;
import vn.host.service.CategoryService;

import java.util.List;

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
        // Gọi phương thức @Query mà bạn đã thêm vào CategoryRepository
        return categoryRepository.findAllCategoryNames();
    }
}