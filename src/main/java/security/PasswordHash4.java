package security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Salted PBKDF2 hashes stored in tblMember4.password, compatible with JDK 8. */
public final class PasswordHash4 {
    private static final int ITERATIONS = 600000;
    private static final SecureRandom RANDOM = new SecureRandom();
    private PasswordHash4() { }

    public static String hash(String password) {
        if (password == null || password.isEmpty() || password.length() > 1024) {
            throw new IllegalArgumentException("Password must contain 1-1024 characters");
        }
        byte[] salt = new byte[16]; RANDOM.nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);
        return "pbkdf2-sha256$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(derived);
    }

    public static boolean verify(String password, String encoded) {
        if (password == null || password.isEmpty() || password.length() > 1024 || encoded == null) return false;
        try {
            String[] parts = encoded.split("\\$", -1);
            if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) return false;
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 10000 || iterations > 2000000) return false;
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            if (salt.length < 16 || salt.length > 64 || expected.length != 32) return false;
            return MessageDigest.isEqual(expected, derive(password, salt, iterations));
        } catch (IllegalArgumentException e) { return false; }
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2WithHmacSHA256 is required", e);
        } finally { spec.clearPassword(); Arrays.fill(chars, '\0'); }
    }
}
