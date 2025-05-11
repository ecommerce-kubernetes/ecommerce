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
    private int cache;
    private int point;
    private LocalDateTime createdAt;
    private List<AddressDto> addresses;
}
