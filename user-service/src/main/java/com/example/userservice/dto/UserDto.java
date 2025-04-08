package com.example.userservice.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserDto {

    private Long id;
    private String email;
    private String pwd;
    private String name;
    private LocalDate createAt;
}
