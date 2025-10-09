package vn.host.service;

import vn.host.entity.ReviewMedia;

import java.util.List;

public interface ReviewMediaService {
    void save(ReviewMedia reviewMedia);
    void delete(long id);
    List<ReviewMedia> findAll();
    ReviewMedia findById(long id);
    List<ReviewMedia> findByProductId(long productId);
}
