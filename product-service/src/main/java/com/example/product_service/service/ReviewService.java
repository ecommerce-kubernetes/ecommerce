package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.entity.Review;
import com.example.product_service.exception.NoPermissionException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.ReviewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.example.product_service.common.MessagePath.REVIEW_FORBIDDEN_DELETE;
import static com.example.product_service.common.MessagePath.REVIEW_NOT_FOUND;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewsRepository reviewsRepository;
    private final MessageSourceUtil ms;

    public void deleteReviewById(Long reviewId, Long userId) {
        Review review = findWithVariantByIdOrThrow(reviewId);

        if(!Objects.equals(review.getUserId(), userId)){
            throw new NoPermissionException(ms.getMessage(REVIEW_FORBIDDEN_DELETE));
        }

        review.getProductVariant().deleteReview(review);
    }

    private Review findWithVariantByIdOrThrow(Long reviewId){
        return reviewsRepository.findWithVariantById(reviewId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(REVIEW_NOT_FOUND)));
    }
}
