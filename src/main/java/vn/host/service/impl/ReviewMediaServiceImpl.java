package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.host.entity.ReviewMedia;
import vn.host.repository.ReviewMediaRepository;
import vn.host.service.ReviewMediaService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewMediaServiceImpl implements ReviewMediaService {
    private final ReviewMediaRepository reviewMediaRepository;

    @Override
    public List<ReviewMedia> findByReview_ReviewId(Long id) {
        return reviewMediaRepository.findByReview_ReviewId(id);
    }

    @Override
    public void save(ReviewMedia review) {
        reviewMediaRepository.save(review);
    }

    @Override
    public void deleteByReview_ReviewId(Long id) {
        reviewMediaRepository.deleteByReview_ReviewId(id);
    }

}
