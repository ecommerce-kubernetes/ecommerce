package com.example.userservice.jpa;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

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

    @Column(nullable = false, unique = true)
    private String encryptedPwd;

    @CreatedDate
    @Column(updatable = false)
    private Date createAt;

    @Builder
    public UserEntity(String email, String name, String encryptedPwd) {
        this.email = email;
        this.name = name;
        this.encryptedPwd = encryptedPwd;
    }
}
