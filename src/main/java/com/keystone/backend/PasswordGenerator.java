package com.keystone.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "temporary-password";

        String hashedPassword = encoder.encode(password);

        System.out.println(hashedPassword);
    }
}