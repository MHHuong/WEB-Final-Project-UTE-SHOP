package vn.host.service;

import vn.host.entity.Category;

import java.util.List;

public interface CategoryService {
    void save(Category category);
    void delete(long id);
    List<Category> findAll();
    Category findById(long id);
    List<String> getAllCategoryNames();
}