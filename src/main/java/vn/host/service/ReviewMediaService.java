package vn.host.service;

import vn.host.entity.ReviewMedia;

import java.util.List;

public interface ReviewMediaService {
    List<ReviewMedia> findByReview_ReviewId(Long id);

    void save(ReviewMedia review);

    void deleteByReview_ReviewId(Long id);
}
