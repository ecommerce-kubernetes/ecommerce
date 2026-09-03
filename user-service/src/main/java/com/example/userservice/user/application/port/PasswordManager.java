package com.example.userservice.user.application.port;

public interface PasswordManager {
    String encrypt(String password);
    boolean matches(String password, String encryptPassword);
}
