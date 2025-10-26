package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.host.entity.Review;
import vn.host.repository.ReviewRepository;
import vn.host.service.ReviewService;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepo;

    @Override
    public Page<Review> findAll(Specification<Review> spec, Pageable pageable) {
        return reviewRepo.findAll(spec, pageable);
    }
}
