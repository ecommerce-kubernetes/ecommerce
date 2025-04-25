package com.example.image_service.repository;

import com.example.image_service.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByImageUrl(String imageUrl);
}
