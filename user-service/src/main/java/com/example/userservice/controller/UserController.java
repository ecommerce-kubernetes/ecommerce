package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome";
    }

    //회원가입
    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {

        UserDto userDto = UserDto.builder()
                .email(user.getEmail())
                .pwd(user.getPwd())
                .name(user.getName())
                .build();

        UserEntity createUser = userService.createUser(userDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseUser(createUser.getId(), createUser.getEmail(), createUser.getName(), createUser.getCreateAt())
        );
    }

    //전체 유저 조회
    @GetMapping("/users")
    public ResponseEntity<Page<ResponseUser>> getUsers(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<UserDto> userPage = userService.getUserByAll(pageable);

        Page<ResponseUser> result = userPage.map(v -> new ResponseUser(
                v.getId(),
                v.getEmail(),
                v.getName(),
                v.getCreateAt()
        ));

        return ResponseEntity.ok(result);
    }

    //유저아이디로 유저 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity getUser(@PathVariable("userId") Long userId) {

        UserDto userDto = userService.getUserById(userId);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseUser(
                        userDto.getId(),
                        userDto.getEmail(),
                        userDto.getName(),
                        userDto.getCreateAt(),
                        userDto.getAddresses()
                )
        );
    }
}
