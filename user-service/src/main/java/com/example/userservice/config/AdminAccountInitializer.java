package com.example.userservice.config;

import com.example.userservice.jpa.UserRepository;
import com.example.userservice.jpa.entity.Gender;
import com.example.userservice.jpa.entity.Role;
import com.example.userservice.jpa.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String adminEmail = "admin@example.com";

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            UserEntity admin = UserEntity.builder()
                    .email(adminEmail)
                    .name("admin")
                    .encryptedPwd(passwordEncoder.encode("Admin1234!"))
                    .phoneNumber("01000000000")
                    .phoneVerified(true)
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1999, 4, 13))
                    .cash(0)
                    .point(0)
                    .role(Role.ROLE_ADMIN)
                    .build();

            userRepository.save(admin);
            System.out.println("관리자 계정 생성 완료: " + adminEmail);
        }
    }
}
