package com.example.userservice.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;


@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUser {
    private Long userId;
    private String email;
    private String name;
    private LocalDate createAt;
    private List<String> addresses;

    public ResponseUser(Long userId, String email, String name, LocalDate createAt) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.createAt = createAt;
    }
}
