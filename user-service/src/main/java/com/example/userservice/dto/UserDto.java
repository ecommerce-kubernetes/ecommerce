package com.example.userservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class UserDto {

    private Long id;
    private String email;
    private String pwd;
    private String name;
    private LocalDateTime createAt;
    private List<String> addresses;
}
