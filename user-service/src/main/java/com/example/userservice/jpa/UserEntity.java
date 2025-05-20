package com.example.userservice.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
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

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate birthDate;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private int cache;

    @Column(nullable = false)
    private int point;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressEntity> addresses = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public UserEntity(String email, String name, String encryptedPwd,
                      String phoneNumber, Gender gender, LocalDate birthDate,
                      int cache, int point, List<AddressEntity> addresses, Role role) {
        this.email = email;
        this.name = name;
        this.encryptedPwd = encryptedPwd;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.birthDate = birthDate;
        this.cache = cache;
        this.point = point;
        this.addresses = (addresses != null) ? addresses : new ArrayList<>();
        this.role = role;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changePassword(String encryptedPwd) {
        this.encryptedPwd = encryptedPwd;
    }

    public void changePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void changeGender(String gender) {
        this.gender = Gender.valueOf(gender);
    }

    public void changeBirthDate(String birthDate) {
        this.birthDate = LocalDate.parse(birthDate);
    }

    public void rechargeCache(int amount) {
        this.cache += amount;
    }

    public void deductCache(int amount) {
        if (amount > this.cache) {
            throw new IllegalArgumentException("캐시가 부족합니다.");
        }
        this.cache -= amount;
    }

    public void rechargePoint(int amount) {
        this.point += amount;
    }

    public void deductPoint(int amount) {
        if (amount > this.point) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        this.point -= amount;
    }
}
