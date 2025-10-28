package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.host.entity.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long>, JpaSpecificationExecutor<Category> {
    List<Category> findByParent_CategoryId(Long parentId);
    boolean existsByName(String name);
    @Query("SELECT c.name FROM Category c ORDER BY c.name ASC")
    List<String> findAllCategoryNames();
}