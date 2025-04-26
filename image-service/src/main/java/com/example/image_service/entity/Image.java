package com.example.image_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileLocation;
    private String imageUrl;
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createAt;

    public Image(String fileLocation, String imageUrl){
        this.fileLocation = fileLocation;
        this.imageUrl = imageUrl;
    }
}
