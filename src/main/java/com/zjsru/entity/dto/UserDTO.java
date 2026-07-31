package com.zjsru.entity.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Integer id;
    private String username;
    private String password;
    private String role;

    private String realName;
    private String phone;
    private Boolean status;
}