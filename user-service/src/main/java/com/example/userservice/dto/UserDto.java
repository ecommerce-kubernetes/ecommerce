package com.example.userservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@ToString(exclude = "pwd")
public class UserDto {

    private Long id;
    private String email;
    private String pwd;
    private String name;
    private String phoneNumber;
    private String gender;
    private String birthDate;
    private int cache;
    private int point;
    private LocalDateTime createdAt;
    private List<AddressDto> addresses;
}
