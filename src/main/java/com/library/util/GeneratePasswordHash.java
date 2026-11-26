package com.library.util;

public class GeneratePasswordHash {

    public static void main(String[] args) {

        String password = "Admin@12345";

        String hashedPassword =
                PasswordUtil.hashPassword(password);

        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hashedPassword);
    }
}