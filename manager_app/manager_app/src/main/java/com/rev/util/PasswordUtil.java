package com.rev.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String password, String hash) {

        if (hash == null) return false;

        hash = hash
                .replace("\n", "")
                .replace("\r", "")
                .replace("\"", "")
                .trim();

        return BCrypt.checkpw(password, hash);
    }
}