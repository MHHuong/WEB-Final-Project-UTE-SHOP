package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.host.entity.Product;

interface ProductRepository extends JpaRepository<Product, Long> {
}
