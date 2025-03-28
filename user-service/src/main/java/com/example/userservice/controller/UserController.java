package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {


        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setPwd(user.getPwd());
        userDto.setName(user.getName());

        UserDto createUserDto = userService.createUser(userDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseUser(createUserDto.getId(), createUserDto.getEmail(), createUserDto.getName())
        );
    }

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers() {
        List<UserDto> userList = userService.getUserByAll();
        List<ResponseUser> result = new ArrayList<>();
        userList.forEach(v -> {
            result.add(new ResponseUser(v.getId(), v.getEmail(), v.getName()));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity getUser(@PathVariable("userId") Long userId) {
        log.info("userId : {}", userId);

        Optional<UserEntity> userEntity = userService.getUserById(userId);

        if (userEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseUser(
                        userEntity.get().getId(),
                        userEntity.get().getEmail(),
                        userEntity.get().getName()
                )
        );
//        //유저 상세 보기를 했을 때, 전체 목록 보기를 보여주기 위한 HATEOAS 기능 추가
//        EntityModel entityModel = EntityModel.of(returnValue);
//        WebMvcLinkBuilder linkTo = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getUsers());
//        entityModel.add(linkTo.withRel("all-users"));
//
//        try {
//            return ResponseEntity.status(HttpStatus.OK).body(entityModel);
//        } catch (Exception ex) {
//            throw new RuntimeException();
//        }
    }
}
