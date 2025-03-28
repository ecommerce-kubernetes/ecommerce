package com.example.userservice.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;


@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUser {
    private Long userId;
    private String email;
    private String name;

//    private List<ResponseOrder> orders;
}
