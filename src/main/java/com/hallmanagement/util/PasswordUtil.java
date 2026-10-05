package com.hallmanagement.util;

public final class PasswordUtil {

    private PasswordUtil() {}

    public static String hashPassword(String plainPassword) {

        return plainPassword;
    }

    public static boolean checkPassword(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) return false;
        return plainPassword.equals(storedPassword);
    }
}
