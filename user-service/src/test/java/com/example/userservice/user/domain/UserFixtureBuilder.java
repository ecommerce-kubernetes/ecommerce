package com.example.userservice.user.domain;

import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.user.domain.context.CreateUserContext;
import com.example.userservice.user.domain.vo.Gender;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

public class UserFixtureBuilder {

    private static final AtomicLong idSeq = new AtomicLong(100L);
    private static final IdGenerator ID_GENERATOR = idSeq::getAndIncrement;

    private String email = "la9814@naver.com";
    private String encryptedPassword = "encrypted:password1234*";
    private String name = "김이박";
    private LocalDate birthDate = LocalDate.of(1999, 12, 25);
    private Gender gender = Gender.MALE;
    private String phoneNumber = "010-1234-5678";

    public static UserFixtureBuilder given() {
        return new UserFixtureBuilder();
    }

    public UserFixtureBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserFixtureBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public UserFixtureBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public UserFixtureBuilder withEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
        return this;
    }

    public User build() {
        CreateUserContext context = CreateUserContext.builder()
                .id(ID_GENERATOR.generate())
                .email(email)
                .encryptedPassword(encryptedPassword)
                .name(name)
                .birthDate(birthDate)
                .gender(gender)
                .phoneNumber(phoneNumber)
                .build();

        return User.create(context);
    }
}
