package com.example.userservice.api.user.domain.model;

import com.example.userservice.api.common.entity.BaseEntity;
import com.example.userservice.api.user.service.dto.command.UserCreateCommand;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String name;
    private String encryptedPwd;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
    private LocalDate birthDate;
    private int point;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressEntity> addresses = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    public User(String email, String name, String encryptedPwd, String phoneNumber, Gender gender, LocalDate birthDate, int point, Role role) {
        this.email = email;
        this.name = name;
        this.encryptedPwd = encryptedPwd;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.birthDate = birthDate;
        this.point = point;
        this.role = role;
    }

    public static User createUser(UserCreateCommand command, String encryptedPwd) {
        return User.builder()
                .email(command.getEmail())
                .name(command.getName())
                .encryptedPwd(encryptedPwd)
                .gender(command.getGender())
                .birthDate(command.getBirthDate())
                .point(0)
                .phoneNumber(command.getPhoneNumber())
                .role(Role.ROLE_USER)
                .build();
    }
}
