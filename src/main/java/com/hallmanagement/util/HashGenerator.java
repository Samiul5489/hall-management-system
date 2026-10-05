package com.hallmanagement.util;

public class HashGenerator {

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println("  BCrypt Hash Generator — Hall Management    ");
        System.out.println("==============================================\n");

        String[] passwordsToHash = {
            "student123",
            "provost123",
            "inactive123",
            "admin123",
            "myNewPassword"
        };

        System.out.println("Generated BCrypt Hashes:");
        System.out.println("──────────────────────────────────────────────");

        for (String password : passwordsToHash) {
            String hash = PasswordUtil.hashPassword(password);
            System.out.println("Password : " + password);
            System.out.println("Hash     : " + hash);
            System.out.println();
        }

        System.out.println("──────────────────────────────────────────────");
        System.out.println("HOW TO USE:");
        System.out.println("Copy the Hash value and paste it into the");
        System.out.println("password_hash column in phpMyAdmin.");
        System.out.println("==============================================");

        System.out.println("\nVerification Test:");
        System.out.println("──────────────────────────────────────────────");
        verify("student123",  "$2a$12$0nP0LKjVLkqRrPZlB.3m0.fX4YhzWlNmOE9aWDqdTJJ4Uflx3Qf5e");
        verify("provost123",  "$2a$12$uSxP8Y5aAfX3kV1Qz.Bs2u1vGPuAXGRuSZjlOjMDWpBdFKl.nVBwS");
        verify("inactive123", "$2a$12$KwdPKGzPqRxJg3.Rv1Z5IOtNJv5j3y6RFhOYGVKkfJL1mKHscEQbG");
    }

    private static void verify(String password, String storedHash) {
        boolean ok = PasswordUtil.checkPassword(password, storedHash);
        System.out.println("'" + password + "' matches stored hash → " + (ok ? "✅ YES" : "❌ NO"));
    }
}
