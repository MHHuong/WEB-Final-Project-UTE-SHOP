package vn.host.service;

import vn.host.entity.Review;

import java.util.List;

public interface ReviewService {
    void save(Review review);
    void delete(long id);
    List<Review> findAll();
    Review findById(long id);
    List<Review> findByProductId(long productId);
}
