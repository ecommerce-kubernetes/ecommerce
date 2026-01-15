package com.example.product_service.repository.query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class ReviewsQueryRepositoryImpl implements ReviewsQueryRepository{

}
