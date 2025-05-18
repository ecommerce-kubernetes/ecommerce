package com.example.userservice.jpa;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(name = "address")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column
    private String details;

    @Column(nullable = false)
    private boolean defaultAddress;

    public void changeName(String name) {
        this.name = name;
    }

    public void changeAddress(String address) {
        this.address = address;
    }

    public void changeDetails(String details) {
        this.details = details;
    }

    public void changeDefaultAddress(boolean isDefault) {
        this.defaultAddress = isDefault;
    }

    @Builder
    public AddressEntity(UserEntity user, String name, String address, String details, boolean defaultAddress) {
        this.user = user;
        this.name = name;
        this.address = address;
        this.details = details;
        this.defaultAddress = defaultAddress;
    }
}
