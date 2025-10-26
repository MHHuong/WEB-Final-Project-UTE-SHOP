package vn.host.service;

import vn.host.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> findAll();

    List<Category> findByParent_CategoryId(Long parentId);

    Category findById(Long id);
}
