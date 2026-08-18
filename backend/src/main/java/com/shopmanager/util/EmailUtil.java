package com.shopmanager.util;

import java.util.Locale;

public final class EmailUtil {

    private EmailUtil() {
    }

    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.toLowerCase(Locale.ROOT);
    }
}