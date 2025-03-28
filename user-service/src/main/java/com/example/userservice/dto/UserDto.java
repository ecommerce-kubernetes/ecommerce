package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
public class UserDto {

    private Long id;
    private String email;
    private String pwd;
    private String name;
    private Date createAt;
}
