package com.example.userservice.jpa;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "users")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String encryptedPwd;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private int cache;

    @Column(nullable = false)
    private int point;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressEntity> addresses = new ArrayList<>();

    @Builder
    public UserEntity(String email, String name, String encryptedPwd, int cache, int point, List<AddressEntity> addresses) {
        this.email = email;
        this.name = name;
        this.encryptedPwd = encryptedPwd;
        this.cache = cache;
        this.point = point;
        this.addresses = addresses;
    }

    public void rechargeCache(int amount) {
        this.cache += amount;
    }

    public void deductCache(int amount) {
        this.cache -= amount;
    }

    public void rechargePoint(int amount) {
        this.point += amount;
    }

    public void deductPoint(int amount) {
        this.point -= amount;
    }
}
