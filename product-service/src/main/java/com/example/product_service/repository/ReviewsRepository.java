package com.example.product_service.repository;

import com.example.product_service.entity.Reviews;
import com.example.product_service.repository.query.ReviewsQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewsRepository extends JpaRepository<Reviews, Long>, ReviewsQueryRepository {
}
