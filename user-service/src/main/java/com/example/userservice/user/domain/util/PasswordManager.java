package com.example.userservice.user.domain.util;

public interface PasswordManager {
    String encrypt(String password);
    boolean matches(String password, String encryptPassword);
}
