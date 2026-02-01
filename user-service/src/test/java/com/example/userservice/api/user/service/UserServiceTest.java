package com.example.userservice.api.user.service;

import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.api.common.exception.UserErrorCode;
import com.example.userservice.api.support.ExcludeInfraTest;
import com.example.userservice.api.user.domain.model.User;
import com.example.userservice.api.user.domain.repository.UserRepository;
import com.example.userservice.api.user.service.dto.command.UserCreateCommand;
import com.example.userservice.api.user.service.dto.result.UserCreateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static com.example.userservice.api.support.fixture.UserCommandFixture.anUserCreateCommand;
import static com.example.userservice.api.support.fixture.UserResponseFixture.anUserCreateResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
public class UserServiceTest extends ExcludeInfraTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("회원 생성")
    class Create {

        @Test
        @DisplayName("회원을 생성한다")
        void createUser(){
            //given
            UserCreateCommand command = anUserCreateCommand().build();
            UserCreateResponse expectedResult = anUserCreateResponse().build();
            //when
            UserCreateResponse result = userService.createUser(command);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(expectedResult);

            User user = userRepository.findById(result.getId()).orElseThrow();
            // 비밀번호가 암호화 되어 저장되었는지 검증
            assertThat(user.getEncryptedPwd()).isNotEqualTo(command.getPassword());
            assertThat(user.getEncryptedPwd()).startsWith("$2a$");
            assertThat(passwordEncoder.matches(command.getPassword(), user.getEncryptedPwd())).isTrue();
        }

        @Test
        @DisplayName("동일한 이메일의 회원은 생성할 수 없다")
        void createUser_duplicate_email(){
            //given
            UserCreateCommand command = anUserCreateCommand().email("test@email.com").build();
            User user = User.createUser(command, "$2a$asdfasdcxzvcx");
            userRepository.save(user);
            //when
            //then
            assertThatThrownBy(() -> userService.createUser(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(UserErrorCode.USER_NOT_FOUND);
        }
    }
}
