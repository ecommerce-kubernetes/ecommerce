package com.example.userservice.user.domain;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.entity.BaseEntity;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.user.domain.context.CreateShippingAddressContext;
import com.example.userservice.user.domain.context.CreateUserContext;
import com.example.userservice.user.domain.util.PasswordManager;
import com.example.userservice.user.domain.vo.Gender;
import com.example.userservice.user.domain.vo.Role;
import com.example.userservice.user.exception.UserErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    private Long id;

    private String email;

    private String name;

    private String encryptedPwd;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    private LocalDate birthDate;

    private Money point;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShippingAddress> shippingAddresses = new ArrayList<>();

    @Version
    private Long version;

    @Builder(access = AccessLevel.PRIVATE)
    public User(Long id, String email, String name, String encryptedPwd, String phoneNumber, Gender gender, LocalDate birthDate, Money point, Role role) {
        Assert.notNull(id, "유저를 생성할때 아이디는 필수이다.");
        Assert.hasText(email, "유저를 생성할때 이메일은 필수이다.");
        Assert.hasText(name, "유저를 생성할때 이름은 필수이다.");
        Assert.hasText(encryptedPwd, "유저를 생성할때 암호화된 비밀번호는 필수이다.");
        Assert.hasText(phoneNumber, "유저를 생성할때 전화번호는 필수이다.");
        Assert.notNull(gender, "유저를 생성할때 성별은 필수이다.");
        Assert.notNull(birthDate, "유저를 생성할때 생년월일은 필수이다.");
        Assert.notNull(point, "유저를 생성할때 포인트는 필수이다.");
        Assert.notNull(role, "유저를 생성할때 권한은 필수이다.");

        this.id = id;
        this.email = email;
        this.name = name;
        this.encryptedPwd = encryptedPwd;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.birthDate = birthDate;
        this.point = point;
        this.role = role;
    }

    public static User create(CreateUserContext context, PasswordManager passwordManager, IdGenerator idGenerator) {
        Long id = idGenerator.generate();
        String encryptPassword = passwordManager.encrypt(context.password());

        return User.builder()
                .id(id)
                .email(context.email())
                .name(context.name())
                .encryptedPwd(encryptPassword)
                .gender(context.gender())
                .birthDate(context.birthDate())
                .point(Money.ZERO)
                .phoneNumber(context.phoneNumber())
                .role(Role.ROLE_USER)
                .build();
    }

    public void authenticate(String password, PasswordManager passwordManager) {
        if (!passwordManager.matches(password, this.encryptedPwd)) {
            throw new BusinessException(UserErrorCode.PASSWORD_NOT_MATCH);
        }
    }

    public void addShippingAddress(CreateShippingAddressContext context, IdGenerator idGenerator) {
        boolean shouldBeDefault = context.isDefault() ||
                this.shippingAddresses.isEmpty() ||
                this.shippingAddresses.stream().noneMatch(ShippingAddress::isDefault);

        if (shouldBeDefault) {
            ShippingAddress currentDefault = getDefaultShippingAddress();
            if (currentDefault != null) {
                currentDefault.demoteFromDefault();
            }
        }

        ShippingAddress shippingAddress = ShippingAddress.create(context, idGenerator, shouldBeDefault);
        addShippingAddress(shippingAddress);
    }

    public void removeShippingAddress(Long shippingAddressId) {
        ShippingAddress addressToRemove = this.shippingAddresses.stream()
                .filter(address -> address.getId().equals(shippingAddressId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(UserErrorCode.SHIPPING_ADDRESS_NOT_FOUND));

        boolean wasDefault = addressToRemove.isDefault();

        this.shippingAddresses.remove(addressToRemove);

        if (wasDefault && !this.shippingAddresses.isEmpty()) {
            ShippingAddress newDefaultAddress = this.shippingAddresses.getFirst();
            newDefaultAddress.promoteToDefault();
        }
    }

    public ShippingAddress getDefaultShippingAddress() {
        if (this.shippingAddresses.isEmpty()) {
            return null;
        }
        return this.shippingAddresses.stream().filter(ShippingAddress::isDefault).findFirst().orElse(null);
    }

    public void addPoints(Money addPoints) {
        this.point = this.point.add(addPoints);
    }

    public void deductPoints(Money deductPoint) {
        if (this.point.isLessThan(deductPoint)) {
            throw new BusinessException(UserErrorCode.INSUFFICIENT_POINTS);
        }
        this.point = this.point.subtract(deductPoint);
    }

    private void addShippingAddress(ShippingAddress shippingAddress) {
        shippingAddress.setUser(this);
        this.shippingAddresses.add(shippingAddress);
    }
}
