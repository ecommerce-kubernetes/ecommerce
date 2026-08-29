package com.example.userservice.user.domain;

import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.common.util.TsidGenerator;
import com.example.userservice.user.domain.context.CreateUserContext;
import com.example.userservice.user.domain.util.PasswordManager;
import com.example.userservice.user.domain.vo.Gender;

import java.time.LocalDate;

public class UserFixtureBuilder {

    private static final IdGenerator ID_GENERATOR = new TsidGenerator();
    private static final PasswordManager PASSWORD_MANAGER = new FixturePasswordManager();

    private String email = "la9814@naver.com";
    private String password = "password1234*";
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

    public User build() {
        CreateUserContext context = CreateUserContext.builder()
                .email(email)
                .password(password)
                .name(name)
                .birthDate(birthDate)
                .gender(gender)
                .phoneNumber(phoneNumber)
                .build();

        return User.create(context, PASSWORD_MANAGER, ID_GENERATOR);
    }

    private static class FixturePasswordManager implements PasswordManager {
        @Override
        public String encrypt(String password) {
            return "encrypted:" + password;
        }

        @Override
        public boolean matches(String password, String encryptPassword) {
            return encrypt(password).equals(encryptPassword);
        }
    }
}
