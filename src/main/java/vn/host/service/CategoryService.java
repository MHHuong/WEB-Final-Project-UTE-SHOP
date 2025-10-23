package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.host.entity.Category;

import java.util.Optional;

public interface CategoryService {
    Page<Category> findAll(Pageable pageable);
    Optional<Category> findById(Long id);
    Page<Category> searchByName(String keyword, Pageable pageable);
    Category save(Category category);
    void delete(Long id);
}
